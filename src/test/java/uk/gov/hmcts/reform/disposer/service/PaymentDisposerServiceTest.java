package uk.gov.hmcts.reform.disposer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.disposer.monitoring.EndStatus.FAILED;
import static uk.gov.hmcts.reform.disposer.monitoring.EndStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.disposer.monitoring.EndStatus.SUCCESS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.disposer.client.ccd.CcdDataStoreClient;
import uk.gov.hmcts.reform.disposer.domain.DisposalRunResult;
import uk.gov.hmcts.reform.disposer.domain.PaymentDeletionResult;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;
import uk.gov.hmcts.reform.disposer.monitoring.ProcessEventTracker;
import uk.gov.hmcts.reform.disposer.monitoring.ProcessMonitorDto;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PaymentDisposerServiceTest {

    private static final int TTL_YEARS = 7;

    @Mock
    private CcdDataStoreClient ccdDataStoreClient;

    @Mock
    private FeeAndPayDeletionService feeAndPayDeletionService;

    @Mock
    private ProcessEventTracker processEventTracker;

    @InjectMocks
    private PaymentDisposerService paymentDisposerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentDisposerService, "ttlYears", TTL_YEARS);
    }

    @Test
    void processClosedCasesTracksFailureWhenDeletionsPersist() {
        LocalDate expectedDate = LocalDate.now(ZoneOffset.UTC).minusYears(TTL_YEARS);
        List<String> cases = List.of("1234567890123456", "6543210987654321");
        PaymentDeletionResult failure = PaymentDeletionResult.failure(
            "1234567890123456",
            "PAY-1",
            3,
            "boom"
        );
        DisposalRunResult runResult = new DisposalRunResult(List.of(), List.of(failure));

        when(ccdDataStoreClient.getClosedCases(expectedDate)).thenReturn(cases);
        when(feeAndPayDeletionService.deletePaymentsForCases(cases)).thenReturn(runResult);

        DisposalRunResult result = paymentDisposerService.processClosedCases();

        assertThat(result).isEqualTo(runResult);
        assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(ccdDataStoreClient).getClosedCases(dateCaptor.capture());
        assertThat(dateCaptor.getValue()).isEqualTo(expectedDate);
        assertThat(completedProcess().getEndStatus()).isEqualTo(FAILED);
        assertThat(completedProcess().getEndDetail()).contains("1234567890123456->PAY-1");
    }

    @Test
    void processClosedCasesTracksPartialSuccessWhenSomeDeletionsFail() {
        LocalDate expectedDate = LocalDate.now(ZoneOffset.UTC).minusYears(TTL_YEARS);
        List<String> cases = List.of("1234567890123456");
        DisposalRunResult runResult = new DisposalRunResult(
            List.of(PaymentDeletionResult.success("1234567890123456", "PAY-1", 1)),
            List.of(PaymentDeletionResult.failure("1234567890123456", "PAY-2", 3, "timeout"))
        );

        when(ccdDataStoreClient.getClosedCases(expectedDate)).thenReturn(cases);
        when(feeAndPayDeletionService.deletePaymentsForCases(cases)).thenReturn(runResult);

        paymentDisposerService.processClosedCases();

        assertThat(completedProcess().getEndStatus()).isEqualTo(PARTIAL_SUCCESS);
    }

    @Test
    void processClosedCasesTracksSuccessWhenAllDeletionsSucceed() {
        LocalDate expectedDate = LocalDate.now(ZoneOffset.UTC).minusYears(TTL_YEARS);
        List<String> cases = List.of("1234567890123456");
        DisposalRunResult runResult = new DisposalRunResult(
            List.of(PaymentDeletionResult.success("1234567890123456", "PAY-1", 1)),
            List.of()
        );

        when(ccdDataStoreClient.getClosedCases(expectedDate)).thenReturn(cases);
        when(feeAndPayDeletionService.deletePaymentsForCases(cases)).thenReturn(runResult);

        DisposalRunResult result = paymentDisposerService.processClosedCases();

        assertThat(result.status()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(completedProcess().getEndStatus()).isEqualTo(SUCCESS);
    }

    @Test
    void processClosedCasesTracksFailureWhenCcdLookupFails() {
        LocalDate expectedDate = LocalDate.now(ZoneOffset.UTC).minusYears(TTL_YEARS);
        when(ccdDataStoreClient.getClosedCases(expectedDate))
            .thenThrow(new CcdDataStoreClientException("failed", new RuntimeException("boom")));

        assertThatExceptionOfType(CcdDataStoreClientException.class)
            .isThrownBy(() -> paymentDisposerService.processClosedCases())
            .withMessage("failed");

        verify(feeAndPayDeletionService, never()).deletePaymentsForCases(any());
        assertThat(completedProcess().getEndStatus()).isEqualTo(FAILED);
        assertThat(completedProcess().getEndDetail()).isEqualTo("failed");
    }

    private ProcessMonitorDto completedProcess() {
        ArgumentCaptor<ProcessMonitorDto> startCaptor = ArgumentCaptor.forClass(ProcessMonitorDto.class);
        ArgumentCaptor<ProcessMonitorDto> completedCaptor = ArgumentCaptor.forClass(ProcessMonitorDto.class);
        verify(processEventTracker).trackEventStarted(startCaptor.capture());
        verify(processEventTracker).trackEventCompleted(completedCaptor.capture());
        assertThat(startCaptor.getValue()).isSameAs(completedCaptor.getValue());
        return completedCaptor.getValue();
    }
}
