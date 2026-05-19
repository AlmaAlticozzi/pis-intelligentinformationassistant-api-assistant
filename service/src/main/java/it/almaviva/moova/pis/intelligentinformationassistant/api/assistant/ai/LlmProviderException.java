package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

/**
 * Runtime exception raised when an LLM provider cannot complete a request.
 */
public class LlmProviderException extends RuntimeException {

    public LlmProviderException() {
        super();
    }

    public LlmProviderException(String message) {
        super(message);
    }

    public LlmProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmProviderException(Throwable cause) {
        super(cause);
    }
}
