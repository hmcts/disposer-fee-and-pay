package uk.gov.hmcts.reform.disposer.monitoring;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.disposer.domain.DeletionAuditRecord;

import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.disposer.monitoring.EndStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.disposer.monitoring.EndStatus.SUCCESS;
import static uk.gov.hmcts.reform.disposer.monitoring.ProcessMonitorDto.PROCESS_NAME;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessEventTracker {

    public static final String AUDIT_FAILED_EVENT = PROCESS_NAME + " - Audit Failed";

    private final TelemetryClient telemetryClient;

    public void trackEventStarted(ProcessMonitorDto processMonitorDto) {
        track(createMessage(processMonitorDto, "Started"), createPropertiesMap(processMonitorDto));
    }

    public void trackEventCompleted(ProcessMonitorDto processMonitorDto) {
        track(createMessage(processMonitorDto, "Completed"), createPropertiesMap(processMonitorDto));
    }

    public void trackAuditFailure(DeletionAuditRecord record) {
        Map<String, String> properties = new LinkedHashMap<>();
        putIfHasText(properties, "ProcessType", PROCESS_NAME);
        putIfHasText(properties, "CaseReference", record.caseReference());
        putIfHasText(properties, "PaymentId", record.paymentId());
        putIfHasText(properties, "Source", record.source());
        if (record.timestamp() != null) {
            properties.put("Timestamp", record.timestamp().toString());
        }
        track(AUDIT_FAILED_EVENT, properties);
    }

    private void track(String message, Map<String, String> properties) {
        try {
            log.info("Tracking process event {}", message);
            telemetryClient.trackEvent(message, properties, null);
            telemetryClient.flush();
        } catch (RuntimeException exception) {
            log.error("Failed to track process event {}", message, exception);
        }
    }

    private Map<String, String> createPropertiesMap(ProcessMonitorDto processMonitorDto) {
        Map<String, String> properties = new LinkedHashMap<>();
        if (processMonitorDto.getId() != null) {
            properties.put("Id", processMonitorDto.getId().toString());
        }
        putIfHasText(properties, "ProcessType", processMonitorDto.getProcessType());
        if (processMonitorDto.getStartTime() != null) {
            properties.put("StartTime", processMonitorDto.getStartTime().toString());
        }
        if (processMonitorDto.getEndTime() != null) {
            properties.put("EndTime", processMonitorDto.getEndTime().toString());
        }
        if (processMonitorDto.getEndStatus() != null) {
            properties.put("EndStatus", processMonitorDto.getEndStatus().toString());
        }
        putIfHasText(properties, "EndDetail", processMonitorDto.getEndDetail());
        return properties;
    }

    private String createMessage(ProcessMonitorDto processMonitorDto, String event) {
        String message = processMonitorDto.getProcessType() + " - " + event;
        if (!"Completed".equals(event)) {
            return message;
        }

        if (processMonitorDto.getEndStatus() == SUCCESS) {
            return message + " - Success";
        }
        if (processMonitorDto.getEndStatus() == PARTIAL_SUCCESS) {
            return message + " - Partial Success";
        }
        return message + " - Failed";
    }

    private void putIfHasText(Map<String, String> properties, String key, String value) {
        if (StringUtils.hasText(value)) {
            properties.put(key, value);
        }
    }
}
