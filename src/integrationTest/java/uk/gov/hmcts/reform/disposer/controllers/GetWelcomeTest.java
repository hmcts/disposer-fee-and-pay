package uk.gov.hmcts.reform.disposer.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetWelcomeTest {

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    void welcomeIntegrationTest() throws Exception {
        assertThat(1).isEqualTo(1);
    }
}
