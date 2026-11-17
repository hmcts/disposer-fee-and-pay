package uk.gov.hmcts.reform.disposer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.disposer.config.AuditWriter;
import uk.gov.hmcts.reform.disposer.config.DeletionProperties;
import uk.gov.hmcts.reform.disposer.domain.DeletionAuditRecord;
import uk.gov.hmcts.reform.disposer.monitoring.ProcessEventTracker;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeletionAuditService {

    private final DeletionProperties deletionProperties;
    private final Clock clock;
    private final AuditWriter auditWriter;
    private final ProcessEventTracker processEventTracker;
    private final ConcurrentLinkedQueue<DeletionAuditRecord> auditBacklog = new ConcurrentLinkedQueue<>();

    public void recordSuccessfulDeletion(String caseReference, String paymentId) {
        DeletionAuditRecord record = new DeletionAuditRecord(
            caseReference,
            paymentId,
            Instant.now(clock),
            deletionProperties.audit().source()
        );

        if (writeAudit(record)) {
            return;
        }

        log.warn("Initial audit write failed for case {} payment {}; retrying once", caseReference, paymentId);
        if (writeAudit(record)) {
            return;
        }

        log.error(
            "Audit write failed after retry for case {} payment {}; queuing backlog and alerting",
            caseReference,
            paymentId
        );
        auditBacklog.add(record);
        processEventTracker.trackAuditFailure(record);
    }

    public List<DeletionAuditRecord> drainBacklog() {
        List<DeletionAuditRecord> drained = new ArrayList<>();
        DeletionAuditRecord next;
        while ((next = auditBacklog.poll()) != null) {
            drained.add(next);
        }
        return drained;
    }

    public int backlogSize() {
        return auditBacklog.size();
    }

    private boolean writeAudit(DeletionAuditRecord record) {
        try {
            auditWriter.write(record);
            return true;
        } catch (RuntimeException exception) {
            log.error(
                "Failed to write deletion audit for case {} payment {}",
                record.caseReference(),
                record.paymentId(),
                exception
            );
            return false;
        }
    }
}
