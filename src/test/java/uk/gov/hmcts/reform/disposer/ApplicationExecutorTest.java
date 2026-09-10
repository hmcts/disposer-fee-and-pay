package uk.gov.hmcts.reform.disposer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.disposer.service.PaymentDisposerService;

@ExtendWith(MockitoExtension.class)
class ApplicationExecutorTest {

    @Mock
    private PaymentDisposerService paymentDisposerService;

    private ApplicationExecutor applicationExecutor;

    @BeforeEach
    void setUp() {
        applicationExecutor = new ApplicationExecutor(paymentDisposerService);
    }

    @Test
    void runInvokesPaymentDisposerWhenServiceEnabled() {
        ReflectionTestUtils.setField(applicationExecutor, "isServiceEnabled", true);

        applicationExecutor.run(new DefaultApplicationArguments());

        verify(paymentDisposerService).run();
    }

    @Test
    void runSkipsPaymentDisposerWhenServiceDisabled() {
        ReflectionTestUtils.setField(applicationExecutor, "isServiceEnabled", false);

        applicationExecutor.run(new DefaultApplicationArguments());

        verifyNoInteractions(paymentDisposerService);
    }
}
