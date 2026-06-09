package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataApiQueryContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationResolutionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledAlertVerificationServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String GORLA = "TNPNTS00000000000001";
    private static final String PERO = "TNPNTS00000000000002";
    private static final String GARIBALDI = "TNPNTS00000000000003";
    private static final String VAREDO = "TNPNTS00000000000101";
    private static final String PALAZZOLO = "TNPNTS00000000000102";
    private static final String LECCO = "TNPNTS00000000000113";
    private static final String MILANO_CENTRALE = "TNPNTS00000000000024";
    private static final String SAN_SIRO_STADIO = "TNPNTS00000000000019";
    private static final String INVENTED = "TNPNTS99999999999999";

    @Test
    void returnsVerifiedForValidScheduledResponse() {
        TestFixture fixture = fixture(json(validResponse(explicitContext(), validCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Fammi sapere quando a Gorla ci sono almeno due treni in arrivo",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        ArgumentCaptor<LlmRequest> request = ArgumentCaptor.forClass(LlmRequest.class);
        verify(fixture.gateway).generateText(request.capture());
        assertThat(request.getValue().useCase()).isEqualTo(AiUseCase.ALERT_SCHEDULED_VERIFY);
        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification()).isNotNull();
        assertThat(outcome.agentBlueprintPreview()).isNotNull();
        assertThat(outcome.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(outcome.technicalSpecification()).containsEntry("accessMode", "SERVICE_DATA_API_SNAPSHOT");
    }

    @Test
    void preservesRejectedScheduledResponse() {
        TestFixture fixture = fixture(json(rejectedResponse("Wifi onboard is not supported.")));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Fammi sapere il numero di treni con wifi a Gorla",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).isEqualTo("Wifi onboard is not supported.");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void rejectsEventContaminatedVerifiedResponse() {
        Map<String, Object> response = validResponse(explicitContext(), validCondition());
        response.put("inputModel", "ServiceDataV2");
        response.put("triggerType", "EVENT");
        Map<String, Object> technical = map(response.get("technicalSpecification"));
        technical.put("inputModel", "ServiceDataV2");
        technical.put("triggerType", "EVENT");

        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Fammi sapere quando a Gorla ci sono almeno due treni in arrivo",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("triggerType SCHEDULE");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void returnsControlledRejectedForInvalidJson() {
        TestFixture fixture = fixture("not-json");

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Fammi sapere quando a Gorla ci sono almeno due treni in arrivo",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("not valid JSON");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void returnsErrorForProviderFailureLikeEventVerify() {
        TestFixture fixture = fixtureWithProviderFailure(new LlmProviderException("IIA-UTL-TXI-503-001"));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Fammi sapere quando a Gorla ci sono almeno due treni in arrivo",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.ERROR);
        assertThat(outcome.summary()).contains("AI provider");
        assertThat(outcome.rejectedReason()).isNull();
        assertThat(outcome.warnings()).contains("Scheduled alert verification could not complete due to a technical AI provider error.");
    }

    @Test
    void rejectsInventedStopPointId() {
        TestFixture fixture = fixture(json(validResponse(explicitContext(),
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callStart.stopPoint.id", "EQUALS", INVENTED)))));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Fammi sapere quando a Gorla ci sono almeno due treni in arrivo",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("not resolved from the user prompt");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesAllKnownStopPointsContext() {
        ScheduledServiceDataLocationContext context = allKnownContext();
        Map<String, Object> response = validReportResponse(context, delayCondition());
        withSchedule(response, 3600, false, "ogni ora");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled alert",
                "Scheduled ServiceData test",
                "Fammi sapere ogni ora quanti treni sono in ritardo in tutte le localita",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("serviceDataQuery")))
                .containsEntry("monitoringScope", "ALL_KNOWN_STOP_POINTS")
                .containsEntry("requiresAllKnownStopPoints", true);
        assertThat(map(outcome.technicalSpecification().get("serviceDataQuery")).get("stopPoints"))
                .isEqualTo(List.of());
    }

    @Test
    void verifiesReportResponseUsingReportCount() {
        Map<String, Object> response = validReportResponse(explicitContext(), delayCondition());
        withSchedule(response, 600, false, "Ogni 10 minuti");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled report",
                "Scheduled ServiceData report test",
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("snapshotEvaluation")))
                .containsEntry("mode", "REPORT_COUNT")
                .containsEntry("threshold", null);
        assertThat(map(outcome.technicalSpecification().get("outputPolicy")))
                .containsEntry("emit", "EVERY_RUN")
                .containsEntry("includeCount", true);
    }

    @Test
    void verifiesArrivalSuppressedJourneysCountAtLeccoWithDirectedCancellationSemantics() {
        ScheduledServiceDataLocationContext context = leccoContext();
        Map<String, Object> condition = condition(Map.of("all", List.of(
                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"))));
        Map<String, Object> response = validReportResponse(context, condition);
        withSchedule(response, 600, false, "ogni 10 min");
        response.put("requirementCoverage", Map.of(
            "requirements", List.of(
                    Map.of(
                            "text", "Recurring count of arrival suppressed journeys at Lecco",
                            "required", true,
                            "mappable", true,
                            "mappedBy", List.of(
                                    "serviceDataQuery.stopPoints",
                                    "schedule.frequencySeconds",
                                    "stopPointsJourneyDetails[].arrivalStatuses[].status",
                                    "stopPointsJourneyDetails[].departureStatuses[].status",
                                    "outputPolicy.emit",
                                    "outputPolicy.includeCount"))),
                "allRequiredRequirementsMapped", true));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled Arrival Suppressed Journeys Count At Lecco",
                "Verify scheduled snapshot routing for a recurring count of arrival suppressed at Lecco.",
                "Avvertimi ogni 10 min su quante corse soppresse in arrivo ci sono a Lecco",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(map(outcome.technicalSpecification().get("schedule"))).containsEntry("frequencySeconds", 600);
        assertThat(map(outcome.technicalSpecification().get("serviceDataQuery")).get("stopPoints")).isEqualTo(List.of(LECCO));
        assertThat(map(outcome.technicalSpecification().get("snapshotEvaluation"))).containsEntry("mode", "REPORT_COUNT");
        String snapshot = String.valueOf(outcome.technicalSpecification().get("snapshotEvaluation"));
        assertThat(snapshot)
                .contains("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION")
                .contains("departureStatuses[].status", "NOT_CONTAINS", "DEPARTURE_CANCELLATION")
                .doesNotContain("passingType")
                .doesNotContain("field=changes")
                .doesNotContain("operator=CONTAINS, value=DEPARTURE_CANCELLATION");
        assertThat(map(outcome.technicalSpecification().get("outputPolicy"))).containsEntry("emit", "EVERY_RUN");
    }

    @Test
    void verifiesSanSiroTypoGenericCancellationPlatformReportWithTechnicalSpecCoverage() {
        ScheduledServiceDataLocationContext context = sanSiroStadioContext();
        Map<String, Object> condition = condition(Map.of("all", List.of(
                leaf("changes", "CONTAINS", "CANCELLATION"),
                leaf("timetabledDeparturePlatform.dsc", "EQUAL_PLATFORM", "5"))));
        Map<String, Object> response = validReportResponse(context, condition);
        response.put("requirementCoverage", Map.of(
                "requirements", List.of(
                        Map.of(
                                "text", "monitored stop point San Siro Stadio",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("serviceDataQuery.stopPoints")),
                        Map.of(
                                "text", "count report",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of(
                                        "snapshotEvaluation.mode",
                                        "outputPolicy.emit",
                                        "outputPolicy.includeCount")),
                        Map.of(
                                "text", "generic cancellation",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("stopPointsJourneyDetails[].changes")),
                        Map.of(
                                "text", "platform 5",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc"))),
                "allRequiredRequirementsMapped", true));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Test casuale Scheduled 6",
                "test casuale scheduled post elaborazione",
                "Fammi sapere quanti treni a San Siro Staeio hanno una cancellazione e sono sul binario 5",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("serviceDataQuery")).get("stopPoints"))
                .isEqualTo(List.of(SAN_SIRO_STADIO));
        Map<String, Object> snapshotEvaluation = map(outcome.technicalSpecification().get("snapshotEvaluation"));
        assertThat(snapshotEvaluation)
                .containsEntry("mode", "REPORT_COUNT")
                .containsEntry("threshold", null);
        assertThat(String.valueOf(snapshotEvaluation.get("condition")))
                .contains("changes")
                .contains("CANCELLATION")
                .contains("timetabledDeparturePlatform.dsc")
                .contains("EQUAL_PLATFORM")
                .contains("5");
        assertThat(map(outcome.technicalSpecification().get("outputPolicy")))
                .containsEntry("emit", "EVERY_RUN")
                .containsEntry("includeCount", true);
        assertThat(outcome.technicalSpecification()).isNotNull();
        assertThat(outcome.agentBlueprintPreview()).isNotNull();
    }

    @Test
    void rejectsOriginFilterReportResponseUsingCountMatchingJourneysThresholdOne() {
        ScheduledServiceDataLocationContext context = originFilterContext();
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("callStart.stopPoint.id", "EQUALS", GARIBALDI));
        Map<String, Object> response = validResponse(context, condition);
        Map<String, Object> technical = map(response.get("technicalSpecification"));
        Map<String, Object> snapshotEvaluation = map(technical.get("snapshotEvaluation"));
        snapshotEvaluation.put("threshold", Map.of("operator", "GREATER_OR_EQUAL", "value", 1));

        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Origin filter report",
                "Scheduled ServiceData origin filter report test",
                "Per la localita Pero fammi sapere quanti hanno come origine Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("SNAPSHOT_REPORT route requires REPORT_COUNT or REPORT_MATCHING_JOURNEYS");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesOriginFilterReportResponseUsingReportCount() {
        ScheduledServiceDataLocationContext context = originFilterContext();
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("callStart.stopPoint.id", "EQUALS", GARIBALDI));
        TestFixture fixture = fixture(json(validReportResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Origin filter report",
                "Scheduled ServiceData origin filter report test",
                "Per la localita Pero fammi sapere quanti hanno come origine Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("serviceDataQuery")).get("stopPoints"))
                .isEqualTo(List.of(PERO));
        assertThat(map(outcome.technicalSpecification().get("snapshotEvaluation")))
                .containsEntry("mode", "REPORT_COUNT")
                .containsEntry("threshold", null);
        assertThat(map(outcome.technicalSpecification().get("outputPolicy")))
                .containsEntry("emit", "EVERY_RUN")
                .containsEntry("includeCount", true);
    }

    @Test
    void rejectsReportResponseUsingCountMatchingJourneysWithoutThreshold() {
        Map<String, Object> response = validReportResponse(explicitContext(), delayCondition());
        Map<String, Object> technical = map(response.get("technicalSpecification"));
        Map<String, Object> snapshotEvaluation = map(technical.get("snapshotEvaluation"));
        snapshotEvaluation.put("mode", "COUNT_MATCHING_JOURNEYS");
        snapshotEvaluation.put("threshold", null);

        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled report",
                "Scheduled ServiceData report test",
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("SNAPSHOT_REPORT route requires REPORT_COUNT or REPORT_MATCHING_JOURNEYS");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void rejectsExplicitFrequencyWhenLlmMarksScheduleDefaulted() {
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), delayCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled report",
                "Scheduled ServiceData report test",
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("schedule.defaulted=true");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesExplicitFrequencyWhenScheduleMatchesBackendHints() {
        Map<String, Object> response = validReportResponse(explicitContext(), delayCondition());
        withSchedule(response, 600, false, "Ogni 10 minuti");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled report",
                "Scheduled ServiceData report test",
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("schedule")))
                .containsEntry("frequencySeconds", 600)
                .containsEntry("defaulted", false)
                .containsEntry("rawText", "Ogni 10 minuti");
    }

    @Test
    void rejectsExplicitLookaheadWhenLlmUsesDefaultWindow() {
        TestFixture fixture = fixture(json(validResponse(explicitContext(), delayCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled condition",
                "Scheduled ServiceData condition test",
                "Fammi sapere se ci sono almeno due treni in ritardo a Garibaldi FS nelle prossime 2 ore",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("NOW_PLUS_DURATION");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesExplicitLookaheadWhenTimeWindowMatchesBackendHints() {
        Map<String, Object> response = validResponse(explicitContext(), delayCondition());
        withLookahead(response, 120, false, "NOW_PLUS_DURATION", "nelle prossime 2 ore");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled condition",
                "Scheduled ServiceData condition test",
                "Fammi sapere se ci sono almeno due treni in ritardo a Garibaldi FS nelle prossime 2 ore",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(map(outcome.technicalSpecification().get("serviceDataQuery")).get("timeWindow")))
                .containsEntry("endMode", "NOW_PLUS_DURATION")
                .containsEntry("lookaheadMinutes", 120)
                .containsEntry("defaulted", false)
                .containsEntry("rawText", "nelle prossime 2 ore");
    }

    @Test
    void verifiesDepartureBetweenJourneyTimeFilter() {
        Map<String, Object> response = validReportResponse(explicitContext(),
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00"))));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Departure time report",
                "Scheduled ServiceData departure time report test",
                "Fammi sapere quante corse partono da Garibaldi FS tra le 10:00 e le 12:00",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsJourneyTimePromptWhenLlmOmitsTimeFilter() {
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), delayCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Departure time report",
                "Scheduled ServiceData departure time report test",
                "Fammi sapere quante corse partono da Garibaldi FS tra le 10:00 e le 12:00",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Scheduled journey time filter was requested");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void rejectsArrivalJourneyTimePromptWhenLlmUsesDepartureField() {
        Map<String, Object> response = validResponse(explicitContext(),
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("14:00:00", "16:00:00"))));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Arrival time condition",
                "Scheduled ServiceData arrival time condition test",
                "Fammi sapere se c'e almeno una corsa che arriva a Garibaldi FS tra le 14:00 e le 16:00",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("direction-compatible");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesOldLocalTimeBetweenInternalHintShapeAfterParserNormalization() {
        Map<String, Object> oldShapeLeaf = new LinkedHashMap<>();
        oldShapeLeaf.put("field", "callStart.departureTime");
        oldShapeLeaf.put("operator", "LOCAL_TIME_BETWEEN");
        oldShapeLeaf.put("startLocalTime", "10:00:00");
        oldShapeLeaf.put("endLocalTime", "12:00:00");
        oldShapeLeaf.put("timezone", "Europe/Rome");
        Map<String, Object> response = validReportResponse(explicitContext(),
                conditionAnyElement("stopPointsJourneyDetails[]", oldShapeLeaf));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Departure time report",
                "Scheduled ServiceData departure time report test",
                "Fammi sapere quante corse partono da Garibaldi FS tra le 10:00 e le 12:00",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        Map<String, Object> leaf = nestedAnyElementConditions(outcome);
        assertThat(map(leaf.get("value")))
                .containsEntry("start", "10:00:00")
                .containsEntry("end", "12:00:00")
                .containsEntry("timezone", "Europe/Rome");
    }

    @Test
    void rejectsAnyElementConditionsDirectArrayFromLlm() {
        Map<String, Object> response = validReportResponse(explicitContext(),
                conditionAnyElementWithRawConditions("stopPointsJourneyDetails[]", List.of(
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00")))));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Departure time report",
                "Scheduled ServiceData departure time report test",
                "Fammi sapere quante corse partono da Garibaldi FS tra le 10:00 e le 12:00",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("anyElement.conditions must be an object");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesArrivalBooleanBetweenTimeWithCorrectShape() {
        Map<String, Object> response = validBooleanResponse(explicitContext(),
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callEnd.arrivalTime", "LOCAL_TIME_BETWEEN", timeValue("14:00:00", "16:00:00"))));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Arrival time boolean",
                "Scheduled ServiceData arrival time boolean test",
                "Fammi sapere se c'e almeno una corsa che arriva a Garibaldi FS tra le 14:00 e le 16:00",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("snapshotEvaluation")))
                .containsEntry("mode", "BOOLEAN_EXISTS")
                .containsEntry("threshold", null);
        assertThat(map(outcome.technicalSpecification().get("outputPolicy")))
                .containsEntry("emit", "ON_MATCH")
                .containsEntry("includeMatchingJourneys", true);
    }

    @Test
    void rejectsBooleanOutputPolicyWithoutCountOrMatchingJourneys() {
        Map<String, Object> response = validBooleanResponse(explicitContext(),
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callEnd.arrivalTime", "LOCAL_TIME_BETWEEN", timeValue("14:00:00", "16:00:00"))));
        map(response.get("technicalSpecification")).put("outputPolicy", Map.of(
                "emit", "ON_MATCH",
                "includeCount", false,
                "includeMatchingJourneys", false));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Arrival time boolean",
                "Scheduled ServiceData arrival time boolean test",
                "Fammi sapere se c'e almeno una corsa che arriva a Garibaldi FS tra le 14:00 e le 16:00",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Scheduled outputPolicy must include at least count or matching journeys");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesFrequencyLookaheadAndJourneyTimeTogether() {
        Map<String, Object> response = validReportResponse(explicitContext(),
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("18:00:00", "20:00:00"))));
        withSchedule(response, 600, false, "Ogni 10 minuti");
        withLookahead(response, 120, false, "NOW_PLUS_DURATION", "nelle prossime 2 ore");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Mixed temporal report",
                "Scheduled ServiceData mixed temporal report test",
                "Ogni 10 minuti dimmi quante corse partono da Garibaldi FS tra le 18:00 e le 20:00 nelle prossime 2 ore",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("schedule")))
                .containsEntry("frequencySeconds", 600)
                .containsEntry("defaulted", false);
        assertThat(map(map(outcome.technicalSpecification().get("serviceDataQuery")).get("timeWindow")))
                .containsEntry("lookaheadMinutes", 120)
                .containsEntry("defaulted", false);
    }

    @Test
    void verifiesMultiMonitoredResponseWithValuesArray() {
        ScheduledServiceDataLocationContext context = multiMonitoredContext();
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leafValues("arrivalStatuses[].status", "CONTAINS_ANY", List.of("ARRIVING")));
        TestFixture fixture = fixture(json(validResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled multi monitored",
                "Scheduled ServiceData multi monitored test",
                "Fammi sapere quando a Varedo e Palazzolo Milanese ci sono due treni in arrivo",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(outcome.technicalSpecification().get("serviceDataQuery")).get("stopPoints"))
                .isEqualTo(List.of(VAREDO, PALAZZOLO));
    }

    @Test
    void verifiesDeparturePlatformEqualityConstraint() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledDeparturePlatform.dsc", "EQUAL_PLATFORM", "3"));
        TestFixture fixture = fixture(json(validResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled platform threshold",
                "Scheduled ServiceData platform threshold test",
                "Avvertimi quando sono presenti almeno 2 treni a Gorla che partono dal binario 3",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsWhenPlatformPromptOmitsPlatformCondition() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureDelay.delay", "GREATER_THAN", 0));
        TestFixture fixture = fixture(json(validResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled platform omitted",
                "Scheduled ServiceData platform omitted test",
                "Avvertimi quando sono presenti almeno 2 treni a Gorla che partono dal binario 3",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Required platform constraint from backend hints");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void verifiesDeparturePlatformGreaterThanReport() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN", 2));
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled platform report",
                "Scheduled ServiceData platform report test",
                "Fammi sapere quanti treni partono da Garibaldi FS da binario maggiore di 2",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void verifiesGenericPlatformChangeBoolean() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("changes", "CONTAINS", "PLATFORM_CHANGED"));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled platform change",
                "Scheduled ServiceData platform change test",
                "Fammi sapere se a San Siro Stadio ci sono treni che hanno subito un cambio di binario",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsPlatformChangeMappedToEventField() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("payload.ongroundServiceEvent.eventsType", "CONTAINS", "PLATFORM_CHANGED"));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled platform change event field",
                "Scheduled ServiceData platform change event field test",
                "Fammi sapere se a San Siro Stadio ci sono treni che hanno subito un cambio di binario",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("payload.ongroundServiceEvent");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void verifiesChangedDestinationReport() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("changes", "CONTAINS", "CHANGED_DESTINATION"));
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled changed destination",
                "Scheduled ServiceData changed destination report test",
                "Fammi sapere quanti treni a Garibaldi FS hanno subito cambio destinazione",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void verifiesGenericCancellationBoolean() {
        Map<String, Object> condition = genericJourneyCancellationCondition();
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled cancellation",
                "Scheduled ServiceData cancellation boolean test",
                "Fammi sapere se a Garibaldi FS ci sono treni cancellati",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(String.valueOf(outcome.technicalSpecification().get("snapshotEvaluation")))
                .contains("arrivalStatuses[].status", "ARRIVAL_CANCELLATION")
                .contains("departureStatuses[].status", "DEPARTURE_CANCELLATION")
                .contains("passingType", "DESTINATION", "ORIGIN")
                .doesNotContain("PARTIALLY_CANCELLATION")
                .doesNotContain("field=changes");
    }

    @Test
    void verifiesSuppressedJourneysCountAtLeccoWithStatusAndPassingTypeSemantics() {
        ScheduledServiceDataLocationContext context = leccoContext();
        Map<String, Object> response = validReportResponse(context, genericJourneyCancellationCondition());
        withSchedule(response, 600, false, "ogni 10 min");
        response.put("requirementCoverage", Map.of(
                "requirements", List.of(Map.of(
                        "text", "Recurring count of suppressed journeys at Lecco",
                        "required", true,
                        "mappable", true,
                        "mappedBy", List.of(
                                "serviceDataQuery.stopPoints",
                                "schedule.frequencySeconds",
                                "stopPointsJourneyDetails[].arrivalStatuses[].status",
                                "stopPointsJourneyDetails[].departureStatuses[].status",
                                "stopPointsJourneyDetails[].passingType",
                                "outputPolicy.emit",
                                "outputPolicy.includeCount"))),
                "allRequiredRequirementsMapped", true));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled Suppressed Journeys Count At Lecco",
                "Verify scheduled snapshot routing for a recurring count of suppressed or cancelled journeys at Lecco.",
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Lecco",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(map(outcome.technicalSpecification().get("schedule"))).containsEntry("frequencySeconds", 600);
        assertThat(map(outcome.technicalSpecification().get("serviceDataQuery")).get("stopPoints")).isEqualTo(List.of(LECCO));
        assertThat(map(outcome.technicalSpecification().get("snapshotEvaluation"))).containsEntry("mode", "REPORT_COUNT");
        String snapshot = String.valueOf(outcome.technicalSpecification().get("snapshotEvaluation"));
        assertThat(snapshot)
                .contains("arrivalStatuses[].status", "ARRIVAL_CANCELLATION")
                .contains("departureStatuses[].status", "DEPARTURE_CANCELLATION")
                .contains("passingType", "DESTINATION", "ORIGIN")
                .doesNotContain("field=changes")
                .doesNotContain("PARTIALLY_CANCELLATION");
        assertThat(map(outcome.technicalSpecification().get("outputPolicy")))
                .containsEntry("emit", "EVERY_RUN")
                .containsEntry("includeCount", true);
    }

    @Test
    void verifiesCancelledJourneysCountAtLeccoWithoutChanges() {
        ScheduledServiceDataLocationContext context = leccoContext();
        Map<String, Object> response = validReportResponse(context, genericJourneyCancellationCondition());
        withSchedule(response, 600, false, "ogni 10 min");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled Cancelled Journeys Count At Lecco",
                "Verify scheduled snapshot routing for a recurring count of cancelled journeys at Lecco.",
                "Avvertimi ogni 10 min su quante corse cancellate ci sono a Lecco",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        String snapshot = String.valueOf(outcome.technicalSpecification().get("snapshotEvaluation"));
        assertThat(snapshot)
                .contains("arrivalStatuses[].status", "ARRIVAL_CANCELLATION")
                .contains("departureStatuses[].status", "DEPARTURE_CANCELLATION")
                .contains("passingType", "DESTINATION", "ORIGIN")
                .doesNotContain("field=changes");
    }

    @Test
    void verifiesChangedOriginStillUsesChanges() {
        ScheduledServiceDataLocationContext context = leccoContext();
        Map<String, Object> response = validReportResponse(context,
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("changes", "CONTAINS", "CHANGED_ORIGIN")));
        withSchedule(response, 600, false, "ogni 10 min");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled Changed Origin Count At Lecco",
                "Verify scheduled snapshot routing for a recurring count of changed origin journeys at Lecco.",
                "Avvertimi ogni 10 min su quante corse hanno cambio origine a Lecco",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(String.valueOf(outcome.technicalSpecification().get("snapshotEvaluation")))
                .contains("field=changes", "value=CHANGED_ORIGIN");
    }

    @Test
    void normalizesGenericCancellationMappedOnlyToChanges() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("any", List.of(
                        leaf("changes", "CONTAINS", "CANCELLATION"),
                        leaf("changes", "CONTAINS", "PARTIALLY_CANCELLATION"))));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled cancellation",
                "Scheduled ServiceData cancellation boolean test",
                "Fammi sapere se a Garibaldi FS ci sono treni cancellati",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(String.valueOf(outcome.technicalSpecification().get("snapshotEvaluation")))
                .contains("arrivalStatuses[].status", "departureStatuses[].status", "passingType")
                .doesNotContain("field=changes")
                .doesNotContain("PARTIALLY_CANCELLATION");
    }

    @Test
    void verifiesTotalExclusionBoolean() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("exclusion.totalExclusion", "EQUALS", true));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled total exclusion",
                "Scheduled ServiceData total exclusion boolean test",
                "Fammi sapere se a Garibaldi FS c'e almeno un treno total exclusion",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void verifiesDepartureSuppressedJourneysCountAtLeccoWithDirectedCancellationSemantics() {
        ScheduledServiceDataLocationContext context = leccoContext();
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"));
        Map<String, Object> response = validReportResponse(context, condition);
        withSchedule(response, 600, false, "ogni 10 min");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled Departure Suppressed Journeys Count At Lecco",
                "Verify scheduled snapshot routing for a recurring count of departure suppressed at Lecco.",
                "Avvertimi ogni 10 min su quante corse soppresse in partenza ci sono a Lecco",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        String snapshot = String.valueOf(outcome.technicalSpecification().get("snapshotEvaluation"));
        assertThat(snapshot)
                .contains("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION")
                .contains("arrivalStatuses[].status", "NOT_CONTAINS", "ARRIVAL_CANCELLATION")
                .doesNotContain("passingType")
                .doesNotContain("field=changes")
                .doesNotContain("operator=CONTAINS, value=ARRIVAL_CANCELLATION");
    }

    @Test
    void rejectsCancellationMappedToEventField() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("payload.ongroundServiceEvent.eventsType", "CONTAINS", "CANCELLATION"));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled cancellation event field",
                "Scheduled ServiceData cancellation event field test",
                "Fammi sapere se a Garibaldi FS ci sono treni cancellati",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("payload.ongroundServiceEvent");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void rejectsWhenCancellationPromptOmitsCancellationCondition() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureDelay.delay", "GREATER_THAN", 0));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Scheduled cancellation omitted",
                "Scheduled ServiceData cancellation omitted test",
                "Fammi sapere se a Garibaldi FS ci sono treni cancellati",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Generic journey cancellation");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void rejectsMalformedInOperatorUsingValueArray() {
        ScheduledServiceDataLocationContext context = originFilterContext();
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("callStart.stopPoint.id", "IN", List.of(GARIBALDI)));
        TestFixture fixture = fixture(json(validReportResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Malformed IN report",
                "Scheduled ServiceData malformed IN test",
                "Per la localita Pero fammi sapere quanti hanno come origine Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.summary()).isEqualTo("Scheduled ServiceData verification failed validation.");
        assertThat(outcome.rejectedReason()).contains("operator IN requires a non-empty values array");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void verifiesSpecificSuppressedStopReport() {
        ScheduledServiceDataLocationContext context = cancelledStopContext("Milano Bruzzano Parco", "Palazzolo Milanese", PALAZZOLO);
        Map<String, Object> condition = cancelledStopCondition(leaf("stopPoint.id", "EQUALS", PALAZZOLO));
        TestFixture fixture = fixture(json(validReportResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Suppressed stop report",
                "Scheduled ServiceData suppressed stop report test",
                "Fammi sapere quante corse a Milano Bruzzano Parco hanno come fermata soppressa Palazzolo Milanese",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification()).isNotNull();
    }

    @Test
    void rejectsSpecificSuppressedStopMappedToJourneyCancellation() {
        ScheduledServiceDataLocationContext context = cancelledStopContext("Milano Bruzzano Parco", "Palazzolo Milanese", PALAZZOLO);
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("changes", "CONTAINS", "CANCELLATION"));
        TestFixture fixture = fixture(json(validReportResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Suppressed stop wrong mapping",
                "Scheduled ServiceData suppressed stop wrong mapping test",
                "Fammi sapere quante corse a Milano Bruzzano Parco hanno come fermata soppressa Palazzolo Milanese",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("not covered");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void verifiesSkippedStopReport() {
        ScheduledServiceDataLocationContext context = cancelledStopContext("Garibaldi FS", "Bovisa", PALAZZOLO);
        Map<String, Object> condition = cancelledStopCondition(leaf("stopPoint.id", "EQUALS", PALAZZOLO));
        TestFixture fixture = fixture(json(validReportResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Skipped stop report",
                "Scheduled ServiceData skipped stop report test",
                "Per Garibaldi FS dimmi quante corse saltano Bovisa",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void verifiesGenericSuppressedStopsBoolean() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("field", "nextCancelledCalls", "operator", "NOT_EMPTY"));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Generic suppressed stops",
                "Scheduled ServiceData generic suppressed stops test",
                "Fammi sapere se a Garibaldi FS ci sono treni con fermate soppresse",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsGenericSuppressedStopsWhenOmitted() {
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), delayCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Generic suppressed stops omitted",
                "Scheduled ServiceData generic suppressed stops omitted test",
                "Fammi sapere se a Garibaldi FS ci sono treni con fermate soppresse",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("nextCancelledCalls");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void verifiesGenericReplacementReport() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("field", "isReplacementOf", "operator", "NOT_EMPTY"));
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Replacement report",
                "Scheduled ServiceData replacement report test",
                "Fammi sapere quante corse sostitutive ci sono a Porto di Mare",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void verifiesGenericReplacementBoolean() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("field", "isReplacementOf", "operator", "NOT_EMPTY"));
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Replacement boolean",
                "Scheduled ServiceData replacement boolean test",
                "Fammi sapere se a Garibaldi FS ci sono corse sostitutive",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void verifiesReplacementStopReport() {
        ScheduledServiceDataLocationContext context = replacementStopContext("Garibaldi FS", "Bovisa", PALAZZOLO);
        Map<String, Object> condition = replacementStopCondition(leaf("stopPointId.id", "EQUALS", PALAZZOLO));
        TestFixture fixture = fixture(json(validReportResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Replacement stop report",
                "Scheduled ServiceData replacement stop report test",
                "Fammi sapere quante corse a Garibaldi FS hanno fermata sostitutiva Bovisa",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void verifiesReplacementStopWithDepartureTypeReport() {
        ScheduledServiceDataLocationContext context = replacementStopContext("Garibaldi FS", "Bovisa", PALAZZOLO);
        Map<String, Object> condition = replacementStopCondition(Map.of("all", List.of(
                leaf("stopPointId.id", "EQUALS", PALAZZOLO),
                leaf("replacementType", "EQUALS", "DEPARTURE"))));
        TestFixture fixture = fixture(json(validReportResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Replacement stop type report",
                "Scheduled ServiceData replacement stop type report test",
                "Fammi sapere quante corse a Garibaldi FS hanno fermata sostitutiva Bovisa in partenza",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsUnsupportedReplacementSourceRouteVerifiedOutput() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("field", "isReplacementOf", "operator", "NOT_EMPTY"));
        Map<String, Object> response = validReportResponse(explicitContext(), condition);
        withSchedule(response, 7200, false, "Ogni 2 ore");
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Unsupported replacement source route",
                "Scheduled ServiceData unsupported replacement source route test",
                "Ogni 2 ore voglio sapere le corse sostitutive a Porto di Mare per la tratta che inizia a Porto di Mare fino a Milano Affori",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Replacement source route start/end stop points are not supported");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void verifiesComplexDelayPlatformChangeAndExcludedDestination() {
        ScheduledServiceDataLocationContext context = excludedDestinationContext();
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(
                        Map.of("any", List.of(
                                leaf("arrivalDelay.delay", "GREATER_THAN", 0),
                                leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        leaf("changes", "CONTAINS", "PLATFORM_CHANGED"),
                        leafValues("callEnd.stopPoint.id", "NOT_IN", List.of(GARIBALDI)))));
        Map<String, Object> response = validResponse(context, condition);
        Map<String, Object> snapshotEvaluation = map(map(response.get("technicalSpecification")).get("snapshotEvaluation"));
        snapshotEvaluation.put("threshold", Map.of("operator", "GREATER_THAN", "value", 5));
        TestFixture fixture = fixture(json(response));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Complex scheduled condition",
                "Scheduled ServiceData complex condition test",
                "Fammi sapere se il numero di treni in ritardo e che hanno subito un cambio di binario a Buonarroti è maggiore di 5. L'importante è che non hanno come destinazione Tre Torri",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsComplexPromptWhenDestinationExclusionIsOmitted() {
        ScheduledServiceDataLocationContext context = excludedDestinationContext();
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(
                        leaf("departureDelay.delay", "GREATER_THAN", 0),
                        leaf("changes", "CONTAINS", "PLATFORM_CHANGED"))));
        TestFixture fixture = fixture(json(validResponse(context, condition)));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Complex scheduled condition missing exclusion",
                "Scheduled ServiceData complex condition missing exclusion test",
                "Fammi sapere se il numero di treni in ritardo e che hanno subito un cambio di binario a Buonarroti è maggiore di 5. L'importante è che non hanno come destinazione Tre Torri",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                context);

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Tre Torri");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void rejectsWifiPromptWhenLlmIgnoresUnsupportedConstraint() {
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), delayCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Unsupported wifi",
                "Scheduled ServiceData unsupported wifi test",
                "Fammi sapere il numero di treni con wifi a bordo e in ritardo in partenza maggiore di 10 min a Portello",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("wifi");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void rejectsCarriagesPromptWhenLlmIgnoresUnsupportedConstraint() {
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), validCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Unsupported carriages",
                "Scheduled ServiceData unsupported carriages test",
                "Fammi sapere il numero di treni con più di 10 carrozze a Gerusalemme",
                route(AlertRouteIntentKind.SNAPSHOT_REPORT, AlertRouteOutputMode.EVERY_RUN_REPORT),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("carriages");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    @Test
    void rejectsAbsenceOverDurationPromptWhenLlmReturnsVerified() {
        TestFixture fixture = fixture(json(validBooleanResponse(explicitContext(), validCondition())));

        AlertVerificationOutcome outcome = fixture.service.verify(
                "ALRT1",
                "Unsupported absence",
                "Scheduled ServiceData unsupported absence test",
                "Fammi sapere se per 30 minuti non passa nessun treno a Garibaldi FS",
                route(AlertRouteIntentKind.SNAPSHOT_CONDITION, AlertRouteOutputMode.ON_MATCH),
                explicitContext());

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("absence");
        assertThat(outcome.technicalSpecification()).isNull();
    }

    private TestFixture fixture(String rawResponse) {
        ScheduledAlertVerificationService service = new ScheduledAlertVerificationService();
        service.promptBuilder = promptBuilder();
        service.responseParser = new ScheduledAlertVerificationResponseParser();
        service.outcomeValidator = new ScheduledAlertVerificationOutcomeValidator();
        service.temporalHintsExtractor = temporalHintsExtractor();
        service.platformHintsExtractor = new ScheduledAlertPlatformHintsExtractor();
        service.changeHintsExtractor = new ScheduledAlertChangeHintsExtractor();
        service.journeyCancellationHintsExtractor = new ScheduledAlertJourneyCancellationHintsExtractor();
        service.cancelledCallHintsExtractor = new ScheduledAlertCancelledCallHintsExtractor();
        service.replacementHintsExtractor = new ScheduledAlertReplacementHintsExtractor();
        service.unsupportedConstraintDetector = new ScheduledUnsupportedConstraintDetector();
        service.llmGateway = mock(Instance.class);

        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse(
                rawResponse,
                "FAKE",
                "scheduled-model",
                null,
                null,
                "resp-1"));
        return new TestFixture(service, gateway);
    }

    private TestFixture fixtureWithProviderFailure(RuntimeException exception) {
        ScheduledAlertVerificationService service = new ScheduledAlertVerificationService();
        service.promptBuilder = promptBuilder();
        service.responseParser = new ScheduledAlertVerificationResponseParser();
        service.outcomeValidator = new ScheduledAlertVerificationOutcomeValidator();
        service.temporalHintsExtractor = temporalHintsExtractor();
        service.platformHintsExtractor = new ScheduledAlertPlatformHintsExtractor();
        service.changeHintsExtractor = new ScheduledAlertChangeHintsExtractor();
        service.journeyCancellationHintsExtractor = new ScheduledAlertJourneyCancellationHintsExtractor();
        service.cancelledCallHintsExtractor = new ScheduledAlertCancelledCallHintsExtractor();
        service.replacementHintsExtractor = new ScheduledAlertReplacementHintsExtractor();
        service.unsupportedConstraintDetector = new ScheduledUnsupportedConstraintDetector();
        service.llmGateway = mock(Instance.class);

        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenThrow(exception);
        return new TestFixture(service, gateway);
    }

    private ScheduledAlertVerificationPromptBuilder promptBuilder() {
        AiConfiguration configuration = mock(AiConfiguration.class);
        AiConfiguration.AlertScheduledVerify scheduledVerify = mock(AiConfiguration.AlertScheduledVerify.class);
        when(configuration.alertScheduledVerify()).thenReturn(scheduledVerify);
        when(scheduledVerify.model()).thenReturn("scheduled-model");
        when(scheduledVerify.temperature()).thenReturn(0.1);
        when(scheduledVerify.maxOutputTokens()).thenReturn(3500);

        TemporalConfiguration temporalConfiguration = mock(TemporalConfiguration.class);
        when(temporalConfiguration.defaultZone()).thenReturn("Europe/Rome");

        ScheduledAlertVerificationPromptBuilder builder = new ScheduledAlertVerificationPromptBuilder();
        builder.aiConfiguration = configuration;
        builder.temporalConfiguration = temporalConfiguration;
        builder.defaultPollingFrequencySeconds = 600;
        builder.defaultLookaheadMinutes = 480;
        return builder;
    }

    private ScheduledAlertTemporalHintsExtractor temporalHintsExtractor() {
        ScheduledAlertTemporalHintsExtractor extractor = new ScheduledAlertTemporalHintsExtractor();
        extractor.defaultFrequencySeconds = 600;
        extractor.minFrequencySeconds = 60;
        extractor.maxFrequencySeconds = 86400;
        extractor.defaultLookaheadMinutes = 480;
        extractor.minLookaheadMinutes = 1;
        extractor.maxLookaheadMinutes = 1440;
        return extractor;
    }

    private AlertRouteUnderstandingResult route(
            AlertRouteIntentKind intentKind,
            AlertRouteOutputMode outputMode) {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                intentKind,
                outputMode,
                true,
                true,
                false,
                true,
                intentKind == AlertRouteIntentKind.SNAPSHOT_CONDITION,
                outputMode == AlertRouteOutputMode.EVERY_RUN_REPORT,
                0.95,
                "Scheduled ServiceData route.",
                null,
                List.of());
    }

    private Map<String, Object> validResponse(
            ScheduledServiceDataLocationContext context,
            Map<String, Object> condition) {
        Map<String, Object> technicalSpecification = technicalSpecification(
                context,
                "COUNT_MATCHING_JOURNEYS",
                condition,
                Map.of("operator", "GREATER_OR_EQUAL", "value", 2),
                "ON_MATCH");
        return response(
                "VERIFIED",
                "The scheduled alert can be verified.",
                null,
                technicalSpecification,
                blueprint(context, "COUNT_MATCHING_JOURNEYS", "ON_MATCH"));
    }

    private Map<String, Object> validReportResponse(
            ScheduledServiceDataLocationContext context,
            Map<String, Object> condition) {
        Map<String, Object> technicalSpecification = technicalSpecification(
                context,
                "REPORT_COUNT",
                condition,
                null,
                "EVERY_RUN");
        return response(
                "VERIFIED",
                "The scheduled report can be verified.",
                null,
                technicalSpecification,
                blueprint(context, "REPORT_COUNT", "EVERY_RUN"));
    }

    private Map<String, Object> validBooleanResponse(
            ScheduledServiceDataLocationContext context,
            Map<String, Object> condition) {
        Map<String, Object> technicalSpecification = technicalSpecification(
                context,
                "BOOLEAN_EXISTS",
                condition,
                null,
                "ON_MATCH");
        return response(
                "VERIFIED",
                "The scheduled boolean alert can be verified.",
                null,
                technicalSpecification,
                blueprint(context, "BOOLEAN_EXISTS", "ON_MATCH"));
    }

    private Map<String, Object> rejectedResponse(String reason) {
        return response("REJECTED", "The scheduled alert cannot be verified.", reason, null, null);
    }

    private Map<String, Object> response(
            String decision,
            String summary,
            String rejectedReason,
            Map<String, Object> technicalSpecification,
            Map<String, Object> blueprint) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("decision", decision);
        response.put("summary", summary);
        response.put("rejectedReason", rejectedReason);
        response.put("confidence", "VERIFIED".equals(decision) ? 0.91 : 0.0);
        response.put("requiredSources", List.of("SERVICE_DATA"));
        response.put("interpreterType", "SCHEDULED_INTERPRETER");
        response.put("accessMode", "SERVICE_DATA_API_SNAPSHOT");
        response.put("triggerType", "SCHEDULE");
        response.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        response.put("inputModel", "ServiceDataStopPointJourneysV2");
        response.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        response.put("targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"));
        response.put("requirementCoverage", coverage());
        response.put("technicalSpecification", technicalSpecification);
        response.put("agentBlueprintPreview", blueprint);
        response.put("warnings", List.of());
        response.put("safetyChecks", List.of("No executable code generated."));
        return response;
    }

    private Map<String, Object> technicalSpecification(
            ScheduledServiceDataLocationContext context,
            String evaluationMode,
            Map<String, Object> condition,
            Map<String, Object> threshold,
            String emitMode) {
        Map<String, Object> snapshotEvaluation = new LinkedHashMap<>();
        snapshotEvaluation.put("mode", evaluationMode);
        snapshotEvaluation.put("journeyPath", "stopPointsJourneyDetails[]");
        snapshotEvaluation.put("condition", condition);
        snapshotEvaluation.put("threshold", threshold);

        return new LinkedHashMap<>(Map.ofEntries(
                Map.entry("schemaVersion", "iia.alert.technical-specification/v2"),
                Map.entry("source", "SERVICE_DATA"),
                Map.entry("interpreterType", "SCHEDULED_INTERPRETER"),
                Map.entry("accessMode", "SERVICE_DATA_API_SNAPSHOT"),
                Map.entry("inputModel", "ServiceDataStopPointJourneysV2"),
                Map.entry("outputModel", "AgentOutput.CANDIDATE_SUGGESTION"),
                Map.entry("triggerType", "SCHEDULE"),
                Map.entry("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH"),
                Map.entry("schedule", Map.of("frequencySeconds", 600, "defaulted", true)),
                Map.entry("serviceDataQuery", serviceDataQuery(context)),
                Map.entry("snapshotEvaluation", snapshotEvaluation),
                Map.entry("outputPolicy", Map.of(
                        "emit", emitMode,
                        "includeCount", true,
                        "includeMatchingJourneys", true)),
                Map.entry("deduplicationKeyTemplate", "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}")));
    }

    private Map<String, Object> blueprint(
            ScheduledServiceDataLocationContext context,
            String evaluationMode,
            String emitMode) {
        return new LinkedHashMap<>(Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ScheduledServiceDataSnapshotAlertAgent",
                "triggerType", "SCHEDULE",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                "parameters", Map.of(
                        "serviceDataQuery", serviceDataQuery(context),
                        "snapshotEvaluation", Map.of("mode", evaluationMode),
                        "outputPolicy", Map.of("emit", emitMode)),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION")));
    }

    private Map<String, Object> serviceDataQuery(ScheduledServiceDataLocationContext context) {
        return Map.of(
                "operation", "POST /v2/stoppointjourneys",
                "monitoringScope", String.valueOf(context.monitoringScope()),
                "stopPoints", context.serviceDataApiStopPoints(),
                "requiresAllKnownStopPoints", context.requiresAllKnownStopPoints(),
                "timeWindow", Map.of(
                        "startMode", "NOW_TRUNCATED_TO_MINUTE",
                        "endMode", "NOW_PLUS_DEFAULT_LOOKAHEAD",
                        "lookaheadMinutes", 480,
                        "defaulted", true));
    }

    private Map<String, Object> timeValue(String start, String end) {
        return Map.of(
                "start", start,
                "end", end,
                "timezone", "Europe/Rome");
    }

    private void withSchedule(
            Map<String, Object> response,
            int frequencySeconds,
            boolean defaulted,
            String rawText) {
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("frequencySeconds", frequencySeconds);
        schedule.put("defaulted", defaulted);
        schedule.put("rawText", rawText);
        map(response.get("technicalSpecification")).put("schedule", schedule);
    }

    private void withLookahead(
            Map<String, Object> response,
            int lookaheadMinutes,
            boolean defaulted,
            String endMode,
            String rawText) {
        Map<String, Object> technical = map(response.get("technicalSpecification"));
        Map<String, Object> serviceDataQuery = new LinkedHashMap<>(map(technical.get("serviceDataQuery")));
        Map<String, Object> timeWindow = new LinkedHashMap<>();
        timeWindow.put("startMode", "NOW_TRUNCATED_TO_MINUTE");
        timeWindow.put("endMode", endMode);
        timeWindow.put("lookaheadMinutes", lookaheadMinutes);
        timeWindow.put("defaulted", defaulted);
        timeWindow.put("rawText", rawText);
        serviceDataQuery.put("timeWindow", timeWindow);
        technical.put("serviceDataQuery", serviceDataQuery);
    }

    private Map<String, Object> validCondition() {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING"));
    }

    private Map<String, Object> delayCondition() {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureDelay.delay", "GREATER_THAN", 0));
    }

    private Map<String, Object> genericJourneyCancellationCondition() {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("any", List.of(
                        Map.of("all", List.of(
                                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                                leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"))),
                        Map.of("all", List.of(
                                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                                leaf("passingType", "EQUALS", "DESTINATION"))),
                        Map.of("all", List.of(
                                leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"),
                                leaf("passingType", "EQUALS", "ORIGIN"))))));
    }

    private Map<String, Object> conditionAnyElement(String path, Map<String, Object> conditions) {
        return condition(Map.of("anyElement", Map.of(
                "path", path,
                "conditions", conditions)));
    }

    private Map<String, Object> conditionAnyElementWithRawConditions(String path, Object conditions) {
        return condition(Map.of("anyElement", Map.of(
                "path", path,
                "conditions", conditions)));
    }

    private Map<String, Object> cancelledStopCondition(Map<String, Object> leaf) {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(Map.of("anyElement", Map.of(
                        "path", "nextCancelledCalls[]",
                        "conditions", leaf)))));
    }

    private Map<String, Object> replacementStopCondition(Map<String, Object> leaf) {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(Map.of("anyElement", Map.of(
                        "path", "replacement.stopPointReplacements[]",
                        "conditions", leaf)))));
    }

    private Map<String, Object> condition(Map<String, Object> body) {
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH");
        condition.putAll(body);
        return condition;
    }

    private Map<String, Object> leaf(String field, String operator, Object value) {
        return Map.of(
                "field", field,
                "operator", operator,
                "value", value);
    }

    private Map<String, Object> leafValues(String field, String operator, List<?> values) {
        return Map.of(
                "field", field,
                "operator", operator,
                "values", values);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedAnyElementConditions(AlertVerificationOutcome outcome) {
        Map<String, Object> snapshotEvaluation = map(outcome.technicalSpecification().get("snapshotEvaluation"));
        Map<String, Object> condition = map(snapshotEvaluation.get("condition"));
        Map<String, Object> anyElement = map(condition.get("anyElement"));
        return (Map<String, Object>) anyElement.get("conditions");
    }

    private ScheduledServiceDataLocationContext explicitContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                "Gorla",
                "Gorla",
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(GORLA),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                List.of(),
                List.of(),
                List.of(GORLA),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(GORLA),
                        false));
    }

    private ScheduledServiceDataLocationContext leccoContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                "Lecco",
                "Lecco",
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(LECCO),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                List.of(),
                List.of(),
                List.of(LECCO),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(LECCO),
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

    private ScheduledServiceDataLocationContext originFilterContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                "Pero",
                "Pero",
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(PERO),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                ""));
        List<ScheduledServiceDataResolvedLocation> filters = List.of(new ScheduledServiceDataResolvedLocation(
                "Garibaldi FS",
                "Garibaldi FS",
                ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                false,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(GARIBALDI),
                List.of(),
                false,
                false,
                List.of("stopPointsJourneyDetails[].callStart.stopPoint.id",
                        "stopPointsJourneyDetails[].callStart.stopPoint.nameLong"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                filters,
                List.of(),
                List.of(PERO),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(PERO),
                        false));
    }

    private ScheduledServiceDataLocationContext milanoCentraleContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                "Milano centrale",
                "Milano Centrale",
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(MILANO_CENTRALE),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]", "serviceDataQuery.stopPoints"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                List.of(),
                List.of(),
                List.of(MILANO_CENTRALE),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(MILANO_CENTRALE),
                        false));
    }

    private ScheduledServiceDataLocationContext sanSiroStadioContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                "San Siro Staeio",
                "SAN SIRO STADIO",
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(SAN_SIRO_STADIO),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]", "serviceDataQuery.stopPoints"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                List.of(),
                List.of(),
                List.of(SAN_SIRO_STADIO),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(SAN_SIRO_STADIO),
                        false));
    }

    private ScheduledServiceDataLocationContext cancelledStopContext(
            String monitoredRawText,
            String cancelledStopRawText,
            String cancelledStopId) {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                monitoredRawText,
                monitoredRawText,
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(GORLA),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                ""));
        List<ScheduledServiceDataResolvedLocation> filters = List.of(new ScheduledServiceDataResolvedLocation(
                cancelledStopRawText,
                cancelledStopRawText,
                ScheduledAlertLocationRole.FILTER_CANCELLED_CALL_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                false,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(cancelledStopId),
                List.of(),
                false,
                false,
                List.of("stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id",
                        "stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.nameLong"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                filters,
                List.of(),
                List.of(GORLA),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(GORLA),
                        false));
    }

    private ScheduledServiceDataLocationContext replacementStopContext(
            String monitoredRawText,
            String replacementStopRawText,
            String replacementStopId) {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                monitoredRawText,
                monitoredRawText,
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(GORLA),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                ""));
        List<ScheduledServiceDataResolvedLocation> filters = List.of(new ScheduledServiceDataResolvedLocation(
                replacementStopRawText,
                replacementStopRawText,
                ScheduledAlertLocationRole.FILTER_REPLACEMENT_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                false,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(replacementStopId),
                List.of(),
                false,
                false,
                List.of("stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id",
                        "stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].stopPointId.id"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                filters,
                List.of(),
                List.of(GORLA),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(GORLA),
                        false));
    }

    private ScheduledServiceDataLocationContext excludedDestinationContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(new ScheduledServiceDataResolvedLocation(
                "Buonarroti",
                "Buonarroti",
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(PERO),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                ""));
        List<ScheduledServiceDataResolvedLocation> excluded = List.of(new ScheduledServiceDataResolvedLocation(
                "Tre Torri",
                "Tre Torri",
                ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                ScheduledAlertLocationPolarity.EXCLUDE,
                false,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(GARIBALDI),
                List.of(),
                false,
                false,
                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id"),
                ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                List.of(),
                excluded,
                List.of(PERO),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(PERO),
                        false));
    }

    private ScheduledServiceDataLocationContext multiMonitoredContext() {
        List<ScheduledServiceDataResolvedLocation> monitored = List.of(
                new ScheduledServiceDataResolvedLocation(
                        "Varedo",
                        "Varedo",
                        ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                        ScheduledAlertLocationPolarity.INCLUDE,
                        true,
                        true,
                        ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                        List.of(VAREDO),
                        List.of(),
                        false,
                        false,
                        List.of("body.stopPoints[]"),
                        ""),
                new ScheduledServiceDataResolvedLocation(
                        "Palazzolo Milanese",
                        "Palazzolo Milanese",
                        ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                        ScheduledAlertLocationPolarity.INCLUDE,
                        true,
                        true,
                        ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                        List.of(PALAZZOLO),
                        List.of(),
                        false,
                        false,
                        List.of("body.stopPoints[]"),
                        ""));
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                List.of(),
                List.of(),
                List.of(VAREDO, PALAZZOLO),
                false,
                false,
                List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(VAREDO, PALAZZOLO),
                        false));
    }

    private Map<String, Object> coverage() {
        return Map.of(
                "requirements", List.of(Map.of(
                        "text", "scheduled condition",
                        "required", true,
                        "mappable", true,
                        "mappedBy", List.of("stopPointsJourneyDetails[].arrivalStatuses[].status"))),
                "allRequiredRequirementsMapped", true);
    }

    private String json(Map<String, Object> value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    private record TestFixture(
            ScheduledAlertVerificationService service,
            LlmGateway gateway) {
    }
}
