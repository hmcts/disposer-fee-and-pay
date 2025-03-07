package uk.gov.hmcts.reform.disposer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.disposer.service.ServiceTokenGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationExecutor implements ApplicationRunner {

    @Value("${service.enabled}")
    private boolean isServiceEnabled;

    private final ServiceTokenGenerator serviceTokenGenerator;

    @Override
    public void run(ApplicationArguments args) {
        if (isServiceEnabled) {
            serviceTokenGenerator.generateToken();
            log.info("Service is enabled ...");
        } else {
            log.info("Service is disabled ...");
        }
    }
}
