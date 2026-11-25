package uk.gov.hmcts.reform.disposer.config;

import uk.gov.hmcts.reform.disposer.domain.DeletionAuditRecord;

@FunctionalInterface
public interface AuditWriter {

    void write(DeletionAuditRecord record);
}
