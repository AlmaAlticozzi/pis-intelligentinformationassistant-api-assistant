package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.provider;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Temporary LLM gateway used until a real provider integration is available.
 */
@ApplicationScoped
public class FakeLlmGateway implements LlmGateway {

    @Override
    public LlmResponse generateText(LlmRequest request) {
        System.out.println("[IIA-AI-TEST] FakeLlmGateway invoked for useCase=" + request.useCase());
        return new LlmResponse(
                "Crea un alert quando una corsa viene cancellata e non è stato fatto nessun annuncio.",
                "FAKE",
                "fake-model",
                null,
                null,
                null);
    }
}
