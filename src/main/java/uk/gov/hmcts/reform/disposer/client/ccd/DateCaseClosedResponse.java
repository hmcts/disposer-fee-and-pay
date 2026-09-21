package uk.gov.hmcts.reform.disposer.client.ccd;

import java.util.List;

public record DateCaseClosedResponse(List<String> caseReferences) {
}
