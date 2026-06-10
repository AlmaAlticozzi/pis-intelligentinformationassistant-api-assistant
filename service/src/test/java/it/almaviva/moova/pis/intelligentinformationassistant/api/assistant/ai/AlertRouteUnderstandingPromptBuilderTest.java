package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertRouteUnderstandingPromptBuilderTest {

    @Test
    void buildsDedicatedRouteUnderstandingPromptWithoutTechnicalSpecificationGeneration() {
        LlmRequest request = builder().build(new AlertVerificationPromptData(
                "ALRT1",
                "Route test",
                "Metadata only",
                "Avvisami quando una corsa parte da Garibaldi"));

        assertThat(request.useCase()).isEqualTo(AiUseCase.ALERT_ROUTE_UNDERSTANDING);
        assertThat(request.systemPrompt())
                .contains("preliminary router")
                .contains("Return only valid raw JSON")
                .contains("Do not produce a technicalSpecification");
        assertThat(request.userPrompt())
                .contains("SERVICE_DATA is currently the only supported data domain")
                .contains("EVENT_INTERPRETER is for alerts triggered by individual ServiceData/Kafka domain events")
                .contains("SCHEDULED_INTERPRETER is for alerts that require periodic snapshot queries")
                .contains("ARRIVING", "DEPARTURE_PLATFORM_CHANGED", "CHANGED_DESTINATION", "RELOAD_JOURNEY")
                .contains("\"dataDomains\": [\"SERVICE_DATA\"]")
                .contains("Do not rely on exact wording or one language")
                .contains("Platform-change wording does not automatically imply EVENT_INTERPRETER")
                .contains("Snapshot state signals")
                .contains("SCHEDULED_INTERPRETER with SERVICE_DATA_API_SNAPSHOT")
                .contains("route to EVENT_INTERPRETER");
    }

    @Test
    void promptSaysAlternativeLocationsDoNotImplyScheduledInterpreter() {
        LlmRequest request = builder().build(new AlertVerificationPromptData(
                "ALRT1",
                "Route test",
                "Metadata only",
                "Avvertimi quando a Bignami o San Siro stadio o Bologna c'e un treno in arrivo"));

        assertThat(request.userPrompt())
                .contains("Multiple monitored locations do not imply scheduled interpreter")
                .contains("X or Y or Z")
                .contains("Do not set hasAggregation=true just because multiple monitored locations are present")
                .contains("Do not route to SCHEDULED_INTERPRETER just because a condition mentions cancellation, delay");
    }

    private AlertRouteUnderstandingPromptBuilder builder() {
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
