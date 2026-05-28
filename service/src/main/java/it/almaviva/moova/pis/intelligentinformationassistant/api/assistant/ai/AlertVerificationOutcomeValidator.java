package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataTemporalCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.TemporalCondition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.TemporalOperator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.TemporalScope;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AlertVerificationOutcomeValidator {

    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final String EVENT_INTERPRETER = "EVENT_INTERPRETER";
    private static final String EVENT = "EVENT";
    private static final String STATELESS_EVENT_MATCH = "STATELESS_EVENT_MATCH";
    private static final String INPUT_MODEL = "ServiceDataV2";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final String DEFAULT_TEMPORAL_ZONE = "Europe/Rome";
    private static final String LOCAL_TIME_BETWEEN = "LOCAL_TIME_BETWEEN";
    private static final String ACTIVATION_WINDOW_REJECTION_REASON =
            "Activation time windows are not supported in the current Alert Verify MVP. "
                    + "Only stateless temporal predicates evaluated on ServiceData event timestamps are supported.";
    private static final DateTimeFormatter HOUR_MINUTE_SECOND = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String DEFAULT_REJECTED_REASON = "The alert cannot be verified with the current MVP constraints.";
    private static final Set<String> LEGACY_CONDITION_TYPES = Set.of(
            "JOURNEY_CANCELLED",
            "JOURNEY_DELAYED",
            "PLATFORM_EVENT",
            "GENERIC_SERVICE_DATA_EVENT");
    @Inject
    TemporalConfiguration temporalConfiguration;

    public AlertVerificationOutcome validate(AlertVerificationOutcome outcome) {
        return validate(outcome, null);
    }

    public AlertVerificationOutcome validate(AlertVerificationOutcome outcome, String originalPrompt) {
        String defaultTemporalZone = defaultTemporalZone();
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] temporal default zone loaded=" + defaultTemporalZone);
        if (outcome == null) {
            return fail(null, DEFAULT_REJECTED_REASON);
        }
        if (outcome.decision() == AlertVerificationDecision.ERROR) {
            System.out.println("[IIA][ALERT_VERIFY][VALIDATOR] Validation passed");
            return outcome;
        }
        if (outcome.decision() == AlertVerificationDecision.REJECTED) {
            String defensiveReason = unsupportedPromptTemporalReason(originalPrompt);
            if (defensiveReason != null) {
                logTemporalRejection(defensiveReason);
                return rejected(outcome, defensiveReason);
            }
            System.out.println("[IIA][ALERT_VERIFY][VALIDATOR] Validation passed");
            return ensureRejectedReason(outcome);
        }
        if (outcome.decision() != AlertVerificationDecision.VERIFIED) {
            return fail(outcome, DEFAULT_REJECTED_REASON);
        }

        ValidationContext context = new ValidationContext(outcome, defaultTemporalZone, originalPrompt);
        validateVerifiedOutcome(context);
        if (context.failureReason != null) {
            return fail(outcome, context.failureReason);
        }

        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR] Validation passed");
        return rebuild(outcome, context.confidence, context.normalizedTargetTypes, context.warnings,
                context.technicalSpecification, context.agentBlueprintPreview);
    }

    private void validateVerifiedOutcome(ValidationContext context) {
        AlertVerificationOutcome outcome = context.outcome;
        logVerifiedChecks(outcome);
        String unsupportedPromptReason = unsupportedPromptTemporalReason(context.originalPrompt);
        if (unsupportedPromptReason != null) {
            rejectTemporalRequest(context, unsupportedPromptReason);
            return;
        }
        boolean temporalIntent = hasTemporalIntent(context);

        List<String> requiredSources = normalizedList(outcome.requiredSources());
        if (requiredSources.stream().anyMatch(source -> !SERVICE_DATA.equals(source))) {
            if (temporalIntent) {
                rejectTemporalRequest(context,
                        "The temporal request uses a source other than SERVICE_DATA and cannot be evaluated on one ServiceData event.");
            } else {
                context.fail("Verified alert uses a data source outside the current MVP contract.");
            }
            return;
        }
        if (containsForbiddenSource(requiredSources)) {
            context.fail("Verified alert uses a forbidden data source.");
            return;
        }

        if (EVENT_INTERPRETER.equals(outcome.interpreterType())) {
            // Expected.
        } else if ("SCHEDULED_INTERPRETER".equals(outcome.interpreterType())) {
            rejectTemporalRequest(context,
                    "The request requires scheduled evaluation; SCHEDULED_INTERPRETER is not permitted for stateless temporal predicates.");
            return;
        } else {
            context.fail("Verified alert uses an unsupported interpreter type.");
            return;
        }

        if (!EVENT.equals(outcome.triggerType())) {
            if (temporalIntent) {
                rejectTemporalRequest(context,
                        "The request requires a scheduled trigger; temporal predicates must use the received EVENT payload.");
            } else {
                context.fail("Verified alert uses an unsupported trigger type.");
            }
            return;
        }
        if (!STATELESS_EVENT_MATCH.equals(outcome.evaluationMode())) {
            context.fail("Verified alert uses an unsupported evaluation mode.");
            return;
        }
        if (outcome.inputModel() != null && !INPUT_MODEL.equals(outcome.inputModel())) {
            context.fail("Verified alert uses an unsupported input model.");
            return;
        }
        if (outcome.outputModel() != null && !OUTPUT_MODEL.equals(outcome.outputModel())) {
            context.fail("Verified alert uses an unsupported output model.");
            return;
        }

        if (outcome.confidence() == null) {
            context.confidence = 0.50;
            context.warn("Confidence was missing. Default value 0.50 was applied.");
        } else if (outcome.confidence() < 0.0 || outcome.confidence() > 1.0) {
            context.fail("Verified alert confidence is outside the valid range 0..1.");
            return;
        } else {
            context.confidence = outcome.confidence();
        }

        if (context.technicalSpecification == null) {
            context.fail("Verified alert is missing technicalSpecification.");
            return;
        }
        if (context.technicalSpecification.isEmpty()) {
            context.fail("Verified alert has empty technicalSpecification.");
            return;
        }
        if (context.agentBlueprintPreview == null) {
            context.fail("Verified alert is missing agentBlueprintPreview.");
            return;
        }
        if (context.agentBlueprintPreview.isEmpty()) {
            context.fail("Verified alert has empty agentBlueprintPreview.");
            return;
        }
        if (!SERVICE_DATA.equals(stringValue(context.technicalSpecification.get("source")))
                || !onlyServiceDataSources(context.agentBlueprintPreview.get("requiredSources"))) {
            rejectTemporalRequest(context,
                    "The temporal request must use only SERVICE_DATA artifacts and cannot use another source.");
            return;
        }

        Object stateRequirements = context.agentBlueprintPreview.get("stateRequirements");
        if (stateRequirements instanceof Map<?, ?> stateRequirementsMap) {
            Object requiresState = stateRequirementsMap.get("requiresState");
            if (Boolean.TRUE.equals(requiresState)) {
                context.fail("Verified alert requires state, which is not supported by the current MVP.");
                return;
            }
        } else {
            context.warn("agentBlueprintPreview.stateRequirements.requiresState is missing.");
        }

        if (containsExternalLookupOrTools(context.technicalSpecification)
                || containsExternalLookupOrTools(context.agentBlueprintPreview)) {
            rejectTemporalRequest(context,
                    "The request requires tools, APIs or queries and is not evaluable on a single ServiceData event.");
            return;
        }
        if (containsSuspiciousContent(context.technicalSpecification) || containsSuspiciousContent(context.agentBlueprintPreview)) {
            context.fail("Verified alert contains forbidden technical keys or values.");
            return;
        }
        if (containsForbiddenTemporalOrchestration(context.technicalSpecification)
                || containsForbiddenTemporalOrchestration(context.agentBlueprintPreview)) {
            rejectTemporalRequest(context,
                    "The request uses activation policy or scheduler; temporal windows must be matched on one ServiceData event.");
            return;
        }

        validateTechnicalCondition(context, context.technicalSpecification);
        if (context.failureReason != null) {
            return;
        }
        int technicalTemporalConditionCount = context.temporalConditions.size();
        if (technicalTemporalConditionCount > 0 || containsPotentialTemporalOperator(context.agentBlueprintPreview)) {
            validateBlueprintTemporalCondition(context);
            if (context.failureReason != null) {
                return;
            }
            if (technicalTemporalConditionCount == 0) {
                context.fail("Verified alert agentBlueprintPreview contains a temporal condition not represented in technicalSpecification.");
                return;
            }
        }

        validateRequirementCoverage(context, outcome.requirementCoverage());
        if (context.failureReason != null) {
            return;
        }

        context.normalizedTargetTypes = normalizeTargetTypes(outcome.interpretedTargetTypes(), requiredSources, outcome.triggerType(), outcome.evaluationMode());
        if (context.normalizedTargetTypes == null) {
            context.fail("Verified alert uses unsupported target types for the current MVP.");
        }
    }

    private void logVerifiedChecks(AlertVerificationOutcome outcome) {
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] requiredSources=" + outcome.requiredSources());
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] interpreterType=" + outcome.interpreterType());
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] triggerType=" + outcome.triggerType());
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] evaluationMode=" + outcome.evaluationMode());
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] inputModel=" + outcome.inputModel());
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] outputModel=" + outcome.outputModel());
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] targetTypes=" + outcome.interpretedTargetTypes());
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] technicalSpecification present=" + (outcome.technicalSpecification() != null));
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] technicalSpecification null=" + (outcome.technicalSpecification() == null));
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] technicalSpecification empty=" + (outcome.technicalSpecification() != null && outcome.technicalSpecification().isEmpty()));
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] agentBlueprintPreview present=" + (outcome.agentBlueprintPreview() != null));
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] agentBlueprintPreview null=" + (outcome.agentBlueprintPreview() == null));
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CHECK] agentBlueprintPreview empty=" + (outcome.agentBlueprintPreview() != null && outcome.agentBlueprintPreview().isEmpty()));
    }

    private void validateTechnicalCondition(ValidationContext context, Map<String, Object> technicalSpecification) {
        Object condition = technicalSpecification.get("condition");
        if (!(condition instanceof Map<?, ?> conditionMap)) {
            context.fail("Verified alert technicalSpecification.condition is missing or invalid.");
            return;
        }

        Object type = conditionMap.get("type");
        if (!"SERVICE_DATA_FIELD_MATCH".equals(type) && !LEGACY_CONDITION_TYPES.contains(String.valueOf(type))) {
            context.fail("Verified alert uses an unsupported technicalSpecification.condition.type.");
            return;
        }

        if (LEGACY_CONDITION_TYPES.contains(String.valueOf(type))) {
            return;
        }

        validateConditionNode(context, conditionMap, "technicalSpecification.condition");
    }

    private void validateBlueprintTemporalCondition(ValidationContext context) {
        Object parameters = context.agentBlueprintPreview.get("parameters");
        if (!(parameters instanceof Map<?, ?> parametersMap)
                || !(parametersMap.get("condition") instanceof Map<?, ?> conditionMap)) {
            context.fail("Verified temporal alert is missing agentBlueprintPreview.parameters.condition.");
            return;
        }
        int temporalConditionCount = context.temporalConditions.size();
        validateConditionNode(context, conditionMap, "agentBlueprintPreview.parameters.condition");
        if (context.failureReason == null && context.temporalConditions.size() == temporalConditionCount) {
            context.fail("Verified temporal alert agentBlueprintPreview.parameters.condition has no temporal condition.");
        }
    }

    private void validateConditionNode(ValidationContext context, Map<?, ?> conditionNode, String path) {
        Object all = conditionNode.get("all");
        Object any = conditionNode.get("any");
        Object anyElement = conditionNode.get("anyElement");
        boolean hasComposite = false;

        if (all != null) {
            hasComposite = true;
            validateConditionArray(context, all, path + ".all");
            if (context.failureReason != null) {
                return;
            }
        }
        if (any != null) {
            hasComposite = true;
            validateConditionArray(context, any, path + ".any");
            if (context.failureReason != null) {
                return;
            }
        }
        if (anyElement != null) {
            hasComposite = true;
            validateAnyElement(context, anyElement, path + ".anyElement");
            if (context.failureReason != null) {
                return;
            }
        }

        if (conditionNode.containsKey("field") || conditionNode.containsKey("operator")) {
            validateConditionLeaf(context, conditionNode, path);
            return;
        }

        if (!hasComposite) {
            context.fail("Verified alert condition must contain all/any/anyElement or a field/operator leaf.");
        }
    }

    private void validateConditionArray(ValidationContext context, Object nodes, String path) {
        if (!(nodes instanceof List<?> list) || list.isEmpty()) {
            context.fail("Verified alert condition " + path + " must be a non-empty array.");
            return;
        }
        for (int index = 0; index < list.size(); index++) {
            Object child = list.get(index);
            if (!(child instanceof Map<?, ?> childMap)) {
                context.fail("Verified alert condition " + path + "[" + index + "] must be an object.");
                return;
            }
            validateConditionNode(context, childMap, path + "[" + index + "]");
            if (context.failureReason != null) {
                return;
            }
        }
    }

    private void validateConditionLeaf(ValidationContext context, Map<?, ?> leaf, String path) {
        String field = stringValue(leaf.get("field"));
        String operator = stringValue(leaf.get("operator"));
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CATALOG] validating field=" + field + " operator=" + operator);

        if (field == null || field.isBlank()) {
            if (LOCAL_TIME_BETWEEN.equals(operator) || isPotentialTemporalOperator(operator)) {
                rejectTemporalCondition(context, field, operator,
                        "field is missing or is not an allowed temporal scope.");
            } else {
                rejectCatalogField(context, field, "field is missing.");
            }
            return;
        }
        if (ServiceDataCapabilityCatalog.isSuspiciousFieldName(field)) {
            rejectCatalogField(context, field, "field contains suspicious content.");
            return;
        }

        ServiceDataCapabilityCatalog.FieldCapability capability = ServiceDataCapabilityCatalog.findField(field)
                .orElse(null);
        if (capability == null) {
            rejectCatalogField(context, field, "field is not allowed by the ServiceData capability catalog.");
            return;
        }
        if (!capability.supportsOperator(operator)) {
            if (LOCAL_TIME_BETWEEN.equals(operator) || isPotentialTemporalOperator(operator)) {
                rejectTemporalCondition(context, field, operator, "operator is not supported on this field.");
                return;
            }
            rejectCatalogField(context, field, "operator is not allowed for this field.");
            return;
        }

        context.conditionFields.add(field);
        if (ServiceDataTemporalCapabilityCatalog.isTemporalOperator(operator)) {
            validateTemporalCondition(context, leaf, field, operator, false);
            return;
        }
        validateConditionValue(context, capability, operator, leaf, field);
    }

    private void validateAnyElement(ValidationContext context, Object anyElement, String path) {
        if (!(anyElement instanceof Map<?, ?> anyElementMap)) {
            rejectArrayCondition(context, null, "anyElement must be an object.");
            return;
        }
        String arrayPath = stringValue(anyElementMap.get("path"));
        System.out.println("[IIA][ALERT_VERIFY][ARRAY] anyElement path found=" + arrayPath);
        if (!isAllowedAnyElementPath(arrayPath)) {
            rejectArrayCondition(context, arrayPath, "path is not allowed.");
            return;
        }
        Object conditions = anyElementMap.get("conditions");
        if (!(conditions instanceof Map<?, ?> conditionMap)) {
            rejectArrayCondition(context, arrayPath, "conditions must be an object.");
            return;
        }
        int leafCount = countConditionLeaves(conditionMap);
        System.out.println("[IIA][ALERT_VERIFY][ARRAY] path=" + arrayPath + " internalConditions=" + leafCount);
        if (leafCount == 0) {
            rejectArrayCondition(context, arrayPath, "conditions must contain at least one field/operator leaf.");
            return;
        }
        validateAnyElementConditionNode(context, conditionMap, path + ".conditions", arrayPath);
        if (context.failureReason == null
                && containsPotentialTemporalOperator(conditionMap)
                && arrayPath.endsWith("nextCalls[]")
                && !hasCorrelatedStopAndTime(conditionMap)) {
            rejectArrayCondition(context, arrayPath,
                    "temporal nextCalls constraints must correlate stopPoint.nameLong and departureTime/arrivalTime in the same all group.");
        }
    }

    private void validateAnyElementConditionNode(
            ValidationContext context,
            Map<?, ?> conditionNode,
            String path,
            String arrayPath) {
        if (conditionNode.containsKey("anyElement")) {
            rejectArrayCondition(context, arrayPath, "nested anyElement is not supported.");
            return;
        }
        boolean hasComposite = false;
        for (String group : List.of("all", "any")) {
            Object nodes = conditionNode.get(group);
            if (nodes == null) {
                continue;
            }
            hasComposite = true;
            if (!(nodes instanceof List<?> list) || list.isEmpty()) {
                rejectArrayCondition(context, arrayPath, path + "." + group + " must be a non-empty array.");
                return;
            }
            for (int index = 0; index < list.size(); index++) {
                Object child = list.get(index);
                if (!(child instanceof Map<?, ?> childMap)) {
                    rejectArrayCondition(context, arrayPath, path + "." + group + "[" + index + "] must be an object.");
                    return;
                }
                validateAnyElementConditionNode(context, childMap, path + "." + group + "[" + index + "]", arrayPath);
                if (context.failureReason != null) {
                    return;
                }
            }
        }
        if (conditionNode.containsKey("field") || conditionNode.containsKey("operator")) {
            validateAnyElementLeaf(context, conditionNode, arrayPath);
            return;
        }
        if (!hasComposite) {
            rejectArrayCondition(context, arrayPath, "conditions must contain all/any or a field/operator leaf.");
        }
    }

    private void validateAnyElementLeaf(ValidationContext context, Map<?, ?> leaf, String arrayPath) {
        String relativeField = stringValue(leaf.get("field"));
        String operator = stringValue(leaf.get("operator"));
        System.out.println("[IIA][ALERT_VERIFY][ARRAY] validating relative field=" + relativeField
                + " operator=" + operator + " path=" + arrayPath);
        String absoluteField = arrayPath + "." + relativeField;
        ServiceDataCapabilityCatalog.FieldCapability capability = ServiceDataCapabilityCatalog.findField(absoluteField)
                .orElse(null);
        if (capability == null) {
            rejectArrayCondition(context, arrayPath, "relative field is not allowed: " + relativeField);
            return;
        }
        if (!capability.supportsOperator(operator)) {
            rejectArrayCondition(context, arrayPath,
                    "operator is not allowed for relative field " + relativeField + ": " + operator);
            return;
        }
        context.conditionFields.add(absoluteField);
        if (ServiceDataTemporalCapabilityCatalog.isTemporalOperator(operator)) {
            validateTemporalCondition(context, leaf, absoluteField, operator, true);
            return;
        }
        validateConditionValue(context, capability, operator, leaf, absoluteField);
    }

    private boolean isAllowedAnyElementPath(String arrayPath) {
        if (arrayPath == null || !arrayPath.endsWith("[]") || ServiceDataCapabilityCatalog.isSuspiciousFieldName(arrayPath)) {
            return false;
        }
        String prefix = arrayPath + ".";
        return ServiceDataCapabilityCatalog.fields().stream()
                .map(ServiceDataCapabilityCatalog.FieldCapability::field)
                .anyMatch(field -> field.startsWith(prefix));
    }

    private int countConditionLeaves(Object node) {
        if (node instanceof Map<?, ?> map) {
            int current = map.containsKey("field") && map.containsKey("operator") ? 1 : 0;
            return current + map.values().stream().mapToInt(this::countConditionLeaves).sum();
        }
        if (node instanceof Collection<?> collection) {
            return collection.stream().mapToInt(this::countConditionLeaves).sum();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private void validateTemporalCondition(
            ValidationContext context,
            Map<?, ?> leaf,
            String field,
            String operator,
            boolean withinAnyElement) {
        TemporalScope scope = TemporalScope.fromField(field).orElse(null);
        if (scope == null || !ServiceDataTemporalCapabilityCatalog.isAllowedTemporalField(field)) {
            rejectTemporalCondition(context, field, operator, "field is not an allowed temporal scope.");
            return;
        }
        if (!ServiceDataTemporalCapabilityCatalog.isAllowedOperator(field, operator)) {
            rejectTemporalCondition(context, field, operator, "operator is not supported on this temporal field.");
            return;
        }
        if (!withinAnyElement && field.contains("[]")) {
            rejectTemporalCondition(context, field, operator,
                    "array temporal fields must be represented inside a correlated anyElement node.");
            return;
        }
        Object value = leaf.get("value");
        if (!(value instanceof Map<?, ?> rawValue)) {
            rejectTemporalCondition(context, field, operator, "value must be an object.");
            return;
        }
        Map<String, Object> temporalValue = (Map<String, Object>) rawValue;
        String timezone = stringValue(temporalValue.get("timezone"));
        if (timezone == null) {
            timezone = context.defaultTemporalZone;
            temporalValue.put("timezone", timezone);
        }
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            rejectTemporalCondition(context, field, operator, "timezone is not a valid zone id.");
            return;
        }

        if (LOCAL_TIME_BETWEEN.equals(operator)) {
            validateLocalTimeBetween(context, temporalValue, field, scope, timezone);
            return;
        }
        validateLocalDayOfWeek(context, temporalValue, field, operator, scope, timezone);
    }

    private void validateLocalTimeBetween(
            ValidationContext context,
            Map<String, Object> temporalValue,
            String field,
            TemporalScope scope,
            String timezone) {
        String startText = stringValue(temporalValue.get("start"));
        String endText = stringValue(temporalValue.get("end"));
        String normalizedStart = normalizeLocalTime(startText);
        String normalizedEnd = normalizeLocalTime(endText);
        if (normalizedStart == null || normalizedEnd == null) {
            rejectTemporalCondition(context, field, LOCAL_TIME_BETWEEN,
                    "start and end must be valid times in H, HH, H:mm, HH:mm or HH:mm:ss format.");
            return;
        }
        logAndApplyTimeNormalization(temporalValue, "start", startText, normalizedStart, field);
        logAndApplyTimeNormalization(temporalValue, "end", endText, normalizedEnd, field);
        LocalTime start = LocalTime.parse(normalizedStart, HOUR_MINUTE_SECOND);
        LocalTime end = LocalTime.parse(normalizedEnd, HOUR_MINUTE_SECOND);
        TemporalCondition condition = new TemporalCondition(
                scope,
                field,
                TemporalOperator.LOCAL_TIME_BETWEEN,
                start,
                end,
                List.of(),
                timezone,
                null,
                null);
        context.temporalConditions.add(condition);
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] validated field=" + condition.field()
                + " operator=" + condition.operator()
                + " start=" + normalizedStart
                + " end=" + normalizedEnd
                + " timezone=" + condition.timezone());
    }

    private void validateLocalDayOfWeek(
            ValidationContext context,
            Map<String, Object> temporalValue,
            String field,
            String operator,
            TemporalScope scope,
            String timezone) {
        Object rawDays = temporalValue.get("days");
        if (!(rawDays instanceof List<?> dayValues) || dayValues.isEmpty()) {
            rejectTemporalCondition(context, field, operator, "value.days must be a non-empty array.");
            return;
        }
        List<DayOfWeek> days = new ArrayList<>();
        List<String> normalizedDays = new ArrayList<>();
        for (Object rawDay : dayValues) {
            String dayText = stringValue(rawDay);
            if (dayText == null) {
                rejectTemporalCondition(context, field, operator, "days must contain valid english day names.");
                return;
            }
            try {
                DayOfWeek day = DayOfWeek.valueOf(dayText.toUpperCase(Locale.ROOT));
                days.add(day);
                normalizedDays.add(day.name());
            } catch (IllegalArgumentException exception) {
                rejectTemporalCondition(context, field, operator, "day is not valid: " + dayText);
                return;
            }
        }
        temporalValue.put("days", normalizedDays);
        TemporalCondition condition = new TemporalCondition(
                scope,
                field,
                TemporalOperator.valueOf(operator),
                null,
                null,
                List.copyOf(days),
                timezone,
                null,
                null);
        context.temporalConditions.add(condition);
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] validated field=" + condition.field()
                + " operator=" + condition.operator()
                + " days=" + normalizedDays
                + " timezone=" + condition.timezone());
    }

    private String normalizeLocalTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            if (value.matches("\\d{1,2}")) {
                return LocalTime.of(Integer.parseInt(value), 0).format(HOUR_MINUTE_SECOND);
            }
            if (value.matches("\\d{1,2}:\\d{2}")) {
                String[] components = value.split(":");
                return LocalTime.of(Integer.parseInt(components[0]), Integer.parseInt(components[1]))
                        .format(HOUR_MINUTE_SECOND);
            }
            if (value.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return LocalTime.parse(value, HOUR_MINUTE_SECOND).format(HOUR_MINUTE_SECOND);
            }
        } catch (DateTimeException | NumberFormatException exception) {
            return null;
        }
        return null;
    }

    private void logAndApplyTimeNormalization(
            Map<String, Object> temporalValue,
            String boundary,
            String original,
            String normalized,
            String field) {
        if (!normalized.equals(original)) {
            temporalValue.put(boundary, normalized);
            System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] normalized field=" + field
                    + " " + boundary + "=" + original + " -> " + normalized);
        }
    }

    private void validateRequirementCoverage(ValidationContext context, Map<String, Object> requirementCoverage) {
        if (requirementCoverage == null || requirementCoverage.isEmpty()) {
            context.fail("Verified alert is missing requirementCoverage.");
            return;
        }

        Object allMapped = requirementCoverage.get("allRequiredRequirementsMapped");
        System.out.println("[IIA][ALERT_VERIFY][COVERAGE] allRequiredRequirementsMapped=" + allMapped);
        if (!Boolean.TRUE.equals(allMapped)) {
            context.fail("Verified alert requirementCoverage indicates not all required requirements are mapped.");
            return;
        }

        Object requirements = requirementCoverage.get("requirements");
        if (!(requirements instanceof List<?> requirementList) || requirementList.isEmpty()) {
            context.fail("Verified alert requirementCoverage.requirements must be a non-empty array.");
            return;
        }

        for (Object item : requirementList) {
            if (!(item instanceof Map<?, ?> requirement)) {
                context.fail("Verified alert requirementCoverage contains an invalid requirement.");
                return;
            }

            String text = stringValue(requirement.get("text"));
            boolean required = !Boolean.FALSE.equals(requirement.get("required"));
            boolean mappable = Boolean.TRUE.equals(requirement.get("mappable"));
            List<String> mappedBy = stringList(requirement.get("mappedBy"));
            System.out.println("[IIA][ALERT_VERIFY][COVERAGE] requirement=" + text
                    + " required=" + required
                    + " mappable=" + mappable
                    + " mappedBy=" + mappedBy);

            if (!required) {
                continue;
            }
            if (!mappable) {
                System.out.println("[IIA][ALERT_VERIFY][COVERAGE] rejected unmapped requirement=" + text);
                context.fail("Verified alert contains a required user constraint that is not mappable to the ServiceData capability catalog.");
                return;
            }
            if (mappedBy.isEmpty()) {
                System.out.println("[IIA][ALERT_VERIFY][COVERAGE] rejected unmapped requirement=" + text);
                context.fail("Verified alert requirementCoverage has a mappable required requirement without mappedBy fields.");
                return;
            }
            for (String field : mappedBy) {
                if (!ServiceDataCapabilityCatalog.isAllowedMappedBy(field)) {
                    System.out.println("[IIA][ALERT_VERIFY][COVERAGE] rejected unmapped requirement=" + text);
                    context.fail("Verified alert requirementCoverage mappedBy contains a field outside the ServiceData capability catalog.");
                    return;
                }
            }
            boolean covered = mappedBy.stream().anyMatch(context.conditionFields::contains);
            if (!covered) {
                System.out.println("[IIA][ALERT_VERIFY][COVERAGE] rejected uncovered requirement=" + text);
                context.fail("Verified alert technicalSpecification does not cover every mapped required requirement.");
                return;
            }
        }
    }

    private void validateConditionValue(
            ValidationContext context,
            ServiceDataCapabilityCatalog.FieldCapability capability,
            String operator,
            Map<?, ?> leaf,
            String field) {
        Object value = leaf.get("value");
        Object values = leaf.get("values");

        if (List.of("EXISTS", "NOT_NULL", "NOT_EMPTY").contains(operator)) {
            return;
        }
        if (List.of("IN", "CONTAINS_ANY").contains(operator)) {
            if (!(values instanceof List<?> valueList) || valueList.isEmpty()) {
                rejectCatalogField(context, field, "operator " + operator + " requires a non-empty values array.");
                return;
            }
            validateTypedValues(context, capability, operator, valueList, field);
            return;
        }

        if (value == null) {
            rejectCatalogField(context, field, "operator " + operator + " requires value.");
            return;
        }
        validateTypedValues(context, capability, operator, List.of(value), field);
    }

    private void validateTypedValues(
            ValidationContext context,
            ServiceDataCapabilityCatalog.FieldCapability capability,
            String operator,
            List<?> values,
            String field) {
        switch (capability.type()) {
            case NUMBER -> {
                if (!values.stream().allMatch(Number.class::isInstance)) {
                    rejectCatalogField(context, field, "numeric operator " + operator + " requires numeric value.");
                }
            }
            case BOOLEAN -> {
                if (!values.stream().allMatch(Boolean.class::isInstance)) {
                    rejectCatalogField(context, field, "boolean field requires boolean value.");
                }
            }
            case ENUM, ENUM_ARRAY -> {
                List<String> normalizedValues = capability.normalizeEnumValues(values);
                if (!capability.enumValues().containsAll(normalizedValues)) {
                    rejectCatalogField(context, field, "enum value is not allowed for this field.");
                }
            }
            case ARRAY -> {
                if (operator.startsWith("SIZE_") && !values.stream().allMatch(Number.class::isInstance)) {
                    rejectCatalogField(context, field, "array size operator requires numeric value.");
                }
            }
            case STRING, OBJECT, TEMPORAL -> {
                // No extra type checks are needed beyond operator allow-list for the current MVP.
            }
        }
    }

    private boolean isPotentialTemporalOperator(String operator) {
        if (operator == null) {
            return false;
        }
        String normalized = operator.toUpperCase(Locale.ROOT);
        return ServiceDataTemporalCapabilityCatalog.isTemporalOperator(normalized)
                || normalized.contains("TIME")
                || normalized.contains("DAY_OF_WEEK");
    }

    private boolean containsPotentialTemporalOperator(Object value) {
        if (value instanceof Map<?, ?> map) {
            if (isPotentialTemporalOperator(stringValue(map.get("operator")))) {
                return true;
            }
            return map.values().stream().anyMatch(this::containsPotentialTemporalOperator);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().anyMatch(this::containsPotentialTemporalOperator);
        }
        return false;
    }

    private boolean hasTemporalIntent(ValidationContext context) {
        return unsupportedPromptTemporalReason(context.originalPrompt) != null
                || containsPotentialTemporalOperator(context.technicalSpecification)
                || containsPotentialTemporalOperator(context.agentBlueprintPreview)
                || containsForbiddenTemporalOrchestration(context.technicalSpecification)
                || containsForbiddenTemporalOrchestration(context.agentBlueprintPreview);
    }

    private String unsupportedPromptTemporalReason(String prompt) {
        String normalized = normalizeText(prompt);
        if (normalized == null) {
            return null;
        }
        if (containsAny(normalized, "attiva questo alert solo", "attiva l'alert solo",
                "solo attivo tra", "esegui questo alert solo", "abilita questo alert solo")) {
            return ACTIVATION_WINDOW_REJECTION_REASON;
        }
        if (containsAny(normalized, "ultimi 30 minuti", "ultimi minuti", "nell'ultima ora",
                "nelle ultime", "negli ultimi", "in the last", "last 30 minutes")) {
            return "The request requires historical event evaluation, which is not evaluable on a single ServiceData event.";
        }
        if (containsAny(normalized, "nessuna corsa", "nessun treno", "nessun autobus",
                "non parte nessuna", "non sono arrivati", "non sono partiti", "assenza eventi", "assenza di eventi")) {
            return "The request requires absence-of-events evaluation, which is not evaluable on a single ServiceData event.";
        }
        if (containsAny(normalized, "probabilmente", "probabile", "predici", "prevedi", "probably", "predict")) {
            return "The request requires prediction, which is not evaluable on a single ServiceData event.";
        }
        if (containsAny(normalized, "domani", "dopodomani", "oggi", "tomorrow", "today", "day after tomorrow")) {
            return "The request requires a scheduled future or date-relative lookup, which is not evaluable on a single ServiceData event.";
        }
        if (containsAny(normalized, "fascia oraria", "time window")
                && !containsAny(normalized, "parte", "partenza", "arriva", "arrivo", "depart", "arrival")) {
            return "The request specifies a time window without a supported event field evaluable on a single ServiceData event.";
        }
        return null;
    }

    private boolean onlyServiceDataSources(Object value) {
        List<String> requiredSources = stringList(value).stream()
                .map(item -> item.toUpperCase(Locale.ROOT))
                .toList();
        return !requiredSources.isEmpty() && requiredSources.stream().allMatch(SERVICE_DATA::equals);
    }

    private boolean containsExternalLookupOrTools(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = normalizeText(String.valueOf(entry.getKey()));
                if (key != null && (key.contains("allowedtools") || key.contains("allowed_tools")
                        || key.contains("tool") || key.equals("api") || key.contains("externalapi")
                        || key.contains("centralapi") || key.contains("databasequery")
                        || key.contains("apiquery") || key.equals("query") || key.equals("http"))) {
                    return true;
                }
                if (containsExternalLookupOrTools(entry.getValue())) {
                    return true;
                }
            }
            return false;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().anyMatch(this::containsExternalLookupOrTools);
        }
        String scalar = normalizeText(value == null ? null : String.valueOf(value));
        return scalar != null && containsAny(scalar, "external_http", "central_api", "database_query", "api_query");
    }

    private boolean hasCorrelatedStopAndTime(Object node) {
        if (!(node instanceof Map<?, ?> map)) {
            return false;
        }
        Object all = map.get("all");
        if (all instanceof List<?> children) {
            boolean stopPoint = children.stream().anyMatch(child -> hasRelativeLeaf(
                    child, "stopPoint.nameLong", Set.of("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED")));
            boolean time = children.stream().anyMatch(child -> hasRelativeLeaf(
                    child, "departureTime", Set.of(LOCAL_TIME_BETWEEN))
                    || hasRelativeLeaf(child, "arrivalTime", Set.of(LOCAL_TIME_BETWEEN)));
            if (stopPoint && time) {
                return true;
            }
        }
        return map.values().stream().anyMatch(this::hasCorrelatedStopAndTime);
    }

    private boolean hasRelativeLeaf(Object node, String field, Set<String> operators) {
        if (!(node instanceof Map<?, ?> map)) {
            return false;
        }
        return field.equals(stringValue(map.get("field")))
                && operators.contains(stringValue(map.get("operator")));
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private void rejectTemporalCondition(ValidationContext context, String field, String operator, String reason) {
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL][REJECT] field=" + field
                + " operator=" + operator + " reason=" + reason);
        context.fail("Verified alert temporal condition is not supported: " + reason);
    }

    private void rejectArrayCondition(ValidationContext context, String path, String reason) {
        System.out.println("[IIA][ALERT_VERIFY][ARRAY] rejected path=" + path + " reason=" + reason);
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL][REJECT] anyElement path=" + path + " reason=" + reason);
        context.fail("Verified alert anyElement condition is not supported: " + reason);
    }

    private void rejectTemporalRequest(ValidationContext context, String reason) {
        logTemporalRejection(reason);
        context.fail(reason);
    }

    private void logTemporalRejection(String reason) {
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL][REJECT] reason=" + reason);
    }

    private void rejectCatalogField(ValidationContext context, String field, String reason) {
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][CATALOG] rejected field=" + field + " reason=" + reason);
        context.fail("Verified alert condition is not supported by the ServiceData capability catalog: " + reason);
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private List<String> stringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::stringValue)
                    .filter(item -> item != null && !item.isBlank())
                    .toList();
        }
        return List.of();
    }

    private AlertVerificationOutcome ensureRejectedReason(AlertVerificationOutcome outcome) {
        if (outcome.rejectedReason() != null && !outcome.rejectedReason().isBlank()
                && outcome.interpreterType() == null
                && outcome.inputModel() == null
                && outcome.outputModel() == null
                && outcome.technicalSpecification() == null
                && outcome.agentBlueprintPreview() == null) {
            return outcome;
        }
        String reason = outcome.rejectedReason() == null || outcome.rejectedReason().isBlank()
                ? DEFAULT_REJECTED_REASON
                : outcome.rejectedReason();
        return rejected(outcome, reason);
    }

    private AlertVerificationOutcome fail(AlertVerificationOutcome outcome, String reason) {
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR] Validation failed: " + reason);
        return rejected(outcome, reason);
    }

    private AlertVerificationOutcome rejected(AlertVerificationOutcome outcome, String reason) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Alert verification rejected the result because it does not satisfy the current MVP constraints.",
                reason,
                0.0,
                outcome == null ? null : outcome.provider(),
                outcome == null ? null : outcome.model(),
                outcome == null ? "alert-verify-mvp-v1" : outcome.promptVersion(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                outcome == null ? null : outcome.requirementCoverage(),
                List.of(reason),
                safetyChecks(outcome));
    }

    private AlertVerificationOutcome rebuild(
            AlertVerificationOutcome outcome,
            Double confidence,
            List<String> targetTypes,
            List<String> warnings,
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview) {
        return new AlertVerificationOutcome(
                outcome.decision(),
                outcome.summary(),
                outcome.rejectedReason(),
                confidence,
                outcome.provider(),
                outcome.model(),
                outcome.promptVersion(),
                normalizedList(outcome.requiredSources()),
                outcome.interpreterType(),
                outcome.inputModel(),
                outcome.outputModel(),
                outcome.triggerType(),
                outcome.evaluationMode(),
                safeList(outcome.interpretedEventNames()),
                targetTypes,
                technicalSpecification,
                agentBlueprintPreview,
                outcome.requirementCoverage(),
                List.copyOf(warnings),
                safeList(outcome.safetyChecks()));
    }

    private List<String> normalizeTargetTypes(
            List<String> targetTypes,
            List<String> requiredSources,
            String triggerType,
            String evaluationMode) {
        boolean canNormalizeServiceDataTarget = requiredSources.contains(SERVICE_DATA)
                && EVENT.equals(triggerType)
                && STATELESS_EVENT_MATCH.equals(evaluationMode);
        List<String> normalizedTargetTypes = new ArrayList<>();

        for (String targetType : safeList(targetTypes)) {
            if ("SERVICE_DATA_JOURNEY".equals(targetType) || "GENERIC".equals(targetType)) {
                normalizedTargetTypes.add(targetType);
            } else if (canNormalizeServiceDataTarget
                    && ("STOP_POINT".equals(targetType) || "JOURNEY".equals(targetType) || "JOURNEY_GROUP".equals(targetType))) {
                normalizedTargetTypes.add("SERVICE_DATA_JOURNEY");
            } else {
                return null;
            }
        }

        if (normalizedTargetTypes.isEmpty()) {
            normalizedTargetTypes.add("SERVICE_DATA_JOURNEY");
        }
        return normalizedTargetTypes.stream().distinct().toList();
    }

    private boolean containsForbiddenSource(List<String> requiredSources) {
        return requiredSources.stream().anyMatch(source -> List.of(
                "AUDIO",
                "VIDEO",
                "DEVICE",
                "DISPLAY",
                "BROADCAST",
                "CONTENT",
                "DB",
                "HTTP",
                "KAFKA").contains(source));
    }

    private boolean containsSuspiciousContent(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (containsSuspiciousContent(entry.getKey()) || containsSuspiciousContent(entry.getValue())) {
                    return true;
                }
            }
            return false;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().anyMatch(this::containsSuspiciousContent);
        }
        String lowered = String.valueOf(value).toLowerCase();
        return List.of("http", "jdbc", "sql", "kafka", "filesystem", "file:", "process", "runtime", "classloader")
                .stream()
                .anyMatch(lowered::contains);
    }

    private boolean containsForbiddenTemporalOrchestration(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT);
                if (key.contains("activationpolicy") || key.contains("activation_policy")
                        || key.contains("scheduler") || key.equals("schedule")) {
                    return true;
                }
                if (containsForbiddenTemporalOrchestration(entry.getValue())) {
                    return true;
                }
            }
            return false;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().anyMatch(this::containsForbiddenTemporalOrchestration);
        }
        return "DAILY_WINDOW".equalsIgnoreCase(String.valueOf(value))
                || "SCHEDULED_INTERPRETER".equalsIgnoreCase(String.valueOf(value));
    }

    private List<String> normalizedList(List<String> values) {
        return safeList(values).stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .toList();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private List<String> safetyChecks(AlertVerificationOutcome outcome) {
        if (outcome != null && outcome.safetyChecks() != null && !outcome.safetyChecks().isEmpty()) {
            return outcome.safetyChecks();
        }
        return List.of(
                "No executable code generated.",
                "No Agent Definition created.",
                "No Suggestion created.");
    }

    private String defaultTemporalZone() {
        if (temporalConfiguration == null
                || temporalConfiguration.defaultZone() == null
                || temporalConfiguration.defaultZone().isBlank()) {
            return DEFAULT_TEMPORAL_ZONE;
        }
        return temporalConfiguration.defaultZone();
    }

    private static class ValidationContext {
        private final AlertVerificationOutcome outcome;
        private final List<String> warnings;
        private final String defaultTemporalZone;
        private final String originalPrompt;
        private final Map<String, Object> technicalSpecification;
        private final Map<String, Object> agentBlueprintPreview;
        private String failureReason;
        private Double confidence;
        private List<String> normalizedTargetTypes;
        private final List<String> conditionFields;
        private final List<TemporalCondition> temporalConditions;

        private ValidationContext(AlertVerificationOutcome outcome, String defaultTemporalZone, String originalPrompt) {
            this.outcome = outcome;
            this.warnings = new ArrayList<>(outcome.warnings() == null ? List.of() : outcome.warnings());
            this.defaultTemporalZone = defaultTemporalZone;
            this.originalPrompt = originalPrompt;
            this.technicalSpecification = mutableMapStatic(outcome.technicalSpecification());
            this.agentBlueprintPreview = mutableMapStatic(outcome.agentBlueprintPreview());
            this.conditionFields = new ArrayList<>();
            this.temporalConditions = new ArrayList<>();
        }

        private void warn(String warning) {
            warnings.add(warning);
        }

        private void fail(String reason) {
            failureReason = reason;
        }

        private static Map<String, Object> mutableMapStatic(Map<String, Object> source) {
            if (source == null) {
                return null;
            }
            Map<String, Object> result = new LinkedHashMap<>();
            source.forEach((key, value) -> result.put(key, mutableValueStatic(value)));
            return result;
        }

        private static Object mutableValueStatic(Object value) {
            if (value instanceof Map<?, ?> map) {
                Map<String, Object> result = new LinkedHashMap<>();
                map.forEach((key, item) -> result.put(String.valueOf(key), mutableValueStatic(item)));
                return result;
            }
            if (value instanceof List<?> list) {
                List<Object> result = new ArrayList<>();
                list.forEach(item -> result.add(mutableValueStatic(item)));
                return result;
            }
            return value;
        }
    }
}
