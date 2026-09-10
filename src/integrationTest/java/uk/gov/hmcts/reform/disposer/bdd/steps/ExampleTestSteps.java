package uk.gov.hmcts.reform.disposer.bdd.steps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
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
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.reform.disposer.service.PaymentDisposerService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public class ExampleTestSteps {

    private static final String SERVICE_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0In0.c2lnbmF0dXJl";
    private static final String BEARER_SERVICE_TOKEN = "Bearer " + SERVICE_TOKEN;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private PaymentDisposerService paymentDisposerService;

    @Value("${baseUrl}")
    private String baseUrl;

    @Value("${service.ttl-years}")
    private int ttlYears;

    @Value("${ccd.user-token}")
    private String userToken;

    private String body;
    private List<String> closedCaseReferences;

    @Before
    public void resetWireMock() {
        wireMockServer.resetAll();
        closedCaseReferences = null;
        body = null;
    }

    @Given("WireMock is running")
    public void wireMockIsRunning() {
        wireMockServer.stubFor(get("/test").willReturn(ok("hello")));
    }

    @When("We make a request to WireMock")
    public void weMakeARequestToWireMock() {
        RestClient client = RestClient.create();
        body = client.get()
            .uri(baseUrl + "/test")
            .retrieve()
            .body(String.class);
    }

    @Then("We receive a response from WireMock")
    public void weReceiveAResponseFromWireMock() {
        assertThat(body).isEqualTo("hello");
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

    @When("the payment disposer runs")
    public void thePaymentDisposerRuns() {
        closedCaseReferences = paymentDisposerService.run();
    }

    @Then("the closed case references are returned")
    public void theClosedCaseReferencesAreReturned() {
        assertThat(closedCaseReferences).containsExactly("1111111111111111", "2222222222222222");
    }

    @Then("no closed case references are returned")
    public void noClosedCaseReferencesAreReturned() {
        assertThat(closedCaseReferences).isEmpty();
    }

    @And("CCD was called with user and service authorization headers")
    public void ccdWasCalledWithUserAndServiceAuthorizationHeaders() {
        wireMockServer.verify(
            getRequestedFor(urlEqualTo(closedCasesPath()))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(userToken))
                .withHeader("ServiceAuthorization", equalTo(BEARER_SERVICE_TOKEN))
        );
    }

    private String closedCasesPath() {
        LocalDate eligibleClosedDate = LocalDate.now(ZoneOffset.UTC).minusYears(ttlYears);
        return "/internal/searchCases/getClosedCases/" + eligibleClosedDate;
    }
}
