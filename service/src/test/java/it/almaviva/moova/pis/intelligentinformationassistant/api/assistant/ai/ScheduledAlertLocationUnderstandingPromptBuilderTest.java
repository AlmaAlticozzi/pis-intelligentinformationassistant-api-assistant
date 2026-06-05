package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertLocationUnderstandingPromptBuilderTest {

    private final ScheduledAlertLocationUnderstandingPromptBuilder builder =
            new ScheduledAlertLocationUnderstandingPromptBuilder();

    @Test
    void buildsScheduledLocationUnderstandingUseCaseRequest() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.useCase()).isEqualTo(AiUseCase.ALERT_SCHEDULED_LOCATION_UNDERSTANDING);
        assertThat(request.correlationId()).isEqualTo("ALRT1");
    }

    @Test
    void promptExplainsScheduledServiceDataSnapshotContext() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.systemPrompt())
                .contains("scheduled ServiceData Alert location understanding")
                .contains("Return only valid raw JSON");
        assertThat(request.userPrompt())
                .contains("SERVICE_DATA + SCHEDULED_INTERPRETER")
                .contains("POST /v2/stoppointjourneys")
                .contains("body.stopPoints[]");
    }

    @Test
    void promptDistinguishesMonitoredStopPointsFromFiltersAndPlatforms() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.userPrompt())
                .contains("MONITORED_STOP_POINT")
                .contains("at/in/for location X")
                .contains("FILTER_ORIGIN_STOP_POINT")
                .contains("FILTER_TIMETABLED_ORIGIN_STOP_POINT")
                .contains("FILTER_DESTINATION_STOP_POINT")
                .contains("FILTER_ROUTE_STOP_POINT")
                .contains("FILTER_CANCELLED_CALL_STOP_POINT")
                .contains("must keep polarity=EXCLUDE")
                .contains("binario, platform, track, quay, banchina or marciapiede")
                .contains("PLATFORM_NUMERIC");
    }

    @Test
    void promptIncludesBackendScheduledLocationHints() {
        LlmRequest request = builder.build(
                "Fammi sapere il numero di corse che partiranno dal binario 7 a Cenisio",
                "ALRT1",
                ScheduledAlertLocationUnderstandingHints.fromPrompt(
                        "Fammi sapere il numero di corse che partiranno dal binario 7 a Cenisio"));

        assertThat(request.userPrompt())
                .contains("Backend scheduled location hints")
                .contains("containsPlatformExpression: true")
                .contains("Platform/binario observations must become nonLocationConstraints");
    }

    @Test
    void promptForbidsResolutionTechnicalSpecificationAndBlueprint() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.systemPrompt())
                .contains("Do not resolve stop point ids")
                .contains("Do not generate technicalSpecification or agentBlueprintPreview")
                .contains("Do not call external services");
    }
}
