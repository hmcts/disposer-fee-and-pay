package uk.gov.hmcts.reform.disposer.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.disposer.exception.ServiceTokenGenerationException;

@ExtendWith(MockitoExtension.class)
class ServiceTokenGeneratorTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private ServiceTokenGenerator serviceTokenGenerator;

    @Test
    void generateTokenGeneratesToken() {
        when(authTokenGenerator.generate()).thenReturn("Bearer generated-token");

        String token = serviceTokenGenerator.generateToken();

        assertThat(token).isEqualTo("Bearer generated-token");
        assertThat(serviceTokenGenerator.getServiceToken()).isEqualTo("Bearer generated-token");
        verify(authTokenGenerator, times(1)).generate();
    }

    @Test
    void generateTokenThrowsException() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException("test exception"));
        assertThatExceptionOfType(ServiceTokenGenerationException.class)
            .isThrownBy(() -> serviceTokenGenerator.generateToken())
            .withMessage("Failed to generate service token - test exception");
    }
}
