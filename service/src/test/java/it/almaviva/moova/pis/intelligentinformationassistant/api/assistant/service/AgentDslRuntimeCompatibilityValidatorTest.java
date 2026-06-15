package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentDslRuntimeCompatibilityValidatorTest {

    private final AgentDslArtifactBuilder builder = new AgentDslArtifactBuilder();
    private final AgentDslRuntimeCompatibilityValidator validator = new AgentDslRuntimeCompatibilityValidator();

    @Test
    void validatesEventDslArtifact() {
        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(eventDsl());

        assertThat(result.compatible()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.schemaVersion()).isEqualTo("iia.agent.dsl/v1");
        assertThat(result.interpreterType()).isEqualTo("EVENT_INTERPRETER");
        assertThat(result.executionModel()).isEqualTo("KAFKA_EVENT");
    }

    @Test
    void validatesScheduledDslArtifact() {
        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(scheduledDsl());

        assertThat(result.compatible()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.schemaVersion()).isEqualTo("iia.agent.dsl/v1");
        assertThat(result.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(result.executionModel()).isEqualTo("SCHEDULED_POLLING");
    }

    @Test
    void rejectsEventDslWithAllowedTools() {
        Map<String, Object> artifact = eventDsl();
        Map<String, Object> runtime = copyMap(artifact.get("runtime"));
        runtime.put("allowedTools", List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));
        artifact.put("runtime", runtime);

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).contains("EVENT_INTERPRETER DSL cannot require external tools.");
    }

    @Test
    void rejectsScheduledDslWithoutRuntimeAllowedTool() {
        Map<String, Object> artifact = scheduledDsl();
        Map<String, Object> runtime = copyMap(artifact.get("runtime"));
        runtime.put("allowedTools", List.of());
        artifact.put("runtime", runtime);

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).contains("SCHEDULED_INTERPRETER DSL requires SERVICE_DATA_API.POST_/v2/stoppointjourneys tool access.");
    }

    @Test
    void rejectsScheduledDslWithoutSchedule() {
        Map<String, Object> artifact = scheduledDsl();
        Map<String, Object> trigger = copyMap(artifact.get("trigger"));
        trigger.remove("schedule");
        artifact.put("trigger", trigger);

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).contains("SCHEDULED_INTERPRETER DSL is missing trigger.schedule.");
    }

    @Test
    void rejectsScheduledDslWithoutServiceDataQuery() {
        Map<String, Object> artifact = scheduledDsl();
        Map<String, Object> query = copyMap(artifact.get("query"));
        query.remove("serviceDataQuery");
        artifact.put("query", query);

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).contains("SCHEDULED_INTERPRETER DSL is missing query.serviceDataQuery.");
    }

    @Test
    void rejectsScheduledDslWithoutSnapshotEvaluationCondition() {
        Map<String, Object> artifact = scheduledDsl();
        Map<String, Object> evaluation = copyMap(artifact.get("evaluation"));
        Map<String, Object> snapshotEvaluation = copyMap(evaluation.get("snapshotEvaluation"));
        snapshotEvaluation.remove("condition");
        evaluation.put("snapshotEvaluation", snapshotEvaluation);
        artifact.put("evaluation", evaluation);

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).contains("SCHEDULED_INTERPRETER DSL is missing snapshotEvaluation.condition.");
    }

    @Test
    void rejectsUnsupportedDslOperator() {
        Map<String, Object> artifact = eventDsl();
        Map<String, Object> evaluation = copyMap(artifact.get("evaluation"));
        evaluation.put("condition", Map.of(
                "field", "payload.status",
                "operator", "MAGIC_OPERATOR",
                "value", "ARRIVING"));
        artifact.put("evaluation", evaluation);

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).contains("Unsupported DSL operator MAGIC_OPERATOR.");
    }

    @Test
    void rejectsForbiddenScriptContent() {
        Map<String, Object> artifact = eventDsl();
        artifact.put("script", "Runtime.getRuntime().exec('whoami')");

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors())
                .contains("DSL contains forbidden dynamic execution key script.")
                .contains("DSL contains forbidden dynamic execution content at $.script.");
    }

    private Map<String, Object> eventDsl() {
        AgentDefinition definition = AgentCompilationTestFixtures.eventDefinition();
        AgentDslArtifactBuildResult result = builder.buildEventArtifact(
                definition,
                eventValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));
        assertThat(result.success()).isTrue();
        return deepCopy(result.artifact().artifact());
    }

    private Map<String, Object> scheduledDsl() {
        AgentDefinition definition = AgentCompilationTestFixtures.scheduledDefinition();
        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));
        assertThat(result.success()).isTrue();
        return deepCopy(result.artifact().artifact());
    }

    private AgentCompilationPreconditionValidationResult eventValidation() {
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> copyMap(Object value) {
        return new LinkedHashMap<>((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> map) {
                copy.put(entry.getKey(), deepCopy((Map<String, Object>) map));
            } else if (value instanceof List<?> list) {
                copy.put(entry.getKey(), List.copyOf(list));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }
}
