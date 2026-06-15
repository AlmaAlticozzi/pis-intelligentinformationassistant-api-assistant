package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class AgentDslRuntimeCompatibilityValidator {

    private static final String REQUIRED_TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";

    @Inject
    AgentGenerationCapabilityCatalog capabilityCatalog;

    public AgentDslRuntimeCompatibilityValidationResult validate(Map<String, Object> artifact) {
        String agentDefinitionId = stringValue(artifact == null ? null : artifact.get("agentDefinitionId"));
        System.out.println("[IIA][AGENT_DSL][RUNTIME_VALIDATOR] start agentDefinitionId=" + agentDefinitionId);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> runtime = mapValue(artifact == null ? null : artifact.get("runtime"));
        String schemaVersion = stringValue(artifact == null ? null : artifact.get("schemaVersion"));
        String artifactType = stringValue(artifact == null ? null : artifact.get("artifactType"));
        String interpreterType = stringValue(runtime == null ? null : runtime.get("interpreterType"));
        String triggerType = stringValue(runtime == null ? null : runtime.get("triggerType"));
        String executionModel = stringValue(runtime == null ? null : runtime.get("executionModel"));
        String inputModel = stringValue(runtime == null ? null : runtime.get("inputModel"));
        String outputModel = stringValue(runtime == null ? null : runtime.get("outputModel"));
        String evaluationMode = stringValue(runtime == null ? null : runtime.get("evaluationMode"));
        System.out.println("[IIA][AGENT_DSL][RUNTIME_VALIDATOR] resolved runtime agentDefinitionId="
                + agentDefinitionId
                + " interpreterType=" + interpreterType
                + " executionModel=" + executionModel
                + " inputModel=" + inputModel
                + " evaluationMode=" + evaluationMode);

        validateCommonRoot(artifact, errors);
        validateCommonRuntime(runtime, errors);
        validateGovernance(mapValue(artifact == null ? null : artifact.get("governance")), errors);
        validateUnsafeContent(artifact, "$", errors);
        validateConditionOperators(artifact, errors);
        validateFieldPaths(artifact, errors);

        if ("EVENT_INTERPRETER".equals(interpreterType)) {
            validateEvent(artifact, runtime, errors);
        } else if ("SCHEDULED_INTERPRETER".equals(interpreterType)) {
            validateScheduled(artifact, runtime, errors);
        } else if (interpreterType != null) {
            errors.add("Unsupported runtime.interpreterType " + interpreterType + ".");
        }

        boolean compatible = errors.isEmpty();
        if (compatible) {
            System.out.println("[IIA][AGENT_DSL][RUNTIME_VALIDATOR] compatible agentDefinitionId="
                    + agentDefinitionId + " warnings=" + warnings.size());
        } else {
            System.out.println("[IIA][AGENT_DSL][RUNTIME_VALIDATOR] rejected agentDefinitionId="
                    + agentDefinitionId + " errors=" + errors);
        }
        return new AgentDslRuntimeCompatibilityValidationResult(
                compatible,
                List.copyOf(errors),
                List.copyOf(warnings),
                schemaVersion,
                artifactType,
                interpreterType,
                triggerType,
                executionModel,
                inputModel,
                outputModel,
                evaluationMode,
                Map.of("agentDefinitionId", agentDefinitionId));
    }

    private void validateCommonRoot(Map<String, Object> artifact, List<String> errors) {
        if (artifact == null || artifact.isEmpty()) {
            errors.add("DSL artifact is missing.");
            return;
        }
        expect("iia.agent.dsl/v1", stringValue(artifact.get("schemaVersion")), "DSL schemaVersion must be iia.agent.dsl/v1.", errors);
        expect("DSL", stringValue(artifact.get("artifactType")), "DSL artifactType must be DSL.", errors);
        if (isBlank(stringValue(artifact.get("agentDefinitionId")))) {
            errors.add("DSL agentDefinitionId is missing.");
        }
        requireMap(artifact.get("runtime"), "DSL runtime section is missing.", errors);
        requireMap(artifact.get("trigger"), "DSL trigger section is missing.", errors);
        requireMap(artifact.get("evaluation"), "DSL evaluation section is missing.", errors);
        requireMap(artifact.get("output"), "DSL output section is missing.", errors);
        requireMap(artifact.get("governance"), "DSL governance section is missing.", errors);
    }

    private void validateCommonRuntime(Map<String, Object> runtime, List<String> errors) {
        if (runtime == null) {
            return;
        }
        expect("STANDARD_AGENT_DSL_EVALUATOR", stringValue(runtime.get("engine")), "runtime.engine must be STANDARD_AGENT_DSL_EVALUATOR.", errors);
        expect("SERVICE_DATA", stringValue(runtime.get("source")), "runtime.source must be SERVICE_DATA.", errors);
        expect("AgentOutput.CANDIDATE_SUGGESTION", stringValue(runtime.get("outputModel")), "runtime.outputModel must be AgentOutput.CANDIDATE_SUGGESTION.", errors);
        String interpreterType = stringValue(runtime.get("interpreterType"));
        if (!"EVENT_INTERPRETER".equals(interpreterType) && !"SCHEDULED_INTERPRETER".equals(interpreterType)) {
            errors.add("runtime.interpreterType must be EVENT_INTERPRETER or SCHEDULED_INTERPRETER.");
        }
        String triggerType = stringValue(runtime.get("triggerType"));
        if (!"EVENT".equals(triggerType) && !"SCHEDULE".equals(triggerType)) {
            errors.add("runtime.triggerType must be EVENT or SCHEDULE.");
        }
        String evaluationMode = stringValue(runtime.get("evaluationMode"));
        if (!"STATELESS_EVENT_MATCH".equals(evaluationMode) && !"SCHEDULED_SNAPSHOT_MATCH".equals(evaluationMode)) {
            errors.add("runtime.evaluationMode must be STATELESS_EVENT_MATCH or SCHEDULED_SNAPSHOT_MATCH.");
        }
    }

    private void validateGovernance(Map<String, Object> governance, List<String> errors) {
        if (governance == null) {
            return;
        }
        if (!Boolean.FALSE.equals(booleanValue(governance.get("llmRuntimeExecutionAllowed")))) {
            errors.add("governance.llmRuntimeExecutionAllowed must be false.");
        }
        if (!Boolean.FALSE.equals(booleanValue(governance.get("externalCodeExecutionAllowed")))) {
            errors.add("governance.externalCodeExecutionAllowed must be false.");
        }
        String compiledBy = stringValue(governance.get("compiledBy"));
        if (compiledBy != null && !"api-assistant".equals(compiledBy)) {
            errors.add("governance.compiledBy must be api-assistant.");
        }
        String generationMode = stringValue(governance.get("generationMode"));
        if (generationMode != null && !"DSL".equals(generationMode)) {
            errors.add("governance.generationMode must be DSL.");
        }
    }

    private void validateEvent(Map<String, Object> artifact, Map<String, Object> runtime, List<String> errors) {
        expect("KAFKA_EVENT", stringValue(runtime.get("executionModel")), "runtime.executionModel must be KAFKA_EVENT for EVENT_INTERPRETER.", errors);
        expect("EVENT", stringValue(runtime.get("triggerType")), "runtime.triggerType must be EVENT for EVENT_INTERPRETER.", errors);
        expect("ServiceDataV2", stringValue(runtime.get("inputModel")), "runtime.inputModel must be ServiceDataV2 for EVENT_INTERPRETER.", errors);
        expect("STATELESS_EVENT_MATCH", stringValue(runtime.get("evaluationMode")), "runtime.evaluationMode must be STATELESS_EVENT_MATCH for EVENT_INTERPRETER.", errors);
        if (Boolean.TRUE.equals(booleanValue(runtime.get("requiresScheduler")))) {
            errors.add("EVENT_INTERPRETER DSL cannot require scheduler execution.");
        }
        if (Boolean.TRUE.equals(booleanValue(runtime.get("requiresExternalTools"))) || !listValue(runtime.get("allowedTools")).isEmpty()) {
            errors.add("EVENT_INTERPRETER DSL cannot require external tools.");
        }

        Map<String, Object> trigger = mapValue(artifact.get("trigger"));
        if (trigger != null) {
            expect("EVENT", stringValue(trigger.get("type")), "trigger.type must be EVENT for EVENT_INTERPRETER.", errors);
            expect("SERVICE_DATA", stringValue(trigger.get("source")), "trigger.source must be SERVICE_DATA for EVENT_INTERPRETER.", errors);
            expect("ServiceDataV2", stringValue(trigger.get("inputModel")), "trigger.inputModel must be ServiceDataV2 for EVENT_INTERPRETER.", errors);
        }
        Map<String, Object> evaluation = mapValue(artifact.get("evaluation"));
        if (evaluation != null) {
            expect("STATELESS_EVENT_MATCH", stringValue(evaluation.get("mode")), "evaluation.mode must be STATELESS_EVENT_MATCH for EVENT_INTERPRETER.", errors);
            if (!presentMap(evaluation.get("condition"))) {
                errors.add("EVENT_INTERPRETER DSL is missing evaluation.condition.");
            }
        }
        validateOutput(mapValue(artifact.get("output")), errors);
    }

    private void validateScheduled(Map<String, Object> artifact, Map<String, Object> runtime, List<String> errors) {
        expect("SCHEDULED_POLLING", stringValue(runtime.get("executionModel")), "runtime.executionModel must be SCHEDULED_POLLING for SCHEDULED_INTERPRETER.", errors);
        expect("SERVICE_DATA_API_SNAPSHOT", stringValue(runtime.get("accessMode")), "runtime.accessMode must be SERVICE_DATA_API_SNAPSHOT for SCHEDULED_INTERPRETER.", errors);
        expect("SCHEDULE", stringValue(runtime.get("triggerType")), "runtime.triggerType must be SCHEDULE for SCHEDULED_INTERPRETER.", errors);
        expect("ServiceDataStopPointJourneysV2", stringValue(runtime.get("inputModel")), "runtime.inputModel must be ServiceDataStopPointJourneysV2 for SCHEDULED_INTERPRETER.", errors);
        expect("SCHEDULED_SNAPSHOT_MATCH", stringValue(runtime.get("evaluationMode")), "runtime.evaluationMode must be SCHEDULED_SNAPSHOT_MATCH for SCHEDULED_INTERPRETER.", errors);
        if (!Boolean.TRUE.equals(booleanValue(runtime.get("requiresScheduler")))) {
            errors.add("SCHEDULED_INTERPRETER DSL must require scheduler execution.");
        }
        if (!stringList(runtime.get("allowedTools")).contains(REQUIRED_TOOL)) {
            errors.add("SCHEDULED_INTERPRETER DSL requires SERVICE_DATA_API.POST_/v2/stoppointjourneys tool access.");
        }

        Map<String, Object> trigger = mapValue(artifact.get("trigger"));
        if (trigger != null) {
            expect("SCHEDULE", stringValue(trigger.get("type")), "trigger.type must be SCHEDULE for SCHEDULED_INTERPRETER.", errors);
            Map<String, Object> schedule = mapValue(trigger.get("schedule"));
            if (schedule == null || schedule.isEmpty()) {
                errors.add("SCHEDULED_INTERPRETER DSL is missing trigger.schedule.");
            } else {
                Integer frequencySeconds = integerValue(schedule.get("frequencySeconds"));
                if (frequencySeconds != null && frequencySeconds <= 0) {
                    errors.add("trigger.schedule.frequencySeconds must be greater than zero.");
                }
            }
        }

        Map<String, Object> toolAccess = mapValue(artifact.get("toolAccess"));
        if (toolAccess == null || !stringList(toolAccess.get("allowedTools")).contains(REQUIRED_TOOL)) {
            errors.add("SCHEDULED_INTERPRETER DSL requires SERVICE_DATA_API.POST_/v2/stoppointjourneys tool access.");
        }

        Map<String, Object> query = mapValue(artifact.get("query"));
        if (query == null || query.isEmpty()) {
            errors.add("SCHEDULED_INTERPRETER DSL is missing query.serviceDataQuery.");
        } else {
            expect("POST /v2/stoppointjourneys", stringValue(query.get("operation")), "query.operation must be POST /v2/stoppointjourneys.", errors);
            Map<String, Object> serviceDataQuery = mapValue(query.get("serviceDataQuery"));
            if (serviceDataQuery == null || serviceDataQuery.isEmpty()) {
                errors.add("SCHEDULED_INTERPRETER DSL is missing query.serviceDataQuery.");
            } else if ("EXPLICIT_STOP_POINTS".equals(stringValue(serviceDataQuery.get("monitoringScope")))
                    && stringList(serviceDataQuery.get("stopPoints")).isEmpty()) {
                errors.add("query.serviceDataQuery.stopPoints is required for EXPLICIT_STOP_POINTS.");
            }
        }

        Map<String, Object> evaluation = mapValue(artifact.get("evaluation"));
        if (evaluation != null) {
            expect("SCHEDULED_SNAPSHOT_MATCH", stringValue(evaluation.get("mode")), "evaluation.mode must be SCHEDULED_SNAPSHOT_MATCH for SCHEDULED_INTERPRETER.", errors);
            Map<String, Object> snapshotEvaluation = mapValue(evaluation.get("snapshotEvaluation"));
            if (snapshotEvaluation == null || snapshotEvaluation.isEmpty()) {
                errors.add("SCHEDULED_INTERPRETER DSL is missing evaluation.snapshotEvaluation.");
            } else if (!presentMap(snapshotEvaluation.get("condition"))) {
                errors.add("SCHEDULED_INTERPRETER DSL is missing snapshotEvaluation.condition.");
            }
            if (!snapshotEvaluationPresent(evaluation) && presentMap(evaluation.get("condition"))) {
                errors.add("SCHEDULED_INTERPRETER DSL must use evaluation.snapshotEvaluation, not only evaluation.condition.");
            }
        }
        Map<String, Object> output = mapValue(artifact.get("output"));
        validateOutput(output, errors);
        if (output != null && !presentMap(output.get("policy"))) {
            errors.add("SCHEDULED_INTERPRETER DSL output.policy is missing.");
        }
    }

    private boolean snapshotEvaluationPresent(Map<String, Object> evaluation) {
        return presentMap(evaluation == null ? null : evaluation.get("snapshotEvaluation"));
    }

    private void validateOutput(Map<String, Object> output, List<String> errors) {
        if (output == null) {
            return;
        }
        expect("CANDIDATE_SUGGESTION", stringValue(output.get("type")), "output.type must be CANDIDATE_SUGGESTION.", errors);
        expect("AgentOutput.CANDIDATE_SUGGESTION", stringValue(output.get("outputModel")), "output.outputModel must be AgentOutput.CANDIDATE_SUGGESTION.", errors);
        if (isBlank(stringValue(output.get("deduplicationKeyTemplate")))) {
            errors.add("output.deduplicationKeyTemplate is missing.");
        }
    }

    private void validateConditionOperators(Object node, List<String> errors) {
        if (node instanceof Map<?, ?> map) {
            Object field = map.get("field");
            Object operator = map.get("operator");
            if (field != null || operator != null) {
                String fieldText = stringValue(field);
                String operatorText = stringValue(operator);
                if (isBlank(fieldText)) {
                    errors.add("DSL condition field is empty.");
                }
                if (isBlank(operatorText)) {
                    errors.add("DSL condition field " + fieldText + " is missing operator.");
                } else if (!catalog().isSupportedDslOperator(operatorText)) {
                    errors.add("Unsupported DSL operator " + operatorText + ".");
                }
            }
            map.values().forEach(value -> validateConditionOperators(value, errors));
        } else if (node instanceof Collection<?> collection) {
            collection.forEach(value -> validateConditionOperators(value, errors));
        }
    }

    private void validateFieldPaths(Object node, List<String> errors) {
        if (node instanceof Map<?, ?> map) {
            Object field = map.get("field");
            if (field != null) {
                String fieldText = stringValue(field);
                if (isBlank(fieldText)) {
                    errors.add("DSL condition field path is empty.");
                } else if (isForbiddenFieldPath(fieldText)) {
                    errors.add("Unsupported DSL field path " + fieldText + ".");
                }
            }
            map.values().forEach(value -> validateFieldPaths(value, errors));
        } else if (node instanceof Collection<?> collection) {
            collection.forEach(value -> validateFieldPaths(value, errors));
        }
    }

    private boolean isForbiddenFieldPath(String field) {
        String normalized = field.toLowerCase(Locale.ROOT);
        return normalized.startsWith("user.password")
                || normalized.startsWith("system.env")
                || normalized.startsWith("file.")
                || normalized.startsWith("http.")
                || normalized.startsWith("sql.");
    }

    private void validateUnsafeContent(Object node, String path, List<String> errors) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (isForbiddenKey(key)) {
                    errors.add("DSL contains forbidden dynamic execution key " + key + ".");
                }
                validateUnsafeContent(entry.getValue(), path + "." + key, errors);
            }
        } else if (node instanceof Collection<?> collection) {
            int index = 0;
            for (Object item : collection) {
                validateUnsafeContent(item, path + "[" + index + "]", errors);
                index++;
            }
        } else if (node instanceof String text && containsForbiddenString(text)) {
            errors.add("DSL contains forbidden dynamic execution content at " + path + ".");
        }
    }

    private boolean isForbiddenKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return List.of("javacode", "script", "sql", "python", "javascript", "shell", "command", "url",
                "endpointurl", "httpurl", "prompt", "llmprompt", "model", "openai", "eval", "expressionlanguage")
                .contains(normalized);
    }

    private boolean containsForbiddenString(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.contains("http://")
                || normalized.contains("https://")
                || normalized.contains("select ")
                || normalized.contains("insert ")
                || normalized.contains("delete ")
                || value.contains("Runtime.getRuntime")
                || value.contains("ProcessBuilder")
                || normalized.contains("eval(");
    }

    private void expect(String expected, String actual, String message, List<String> errors) {
        if (!expected.equals(actual)) {
            errors.add(message);
        }
    }

    private void requireMap(Object value, String message, List<String> errors) {
        if (!presentMap(value)) {
            errors.add(message);
        }
    }

    private boolean presentMap(Object value) {
        return value instanceof Map<?, ?> map && !map.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private List<Object> listValue(Object value) {
        if (value instanceof Collection<?> collection) {
            return List.copyOf(collection);
        }
        return List.of();
    }

    private List<String> stringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::stringValue)
                    .filter(text -> text != null && !text.isBlank())
                    .toList();
        }
        return List.of();
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

    private Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private AgentGenerationCapabilityCatalog catalog() {
        return capabilityCatalog == null ? new AgentGenerationCapabilityCatalog() : capabilityCatalog;
    }
}
