package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataTemporalCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AgentGenerationCapabilityCatalog {

    private static final Set<String> SOURCES = orderedSet("SERVICE_DATA");
    private static final Set<String> TRIGGER_TYPES = orderedSet("EVENT");
    private static final Set<String> EVALUATION_MODES = orderedSet("STATELESS_EVENT_MATCH");
    private static final Set<String> INPUT_MODELS = orderedSet("ServiceDataV2");
    private static final Set<String> OUTPUT_MODELS = orderedSet("AgentOutput.CANDIDATE_SUGGESTION", "CANDIDATE_SUGGESTION");
    private static final Set<String> TARGET_TYPES = orderedSet("SERVICE_DATA_JOURNEY");
    private static final Set<String> GENERATION_MODES = orderedSet("AUTO", "DSL");
    private static final Set<String> PREVIEW_ONLY_GENERATION_MODES = orderedSet("JAVA_TEMPLATE");
    private static final Set<String> COMPLEXITIES = orderedSet("LOW", "MEDIUM", "HIGH", "UNSUPPORTED");
    private static final Set<String> PERMISSIONS = orderedSet("READ_SERVICE_DATA");
    private static final Set<String> DSL_OPERATORS = orderedSet(
            "CONTAINS", "CONTAINS_ANY", "CONTAINS_IGNORE_CASE", "CONTAINS_NORMALIZED",
            "EQUALS", "EQUALS_IGNORE_CASE", "EQUALS_NORMALIZED", "NOT_EQUALS",
            "GREATER_THAN", "GREATER_OR_EQUAL", "LESS_THAN", "LESS_OR_EQUAL",
            "EXISTS", "NOT_NULL", "NOT_EMPTY", "IN", "NOT_IN", "LOCAL_TIME_BETWEEN",
            "LOCAL_DAY_OF_WEEK_IN", "LOCAL_DAY_OF_WEEK_NOT_IN",
            "EQUAL_PLATFORM", "NOT_EQUAL_PLATFORM", "IN_PLATFORMS", "NOT_IN_PLATFORMS");
    private static final Set<String> DSL_LOGICAL_NODES = orderedSet("all", "any", "anyElement");
    private static final String NEXT_CALLS_ARRAY_PATH =
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]";
    private static final Set<String> ARRAY_PATHS = orderedSet(
            "payload.stopPointJourney.stopPointsJourneyDetails[]",
            NEXT_CALLS_ARRAY_PATH,
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[]",
            "payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf[]");
    private static final Set<String> DSL_FUNCTIONS = orderedSet(
            "normalize", "contains", "equals", "equalsIgnoreCase", "in", "exists");
    private static final Set<String> FORBIDDEN_CAPABILITIES = orderedSet(
            "EXTERNAL_HTTP",
            "DATABASE_QUERY",
            "FILESYSTEM",
            "KAFKA_PRODUCER",
            "ARBITRARY_CODE",
            "DEVICE_CONTROL",
            "AUDIO_PLAYBACK",
            "VIDEO_PLAYBACK",
            "CONTENT_PLAYBACK",
            "UNSAFE_JAVA",
            "SHELL_COMMAND");
    private static final Map<String, String> SOURCE_PERMISSIONS = Map.of("SERVICE_DATA", "READ_SERVICE_DATA");

    public AgentGenerationCapabilityCatalog() {
        System.out.println("[IIA][AGENT_CAPABILITY] Catalog initialized sources=" + SOURCES
                + ", generationModes=" + GENERATION_MODES
                + ", dslOperators=" + DSL_OPERATORS);
    }

    public boolean isSupportedSource(String source) {
        return SOURCES.contains(source);
    }

    public boolean isSupportedTriggerType(String triggerType) {
        return TRIGGER_TYPES.contains(triggerType);
    }

    public boolean isSupportedEvaluationMode(String evaluationMode) {
        return EVALUATION_MODES.contains(evaluationMode);
    }

    public boolean isSupportedInputModel(String inputModel) {
        return INPUT_MODELS.contains(inputModel);
    }

    public boolean isSupportedOutputModel(String outputModel) {
        return OUTPUT_MODELS.contains(outputModel);
    }

    public boolean isSupportedTargetType(String targetType) {
        return TARGET_TYPES.contains(targetType);
    }

    public boolean isSupportedGenerationMode(String generationMode) {
        return GENERATION_MODES.contains(generationMode);
    }

    public boolean isPreviewOnlyGenerationMode(String generationMode) {
        return PREVIEW_ONLY_GENERATION_MODES.contains(generationMode);
    }

    public boolean isSupportedDslOperator(String operator) {
        return DSL_OPERATORS.contains(operator);
    }

    public boolean isSupportedDslFunction(String function) {
        return DSL_FUNCTIONS.contains(function);
    }

    public boolean isSupportedTemporalField(String field) {
        return ServiceDataTemporalCapabilityCatalog.isAllowedTemporalField(field);
    }

    public boolean isSupportedArrayPath(String path) {
        return hasText(path) && (ARRAY_PATHS.contains(path)
                || ServiceDataCapabilityCatalog.fields().stream()
                .map(ServiceDataCapabilityCatalog.FieldCapability::field)
                .anyMatch(field -> field.startsWith(path + ".")));
    }

    public boolean isSupportedArrayRelativeField(String path, String field) {
        return hasText(path) && hasText(field)
                && ServiceDataCapabilityCatalog.findField(path + "." + field).isPresent();
    }

    public boolean isSupportedPermission(String permission) {
        return PERMISSIONS.contains(permission);
    }

    public boolean isForbiddenCapability(String capability) {
        return FORBIDDEN_CAPABILITIES.contains(capability);
    }

    public Set<String> supportedSources() {
        return SOURCES;
    }

    public Set<String> supportedPermissions() {
        return PERMISSIONS;
    }

    public Set<String> supportedGenerationModes() {
        return GENERATION_MODES;
    }

    public Set<String> supportedComplexities() {
        return COMPLEXITIES;
    }

    public Set<String> previewOnlyGenerationModes() {
        return PREVIEW_ONLY_GENERATION_MODES;
    }

    public Set<String> supportedDslOperators() {
        return DSL_OPERATORS;
    }

    public Set<String> supportedDslLogicalNodes() {
        return DSL_LOGICAL_NODES;
    }

    public Set<String> supportedDslFunctions() {
        return DSL_FUNCTIONS;
    }

    public Set<String> supportedTemporalFields() {
        return ServiceDataTemporalCapabilityCatalog.temporalFields();
    }

    public Set<String> supportedArrayPaths() {
        return ARRAY_PATHS;
    }

    public Set<String> supportedArrayRelativeFields(String path) {
        if (!hasText(path)) {
            return Set.of();
        }
        String prefix = path + ".";
        return ServiceDataCapabilityCatalog.fields().stream()
                .map(ServiceDataCapabilityCatalog.FieldCapability::field)
                .filter(field -> field.startsWith(prefix))
                .map(field -> field.substring(prefix.length()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> forbiddenCapabilities() {
        return FORBIDDEN_CAPABILITIES;
    }

    public String permissionForSource(String source) {
        return SOURCE_PERMISSIONS.get(source);
    }

    public List<String> permissionsForSources(List<String> sources) {
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        sources.stream()
                .map(this::permissionForSource)
                .filter(permission -> permission != null && !permission.isBlank())
                .forEach(permissions::add);
        List<String> result = List.copyOf(permissions);
        System.out.println("[IIA][AGENT_CAPABILITY] Resolved permissions sources=" + sources
                + ", permissions=" + result);
        return result;
    }

    public AgentDataSource defaultSource() {
        return AgentDataSource.SERVICE_DATA;
    }

    public AgentGenerationMode recommendedDefaultGenerationMode() {
        return AgentGenerationMode.DSL;
    }

    public AgentComplexity defaultMvpComplexity() {
        return AgentComplexity.LOW;
    }

    public RuntimeSupportEvaluation evaluateRuntimeSupport(AgentGenerationCapabilitySnapshot snapshot) {
        LinkedHashSet<String> unsupported = new LinkedHashSet<>();
        collectUnsupported(snapshot.sources(), this::isSupportedSource, unsupported);
        collectUnsupported(snapshot.permissions(), this::isSupportedPermission, unsupported);
        collectUnsupported(List.of(snapshot.triggerType()), this::isSupportedTriggerType, unsupported);
        collectUnsupported(List.of(snapshot.evaluationMode()), this::isSupportedEvaluationMode, unsupported);
        collectUnsupported(List.of(snapshot.inputModel()), this::isSupportedInputModel, unsupported);
        collectUnsupported(List.of(snapshot.outputModel()), this::isSupportedOutputModel, unsupported);
        collectUnsupported(snapshot.targetTypes(), this::isSupportedTargetType, unsupported);
        collectUnsupported(new ArrayList<>(snapshot.dslOperators()), this::isSupportedDslOperator, unsupported);
        if (!snapshot.explicitlyStateless()) {
            unsupported.add("STATEFUL_OR_UNSPECIFIED_RUNTIME");
        }
        boolean supported = unsupported.isEmpty();
        System.out.println("[IIA][AGENT_CAPABILITY] Runtime support evaluated supported=" + supported
                + ", unsupportedCapabilities=" + unsupported);
        return new RuntimeSupportEvaluation(supported, java.util.Collections.unmodifiableSet(unsupported));
    }

    private void collectUnsupported(
            List<String> values,
            java.util.function.Predicate<String> supported,
            Set<String> unsupported) {
        values.stream()
                .filter(value -> value == null || !supported.test(value))
                .map(value -> value == null ? "UNSPECIFIED" : value)
                .forEach(unsupported::add);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @SafeVarargs
    private static <T> Set<T> orderedSet(T... values) {
        LinkedHashSet<T> result = new LinkedHashSet<>(List.of(values));
        return java.util.Collections.unmodifiableSet(result);
    }

    record RuntimeSupportEvaluation(boolean supported, Set<String> unsupportedCapabilities) {
    }
}
