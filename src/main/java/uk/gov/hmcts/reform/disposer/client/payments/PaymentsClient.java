package uk.gov.hmcts.reform.disposer.client.payments;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.disposer.client.FeeAndPayApiClient;
import uk.gov.hmcts.reform.disposer.exception.FeeAndPayClientException;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentsClient extends FeeAndPayApiClient {

    private final PaymentsApi paymentsApi;

    public void deleteByCase(String caseReference) {
        try {
            paymentsApi.deletePaymentsByCase(caseReference);
        } catch (FeignException exception) {
            if (isAbsent(exception)) {
                log.info("No payments to delete for case {}", caseReference);
                return;
            }
            throw new FeeAndPayClientException(
                String.format("Failed to delete payments for case %s", caseReference),
                exception
            );
        }
    }
}
