package uk.gov.hmcts.reform.disposer.client.refunds;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "refunds-api", url = "${refunds.url}")
public interface RefundsApi {

    @DeleteMapping(
        value = "/refunds/ccd_case_reference/{ccdCaseNumber}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void deleteByCase(@PathVariable("ccdCaseNumber") String ccdCaseNumber);
}
