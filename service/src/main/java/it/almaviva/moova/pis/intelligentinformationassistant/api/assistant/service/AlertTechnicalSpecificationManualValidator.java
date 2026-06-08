package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AlertTechnicalSpecificationManualValidator {

    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final String EVENT_INTERPRETER = "EVENT_INTERPRETER";
    private static final String SCHEDULED_INTERPRETER = "SCHEDULED_INTERPRETER";
    private static final List<String> EXECUTABLE_MARKERS = List.of(
            "function(",
            "select ",
            "insert ",
            "delete from",
            "<script",
            "runtime.getruntime");

    @Inject
    AlertVerificationOutcomeValidator eventValidator;

    @Inject
    ScheduledAlertVerificationOutcomeValidator scheduledValidator;

    public ValidationResult validate(Map<String, Object> technicalSpecification) {
        if (technicalSpecification == null || technicalSpecification.isEmpty()) {
            return ValidationResult.invalid("technicalSpecification must be a non-empty object.");
        }
        if (containsExecutableMarker(technicalSpecification)) {
            return ValidationResult.unsupported("technicalSpecification contains executable, script or SQL-like content.");
        }

        String interpreterType = stringValue(technicalSpecification.get("interpreterType"));
        AlertVerificationOutcome candidate = candidateOutcome(technicalSpecification, interpreterType);
        AlertVerificationOutcome validated = SCHEDULED_INTERPRETER.equals(interpreterType)
                ? scheduledValidator.validate(candidate)
                : eventValidator.validate(candidate);

        if (validated.decision() == AlertVerificationDecision.VERIFIED) {
            return ValidationResult.valid(interpreterType);
        }
        String reason = validated.rejectedReason() == null ? "Technical specification validation failed." : validated.rejectedReason();
        return isUnsupportedReason(reason)
                ? ValidationResult.unsupported(reason)
                : ValidationResult.invalid(reason);
    }

    private AlertVerificationOutcome candidateOutcome(
            Map<String, Object> technicalSpecification,
            String interpreterType) {
        String inputModel = stringValue(technicalSpecification.get("inputModel"));
        String outputModel = stringValue(technicalSpecification.get("outputModel"));
        String triggerType = stringValue(technicalSpecification.get("triggerType"));
        String evaluationMode = stringValue(technicalSpecification.get("evaluationMode"));
        Map<String, Object> blueprint = syntheticAgentBlueprintPreview(technicalSpecification, interpreterType);

        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "Manual technical specification candidate.",
                null,
                0.90,
                "MANUAL",
                "manual",
                "manual-technical-specification",
                List.of(SERVICE_DATA),
                interpreterType,
                inputModel,
                outputModel,
                triggerType,
                evaluationMode,
                List.of(),
                interpretedTargetTypes(interpreterType),
                technicalSpecification,
                blueprint,
                requirementCoverage(technicalSpecification, interpreterType),
                List.of(),
                List.of("Manual technical specification validation."));
    }

    private List<String> interpretedTargetTypes(String interpreterType) {
        if (SCHEDULED_INTERPRETER.equals(interpreterType)) {
            return List.of("SERVICE_DATA_JOURNEY_AGGREGATE");
        }
        return List.of("SERVICE_DATA_JOURNEY");
    }

    private Map<String, Object> syntheticAgentBlueprintPreview(Map<String, Object> technicalSpecification, String interpreterType) {
        if (SCHEDULED_INTERPRETER.equals(interpreterType)) {
            Map<String, Object> snapshotEvaluation = asMap(technicalSpecification.get("snapshotEvaluation"));
            return Map.of(
                    "schemaVersion", "iia.agent.blueprint/v1",
                    "agentName", "ScheduledServiceDataSnapshotAlertAgent",
                    "triggerType", "SCHEDULE",
                    "requiredSources", List.of(SERVICE_DATA),
                    "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                    "targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                    "stateRequirements", Map.of("requiresState", false),
                    "output", Map.of("type", "CANDIDATE_SUGGESTION"),
                    "parameters", Map.of(
                            "serviceDataQuery", valueOrEmptyMap(technicalSpecification.get("serviceDataQuery")),
                            "snapshotEvaluation", snapshotEvaluation == null ? Map.of() : snapshotEvaluation,
                            "outputPolicy", valueOrEmptyMap(technicalSpecification.get("outputPolicy"))));
        }
        return Map.of(
                "requiredSources", List.of(SERVICE_DATA),
                "stateRequirements", Map.of("requiresState", false),
                "parameters", Map.of("condition", valueOrEmptyMap(technicalSpecification.get("condition"))));
    }

    private Map<String, Object> requirementCoverage(Map<String, Object> technicalSpecification, String interpreterType) {
        List<String> mappedBy = new ArrayList<>(conditionFields(technicalSpecification, interpreterType));
        if (SCHEDULED_INTERPRETER.equals(interpreterType)) {
            mappedBy.add("snapshotEvaluation.mode");
            mappedBy.add("outputPolicy.emit");
        }
        if (mappedBy.isEmpty()) {
            mappedBy.add(SCHEDULED_INTERPRETER.equals(interpreterType) ? "snapshotEvaluation.mode" : "payload.ongroundServiceEvent.eventsType");
        }
        return Map.of(
                "allRequiredRequirementsMapped", true,
                "requirements", List.of(Map.of(
                        "text", "manual technical specification",
                        "required", true,
                        "mappable", true,
                        "mappedBy", mappedBy)));
    }

    private Set<String> conditionFields(Map<String, Object> technicalSpecification, String interpreterType) {
        Set<String> fields = new LinkedHashSet<>();
        Map<String, Object> rootCondition;
        if (SCHEDULED_INTERPRETER.equals(interpreterType)) {
            Map<String, Object> snapshotEvaluation = asMap(technicalSpecification.get("snapshotEvaluation"));
            rootCondition = snapshotEvaluation == null ? null : asMap(snapshotEvaluation.get("condition"));
        } else {
            rootCondition = asMap(technicalSpecification.get("condition"));
        }
        collectFields(rootCondition, fields);
        return fields;
    }

    private void collectFields(Map<String, Object> node, Set<String> fields) {
        if (node == null || node.isEmpty()) {
            return;
        }
        String field = stringValue(node.get("field"));
        if (field != null) {
            fields.add(field);
        }
        collectNodeList(node.get("all"), fields);
        collectNodeList(node.get("any"), fields);
        Map<String, Object> anyElement = asMap(node.get("anyElement"));
        if (anyElement != null) {
            collectFields(asMap(anyElement.get("conditions")), fields);
        }
    }

    private void collectNodeList(Object value, Set<String> fields) {
        if (!(value instanceof Collection<?> nodes)) {
            return;
        }
        for (Object node : nodes) {
            collectFields(asMap(node), fields);
        }
    }

    private boolean containsExecutableMarker(Object value) {
        if (value instanceof String text) {
            String normalized = text.toLowerCase(Locale.ROOT);
            return EXECUTABLE_MARKERS.stream().anyMatch(normalized::contains);
        }
        if (value instanceof Map<?, ?> map) {
            return map.values().stream().anyMatch(this::containsExecutableMarker);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().anyMatch(this::containsExecutableMarker);
        }
        return false;
    }

    private boolean isUnsupportedReason(String reason) {
        String normalized = reason.toLowerCase(Locale.ROOT);
        return normalized.contains("unsupported")
                || normalized.contains("outside the servicedata capability catalog")
                || normalized.contains("not in the scheduled servicedata catalog")
                || normalized.contains("source")
                || normalized.contains("interpreter")
                || normalized.contains("field")
                || normalized.contains("operator")
                || normalized.contains("enum")
                || normalized.contains("evaluation mode")
                || normalized.contains("input model")
                || normalized.contains("output model")
                || normalized.contains("accessmode");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private Object valueOrEmptyMap(Object value) {
        return value == null ? Map.of() : value;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? null : text;
    }

    public record ValidationResult(boolean valid, FailureKind failureKind, String reason, String interpreterType) {

        static ValidationResult valid(String interpreterType) {
            return new ValidationResult(true, null, null, interpreterType);
        }

        static ValidationResult invalid(String reason) {
            return new ValidationResult(false, FailureKind.INVALID_SPECIFICATION, reason, null);
        }

        static ValidationResult unsupported(String reason) {
            return new ValidationResult(false, FailureKind.UNSUPPORTED_SPECIFICATION, reason, null);
        }
    }

    public enum FailureKind {
        INVALID_SPECIFICATION,
        UNSUPPORTED_SPECIFICATION
    }
}
