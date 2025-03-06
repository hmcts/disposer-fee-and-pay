package uk.gov.hmcts.reform.disposer.bdd.steps;


import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

public class ExampleTestSteps {

    @Autowired
    private WireMockServer wireMockServer;

    @Value("${baseUrl}")
    private String baseUrl;

    private String body;

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

}
