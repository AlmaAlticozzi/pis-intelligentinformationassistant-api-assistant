package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AlertRouteUnderstandingResponseParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Optional<AlertRouteUnderstandingResult> parse(String text) {
        return parseDetailed(text).result();
    }

    public ParseResult parseDetailed(String text) {
        int rawLength = text == null ? 0 : text.length();
        if (text == null || text.isBlank()) {
            return new ParseResult(Optional.empty(), "LLM route response is empty.", rawLength);
        }
        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(text, new TypeReference<>() {
            });
            AlertRouteDecision decision = enumValue(AlertRouteDecision.class, payload.get("decision"));
            if (decision == null) {
                return new ParseResult(Optional.empty(), "Missing or unsupported route decision field.", rawLength);
            }
            return new ParseResult(Optional.of(new AlertRouteUnderstandingResult(
                    decision,
                    upperStringList(payload.get("dataDomains")),
                    upperString(payload.get("primaryDataDomain")),
                    enumValue(AlertRouteInterpreterType.class, payload.get("interpreterType"), AlertRouteInterpreterType.UNKNOWN),
                    enumValue(AlertRouteAccessMode.class, payload.get("accessMode"), AlertRouteAccessMode.NONE),
                    enumValue(AlertRouteIntentKind.class, payload.get("intentKind"), AlertRouteIntentKind.UNSUPPORTED),
                    enumValue(AlertRouteOutputMode.class, payload.get("outputMode"), AlertRouteOutputMode.NONE),
                    asBoolean(payload.get("requiresPolling")),
                    asBoolean(payload.get("requiresServiceDataApi")),
                    asBoolean(payload.get("requiresKafkaEvent")),
                    asBoolean(payload.get("hasAggregation")),
                    asBoolean(payload.get("hasCardinalityThreshold")),
                    asBoolean(payload.get("hasReportIntent")),
                    asDouble(payload.get("confidence")),
                    asString(payload.get("summary")),
                    asString(payload.get("rejectedReason")),
                    stringList(payload.get("warnings")))), null, rawLength);
        } catch (JsonProcessingException ex) {
            return new ParseResult(Optional.empty(), concise(ex.getOriginalMessage()), rawLength);
        }
    }

    private <T extends Enum<T>> T enumValue(Class<T> enumType, Object value) {
        return enumValue(enumType, value, null);
    }

    private <T extends Enum<T>> T enumValue(Class<T> enumType, Object value, T defaultValue) {
        String text = upperString(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, text);
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    private String upperString(Object value) {
        String text = asString(value);
        return text == null ? null : text.toUpperCase(Locale.ROOT);
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(value));
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

    private List<String> upperStringList(Object value) {
        return stringList(value).stream()
                .map(item -> item.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private List<String> stringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::asString)
                    .filter(item -> item != null && !item.isBlank())
                    .toList();
        }
        return List.of();
    }

    private String concise(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Invalid JSON route response.";
        }
        return reason.length() > 240 ? reason.substring(0, 240) : reason;
    }

    public record ParseResult(
            Optional<AlertRouteUnderstandingResult> result,
            String failureReason,
            int rawLength) {
    }
}
