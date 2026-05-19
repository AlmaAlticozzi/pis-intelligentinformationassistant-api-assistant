package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

/**
 * Provider-neutral request for an LLM text generation operation.
 */
public record LlmRequest(
        AiUseCase useCase,
        String systemPrompt,
        String userPrompt,
        String model,
        Double temperature,
        Integer maxOutputTokens,
        String correlationId,
        String profile
) {
}
