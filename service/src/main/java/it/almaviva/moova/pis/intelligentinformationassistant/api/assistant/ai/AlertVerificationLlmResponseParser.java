package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AlertVerificationLlmResponseParser {

    private static final String PROMPT_VERSION = "alert-verify-mvp-v1";
    private static final String INPUT_MODEL = "ServiceDataV2";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Optional<AlertVerificationOutcome> parse(String text, String provider, String model) {
        return parseDetailed(text, provider, model).outcome();
    }

    public ParseResult parseDetailed(String text, String provider, String model) {
        int rawLength = text == null ? 0 : text.length();
        boolean looksTruncated = looksTruncated(text);
        if (text == null || text.isBlank()) {
            return new ParseResult(Optional.empty(), "LLM response is empty.", rawLength, false);
        }

        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(text, new TypeReference<>() {
            });
            AlertVerificationDecision decision = parseDecision(asString(payload.get("decision")));
            if (decision == null) {
                return new ParseResult(Optional.empty(), "Missing or unsupported decision field.", rawLength, looksTruncated);
            }
            Map<String, Object> technicalSpecification = asMap(payload.get("technicalSpecification"));
            Map<String, Object> agentBlueprintPreview = asMap(payload.get("agentBlueprintPreview"));
            Map<String, Object> requirementCoverage = asMap(payload.get("requirementCoverage"));
            int temporalConditionCount = countTemporalConditions(technicalSpecification)
                    + countTemporalConditions(agentBlueprintPreview);
            System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] temporal conditions found by parser=" + temporalConditionCount);

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
                    firstString(payload.get("inputModel"), technicalSpecification == null ? null : technicalSpecification.get("inputModel"), INPUT_MODEL),
                    firstString(payload.get("outputModel"), technicalSpecification == null ? null : technicalSpecification.get("outputModel"), OUTPUT_MODEL),
                    asString(payload.get("triggerType")),
                    asString(payload.get("evaluationMode")),
                    asStringList(payload.get("interpretedEventNames")),
                    asStringList(payload.get("targetTypes")),
                    technicalSpecification,
                    agentBlueprintPreview,
                    requirementCoverage,
                    asStringList(payload.get("warnings")),
                    asStringList(payload.get("safetyChecks")))), null, rawLength, false);
        } catch (JsonProcessingException ex) {
            return new ParseResult(Optional.empty(), concise(ex.getOriginalMessage()), rawLength, looksTruncated);
        }
    }

    private boolean looksTruncated(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String trimmed = text.trim();
        if (!trimmed.endsWith("}")) {
            return true;
        }
        int braceBalance = 0;
        boolean quoted = false;
        boolean escaped = false;
        for (char character : trimmed.toCharArray()) {
            if (escaped) {
                escaped = false;
            } else if (character == '\\' && quoted) {
                escaped = true;
            } else if (character == '"') {
                quoted = !quoted;
            } else if (!quoted && character == '{') {
                braceBalance++;
            } else if (!quoted && character == '}') {
                braceBalance--;
            }
        }
        return quoted || braceBalance != 0;
    }

    private String concise(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Invalid JSON response.";
        }
        return reason.length() > 240 ? reason.substring(0, 240) : reason;
    }

    private AlertVerificationDecision parseDecision(String decision) {
        if (decision == null) {
            return null;
        }
        try {
            return AlertVerificationDecision.valueOf(decision);
        } catch (IllegalArgumentException ex) {
            return null;
        }
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

    private int countTemporalConditions(Object value) {
        if (value instanceof Map<?, ?> map) {
            int current = "LOCAL_TIME_BETWEEN".equals(String.valueOf(map.get("operator"))) ? 1 : 0;
            return current + map.values().stream().mapToInt(this::countTemporalConditions).sum();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().mapToInt(this::countTemporalConditions).sum();
        }
        return 0;
    }

    public record ParseResult(
            Optional<AlertVerificationOutcome> outcome,
            String failureReason,
            int rawLength,
            boolean looksTruncated) {
    }
}
