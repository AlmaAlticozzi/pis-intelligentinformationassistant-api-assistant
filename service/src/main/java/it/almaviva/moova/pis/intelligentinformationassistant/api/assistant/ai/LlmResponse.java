package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

/**
 * Provider-neutral response returned by an LLM text generation operation.
 */
public record LlmResponse(
        String text,
        String provider,
        String model,
        Integer inputTokens,
        Integer outputTokens,
        String rawResponseId
) {
}
