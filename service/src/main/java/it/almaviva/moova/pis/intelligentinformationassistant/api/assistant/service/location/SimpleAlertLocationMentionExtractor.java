package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
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
                    0.82),
            new Rule(Pattern.compile("\\barrives?\\s+at\\s+(.+)$", PATTERN_FLAGS),
                    AlertLocationSemanticRole.ARRIVAL_EVENT_STOP_POINT,
                    0.90),
            new Rule(Pattern.compile("\\bdeparts?\\s+from\\s+(.+)$", PATTERN_FLAGS),
                    AlertLocationSemanticRole.DEPARTURE_EVENT_STOP_POINT,
                    0.90));

    private static final Pattern GENERIC_AT_PATTERN = Pattern.compile("\\ba\\s+(.+)$", PATTERN_FLAGS);
    private static final Pattern EXCLUDED_LOCATION_PATTERN = Pattern.compile(
            "\\b(?:(?:localit[a\\x{00E0}]|fermata|stazione)\\s+divers[ao]\\s+da"
                    + "|divers[ao]\\s+da"
                    + "|non\\s+in\\s+partenza\\s+da"
                    + "|non\\s+da"
                    + "|tranne|eccetto|esclus[ao])\\s+(.+)$",
            PATTERN_FLAGS);
    private static final Pattern PLATFORM_BOUNDARY_PATTERN = Pattern.compile(
            "(?:^|\\s+)(?:(?:sul(?:la)?|al(?:la)?|in|dal(?:la)?|del(?:la)?|di|da|on|from|at|de|d['\u2019]|une?|una|un|a|einem?|einer|von)\\s+)*"
                    + "(binario|platform|track|quay|banchina|marciapiede|tronco|piattaforma|voie|quai|v[i\u00eda]a|anden|and\u00e9n|gleis|bahnsteig)\\b",
            PATTERN_FLAGS);
    private static final Pattern TRAILING_PLATFORM_CONTEXT_PATTERN = Pattern.compile(
            "\\s+(?:(?:e|o)\\s+)?(?:che\\s+)?(?:non\\s+sia\\s+)?"
                    + "(?:in\\s+partenza\\s+)?(?:ne\\s*)?$",
            PATTERN_FLAGS);
    private static final Pattern TRAILING_LOCATION_CLAUSE_PATTERN = Pattern.compile(
            "\\s+(?:parte|arriva|transita|ferma|subisce|si\\s+verifica)\\b.*$",
            PATTERN_FLAGS);
    private static final Pattern ALTERNATIVE_LOCATION_PATTERN = Pattern.compile("\\s+o\\s+", PATTERN_FLAGS);
    private static final Pattern EXCLUDED_LOCATION_SEPARATOR_PATTERN = Pattern.compile("\\s*(?:,|\\be\\b|\\bo\\b)\\s*",
            PATTERN_FLAGS);
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
    private static final Pattern LOCATION_TOKEN_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Set<String> FUNCTION_WORDS = Set.of(
            "a", "al", "alla", "da", "dal", "dalla", "de", "di", "del", "della", "in", "un", "uno", "una",
            "at", "from", "on", "the", "an", "of",
            "d", "du", "des", "une", "la", "le", "el",
            "von", "einem", "einer", "ein", "eine", "der", "die", "das");

    public AlertLocationExtractionResult extract(String prompt) {
        System.out.println("[IIA][LOCATION_EXTRACTOR] prompt=" + prompt);

        if (prompt == null || prompt.isBlank()) {
            return log(AlertLocationExtractionResult.empty());
        }

        List<AlertLocationMention> excludedMentions = extractExcludedMentions(prompt);
        if (!excludedMentions.isEmpty()) {
            return log(new AlertLocationExtractionResult(
                    true,
                    excludedMentions,
                    List.of("NEGATED/EXCLUDED location pattern matched by simple deterministic extractor.")));
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

        CleanedLocation cleanedLocation = cleanLocationText(matcher.group(1));
        String cleanedText = cleanedLocation.text();
        if (!isMeaningfulLocationCandidate(cleanedLocation)) {
            return List.of();
        }
        return ALTERNATIVE_LOCATION_PATTERN.splitAsStream(cleanedText)
                .map(String::trim)
                .filter(rawText -> !rawText.isBlank() && startsLikeLocationName(rawText))
                .map(rawText -> new AlertLocationMention(rawText, AlertLocationSemanticRole.GENERIC_STOP_POINT, 0.55))
                .toList();
    }

    private List<AlertLocationMention> extractExcludedMentions(String prompt) {
        Matcher matcher = EXCLUDED_LOCATION_PATTERN.matcher(prompt);
        if (!matcher.find()) {
            return List.of();
        }
        String rawMention = matcher.group(1);
        CleanedLocation cleanedLocation = cleanLocationText(rawMention);
        String cleanedText = cleanedLocation.text();
        System.out.println("[IIA][LOCATION_EXTRACTOR] location pattern type=NEGATED/EXCLUDED"
                + " raw mention=" + rawMention
                + " cleaned mention=" + cleanedText);
        if (!isMeaningfulLocationCandidate(cleanedLocation)) {
            return List.of();
        }
        return EXCLUDED_LOCATION_SEPARATOR_PATTERN.splitAsStream(cleanedText)
                .map(String::trim)
                .filter(rawText -> !rawText.isBlank() && startsLikeLocationName(rawText))
                .map(rawText -> new AlertLocationMention(rawText, AlertLocationSemanticRole.GENERIC_STOP_POINT, 0.75))
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

    private static CleanedLocation cleanLocationText(String value) {
        String rawMention = value == null ? "" : value.trim();
        String cleaned = rawMention;
        Matcher platformBoundaryMatcher = PLATFORM_BOUNDARY_PATTERN.matcher(cleaned);
        String boundaryToken = null;
        if (platformBoundaryMatcher.find()) {
            boundaryToken = platformBoundaryMatcher.group(1);
            cleaned = cleaned.substring(0, platformBoundaryMatcher.start());
            cleaned = TRAILING_PLATFORM_CONTEXT_PATTERN.matcher(cleaned).replaceFirst("");
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
        return new CleanedLocation(rawMention, cleaned, boundaryToken);
    }

    private static boolean isMeaningfulLocationCandidate(CleanedLocation location) {
        String cleaned = location.text();
        if (cleaned.isBlank()) {
            return discard(location, "empty after normalization");
        }
        if (!location.boundaryDetected()) {
            return true;
        }
        Matcher matcher = LOCATION_TOKEN_PATTERN.matcher(cleaned);
        boolean hasInformativeToken = false;
        while (matcher.find()) {
            String token = matcher.group();
            String lowered = token.toLowerCase(Locale.ROOT);
            boolean acronymOrCode = token.codePoints().anyMatch(Character::isDigit)
                    || token.length() >= 2 && token.equals(token.toUpperCase(Locale.ROOT));
            if (!FUNCTION_WORDS.contains(lowered) && (token.length() >= 3 || acronymOrCode)) {
                hasInformativeToken = true;
                break;
            }
        }
        return hasInformativeToken || discard(location, "no informative location token after platform boundary");
    }

    private static boolean discard(CleanedLocation location, String reason) {
        System.out.println("[IIA][ALERT_VERIFY][LOCATION_BOUNDARY] discarded non-location candidate raw="
                + location.raw() + " cleaned=" + location.text() + " reason=" + reason);
        return false;
    }

    private record Rule(Pattern pattern, AlertLocationSemanticRole semanticRole, double confidence) {
        Optional<AlertLocationMention> extract(String prompt) {
            Matcher matcher = pattern.matcher(prompt);
            if (!matcher.find()) {
                return Optional.empty();
            }
            CleanedLocation cleanedLocation = cleanLocationText(matcher.group(1));
            if (!isMeaningfulLocationCandidate(cleanedLocation)) {
                return Optional.empty();
            }
            return Optional.of(new AlertLocationMention(cleanedLocation.text(), semanticRole, confidence));
        }
    }

    private record CleanedLocation(String raw, String text, String boundaryToken) {
        boolean boundaryDetected() {
            return boundaryToken != null;
        }
    }
}
