package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AgentGenerationLlmResponseParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Optional<AgentGenerationPreviewOutcome> parse(String text) {
        if (text == null || text.isBlank() || text.contains("```")) {
            return Optional.empty();
        }
        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(text, new TypeReference<>() {
            });
            Map<String, Object> blueprint = asMap(payload.get("blueprint"));
            if (blueprint == null || blueprint.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new AgentGenerationPreviewOutcome(
                    Boolean.TRUE.equals(payload.get("canGenerate")),
                    stringValue(payload.get("recommendedGenerationMode")),
                    stringValue(payload.get("estimatedComplexity")),
                    stringList(payload.get("requiredSources")),
                    stringList(payload.get("requiredPermissions")),
                    blueprint,
                    stringList(payload.get("warnings")),
                    stringValue(payload.get("rejectedReason"))));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private List<String> stringList(Object value) {
        return value instanceof Collection<?> collection
                ? collection.stream().map(String::valueOf).toList()
                : List.of();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
