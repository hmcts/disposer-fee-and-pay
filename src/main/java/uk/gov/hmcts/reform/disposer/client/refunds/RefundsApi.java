package uk.gov.hmcts.reform.disposer.client.refunds;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "refunds-api", url = "${refunds.url}")
public interface RefundsApi {

    @DeleteMapping(
        value = "/cases/{ccd-case-number}/refunds",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void deleteByCase(@PathVariable("ccd-case-number") String ccdCaseNumber);
}
