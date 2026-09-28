package uk.gov.hmcts.reform.disposer.client.payments;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "payments-api", url = "${payments.url}")
public interface PaymentsApi {

    @DeleteMapping(
        value = "/payments/ccd_case_reference/{ccdCaseNumber}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void deletePaymentsByCase(@PathVariable("ccdCaseNumber") String ccdCaseNumber);
}
