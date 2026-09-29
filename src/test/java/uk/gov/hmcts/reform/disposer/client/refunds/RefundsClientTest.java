package uk.gov.hmcts.reform.disposer.client.refunds;

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
class RefundsClientTest {

    @Mock
    private RefundsApi refundsApi;

    @InjectMocks
    private RefundsClient refundsClient;

    @Test
    void deleteByCaseTreatsNotFoundAsSuccess() {
        doThrow(feignException(404)).when(refundsApi).deleteByCase("CASE-1");

        refundsClient.deleteByCase("CASE-1");

        verify(refundsApi).deleteByCase("CASE-1");
    }

    @Test
    void deleteByCaseWrapsServerErrors() {
        doThrow(feignException(500)).when(refundsApi).deleteByCase("CASE-1");

        assertThatExceptionOfType(FeeAndPayClientException.class)
            .isThrownBy(() -> refundsClient.deleteByCase("CASE-1"))
            .withMessageContaining("CASE-1");
    }

    private feign.FeignException feignException(int status) {
        Request request = Request.create(
            Request.HttpMethod.DELETE,
            "/refunds/ccd_case_reference/CASE-1",
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
        return feign.FeignException.errorStatus("RefundsApi#deleteByCase", response);
    }
}
