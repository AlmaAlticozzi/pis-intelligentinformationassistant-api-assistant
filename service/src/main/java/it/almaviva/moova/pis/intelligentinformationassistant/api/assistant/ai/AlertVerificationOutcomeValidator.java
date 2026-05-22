package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private static final String DEFAULT_REJECTED_REASON = "The alert cannot be verified with the current MVP constraints.";
    private static final Set<String> LEGACY_CONDITION_TYPES = Set.of(
            "JOURNEY_CANCELLED",
            "JOURNEY_DELAYED",
            "PLATFORM_EVENT",
            "GENERIC_SERVICE_DATA_EVENT");

    public AlertVerificationOutcome validate(AlertVerificationOutcome outcome) {
        if (outcome == null) {
            return fail(null, DEFAULT_REJECTED_REASON);
        }
        if (outcome.decision() == AlertVerificationDecision.ERROR) {
            System.out.println("[IIA][ALERT_VERIFY][VALIDATOR] Validation passed");
            return outcome;
        }
        if (outcome.decision() == AlertVerificationDecision.REJECTED) {
            System.out.println("[IIA][ALERT_VERIFY][VALIDATOR] Validation passed");
            return ensureRejectedReason(outcome);
        }
        if (outcome.decision() != AlertVerificationDecision.VERIFIED) {
            return fail(outcome, DEFAULT_REJECTED_REASON);
        }

        ValidationContext context = new ValidationContext(outcome);
        validateVerifiedOutcome(context);
        if (context.failureReason != null) {
            return fail(outcome, context.failureReason);
        }

        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR] Validation passed");
        return rebuild(outcome, context.confidence, context.normalizedTargetTypes, context.warnings);
    }

    private void validateVerifiedOutcome(ValidationContext context) {
        AlertVerificationOutcome outcome = context.outcome;
        logVerifiedChecks(outcome);

        List<String> requiredSources = normalizedList(outcome.requiredSources());
        if (requiredSources.stream().anyMatch(source -> !SERVICE_DATA.equals(source))) {
            context.fail("Verified alert uses a data source outside the current MVP contract.");
            return;
        }
        if (containsForbiddenSource(requiredSources)) {
            context.fail("Verified alert uses a forbidden data source.");
            return;
        }

        if (EVENT_INTERPRETER.equals(outcome.interpreterType())) {
            // Expected.
        } else if ("SCHEDULED_INTERPRETER".equals(outcome.interpreterType())) {
            context.fail("SCHEDULED_INTERPRETER is not supported by the current MVP.");
            return;
        } else {
            context.fail("Verified alert uses an unsupported interpreter type.");
            return;
        }

        if (!EVENT.equals(outcome.triggerType())) {
            context.fail("Verified alert uses an unsupported trigger type.");
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

        if (outcome.technicalSpecification() == null) {
            context.fail("Verified alert is missing technicalSpecification.");
            return;
        }
        if (outcome.technicalSpecification().isEmpty()) {
            context.fail("Verified alert has empty technicalSpecification.");
            return;
        }
        if (outcome.agentBlueprintPreview() == null) {
            context.fail("Verified alert is missing agentBlueprintPreview.");
            return;
        }
        if (outcome.agentBlueprintPreview().isEmpty()) {
            context.fail("Verified alert has empty agentBlueprintPreview.");
            return;
        }

        Object stateRequirements = outcome.agentBlueprintPreview().get("stateRequirements");
        if (stateRequirements instanceof Map<?, ?> stateRequirementsMap) {
            Object requiresState = stateRequirementsMap.get("requiresState");
            if (Boolean.TRUE.equals(requiresState)) {
                context.fail("Verified alert requires state, which is not supported by the current MVP.");
                return;
            }
        } else {
            context.warn("agentBlueprintPreview.stateRequirements.requiresState is missing.");
        }

        if (containsSuspiciousContent(outcome.technicalSpecification()) || containsSuspiciousContent(outcome.agentBlueprintPreview())) {
            context.fail("Verified alert contains forbidden technical keys or values.");
            return;
        }

        validateTechnicalCondition(context, outcome.technicalSpecification());
        if (context.failureReason != null) {
            return;
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

    private void validateConditionNode(ValidationContext context, Map<?, ?> conditionNode, String path) {
        Object all = conditionNode.get("all");
        Object any = conditionNode.get("any");
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

        if (conditionNode.containsKey("field") || conditionNode.containsKey("operator")) {
            validateConditionLeaf(context, conditionNode, path);
            return;
        }

        if (!hasComposite) {
            context.fail("Verified alert condition must contain all/any or a field/operator leaf.");
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
            rejectCatalogField(context, field, "field is missing.");
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
            rejectCatalogField(context, field, "operator is not allowed for this field.");
            return;
        }

        context.conditionFields.add(field);
        validateConditionValue(context, capability, operator, leaf, field);
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
            case STRING, OBJECT -> {
                // No extra type checks are needed beyond operator allow-list for the current MVP.
            }
        }
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
            List<String> warnings) {
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
                outcome.technicalSpecification(),
                outcome.agentBlueprintPreview(),
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

    private static class ValidationContext {
        private final AlertVerificationOutcome outcome;
        private final List<String> warnings;
        private String failureReason;
        private Double confidence;
        private List<String> normalizedTargetTypes;
        private final List<String> conditionFields;

        private ValidationContext(AlertVerificationOutcome outcome) {
            this.outcome = outcome;
            this.warnings = new ArrayList<>(outcome.warnings() == null ? List.of() : outcome.warnings());
            this.conditionFields = new ArrayList<>();
        }

        private void warn(String warning) {
            warnings.add(warning);
        }

        private void fail(String reason) {
            failureReason = reason;
        }
    }
}
