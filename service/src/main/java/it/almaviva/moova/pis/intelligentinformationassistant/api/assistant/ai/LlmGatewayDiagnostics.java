package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateDiagnostics;

public final class LlmGatewayDiagnostics {

    private LlmGatewayDiagnostics() {
    }

    public static void logRequest(String provider, LlmRequest request, boolean requestContextActive) {
        String systemPrompt = request == null ? null : request.systemPrompt();
        String userPrompt = request == null ? null : request.userPrompt();
        int systemLength = systemPrompt == null ? 0 : systemPrompt.length();
        int userLength = userPrompt == null ? 0 : userPrompt.length();
        System.out.println("[IIA][LLM_GATEWAY][REQUEST]");
        System.out.println("useCase=" + (request == null ? null : request.useCase())
                + " provider=" + provider
                + " model=" + (request == null ? null : request.model())
                + " temperature=" + (request == null ? null : request.temperature())
                + " maxOutputTokens=" + (request == null ? null : request.maxOutputTokens())
                + " systemPromptLength=" + systemLength
                + " userPromptLength=" + userLength
                + " totalPromptLength=" + (systemLength + userLength)
                + " systemPromptHash=" + PromptTemplateDiagnostics.shortSha256(systemPrompt)
                + " userPromptHash=" + PromptTemplateDiagnostics.shortSha256(userPrompt)
                + " requestContextActive=" + requestContextActive);
    }

    public static void logFailure(String provider, LlmRequest request, Throwable exception) {
        LlmProviderFailure failure = LlmProviderExceptionClassifier.classify(exception);
        Throwable root = LlmProviderExceptionClassifier.rootCause(exception);
        System.out.println("[IIA][LLM_GATEWAY][FAILURE]");
        System.out.println("useCase=" + (request == null ? null : request.useCase())
                + " provider=" + provider
                + " exceptionClass=" + className(exception)
                + " message=" + LlmProviderExceptionClassifier.sanitizeMessage(exception == null ? null : exception.getMessage())
                + " rootCauseClass=" + className(root)
                + " rootCauseMessage=" + LlmProviderExceptionClassifier.sanitizeMessage(root == null ? null : root.getMessage())
                + " httpStatus=" + failure.httpStatus()
                + " providerErrorType=" + failure.providerErrorType()
                + " providerErrorCode=" + failure.providerErrorCode()
                + " requestId=" + failure.requestId()
                + " retryable=" + failure.retryable()
                + " failureKind=" + failure.kind());
        if (exception != null) {
            exception.printStackTrace(System.out);
        }
    }

    private static String className(Throwable throwable) {
        return throwable == null ? null : throwable.getClass().getName();
    }
}
