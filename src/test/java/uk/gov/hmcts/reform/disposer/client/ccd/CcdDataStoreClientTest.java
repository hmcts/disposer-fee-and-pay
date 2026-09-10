package uk.gov.hmcts.reform.disposer.client.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;
import uk.gov.hmcts.reform.disposer.service.ServiceTokenGenerator;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CcdDataStoreClientTest {

    private static final LocalDate CLOSED_DATE = LocalDate.of(2019, 1, 15);
    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "Bearer service-token";

    @Mock
    private ServiceTokenGenerator serviceTokenGenerator;

    private MockRestServiceServer mockServer;
    private CcdDataStoreClient ccdDataStoreClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ccdDataStoreClient = new CcdDataStoreClient(builder.build(), serviceTokenGenerator);
    }

    @Test
    void getClosedCasesReturnsCaseReferences() {
        when(serviceTokenGenerator.generateToken()).thenReturn(SERVICE_TOKEN);
        mockServer.expect(requestTo("/internal/searchCases/getClosedCases/2019-01-15"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, USER_TOKEN))
            .andExpect(header("ServiceAuthorization", SERVICE_TOKEN))
            .andRespond(withSuccess(
                "{\"caseReferences\":[\"1111111111111111\",\"2222222222222222\"]}",
                MediaType.APPLICATION_JSON
            ));

        List<String> result = ccdDataStoreClient.getClosedCases(CLOSED_DATE, USER_TOKEN);

        assertThat(result).containsExactly("1111111111111111", "2222222222222222");
        mockServer.verify();
    }

    @Test
    void getClosedCasesReturnsEmptyListWhenBodyIsNull() {
        when(serviceTokenGenerator.generateToken()).thenReturn(SERVICE_TOKEN);
        mockServer.expect(requestTo("/internal/searchCases/getClosedCases/2019-01-15"))
            .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        List<String> result = ccdDataStoreClient.getClosedCases(CLOSED_DATE, USER_TOKEN);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void getClosedCasesReturnsEmptyListWhenCaseReferencesNull() {
        when(serviceTokenGenerator.generateToken()).thenReturn(SERVICE_TOKEN);
        mockServer.expect(requestTo("/internal/searchCases/getClosedCases/2019-01-15"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        List<String> result = ccdDataStoreClient.getClosedCases(CLOSED_DATE, USER_TOKEN);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void getClosedCasesReturnsEmptyListOnNotFound() {
        when(serviceTokenGenerator.generateToken()).thenReturn(SERVICE_TOKEN);
        mockServer.expect(requestTo("/internal/searchCases/getClosedCases/2019-01-15"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        List<String> result = ccdDataStoreClient.getClosedCases(CLOSED_DATE, USER_TOKEN);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void getClosedCasesThrowsCcdDataStoreClientExceptionOnServerError() {
        when(serviceTokenGenerator.generateToken()).thenReturn(SERVICE_TOKEN);
        mockServer.expect(requestTo("/internal/searchCases/getClosedCases/2019-01-15"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatExceptionOfType(CcdDataStoreClientException.class)
            .isThrownBy(() -> ccdDataStoreClient.getClosedCases(CLOSED_DATE, USER_TOKEN))
            .withMessage("Failed to retrieve closed cases from CCD for date 2019-01-15");

        mockServer.verify();
    }
}
