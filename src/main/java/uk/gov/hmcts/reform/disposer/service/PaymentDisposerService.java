package uk.gov.hmcts.reform.disposer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.disposer.client.ccd.CcdDataStoreClient;
import uk.gov.hmcts.reform.disposer.exception.CcdDataStoreClientException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentDisposerService {

    private final CcdDataStoreClient ccdDataStoreClient;
    private final UserTokenProvider userTokenProvider;

    @Value("${service.ttl-years}")
    private int ttlYears;

    public List<String> run() {
        LocalDate eligibleClosedDate = LocalDate.now(ZoneOffset.UTC).minusYears(ttlYears);
        log.info("Retrieving cases from CCD with closed date {}", eligibleClosedDate);
        try {
            List<String> caseReferences = ccdDataStoreClient.getClosedCases(
                eligibleClosedDate,
                userTokenProvider.getUserToken()
            );
            log.info(
                "Retrieved {} eligible case references from CCD for closed date {}",
                caseReferences.size(),
                eligibleClosedDate
            );
            return caseReferences;
        } catch (CcdDataStoreClientException exception) {
            log.error("Failed to retrieve eligible case references from CCD. Skipping processing.", exception);
            throw exception;
        }
    }
}
