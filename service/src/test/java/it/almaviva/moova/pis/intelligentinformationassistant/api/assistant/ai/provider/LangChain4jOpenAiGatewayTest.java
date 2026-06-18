package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.provider;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmProviderException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmProviderFailureKind;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import jakarta.ws.rs.ProcessingException;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LangChain4jOpenAiGatewayTest {

    @Test
    void minimalRequestsForCoreUseCasesPreserveResolvedRequestConfiguration() {
        for (AiUseCase useCase : List.of(
                AiUseCase.TEXT_IMPROVE,
                AiUseCase.ALERT_ROUTE_UNDERSTANDING,
                AiUseCase.ALERT_VERIFY)) {
            RespondingChatModel chatModel = new RespondingChatModel();
            LangChain4jOpenAiGateway gateway = new LangChain4jOpenAiGateway();
            gateway.chatModel = chatModel;

            LlmResponse response = gateway.generateText(new LlmRequest(
                    useCase,
                    "system",
                    "user",
                    "gpt-4.1-mini",
                    0.1,
                    1200,
                    "corr-1"));

            assertThat(response.provider()).as(useCase.name()).isEqualTo("OPENAI");
            assertThat(response.model()).as(useCase.name()).isEqualTo("gpt-4.1-mini");
            assertThat(chatModel.lastMessages).as(useCase.name()).hasSize(2);
        }
    }

    @Test
    void providerTimeoutPreservesRootCauseAndIsRetryable() {
        LlmProviderException exception = thrownByGateway(new TimeoutException("request timed out"));

        assertThat(exception.getMessage()).isEqualTo("IIA-UTL-TXI-503-001");
        assertThat(exception.getCause()).isInstanceOf(TimeoutException.class);
        assertThat(exception.failure().kind()).isEqualTo(LlmProviderFailureKind.TIMEOUT);
        assertThat(exception.failure().retryable()).isTrue();
    }

    @Test
    void http401IsClassifiedAsAuthenticationNotGenericUnavailable() {
        LlmProviderException exception = thrownByGateway(new HttpException(
                401,
                "{\"error\":{\"type\":\"invalid_api_key\",\"code\":\"invalid_api_key\",\"message\":\"bad key\"}}"));

        assertThat(exception.failure().kind()).isEqualTo(LlmProviderFailureKind.AUTHENTICATION);
        assertThat(exception.failure().httpStatus()).isEqualTo(401);
        assertThat(exception.failure().providerErrorType()).isEqualTo("invalid_api_key");
        assertThat(exception.failure().providerErrorCode()).isEqualTo("invalid_api_key");
        assertThat(exception.failure().retryable()).isFalse();
    }

    @Test
    void http400InvalidRequestPreservesProviderCategory() {
        LlmProviderException exception = thrownByGateway(new HttpException(
                400,
                "{\"error\":{\"type\":\"invalid_request_error\",\"code\":\"unsupported_parameter\",\"message\":\"bad max_tokens\"}}"));

        assertThat(exception.failure().kind()).isEqualTo(LlmProviderFailureKind.INVALID_REQUEST);
        assertThat(exception.failure().httpStatus()).isEqualTo(400);
        assertThat(exception.failure().providerErrorType()).isEqualTo("invalid_request_error");
        assertThat(exception.failure().providerErrorCode()).isEqualTo("unsupported_parameter");
        assertThat(exception.failure().retryable()).isFalse();
    }

    @Test
    void http429IsRateLimitedAndRetryable() {
        LlmProviderException exception = thrownByGateway(new HttpException(
                429,
                "{\"error\":{\"type\":\"rate_limit_error\",\"code\":\"rate_limit_exceeded\",\"message\":\"too many requests\"}}"));

        assertThat(exception.failure().kind()).isEqualTo(LlmProviderFailureKind.RATE_LIMIT);
        assertThat(exception.failure().httpStatus()).isEqualTo(429);
        assertThat(exception.failure().retryable()).isTrue();
    }

    @Test
    void modelNotFoundIsDifferentFromConnectionFailure() {
        LlmProviderException modelNotFound = thrownByGateway(new ModelNotFoundException("model does not exist"));
        LlmProviderException connection = thrownByGateway(new ProcessingException(new ConnectException("connection refused")));

        assertThat(modelNotFound.failure().kind()).isEqualTo(LlmProviderFailureKind.MODEL_NOT_FOUND);
        assertThat(modelNotFound.failure().retryable()).isFalse();
        assertThat(connection.failure().kind()).isEqualTo(LlmProviderFailureKind.CONNECTION);
        assertThat(connection.failure().retryable()).isTrue();
    }

    private LlmProviderException thrownByGateway(RuntimeException failure) {
        LangChain4jOpenAiGateway gateway = new LangChain4jOpenAiGateway();
        gateway.chatModel = new ThrowingChatModel(failure);
        try {
            gateway.generateText(new LlmRequest(
                    AiUseCase.ALERT_VERIFY,
                    "system",
                    "user",
                    "gpt-4.1-mini",
                    0.1,
                    5000,
                    "ALRT1"));
        } catch (LlmProviderException ex) {
            return ex;
        }
        throw new AssertionError("Expected LlmProviderException");
    }

    private static final class ThrowingChatModel implements ChatModel {
        private final RuntimeException failure;

        private ThrowingChatModel(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public ChatResponse chat(List<ChatMessage> messages) {
            throw failure;
        }
    }

    private static final class RespondingChatModel implements ChatModel {
        private List<ChatMessage> lastMessages = List.of();

        @Override
        public ChatResponse chat(List<ChatMessage> messages) {
            lastMessages = List.copyOf(messages);
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from("{\"ok\":true}"))
                    .id("resp-1")
                    .build();
        }
    }
}
