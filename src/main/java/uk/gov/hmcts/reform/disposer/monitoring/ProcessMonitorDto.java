package uk.gov.hmcts.reform.disposer.monitoring;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ProcessMonitorDto {

    public static final String PROCESS_NAME = "Fee and Pay Deletion";

    private final UUID id;
    private final String processType;
    private final Instant startTime;
    private Instant endTime;
    private EndStatus endStatus;
    private String endDetail;

    public ProcessMonitorDto() {
        this.id = UUID.randomUUID();
        this.processType = PROCESS_NAME;
        this.startTime = Instant.now();
    }

    public void markAsSuccess() {
        applyResult(EndStatus.SUCCESS, null);
    }

    public void markAsPartialSuccess(String endDetail) {
        applyResult(EndStatus.PARTIAL_SUCCESS, endDetail);
    }

    public void markAsFailed(String endDetail) {
        applyResult(EndStatus.FAILED, endDetail);
    }

    private void applyResult(EndStatus endStatus, String endDetail) {
        this.endTime = Instant.now();
        this.endStatus = endStatus;
        this.endDetail = endDetail;
    }
}
