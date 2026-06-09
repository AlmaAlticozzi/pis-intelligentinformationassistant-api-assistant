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

    @Test
    void normalizesScheduledLlmResponseToEventForSingleEventAttributeThresholds() {
        for (String prompt : java.util.List.of(
                "Avvisami quando una corsa parte da Garibaldi FS con ritardo di almeno 5 min",
                "Notify me when a journey departs from Garibaldi FS with a delay of at least 5 minutes",
                "Avvisami quando una corsa arriva a Garibaldi FS con ritardo superiore a 10 minuti")) {
            AlertRouteUnderstandingResult result = serviceWithResponse(scheduledResponse())
                    .understand(new AlertVerificationPromptData("ALRT1", "Route", null, prompt));

            assertThat(result.interpreterType()).as(prompt).isEqualTo(AlertRouteInterpreterType.EVENT_INTERPRETER);
            assertThat(result.accessMode()).as(prompt).isEqualTo(AlertRouteAccessMode.KAFKA_EVENT);
            assertThat(result.intentKind()).as(prompt).isEqualTo(AlertRouteIntentKind.EVENT_CONDITION);
            assertThat(result.outputMode()).as(prompt).isEqualTo(AlertRouteOutputMode.ON_MATCH);
            assertThat(result.requiresKafkaEvent()).as(prompt).isTrue();
            assertThat(result.requiresPolling()).as(prompt).isFalse();
            assertThat(result.requiresServiceDataApi()).as(prompt).isFalse();
            assertThat(result.hasAggregation()).as(prompt).isFalse();
            assertThat(result.hasCardinalityThreshold()).as(prompt).isFalse();
            assertThat(result.hasReportIntent()).as(prompt).isFalse();
        }
    }

    @Test
    void normalizesEventLlmResponseToScheduledForSnapshotCountAndPollingPrompts() {
        for (String prompt : java.util.List.of(
                "Ci sono corse a Garibaldi FS con ritardo di almeno 5 min",
                "Quanti treni a Garibaldi FS hanno ritardo di almeno 5 min",
                "Avvisami quando ci sono almeno 3 corse in ritardo a Garibaldi FS",
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi FS")) {
            AlertRouteUnderstandingResult result = serviceWithResponse(eventResponse())
                    .understand(new AlertVerificationPromptData("ALRT1", "Route", null, prompt));

            assertThat(result.interpreterType()).as(prompt).isEqualTo(AlertRouteInterpreterType.SCHEDULED_INTERPRETER);
            assertThat(result.accessMode()).as(prompt).isEqualTo(AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT);
            assertThat(result.requiresPolling()).as(prompt).isTrue();
            assertThat(result.requiresServiceDataApi()).as(prompt).isTrue();
            assertThat(result.requiresKafkaEvent()).as(prompt).isFalse();
        }
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

    private AlertRouteUnderstandingService serviceWithResponse(String response) {
        AlertRouteUnderstandingService service = new AlertRouteUnderstandingService();
        service.promptBuilder = promptBuilder();
        service.responseParser = new AlertRouteUnderstandingResponseParser();
        service.validator = new AlertRouteUnderstandingValidator();
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse(response, "FAKE", "fake-model", null, null, null));
        return service;
    }

    private String eventResponse() {
        return """
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
                """;
    }

    private String scheduledResponse() {
        return """
                {
                  "decision":"ROUTED",
                  "dataDomains":["SERVICE_DATA"],
                  "primaryDataDomain":"SERVICE_DATA",
                  "interpreterType":"SCHEDULED_INTERPRETER",
                  "accessMode":"SERVICE_DATA_API_SNAPSHOT",
                  "intentKind":"SNAPSHOT_CONDITION",
                  "outputMode":"ON_MATCH",
                  "requiresPolling":true,
                  "requiresServiceDataApi":true,
                  "requiresKafkaEvent":false,
                  "hasAggregation":true,
                  "hasCardinalityThreshold":true,
                  "hasReportIntent":false,
                  "confidence":0.86,
                  "summary":"Scheduled route.",
                  "rejectedReason":null,
                  "warnings":[]
                }
                """;
    }
}
