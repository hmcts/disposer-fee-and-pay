package uk.gov.hmcts.reform.disposer.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.gov.hmcts.reform.disposer.client.idam.SystemUserPasswordGrantInterceptor;

/**
 * Boot 4 / Spring Security 7 removed OAuth2AuthorizedClientProviderBuilder.password(),
 * so idam-legacy-auth-support cannot create its password-grant interceptor.
 * This uses the same YAML contract and attaches the system-user token to matching Feign calls.
 */
@Configuration
@ConditionalOnProperty(prefix = "idam.legacy.password-grant", name = "registration-reference")
public class PasswordGrantFeignConfiguration {

    @Bean
    public RequestInterceptor passwordGrantRequestInterceptor(
        ClientRegistrationRepository clientRegistrationRepository,
        @Value("${idam.legacy.password-grant.registration-reference}") String registrationReference,
        @Value("${idam.legacy.password-grant.service-account.email-address}") String username,
        @Value("${idam.legacy.password-grant.service-account.password}") String password,
        @Value("${idam.legacy.password-grant.endpoint-regex}") String endpointRegex
    ) {
        ClientRegistration clientRegistration =
            clientRegistrationRepository.findByRegistrationId(registrationReference);
        if (clientRegistration == null) {
            throw new IllegalStateException(
                "OAuth2 client registration not found: " + registrationReference
            );
        }
        return new SystemUserPasswordGrantInterceptor(clientRegistration, username, password, endpointRegex);
    }
}
