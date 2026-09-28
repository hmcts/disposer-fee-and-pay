package uk.gov.hmcts.reform.disposer.client.bulkscan;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.disposer.client.FeeAndPayApiClient;
import uk.gov.hmcts.reform.disposer.exception.FeeAndPayClientException;

@Component
@Slf4j
@RequiredArgsConstructor
public class BulkScanningClient extends FeeAndPayApiClient {

    private final BulkScanningApi bulkScanningApi;

    public void deleteByCase(String caseReference) {
        try {
            bulkScanningApi.deleteByCase(caseReference);
        } catch (FeignException exception) {
            if (isAbsent(exception)) {
                log.info("No bulk-scan payments to delete for case {}", caseReference);
                return;
            }
            throw new FeeAndPayClientException(
                String.format("Failed to delete bulk-scan payments for case %s", caseReference),
                exception
            );
        }
    }
}
