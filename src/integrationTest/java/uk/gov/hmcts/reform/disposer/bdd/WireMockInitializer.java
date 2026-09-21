package uk.gov.hmcts.reform.disposer.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();

        applicationContext
            .getBeanFactory()
            .registerSingleton("wireMockServer", wireMockServer);

        applicationContext.addApplicationListener(applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
                wireMockServer.stop();
            }
        });

        String wireMockBaseUrl = "http://localhost:" + wireMockServer.port();
        TestPropertyValues
            .of(
                "baseUrl=" + wireMockBaseUrl,
                "ccd.data-store.url=" + wireMockBaseUrl,
                "idam.s2s-auth.url=" + wireMockBaseUrl,
                "spring.security.oauth2.client.provider.ccd-data-store.token-uri=" + wireMockBaseUrl + "/o/token",
                "spring.security.oauth2.client.registration.ccd-data-store.client-secret=integration-client-secret",
                "idam.legacy.password-grant.service-account.email-address=disposer@test.com",
                "idam.legacy.password-grant.service-account.password=password",
                "service.enabled=false",
                "service.ttl-years=7"
            )
            .applyTo(applicationContext);
    }

}
