package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentDslArtifactBuilderTest {

    private final AgentDslArtifactBuilder builder = new AgentDslArtifactBuilder();

    @Test
    void buildsEventDslArtifact() {
        AgentDefinition definition = AgentCompilationTestFixtures.eventDefinition();
        Map<String, Object> condition = condition(definition);
        AgentCompilationPreconditionValidationResult validation = validation();

        AgentDslArtifactBuildResult result = builder.buildEventArtifact(
                definition,
                validation,
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isTrue();
        Map<String, Object> artifact = result.artifact().artifact();
        assertThat(artifact)
                .containsEntry("schemaVersion", "iia.agent.dsl/v1")
                .containsEntry("artifactType", "DSL")
                .containsEntry("agentDefinitionId", "AGDF1");
        assertThat(map(artifact.get("runtime")))
                .containsEntry("engine", "STANDARD_AGENT_DSL_EVALUATOR")
                .containsEntry("executionModel", "KAFKA_EVENT")
                .containsEntry("source", "SERVICE_DATA")
                .containsEntry("requiresScheduler", false)
                .containsEntry("requiresExternalTools", false);
        assertThat(list(map(artifact.get("runtime")).get("allowedTools"))).isEmpty();
        assertThat(map(artifact.get("trigger"))).containsEntry("type", "EVENT");
        assertThat(map(artifact.get("evaluation")))
                .containsEntry("mode", "STATELESS_EVENT_MATCH")
                .containsEntry("condition", condition);
        assertThat(map(artifact.get("output")))
                .containsEntry("deduplicationKeyTemplate", "SERVICE_DATA_EVENT:${agentDefinitionId}:${eventId}:${conditionHash}");
        assertThat(map(artifact.get("governance")))
                .containsEntry("llmRuntimeExecutionAllowed", false)
                .containsEntry("externalCodeExecutionAllowed", false);
    }

    @Test
    void failsWhenEventConditionIsMissing() {
        AgentDefinition definition = AgentCompilationTestFixtures.eventDefinition();
        definition.setJsnBlueprint(Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "triggerType", "EVENT",
                "parameters", Map.of()));

        AgentDslArtifactBuildResult result = builder.buildEventArtifact(
                definition,
                validation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo(
                "Event DSL artifact generation failed because no condition tree could be extracted from the validated blueprint.");
    }

    @Test
    void buildsScheduledDslArtifact() {
        AgentDefinition definition = AgentCompilationTestFixtures.scheduledDefinition();
        Map<String, Object> parameters = map(definition.getJsnBlueprint().get("parameters"));
        AgentCompilationPreconditionValidationResult validation = scheduledValidation();

        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                validation,
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isTrue();
        Map<String, Object> artifact = result.artifact().artifact();
        assertThat(artifact)
                .containsEntry("schemaVersion", "iia.agent.dsl/v1")
                .containsEntry("artifactType", "DSL")
                .containsEntry("agentDefinitionId", "AGDF1");
        assertThat(map(artifact.get("runtime")))
                .containsEntry("executionModel", "SCHEDULED_POLLING")
                .containsEntry("accessMode", "SERVICE_DATA_API_SNAPSHOT")
                .containsEntry("requiresScheduler", true)
                .containsEntry("requiresExternalTools", false);
        assertThat(list(map(artifact.get("runtime")).get("allowedTools")))
                .contains("SERVICE_DATA_API.POST_/v2/stoppointjourneys");
        assertThat(map(artifact.get("trigger")))
                .containsEntry("type", "SCHEDULE")
                .containsEntry("schedule", parameters.get("schedule"));
        assertThat(map(artifact.get("query")))
                .containsEntry("serviceDataQuery", parameters.get("serviceDataQuery"));
        assertThat(map(artifact.get("evaluation")))
                .containsEntry("mode", "SCHEDULED_SNAPSHOT_MATCH")
                .containsEntry("snapshotEvaluation", parameters.get("snapshotEvaluation"));
        assertThat(map(artifact.get("output")))
                .containsEntry("policy", parameters.get("outputPolicy"))
                .containsEntry("deduplicationKeyTemplate", "SERVICE_DATA_SCHEDULED:${agentDefinitionId}:${queryWindowStart}:${conditionHash}");
        assertThat(map(artifact.get("governance")))
                .containsEntry("llmRuntimeExecutionAllowed", false)
                .containsEntry("externalCodeExecutionAllowed", false);
    }

    @Test
    void buildsScheduledDslKeepingMonitoringStopPointInQueryAndDestinationInCondition() {
        AgentDefinition definition = AgentCompilationTestFixtures.scheduledDefinition();
        Map<String, Object> destinationAndCancellationCondition = Map.of(
                "type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "all", List.of(
                                        Map.of(
                                                "field", "callEnd.stopPoint.id",
                                                "operator", "EQUALS",
                                                "value", "TNPNTS_BIGNAMI"),
                                        Map.of(
                                                "any", List.of(
                                                        Map.of(
                                                                "field", "arrivalStatuses",
                                                                "operator", "CONTAINS_ANY",
                                                                "values", List.of("ARRIVAL_CANCELLATION")),
                                                        Map.of(
                                                                "field", "departureStatuses",
                                                                "operator", "CONTAINS_ANY",
                                                                "values", List.of("DEPARTURE_CANCELLATION")),
                                                        Map.of(
                                                                "field", "changes",
                                                                "operator", "CONTAINS_ANY",
                                                                "values", List.of("CANCELLATION"))))))));
        Map<String, Object> blueprint = new java.util.LinkedHashMap<>(definition.getJsnBlueprint());
        blueprint.put("parameters", Map.of(
                "schedule", Map.of("frequencySeconds", 600),
                "serviceDataQuery", Map.of(
                        "operation", "POST /v2/stoppointjourneys",
                        "stopPoints", List.of("TNPNTS_GERUSALEMME")),
                "snapshotEvaluation", Map.of(
                        "mode", "REPORT_COUNT",
                        "condition", destinationAndCancellationCondition),
                "outputPolicy", Map.of("emit", "EVERY_RUN_REPORT")));
        definition.setJsnBlueprint(blueprint);

        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isTrue();
        Map<String, Object> artifact = result.artifact().artifact();
        assertThat(map(map(artifact.get("trigger")).get("schedule"))).containsEntry("frequencySeconds", 600);
        assertThat(map(map(artifact.get("query")).get("serviceDataQuery")).get("stopPoints"))
                .isEqualTo(List.of("TNPNTS_GERUSALEMME"));
        assertThat(map(map(artifact.get("evaluation")).get("snapshotEvaluation")).get("condition"))
                .isEqualTo(destinationAndCancellationCondition);
        assertThat(map(map(artifact.get("output")).get("policy"))).containsEntry("emit", "EVERY_RUN_REPORT");
        assertThat(list(map(artifact.get("runtime")).get("allowedTools")))
                .contains("SERVICE_DATA_API.POST_/v2/stoppointjourneys");
    }

    @Test
    void failsWhenScheduledScheduleIsMissing() {
        AgentDefinition definition = scheduledWithout("schedule");

        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Scheduled DSL artifact generation failed because schedule is missing.");
    }

    @Test
    void failsWhenScheduledServiceDataQueryIsMissing() {
        AgentDefinition definition = scheduledWithout("serviceDataQuery");

        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Scheduled DSL artifact generation failed because serviceDataQuery is missing.");
    }

    @Test
    void failsWhenScheduledSnapshotEvaluationIsMissing() {
        AgentDefinition definition = scheduledWithout("snapshotEvaluation");

        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Scheduled DSL artifact generation failed because snapshotEvaluation is missing.");
    }

    @Test
    void failsWhenScheduledOutputPolicyIsMissing() {
        AgentDefinition definition = scheduledWithout("outputPolicy");

        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Scheduled DSL artifact generation failed because outputPolicy is missing.");
    }

    private AgentCompilationPreconditionValidationResult validation() {
        return new AgentCompilationPreconditionValidationResult(
                true,
                List.of(),
                List.of(),
                "EVENT_INTERPRETER",
                "EVENT",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "STATELESS_EVENT_MATCH",
                "DSL",
                "KAFKA_EVENT",
                Map.of("source", "SERVICE_DATA"));
    }

    private AgentCompilationPreconditionValidationResult scheduledValidation() {
        return new AgentCompilationPreconditionValidationResult(
                true,
                List.of(),
                List.of(),
                "SCHEDULED_INTERPRETER",
                "SCHEDULE",
                "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "SCHEDULED_SNAPSHOT_MATCH",
                "DSL",
                "SCHEDULED_POLLING",
                Map.of("source", "SERVICE_DATA", "accessMode", "SERVICE_DATA_API_SNAPSHOT"));
    }

    private AgentDefinition scheduledWithout(String key) {
        AgentDefinition definition = AgentCompilationTestFixtures.scheduledDefinition();
        Map<String, Object> blueprint = map(definition.getJsnBlueprint());
        Map<String, Object> parameters = new java.util.LinkedHashMap<>(map(blueprint.get("parameters")));
        parameters.remove(key);
        blueprint = new java.util.LinkedHashMap<>(blueprint);
        blueprint.put("parameters", parameters);
        definition.setJsnBlueprint(blueprint);
        return definition;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Object> list(Object value) {
        return (List<Object>) value;
    }

    private Map<String, Object> condition(AgentDefinition definition) {
        return map(map(definition.getJsnBlueprint().get("parameters")).get("condition"));
    }
}
