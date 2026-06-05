package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertPlatformConstraint(
        String rawText,
        Direction direction,
        PlatformIntent platformIntent,
        String platformValue,
        List<String> platformValues,
        Integer minValue,
        Integer maxValue,
        Integer numericValue,
        SourcePreference sourcePreference,
        double confidence) {

    public enum Direction {
        DEPARTURE,
        ARRIVAL,
        UNSPECIFIED
    }

    public enum PlatformIntent {
        EQUALS,
        IN,
        NOT_EQUALS,
        GREATER_THAN,
        GREATER_OR_EQUAL,
        LESS_THAN,
        LESS_OR_EQUAL,
        BETWEEN,
        EVEN,
        ODD,
        DOUBLE_DIGIT,
        HAS_LETTER_SUFFIX,
        MULTIPLE_OF,
        CHANGED,
        UNKNOWN
    }

    public enum SourcePreference {
        TIMETABLED,
        ACTUAL_OR_EFFECTIVE,
        UNSPECIFIED
    }
}
