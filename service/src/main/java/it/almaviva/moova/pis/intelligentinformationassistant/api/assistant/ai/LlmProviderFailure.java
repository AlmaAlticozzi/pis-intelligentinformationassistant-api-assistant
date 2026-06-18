package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record LlmProviderFailure(
        LlmProviderFailureKind kind,
        Integer httpStatus,
        String providerErrorType,
        String providerErrorCode,
        String requestId,
        boolean retryable
) {

    public static LlmProviderFailure unexpected() {
        return new LlmProviderFailure(
                LlmProviderFailureKind.UNEXPECTED_INTERNAL,
                null,
                null,
                null,
                null,
                false);
    }
}
