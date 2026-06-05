package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataApiQueryContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationResolutionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScheduledAlertVerificationPromptBuilderTest {

    @Test
    void promptContainsScheduledMissionAndMvpContract() {
        LlmRequest request = builder().build(promptData(explicitContext()));

        assertThat(request.useCase()).isEqualTo(AiUseCase.ALERT_SCHEDULED_VERIFY);
        assertThat(request.systemPrompt())
                .contains("SCHEDULED_INTERPRETER")
                .contains("SERVICE_DATA_API_SNAPSHOT")
                .contains("POST /v2/stoppointjourneys")
                .contains("ServiceDataStopPointJourneysV2")
                .contains("SCHEDULED_SNAPSHOT_MATCH")
                .contains("The future Agent evaluates the returned snapshot, not a Kafka event");
    }

    @Test
    void promptDoesNotContainEventOnlyContractOrEventCatalogFieldsAsSupportedFields() {
        LlmRequest request = builder().build(promptData(explicitContext()));

        assertThat(request.systemPrompt())
                .doesNotContain("The only allowed interpreter is EVENT_INTERPRETER")
                .doesNotContain("The only allowed evaluation mode is STATELESS_EVENT_MATCH");
        assertThat(request.userPrompt())
                .doesNotContain("- field: payload.ongroundServiceEvent.eventsType")
                .doesNotContain("STATELESS_EVENT_MATCH");
        assertThat(request.systemPrompt())
                .contains("Do not use payload.ongroundServiceEvent.*");
    }

    @Test
    void promptContainsScheduledLocationContextAndKeepsFilterOutOfApiStopPoints() {
        LlmRequest request = builder().build(promptData(explicitContext()));

        assertThat(request.userPrompt())
                .contains("serviceDataApiStopPoints: [TNPNTS00000000000001]")
                .contains("rawText: Pero")
                .contains("rawText: Garibaldi FS")
                .contains("role: FILTER_ORIGIN_STOP_POINT")
                .contains("targetFieldHints: [stopPointsJourneyDetails[].callStart.stopPoint.id")
                .contains("Filter locations must not be placed into serviceDataQuery.stopPoints");
        assertThat(request.userPrompt()).doesNotContain("serviceDataApiStopPoints: [TNPNTS00000000000001, TNPNTS00000000005439]");
    }

    @Test
    void promptContainsAllKnownStopPointsContext() {
        LlmRequest request = builder().build(promptData(allKnownContext()));

        assertThat(request.userPrompt())
                .contains("monitoringScope: ALL_KNOWN_STOP_POINTS")
                .contains("requiresAllKnownStopPoints: true")
                .contains("serviceDataApiStopPoints: []")
                .contains("stopPoints may be empty")
                .contains("requiresAllKnownStopPoints=true is not an error");
    }

    @Test
    void promptContainsReportVsConditionRules() {
        LlmRequest request = builder().build(promptData(explicitContext()));

        assertThat(request.userPrompt())
                .contains("Report vs conditional notification")
                .contains("outputPolicy.emit = EVERY_RUN")
                .contains("snapshotEvaluation.mode = REPORT_COUNT or REPORT_MATCHING_JOURNEYS")
                .contains("outputPolicy.emit = ON_MATCH")
                .contains("threshold is required when a numeric threshold is requested");
    }

    @Test
    void promptContainsTimeDistinction() {
        LlmRequest request = builder().build(promptData(explicitContext()));

        assertThat(request.userPrompt())
                .contains("Polling frequency")
                .contains("technicalSpecification.schedule.frequencySeconds")
                .contains("API visibility window")
                .contains("serviceDataQuery.timeWindow.lookaheadMinutes")
                .contains("Journey time filters")
                .contains("LOCAL_TIME_BETWEEN")
                .contains("defaultTimezone: Europe/Rome");
    }

    @Test
    void promptContainsReplacementCancellationPlatformAndChangeRules() {
        LlmRequest request = builder().build(promptData(explicitContext()));

        assertThat(request.userPrompt())
                .contains("Platform/binario/track/quay/banchina/marciapiede are not locations")
                .contains("Platform change uses changes CONTAINS PLATFORM_CHANGED")
                .contains("Use changes CONTAINS CANCELLATION")
                .contains("exclusion.totalExclusion EQUALS true")
                .contains("\"replacement stop X\" -> nested anyElement replacement.stopPointReplacements[] stopPointId.id")
                .contains("replacementType IN/EQUALS DEPARTURE/ARRIVAL/ARRIVALDEPARTURE");
    }

    private ScheduledAlertVerificationPromptBuilder builder() {
        AiConfiguration configuration = mock(AiConfiguration.class);
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(configuration.alertVerify()).thenReturn(alertVerify);
        when(alertVerify.model()).thenReturn("test-model");
        when(alertVerify.temperature()).thenReturn(0.1);
        when(alertVerify.maxOutputTokens()).thenReturn(5000);

        TemporalConfiguration temporalConfiguration = mock(TemporalConfiguration.class);
        when(temporalConfiguration.defaultZone()).thenReturn("Europe/Rome");

        ScheduledAlertVerificationPromptBuilder builder = new ScheduledAlertVerificationPromptBuilder();
        builder.aiConfiguration = configuration;
        builder.temporalConfiguration = temporalConfiguration;
        builder.defaultPollingFrequencySeconds = 600;
        builder.defaultLookaheadMinutes = 480;
        return builder;
    }

    private ScheduledAlertVerificationPromptData promptData(ScheduledServiceDataLocationContext context) {
        return new ScheduledAlertVerificationPromptData(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Ogni 10 minuti dimmi quante corse hanno origine Garibaldi FS a Pero",
                route(),
                context);
    }

    private AlertRouteUnderstandingResult route() {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                AlertRouteIntentKind.SNAPSHOT_REPORT,
                AlertRouteOutputMode.EVERY_RUN_REPORT,
                true,
                true,
                false,
                true,
                false,
                true,
                0.95,
                "Scheduled ServiceData route.",
                null,
                List.of());
    }

    private ScheduledServiceDataLocationContext explicitContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(location(
                "Pero",
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                List.of("TNPNTS00000000000001"),
                List.of("body.stopPoints[]")));
        List<ScheduledServiceDataResolvedLocation> filters = List.of(location(
                "Garibaldi FS",
                ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                List.of("TNPNTS00000000005439"),
                List.of("stopPointsJourneyDetails[].callStart.stopPoint.id",
                        "stopPointsJourneyDetails[].callStart.stopPoint.nameLong")));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                filters,
                List.of(),
                List.of("TNPNTS00000000000001"),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of("TNPNTS00000000000001"),
                        false));
    }

    private ScheduledServiceDataLocationContext allKnownContext() {
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                true,
                false,
                List.of(),
                List.of("All known stop points scope requested."),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS,
                        List.of(),
                        true));
    }

    private ScheduledServiceDataResolvedLocation location(
            String rawText,
            ScheduledAlertLocationRole role,
            ScheduledAlertLocationPolarity polarity,
            List<String> selectedPointIds,
            List<String> targetFieldHints) {
        return new ScheduledServiceDataResolvedLocation(
                rawText,
                rawText,
                role,
                polarity,
                role == ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                selectedPointIds,
                List.of(),
                false,
                false,
                targetFieldHints,
                "");
    }
}
