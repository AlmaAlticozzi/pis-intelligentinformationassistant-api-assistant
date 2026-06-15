package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentProfileRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AgentCompilationPreconditionValidator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String SCHEDULED_TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";

    @Inject
    AgentProfileRepository agentProfileRepository;

    public AgentCompilationPreconditionValidationResult validate(
            AgentDefinition definition,
            String effectiveGenerationMode) {
        String agentDefinitionId = definition == null ? null : definition.getCodAgentdefinition();
        System.out.println("[IIA][AGENT_COMPILATION][VALIDATOR] start agentDefinitionId=" + agentDefinitionId);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> diagnosticDetails = new LinkedHashMap<>();

        if (definition == null) {
            errors.add("Agent Definition is missing.");
            return result(false, errors, warnings, null, null, null, null, null, effectiveGenerationMode, null, diagnosticDetails);
        }

        validateBaseDefinition(definition, errors);
        validateProfile(definition, errors);
        String normalizedGenerationMode = validateGenerationMode(effectiveGenerationMode, errors, warnings);

        Map<String, Object> blueprint = mapValue(definition.getJsnBlueprint());
        Map<String, Object> runtimeContract = mapValue(definition.getJsnRuntimecontract());
        if (blueprint == null || blueprint.isEmpty()) {
            errors.add("Agent Definition blueprint is missing; compilation cannot continue.");
        }
        if (runtimeContract == null || runtimeContract.isEmpty()) {
            errors.add("Agent Definition runtimeContract is missing; compilation cannot continue.");
        }

        ContractShape contract = resolveContract(definition, runtimeContract, blueprint, warnings);
        System.out.println("[IIA][AGENT_COMPILATION][VALIDATOR] resolved contract agentDefinitionId=" + agentDefinitionId
                + " interpreterType=" + contract.interpreterType()
                + " triggerType=" + contract.triggerType()
                + " inputModel=" + contract.inputModel()
                + " evaluationMode=" + contract.evaluationMode());

        validateRequiredContractFields(contract, errors);
        if (!hasRequiredFieldErrors(contract)) {
            validateSupportedContract(contract, errors);
            validateContractSpecificRules(definition, runtimeContract, blueprint, contract, errors);
            validateBlueprintShape(blueprint, runtimeContract, contract, errors);
        }

        diagnosticDetails.put("source", contract.source());
        diagnosticDetails.put("accessMode", contract.accessMode());
        diagnosticDetails.put("requiresScheduler", contract.requiresScheduler());
        diagnosticDetails.put("allowedTools", contract.allowedTools());
        boolean valid = errors.isEmpty();
        AgentCompilationPreconditionValidationResult result = result(
                valid,
                List.copyOf(errors),
                List.copyOf(warnings),
                contract.interpreterType(),
                contract.triggerType(),
                contract.inputModel(),
                contract.outputModel(),
                contract.evaluationMode(),
                normalizedGenerationMode,
                contract.executionModel(),
                diagnosticDetails);
        if (valid) {
            System.out.println("[IIA][AGENT_COMPILATION][VALIDATOR] validation ok agentDefinitionId=" + agentDefinitionId
                    + " warnings=" + warnings.size());
        } else {
            System.out.println("[IIA][AGENT_COMPILATION][VALIDATOR] validation rejected agentDefinitionId=" + agentDefinitionId
                    + " errors=" + errors);
        }
        return result;
    }

    private void validateBaseDefinition(AgentDefinition definition, List<String> errors) {
        if (isBlank(definition.getCodAgentdefinition())) {
            errors.add("Agent Definition id is missing.");
        }
        if (definition.getSglStatus() == null || isBlank(definition.getSglStatus().getSglStatus())) {
            errors.add("Agent Definition status is missing.");
        }
        if (isBlank(definition.getDscName())) {
            errors.add("Agent Definition name is missing.");
        }
        if (definition.getSglGenerationmode() == null || isBlank(definition.getSglGenerationmode().getSglGenerationmode())) {
            errors.add("Agent Definition generationMode is missing.");
        }
        if (definition.getCodAlert() == null) {
            errors.add("Agent Definition alert reference is missing.");
        }
        if (definition.getNumAlertversion() == null || definition.getNumAlertversion() <= 0) {
            errors.add("Agent Definition alertVersion is missing or invalid.");
        }
        if (definition.getCodAgentprofile() == null) {
            errors.add("Agent Profile is missing or cannot be resolved.");
        }
        if (definition.getJsnActivationpolicy() == null || definition.getJsnActivationpolicy().isEmpty()) {
            errors.add("Agent Definition activationPolicy is missing.");
        }
    }

    private void validateProfile(AgentDefinition definition, List<String> errors) {
        AgentProfile profile = definition.getCodAgentprofile();
        if (profile == null && !isBlank(definition.getCodProfile()) && agentProfileRepository != null) {
            profile = agentProfileRepository.findByProfileId(definition.getCodProfile()).orElse(null);
        }
        if (profile == null) {
            return;
        }
        if (!Boolean.TRUE.equals(profile.getFlgEnabled())) {
            errors.add("Agent Profile " + profile.getCodAgentprofile() + " is disabled and cannot be used for compilation.");
        }
    }

    private String validateGenerationMode(String effectiveGenerationMode, List<String> errors, List<String> warnings) {
        if (isBlank(effectiveGenerationMode) || "DSL".equals(effectiveGenerationMode)) {
            return "DSL";
        }
        if ("AUTO".equals(effectiveGenerationMode)) {
            warnings.add("Generation mode AUTO reached precondition validation and was normalized to DSL.");
            return "DSL";
        }
        if ("JAVA_TEMPLATE".equals(effectiveGenerationMode)) {
            errors.add("Generation mode JAVA_TEMPLATE is not supported by the DSL compilation MVP.");
            return effectiveGenerationMode;
        }
        errors.add("Unsupported generation mode " + effectiveGenerationMode + ".");
        return effectiveGenerationMode;
    }

    private ContractShape resolveContract(
            AgentDefinition definition,
            Map<String, Object> runtimeContract,
            Map<String, Object> blueprint,
            List<String> warnings) {
        Map<String, Object> parameters = mapValue(blueprint == null ? null : blueprint.get("parameters"));
        Map<String, Object> blueprintRuntimeContract = mapValue(parameters == null ? null : parameters.get("runtimeContract"));

        String interpreterType = firstNonBlank(
                definition.getCodAlert() == null || definition.getCodAlert().getSglInterpretertype() == null
                        ? null
                        : definition.getCodAlert().getSglInterpretertype().getSglInterpretertype(),
                stringValue(runtimeContract == null ? null : runtimeContract.get("interpreterType")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("interpreterType")),
                stringValue(parameters == null ? null : parameters.get("interpreterType")));
        String triggerType = firstNonBlank(
                stringValue(runtimeContract == null ? null : runtimeContract.get("triggerType")),
                stringValue(blueprint == null ? null : blueprint.get("triggerType")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("triggerType")),
                stringValue(parameters == null ? null : parameters.get("triggerType")));
        String inputModel = firstNonBlank(
                definition.getDscInputmodel(),
                stringValue(runtimeContract == null ? null : runtimeContract.get("inputModel")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("inputModel")),
                stringValue(parameters == null ? null : parameters.get("inputModel")));
        String outputModel = firstNonBlank(
                definition.getDscOutputmodel(),
                stringValue(runtimeContract == null ? null : runtimeContract.get("outputModel")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("outputModel")),
                stringValue(parameters == null ? null : parameters.get("outputModel")));
        String evaluationMode = firstNonBlank(
                stringValue(runtimeContract == null ? null : runtimeContract.get("evaluationMode")),
                stringValue(blueprint == null ? null : blueprint.get("evaluationMode")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("evaluationMode")),
                stringValue(parameters == null ? null : parameters.get("evaluationMode")));
        String executionModel = firstNonBlank(
                stringValue(runtimeContract == null ? null : runtimeContract.get("executionModel")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("executionModel")),
                stringValue(parameters == null ? null : parameters.get("executionModel")));
        String source = firstNonBlank(
                stringValue(runtimeContract == null ? null : runtimeContract.get("source")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("source")),
                stringValue(parameters == null ? null : parameters.get("source")));
        String accessMode = firstNonBlank(
                stringValue(runtimeContract == null ? null : runtimeContract.get("accessMode")),
                stringValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("accessMode")),
                stringValue(parameters == null ? null : parameters.get("accessMode")));
        Boolean requiresScheduler = booleanValue(firstNonNull(
                runtimeContract == null ? null : runtimeContract.get("requiresScheduler"),
                blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("requiresScheduler"),
                parameters == null ? null : parameters.get("requiresScheduler")));
        if (requiresScheduler == null
                && "SCHEDULED_INTERPRETER".equals(interpreterType)
                && "SCHEDULE".equals(triggerType)
                && "SCHEDULED_SNAPSHOT_MATCH".equals(evaluationMode)) {
            requiresScheduler = true;
            warnings.add("requiresScheduler was missing from runtimeContract and was derived from SCHEDULED_INTERPRETER contract.");
        }
        List<Object> allowedTools = firstNonEmptyList(
                listValue(runtimeContract == null ? null : runtimeContract.get("allowedTools")),
                definition.getJsnAllowedtools(),
                listValue(blueprintRuntimeContract == null ? null : blueprintRuntimeContract.get("allowedTools")),
                listValue(parameters == null ? null : parameters.get("allowedTools")));
        return new ContractShape(
                interpreterType,
                triggerType,
                inputModel,
                outputModel,
                evaluationMode,
                executionModel,
                source,
                accessMode,
                requiresScheduler,
                allowedTools == null ? List.of() : allowedTools);
    }

    private void validateRequiredContractFields(ContractShape contract, List<String> errors) {
        if (isBlank(contract.interpreterType())) {
            errors.add("interpreterType is missing.");
        }
        if (isBlank(contract.triggerType())) {
            errors.add("triggerType is missing.");
        }
        if (isBlank(contract.inputModel())) {
            errors.add("inputModel is missing.");
        }
        if (isBlank(contract.outputModel())) {
            errors.add("outputModel is missing.");
        }
        if (isBlank(contract.evaluationMode())) {
            errors.add("evaluationMode is missing.");
        }
    }

    private boolean hasRequiredFieldErrors(ContractShape contract) {
        return isBlank(contract.interpreterType())
                || isBlank(contract.triggerType())
                || isBlank(contract.inputModel())
                || isBlank(contract.outputModel())
                || isBlank(contract.evaluationMode());
    }

    private void validateSupportedContract(ContractShape contract, List<String> errors) {
        if (isSupportedEvent(contract) || isSupportedScheduled(contract)) {
            return;
        }
        errors.add("Unsupported Agent runtime contract: interpreterType=" + contract.interpreterType()
                + ", triggerType=" + contract.triggerType()
                + ", inputModel=" + contract.inputModel()
                + ", evaluationMode=" + contract.evaluationMode() + ".");
    }

    private void validateContractSpecificRules(
            AgentDefinition definition,
            Map<String, Object> runtimeContract,
            Map<String, Object> blueprint,
            ContractShape contract,
            List<String> errors) {
        if ("EVENT_INTERPRETER".equals(contract.interpreterType())) {
            if (!isBlank(contract.executionModel()) && !"KAFKA_EVENT".equals(contract.executionModel())) {
                errors.add("EVENT_INTERPRETER Agent requires KAFKA_EVENT execution model in the DSL MVP.");
            }
            if (!isBlank(contract.source()) && !"SERVICE_DATA".equals(contract.source())) {
                errors.add("EVENT_INTERPRETER Agent source must be SERVICE_DATA.");
            }
            if (Boolean.TRUE.equals(contract.requiresScheduler())) {
                errors.add("EVENT_INTERPRETER Agent cannot require scheduler execution.");
            }
            if (!contract.allowedTools().isEmpty()) {
                errors.add("EVENT_INTERPRETER Agent cannot require external tools in the DSL MVP.");
            }
            return;
        }
        if ("SCHEDULED_INTERPRETER".equals(contract.interpreterType())) {
            if (!isBlank(contract.accessMode()) && !"SERVICE_DATA_API_SNAPSHOT".equals(contract.accessMode())) {
                errors.add("SCHEDULED_INTERPRETER Agent accessMode must be SERVICE_DATA_API_SNAPSHOT.");
            }
            if (!isBlank(contract.executionModel()) && !"SCHEDULED_POLLING".equals(contract.executionModel())) {
                errors.add("SCHEDULED_INTERPRETER Agent requires SCHEDULED_POLLING execution model in the DSL MVP.");
            }
            if (!isBlank(contract.source()) && !"SERVICE_DATA".equals(contract.source())) {
                errors.add("SCHEDULED_INTERPRETER Agent source must be SERVICE_DATA.");
            }
            if (!Boolean.TRUE.equals(contract.requiresScheduler())) {
                errors.add("SCHEDULED_INTERPRETER Agent must require scheduler execution.");
            }
            if (!containsStringDeep(contract.allowedTools(), SCHEDULED_TOOL)) {
                errors.add("SCHEDULED_INTERPRETER Agent requires SERVICE_DATA_API.POST_/v2/stoppointjourneys tool access.");
            }
        }
    }

    private void validateBlueprintShape(
            Map<String, Object> blueprint,
            Map<String, Object> runtimeContract,
            ContractShape contract,
            List<String> errors) {
        if (blueprint == null || blueprint.isEmpty()) {
            return;
        }
        if ("EVENT_INTERPRETER".equals(contract.interpreterType())) {
            if (!hasAnyNonEmpty(blueprint,
                    List.of("parameters", "condition"),
                    List.of("parameters", "conditions"),
                    List.of("condition"),
                    List.of("conditions"),
                    List.of("evaluation", "condition"),
                    List.of("parameters", "evaluation", "condition"))) {
                errors.add("Event Agent blueprint does not contain an evaluable condition.");
            }
            return;
        }
        if ("SCHEDULED_INTERPRETER".equals(contract.interpreterType())) {
            if (!hasAnyNonEmpty(blueprint, List.of("parameters", "serviceDataQuery"), List.of("serviceDataQuery"))) {
                errors.add("Scheduled Agent blueprint is missing serviceDataQuery.");
            }
            if (!hasAnyNonEmpty(blueprint, List.of("parameters", "snapshotEvaluation"), List.of("snapshotEvaluation"))) {
                errors.add("Scheduled Agent blueprint is missing snapshotEvaluation.");
            }
            if (!hasAnyNonEmpty(blueprint, List.of("parameters", "outputPolicy"), List.of("outputPolicy"))) {
                errors.add("Scheduled Agent blueprint is missing outputPolicy.");
            }
            if (!hasAnyNonEmpty(blueprint,
                    List.of("parameters", "schedule"),
                    List.of("schedule"),
                    List.of("parameters", "technicalSpecification", "schedule"),
                    List.of("technicalSpecification", "schedule"),
                    List.of("parameters", "runtimeContract", "schedule"))
                    && !hasAnyNonEmpty(runtimeContract, List.of("schedule"))
                    && !hasAnyNonEmpty(mapValue(runtimeContract == null ? null : runtimeContract.get("technicalSpecification")), List.of("schedule"))) {
                errors.add("Scheduled Agent blueprint is missing schedule.");
            }
        }
    }

    private boolean isSupportedEvent(ContractShape contract) {
        return "EVENT_INTERPRETER".equals(contract.interpreterType())
                && "EVENT".equals(contract.triggerType())
                && "ServiceDataV2".equals(contract.inputModel())
                && "AgentOutput.CANDIDATE_SUGGESTION".equals(contract.outputModel())
                && "STATELESS_EVENT_MATCH".equals(contract.evaluationMode());
    }

    private boolean isSupportedScheduled(ContractShape contract) {
        return "SCHEDULED_INTERPRETER".equals(contract.interpreterType())
                && "SCHEDULE".equals(contract.triggerType())
                && "ServiceDataStopPointJourneysV2".equals(contract.inputModel())
                && "AgentOutput.CANDIDATE_SUGGESTION".equals(contract.outputModel())
                && "SCHEDULED_SNAPSHOT_MATCH".equals(contract.evaluationMode());
    }

    @SafeVarargs
    private boolean hasAnyNonEmpty(Map<String, Object> source, List<String>... paths) {
        if (source == null) {
            return false;
        }
        for (List<String> path : paths) {
            if (isNonEmpty(nestedValue(source, path))) {
                return true;
            }
        }
        return false;
    }

    private Object nestedValue(Map<String, Object> source, List<String> path) {
        Object value = source;
        for (String segment : path) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(segment);
        }
        return value;
    }

    private boolean isNonEmpty(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Map<?, ?> map) {
            return !map.isEmpty();
        }
        if (value instanceof List<?> list) {
            return !list.isEmpty();
        }
        if (value instanceof String text) {
            return !text.isBlank();
        }
        return true;
    }

    private boolean containsStringDeep(Object value, String expected) {
        if (value == null) {
            return false;
        }
        if (value instanceof String text) {
            return expected.equals(text);
        }
        if (value instanceof Map<?, ?> map) {
            return map.values().stream().anyMatch(nested -> containsStringDeep(nested, expected));
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object nested : iterable) {
                if (containsStringDeep(nested, expected)) {
                    return true;
                }
            }
        }
        return false;
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

    @SuppressWarnings("unchecked")
    private List<Object> listValue(Object value) {
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return List.of();
    }

    @SafeVarargs
    private List<Object> firstNonEmptyList(List<Object>... values) {
        for (List<Object> value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return List.of();
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private AgentCompilationPreconditionValidationResult result(
            boolean valid,
            List<String> errors,
            List<String> warnings,
            String interpreterType,
            String triggerType,
            String inputModel,
            String outputModel,
            String evaluationMode,
            String effectiveGenerationMode,
            String executionModel,
            Map<String, Object> diagnosticDetails) {
        return new AgentCompilationPreconditionValidationResult(
                valid,
                errors == null ? List.of() : errors,
                warnings == null ? List.of() : warnings,
                interpreterType,
                triggerType,
                inputModel,
                outputModel,
                evaluationMode,
                effectiveGenerationMode,
                executionModel,
                diagnosticDetails == null ? Map.of() : diagnosticDetails);
    }

    private record ContractShape(
            String interpreterType,
            String triggerType,
            String inputModel,
            String outputModel,
            String evaluationMode,
            String executionModel,
            String source,
            String accessMode,
            Boolean requiresScheduler,
            List<Object> allowedTools) {
    }
}
