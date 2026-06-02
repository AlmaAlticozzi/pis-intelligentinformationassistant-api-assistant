package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class SimpleAlertLocationMentionExtractor {

    private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    private static final List<Rule> RULES = List.of(
            new Rule(Pattern.compile("\\bparte\\s+da\\s+(.+)$", PATTERN_FLAGS),
                    AlertLocationSemanticRole.DEPARTURE_EVENT_STOP_POINT,
                    0.90),
            new Rule(Pattern.compile("\\barriva\\s+a\\s+(.+)$", PATTERN_FLAGS),
                    AlertLocationSemanticRole.ARRIVAL_EVENT_STOP_POINT,
                    0.90),
            new Rule(Pattern.compile("\\btransita\\s+a\\s+(.+)$", PATTERN_FLAGS),
                    AlertLocationSemanticRole.NEXT_CALL_STOP_POINT,
                    0.88),
            new Rule(Pattern.compile("\\btransiter[aà]\\s+a\\s+(.+)$", PATTERN_FLAGS),
                    AlertLocationSemanticRole.NEXT_CALL_STOP_POINT,
                    0.88),
            new Rule(Pattern.compile("\\bpassa\\s+da\\s+(.+)$", PATTERN_FLAGS),
                    AlertLocationSemanticRole.NEXT_TRANSIT_STOP_POINT,
                    0.82));

    private static final Pattern GENERIC_AT_PATTERN = Pattern.compile("\\ba\\s+(.+)$", PATTERN_FLAGS);
    private static final Pattern PLATFORM_BOUNDARY_PATTERN = Pattern.compile(
            "\\s+(?:(?:sul(?:la)?|al(?:la)?|in|dal(?:la)?|del(?:la)?|di)\\s+)?"
                    + "(binario|platform|track|quay|banchina|marciapiede|tronco|piattaforma)\\b",
            PATTERN_FLAGS);
    private static final Pattern TRAILING_LOCATION_CLAUSE_PATTERN = Pattern.compile(
            "\\s+(?:parte|arriva|transita|ferma|subisce)\\b.*$",
            PATTERN_FLAGS);
    private static final Pattern ALTERNATIVE_LOCATION_PATTERN = Pattern.compile("\\s+o\\s+", PATTERN_FLAGS);
    private static final List<String> TRAILING_BOUNDARIES = List.of(
            " quando ",
            " se ",
            " con ",
            " entro ",
            " dopo ",
            " prima ",
            " nel ",
            " nella ",
            " per ");

    public AlertLocationExtractionResult extract(String prompt) {
        System.out.println("[IIA][LOCATION_EXTRACTOR] prompt=" + prompt);

        if (prompt == null || prompt.isBlank()) {
            return log(AlertLocationExtractionResult.empty());
        }

        Optional<AlertLocationMention> explicitMention = RULES.stream()
                .map(rule -> rule.extract(prompt))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        if (explicitMention.isPresent()) {
            return log(new AlertLocationExtractionResult(true, List.of(explicitMention.get()), List.of()));
        }

        List<AlertLocationMention> genericMentions = extractGenericAtMentions(prompt);
        if (!genericMentions.isEmpty()) {
            return log(new AlertLocationExtractionResult(
                    true,
                    genericMentions,
                    List.of("Generic location pattern matched by simple deterministic extractor.")));
        }

        return log(AlertLocationExtractionResult.empty());
    }

    private List<AlertLocationMention> extractGenericAtMentions(String prompt) {
        Matcher matcher = GENERIC_AT_PATTERN.matcher(prompt);
        if (!matcher.find()) {
            return List.of();
        }

        String cleanedText = cleanLocationText(matcher.group(1));
        if (cleanedText.isBlank()) {
            return List.of();
        }
        return ALTERNATIVE_LOCATION_PATTERN.splitAsStream(cleanedText)
                .map(String::trim)
                .filter(rawText -> !rawText.isBlank() && startsLikeLocationName(rawText))
                .map(rawText -> new AlertLocationMention(rawText, AlertLocationSemanticRole.GENERIC_STOP_POINT, 0.55))
                .toList();
    }

    private boolean startsLikeLocationName(String rawText) {
        int firstCodePoint = rawText.codePointAt(0);
        return Character.isUpperCase(firstCodePoint) || Character.isDigit(firstCodePoint);
    }

    private AlertLocationExtractionResult log(AlertLocationExtractionResult result) {
        System.out.println("[IIA][LOCATION_EXTRACTOR] mentions=" + result.mentions().size());
        return result;
    }

    private static String cleanLocationText(String value) {
        String rawMention = value == null ? "" : value.trim();
        String cleaned = rawMention;
        Matcher platformBoundaryMatcher = PLATFORM_BOUNDARY_PATTERN.matcher(cleaned);
        String boundaryToken = null;
        if (platformBoundaryMatcher.find()) {
            boundaryToken = platformBoundaryMatcher.group(1);
            cleaned = cleaned.substring(0, platformBoundaryMatcher.start());
        }
        for (String boundary : TRAILING_BOUNDARIES) {
            int index = cleaned.toLowerCase(Locale.ROOT).indexOf(boundary);
            if (index >= 0) {
                cleaned = cleaned.substring(0, index);
            }
        }
        cleaned = TRAILING_LOCATION_CLAUSE_PATTERN.matcher(cleaned).replaceFirst("");
        cleaned = cleaned.replaceAll("[,.;:!?]+$", "").trim();
        if (boundaryToken != null) {
            System.out.println("[IIA][ALERT_VERIFY][LOCATION_BOUNDARY] raw mention=" + rawMention
                    + " cleaned mention=" + cleaned
                    + " detected boundary token=" + boundaryToken);
        }
        return cleaned;
    }

    private record Rule(Pattern pattern, AlertLocationSemanticRole semanticRole, double confidence) {
        Optional<AlertLocationMention> extract(String prompt) {
            Matcher matcher = pattern.matcher(prompt);
            if (!matcher.find()) {
                return Optional.empty();
            }
            String rawText = cleanLocationText(matcher.group(1));
            if (rawText.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new AlertLocationMention(rawText, semanticRole, confidence));
        }
    }
}
