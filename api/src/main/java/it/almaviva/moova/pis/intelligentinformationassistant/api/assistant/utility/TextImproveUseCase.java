package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.utility;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.TextImprovementPromptBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
    LlmGateway llmGateway;

    public String improve(String inputText, String profile) {
        String normalizedInput = validator.validateAndNormalizeInput(inputText);
        System.out.println("[IIA-AI-TEST] TextImproveUseCase validated input");
        LlmRequest request = new LlmRequest(
                AiUseCase.TEXT_IMPROVE,
                promptBuilder.systemPrompt(),
                promptBuilder.userPrompt(normalizedInput),
                null,
                0.1,
                1200,
                UUID.randomUUID().toString(),
                profile);

        LlmResponse response = llmGateway.generateText(request);
        return validator.validateAndNormalizeOutput(response == null ? null : response.text());
    }
}
