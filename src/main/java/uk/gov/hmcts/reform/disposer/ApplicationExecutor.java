package uk.gov.hmcts.reform.disposer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationExecutor implements ApplicationRunner {

    @Value("${service.enabled}")
    private boolean isServiceEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (isServiceEnabled) {
            log.info("Service is enabled ...");
        } else {
            log.info("Service is disabled ...");
        }
    }
}
