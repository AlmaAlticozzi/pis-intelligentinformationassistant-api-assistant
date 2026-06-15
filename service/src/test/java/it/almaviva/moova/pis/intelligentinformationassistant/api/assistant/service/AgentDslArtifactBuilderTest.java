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

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    private Map<String, Object> condition(AgentDefinition definition) {
        return map(map(definition.getJsnBlueprint().get("parameters")).get("condition"));
    }
}
