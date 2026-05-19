package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

/**
 * Gateway abstraction for LLM providers used by Assistant AI workflows.
 */
public interface LlmGateway {

    LlmResponse generateText(LlmRequest request);
}
