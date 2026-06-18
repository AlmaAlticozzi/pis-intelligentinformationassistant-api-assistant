package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class AlertJourneyReferenceDetector {

    private static final String VALUE = "([\\p{Alnum}][\\p{Alnum}._/-]{0,31})";
    private static final Pattern EXPLICIT_JOURNEY_NAME = Pattern.compile(
            "\\b(?:journey|service|train|trip|corsa|treno)\\s+(?:number|no\\.?|num(?:ero)?\\s+)?"
                    + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_JOURNEY_NUMBER = Pattern.compile(
            "\\b(?:journey|service|train|trip|corsa|treno)\\s+(?:number|no\\.?|num(?:ero)?)\\s+"
                    + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_LINE = Pattern.compile(
            "\\b(?:line|linea|ligne|linee|lineas)\\s+" + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_SERVICE_CATEGORY = Pattern.compile(
            "\\b(?:service\\s+category|category|categoria(?:\\s+servizio)?|categoria\\s+di\\s+servizio)\\s+"
                    + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_OPERATOR = Pattern.compile(
            "\\b(?:operator|transport\\s+operator|operated\\s+by|operatore|gestore|esercente)\\s+"
                    + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern UNQUALIFIED_AFTER_JOURNEY = Pattern.compile(
            "\\b(?:journey|service|train|trip|corsa|treno)\\s+" + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern UNQUALIFIED_BEFORE_JOURNEY = Pattern.compile(
            "\\b" + VALUE + "\\s+(?:journey|service|train|trip|corsa|treno)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public Optional<AlertJourneyReferenceIntent> detect(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        NormalizedText normalized = normalize(text);

        Optional<AlertJourneyReferenceIntent> explicit = first(
                match(normalized, EXPLICIT_JOURNEY_NUMBER, AlertJourneyReferenceKind.JOURNEY_NAME, 1, 0.94),
                match(normalized, EXPLICIT_LINE, AlertJourneyReferenceKind.LINE, 1, 0.94),
                match(normalized, EXPLICIT_SERVICE_CATEGORY, AlertJourneyReferenceKind.SERVICE_CATEGORY, 1, 0.94),
                match(normalized, EXPLICIT_OPERATOR, AlertJourneyReferenceKind.TRANSPORT_OPERATOR, 1, 0.94));
        if (explicit.isPresent()) {
            return explicit;
        }

        Optional<AlertJourneyReferenceIntent> maybeJourneyName =
                match(normalized, EXPLICIT_JOURNEY_NAME, AlertJourneyReferenceKind.JOURNEY_NAME, 1, 0.90)
                        .filter(intent -> looksNumeric(intent.normalizedValue()));
        if (maybeJourneyName.isPresent()) {
            return maybeJourneyName;
        }

        Optional<AlertJourneyReferenceIntent> unqualified =
                match(normalized, UNQUALIFIED_AFTER_JOURNEY, AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, 1, 0.88)
                        .filter(intent -> isDescriptorLike(intent.rawText(), intent.normalizedValue()));
        if (unqualified.isPresent() && !looksNumeric(unqualified.get().normalizedValue())) {
            return unqualified;
        }
        return match(normalized, UNQUALIFIED_BEFORE_JOURNEY, AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, 1, 0.86)
                .filter(intent -> isDescriptorLike(intent.rawText(), intent.normalizedValue()))
                .filter(intent -> !looksNumeric(intent.normalizedValue()));
    }

    private Optional<AlertJourneyReferenceIntent> match(
            NormalizedText normalizedText,
            Pattern pattern,
            AlertJourneyReferenceKind kind,
            int valueGroup,
            double confidence) {
        Matcher matcher = pattern.matcher(normalizedText.text());
        if (!matcher.find()) {
            return Optional.empty();
        }
        String value = cleanValue(matcher.group(valueGroup));
        if (value.isBlank() || isFunctionalToken(value)) {
            return Optional.empty();
        }
        String raw = normalizedText.originalByToken().getOrDefault(value.toLowerCase(Locale.ROOT), value);
        return Optional.of(new AlertJourneyReferenceIntent(kind, raw, value.toUpperCase(Locale.ROOT), true, confidence));
    }

    @SafeVarargs
    private Optional<AlertJourneyReferenceIntent> first(Optional<AlertJourneyReferenceIntent>... values) {
        for (Optional<AlertJourneyReferenceIntent> value : values) {
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private NormalizedText normalize(String value) {
        Map<String, String> originalByToken = new java.util.LinkedHashMap<>();
        String deaccented = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('’', '\'')
                .replaceAll("[,;:!?()\\[\\]{}]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
        for (String token : value.split("[\\s,;:!?()\\[\\]{}]+")) {
            String normalizedToken = cleanValue(Normalizer.normalize(token, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase(Locale.ROOT));
            if (!normalizedToken.isBlank()) {
                originalByToken.putIfAbsent(normalizedToken, cleanValue(token));
            }
        }
        return new NormalizedText(deaccented, originalByToken);
    }

    private String cleanValue(String value) {
        return value == null ? "" : value.replaceAll("^[\"'`]+|[\"'`.]+$", "").trim();
    }

    private boolean looksNumeric(String value) {
        return value != null && value.matches("\\d+");
    }

    private boolean isFunctionalToken(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "in", "arrivo", "partenza", "arrival", "departure", "da", "a", "from", "to",
                    "una", "un", "the", "la", "il", "lo", "le", "of", "di", "della", "del" -> true;
            default -> false;
        };
    }

    private boolean isDescriptorLike(String rawText, String normalizedValue) {
        if (isFunctionalToken(normalizedValue) || normalizedValue == null || normalizedValue.length() < 2) {
            return false;
        }
        String normalized = normalizedValue.toLowerCase(Locale.ROOT);
        if (switch (normalized) {
            case "e", "è", "is", "are", "be", "parte", "parti", "partono", "partenza", "partendo",
                    "passa", "passano", "passera", "passerà", "transita", "arriva", "arrivano",
                    "departs", "depart", "departing", "passes", "pass", "passing", "arrives", "arrive", "arriving",
                    "cancelled", "canceled", "suppressed", "ritardo", "delay" -> true;
            default -> false;
        }) {
            return false;
        }
        String raw = rawText == null ? normalizedValue : rawText.trim();
        return normalizedValue.matches(".*\\d.*")
                || raw.matches("[A-Z0-9._/-]{2,}")
                || raw.matches("[A-Z][a-z]+[A-Z0-9._/-].*");
    }

    private record NormalizedText(String text, Map<String, String> originalByToken) {
    }
}
