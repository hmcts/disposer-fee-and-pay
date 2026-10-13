package uk.gov.hmcts.reform.disposer.client.bulkscan;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bulk-scanning-api", url = "${bulk-scanning.url}")
public interface BulkScanningApi {

    @DeleteMapping(
        value = "/cases/{ccd-case-number}/bulk-scan-payments",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void deleteByCase(@PathVariable("ccd-case-number") String ccdCaseNumber);
}
