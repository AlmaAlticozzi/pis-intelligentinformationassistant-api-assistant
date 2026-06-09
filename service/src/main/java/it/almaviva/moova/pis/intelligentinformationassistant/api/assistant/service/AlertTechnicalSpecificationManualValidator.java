package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class AlertTechnicalSpecificationManualValidator {

    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final String EVENT_INTERPRETER = "EVENT_INTERPRETER";
    private static final String SCHEDULED_INTERPRETER = "SCHEDULED_INTERPRETER";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final List<String> EXECUTABLE_MARKERS = List.of(
            "<script",
            "runtime.getruntime",
            "processbuilder",
            "select ",
            "insert ",
            "delete from",
            "drop table",
            "function(");

    public ValidationResult validate(Map<String, Object> technicalSpecification) {
        return validate(technicalSpecification, null);
    }

    public ValidationResult validate(
            Map<String, Object> technicalSpecification,
            ManualValidationContext context) {
        if (technicalSpecification == null || technicalSpecification.isEmpty()) {
            return ValidationResult.invalid("technicalSpecification must be a non-empty object.");
        }
        if (containsExecutableMarker(technicalSpecification)) {
            return ValidationResult.unsupported("technicalSpecification contains executable, script or SQL-like content.");
        }
        if (!SERVICE_DATA.equals(stringValue(technicalSpecification.get("source")))) {
            return ValidationResult.unsupported("technicalSpecification.source must be SERVICE_DATA.");
        }

        String interpreterType = resolveInterpreterType(technicalSpecification, context);
        if (EVENT_INTERPRETER.equals(interpreterType)) {
            return validateEventSpecification(technicalSpecification, interpreterType);
        }
        if (SCHEDULED_INTERPRETER.equals(interpreterType)) {
            return validateScheduledSpecification(technicalSpecification, interpreterType);
        }
        return ValidationResult.unsupported("technicalSpecification.interpreterType is missing or not supported.");
    }

    private String resolveInterpreterType(
            Map<String, Object> technicalSpecification,
            ManualValidationContext context) {
        String interpreterType = stringValue(technicalSpecification.get("interpreterType"));
        if (interpreterType != null) {
            return interpreterType;
        }

        if (context != null && stringValue(context.alertInterpreterType()) != null) {
            interpreterType = stringValue(context.alertInterpreterType());
            technicalSpecification.put("interpreterType", interpreterType);
            System.out.println("[IIA][ALERT_TECH_SPEC_PUT][INTERPRETER_FALLBACK_FROM_ALERT] alertId="
                    + context.alertId()
                    + " interpreterType=" + interpreterType);
            return interpreterType;
        }

        InferredInterpreter inferred = inferInterpreterType(technicalSpecification);
        if (inferred != null) {
            technicalSpecification.put("interpreterType", inferred.interpreterType());
            if (context != null) {
                System.out.println("[IIA][ALERT_TECH_SPEC_PUT][INTERPRETER_INFERRED] alertId="
                        + context.alertId()
                        + " interpreterType=" + inferred.interpreterType()
                        + " reason=" + inferred.reason());
            }
            return inferred.interpreterType();
        }
        return null;
    }

    private InferredInterpreter inferInterpreterType(Map<String, Object> technicalSpecification) {
        String inputModel = stringValue(technicalSpecification.get("inputModel"));
        String triggerType = stringValue(technicalSpecification.get("triggerType"));
        String evaluationMode = stringValue(technicalSpecification.get("evaluationMode"));
        String accessMode = stringValue(technicalSpecification.get("accessMode"));

        boolean eventSignal = "ServiceDataV2".equals(inputModel)
                || "EVENT".equals(triggerType)
                || "STATELESS_EVENT_MATCH".equals(evaluationMode);
        boolean scheduledSignal = "ServiceDataStopPointJourneysV2".equals(inputModel)
                || "SCHEDULE".equals(triggerType)
                || "SCHEDULED_SNAPSHOT_MATCH".equals(evaluationMode)
                || "SERVICE_DATA_API_SNAPSHOT".equals(accessMode)
                || presentMap(technicalSpecification.get("snapshotEvaluation"));

        if (eventSignal == scheduledSignal) {
            return null;
        }
        if (eventSignal) {
            return new InferredInterpreter(EVENT_INTERPRETER, "event metadata");
        }
        return new InferredInterpreter(SCHEDULED_INTERPRETER, "scheduled metadata");
    }

    private ValidationResult validateEventSpecification(Map<String, Object> technicalSpecification, String interpreterType) {
        ValidationResult common = validateCommon(
                technicalSpecification,
                interpreterType,
                "EVENT",
                "ServiceDataV2",
                "STATELESS_EVENT_MATCH");
        if (!common.valid()) {
            return common;
        }
        if (!presentMap(technicalSpecification.get("condition"))) {
            return ValidationResult.invalid("technicalSpecification.condition is required for EVENT_INTERPRETER.");
        }
        return ValidationResult.valid(interpreterType);
    }

    private ValidationResult validateScheduledSpecification(Map<String, Object> technicalSpecification, String interpreterType) {
        ValidationResult common = validateCommon(
                technicalSpecification,
                interpreterType,
                "SCHEDULE",
                "ServiceDataStopPointJourneysV2",
                "SCHEDULED_SNAPSHOT_MATCH");
        if (!common.valid()) {
            return common;
        }
        if (!"SERVICE_DATA_API_SNAPSHOT".equals(stringValue(technicalSpecification.get("accessMode")))) {
            return ValidationResult.unsupported("technicalSpecification.accessMode must be SERVICE_DATA_API_SNAPSHOT.");
        }

        Map<String, Object> serviceDataQuery = asMap(technicalSpecification.get("serviceDataQuery"));
        if (serviceDataQuery == null) {
            return ValidationResult.invalid("technicalSpecification.serviceDataQuery is required.");
        }
        if (!"POST /v2/stoppointjourneys".equals(stringValue(serviceDataQuery.get("operation")))) {
            return ValidationResult.unsupported("technicalSpecification.serviceDataQuery.operation must be POST /v2/stoppointjourneys.");
        }
        if ("EXPLICIT_STOP_POINTS".equals(stringValue(serviceDataQuery.get("monitoringScope")))
                && !presentCollection(serviceDataQuery.get("stopPoints"))) {
            return ValidationResult.invalid("technicalSpecification.serviceDataQuery.stopPoints is required for EXPLICIT_STOP_POINTS monitoring.");
        }

        Map<String, Object> schedule = asMap(technicalSpecification.get("schedule"));
        if (schedule == null || positiveNumber(schedule.get("frequencySeconds")) == null) {
            return ValidationResult.invalid("technicalSpecification.schedule.frequencySeconds must be greater than zero.");
        }
        if (!presentMap(technicalSpecification.get("outputPolicy"))) {
            return ValidationResult.invalid("technicalSpecification.outputPolicy is required.");
        }
        if (!presentMap(technicalSpecification.get("snapshotEvaluation"))) {
            return ValidationResult.invalid("technicalSpecification.snapshotEvaluation is required.");
        }
        return ValidationResult.valid(interpreterType);
    }

    private ValidationResult validateCommon(
            Map<String, Object> technicalSpecification,
            String interpreterType,
            String expectedTriggerType,
            String expectedInputModel,
            String expectedEvaluationMode) {
        if (!expectedTriggerType.equals(stringValue(technicalSpecification.get("triggerType")))) {
            return ValidationResult.unsupported("technicalSpecification.triggerType is not coherent with " + interpreterType + ".");
        }
        if (!expectedInputModel.equals(stringValue(technicalSpecification.get("inputModel")))) {
            return ValidationResult.unsupported("technicalSpecification.inputModel is not coherent with " + interpreterType + ".");
        }
        if (!OUTPUT_MODEL.equals(stringValue(technicalSpecification.get("outputModel")))) {
            return ValidationResult.unsupported("technicalSpecification.outputModel must be " + OUTPUT_MODEL + ".");
        }
        if (!expectedEvaluationMode.equals(stringValue(technicalSpecification.get("evaluationMode")))) {
            return ValidationResult.unsupported("technicalSpecification.evaluationMode is not coherent with " + interpreterType + ".");
        }
        return ValidationResult.valid(interpreterType);
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private boolean presentMap(Object value) {
        return value instanceof Map<?, ?> map && !map.isEmpty();
    }

    private boolean presentCollection(Object value) {
        return value instanceof Collection<?> collection && !collection.isEmpty();
    }

    private Number positiveNumber(Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }
        return number.doubleValue() > 0 ? number : null;
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

    public record ManualValidationContext(
            String alertId,
            String alertInterpreterType) {
    }

    private record InferredInterpreter(
            String interpreterType,
            String reason) {
    }
}
