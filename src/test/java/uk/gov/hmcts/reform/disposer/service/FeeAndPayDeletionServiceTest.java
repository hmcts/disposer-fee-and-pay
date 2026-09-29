package uk.gov.hmcts.reform.disposer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.disposer.client.bulkscan.BulkScanningClient;
import uk.gov.hmcts.reform.disposer.client.payments.PaymentsClient;
import uk.gov.hmcts.reform.disposer.client.refunds.RefundsClient;
import uk.gov.hmcts.reform.disposer.domain.DisposalRunResult;
import uk.gov.hmcts.reform.disposer.domain.PaymentDeletionResult;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class FeeAndPayDeletionServiceTest {

    @Mock
    private RefundsClient refundsClient;

    @Mock
    private BulkScanningClient bulkScanningClient;

    @Mock
    private PaymentsClient paymentsClient;

    @Mock
    private DeletionRetryService deletionRetryService;

    @Mock
    private DeletionAuditService deletionAuditService;

    @InjectMocks
    private FeeAndPayDeletionService feeAndPayDeletionService;

    @Test
    void deletePaymentsForCasesDeletesRefundsBulkScanThenPayments() {
        stubSuccess("CASE-1", "refunds");
        stubSuccess("CASE-1", "bulk-scan");
        stubSuccess("CASE-1", "payments");

        DisposalRunResult result = feeAndPayDeletionService.deletePaymentsForCases(List.of("CASE-1"));

        assertThat(result.status()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.successes()).hasSize(3);
        verify(deletionAuditService).recordSuccessfulDeletion("CASE-1", "refunds");
        verify(deletionAuditService).recordSuccessfulDeletion("CASE-1", "bulk-scan");
        verify(deletionAuditService).recordSuccessfulDeletion("CASE-1", "payments");
    }

    @Test
    void deletePaymentsForCasesCollectsFailuresFromAnyApi() {
        stubSuccess("CASE-1", "refunds");
        stubSuccess("CASE-1", "bulk-scan");
        when(deletionRetryService.deleteWithRetry(eq("CASE-1"), eq("payments"), any()))
            .thenReturn(PaymentDeletionResult.failure("CASE-1", "payments", 3, "server error"));

        DisposalRunResult result = feeAndPayDeletionService.deletePaymentsForCases(List.of("CASE-1"));

        assertThat(result.status()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(result.successes()).hasSize(2);
        assertThat(result.failures()).hasSize(1);
        verify(deletionAuditService).recordSuccessfulDeletion("CASE-1", "refunds");
        verify(deletionAuditService, never()).recordSuccessfulDeletion("CASE-1", "payments");
    }

    private void stubSuccess(String caseReference, String resourceLabel) {
        when(deletionRetryService.deleteWithRetry(eq(caseReference), eq(resourceLabel), any()))
            .thenReturn(PaymentDeletionResult.success(caseReference, resourceLabel, 1));
    }
}
