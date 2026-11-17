package uk.gov.hmcts.reform.disposer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.disposer.config.BackoffSleeper;
import uk.gov.hmcts.reform.disposer.config.DeletionProperties;
import uk.gov.hmcts.reform.disposer.domain.PaymentDeletionResult;
import uk.gov.hmcts.reform.disposer.exception.FeeAndPayClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeletionRetryService {

    @FunctionalInterface
    public interface CaseDeletion {
        void delete();
    }

    private final DeletionProperties deletionProperties;
    private final BackoffSleeper backoffSleeper;

    public PaymentDeletionResult deleteWithRetry(
        String caseReference,
        String resourceLabel,
        CaseDeletion deletion
    ) {
        int maxAttempts = deletionProperties.retry().maxAttempts();
        FeeAndPayClientException lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                deletion.delete();
                if (attempt > 1) {
                    log.info(
                        "{} deletion succeeded on retry attempt {} for case {}",
                        resourceLabel,
                        attempt,
                        caseReference
                    );
                }
                return PaymentDeletionResult.success(caseReference, resourceLabel, attempt);
            } catch (FeeAndPayClientException exception) {
                lastFailure = exception;
                log.warn(
                    "{} deletion attempt {}/{} failed for case {}: {}",
                    resourceLabel,
                    attempt,
                    maxAttempts,
                    caseReference,
                    exception.getMessage()
                );
                if (attempt < maxAttempts) {
                    sleepBeforeRetry(attempt);
                }
            }
        }

        String message = lastFailure == null ? "Unknown deletion failure" : lastFailure.getMessage();
        log.error(
            "{} deletion failed after {} attempts for case {}",
            resourceLabel,
            maxAttempts,
            caseReference,
            lastFailure
        );
        return PaymentDeletionResult.failure(caseReference, resourceLabel, maxAttempts, message);
    }

    private void sleepBeforeRetry(int attempt) {
        long delayMs = Math.round(
            deletionProperties.retry().initialBackoffMs()
                * Math.pow(deletionProperties.retry().multiplier(), attempt - 1)
        );
        try {
            backoffSleeper.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new FeeAndPayClientException("Deletion retry interrupted", interruptedException);
        }
    }
}
