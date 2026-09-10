package uk.gov.hmcts.reform.disposer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.disposer.client.ccd.CcdDataStoreClient;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PaymentDisposerServiceTest {

    private static final int TTL_YEARS = 7;
    private static final String USER_TOKEN = "Bearer user-token";

    @Mock
    private CcdDataStoreClient ccdDataStoreClient;

    @Mock
    private UserTokenProvider userTokenProvider;

    @InjectMocks
    private PaymentDisposerService paymentDisposerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentDisposerService, "ttlYears", TTL_YEARS);
    }

    @Test
    void runRetrievesClosedCasesForEligibleDate() {
        LocalDate expectedDate = LocalDate.now(ZoneOffset.UTC).minusYears(TTL_YEARS);
        when(userTokenProvider.getUserToken()).thenReturn(USER_TOKEN);
        when(ccdDataStoreClient.getClosedCases(expectedDate, USER_TOKEN))
            .thenReturn(List.of("1234567890123456", "6543210987654321"));

        List<String> result = paymentDisposerService.run();

        assertThat(result).containsExactly("1234567890123456", "6543210987654321");
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(ccdDataStoreClient).getClosedCases(dateCaptor.capture(), eq(USER_TOKEN));
        assertThat(dateCaptor.getValue()).isEqualTo(expectedDate);
    }

    @Test
    void runPropagatesCcdDataStoreClientException() {
        LocalDate expectedDate = LocalDate.now(ZoneOffset.UTC).minusYears(TTL_YEARS);
        when(userTokenProvider.getUserToken()).thenReturn(USER_TOKEN);
        when(ccdDataStoreClient.getClosedCases(expectedDate, USER_TOKEN))
            .thenThrow(new CcdDataStoreClientException("failed", new RuntimeException("boom")));

        assertThatExceptionOfType(CcdDataStoreClientException.class)
            .isThrownBy(() -> paymentDisposerService.run())
            .withMessage("failed");
    }
}
