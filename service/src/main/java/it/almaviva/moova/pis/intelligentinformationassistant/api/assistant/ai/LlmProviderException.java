package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

/**
 * Runtime exception raised when an LLM provider cannot complete a request.
 */
public class LlmProviderException extends RuntimeException {

    private final LlmProviderFailure failure;

    public LlmProviderException() {
        this(null, null, LlmProviderFailure.unexpected());
    }

    public LlmProviderException(String message) {
        this(message, null, LlmProviderFailure.unexpected());
    }

    public LlmProviderException(String message, Throwable cause) {
        this(message, cause, LlmProviderExceptionClassifier.classify(cause));
    }

    public LlmProviderException(Throwable cause) {
        this(cause == null ? null : cause.getMessage(), cause, LlmProviderExceptionClassifier.classify(cause));
    }

    public LlmProviderException(String message, Throwable cause, LlmProviderFailure failure) {
        super(message, cause);
        this.failure = failure == null ? LlmProviderFailure.unexpected() : failure;
    }

    public LlmProviderFailure failure() {
        return failure;
    }
}
