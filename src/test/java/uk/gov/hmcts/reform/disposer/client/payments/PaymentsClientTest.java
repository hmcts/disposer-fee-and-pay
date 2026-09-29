package uk.gov.hmcts.reform.disposer.client.payments;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.disposer.exception.FeeAndPayClientException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PaymentsClientTest {

    @Mock
    private PaymentsApi paymentsApi;

    @InjectMocks
    private PaymentsClient paymentsClient;

    @Test
    void deleteByCaseTreatsNotFoundAsSuccess() {
        doThrow(feignException(404)).when(paymentsApi).deletePaymentsByCase("CASE-1");

        paymentsClient.deleteByCase("CASE-1");

        verify(paymentsApi).deletePaymentsByCase("CASE-1");
    }

    @Test
    void deleteByCaseWrapsServerErrors() {
        doThrow(feignException(500)).when(paymentsApi).deletePaymentsByCase("CASE-1");

        assertThatExceptionOfType(FeeAndPayClientException.class)
            .isThrownBy(() -> paymentsClient.deleteByCase("CASE-1"))
            .withMessageContaining("CASE-1");
    }

    private feign.FeignException feignException(int status) {
        Request request = Request.create(
            Request.HttpMethod.DELETE,
            "/payments/ccd_case_reference/CASE-1",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        Response response = Response.builder()
            .status(status)
            .reason("error")
            .request(request)
            .headers(Collections.emptyMap())
            .build();
        return feign.FeignException.errorStatus("PaymentsApi#delete", response);
    }
}
