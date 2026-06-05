package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertPlatformHintsExtractor {

    private static final String PLATFORM = "(?:binario|platform|track|quay|banchina|marciapiede)";
    private static final Pattern PLATFORM_CHANGE = Pattern.compile("\\b(?:cambio\\s+di\\s+binario|cambio\\s+binario|platform\\s+changed|platform\\s+change)\\b");
    private static final Pattern PLATFORM_BETWEEN = Pattern.compile("\\b" + PLATFORM + "\\s+(?:tra|between)\\s+(\\d+)\\s+(?:e|and)\\s+(\\d+)\\b");
    private static final Pattern PLATFORM_GREATER = Pattern.compile("\\b" + PLATFORM + "\\s+(?:maggiore\\s+di|superiore\\s+a|greater\\s+than|more\\s+than)\\s+(\\d+)\\b");
    private static final Pattern PLATFORM_LESS = Pattern.compile("\\b" + PLATFORM + "\\s+(?:minore\\s+di|inferiore\\s+a|less\\s+than|lower\\s+than)\\s+(\\d+)\\b");
    private static final Pattern PLATFORM_MULTIPLE = Pattern.compile("\\b" + PLATFORM + "\\s+(?:multiplo\\s+di|multiple\\s+of)\\s+(\\d+)\\b");
    private static final Pattern PLATFORM_EQUALS = Pattern.compile("\\b(?:(?:dal|al|da|a|from|at|on)\\s+)?" + PLATFORM + "\\s+(\\d+[a-z]?)\\b");
    private static final Pattern PLATFORM_EVEN = Pattern.compile("\\b" + PLATFORM + "\\s+(?:pari|even)\\b");
    private static final Pattern PLATFORM_ODD = Pattern.compile("\\b" + PLATFORM + "\\s+(?:dispari|odd)\\b");
    private static final Pattern PLATFORM_DOUBLE_DIGIT = Pattern.compile("\\b" + PLATFORM + "\\s+(?:a\\s+doppia\\s+cifra|double\\s+digit)\\b");
    private static final Pattern PLATFORM_LETTER = Pattern.compile("\\b" + PLATFORM + "\\s+(?:con\\s+(?:suffisso\\s+)?lettera|with\\s+(?:a\\s+)?letter(?:\\s+suffix)?)\\b");

    public ScheduledAlertPlatformHints extract(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null || normalized.isBlank()) {
            return ScheduledAlertPlatformHints.empty();
        }

        List<ScheduledAlertPlatformConstraint> constraints = new ArrayList<>();
        DirectionAndSource direction = directionAndSource(normalized);

        addChange(normalized, prompt, direction, constraints);
        addBetween(normalized, prompt, direction, constraints);
        addNumeric(normalized, prompt, direction, constraints, PLATFORM_GREATER, ScheduledAlertPlatformConstraint.PlatformIntent.GREATER_THAN);
        addNumeric(normalized, prompt, direction, constraints, PLATFORM_LESS, ScheduledAlertPlatformConstraint.PlatformIntent.LESS_THAN);
        addNumeric(normalized, prompt, direction, constraints, PLATFORM_MULTIPLE, ScheduledAlertPlatformConstraint.PlatformIntent.MULTIPLE_OF);
        addValueless(normalized, prompt, direction, constraints, PLATFORM_EVEN, ScheduledAlertPlatformConstraint.PlatformIntent.EVEN);
        addValueless(normalized, prompt, direction, constraints, PLATFORM_ODD, ScheduledAlertPlatformConstraint.PlatformIntent.ODD);
        addValueless(normalized, prompt, direction, constraints, PLATFORM_DOUBLE_DIGIT, ScheduledAlertPlatformConstraint.PlatformIntent.DOUBLE_DIGIT);
        addValueless(normalized, prompt, direction, constraints, PLATFORM_LETTER, ScheduledAlertPlatformConstraint.PlatformIntent.HAS_LETTER_SUFFIX);
        addEquals(normalized, prompt, direction, constraints);

        return constraints.isEmpty()
                ? ScheduledAlertPlatformHints.empty()
                : new ScheduledAlertPlatformHints(true, List.copyOf(constraints), List.of());
    }

    private void addChange(
            String normalized,
            String prompt,
            DirectionAndSource direction,
            List<ScheduledAlertPlatformConstraint> constraints) {
        Matcher matcher = PLATFORM_CHANGE.matcher(normalized);
        while (matcher.find()) {
            constraints.add(constraint(
                    rawText(prompt, matcher),
                    direction.direction(),
                    ScheduledAlertPlatformConstraint.PlatformIntent.CHANGED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    direction.sourcePreference(),
                    0.9));
        }
    }

    private void addBetween(
            String normalized,
            String prompt,
            DirectionAndSource direction,
            List<ScheduledAlertPlatformConstraint> constraints) {
        Matcher matcher = PLATFORM_BETWEEN.matcher(normalized);
        while (matcher.find()) {
            constraints.add(constraint(
                    rawText(prompt, matcher),
                    direction.direction(),
                    ScheduledAlertPlatformConstraint.PlatformIntent.BETWEEN,
                    null,
                    null,
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    null,
                    direction.sourcePreference(),
                    0.9));
        }
    }

    private void addNumeric(
            String normalized,
            String prompt,
            DirectionAndSource direction,
            List<ScheduledAlertPlatformConstraint> constraints,
            Pattern pattern,
            ScheduledAlertPlatformConstraint.PlatformIntent intent) {
        Matcher matcher = pattern.matcher(normalized);
        while (matcher.find()) {
            constraints.add(constraint(
                    rawText(prompt, matcher),
                    direction.direction(),
                    intent,
                    null,
                    null,
                    null,
                    null,
                    Integer.parseInt(matcher.group(1)),
                    direction.sourcePreference(),
                    0.9));
        }
    }

    private void addValueless(
            String normalized,
            String prompt,
            DirectionAndSource direction,
            List<ScheduledAlertPlatformConstraint> constraints,
            Pattern pattern,
            ScheduledAlertPlatformConstraint.PlatformIntent intent) {
        Matcher matcher = pattern.matcher(normalized);
        while (matcher.find()) {
            constraints.add(constraint(
                    rawText(prompt, matcher),
                    direction.direction(),
                    intent,
                    null,
                    null,
                    null,
                    null,
                    null,
                    direction.sourcePreference(),
                    0.9));
        }
    }

    private void addEquals(
            String normalized,
            String prompt,
            DirectionAndSource direction,
            List<ScheduledAlertPlatformConstraint> constraints) {
        Matcher matcher = PLATFORM_EQUALS.matcher(normalized);
        while (matcher.find()) {
            if (overlapsExisting(rawText(prompt, matcher), constraints)) {
                continue;
            }
            constraints.add(constraint(
                    rawText(prompt, matcher),
                    direction.direction(),
                    ScheduledAlertPlatformConstraint.PlatformIntent.EQUALS,
                    matcher.group(1),
                    List.of(),
                    null,
                    null,
                    null,
                    direction.sourcePreference(),
                    0.85));
        }
    }

    private boolean overlapsExisting(String rawText, List<ScheduledAlertPlatformConstraint> constraints) {
        if (rawText == null) {
            return false;
        }
        String normalized = normalize(rawText);
        return constraints.stream().anyMatch(existing -> {
            String existingText = normalize(existing.rawText());
            return existingText != null && (existingText.contains(normalized) || normalized.contains(existingText));
        });
    }

    private ScheduledAlertPlatformConstraint constraint(
            String rawText,
            ScheduledAlertPlatformConstraint.Direction direction,
            ScheduledAlertPlatformConstraint.PlatformIntent platformIntent,
            String platformValue,
            List<String> platformValues,
            Integer minValue,
            Integer maxValue,
            Integer numericValue,
            ScheduledAlertPlatformConstraint.SourcePreference sourcePreference,
            double confidence) {
        return new ScheduledAlertPlatformConstraint(
                rawText,
                direction,
                platformIntent,
                platformValue,
                platformValues == null ? List.of() : platformValues,
                minValue,
                maxValue,
                numericValue,
                sourcePreference,
                confidence);
    }

    private DirectionAndSource directionAndSource(String normalized) {
        ScheduledAlertPlatformConstraint.Direction direction = ScheduledAlertPlatformConstraint.Direction.UNSPECIFIED;
        if (normalized.matches(".*\\b(partenza|partenze|parte|partono|depart|departure|departing|starts?)\\b.*")) {
            direction = ScheduledAlertPlatformConstraint.Direction.DEPARTURE;
        } else if (normalized.matches(".*\\b(arrivo|arrivi|arriva|arrivano|arrival|arriving|arrives?)\\b.*")) {
            direction = ScheduledAlertPlatformConstraint.Direction.ARRIVAL;
        }
        if (normalized.matches(".*\\b(in\\s+partenza|departure)\\b.*")) {
            direction = ScheduledAlertPlatformConstraint.Direction.DEPARTURE;
        } else if (normalized.matches(".*\\b(in\\s+arrivo|arrival)\\b.*")) {
            direction = ScheduledAlertPlatformConstraint.Direction.ARRIVAL;
        }

        ScheduledAlertPlatformConstraint.SourcePreference source = ScheduledAlertPlatformConstraint.SourcePreference.UNSPECIFIED;
        if (normalized.matches(".*\\b(programmato|programmata|planned|scheduled|timetabled)\\b.*")) {
            source = ScheduledAlertPlatformConstraint.SourcePreference.TIMETABLED;
        } else if (normalized.matches(".*\\b(attuale|effettivo|effettiva|reale|actual|current|effective)\\b.*")) {
            source = ScheduledAlertPlatformConstraint.SourcePreference.ACTUAL_OR_EFFECTIVE;
        }
        return new DirectionAndSource(direction, source);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return decomposed.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private String rawText(String originalPrompt, Matcher matcher) {
        if (originalPrompt == null || matcher.start() < 0 || matcher.end() > originalPrompt.length()) {
            return matcher.group();
        }
        return originalPrompt.substring(matcher.start(), matcher.end());
    }

    private record DirectionAndSource(
            ScheduledAlertPlatformConstraint.Direction direction,
            ScheduledAlertPlatformConstraint.SourcePreference sourcePreference) {
    }
}
