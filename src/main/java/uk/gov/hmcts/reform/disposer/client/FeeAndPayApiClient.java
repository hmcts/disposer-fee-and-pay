package uk.gov.hmcts.reform.disposer.client;

import feign.FeignException;
import org.springframework.http.HttpStatus;

public abstract class FeeAndPayApiClient {

    protected boolean isAbsent(FeignException exception) {
        return exception.status() == HttpStatus.NOT_FOUND.value()
            || exception.status() == HttpStatus.NO_CONTENT.value();
    }
}
