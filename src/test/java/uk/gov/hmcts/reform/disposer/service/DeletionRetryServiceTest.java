package uk.gov.hmcts.reform.disposer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.disposer.config.BackoffSleeper;
import uk.gov.hmcts.reform.disposer.config.DeletionProperties;
import uk.gov.hmcts.reform.disposer.domain.PaymentDeletionResult;
import uk.gov.hmcts.reform.disposer.exception.FeeAndPayClientException;

@ExtendWith(MockitoExtension.class)
class DeletionRetryServiceTest {

    @Mock
    private DeletionRetryService.CaseDeletion deletion;

    @Mock
    private BackoffSleeper backoffSleeper;

    private DeletionRetryService deletionRetryService;

    @BeforeEach
    void setUp() {
        DeletionProperties properties = new DeletionProperties(
            new DeletionProperties.Retry(3, 10L, 2.0),
            new DeletionProperties.Audit("disposer-fee-and-pay")
        );
        deletionRetryService = new DeletionRetryService(properties, backoffSleeper);
    }

    @Test
    void deleteWithRetrySucceedsOnFirstAttempt() throws Exception {
        PaymentDeletionResult result = deletionRetryService.deleteWithRetry("CASE-1", "payments", deletion);

        assertThat(result.success()).isTrue();
        assertThat(result.paymentId()).isEqualTo("payments");
        assertThat(result.attempts()).isEqualTo(1);
        verify(deletion).delete();
        verify(backoffSleeper, times(0)).sleep(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void deleteWithRetrySucceedsOnSecondAttempt() throws Exception {
        doThrow(new FeeAndPayClientException("transient"))
            .doNothing()
            .when(deletion).delete();

        PaymentDeletionResult result = deletionRetryService.deleteWithRetry("CASE-1", "payments", deletion);

        assertThat(result.success()).isTrue();
        assertThat(result.attempts()).isEqualTo(2);
        verify(deletion, times(2)).delete();
        verify(backoffSleeper).sleep(10L);
    }

    @Test
    void deleteWithRetryFailsAfterMaxAttempts() throws Exception {
        doThrow(new FeeAndPayClientException("persistent"))
            .when(deletion).delete();

        PaymentDeletionResult result = deletionRetryService.deleteWithRetry("CASE-1", "payments", deletion);

        assertThat(result.success()).isFalse();
        assertThat(result.attempts()).isEqualTo(3);
        assertThat(result.errorMessage()).contains("persistent");
        verify(deletion, times(3)).delete();
        verify(backoffSleeper, times(2)).sleep(org.mockito.ArgumentMatchers.anyLong());
    }
}
