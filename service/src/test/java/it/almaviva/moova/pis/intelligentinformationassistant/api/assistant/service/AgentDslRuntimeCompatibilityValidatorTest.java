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
    void validatesConditionlessReportCountDslArtifact() {
        Map<String, Object> artifact = scheduledConditionlessReportDsl();

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(snapshot(artifact)).doesNotContainKey("condition");
    }

    @Test
    void validatesAllScheduledRuntimeSemanticContracts() {
        for (String mode : List.of("REPORT_COUNT", "COUNT_MATCHING_JOURNEYS", "BOOLEAN_EXISTS")) {
            AgentDslRuntimeCompatibilityValidationResult result = validator.validate(scheduledDsl(mode));
            assertThat(result.errors()).as(mode).isEmpty();
            assertThat(result.compatible()).as(mode).isTrue();
        }
    }

    @Test
    void thresholdOperatorIsNotAConditionButScheduledConditionOperatorStillIsValidated() {
        Map<String, Object> validThresholdArtifact = scheduledDsl("COUNT_MATCHING_JOURNEYS");
        assertThat(validator.validate(validThresholdArtifact).errors())
                .doesNotContain("DSL condition field is empty.");

        Map<String, Object> invalidConditionArtifact = scheduledDsl("COUNT_MATCHING_JOURNEYS");
        Map<String, Object> snapshot = snapshot(invalidConditionArtifact);
        snapshot.put("condition", Map.of("field", "departureDelay.delay", "operator", "MAGIC_OPERATOR", "value", 0));

        assertThat(validator.validate(invalidConditionArtifact).errors())
                .contains("Unsupported DSL operator MAGIC_OPERATOR.");
    }

    @Test
    void rejectsInvalidScheduledSemanticContracts() {
        assertInvalid(withThreshold(scheduledDsl("REPORT_COUNT"), Map.of("operator", "EQUALS", "value", 1)), "must not contain threshold");
        assertInvalid(withEmit(scheduledDsl("REPORT_COUNT"), "ON_MATCH"), "requires output.policy.emit EVERY_RUN");
        assertInvalid(withFlag(scheduledDsl("REPORT_COUNT"), "includeCount", false), "includeCount=true");
        assertInvalid(withoutThreshold(scheduledDsl("COUNT_MATCHING_JOURNEYS")), "requires threshold");
        assertInvalid(withThreshold(scheduledDsl("COUNT_MATCHING_JOURNEYS"), Map.of()), "non-empty object");
        assertInvalid(withThreshold(scheduledDsl("COUNT_MATCHING_JOURNEYS"), Map.of("operator", "MAGIC", "value", 3)), "Unsupported Scheduled threshold operator");
        assertInvalid(withThreshold(scheduledDsl("COUNT_MATCHING_JOURNEYS"), Map.of("operator", "EQUALS")), "threshold.value must be numeric");
        assertInvalid(withThreshold(scheduledDsl("COUNT_MATCHING_JOURNEYS"), Map.of("operator", "EQUALS", "value", "3")), "threshold.value must be numeric");
        assertInvalid(withEmit(scheduledDsl("COUNT_MATCHING_JOURNEYS"), "EVERY_RUN"), "requires output.policy.emit ON_MATCH");
        assertInvalid(withThreshold(scheduledDsl("BOOLEAN_EXISTS"), Map.of("operator", "EQUALS", "value", 1)), "must not contain threshold");
        assertInvalid(withEmit(scheduledDsl("BOOLEAN_EXISTS"), "EVERY_RUN"), "requires output.policy.emit ON_MATCH");
        assertInvalid(withMode(scheduledDsl("REPORT_COUNT"), "UNKNOWN_MODE"), "Unsupported Scheduled snapshotEvaluation.mode");
        assertInvalid(withEmit(scheduledDsl("REPORT_COUNT"), "UNKNOWN_EMIT"), "Unsupported Scheduled output.policy.emit");
        Map<String, Object> neitherOutput = withFlag(scheduledDsl("BOOLEAN_EXISTS"), "includeCount", false);
        assertInvalid(withFlag(neitherOutput, "includeMatchingJourneys", false), "must include at least count or matching journeys");
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
    void rejectsConditionalScheduledDslWithoutSnapshotEvaluationCondition() {
        Map<String, Object> artifact = scheduledDsl("COUNT_MATCHING_JOURNEYS");
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

    @Test
    void rejectsExternalHttpUrlContent() {
        Map<String, Object> artifact = eventDsl();
        artifact.put("metadata", Map.of("endpointUrl", "https://example.invalid/runtime"));

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("endpointUrl"));
        assertThat(result.errors()).anyMatch(error -> error.contains("$.metadata.endpointUrl"));
    }

    @Test
    void rejectsSqlContent() {
        Map<String, Object> artifact = eventDsl();
        artifact.put("diagnostics", Map.of("note", "SELECT * FROM users"));

        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);

        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).contains("DSL contains forbidden dynamic execution content at $.diagnostics.note.");
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
        return scheduledDsl("REPORT_COUNT");
    }

    private Map<String, Object> scheduledDsl(String mode) {
        AgentDefinition definition = AgentCompilationTestFixtures.scheduledDefinition(mode);
        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));
        assertThat(result.success()).isTrue();
        return deepCopy(result.artifact().artifact());
    }

    private Map<String, Object> scheduledConditionlessReportDsl() {
        AgentDefinition definition = AgentCompilationTestFixtures.conditionlessScheduledReportDefinition();
        AgentDslArtifactBuildResult result = builder.buildScheduledArtifact(
                definition,
                scheduledValidation(),
                OffsetDateTime.parse("2026-06-15T10:00:00Z"));
        assertThat(result.success()).isTrue();
        return deepCopy(result.artifact().artifact());
    }

    private void assertInvalid(Map<String, Object> artifact, String errorFragment) {
        AgentDslRuntimeCompatibilityValidationResult result = validator.validate(artifact);
        assertThat(result.compatible()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains(errorFragment));
    }

    private Map<String, Object> withThreshold(Map<String, Object> artifact, Object threshold) {
        snapshot(artifact).put("threshold", threshold);
        return artifact;
    }

    private Map<String, Object> withoutThreshold(Map<String, Object> artifact) {
        snapshot(artifact).remove("threshold");
        return artifact;
    }

    private Map<String, Object> withMode(Map<String, Object> artifact, String mode) {
        snapshot(artifact).put("mode", mode);
        return artifact;
    }

    private Map<String, Object> withEmit(Map<String, Object> artifact, String emit) {
        policy(artifact).put("emit", emit);
        return artifact;
    }

    private Map<String, Object> withFlag(Map<String, Object> artifact, String name, boolean value) {
        policy(artifact).put(name, value);
        return artifact;
    }

    private Map<String, Object> snapshot(Map<String, Object> artifact) {
        return copyInto(map(map(artifact.get("evaluation")).get("snapshotEvaluation")), map(artifact.get("evaluation")), "snapshotEvaluation");
    }

    private Map<String, Object> policy(Map<String, Object> artifact) {
        return copyInto(map(map(artifact.get("output")).get("policy")), map(artifact.get("output")), "policy");
    }

    private Map<String, Object> copyInto(Map<String, Object> value, Map<String, Object> parent, String key) {
        Map<String, Object> copy = new LinkedHashMap<>(value);
        parent.put(key, copy);
        return copy;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
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
