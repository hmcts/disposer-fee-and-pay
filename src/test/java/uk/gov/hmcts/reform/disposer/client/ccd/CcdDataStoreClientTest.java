package uk.gov.hmcts.reform.disposer.client.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CcdDataStoreClientTest {

    private static final LocalDate CLOSED_DATE = LocalDate.of(2019, 1, 15);
    private static final String CLOSED_DATE_PATH = "2019-01-15";

    @Mock
    private CcdDataStoreApi ccdDataStoreApi;

    @InjectMocks
    private CcdDataStoreClient ccdDataStoreClient;

    @Test
    void getClosedCasesReturnsCaseReferences() {
        when(ccdDataStoreApi.getClosedCases(CLOSED_DATE_PATH))
            .thenReturn(new DateCaseClosedResponse(List.of("1111111111111111", "2222222222222222")));

        List<String> result = ccdDataStoreClient.getClosedCases(CLOSED_DATE);

        assertThat(result).containsExactly("1111111111111111", "2222222222222222");
    }

    @Test
    void getClosedCasesReturnsEmptyListWhenBodyIsNull() {
        when(ccdDataStoreApi.getClosedCases(CLOSED_DATE_PATH)).thenReturn(null);

        assertThat(ccdDataStoreClient.getClosedCases(CLOSED_DATE)).isEmpty();
    }

    @Test
    void getClosedCasesReturnsEmptyListWhenCaseReferencesNull() {
        when(ccdDataStoreApi.getClosedCases(CLOSED_DATE_PATH)).thenReturn(new DateCaseClosedResponse(null));

        assertThat(ccdDataStoreClient.getClosedCases(CLOSED_DATE)).isEmpty();
    }

    @Test
    void getClosedCasesReturnsEmptyListOnNotFound() {
        when(ccdDataStoreApi.getClosedCases(CLOSED_DATE_PATH)).thenThrow(feignException(404));

        assertThat(ccdDataStoreClient.getClosedCases(CLOSED_DATE)).isEmpty();
    }

    @Test
    void getClosedCasesThrowsCcdDataStoreClientExceptionOnServerError() {
        when(ccdDataStoreApi.getClosedCases(CLOSED_DATE_PATH)).thenThrow(feignException(500));

        assertThatExceptionOfType(CcdDataStoreClientException.class)
            .isThrownBy(() -> ccdDataStoreClient.getClosedCases(CLOSED_DATE))
            .withMessage("Failed to retrieve closed cases from CCD for date 2019-01-15");
    }

    private FeignException feignException(int status) {
        return FeignException.errorStatus(
            "getClosedCases",
            Response.builder()
                .status(status)
                .reason("error")
                .request(Request.create(
                    Request.HttpMethod.GET,
                    "/internal/searchCases/getClosedCases/" + CLOSED_DATE_PATH,
                    Map.of(),
                    null,
                    StandardCharsets.UTF_8,
                    new RequestTemplate()
                ))
                .headers(Map.of())
                .build()
        );
    }
}
