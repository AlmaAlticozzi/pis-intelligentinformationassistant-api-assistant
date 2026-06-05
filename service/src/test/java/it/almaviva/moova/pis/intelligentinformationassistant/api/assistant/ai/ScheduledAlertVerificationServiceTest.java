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
    private static final String VAREDO = "TNPNTS00000000000101";
    private static final String PALAZZOLO = "TNPNTS00000000000102";
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
        assertThat(outcome.rejectedReason()).contains("could not be parsed or accepted");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
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
        TestFixture fixture = fixture(json(validReportResponse(context, delayCondition())));

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
        TestFixture fixture = fixture(json(validReportResponse(explicitContext(), delayCondition())));

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
        assertThat(outcome.rejectedReason()).contains("COUNT_MATCHING_JOURNEYS requires threshold");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
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

    private TestFixture fixture(String rawResponse) {
        ScheduledAlertVerificationService service = new ScheduledAlertVerificationService();
        service.promptBuilder = promptBuilder();
        service.responseParser = new ScheduledAlertVerificationResponseParser();
        service.outcomeValidator = new ScheduledAlertVerificationOutcomeValidator();
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

    private Map<String, Object> validCondition() {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING"));
    }

    private Map<String, Object> delayCondition() {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureDelay.delay", "GREATER_THAN", 0));
    }

    private Map<String, Object> conditionAnyElement(String path, Map<String, Object> conditions) {
        return condition(Map.of("anyElement", Map.of(
                "path", path,
                "conditions", conditions)));
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
