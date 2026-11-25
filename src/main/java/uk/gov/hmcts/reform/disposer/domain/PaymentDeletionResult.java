package uk.gov.hmcts.reform.disposer.domain;

public record PaymentDeletionResult(
    String caseReference,
    String paymentId,
    boolean success,
    int attempts,
    String errorMessage
) {

    public static PaymentDeletionResult success(String caseReference, String paymentId, int attempts) {
        return new PaymentDeletionResult(caseReference, paymentId, true, attempts, null);
    }

    public static PaymentDeletionResult failure(
        String caseReference,
        String paymentId,
        int attempts,
        String errorMessage
    ) {
        return new PaymentDeletionResult(caseReference, paymentId, false, attempts, errorMessage);
    }
}
