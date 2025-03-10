package uk.gov.hmcts.reform.disposer.service;


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
        serviceTokenGenerator.generateToken();
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
