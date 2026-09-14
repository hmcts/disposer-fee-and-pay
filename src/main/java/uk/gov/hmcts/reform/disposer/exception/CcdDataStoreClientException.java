package uk.gov.hmcts.reform.disposer.exception;

public class CcdDataStoreClientException extends RuntimeException {

    public CcdDataStoreClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
