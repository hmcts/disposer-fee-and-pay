package uk.gov.hmcts.reform.disposer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.disposer.client.bulkscan.BulkScanningClient;
import uk.gov.hmcts.reform.disposer.client.payments.PaymentsClient;
import uk.gov.hmcts.reform.disposer.client.refunds.RefundsClient;
import uk.gov.hmcts.reform.disposer.domain.DisposalRunResult;
import uk.gov.hmcts.reform.disposer.domain.PaymentDeletionResult;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeeAndPayDeletionService {

    private static final String REFUNDS = "refunds";
    private static final String BULK_SCAN = "bulk-scan";
    private static final String PAYMENTS = "payments";

    private final RefundsClient refundsClient;
    private final BulkScanningClient bulkScanningClient;
    private final PaymentsClient paymentsClient;
    private final DeletionRetryService deletionRetryService;
    private final DeletionAuditService deletionAuditService;

    public DisposalRunResult deletePaymentsForCases(List<String> caseReferences) {
        List<PaymentDeletionResult> successes = new ArrayList<>();
        List<PaymentDeletionResult> failures = new ArrayList<>();

        for (String caseReference : caseReferences) {
            disposeCase(caseReference, successes, failures);
        }

        DisposalRunResult result = new DisposalRunResult(successes, failures);
        if (result.isPartialSuccess()) {
            log.warn(
                "Partial Fee & Pay deletion success: {} succeeded, {} failed (multi-status {})",
                successes.size(),
                failures.size(),
                result.status().value()
            );
        } else if (result.hasPersistentFailures()) {
            log.error(
                "Fee & Pay deletion run failed for all targeted records: {} failure(s)",
                failures.size()
            );
        } else {
            log.info(
                "Fee & Pay deletion run completed successfully for {} record(s)",
                successes.size()
            );
        }
        return result;
    }

    private void disposeCase(
        String caseReference,
        List<PaymentDeletionResult> successes,
        List<PaymentDeletionResult> failures
    ) {
        deleteByCaseReference(
            caseReference,
            REFUNDS,
            () -> refundsClient.deleteByCase(caseReference),
            successes,
            failures
        );
        deleteByCaseReference(
            caseReference,
            BULK_SCAN,
            () -> bulkScanningClient.deleteByCase(caseReference),
            successes,
            failures
        );
        deleteByCaseReference(
            caseReference,
            PAYMENTS,
            () -> paymentsClient.deleteByCase(caseReference),
            successes,
            failures
        );
    }

    private void deleteByCaseReference(
        String caseReference,
        String resourceLabel,
        DeletionRetryService.CaseDeletion deletion,
        List<PaymentDeletionResult> successes,
        List<PaymentDeletionResult> failures
    ) {
        PaymentDeletionResult deletionResult = deletionRetryService.deleteWithRetry(
            caseReference,
            resourceLabel,
            deletion
        );
        if (deletionResult.success()) {
            deletionAuditService.recordSuccessfulDeletion(caseReference, resourceLabel);
            successes.add(deletionResult);
        } else {
            failures.add(deletionResult);
        }
    }
}
