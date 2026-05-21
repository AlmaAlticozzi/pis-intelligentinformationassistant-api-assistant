package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.provider;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import io.quarkus.arc.lookup.LookupIfProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmProviderException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * OpenAI-backed LLM gateway implemented through Quarkus LangChain4j.
 */
@ApplicationScoped
@LookupIfProperty(name = "iia.ai.provider", stringValue = "openai")
public class LangChain4jOpenAiGateway implements LlmGateway {

    private static final String PROVIDER_UNAVAILABLE_ERROR_CODE = "IIA-UTL-TXI-503-001";

    @Inject
    ChatModel chatModel;

    @Override
    public LlmResponse generateText(LlmRequest request) {
        System.out.println("[IIA-AI-TEST] Real OpenAI gateway invoked");
        if (request != null && request.useCase() == it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase.ALERT_VERIFY) {
            System.out.println("[IIA][ALERT_VERIFY][OPENAI_GATEWAY_CONTEXT] requestContextActive="
                    + io.quarkus.arc.Arc.container().requestContext().isActive());
        }
        try {
            ChatResponse response = chatModel.chat(List.of(
                    SystemMessage.from(request.systemPrompt()),
                    UserMessage.from(request.userPrompt())));

            if (response == null || response.aiMessage() == null) {
                throw new LlmProviderException(PROVIDER_UNAVAILABLE_ERROR_CODE);
            }

            AiMessage aiMessage = response.aiMessage();
            TokenUsage tokenUsage = response.tokenUsage();

            return new LlmResponse(
                    aiMessage.text(),
                    "OPENAI",
                    request.model(),
                    tokenUsage == null ? null : tokenUsage.inputTokenCount(),
                    tokenUsage == null ? null : tokenUsage.outputTokenCount(),
                    response.id());
        } catch (LlmProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LlmProviderException(PROVIDER_UNAVAILABLE_ERROR_CODE, ex);
        }
    }
}
