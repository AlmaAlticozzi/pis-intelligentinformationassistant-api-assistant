package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AgentDslArtifactBuilder {

    static final String SCHEMA_VERSION = "iia.agent.dsl/v1";
    static final String ARTIFACT_TYPE = "DSL";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    public AgentDslArtifactBuildResult buildEventArtifact(
            AgentDefinition definition,
            AgentCompilationPreconditionValidationResult validation,
            OffsetDateTime createdAt) {
        String agentDefinitionId = definition == null ? null : definition.getCodAgentdefinition();
        String interpreterType = validation == null ? null : validation.interpreterType();
        System.out.println("[IIA][AGENT_DSL][BUILDER] start agentDefinitionId=" + agentDefinitionId
                + " interpreterType=" + interpreterType);

        Map<String, Object> blueprint = mapValue(definition == null ? null : definition.getJsnBlueprint());
        Map<String, Object> condition = extractEventCondition(blueprint);
        if (condition == null || condition.isEmpty()) {
            return AgentDslArtifactBuildResult.failure(
                    "Event DSL artifact generation failed because no condition tree could be extracted from the validated blueprint.");
        }
        System.out.println("[IIA][AGENT_DSL][BUILDER] event condition extracted agentDefinitionId=" + agentDefinitionId);

        String inputModel = firstNonBlank(validation == null ? null : validation.inputModel(), definition.getDscInputmodel(), "ServiceDataV2");
        String outputModel = firstNonBlank(validation == null ? null : validation.outputModel(), definition.getDscOutputmodel(), "AgentOutput.CANDIDATE_SUGGESTION");
        String evaluationMode = firstNonBlank(validation == null ? null : validation.evaluationMode(), "STATELESS_EVENT_MATCH");
        String triggerType = firstNonBlank(validation == null ? null : validation.triggerType(), "EVENT");
        String executionModel = firstNonBlank(validation == null ? null : validation.executionModel(), runtimeValue(definition, "executionModel"), "KAFKA_EVENT");
        String source = firstNonBlank(runtimeValue(definition, "source"), "SERVICE_DATA");
        boolean requiresState = Boolean.TRUE.equals(booleanValue(runtimeValue(definition, "requiresState")));

        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("schemaVersion", SCHEMA_VERSION);
        artifact.put("artifactType", ARTIFACT_TYPE);
        artifact.put("agentDefinitionId", agentDefinitionId);
        artifact.put("agentDefinitionVersion", 1);
        artifact.put("source", Map.of(
                "type", "AGENT_DEFINITION",
                "alertId", definition == null || definition.getCodAlert() == null ? null : definition.getCodAlert().getCodAlert(),
                "alertVersion", definition == null ? null : definition.getNumAlertversion()));
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("engine", "STANDARD_AGENT_DSL_EVALUATOR");
        runtime.put("executionModel", executionModel);
        runtime.put("source", source);
        runtime.put("interpreterType", "EVENT_INTERPRETER");
        runtime.put("triggerType", triggerType);
        runtime.put("inputModel", inputModel);
        runtime.put("outputModel", outputModel);
        runtime.put("evaluationMode", evaluationMode);
        runtime.put("requiresScheduler", false);
        runtime.put("requiresState", requiresState);
        runtime.put("requiresExternalTools", false);
        runtime.put("allowedTools", List.of());
        artifact.put("runtime", runtime);
        artifact.put("trigger", Map.of(
                "type", triggerType,
                "source", source,
                "inputModel", inputModel));
        artifact.put("evaluation", Map.of(
                "mode", evaluationMode,
                "condition", condition));
        artifact.put("output", Map.of(
                "type", "CANDIDATE_SUGGESTION",
                "outputModel", outputModel,
                "deduplicationKeyTemplate", "SERVICE_DATA_EVENT:${agentDefinitionId}:${eventId}:${conditionHash}"));
        artifact.put("governance", Map.of(
                "generationMode", "DSL",
                "compiledBy", "api-assistant",
                "llmRuntimeExecutionAllowed", false,
                "externalCodeExecutionAllowed", false,
                "createdAt", (createdAt == null ? OffsetDateTime.now() : createdAt).toString()));

        AgentDslArtifact dslArtifact = new AgentDslArtifact(
                SCHEMA_VERSION,
                ARTIFACT_TYPE,
                agentDefinitionId,
                "EVENT_INTERPRETER",
                triggerType,
                inputModel,
                outputModel,
                evaluationMode,
                artifact,
                Map.of(
                        "conditionSource", "agentDefinition.jsnBlueprint",
                        "runtimeSource", "agentDefinition.jsnRuntimecontract"));
        System.out.println("[IIA][AGENT_DSL][BUILDER] event artifact generated agentDefinitionId="
                + agentDefinitionId + " schemaVersion=iia.agent.dsl/v1");
        return AgentDslArtifactBuildResult.success(dslArtifact);
    }

    public AgentDslArtifactBuildResult buildScheduledArtifact(
            AgentDefinition definition,
            AgentCompilationPreconditionValidationResult validation,
            OffsetDateTime createdAt) {
        String agentDefinitionId = definition == null ? null : definition.getCodAgentdefinition();
        String interpreterType = validation == null ? null : validation.interpreterType();
        System.out.println("[IIA][AGENT_DSL][BUILDER] start agentDefinitionId=" + agentDefinitionId
                + " interpreterType=" + interpreterType);

        Map<String, Object> blueprint = mapValue(definition == null ? null : definition.getJsnBlueprint());
        Map<String, Object> schedule = extractScheduledSchedule(blueprint);
        if (schedule == null || schedule.isEmpty()) {
            return AgentDslArtifactBuildResult.failure("Scheduled DSL artifact generation failed because schedule is missing.");
        }
        System.out.println("[IIA][AGENT_DSL][BUILDER] scheduled schedule extracted agentDefinitionId=" + agentDefinitionId);

        Map<String, Object> serviceDataQuery = extractScheduledServiceDataQuery(blueprint);
        if (serviceDataQuery == null || serviceDataQuery.isEmpty()) {
            return AgentDslArtifactBuildResult.failure("Scheduled DSL artifact generation failed because serviceDataQuery is missing.");
        }
        System.out.println("[IIA][AGENT_DSL][BUILDER] scheduled serviceDataQuery extracted agentDefinitionId=" + agentDefinitionId);

        Map<String, Object> snapshotEvaluation = extractScheduledSnapshotEvaluation(blueprint);
        if (snapshotEvaluation == null || snapshotEvaluation.isEmpty()) {
            return AgentDslArtifactBuildResult.failure("Scheduled DSL artifact generation failed because snapshotEvaluation is missing.");
        }
        System.out.println("[IIA][AGENT_DSL][BUILDER] scheduled snapshotEvaluation extracted agentDefinitionId=" + agentDefinitionId);

        Map<String, Object> outputPolicy = extractScheduledOutputPolicy(blueprint);
        if (outputPolicy == null || outputPolicy.isEmpty()) {
            return AgentDslArtifactBuildResult.failure("Scheduled DSL artifact generation failed because outputPolicy is missing.");
        }
        System.out.println("[IIA][AGENT_DSL][BUILDER] scheduled outputPolicy extracted agentDefinitionId=" + agentDefinitionId);

        String inputModel = firstNonBlank(validation == null ? null : validation.inputModel(), definition.getDscInputmodel(), "ServiceDataStopPointJourneysV2");
        String outputModel = firstNonBlank(validation == null ? null : validation.outputModel(), definition.getDscOutputmodel(), "AgentOutput.CANDIDATE_SUGGESTION");
        String evaluationMode = firstNonBlank(validation == null ? null : validation.evaluationMode(), "SCHEDULED_SNAPSHOT_MATCH");
        String triggerType = firstNonBlank(validation == null ? null : validation.triggerType(), "SCHEDULE");
        String executionModel = firstNonBlank(validation == null ? null : validation.executionModel(), runtimeValue(definition, "executionModel"), "SCHEDULED_POLLING");
        String source = firstNonBlank(runtimeValue(definition, "source"), "SERVICE_DATA");
        String accessMode = firstNonBlank(runtimeValue(definition, "accessMode"), diagnosticValue(validation, "accessMode"), "SERVICE_DATA_API_SNAPSHOT");
        boolean requiresState = Boolean.TRUE.equals(booleanValue(runtimeValue(definition, "requiresState")));
        List<Object> allowedTools = scheduledAllowedTools(definition);

        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("schemaVersion", SCHEMA_VERSION);
        artifact.put("artifactType", ARTIFACT_TYPE);
        artifact.put("agentDefinitionId", agentDefinitionId);
        artifact.put("agentDefinitionVersion", 1);
        artifact.put("source", Map.of(
                "type", "AGENT_DEFINITION",
                "alertId", definition == null || definition.getCodAlert() == null ? null : definition.getCodAlert().getCodAlert(),
                "alertVersion", definition == null ? null : definition.getNumAlertversion()));
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("engine", "STANDARD_AGENT_DSL_EVALUATOR");
        runtime.put("executionModel", executionModel);
        runtime.put("source", source);
        runtime.put("accessMode", accessMode);
        runtime.put("interpreterType", "SCHEDULED_INTERPRETER");
        runtime.put("triggerType", triggerType);
        runtime.put("inputModel", inputModel);
        runtime.put("outputModel", outputModel);
        runtime.put("evaluationMode", evaluationMode);
        runtime.put("requiresScheduler", true);
        runtime.put("requiresState", requiresState);
        runtime.put("requiresExternalTools", false);
        runtime.put("allowedTools", allowedTools);
        artifact.put("runtime", runtime);
        artifact.put("trigger", Map.of(
                "type", triggerType,
                "schedule", schedule));
        artifact.put("toolAccess", Map.of("allowedTools", allowedTools));
        artifact.put("query", Map.of(
                "operation", firstNonBlank(stringValue(serviceDataQuery.get("operation")), "POST /v2/stoppointjourneys"),
                "serviceDataQuery", serviceDataQuery));
        artifact.put("evaluation", Map.of(
                "mode", evaluationMode,
                "snapshotEvaluation", snapshotEvaluation));
        artifact.put("output", Map.of(
                "type", "CANDIDATE_SUGGESTION",
                "outputModel", outputModel,
                "policy", outputPolicy,
                "deduplicationKeyTemplate", "SERVICE_DATA_SCHEDULED:${agentDefinitionId}:${queryWindowStart}:${conditionHash}"));
        artifact.put("governance", Map.of(
                "generationMode", "DSL",
                "compiledBy", "api-assistant",
                "llmRuntimeExecutionAllowed", false,
                "externalCodeExecutionAllowed", false,
                "createdAt", (createdAt == null ? OffsetDateTime.now() : createdAt).toString()));

        AgentDslArtifact dslArtifact = new AgentDslArtifact(
                SCHEMA_VERSION,
                ARTIFACT_TYPE,
                agentDefinitionId,
                "SCHEDULED_INTERPRETER",
                triggerType,
                inputModel,
                outputModel,
                evaluationMode,
                artifact,
                Map.of(
                        "scheduleSource", "agentDefinition.jsnBlueprint",
                        "serviceDataQuerySource", "agentDefinition.jsnBlueprint",
                        "snapshotEvaluationSource", "agentDefinition.jsnBlueprint",
                        "outputPolicySource", "agentDefinition.jsnBlueprint",
                        "runtimeSource", "agentDefinition.jsnRuntimecontract"));
        System.out.println("[IIA][AGENT_DSL][BUILDER] scheduled artifact generated agentDefinitionId="
                + agentDefinitionId + " schemaVersion=iia.agent.dsl/v1");
        return AgentDslArtifactBuildResult.success(dslArtifact);
    }

    Map<String, Object> extractEventCondition(Map<String, Object> blueprint) {
        return firstNonEmptyMap(
                nestedMap(blueprint, "parameters", "condition"),
                nestedMap(blueprint, "condition"),
                nestedMap(blueprint, "parameters", "technicalSpecification", "condition"),
                nestedMap(blueprint, "evaluation", "condition"),
                nestedMap(blueprint, "parameters", "evaluation", "condition"));
    }

    Map<String, Object> extractScheduledSchedule(Map<String, Object> blueprint) {
        return firstNonEmptyMap(
                nestedMap(blueprint, "parameters", "schedule"),
                nestedMap(blueprint, "schedule"),
                nestedMap(blueprint, "parameters", "technicalSpecification", "schedule"),
                nestedMap(blueprint, "parameters", "runtimeContract", "schedule"));
    }

    Map<String, Object> extractScheduledServiceDataQuery(Map<String, Object> blueprint) {
        return firstNonEmptyMap(
                nestedMap(blueprint, "parameters", "serviceDataQuery"),
                nestedMap(blueprint, "serviceDataQuery"),
                nestedMap(blueprint, "parameters", "technicalSpecification", "serviceDataQuery"));
    }

    Map<String, Object> extractScheduledSnapshotEvaluation(Map<String, Object> blueprint) {
        return firstNonEmptyMap(
                nestedMap(blueprint, "parameters", "snapshotEvaluation"),
                nestedMap(blueprint, "snapshotEvaluation"),
                nestedMap(blueprint, "parameters", "technicalSpecification", "snapshotEvaluation"));
    }

    Map<String, Object> extractScheduledOutputPolicy(Map<String, Object> blueprint) {
        return firstNonEmptyMap(
                nestedMap(blueprint, "parameters", "outputPolicy"),
                nestedMap(blueprint, "outputPolicy"),
                nestedMap(blueprint, "parameters", "technicalSpecification", "outputPolicy"));
    }

    private String runtimeValue(AgentDefinition definition, String key) {
        Map<String, Object> runtimeContract = mapValue(definition == null ? null : definition.getJsnRuntimecontract());
        return stringValue(runtimeContract == null ? null : runtimeContract.get(key));
    }

    private String diagnosticValue(AgentCompilationPreconditionValidationResult validation, String key) {
        return stringValue(validation == null || validation.diagnosticDetails() == null
                ? null
                : validation.diagnosticDetails().get(key));
    }

    private List<Object> scheduledAllowedTools(AgentDefinition definition) {
        List<Object> allowedTools = definition == null ? null : definition.getJsnAllowedtools();
        if (allowedTools != null && !allowedTools.isEmpty()) {
            return allowedTools;
        }
        return List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys");
    }

    @SafeVarargs
    private Map<String, Object> firstNonEmptyMap(Map<String, Object>... values) {
        for (Map<String, Object> value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private Map<String, Object> nestedMap(Map<String, Object> source, String... path) {
        Object value = source;
        for (String segment : path) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(segment);
        }
        return mapValue(value);
    }

    private Map<String, Object> mapValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return OBJECT_MAPPER.convertValue(map, MAP_TYPE);
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.valueOf(text);
        }
        return null;
    }
}
