package uk.gov.hmcts.reform.disposer.client.bulkscan;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bulk-scanning-api", url = "${bulk-scanning.url}")
public interface BulkScanningApi {

    @DeleteMapping(
        value = "/ccd_case_reference/{ccdCaseNumber}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void deleteByCase(@PathVariable("ccdCaseNumber") String ccdCaseNumber);
}
