package uk.gov.hmcts.reform.disposer.config;

@FunctionalInterface
public interface BackoffSleeper {

    void sleep(long millis) throws InterruptedException;
}
