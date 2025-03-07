package uk.gov.hmcts.reform.disposer;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.disposer.service.ServiceTokenGenerator;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class SampleFunctionalTest {

    private final ServiceTokenGenerator serviceTokenGenerator;

    @Test
    void itShouldGenerateServiceToken() {
        serviceTokenGenerator.generateToken();
        assertThat(serviceTokenGenerator.getServiceToken()).startsWith("Bearer eyJhbGciOiJIUzUxMiJ9");
    }
}
