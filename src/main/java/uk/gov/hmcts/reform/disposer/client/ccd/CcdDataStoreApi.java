package uk.gov.hmcts.reform.disposer.client.ccd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ccd-data-store-api", url = "${ccd.data-store.url}")
public interface CcdDataStoreApi {

    @GetMapping(
        value = "/internal/searchCases/getClosedCases/{date}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    DateCaseClosedResponse getClosedCases(@PathVariable("date") String date);
}
