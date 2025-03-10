package uk.gov.hmcts.reform.disposer;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.disposer.service.ServiceTokenGenerator;

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
