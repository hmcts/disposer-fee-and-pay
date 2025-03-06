package uk.gov.hmcts.reform.disposer.bdd;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ConfigurationParameters;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters(value = {
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "uk.gov.hmcts.reform.disposer.bdd"),
    @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber/cucumber-report.html")
})
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("integration")
@ContextConfiguration(initializers = WireMockInitializer.class)
public class CucumberRunner {
}
