package uk.gov.hmcts.reform.disposer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConfiguredUserTokenProvider implements UserTokenProvider {
    //TODO: Replace
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${ccd.user-token}")
    private String userToken;

    @Override
    public String getUserToken() {
        if (!StringUtils.hasText(userToken)) {
            throw new IllegalStateException("CCD user token has not been configured");
        }

        if (userToken.startsWith(BEARER_PREFIX)) {
            return userToken;
        }

        return BEARER_PREFIX + userToken;
    }
}
