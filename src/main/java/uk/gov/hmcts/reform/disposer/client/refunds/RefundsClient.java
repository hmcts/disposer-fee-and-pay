package uk.gov.hmcts.reform.disposer.client.refunds;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.disposer.client.FeeAndPayApiClient;
import uk.gov.hmcts.reform.disposer.exception.FeeAndPayClientException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundsClient extends FeeAndPayApiClient {

    private final RefundsApi refundsApi;

    public void deleteByCase(String caseReference) {
        try {
            refundsApi.deleteByCase(caseReference);
        } catch (FeignException exception) {
            if (isAbsent(exception)) {
                log.info("No refunds to delete for case {}", caseReference);
                return;
            }
            throw new FeeAndPayClientException(
                String.format("Failed to delete refunds for case %s", caseReference),
                exception
            );
        }
    }
}
