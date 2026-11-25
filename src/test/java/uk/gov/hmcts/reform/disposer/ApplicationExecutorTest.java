package uk.gov.hmcts.reform.disposer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.disposer.domain.DisposalRunResult;
import uk.gov.hmcts.reform.disposer.domain.PaymentDeletionResult;
import uk.gov.hmcts.reform.disposer.exception.PersistentDeletionException;
import uk.gov.hmcts.reform.disposer.service.PaymentDisposerService;

import java.util.List;

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

        verify(paymentDisposerService).processClosedCases();
    }

    @Test
    void runPropagatesPersistentDeletionFailures() {
        ReflectionTestUtils.setField(applicationExecutor, "isServiceEnabled", true);
        DisposalRunResult runResult = new DisposalRunResult(
            List.of(),
            List.of(PaymentDeletionResult.failure("CASE-1", "payments", 3, "timeout"))
        );
        when(paymentDisposerService.processClosedCases())
            .thenThrow(new PersistentDeletionException("failed", runResult));

        assertThatExceptionOfType(PersistentDeletionException.class)
            .isThrownBy(() -> applicationExecutor.run(new DefaultApplicationArguments()))
            .withMessage("failed");
    }

    @Test
    void runSkipsPaymentDisposerWhenServiceDisabled() {
        ReflectionTestUtils.setField(applicationExecutor, "isServiceEnabled", false);

        applicationExecutor.run(new DefaultApplicationArguments());

        verifyNoInteractions(paymentDisposerService);
    }
}
