package uk.gov.hmcts.reform.disposer.bdd.steps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.disposer.domain.DisposalRunResult;
import uk.gov.hmcts.reform.disposer.service.PaymentDisposerService;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class FeeAndPayDisposerTestSteps {

    private static final String SERVICE_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0In0.c2lnbmF0dXJl";
    private static final String BEARER_SERVICE_TOKEN = "Bearer " + SERVICE_TOKEN;
    private static final String USER_ACCESS_TOKEN = "idam-access-token";
    private static final String BEARER_USER_TOKEN = "Bearer " + USER_ACCESS_TOKEN;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private PaymentDisposerService paymentDisposerService;

    @Value("${service.ttl-years}")
    private int ttlYears;

    private DisposalRunResult disposalRunResult;

    @Before
    public void resetWireMock() {
        wireMockServer.resetAll();
        disposalRunResult = null;
    }

    @Given("CCD returns closed cases for the eligible date")
    public void ccdReturnsClosedCasesForTheEligibleDate() {
        wireMockServer.stubFor(
            get(urlEqualTo(closedCasesPath()))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("{\"caseReferences\":[\"1111111111111111\",\"2222222222222222\"]}"))
        );
    }

    @Given("CCD returns not found for the eligible date")
    public void ccdReturnsNotFoundForTheEligibleDate() {
        wireMockServer.stubFor(
            get(urlEqualTo(closedCasesPath()))
                .willReturn(aResponse().withStatus(404))
        );
    }

    @Given("Fee and Pay returns payments for the closed cases")
    public void feeAndPayReturnsPaymentsForTheClosedCases() {
        stubFeeAndPayDeletes("1111111111111111", 204);
        stubFeeAndPayDeletes("2222222222222222", 204);
    }

    @Given("Fee and Pay fails deletion for one payment after retries")
    public void feeAndPayFailsDeletionForOnePaymentAfterRetries() {
        stubFeeAndPayDeletes("1111111111111111", 204);
        stubFeeAndPayDeletes("2222222222222222", 500);
    }

    @And("S2S returns a service token")
    public void s2sReturnsAServiceToken() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/lease"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .withBody(SERVICE_TOKEN))
        );
    }

    @And("IdAM returns a user access token")
    public void idamReturnsAUserAccessToken() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/o/token"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("{\"access_token\":\"" + USER_ACCESS_TOKEN
                        + "\",\"expires_in\":3600,\"token_type\":\"Bearer\"}"))
        );
    }

    @When("the payment disposer runs")
    public void thePaymentDisposerRuns() {
        disposalRunResult = paymentDisposerService.processClosedCases();
    }

    @Then("the closed case references are returned")
    public void theClosedCaseReferencesAreReturned() {
        assertThat(disposalRunResult).isNotNull();
        assertThat(disposalRunResult.successes())
            .extracting(result -> result.caseReference())
            .containsOnly("1111111111111111", "2222222222222222");
    }

    @Then("no closed case references are returned")
    public void noClosedCaseReferencesAreReturned() {
        assertThat(disposalRunResult.successes()).isEmpty();
        assertThat(disposalRunResult.failures()).isEmpty();
    }

    @Then("all payment deletions succeed")
    public void allPaymentDeletionsSucceed() {
        assertThat(disposalRunResult.status()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(disposalRunResult.failures()).isEmpty();
        assertThat(disposalRunResult.successes()).hasSize(6);
    }

    @Then("the disposer reports a multi-status partial success")
    public void theDisposerReportsAMultiStatusPartialSuccess() {
        assertThat(disposalRunResult.status()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(disposalRunResult.successes()).hasSize(5);
        assertThat(disposalRunResult.failures()).hasSize(1);
        assertThat(disposalRunResult.failures().getFirst().paymentId()).isEqualTo("payments");
        assertThat(disposalRunResult.failures().getFirst().attempts()).isEqualTo(3);
    }

    @And("CCD was called with user and service authorization headers")
    public void ccdWasCalledWithUserAndServiceAuthorizationHeaders() {
        wireMockServer.verify(
            getRequestedFor(urlEqualTo(closedCasesPath()))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(BEARER_USER_TOKEN))
                .withHeader("ServiceAuthorization", equalTo(BEARER_SERVICE_TOKEN))
        );
    }

    @And("Fee and Pay delete endpoints were called")
    public void feeAndPayDeleteEndpointsWereCalled() {
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(
            "/refunds/ccd_case_reference/1111111111111111")));
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(
            "/refunds/ccd_case_reference/2222222222222222")));
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(
            "/ccd_case_reference/1111111111111111")));
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(
            "/ccd_case_reference/2222222222222222")));
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(
            "/payments/ccd_case_reference/1111111111111111")));
        wireMockServer.verify(deleteRequestedFor(urlEqualTo(
            "/payments/ccd_case_reference/2222222222222222")));
    }

    private void stubFeeAndPayDeletes(String caseReference, int paymentDeleteStatus) {
        stubDelete("/refunds/ccd_case_reference/" + caseReference, 204);
        stubDelete("/ccd_case_reference/" + caseReference, 204);
        stubDelete("/payments/ccd_case_reference/" + caseReference, paymentDeleteStatus);
    }

    private void stubDelete(String path, int status) {
        wireMockServer.stubFor(
            delete(urlEqualTo(path)).willReturn(aResponse().withStatus(status))
        );
    }

    private String closedCasesPath() {
        LocalDate eligibleClosedDate = LocalDate.now(ZoneOffset.UTC).minusYears(ttlYears);
        return "/internal/searchCases/getClosedCases/" + eligibleClosedDate;
    }
}
