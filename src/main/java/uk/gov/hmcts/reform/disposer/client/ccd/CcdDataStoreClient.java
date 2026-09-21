package uk.gov.hmcts.reform.disposer.client.ccd;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreClient {

    private final CcdDataStoreApi ccdDataStoreApi;

    public List<String> getClosedCases(LocalDate closedCasesDate) {
        try {
            DateCaseClosedResponse response = ccdDataStoreApi.getClosedCases(
                DateTimeFormatter.ISO_DATE.format(closedCasesDate)
            );

            if (response == null || response.caseReferences() == null) {
                return List.of();
            }
            return response.caseReferences();
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                log.info("No closed cases found in CCD for date {}", closedCasesDate);
                return List.of();
            }
            throw new CcdDataStoreClientException(
                String.format("Failed to retrieve closed cases from CCD for date %s", closedCasesDate),
                exception
            );
        }
    }
}
