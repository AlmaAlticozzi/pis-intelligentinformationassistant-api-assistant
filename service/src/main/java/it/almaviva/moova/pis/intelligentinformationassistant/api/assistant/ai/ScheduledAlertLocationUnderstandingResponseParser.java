package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ScheduledAlertLocationUnderstandingResponseParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ScheduledAlertLocationUnderstandingResult parse(String text) {
        return parseDetailed(text).result();
    }

    public ParseResult parseDetailed(String text) {
        List<String> warnings = new ArrayList<>();
        if (text == null || text.isBlank()) {
            warnings.add("LLM response is empty.");
            return new ParseResult(ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(warnings), warnings, false);
        }

        String trimmed = text.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            warnings.add("LLM response must be raw JSON object only.");
            return new ParseResult(ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(warnings), warnings, false);
        }

        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(trimmed, new TypeReference<>() {
            });
            ScheduledAlertLocationUnderstandingResult result = parsePayload(payload, warnings);
            if (result.monitoringScope() == ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS
                    && !result.hasLocations()) {
                warnings.add("EXPLICIT_STOP_POINTS monitoringScope requires at least one location.");
                return new ParseResult(ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(warnings), warnings, false);
            }
            return new ParseResult(result, List.copyOf(warnings), true);
        } catch (JsonProcessingException ex) {
            warnings.add("LLM response is not valid JSON: " + concise(ex.getOriginalMessage()));
            return new ParseResult(ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(warnings), warnings, false);
        }
    }

    private ScheduledAlertLocationUnderstandingResult parsePayload(Map<String, Object> payload, List<String> warnings) {
        boolean hasLocations = Boolean.TRUE.equals(payload.get("hasLocations"));
        String language = asString(payload.get("language"));
        ScheduledAlertMonitoringScope monitoringScope = parseEnum(
                ScheduledAlertMonitoringScope.class,
                asString(payload.get("monitoringScope")),
                ScheduledAlertMonitoringScope.UNSPECIFIED,
                "monitoringScope",
                warnings);
        List<ScheduledAlertLocationMention> locations = parseLocations(payload.get("locations"), warnings);
        List<ScheduledAlertNonLocationConstraint> nonLocationConstraints =
                parseNonLocationConstraints(payload.get("nonLocationConstraints"), warnings);
        warnings.addAll(asStringList(payload.get("warnings")));

        return new ScheduledAlertLocationUnderstandingResult(
                hasLocations,
                language,
                monitoringScope,
                locations,
                nonLocationConstraints,
                warnings);
    }

    private List<ScheduledAlertLocationMention> parseLocations(Object value, List<String> warnings) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        List<ScheduledAlertLocationMention> result = new ArrayList<>();
        int index = 0;
        for (Object item : collection) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                warnings.add("locations[" + index + "] is not an object and was ignored.");
                index++;
                continue;
            }
            Map<String, Object> map = stringKeyMap(rawMap);
            ScheduledAlertLocationRole role = parseEnum(
                    ScheduledAlertLocationRole.class,
                    asString(map.get("role")),
                    ScheduledAlertLocationRole.UNKNOWN_LOCATION_ROLE,
                    "locations[" + index + "].role",
                    warnings);
            ScheduledAlertLocationRelation relation = parseEnum(
                    ScheduledAlertLocationRelation.class,
                    asString(map.get("relationToSnapshot")),
                    ScheduledAlertLocationRelation.UNKNOWN,
                    "locations[" + index + "].relationToSnapshot",
                    warnings);
            ScheduledAlertLocationPolarity polarity = parseEnum(
                    ScheduledAlertLocationPolarity.class,
                    asString(map.get("polarity")),
                    ScheduledAlertLocationPolarity.INCLUDE,
                    "locations[" + index + "].polarity",
                    warnings);
            double confidence = clamp(asDouble(map.get("confidence"), 0.0),
                    "locations[" + index + "].confidence",
                    warnings);
            result.add(new ScheduledAlertLocationMention(
                    asString(map.get("rawText")),
                    asString(map.get("normalizedText")),
                    role,
                    relation,
                    Boolean.TRUE.equals(map.get("requiredForApiQuery")),
                    Boolean.TRUE.equals(map.get("requiredCoverage")),
                    polarity,
                    asString(map.get("logicalGroup")),
                    confidence));
            index++;
        }
        return result;
    }

    private List<ScheduledAlertNonLocationConstraint> parseNonLocationConstraints(
            Object value,
            List<String> warnings) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        List<ScheduledAlertNonLocationConstraint> result = new ArrayList<>();
        int index = 0;
        for (Object item : collection) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                warnings.add("nonLocationConstraints[" + index + "] is not an object and was ignored.");
                index++;
                continue;
            }
            Map<String, Object> map = stringKeyMap(rawMap);
            ScheduledAlertNonLocationConstraintType type = parseEnum(
                    ScheduledAlertNonLocationConstraintType.class,
                    asString(map.get("type")),
                    ScheduledAlertNonLocationConstraintType.UNKNOWN,
                    "nonLocationConstraints[" + index + "].type",
                    warnings);
            result.add(new ScheduledAlertNonLocationConstraint(type, asString(map.get("rawText"))));
            index++;
        }
        return result;
    }

    private <E extends Enum<E>> E parseEnum(
            Class<E> enumType,
            String value,
            E fallback,
            String field,
            List<String> warnings) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            warnings.add("Unknown " + field + " value \"" + value + "\" mapped to " + fallback.name() + ".");
            return fallback;
        }
    }

    private double clamp(double value, String field, List<String> warnings) {
        if (Double.isNaN(value)) {
            warnings.add(field + " is not a number and was set to 0.0.");
            return 0.0;
        }
        if (value < 0.0) {
            warnings.add(field + " was below 0.0 and was clamped to 0.0.");
            return 0.0;
        }
        if (value > 1.0) {
            warnings.add(field + " was above 1.0 and was clamped to 1.0.");
            return 1.0;
        }
        return value;
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

    private double asDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    private List<String> asStringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(String::valueOf)
                    .toList();
        }
        return List.of();
    }

    private Map<String, Object> stringKeyMap(Map<?, ?> map) {
        return map.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        Map.Entry::getValue,
                        (left, right) -> right,
                        java.util.LinkedHashMap::new));
    }

    public record ParseResult(
            ScheduledAlertLocationUnderstandingResult result,
            List<String> warnings,
            boolean parsed) {
    }
}
