package uk.gov.hmcts.reform.disposer.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.disposer.monitoring.ProcessEventTracker.AUDIT_FAILED_EVENT;
import static uk.gov.hmcts.reform.disposer.monitoring.ProcessMonitorDto.PROCESS_NAME;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.disposer.domain.DeletionAuditRecord;

import java.time.Instant;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ProcessEventTrackerTest {

    @Mock
    private TelemetryClient telemetryClient;

    @InjectMocks
    private ProcessEventTracker processEventTracker;

    @Test
    void trackEventStartedSendsStartedCustomEvent() {
        ProcessMonitorDto process = new ProcessMonitorDto();

        processEventTracker.trackEventStarted(process);

        verifyTrackedEvent(PROCESS_NAME + " - Started");
        verify(telemetryClient).flush();
    }

    @Test
    void trackEventCompletedSendsSuccessSuffix() {
        ProcessMonitorDto process = new ProcessMonitorDto();
        process.markAsSuccess();

        processEventTracker.trackEventCompleted(process);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(telemetryClient).trackEvent(
            eq(PROCESS_NAME + " - Completed - Success"),
            propertiesCaptor.capture(),
            isNull()
        );
        assertThat(propertiesCaptor.getValue())
            .containsEntry("EndStatus", "SUCCESS")
            .containsEntry("ProcessType", PROCESS_NAME);
        verify(telemetryClient).flush();
    }

    @Test
    void trackEventCompletedSendsPartialSuccessSuffix() {
        ProcessMonitorDto process = new ProcessMonitorDto();
        process.markAsPartialSuccess("CASE-1->payments");

        processEventTracker.trackEventCompleted(process);

        verifyTrackedEvent(PROCESS_NAME + " - Completed - Partial Success");
    }

    @Test
    void trackEventCompletedSendsFailedSuffix() {
        ProcessMonitorDto process = new ProcessMonitorDto();
        process.markAsFailed("ccd unavailable");

        processEventTracker.trackEventCompleted(process);

        verifyTrackedEvent(PROCESS_NAME + " - Completed - Failed");
    }

    @Test
    void trackAuditFailureSendsAuditFailedEvent() {
        DeletionAuditRecord record = new DeletionAuditRecord(
            "CASE-1",
            "PAY-1",
            Instant.parse("2026-09-22T12:00:00Z"),
            "disposer-fee-and-pay"
        );

        processEventTracker.trackAuditFailure(record);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(telemetryClient).trackEvent(eq(AUDIT_FAILED_EVENT), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("CaseReference", "CASE-1")
            .containsEntry("PaymentId", "PAY-1")
            .containsEntry("Source", "disposer-fee-and-pay");
        verify(telemetryClient).flush();
    }

    @Test
    void trackEventStartedDoesNotFailTheJobWhenTelemetryThrows() {
        doThrow(new RuntimeException("app insights down"))
            .when(telemetryClient).trackEvent(any(), any(), any());

        processEventTracker.trackEventStarted(new ProcessMonitorDto());

        verify(telemetryClient, never()).flush();
    }

    private void verifyTrackedEvent(String eventName) {
        verify(telemetryClient).trackEvent(eq(eventName), any(), isNull());
    }
}
