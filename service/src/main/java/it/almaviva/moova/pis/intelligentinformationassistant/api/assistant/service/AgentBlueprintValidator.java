package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataTemporalCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.PlatformNormalizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AgentBlueprintValidator {

    private static final String LOCAL_TIME_BETWEEN = "LOCAL_TIME_BETWEEN";
    private static final String DEFAULT_TEMPORAL_ZONE = "Europe/Rome";
    private static final String STOP_POINTS_JOURNEY_DETAILS_ARRAY_PATH =
            "payload.stopPointJourney.stopPointsJourneyDetails[]";
    private static final String STOP_POINTS_JOURNEY_DETAILS_PREFIX =
            STOP_POINTS_JOURNEY_DETAILS_ARRAY_PATH + ".";
    private static final Set<String> STOP_POINTS_CHILD_ARRAY_PATHS = Set.of(
            "nextCalls[]",
            "nextTransitCalls[]",
            "nextCancelledCalls[]",
            "isReplacementOf[]",
            "replacement.stopPointReplacements[]",
            "externalReplacement.stopPointReplacements[]");
    private static final Set<String> DAY_OF_WEEK_OPERATORS = Set.of(
            "LOCAL_DAY_OF_WEEK_IN", "LOCAL_DAY_OF_WEEK_NOT_IN");
    private static final Set<String> PLATFORM_FIELD_COMPARE_OPERATORS = Set.of(
            "PLATFORM_EQUALS_FIELD", "PLATFORM_NOT_EQUALS_FIELD");
    private static final Set<String> PLATFORM_VALUE_OPERATORS = Set.of(
            "EQUAL_PLATFORM", "NOT_EQUAL_PLATFORM");
    private static final Set<String> PLATFORM_VALUES_OPERATORS = Set.of(
            "IN_PLATFORMS", "NOT_IN_PLATFORMS");
    private static final Set<String> VALUELESS_PLATFORM_OPERATORS = Set.of(
            "PLATFORM_NUMBER_EVEN", "PLATFORM_NUMBER_ODD",
            "PLATFORM_NUMBER_DOUBLE_DIGIT", "PLATFORM_HAS_LETTER_SUFFIX");
    private static final Set<String> NUMERIC_PLATFORM_OPERATORS = Set.of(
            "PLATFORM_NUMBER_GREATER_THAN", "PLATFORM_NUMBER_GREATER_OR_EQUAL",
            "PLATFORM_NUMBER_LESS_THAN", "PLATFORM_NUMBER_LESS_OR_EQUAL",
            "PLATFORM_NUMBER_MULTIPLE_OF");
    private static final Set<String> NUMERIC_PROPERTY_PLATFORM_OPERATORS = Set.of(
            "PLATFORM_NUMBER_GREATER_THAN", "PLATFORM_NUMBER_GREATER_OR_EQUAL",
            "PLATFORM_NUMBER_LESS_THAN", "PLATFORM_NUMBER_LESS_OR_EQUAL",
            "PLATFORM_NUMBER_BETWEEN", "PLATFORM_NUMBER_EVEN", "PLATFORM_NUMBER_ODD",
            "PLATFORM_NUMBER_DOUBLE_DIGIT", "PLATFORM_HAS_LETTER_SUFFIX",
            "PLATFORM_NUMBER_MULTIPLE_OF");
    private static final Set<String> CURRENT_PLATFORM_EVENT_TYPES = Set.of(
            "DEPARTING", "DEPARTED", "ARRIVING", "ARRIVED");
    private static final String PLATFORM_NUMERIC_EVENT_BINDING_REASON =
            "Platform numeric/property predicates must include payload.ongroundServiceEvent.eventsType "
                    + "to bind the predicate to a current ServiceData event.";
    private static final Set<String> PLATFORM_OPERATORS = Set.of(
            "EQUAL_PLATFORM", "NOT_EQUAL_PLATFORM", "IN_PLATFORMS", "NOT_IN_PLATFORMS",
            "PLATFORM_EQUALS_FIELD", "PLATFORM_NOT_EQUALS_FIELD",
            "PLATFORM_NUMBER_GREATER_THAN", "PLATFORM_NUMBER_GREATER_OR_EQUAL",
            "PLATFORM_NUMBER_LESS_THAN", "PLATFORM_NUMBER_LESS_OR_EQUAL",
            "PLATFORM_NUMBER_BETWEEN", "PLATFORM_NUMBER_EVEN", "PLATFORM_NUMBER_ODD",
            "PLATFORM_NUMBER_DOUBLE_DIGIT", "PLATFORM_HAS_LETTER_SUFFIX",
            "PLATFORM_NUMBER_MULTIPLE_OF");
    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HOUR_MINUTE_SECOND = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AgentGenerationCapabilityCatalog capabilityCatalog;
    private final PlatformNormalizer platformNormalizer = new PlatformNormalizer();

    @Inject
    public AgentBlueprintValidator(AgentGenerationCapabilityCatalog capabilityCatalog) {
        this.capabilityCatalog = capabilityCatalog;
    }

    AgentBlueprintValidationResult validate(
            String alertId,
            AlertAgentGenerationPreviewData data,
            AgentBlueprint blueprint,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary,
            List<String> sources,
            List<String> permissions,
            String requestedGenerationMode,
            String recommendedGenerationMode) {
        System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Validating blueprint alertId=" + alertId);
        Set<String> detectedSources = new LinkedHashSet<>(sources);
        Set<String> detectedTargets = new LinkedHashSet<>();
        if (blueprint.getTargetTypes() != null) {
            blueprint.getTargetTypes().forEach(target -> detectedTargets.add(String.valueOf(target)));
        }
        String triggerType = blueprint.getTriggerType() == null ? null : blueprint.getTriggerType().toString();
        String evaluationMode = firstString(blueprint.get("evaluationMode"), data.technicalSpecification().get("evaluationMode"));
        String inputModel = firstString(data.inputModel(), data.technicalSpecification().get("inputModel"));
        Map<String, Object> output = mapValue(blueprint.get("output"));
        String outputModel = firstString(output.get("type"), data.outputModel(), data.technicalSpecification().get("outputModel"));
        boolean requiresState = Boolean.TRUE.equals(blueprint.getStateRequirements().get("requiresState"));
        boolean explicitlyStateless = Boolean.FALSE.equals(blueprint.getStateRequirements().get("requiresState"));

        System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Detected capabilities alertId=" + alertId
                + ", sources=" + detectedSources
                + ", trigger=" + triggerType
                + ", evaluationMode=" + evaluationMode
                + ", operators=" + conditionSummary.dslOperators());

        List<String> errors = new ArrayList<>();
        if (!hasText(firstString(blueprint.get("schemaVersion"), "iia.agent.blueprint/v1"))) {
            errors.add("Blueprint schemaVersion is missing.");
        }
        if (!capabilityCatalog.isSupportedTriggerType(triggerType)) {
            errors.add("Unsupported triggerType: " + triggerType);
        }
        if (detectedSources.isEmpty() || !detectedSources.contains("SERVICE_DATA")) {
            errors.add("Blueprint must require SERVICE_DATA.");
        }
        detectedSources.stream().filter(source -> !capabilityCatalog.isSupportedSource(source))
                .forEach(source -> errors.add("Unsupported source: " + source));
        if (detectedTargets.isEmpty()) {
            errors.add("Blueprint targetTypes are missing.");
        }
        detectedTargets.stream().filter(target -> !capabilityCatalog.isSupportedTargetType(target))
                .forEach(target -> errors.add("Unsupported targetType: " + target));
        if (!capabilityCatalog.isSupportedEvaluationMode(evaluationMode)) {
            errors.add("Unsupported evaluationMode: " + evaluationMode);
        }
        if (!explicitlyStateless) {
            errors.add("Blueprint requires state or does not explicitly declare requiresState=false.");
        }
        if (hasText(inputModel) && !capabilityCatalog.isSupportedInputModel(inputModel)) {
            errors.add("Unsupported inputModel: " + inputModel);
        }
        if (!capabilityCatalog.isSupportedOutputModel(outputModel)) {
            errors.add("Unsupported output type: " + outputModel);
        }
        if (!hasText(firstString(output.get("type")))) {
            errors.add("Blueprint output.type is missing.");
        }
        if (!"SERVICE_DATA_FIELD_MATCH".equals(conditionSummary.conditionType())) {
            errors.add("Unsupported or missing conditionType: " + conditionSummary.conditionType());
        }
        if (conditionSummary.condition().isEmpty() || conditionSummary.partial()) {
            if (conditionSummary.renderIssues().isEmpty()) {
                errors.add("Condition tree cannot be rendered completely by the deterministic DSL renderer.");
            } else {
                conditionSummary.renderIssues().forEach(issue -> {
                    String detail = "Condition tree cannot be rendered completely by the deterministic DSL renderer at "
                            + issue.path() + ": " + issue.reason() + "; keys=" + issue.keys() + ".";
                    errors.add(detail);
                    System.out.println("[IIA][AGENT_DSL_RENDERER] Unsupported condition node alertId=" + alertId
                            + ", path=" + issue.path()
                            + ", reason=" + issue.reason()
                            + ", operator=" + issue.operator()
                            + ", keys=" + issue.keys()
                            + ", node=" + issue.snippet());
                    System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Condition renderability failed alertId="
                            + alertId + ", path=" + issue.path() + ", reason=" + issue.reason());
                });
            }
        }
        conditionSummary.dslOperators().stream()
                .filter(operator -> !capabilityCatalog.isSupportedDslOperator(operator))
                .forEach(operator -> errors.add("Unsupported DSL operator: " + operator));
        Set<String> temporalOperators = conditionSummary.dslOperators().stream()
                .filter(this::isTemporalOperator)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (!temporalOperators.isEmpty()) {
            System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL] operators found=" + temporalOperators);
        }
        validateConditionCapabilities(conditionSummary.condition(), "condition", null, errors);
        validatePlatformNumericEventBinding(conditionSummary.condition(), errors);

        Set<String> forbidden = new LinkedHashSet<>();
        findForbidden(blueprint, forbidden);
        findForbidden(data.technicalSpecification(), forbidden);
        sources.stream()
                .filter(capabilityCatalog::isForbiddenCapability)
                .forEach(forbidden::add);
        forbidden.forEach(capability -> errors.add("Forbidden capability: " + capability));

        AgentGenerationCapabilitySnapshot snapshot = new AgentGenerationCapabilitySnapshot(
                List.copyOf(detectedSources),
                permissions,
                triggerType,
                evaluationMode,
                inputModel,
                outputModel,
                List.copyOf(detectedTargets),
                conditionSummary.dslOperators(),
                explicitlyStateless,
                requestedGenerationMode,
                recommendedGenerationMode);
        AgentGenerationCapabilityCatalog.RuntimeSupportEvaluation runtime =
                capabilityCatalog.evaluateRuntimeSupport(snapshot);
        LinkedHashSet<String> unsupported = new LinkedHashSet<>(runtime.unsupportedCapabilities());
        unsupported.addAll(forbidden);
        boolean valid = errors.isEmpty();
        boolean runtimeSupported = valid && runtime.supported() && unsupported.isEmpty();
        System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL] validation result alertId=" + alertId
                + " runtimeSupported=" + runtimeSupported);
        if (valid) {
            System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Blueprint valid alertId=" + alertId
                    + ", runtimeSupported=" + runtimeSupported);
        } else {
            System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Blueprint invalid alertId=" + alertId
                    + ", errors=" + errors);
        }
        if (!unsupported.isEmpty()) {
            System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Unsupported capabilities alertId=" + alertId
                    + ", unsupported=" + unsupported);
        }
        return new AgentBlueprintValidationResult(
                valid,
                runtimeSupported,
                List.copyOf(unsupported),
                List.copyOf(errors),
                List.of(),
                Set.copyOf(conditionSummary.dslOperators()),
                Set.copyOf(detectedSources),
                Set.copyOf(detectedTargets),
                requiresState,
                triggerType,
                evaluationMode,
                inputModel,
                outputModel);
    }

    private void validateConditionCapabilities(
            Map<String, Object> node,
            String path,
            String arrayPath,
            List<String> errors) {
        if (node.containsKey("anyElement")) {
            if (arrayPath != null && !isAllowedNestedAnyElement(node, arrayPath)) {
                rejectArray(errors, path,
                        "nested anyElement is supported only for relative child arrays under stopPointsJourneyDetails[].");
                return;
            }
            validateAnyElement(node.get("anyElement"), path + ".anyElement", arrayPath, errors);
            return;
        }
        if (node.containsKey("field") || node.containsKey("operator")) {
            validateConditionLeaf(node, path, arrayPath, errors);
            return;
        }
        for (String group : List.of("all", "any")) {
            if (!(node.get(group) instanceof List<?> children)) {
                continue;
            }
            if ("all".equals(group) && hasFlattenedStopPointChildAnyElementCorrelation(children)) {
                rejectTemporalCorrelation(errors, path + "." + group,
                        "nested anyElement is required to preserve correlation on the same stopPointsJourneyDetails element.");
                continue;
            }
            for (int index = 0; index < children.size(); index++) {
                if (children.get(index) instanceof Map<?, ?> child) {
                    validateConditionCapabilities(mapValue(child), path + "." + group + "[" + index + "]",
                            arrayPath, errors);
                }
            }
        }
    }

    private void validatePlatformNumericEventBinding(Map<String, Object> condition, List<String> errors) {
        if (!containsPlatformNumericPropertyOperator(condition)) {
            return;
        }
        boolean bound = containsTopLevelCurrentPlatformEventBinding(condition);
        System.out.println("[IIA][AGENT_PREVIEW][PLATFORM_NUMERIC_EVENT_BINDING] "
                + (bound ? "validated" : "rejected")
                + " reason=" + (bound ? "current ServiceData eventsType binding found" : PLATFORM_NUMERIC_EVENT_BINDING_REASON));
        if (!bound) {
            errors.add(PLATFORM_NUMERIC_EVENT_BINDING_REASON);
        }
    }

    private boolean containsPlatformNumericPropertyOperator(Object node) {
        if (node instanceof Map<?, ?> map) {
            String operator = stringValue(map.get("operator"));
            if (operator != null && NUMERIC_PROPERTY_PLATFORM_OPERATORS.contains(operator)) {
                return true;
            }
            return map.values().stream().anyMatch(this::containsPlatformNumericPropertyOperator);
        }
        if (node instanceof List<?> list) {
            return list.stream().anyMatch(this::containsPlatformNumericPropertyOperator);
        }
        return false;
    }

    private boolean containsTopLevelCurrentPlatformEventBinding(Map<?, ?> node) {
        if (isCurrentPlatformEventBinding(node)) {
            return true;
        }
        for (String group : List.of("all", "any")) {
            if (!(node.get(group) instanceof List<?> children)) {
                continue;
            }
            for (Object child : children) {
                if (child instanceof Map<?, ?> childMap && isCurrentPlatformEventBinding(childMap)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCurrentPlatformEventBinding(Map<?, ?> leaf) {
        if (!"payload.ongroundServiceEvent.eventsType".equals(stringValue(leaf.get("field")))) {
            return false;
        }
        String operator = stringValue(leaf.get("operator"));
        if ("CONTAINS".equals(operator)) {
            return CURRENT_PLATFORM_EVENT_TYPES.contains(stringValue(leaf.get("value")));
        }
        if ("CONTAINS_ANY".equals(operator) && leaf.get("values") instanceof List<?> values && !values.isEmpty()) {
            return values.stream().map(this::stringValue).allMatch(CURRENT_PLATFORM_EVENT_TYPES::contains);
        }
        return false;
    }

    private void validateAnyElement(Object value, String path, String parentArrayPath, List<String> errors) {
        Map<String, Object> anyElement = mapValue(value);
        String rawArrayPath = stringValue(anyElement.get("path"));
        String arrayPath = resolveAnyElementPath(rawArrayPath, parentArrayPath);
        System.out.println("[IIA][AGENT_PREVIEW][ARRAY] validating anyElement path=" + rawArrayPath
                + " resolvedPath=" + arrayPath);
        if (!capabilityCatalog.isSupportedArrayPath(arrayPath)) {
            rejectArray(errors, path, "path is not supported: " + rawArrayPath);
            return;
        }
        Map<String, Object> conditions = mapValue(anyElement.get("conditions"));
        if (conditions.isEmpty()) {
            rejectArray(errors, path, "conditions are missing.");
            return;
        }
        validateConditionCapabilities(conditions, path + ".conditions", arrayPath, errors);
        if (errors.isEmpty() && containsTemporalOperator(conditions)
                && arrayPath.endsWith("nextCalls[]")
                && !hasCorrelatedStopAndTime(conditions)) {
            rejectArray(errors, path,
                    "temporal nextCalls constraints must correlate stopPoint.nameLong and departureTime/arrivalTime in the same all group.");
        }
    }

    private void validateConditionLeaf(
            Map<String, Object> leaf,
            String path,
            String arrayPath,
            List<String> errors) {
        String field = stringValue(leaf.get("field"));
        String operator = stringValue(leaf.get("operator"));
        if (arrayPath != null) {
            System.out.println("[IIA][AGENT_PREVIEW][ARRAY] validating relative field=" + field
                    + " operator=" + operator + " path=" + arrayPath);
            String absoluteField = arrayPath + "." + field;
            if (ServiceDataCapabilityCatalog.isPlatformTechnicalIdField(absoluteField)
                    && PLATFORM_OPERATORS.contains(operator)) {
                errors.add(path + ": platform technical id field is not supported for user platform matching: " + field);
                return;
            }
            ServiceDataCapabilityCatalog.FieldCapability capability = ServiceDataCapabilityCatalog.findField(absoluteField)
                    .orElse(null);
            if (capability == null) {
                rejectArray(errors, path, "relative field is not supported: " + field);
                return;
            }
            if (!capability.supportsOperator(operator)) {
                rejectArray(errors, path, "operator is not supported for relative field " + field + ": " + operator);
                return;
            }
            if (PLATFORM_FIELD_COMPARE_OPERATORS.contains(operator)) {
                validatePlatformFieldComparison(leaf, path, absoluteField, arrayPath, errors);
                return;
            }
            if (capability.type() == ServiceDataCapabilityCatalog.FieldType.PLATFORM) {
                validatePlatformValue(leaf, path, absoluteField, operator, errors);
                return;
            }
            if (isStopPointIdField(absoluteField)) {
                validateStopPointIdValue(leaf, path, absoluteField, operator, errors);
                return;
            }
            if (isTemporalOperator(operator)) {
                validateTemporalValue(leaf, path, absoluteField, operator, errors);
            }
            return;
        }
        if (ServiceDataCapabilityCatalog.isPlatformTechnicalIdField(field)
                && PLATFORM_OPERATORS.contains(operator)) {
            errors.add(path + ": platform technical id field is not supported for user platform matching: " + field);
            return;
        }
        if (PLATFORM_FIELD_COMPARE_OPERATORS.contains(operator)) {
            ServiceDataCapabilityCatalog.FieldCapability capability = ServiceDataCapabilityCatalog.findField(field)
                    .orElse(null);
            if (capability == null || !capability.supportsOperator(operator)) {
                errors.add(path + ": operator is not supported on platform field " + field + ": " + operator);
                return;
            }
            validatePlatformFieldComparison(leaf, path, field, null, errors);
            return;
        }
        ServiceDataCapabilityCatalog.FieldCapability platformCapability = ServiceDataCapabilityCatalog.findField(field)
                .orElse(null);
        if (platformCapability != null && platformCapability.type() == ServiceDataCapabilityCatalog.FieldType.PLATFORM) {
            if (!platformCapability.supportsOperator(operator)) {
                errors.add(path + ": operator is not supported on platform field " + field + ": " + operator);
                return;
            }
            validatePlatformValue(leaf, path, field, operator, errors);
            return;
        }
        if ("payload.ongroundServiceEvent.eventsType".equals(field)) {
            validateCurrentEventType(leaf, path, field, operator, errors);
            return;
        }
        if (isStopPointIdField(field)) {
            ServiceDataCapabilityCatalog.FieldCapability capability = ServiceDataCapabilityCatalog.findField(field)
                    .orElse(null);
            if (capability == null) {
                errors.add(path + ": stopPoint.id field is not supported: " + field);
                return;
            }
            if (!capability.supportsOperator(operator)) {
                errors.add(path + ": operator is not supported on stopPoint.id field " + field + ": " + operator);
                return;
            }
            validateStopPointIdValue(leaf, path, field, operator, errors);
            return;
        }
        if (isTemporalOperator(operator)) {
            if (!capabilityCatalog.isSupportedTemporalField(field)) {
                rejectTemporal(errors, path, "field is not supported for " + operator + ": " + field);
                return;
            }
            if (field != null && field.contains("[]")) {
                rejectTemporal(errors, path, "array temporal fields must be represented inside a correlated anyElement node.");
                return;
            }
            validateTemporalValue(leaf, path, field, operator, errors);
        } else if (capabilityCatalog.isSupportedTemporalField(field)) {
            rejectTemporal(errors, path, "operator is not supported on temporal field " + field + ": " + operator);
        } else if (isPotentialTemporalOperator(operator)) {
            rejectTemporal(errors, path, "temporal operator is not supported: " + operator);
        }
    }

    private void validatePlatformFieldComparison(
            Map<String, Object> leaf,
            String path,
            String field,
            String arrayPath,
            List<String> errors) {
        String rawOtherField = stringValue(leaf.get("otherField"));
        if (rawOtherField == null || rawOtherField.isBlank()) {
            errors.add(path + ": platform field comparison requires a non-empty otherField for " + field);
            return;
        }
        String otherField = arrayPath != null && !rawOtherField.startsWith("payload.")
                ? arrayPath + "." + rawOtherField
                : rawOtherField;
        if (ServiceDataCapabilityCatalog.isPlatformTechnicalIdField(otherField)) {
            errors.add(path + ": platform field comparison otherField cannot be a platform technical id field: "
                    + rawOtherField);
            return;
        }
        ServiceDataCapabilityCatalog.FieldCapability otherCapability =
                ServiceDataCapabilityCatalog.findField(otherField).orElse(null);
        if (otherCapability == null || otherCapability.type() != ServiceDataCapabilityCatalog.FieldType.PLATFORM) {
            errors.add(path + ": platform field comparison otherField is not a supported platform description field: "
                    + rawOtherField);
        }
    }

    private void validatePlatformValue(
            Map<String, Object> leaf,
            String path,
            String field,
            String operator,
            List<String> errors) {
        if (VALUELESS_PLATFORM_OPERATORS.contains(operator)) {
            if (leaf.containsKey("value") || leaf.containsKey("values")) {
                errors.add(path + ": " + operator + " does not accept value for " + field);
            }
            return;
        }
        if (NUMERIC_PLATFORM_OPERATORS.contains(operator)) {
            Object value = leaf.get("value");
            if (!(value instanceof Number number)) {
                errors.add(path + ": " + operator + " requires numeric value for " + field);
            } else if ("PLATFORM_NUMBER_MULTIPLE_OF".equals(operator) && number.doubleValue() <= 0) {
                errors.add(path + ": PLATFORM_NUMBER_MULTIPLE_OF requires numeric value greater than 0 for " + field);
            }
            return;
        }
        if ("PLATFORM_NUMBER_BETWEEN".equals(operator)) {
            Object value = leaf.get("value");
            if (!(value instanceof Map<?, ?> range)
                    || !(range.get("min") instanceof Number min)
                    || !(range.get("max") instanceof Number max)) {
                errors.add(path + ": PLATFORM_NUMBER_BETWEEN requires value with numeric min and max for " + field);
            } else if (min.doubleValue() > max.doubleValue()) {
                errors.add(path + ": PLATFORM_NUMBER_BETWEEN requires min less than or equal to max for " + field);
            }
            return;
        }
        if (PLATFORM_VALUE_OPERATORS.contains(operator)) {
            Object value = leaf.get("value");
            if (!(value instanceof String text) || text.isBlank() || !platformNormalizer.normalize(text).hasNumber()) {
                errors.add(path + ": " + operator + " requires a non-empty human platform value for " + field);
            }
            return;
        }
        if (PLATFORM_VALUES_OPERATORS.contains(operator)) {
            Object values = leaf.get("values");
            if (!(values instanceof List<?> platforms) || platforms.isEmpty()) {
                errors.add(path + ": " + operator + " requires a non-empty values array for " + field);
                return;
            }
            if (!platforms.stream().allMatch(value -> value instanceof String text
                    && !text.isBlank()
                    && platformNormalizer.normalize(text).hasNumber())) {
                errors.add(path + ": " + operator + " requires only non-empty human platform values for " + field);
            }
        }
    }

    private void validateCurrentEventType(
            Map<String, Object> leaf,
            String path,
            String field,
            String operator,
            List<String> errors) {
        ServiceDataCapabilityCatalog.FieldCapability capability =
                ServiceDataCapabilityCatalog.findField(field).orElse(null);
        if (capability == null || !capability.supportsOperator(operator)) {
            errors.add(path + ": operator is not supported on current eventsType field: " + operator);
            return;
        }
        List<?> values;
        if ("CONTAINS_ANY".equals(operator)) {
            if (!(leaf.get("values") instanceof List<?> candidates) || candidates.isEmpty()) {
                errors.add(path + ": CONTAINS_ANY requires a non-empty values array for " + field);
                return;
            }
            values = candidates;
        } else {
            Object value = leaf.get("value");
            if (value == null) {
                errors.add(path + ": " + operator + " requires value for " + field);
                return;
            }
            values = List.of(value);
        }
        if (values.stream().anyMatch(value -> value == null
                || !ServiceDataCapabilityCatalog.isAllowedEnumValue(field, value))) {
            errors.add(path + ": enum value is not supported on current eventsType field.");
        }
    }

    private void validateStopPointIdValue(
            Map<String, Object> leaf,
            String path,
            String field,
            String operator,
            List<String> errors) {
        if ("EQUALS".equals(operator)) {
            Object value = leaf.get("value");
            if (!(value instanceof String id) || id.isBlank()) {
                errors.add(path + ": stopPoint.id EQUALS requires a non-empty string value for " + field);
            }
            return;
        }
        if ("IN".equals(operator) || "NOT_IN".equals(operator)) {
            Object values = leaf.get("values");
            if (!(values instanceof List<?> ids) || ids.isEmpty()) {
                errors.add(path + ": stopPoint.id " + operator + " requires a non-empty values array for " + field);
                return;
            }
            if (!ids.stream().allMatch(value -> value instanceof String id && !id.isBlank())) {
                errors.add(path + ": stopPoint.id " + operator + " requires only non-empty string values for " + field);
            }
            return;
        }
        errors.add(path + ": operator is not supported on stopPoint.id field " + field + ": " + operator);
    }

    private boolean isStopPointIdField(String field) {
        return field != null && (field.endsWith(".stopPoint.id") || field.endsWith(".stopPointId.id"));
    }

    private void validateTemporalValue(
            Map<String, Object> leaf,
            String path,
            String field,
            String operator,
            List<String> errors) {
        Map<String, Object> value = mapValue(leaf.get("value"));
        if (value.isEmpty()) {
            rejectTemporal(errors, path, "value must be an object.");
            return;
        }
        if (!ServiceDataTemporalCapabilityCatalog.isAllowedOperator(field, operator)) {
            rejectTemporal(errors, path, "operator is not supported on temporal field " + field + ": " + operator);
            return;
        }
        String timezone = stringValue(value.get("timezone"));
        if (timezone == null || timezone.isBlank()) {
            timezone = DEFAULT_TEMPORAL_ZONE;
            value.put("timezone", timezone);
            leaf.put("value", value);
        }
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            rejectTemporal(errors, path, "timezone is not a valid zone id: " + timezone);
            return;
        }
        if (DAY_OF_WEEK_OPERATORS.contains(operator)) {
            validateDayOfWeekValue(value, path, field, operator, timezone, errors);
            return;
        }
        String start = stringValue(value.get("start"));
        String end = stringValue(value.get("end"));
        if (parseLocalTime(start) == null || parseLocalTime(end) == null) {
            rejectTemporal(errors, path, "start and end must use HH:mm or HH:mm:ss.");
            return;
        }
        System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL] recognized operator=" + LOCAL_TIME_BETWEEN
                + " field=" + field + " start=" + start + " end=" + end + " timezone=" + timezone);
    }

    private void validateDayOfWeekValue(
            Map<String, Object> value,
            String path,
            String field,
            String operator,
            String timezone,
            List<String> errors) {
        Object daysValue = value.get("days");
        if (!(daysValue instanceof List<?> days) || days.isEmpty()) {
            rejectTemporal(errors, path, "value.days must be a non-empty array.");
            return;
        }
        List<String> normalizedDays = new ArrayList<>();
        for (Object rawDay : days) {
            String day = stringValue(rawDay);
            if (day == null || day.isBlank()) {
                rejectTemporal(errors, path, "value.days contains an empty day.");
                return;
            }
            try {
                normalizedDays.add(DayOfWeek.valueOf(day.toUpperCase(java.util.Locale.ROOT)).name());
            } catch (IllegalArgumentException exception) {
                rejectTemporal(errors, path, "day is not supported: " + day);
                return;
            }
        }
        value.put("days", normalizedDays);
        System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL] recognized operator=" + operator
                + " field=" + field + " days=" + normalizedDays + " timezone=" + timezone);
    }

    private LocalTime parseLocalTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            if (value.matches("\\d{2}:\\d{2}")) {
                return LocalTime.parse(value, HOUR_MINUTE);
            }
            if (value.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return LocalTime.parse(value, HOUR_MINUTE_SECOND);
            }
        } catch (DateTimeParseException exception) {
            return null;
        }
        return null;
    }

    private boolean isPotentialTemporalOperator(String operator) {
        if (operator == null) {
            return false;
        }
        String normalized = operator.toUpperCase();
        return normalized.contains("TIME") || normalized.contains("DAY_OF_WEEK");
    }

    private boolean containsTemporalOperator(Object node) {
        if (node instanceof Map<?, ?> map) {
            if (isTemporalOperator(stringValue(map.get("operator")))) {
                return true;
            }
            return map.values().stream().anyMatch(this::containsTemporalOperator);
        }
        if (node instanceof List<?> list) {
            return list.stream().anyMatch(this::containsTemporalOperator);
        }
        return false;
    }

    private boolean hasCorrelatedStopAndTime(Map<String, Object> conditions) {
        if (!(conditions.get("all") instanceof List<?> children)) {
            return false;
        }
        boolean stopPoint = children.stream().anyMatch(child -> isRelativeLeaf(
                child, "stopPoint.nameLong", Set.of("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED")));
        boolean timestamp = children.stream().anyMatch(child -> isRelativeLeaf(
                child, "departureTime", ServiceDataTemporalCapabilityCatalog.temporalOperators())
                || isRelativeLeaf(child, "arrivalTime", ServiceDataTemporalCapabilityCatalog.temporalOperators()));
        return stopPoint && timestamp;
    }

    private boolean isTemporalOperator(String operator) {
        return ServiceDataTemporalCapabilityCatalog.isTemporalOperator(operator);
    }

    private boolean isRelativeLeaf(Object node, String field, Set<String> operators) {
        if (!(node instanceof Map<?, ?> map)) {
            return false;
        }
        return field.equals(stringValue(map.get("field")))
                && operators.contains(stringValue(map.get("operator")));
    }

    private String resolveAnyElementPath(String rawArrayPath, String parentArrayPath) {
        if (rawArrayPath == null || rawArrayPath.isBlank()) {
            return null;
        }
        if (parentArrayPath == null || rawArrayPath.startsWith("payload.")) {
            return rawArrayPath;
        }
        if (STOP_POINTS_JOURNEY_DETAILS_ARRAY_PATH.equals(parentArrayPath)
                && STOP_POINTS_CHILD_ARRAY_PATHS.contains(rawArrayPath)) {
            return parentArrayPath + "." + rawArrayPath;
        }
        return null;
    }

    private boolean isAllowedNestedAnyElement(Map<String, Object> node, String parentArrayPath) {
        if (!STOP_POINTS_JOURNEY_DETAILS_ARRAY_PATH.equals(parentArrayPath)) {
            return false;
        }
        Map<String, Object> anyElement = mapValue(node.get("anyElement"));
        String nestedPath = stringValue(anyElement.get("path"));
        return STOP_POINTS_CHILD_ARRAY_PATHS.contains(nestedPath);
    }

    private boolean hasFlattenedStopPointChildAnyElementCorrelation(List<?> children) {
        boolean hasStopPointsJourneyDetailsAnyElement = false;
        boolean hasChildArrayAnyElement = false;
        for (Object child : children) {
            if (!(child instanceof Map<?, ?> childMap)) {
                continue;
            }
            String path = directAnyElementPath(childMap);
            if (STOP_POINTS_JOURNEY_DETAILS_ARRAY_PATH.equals(path)) {
                hasStopPointsJourneyDetailsAnyElement = true;
            } else if (isAbsoluteStopPointsChildArrayPath(path)) {
                hasChildArrayAnyElement = true;
            }
        }
        return hasStopPointsJourneyDetailsAnyElement && hasChildArrayAnyElement;
    }

    private String directAnyElementPath(Map<?, ?> node) {
        Object anyElement = node.get("anyElement");
        if (!(anyElement instanceof Map<?, ?> anyElementMap)) {
            return null;
        }
        return stringValue(anyElementMap.get("path"));
    }

    private boolean isAbsoluteStopPointsChildArrayPath(String path) {
        if (path == null || !path.startsWith(STOP_POINTS_JOURNEY_DETAILS_PREFIX)) {
            return false;
        }
        String relativePath = path.substring(STOP_POINTS_JOURNEY_DETAILS_PREFIX.length());
        return STOP_POINTS_CHILD_ARRAY_PATHS.contains(relativePath);
    }

    private void rejectTemporal(List<String> errors, String path, String reason) {
        System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL] rejected path=" + path + " reason=" + reason);
        errors.add("Unsupported temporal condition at " + path + ": " + reason);
    }

    private void rejectArray(List<String> errors, String path, String reason) {
        System.out.println("[IIA][AGENT_PREVIEW][ARRAY] rejected path=" + path + " reason=" + reason);
        errors.add("Unsupported anyElement condition at " + path + ": " + reason);
    }

    private void rejectTemporalCorrelation(List<String> errors, String path, String reason) {
        System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL][CORRELATION] rejected path=" + path
                + " reason=" + reason);
        errors.add("Unsupported anyElement condition at " + path + ": " + reason);
    }

    private void findForbidden(Object value, Set<String> forbidden) {
        if (value instanceof Map<?, ?> map) {
            map.values().forEach(nested -> findForbidden(nested, forbidden));
        } else if (value instanceof List<?> list) {
            list.forEach(nested -> findForbidden(nested, forbidden));
        } else if (value != null && capabilityCatalog.isForbiddenCapability(String.valueOf(value))) {
            forbidden.add(String.valueOf(value));
        }
    }

    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        map.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
        return result;
    }

    private String firstString(Object... values) {
        for (Object value : values) {
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
