package uk.gov.hmcts.reform.disposer.client.idam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SystemUserPasswordGrantInterceptorTest {

    private MockRestServiceServer mockServer;
    private SystemUserPasswordGrantInterceptor interceptor;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ClientRegistration registration = ClientRegistration.withRegistrationId("ccd-data-store")
            .clientId("disposer_fee_and_pay")
            .clientSecret("client-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(new AuthorizationGrantType("password"))
            .scope("openid", "profile", "roles")
            .tokenUri("http://localhost/o/token")
            .build();
        interceptor = new SystemUserPasswordGrantInterceptor(
            registration,
            "disposer@test.com",
            "password",
            "/internal/searchCases/getClosedCases.*",
            builder.build()
        );
    }

    @Test
    void applyAddsBearerTokenForMatchingCcdUrl() {
        mockServer.expect(requestTo("http://localhost/o/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andRespond(withSuccess(
                "{\"access_token\":\"idam-access-token\",\"expires_in\":3600,\"token_type\":\"Bearer\"}",
                MediaType.APPLICATION_JSON
            ));

        RequestTemplate template = new RequestTemplate();
        template.uri("/internal/searchCases/getClosedCases/2019-01-15");

        interceptor.apply(template);

        assertThat(template.headers().get("Authorization")).containsExactly("Bearer idam-access-token");
        mockServer.verify();
    }

    @Test
    void applyDoesNotAddTokenForUnrelatedUrl() {
        RequestTemplate template = new RequestTemplate();
        template.uri("/lease");

        interceptor.apply(template);

        assertThat(template.headers().get("Authorization")).isNull();
        mockServer.verify();
    }
}
