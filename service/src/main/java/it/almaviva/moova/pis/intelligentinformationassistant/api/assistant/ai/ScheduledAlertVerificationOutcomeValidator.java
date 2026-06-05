package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ScheduledAlertVerificationOutcomeValidator {

    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final String SCHEDULED_INTERPRETER = "SCHEDULED_INTERPRETER";
    private static final String SERVICE_DATA_API_SNAPSHOT = "SERVICE_DATA_API_SNAPSHOT";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String SCHEDULED_SNAPSHOT_MATCH = "SCHEDULED_SNAPSHOT_MATCH";
    private static final String INPUT_MODEL = "ServiceDataStopPointJourneysV2";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final String TARGET_TYPE = "SERVICE_DATA_JOURNEY_AGGREGATE";
    private static final String TECHNICAL_SCHEMA_VERSION = "iia.alert.technical-specification/v2";
    private static final String BLUEPRINT_SCHEMA_VERSION = "iia.agent.blueprint/v1";
    private static final String AGENT_NAME = "ScheduledServiceDataSnapshotAlertAgent";
    private static final String OUTPUT_TYPE = "CANDIDATE_SUGGESTION";
    private static final String DEFAULT_REJECTED_REASON =
            "Scheduled alert verification rejected the result because it does not satisfy the Scheduled MVP contract.";
    private static final List<String> FORBIDDEN_OUTPUT_MARKERS = List.of(
            "payload.ongroundServiceEvent",
            "ServiceDataV2",
            "EVENT_INTERPRETER",
            "STATELESS_EVENT_MATCH");

    public AlertVerificationOutcome validate(AlertVerificationOutcome outcome) {
        if (outcome == null) {
            return fail(null, DEFAULT_REJECTED_REASON);
        }
        logBase(outcome);

        if (outcome.decision() == AlertVerificationDecision.REJECTED) {
            if (isBlank(outcome.rejectedReason())) {
                return fail(outcome, "Rejected scheduled verification outcome requires rejectedReason.");
            }
            if (outcome.technicalSpecification() != null) {
                return fail(outcome, "Rejected scheduled verification outcome must not contain technicalSpecification.");
            }
            if (outcome.agentBlueprintPreview() != null) {
                return fail(outcome, "Rejected scheduled verification outcome must not contain agentBlueprintPreview.");
            }
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] Validation passed");
            return outcome;
        }

        if (outcome.decision() != AlertVerificationDecision.VERIFIED) {
            return fail(outcome, DEFAULT_REJECTED_REASON);
        }

        String failure = validateVerified(outcome);
        if (failure != null) {
            return fail(outcome, failure);
        }
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] Validation passed");
        return outcome;
    }

    private String validateVerified(AlertVerificationOutcome outcome) {
        if (!containsOnlyServiceData(outcome.requiredSources())) {
            return "Verified scheduled outcome must use only SERVICE_DATA as required source.";
        }
        if (!SCHEDULED_INTERPRETER.equals(outcome.interpreterType())) {
            return "Verified scheduled outcome requires interpreterType SCHEDULED_INTERPRETER.";
        }
        if (!INPUT_MODEL.equals(outcome.inputModel())) {
            return "Verified scheduled outcome requires inputModel ServiceDataStopPointJourneysV2.";
        }
        if (!OUTPUT_MODEL.equals(outcome.outputModel())) {
            return "Verified scheduled outcome requires outputModel AgentOutput.CANDIDATE_SUGGESTION.";
        }
        if (!SCHEDULE.equals(outcome.triggerType())) {
            return "Verified scheduled outcome requires triggerType SCHEDULE.";
        }
        if (!SCHEDULED_SNAPSHOT_MATCH.equals(outcome.evaluationMode())) {
            return "Verified scheduled outcome requires evaluationMode SCHEDULED_SNAPSHOT_MATCH.";
        }
        if (!normalizedList(outcome.interpretedTargetTypes()).contains(TARGET_TYPE)) {
            return "Verified scheduled outcome requires targetTypes to contain SERVICE_DATA_JOURNEY_AGGREGATE.";
        }
        if (outcome.confidence() != null && (outcome.confidence() < 0.0 || outcome.confidence() > 1.0)) {
            return "Verified scheduled outcome confidence must be between 0.0 and 1.0.";
        }
        if (outcome.technicalSpecification() == null || outcome.technicalSpecification().isEmpty()) {
            return "Verified scheduled outcome requires technicalSpecification.";
        }
        if (outcome.agentBlueprintPreview() == null || outcome.agentBlueprintPreview().isEmpty()) {
            return "Verified scheduled outcome requires agentBlueprintPreview.";
        }
        String contamination = forbiddenOutputContamination(outcome.technicalSpecification(), outcome.agentBlueprintPreview());
        if (contamination != null) {
            return "Verified scheduled outcome contains Event/Kafka contamination: " + contamination + ".";
        }
        String technicalFailure = validateTechnicalSpecification(outcome.technicalSpecification());
        if (technicalFailure != null) {
            return technicalFailure;
        }
        return validateAgentBlueprintPreview(outcome.agentBlueprintPreview());
    }

    private String validateTechnicalSpecification(Map<String, Object> technicalSpecification) {
        if (!TECHNICAL_SCHEMA_VERSION.equals(stringValue(technicalSpecification.get("schemaVersion")))) {
            return "technicalSpecification.schemaVersion must be iia.alert.technical-specification/v2.";
        }
        if (!SERVICE_DATA.equals(stringValue(technicalSpecification.get("source")))) {
            return "technicalSpecification.source must be SERVICE_DATA.";
        }
        if (!SCHEDULED_INTERPRETER.equals(stringValue(technicalSpecification.get("interpreterType")))) {
            return "technicalSpecification.interpreterType must be SCHEDULED_INTERPRETER.";
        }
        if (!SERVICE_DATA_API_SNAPSHOT.equals(stringValue(technicalSpecification.get("accessMode")))) {
            return "technicalSpecification.accessMode must be SERVICE_DATA_API_SNAPSHOT.";
        }
        if (!INPUT_MODEL.equals(stringValue(technicalSpecification.get("inputModel")))) {
            return "technicalSpecification.inputModel must be ServiceDataStopPointJourneysV2.";
        }
        if (!OUTPUT_MODEL.equals(stringValue(technicalSpecification.get("outputModel")))) {
            return "technicalSpecification.outputModel must be AgentOutput.CANDIDATE_SUGGESTION.";
        }
        if (!SCHEDULE.equals(stringValue(technicalSpecification.get("triggerType")))) {
            return "technicalSpecification.triggerType must be SCHEDULE.";
        }
        if (!SCHEDULED_SNAPSHOT_MATCH.equals(stringValue(technicalSpecification.get("evaluationMode")))) {
            return "technicalSpecification.evaluationMode must be SCHEDULED_SNAPSHOT_MATCH.";
        }
        if (!presentMap(technicalSpecification.get("schedule"))) {
            return "technicalSpecification.schedule is required.";
        }
        if (!presentMap(technicalSpecification.get("serviceDataQuery"))) {
            return "technicalSpecification.serviceDataQuery is required.";
        }
        if (!presentMap(technicalSpecification.get("snapshotEvaluation"))) {
            return "technicalSpecification.snapshotEvaluation is required.";
        }
        if (!presentMap(asMap(technicalSpecification.get("snapshotEvaluation")).get("condition"))) {
            return "technicalSpecification.snapshotEvaluation.condition is required.";
        }
        if (!presentMap(technicalSpecification.get("outputPolicy"))) {
            return "technicalSpecification.outputPolicy is required.";
        }
        if (isBlank(stringValue(technicalSpecification.get("deduplicationKeyTemplate")))) {
            return "technicalSpecification.deduplicationKeyTemplate is required.";
        }
        return null;
    }

    private String validateAgentBlueprintPreview(Map<String, Object> preview) {
        if (!BLUEPRINT_SCHEMA_VERSION.equals(stringValue(preview.get("schemaVersion")))) {
            return "agentBlueprintPreview.schemaVersion must be iia.agent.blueprint/v1.";
        }
        if (isBlank(stringValue(preview.get("agentName")))) {
            return "agentBlueprintPreview.agentName is required.";
        }
        if (!AGENT_NAME.equals(stringValue(preview.get("agentName")))) {
            return "agentBlueprintPreview.agentName must be ScheduledServiceDataSnapshotAlertAgent.";
        }
        if (!SCHEDULE.equals(stringValue(preview.get("triggerType")))) {
            return "agentBlueprintPreview.triggerType must be SCHEDULE.";
        }
        if (!containsOnlyServiceData(stringList(preview.get("requiredSources")))) {
            return "agentBlueprintPreview.requiredSources must contain only SERVICE_DATA.";
        }
        if (!SCHEDULED_SNAPSHOT_MATCH.equals(stringValue(preview.get("evaluationMode")))) {
            return "agentBlueprintPreview.evaluationMode must be SCHEDULED_SNAPSHOT_MATCH.";
        }
        if (!normalizedList(stringList(preview.get("targetTypes"))).contains(TARGET_TYPE)) {
            return "agentBlueprintPreview.targetTypes must contain SERVICE_DATA_JOURNEY_AGGREGATE.";
        }
        Map<String, Object> parameters = asMap(preview.get("parameters"));
        if (parameters == null || parameters.isEmpty()) {
            return "agentBlueprintPreview.parameters is required.";
        }
        if (!presentMap(parameters.get("serviceDataQuery"))) {
            return "agentBlueprintPreview.parameters.serviceDataQuery is required.";
        }
        if (!presentMap(parameters.get("snapshotEvaluation"))) {
            return "agentBlueprintPreview.parameters.snapshotEvaluation is required.";
        }
        if (!presentMap(parameters.get("outputPolicy"))) {
            return "agentBlueprintPreview.parameters.outputPolicy is required.";
        }
        Map<String, Object> stateRequirements = asMap(preview.get("stateRequirements"));
        if (stateRequirements == null || !Boolean.FALSE.equals(stateRequirements.get("requiresState"))) {
            return "agentBlueprintPreview.stateRequirements.requiresState must be false.";
        }
        Map<String, Object> output = asMap(preview.get("output"));
        if (output == null || !OUTPUT_TYPE.equals(stringValue(output.get("type")))) {
            return "agentBlueprintPreview.output.type must be CANDIDATE_SUGGESTION.";
        }
        return null;
    }

    private void logBase(AlertVerificationOutcome outcome) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] decision=" + outcome.decision());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] requiredSources=" + outcome.requiredSources()
                + " interpreterType=" + outcome.interpreterType()
                + " triggerType=" + outcome.triggerType()
                + " evaluationMode=" + outcome.evaluationMode()
                + " inputModel=" + outcome.inputModel()
                + " outputModel=" + outcome.outputModel());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] technicalSpecification present="
                + (outcome.technicalSpecification() != null && !outcome.technicalSpecification().isEmpty()));
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] agentBlueprintPreview present="
                + (outcome.agentBlueprintPreview() != null && !outcome.agentBlueprintPreview().isEmpty()));
    }

    private AlertVerificationOutcome fail(AlertVerificationOutcome outcome, String reason) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] Validation failed: " + reason);
        return rejected(outcome, reason);
    }

    private AlertVerificationOutcome rejected(AlertVerificationOutcome outcome, String reason) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Scheduled alert verification rejected the result because it does not satisfy the current Scheduled MVP contract.",
                reason,
                0.0,
                outcome == null ? null : outcome.provider(),
                outcome == null ? null : outcome.model(),
                outcome == null ? "alert-scheduled-verify-mvp-v1" : outcome.promptVersion(),
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
                outcome == null || outcome.safetyChecks() == null ? List.of() : outcome.safetyChecks());
    }

    private String forbiddenOutputContamination(Object... roots) {
        for (Object root : roots) {
            String marker = findForbiddenMarker(root);
            if (marker != null) {
                return marker;
            }
        }
        return null;
    }

    private String findForbiddenMarker(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String keyMarker = findForbiddenMarker(String.valueOf(entry.getKey()));
                if (keyMarker != null) {
                    return keyMarker;
                }
                String valueMarker = findForbiddenMarker(entry.getValue());
                if (valueMarker != null) {
                    return valueMarker;
                }
            }
            String triggerType = stringValue(map.get("triggerType"));
            if ("EVENT".equals(triggerType)) {
                return "triggerType EVENT";
            }
            String inputModel = stringValue(map.get("inputModel"));
            if ("ServiceDataV2".equals(inputModel)) {
                return "inputModel ServiceDataV2";
            }
            return null;
        }
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                String marker = findForbiddenMarker(item);
                if (marker != null) {
                    return marker;
                }
            }
            return null;
        }
        String text = stringValue(value);
        if (text == null) {
            return null;
        }
        for (String marker : FORBIDDEN_OUTPUT_MARKERS) {
            if (text.contains(marker)) {
                return marker;
            }
        }
        if ("EVENT".equals(text)) {
            return "triggerType EVENT";
        }
        return null;
    }

    private boolean containsOnlyServiceData(List<String> sources) {
        List<String> normalized = normalizedList(sources);
        return normalized.size() == 1 && normalized.contains(SERVICE_DATA);
    }

    private List<String> normalizedList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .toList();
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private boolean presentMap(Object value) {
        Map<String, Object> map = asMap(value);
        return map != null && !map.isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
