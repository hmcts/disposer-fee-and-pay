package uk.gov.hmcts.reform.disposer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.disposer.config.AuditWriter;
import uk.gov.hmcts.reform.disposer.config.DeletionProperties;
import uk.gov.hmcts.reform.disposer.domain.DeletionAuditRecord;
import uk.gov.hmcts.reform.disposer.monitoring.ProcessEventTracker;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@ExtendWith(MockitoExtension.class)
class DeletionAuditServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-22T12:00:00Z");

    @Mock
    private AuditWriter auditWriter;

    @Mock
    private ProcessEventTracker processEventTracker;

    private DeletionAuditService deletionAuditService;

    @BeforeEach
    void setUp() {
        DeletionProperties properties = new DeletionProperties(
            new DeletionProperties.Retry(3, 10L, 2.0),
            new DeletionProperties.Audit("disposer-fee-and-pay")
        );
        deletionAuditService = new DeletionAuditService(
            properties,
            Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC),
            auditWriter,
            processEventTracker
        );
    }

    @Test
    void recordSuccessfulDeletionWritesAuditOnce() {
        deletionAuditService.recordSuccessfulDeletion("CASE-1", "PAY-1");

        verify(auditWriter).write(new DeletionAuditRecord(
            "CASE-1",
            "PAY-1",
            FIXED_INSTANT,
            "disposer-fee-and-pay"
        ));
        verify(processEventTracker, never()).trackAuditFailure(any());
        assertThat(deletionAuditService.backlogSize()).isZero();
    }

    @Test
    void recordSuccessfulDeletionRetriesOnceThenQueuesAndAlerts() {
        doThrow(new RuntimeException("disk full"))
            .when(auditWriter).write(any());

        deletionAuditService.recordSuccessfulDeletion("CASE-1", "PAY-1");

        verify(auditWriter, times(2)).write(any());
        verify(processEventTracker).trackAuditFailure(new DeletionAuditRecord(
            "CASE-1",
            "PAY-1",
            FIXED_INSTANT,
            "disposer-fee-and-pay"
        ));
        assertThat(deletionAuditService.backlogSize()).isEqualTo(1);
        assertThat(deletionAuditService.drainBacklog()).hasSize(1);
        assertThat(deletionAuditService.backlogSize()).isZero();
    }

    @Test
    void recordSuccessfulDeletionSucceedsOnRetry() {
        doThrow(new RuntimeException("transient"))
            .doNothing()
            .when(auditWriter).write(any());

        deletionAuditService.recordSuccessfulDeletion("CASE-1", "PAY-1");

        verify(auditWriter, times(2)).write(any());
        verify(processEventTracker, never()).trackAuditFailure(any());
        assertThat(deletionAuditService.backlogSize()).isZero();
    }
}
