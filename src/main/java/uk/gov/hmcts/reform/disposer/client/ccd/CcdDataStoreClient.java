package uk.gov.hmcts.reform.disposer.client.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;
import uk.gov.hmcts.reform.disposer.service.ServiceTokenGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class CcdDataStoreClient {

    private static final String GET_CLOSED_CASES = "/internal/searchCases/getClosedCases/{date}";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    private final RestClient ccdDataStoreRestClient;
    private final ServiceTokenGenerator serviceTokenGenerator;

    public CcdDataStoreClient(
        @Value("${ccd.data-store.url}") String ccdDataStoreUrl,
        ServiceTokenGenerator serviceTokenGenerator
    ) {
        this.ccdDataStoreRestClient = RestClient
            .builder()
            .baseUrl(ccdDataStoreUrl)
            .build();
        this.serviceTokenGenerator = serviceTokenGenerator;
    }

    public List<String> getClosedCases(LocalDate closedCasesDate, String userToken) {
        try {
            DateCaseClosedResponse response = ccdDataStoreRestClient
                .get()
                .uri(GET_CLOSED_CASES, DateTimeFormatter.ISO_DATE.format(closedCasesDate))
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, userToken)
                .header(SERVICE_AUTHORIZATION_HEADER, serviceTokenGenerator.generateToken())
                .retrieve()
                .body(DateCaseClosedResponse.class);

            if (response == null || response.caseReferences() == null) {
                return List.of();
            }
            return response.caseReferences();
        } catch (NotFound exception) {
            log.info("No closed cases found in CCD for date {}", closedCasesDate);
            return List.of();
        } catch (RestClientException exception) {
            throw new CcdDataStoreClientException(
                String.format("Failed to retrieve closed cases from CCD for date %s", closedCasesDate),
                exception
            );
        }
    }
}
