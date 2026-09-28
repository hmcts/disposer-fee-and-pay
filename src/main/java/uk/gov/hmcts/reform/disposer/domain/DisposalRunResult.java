package uk.gov.hmcts.reform.disposer.domain;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Internal multi-status style summary of a disposer run (CME-953).
 * HTTP 207 semantics when successes and failures are mixed.
 */
public record DisposalRunResult(
    List<PaymentDeletionResult>     successes,
    List<PaymentDeletionResult> failures
) {

    public DisposalRunResult {
        successes = List.copyOf(successes);
        failures = List.copyOf(failures);
    }

    public boolean hasPersistentFailures() {
        return !failures.isEmpty();
    }

    public boolean isPartialSuccess() {
        return !successes.isEmpty() && !failures.isEmpty();
    }

    public HttpStatus status() {
        if (failures.isEmpty()) {
            return HttpStatus.NO_CONTENT;
        }
        if (successes.isEmpty()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.MULTI_STATUS;
    }
}
