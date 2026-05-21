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
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(text, new TypeReference<>() {
            });
            AlertVerificationDecision decision = parseDecision(asString(payload.get("decision")));
            if (decision == null) {
                return Optional.empty();
            }

            return Optional.of(new AlertVerificationOutcome(
                    decision,
                    asString(payload.get("summary")),
                    asString(payload.get("rejectedReason")),
                    asDouble(payload.get("confidence")),
                    provider,
                    model,
                    PROMPT_VERSION,
                    asStringList(payload.get("requiredSources")),
                    asString(payload.get("interpreterType")),
                    INPUT_MODEL,
                    OUTPUT_MODEL,
                    asString(payload.get("triggerType")),
                    asString(payload.get("evaluationMode")),
                    asStringList(payload.get("interpretedEventNames")),
                    asStringList(payload.get("targetTypes")),
                    asMap(payload.get("technicalSpecification")),
                    asMap(payload.get("agentBlueprintPreview")),
                    asStringList(payload.get("warnings")),
                    asStringList(payload.get("safetyChecks"))));
        } catch (JsonProcessingException ex) {
            return Optional.empty();
        }
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
        return Map.of();
    }
}
