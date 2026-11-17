package uk.gov.hmcts.reform.disposer.domain;

import java.time.Instant;

public record DeletionAuditRecord(
    String caseReference,
    String paymentId,
    Instant timestamp,
    String source
) {
}
