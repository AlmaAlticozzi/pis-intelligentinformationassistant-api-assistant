package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ScheduledAlertVerificationResponseParser {

    private static final String PROMPT_VERSION = "alert-scheduled-verify-mvp-v1";
    private static final String REQUIRED_SOURCE = "SERVICE_DATA";
    private static final String INTERPRETER_TYPE = "SCHEDULED_INTERPRETER";
    private static final String ACCESS_MODE = "SERVICE_DATA_API_SNAPSHOT";
    private static final String TRIGGER_TYPE = "SCHEDULE";
    private static final String EVALUATION_MODE = "SCHEDULED_SNAPSHOT_MATCH";
    private static final String INPUT_MODEL = "ServiceDataStopPointJourneysV2";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Optional<AlertVerificationOutcome> parse(String text, String provider, String model) {
        return parseDetailed(text, provider, model).outcome();
    }

    public ParseResult parseDetailed(String text, String provider, String model) {
        int rawLength = text == null ? 0 : text.length();
        if (text == null || text.isBlank()) {
            return new ParseResult(Optional.empty(), "LLM response is empty.", rawLength);
        }
        String trimmed = text.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return new ParseResult(Optional.empty(), "LLM response must be raw JSON object without markdown or prose.", rawLength);
        }

        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(trimmed, new TypeReference<>() {
            });
            AlertVerificationDecision decision = parseDecision(asString(payload.get("decision")));
            if (decision == null) {
                return new ParseResult(Optional.empty(), "Missing or unsupported decision field.", rawLength);
            }

            Map<String, Object> technicalSpecification = asMap(payload.get("technicalSpecification"));
            Map<String, Object> agentBlueprintPreview = asMap(payload.get("agentBlueprintPreview"));
            Map<String, Object> requirementCoverage = asMap(payload.get("requirementCoverage"));
            normalizeScheduledFields(payload, technicalSpecification, agentBlueprintPreview);

            String structuralFailure = validateScheduledEnvelope(decision, payload, technicalSpecification, agentBlueprintPreview);
            if (structuralFailure != null) {
                return new ParseResult(Optional.empty(), structuralFailure, rawLength);
            }

            return new ParseResult(Optional.of(new AlertVerificationOutcome(
                    decision,
                    asString(payload.get("summary")),
                    asString(payload.get("rejectedReason")),
                    asDouble(payload.get("confidence")),
                    provider,
                    model,
                    PROMPT_VERSION,
                    asStringList(payload.get("requiredSources")),
                    asString(payload.get("interpreterType")),
                    firstString(payload.get("inputModel"), technicalSpecification == null ? null : technicalSpecification.get("inputModel")),
                    firstString(payload.get("outputModel"), technicalSpecification == null ? null : technicalSpecification.get("outputModel")),
                    asString(payload.get("triggerType")),
                    asString(payload.get("evaluationMode")),
                    asStringList(payload.get("interpretedEventNames")),
                    asStringList(payload.get("targetTypes")),
                    technicalSpecification,
                    agentBlueprintPreview,
                    requirementCoverage,
                    asStringList(payload.get("warnings")),
                    asStringList(payload.get("safetyChecks")))), null, rawLength);
        } catch (JsonProcessingException ex) {
            return new ParseResult(Optional.empty(), concise(ex.getOriginalMessage()), rawLength);
        }
    }

    private void normalizeScheduledFields(
            Map<String, Object> payload,
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview) {
        uppercase(payload, "decision");
        uppercase(payload, "interpreterType");
        uppercase(payload, "accessMode");
        uppercase(payload, "triggerType");
        uppercase(payload, "evaluationMode");
        uppercaseList(payload, "requiredSources");
        uppercaseList(payload, "targetTypes");
        if (technicalSpecification != null) {
            uppercase(technicalSpecification, "source");
            uppercase(technicalSpecification, "interpreterType");
            uppercase(technicalSpecification, "accessMode");
            uppercase(technicalSpecification, "triggerType");
            uppercase(technicalSpecification, "evaluationMode");
        }
        if (agentBlueprintPreview != null) {
            uppercase(agentBlueprintPreview, "triggerType");
            uppercase(agentBlueprintPreview, "evaluationMode");
            uppercaseList(agentBlueprintPreview, "requiredSources");
            uppercaseList(agentBlueprintPreview, "targetTypes");
        }
    }

    private String validateScheduledEnvelope(
            AlertVerificationDecision decision,
            Map<String, Object> payload,
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview) {
        if (decision == AlertVerificationDecision.REJECTED) {
            if (isBlank(asString(payload.get("rejectedReason")))) {
                return "REJECTED scheduled verification response requires rejectedReason.";
            }
            if (technicalSpecification != null) {
                return "REJECTED scheduled verification response must have null technicalSpecification.";
            }
            if (agentBlueprintPreview != null) {
                return "REJECTED scheduled verification response must have null agentBlueprintPreview.";
            }
            return null;
        }

        if (decision != AlertVerificationDecision.VERIFIED) {
            return "Unsupported scheduled verification decision.";
        }
        if (technicalSpecification == null) {
            return "VERIFIED scheduled verification response requires technicalSpecification.";
        }
        if (agentBlueprintPreview == null) {
            return "VERIFIED scheduled verification response requires agentBlueprintPreview.";
        }
        if (!asStringList(payload.get("requiredSources")).contains(REQUIRED_SOURCE)) {
            return "VERIFIED scheduled verification response requires SERVICE_DATA source.";
        }
        if (!INTERPRETER_TYPE.equals(firstString(payload.get("interpreterType"), technicalSpecification.get("interpreterType")))) {
            return "VERIFIED scheduled verification response requires SCHEDULED_INTERPRETER.";
        }
        if (!ACCESS_MODE.equals(firstString(payload.get("accessMode"), technicalSpecification.get("accessMode")))) {
            return "VERIFIED scheduled verification response requires SERVICE_DATA_API_SNAPSHOT.";
        }
        if (!TRIGGER_TYPE.equals(firstString(payload.get("triggerType"), technicalSpecification.get("triggerType")))) {
            return "VERIFIED scheduled verification response requires triggerType SCHEDULE.";
        }
        if (!EVALUATION_MODE.equals(firstString(payload.get("evaluationMode"), technicalSpecification.get("evaluationMode")))) {
            return "VERIFIED scheduled verification response requires SCHEDULED_SNAPSHOT_MATCH.";
        }
        if (!INPUT_MODEL.equals(firstString(payload.get("inputModel"), technicalSpecification.get("inputModel")))) {
            return "VERIFIED scheduled verification response requires ServiceDataStopPointJourneysV2 inputModel.";
        }
        if (!OUTPUT_MODEL.equals(firstString(payload.get("outputModel"), technicalSpecification.get("outputModel")))) {
            return "VERIFIED scheduled verification response requires AgentOutput.CANDIDATE_SUGGESTION outputModel.";
        }
        return null;
    }

    private AlertVerificationDecision parseDecision(String decision) {
        if (decision == null) {
            return null;
        }
        try {
            return AlertVerificationDecision.valueOf(decision.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void uppercase(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            map.put(key, stringValue.trim().toUpperCase(Locale.ROOT));
        }
    }

    private void uppercaseList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Collection<?> collection) {
            map.put(key, collection.stream()
                    .map(String::valueOf)
                    .map(item -> item.trim().toUpperCase(Locale.ROOT))
                    .toList());
        }
    }

    private String concise(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Invalid JSON response.";
        }
        return reason.length() > 240 ? reason.substring(0, 240) : reason;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstString(Object... values) {
        for (Object value : values) {
            String stringValue = asString(value);
            if (stringValue != null && !stringValue.isBlank()) {
                return stringValue;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<String> asStringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
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

    public record ParseResult(
            Optional<AlertVerificationOutcome> outcome,
            String failureReason,
            int rawLength) {
    }
}
