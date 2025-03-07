package uk.gov.hmcts.reform.disposer.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.disposer.exception.ServiceTokenGenerationException;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter
public class ServiceTokenGenerator {

    private final AuthTokenGenerator authTokenGenerator;

    private String serviceToken = "dummy token";

    public void generateToken() {
        try {
            serviceToken = authTokenGenerator.generate();
        } catch (Exception e) {
            String msg = String.format("Failed to generate service token - %s", e.getMessage());
            log.error(msg, e);
            throw new ServiceTokenGenerationException(msg, e);
        }
    }
}
