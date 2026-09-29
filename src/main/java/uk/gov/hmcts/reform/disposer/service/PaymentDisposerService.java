package uk.gov.hmcts.reform.disposer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.disposer.client.ccd.CcdDataStoreClient;
import uk.gov.hmcts.reform.disposer.domain.DisposalRunResult;
import uk.gov.hmcts.reform.disposer.domain.PaymentDeletionResult;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;
import uk.gov.hmcts.reform.disposer.monitoring.ProcessEventTracker;
import uk.gov.hmcts.reform.disposer.monitoring.ProcessMonitorDto;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentDisposerService {

    private final CcdDataStoreClient ccdDataStoreClient;
    private final FeeAndPayDeletionService feeAndPayDeletionService;
    private final ProcessEventTracker processEventTracker;

    @Value("${service.ttl-years}")
    private int ttlYears;

    public DisposalRunResult processClosedCases() {
        ProcessMonitorDto process = new ProcessMonitorDto();
        processEventTracker.trackEventStarted(process);

        LocalDate eligibleClosedDate = LocalDate.now(ZoneOffset.UTC).minusYears(ttlYears);
        log.info("Retrieving cases from CCD with closed date {}", eligibleClosedDate);
        try {
            List<String> caseReferences = ccdDataStoreClient.getClosedCases(eligibleClosedDate);
            log.info(
                "Retrieved {} eligible case references from CCD for closed date {}",
                caseReferences.size(),
                eligibleClosedDate
            );

            DisposalRunResult result = feeAndPayDeletionService.deletePaymentsForCases(caseReferences);
            completeProcess(process, result);
            return result;
        } catch (CcdDataStoreClientException exception) {
            log.error("Failed to retrieve eligible case references from CCD. Skipping processing.", exception);
            process.markAsFailed(exception.getMessage());
            processEventTracker.trackEventCompleted(process);
            throw exception;
        }
    }

    private void completeProcess(ProcessMonitorDto process, DisposalRunResult result) {
        if (result.isPartialSuccess()) {
            process.markAsPartialSuccess(failureDetail(result.failures()));
        } else if (result.hasPersistentFailures()) {
            process.markAsFailed(failureDetail(result.failures()));
        } else {
            process.markAsSuccess();
        }
        processEventTracker.trackEventCompleted(process);
    }

    private String failureDetail(List<PaymentDeletionResult> failures) {
        String failedPairs = failures.stream()
            .map(failure -> failure.caseReference() + "->" + failure.paymentId())
            .collect(Collectors.joining(", "));
        return String.format(
            "Persistent Fee & Pay deletion failures after retries. count=%d details=[%s]",
            failures.size(),
            failedPairs
        );
    }
}
