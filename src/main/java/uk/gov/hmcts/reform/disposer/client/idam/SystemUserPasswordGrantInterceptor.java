package uk.gov.hmcts.reform.disposer.client.idam;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.regex.Pattern;

public class SystemUserPasswordGrantInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long EXPIRY_SKEW_SECONDS = 60;

    private final ClientRegistration clientRegistration;
    private final String username;
    private final String password;
    private final Pattern matchesPattern;
    private final RestClient restClient;

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public SystemUserPasswordGrantInterceptor(
        ClientRegistration clientRegistration,
        String username,
        String password,
        String endpointRegex
    ) {
        this(clientRegistration, username, password, endpointRegex, RestClient.create());
    }

    SystemUserPasswordGrantInterceptor(
        ClientRegistration clientRegistration,
        String username,
        String password,
        String endpointRegex,
        RestClient restClient
    ) {
        this.clientRegistration = clientRegistration;
        this.username = username;
        this.password = password;
        this.matchesPattern = Pattern.compile(endpointRegex);
        this.restClient = restClient;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (template.url() != null && matchesPattern.matcher(template.url()).find()) {
            template.header(AUTHORIZATION_HEADER, BEARER_PREFIX + getAccessToken());
        }
    }

    private synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(EXPIRY_SKEW_SECONDS))) {
            return cachedToken;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", username);
        form.add("password", password);
        form.add("client_id", clientRegistration.getClientId());
        form.add("client_secret", clientRegistration.getClientSecret());
        form.add("scope", String.join(" ", clientRegistration.getScopes()));

        IdamTokenResponse tokenResponse = restClient.post()
            .uri(clientRegistration.getProviderDetails().getTokenUri())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(IdamTokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new IllegalStateException(
                "Password grant failed for client " + clientRegistration.getRegistrationId()
            );
        }

        cachedToken = tokenResponse.accessToken();
        long expiresIn = tokenResponse.expiresIn() == null ? 3600 : tokenResponse.expiresIn();
        expiresAt = Instant.now().plusSeconds(expiresIn);
        return cachedToken;
    }
}
