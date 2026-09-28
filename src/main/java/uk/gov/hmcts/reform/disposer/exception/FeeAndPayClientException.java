package uk.gov.hmcts.reform.disposer.exception;

public class FeeAndPayClientException extends RuntimeException {

    public FeeAndPayClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeeAndPayClientException(String message) {
        super(message);
    }
}
