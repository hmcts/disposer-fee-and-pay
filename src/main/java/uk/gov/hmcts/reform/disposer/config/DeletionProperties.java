package uk.gov.hmcts.reform.disposer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deletion")
public record DeletionProperties(
    Retry retry,
    Audit audit
) {

    public record Retry(
        int maxAttempts,
        long initialBackoffMs,
        double multiplier
    ) {
    }

    public record Audit(
        String source
    ) {
    }
}
