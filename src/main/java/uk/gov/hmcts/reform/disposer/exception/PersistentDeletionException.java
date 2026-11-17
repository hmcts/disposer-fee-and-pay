package uk.gov.hmcts.reform.disposer.exception;

import uk.gov.hmcts.reform.disposer.domain.DisposalRunResult;

public class PersistentDeletionException extends RuntimeException {

    private final DisposalRunResult result;

    public PersistentDeletionException(String message, DisposalRunResult result) {
        super(message);
        this.result = result;
    }

    public DisposalRunResult getResult() {
        return result;
    }
}
