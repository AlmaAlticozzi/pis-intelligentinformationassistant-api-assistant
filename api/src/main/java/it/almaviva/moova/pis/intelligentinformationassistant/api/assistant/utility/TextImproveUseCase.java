package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.utility;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.TextImprovementPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServiceUnavailableException;

import java.util.UUID;

/**
 * Coordinates the text improvement workflow independently from the REST method.
 */
@ApplicationScoped
public class TextImproveUseCase {

    @Inject
    TextImproveValidator validator;

    @Inject
    TextImprovementPromptBuilder promptBuilder;

    @Inject
    Instance<LlmGateway> llmGateways;

    @Inject
    AiConfiguration aiConfiguration;

    public String improve(String inputText) {
        String normalizedInput = validator.validateAndNormalizeInput(inputText);
        System.out.println("[IIA-AI-TEST] TextImproveUseCase validated input");

        AiConfiguration.TextImprove textImproveConfiguration = aiConfiguration.textImprove();
        if (!textImproveConfiguration.enabled()) {
            throw new ServiceUnavailableException("IIA-UTL-TXI-503-001");
        }

        System.out.println("[IIA-AI-TEST] AI config loaded provider="
                + aiConfiguration.provider()
                + ", model="
                + textImproveConfiguration.model());

        LlmRequest request = new LlmRequest(
                AiUseCase.TEXT_IMPROVE,
                promptBuilder.systemPrompt(),
                promptBuilder.userPrompt(normalizedInput),
                textImproveConfiguration.model(),
                textImproveConfiguration.temperature(),
                textImproveConfiguration.maxOutputTokens(),
                UUID.randomUUID().toString());

        LlmResponse response = llmGateways.get().generateText(request);
        return validator.validateAndNormalizeOutput(response == null ? null : response.text());
    }
}
