package uk.gov.hmcts.reform.disposer.exception;

import java.io.Serial;

public class ServiceTokenGenerationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceTokenGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ServiceTokenGenerationException(final String message) {
        super(message);
    }
}
