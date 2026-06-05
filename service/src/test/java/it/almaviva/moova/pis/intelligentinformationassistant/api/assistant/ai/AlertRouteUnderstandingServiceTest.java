package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertRouteUnderstandingServiceTest {

    @Test
    void buildsCallsParsesAndValidatesRouteUnderstandingRequest() {
        AlertRouteUnderstandingService service = new AlertRouteUnderstandingService();
        service.promptBuilder = promptBuilder();
        service.responseParser = new AlertRouteUnderstandingResponseParser();
        service.validator = new AlertRouteUnderstandingValidator();
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse("""
                {
                  "decision":"ROUTED",
                  "dataDomains":["SERVICE_DATA"],
                  "primaryDataDomain":"SERVICE_DATA",
                  "interpreterType":"EVENT_INTERPRETER",
                  "accessMode":"KAFKA_EVENT",
                  "intentKind":"EVENT_OCCURRENCE",
                  "outputMode":"ON_MATCH",
                  "requiresPolling":false,
                  "requiresServiceDataApi":false,
                  "requiresKafkaEvent":true,
                  "hasAggregation":false,
                  "hasCardinalityThreshold":false,
                  "hasReportIntent":false,
                  "confidence":0.86,
                  "summary":"Event route.",
                  "rejectedReason":null,
                  "warnings":[]
                }
                """, "FAKE", "fake-model", null, null, null));

        AlertRouteUnderstandingResult result = service.understand(new AlertVerificationPromptData(
                "ALRT1",
                "Route",
                null,
                "Avvisami quando una corsa parte da Garibaldi"));

        ArgumentCaptor<LlmRequest> request = ArgumentCaptor.forClass(LlmRequest.class);
        org.mockito.Mockito.verify(gateway).generateText(request.capture());
        assertThat(request.getValue().useCase()).isEqualTo(AiUseCase.ALERT_ROUTE_UNDERSTANDING);
        assertThat(result.decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(result.interpreterType()).isEqualTo(AlertRouteInterpreterType.EVENT_INTERPRETER);
    }

    @Test
    void normalizesWrongEventLlmResponseForMultiLocationCardinalityPrompt() {
        AlertRouteUnderstandingService service = new AlertRouteUnderstandingService();
        service.promptBuilder = promptBuilder();
        service.responseParser = new AlertRouteUnderstandingResponseParser();
        service.validator = new AlertRouteUnderstandingValidator();
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse("""
                {
                  "decision":"ROUTED",
                  "dataDomains":["SERVICE_DATA"],
                  "primaryDataDomain":"SERVICE_DATA",
                  "interpreterType":"EVENT_INTERPRETER",
                  "accessMode":"KAFKA_EVENT",
                  "intentKind":"EVENT_OCCURRENCE",
                  "outputMode":"ON_MATCH",
                  "requiresPolling":false,
                  "requiresServiceDataApi":false,
                  "requiresKafkaEvent":true,
                  "hasAggregation":false,
                  "hasCardinalityThreshold":false,
                  "hasReportIntent":false,
                  "confidence":0.86,
                  "summary":"Event route.",
                  "rejectedReason":null,
                  "warnings":[]
                }
                """, "FAKE", "fake-model", null, null, null));

        AlertRouteUnderstandingResult result = service.understand(new AlertVerificationPromptData(
                "ALRT1",
                "Route",
                null,
                "Fammi sapere quando a Varedo e Palazzolo Milanese ci sono due treni in arrivo"));

        assertThat(result.interpreterType()).isEqualTo(AlertRouteInterpreterType.SCHEDULED_INTERPRETER);
        assertThat(result.accessMode()).isEqualTo(AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT);
        assertThat(result.intentKind()).isEqualTo(AlertRouteIntentKind.SNAPSHOT_CONDITION);
        assertThat(result.hasAggregation()).isTrue();
        assertThat(result.hasCardinalityThreshold()).isTrue();
    }

    private AlertRouteUnderstandingPromptBuilder promptBuilder() {
        AiConfiguration configuration = mock(AiConfiguration.class);
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(configuration.alertVerify()).thenReturn(alertVerify);
        when(alertVerify.model()).thenReturn("test-model");
        when(alertVerify.temperature()).thenReturn(0.1);
        when(alertVerify.maxOutputTokens()).thenReturn(5000);

        AlertRouteUnderstandingPromptBuilder builder = new AlertRouteUnderstandingPromptBuilder();
        builder.aiConfiguration = configuration;
        return builder;
    }
}
