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
public class AlertLocationUnderstandingLlmResponseParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public AlertLocationUnderstandingResult parse(String text) {
        return parseDetailed(text).result();
    }

    public ParseResult parseDetailed(String text) {
        List<String> warnings = new ArrayList<>();
        if (text == null || text.isBlank()) {
            warnings.add("LLM response is empty.");
            return new ParseResult(AlertLocationUnderstandingResult.emptyWithWarnings(warnings), warnings, false);
        }

        String cleaned = stripCodeFence(text);
        try {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(cleaned, new TypeReference<>() {
            });
            return new ParseResult(parsePayload(payload, warnings), List.copyOf(warnings), true);
        } catch (JsonProcessingException ex) {
            warnings.add("LLM response is not valid JSON: " + concise(ex.getOriginalMessage()));
            return new ParseResult(AlertLocationUnderstandingResult.emptyWithWarnings(warnings), warnings, false);
        }
    }

    private AlertLocationUnderstandingResult parsePayload(Map<String, Object> payload, List<String> warnings) {
        boolean hasLocations = Boolean.TRUE.equals(payload.get("hasLocations"));
        String language = asString(payload.get("language"));
        AlertLocationUnderstandingMainEvent mainEvent = parseMainEvent(asMap(payload.get("mainEvent")), warnings);
        List<AlertLocationUnderstandingLocation> locations = parseLocations(payload.get("locations"), warnings);
        List<AlertLocationUnderstandingNonLocationConstraint> nonLocationConstraints =
                parseNonLocationConstraints(payload.get("nonLocationConstraints"), warnings);
        warnings.addAll(asStringList(payload.get("warnings")));

        return new AlertLocationUnderstandingResult(
                hasLocations,
                language,
                mainEvent,
                locations,
                nonLocationConstraints,
                warnings);
    }

    private AlertLocationUnderstandingMainEvent parseMainEvent(Map<String, Object> payload, List<String> warnings) {
        if (payload == null) {
            return AlertLocationUnderstandingMainEvent.unknown();
        }
        AlertLocationMainEventIntent eventIntent = parseEnum(
                AlertLocationMainEventIntent.class,
                asString(payload.get("eventIntent")),
                AlertLocationMainEventIntent.UNKNOWN,
                "mainEvent.eventIntent",
                warnings);
        double confidence = clamp(asDouble(payload.get("confidence"), 0.0), "mainEvent.confidence", warnings);
        return new AlertLocationUnderstandingMainEvent(eventIntent, confidence);
    }

    private List<AlertLocationUnderstandingLocation> parseLocations(Object value, List<String> warnings) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        List<AlertLocationUnderstandingLocation> result = new ArrayList<>();
        int index = 0;
        for (Object item : collection) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                warnings.add("locations[" + index + "] is not an object and was ignored.");
                index++;
                continue;
            }
            Map<String, Object> map = stringKeyMap(rawMap);
            AlertLocationRole role = parseEnum(
                    AlertLocationRole.class,
                    asString(map.get("role")),
                    AlertLocationRole.GENERIC_LOCATION,
                    "locations[" + index + "].role",
                    warnings);
            AlertLocationRelation relation = parseEnum(
                    AlertLocationRelation.class,
                    asString(map.get("relationToMainEvent")),
                    AlertLocationRelation.UNKNOWN,
                    "locations[" + index + "].relationToMainEvent",
                    warnings);
            AlertLocationPolarity polarity = parseEnum(
                    AlertLocationPolarity.class,
                    asString(map.get("polarity")),
                    AlertLocationPolarity.INCLUDE,
                    "locations[" + index + "].polarity",
                    warnings);
            double confidence = clamp(asDouble(map.get("confidence"), 0.0),
                    "locations[" + index + "].confidence",
                    warnings);
            result.add(new AlertLocationUnderstandingLocation(
                    asString(map.get("rawText")),
                    asString(map.get("normalizedText")),
                    role,
                    relation,
                    Boolean.TRUE.equals(map.get("requiredCoverage")),
                    polarity,
                    asString(map.get("logicalGroup")),
                    confidence));
            index++;
        }
        return result;
    }

    private List<AlertLocationUnderstandingNonLocationConstraint> parseNonLocationConstraints(
            Object value,
            List<String> warnings) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        List<AlertLocationUnderstandingNonLocationConstraint> result = new ArrayList<>();
        int index = 0;
        for (Object item : collection) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                warnings.add("nonLocationConstraints[" + index + "] is not an object and was ignored.");
                index++;
                continue;
            }
            Map<String, Object> map = stringKeyMap(rawMap);
            AlertLocationNonLocationConstraintType type = parseEnum(
                    AlertLocationNonLocationConstraintType.class,
                    asString(map.get("type")),
                    AlertLocationNonLocationConstraintType.UNKNOWN,
                    "nonLocationConstraints[" + index + "].type",
                    warnings);
            AlertJourneyReferenceKind journeyReferenceKind = parseEnum(
                    AlertJourneyReferenceKind.class,
                    asString(map.get("kind")),
                    null,
                    "nonLocationConstraints[" + index + "].kind",
                    warnings);
            result.add(new AlertLocationUnderstandingNonLocationConstraint(
                    type,
                    asString(map.get("rawText")),
                    journeyReferenceKind,
                    asString(map.get("normalizedValue")),
                    asStringList(map.get("normalizedValues")),
                    parseEnum(
                            AlertJourneyReferenceValueCombination.class,
                            asString(map.get("valueCombination")),
                            AlertJourneyReferenceValueCombination.SINGLE,
                            "nonLocationConstraints[" + index + "].valueCombination",
                            warnings),
                    !map.containsKey("requiredCoverage") || Boolean.TRUE.equals(map.get("requiredCoverage")),
                    clamp(asDouble(map.get("confidence"), 0.0),
                            "nonLocationConstraints[" + index + "].confidence",
                            warnings)));
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

    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
            return trimmed.substring(firstLineEnd + 1, lastFence).trim();
        }
        return trimmed;
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
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
            AlertLocationUnderstandingResult result,
            List<String> warnings,
            boolean parsed) {
    }
}
