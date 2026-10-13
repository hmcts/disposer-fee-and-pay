package uk.gov.hmcts.reform.disposer.config;

import java.time.Clock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DeletionProperties.class)
@Slf4j
public class DeletionConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public BackoffSleeper backoffSleeper() {
        return Thread::sleep;
    }

    @Bean
    public AuditWriter auditWriter() {
        return record -> log.info(
            "AUDIT_DELETION caseId={} paymentId={} timestamp={} source={}",
            record.caseReference(),
            record.paymentId(),
            record.timestamp(),
            record.source()
        );
    }
}
