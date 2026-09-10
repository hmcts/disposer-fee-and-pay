package uk.gov.hmcts.reform.disposer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ConfiguredUserTokenProviderTest {

    private ConfiguredUserTokenProvider userTokenProvider;

    @BeforeEach
    void setUp() {
        userTokenProvider = new ConfiguredUserTokenProvider();
    }

    @Test
    void getUserTokenPrefixesBearerWhenMissing() {
        ReflectionTestUtils.setField(userTokenProvider, "userToken", "raw-token");

        assertThat(userTokenProvider.getUserToken()).isEqualTo("Bearer raw-token");
    }

    @Test
    void getUserTokenKeepsExistingBearerPrefix() {
        ReflectionTestUtils.setField(userTokenProvider, "userToken", "Bearer already-prefixed");

        assertThat(userTokenProvider.getUserToken()).isEqualTo("Bearer already-prefixed");
    }

    @Test
    void getUserTokenThrowsWhenTokenMissing() {
        ReflectionTestUtils.setField(userTokenProvider, "userToken", "");

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> userTokenProvider.getUserToken())
            .withMessage("CCD user token has not been configured");
    }

    @Test
    void getUserTokenThrowsWhenTokenBlank() {
        ReflectionTestUtils.setField(userTokenProvider, "userToken", "   ");

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> userTokenProvider.getUserToken())
            .withMessage("CCD user token has not been configured");
    }
}
