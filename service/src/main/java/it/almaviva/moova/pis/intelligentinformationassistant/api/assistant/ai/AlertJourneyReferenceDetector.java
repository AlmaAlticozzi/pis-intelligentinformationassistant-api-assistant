package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class AlertJourneyReferenceDetector {

    private static final String VALUE = "([\\p{Alnum}][\\p{Alnum}._/-]{0,31})";
    private static final String JOURNEY_NOUN =
            "(?:journey|service|train|trip|bus|corsa|treno|autobus)";
    private static final Pattern EXPLICIT_JOURNEY_NUMBER = Pattern.compile(
            "\\b" + JOURNEY_NOUN + "\\s+(?:number|no\\.?|num(?:ero)?)\\s+" + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_JOURNEY_NAME = Pattern.compile(
            "\\b" + JOURNEY_NOUN + "\\s+(?:number|no\\.?|num(?:ero)?\\s+)?" + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_LINE = Pattern.compile(
            "\\b(?:on\\s+line|line|linea|ligne|linee|lineas)\\s+" + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_SERVICE_CATEGORY = Pattern.compile(
            "\\b(?:service\\s+category|category|categoria(?:\\s+servizio)?|categoria\\s+di\\s+servizio)\\s+"
                    + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EXPLICIT_OPERATOR = Pattern.compile(
            "\\b(?:transport\\s+operator|operator|operated\\s+by|operatore(?:\\s+di\\s+trasporto)?|gestore|esercente)\\s+"
                    + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern UNQUALIFIED_AFTER_JOURNEY = Pattern.compile(
            "\\b" + JOURNEY_NOUN + "\\s+" + VALUE + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern UNQUALIFIED_BEFORE_JOURNEY = Pattern.compile(
            "\\b" + VALUE + "\\s+" + JOURNEY_NOUN + "\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public Optional<AlertJourneyReferenceIntent> detect(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        NormalizedText normalized = normalize(text);
        List<Candidate> candidates = collectCandidates(normalized);
        List<ResolvedCandidate> resolved = resolveCandidates(candidates);
        logResult(resolved);
        return resolved.stream()
                .filter(ResolvedCandidate::accepted)
                .min(Comparator.comparingInt(candidate -> precedence(candidate.candidate().kind())))
                .map(ResolvedCandidate::candidate)
                .map(candidate -> new AlertJourneyReferenceIntent(
                        candidate.kind(),
                        candidate.rawText(),
                        candidate.normalizedValue(),
                        true,
                        candidate.confidence()));
    }

    private List<Candidate> collectCandidates(NormalizedText normalized) {
        List<Candidate> candidates = new ArrayList<>();
        addMatches(candidates, normalized, EXPLICIT_JOURNEY_NUMBER,
                AlertJourneyReferenceKind.JOURNEY_NAME, 1, true, 0.94);
        addMatches(candidates, normalized, EXPLICIT_LINE,
                AlertJourneyReferenceKind.LINE, 1, true, 0.94);
        addMatches(candidates, normalized, EXPLICIT_SERVICE_CATEGORY,
                AlertJourneyReferenceKind.SERVICE_CATEGORY, 1, true, 0.94);
        addMatches(candidates, normalized, EXPLICIT_OPERATOR,
                AlertJourneyReferenceKind.TRANSPORT_OPERATOR, 1, true, 0.94);
        addMatches(candidates, normalized, EXPLICIT_JOURNEY_NAME,
                AlertJourneyReferenceKind.JOURNEY_NAME, 1, true, 0.90);
        addMatches(candidates, normalized, UNQUALIFIED_AFTER_JOURNEY,
                AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, 1, false, 0.88);
        addMatches(candidates, normalized, UNQUALIFIED_BEFORE_JOURNEY,
                AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, 1, false, 0.86);
        return candidates;
    }

    private void addMatches(
            List<Candidate> candidates,
            NormalizedText normalized,
            Pattern pattern,
            AlertJourneyReferenceKind kind,
            int valueGroup,
            boolean explicit,
            double confidence) {
        Matcher matcher = pattern.matcher(normalized.text());
        while (matcher.find()) {
            String value = cleanValue(matcher.group(valueGroup));
            String rawValue = normalized.originalByToken().getOrDefault(value.toLowerCase(Locale.ROOT), value);
            String rawText = normalized.originalSpan(matcher.start(), matcher.end());
            candidates.add(new Candidate(
                    kind,
                    cleanValue(rawText).isBlank() ? rawValue : cleanValue(rawText),
                    value.toUpperCase(Locale.ROOT),
                    matcher.start(),
                    matcher.end(),
                    matcher.start(valueGroup),
                    matcher.end(valueGroup),
                    explicit,
                    confidence));
        }
    }

    private List<ResolvedCandidate> resolveCandidates(List<Candidate> candidates) {
        List<ResolvedCandidate> resolved = new ArrayList<>();
        for (Candidate candidate : candidates) {
            String reason = rejectionReason(candidate, candidates);
            ResolvedCandidate item = new ResolvedCandidate(candidate, reason == null, reason);
            resolved.add(item);
            logCandidate(item);
        }
        return resolved.stream()
                .filter(ResolvedCandidate::accepted)
                .sorted(Comparator
                        .comparingInt((ResolvedCandidate item) -> precedence(item.candidate().kind()))
                        .thenComparingInt(item -> item.candidate().start()))
                .collect(ArrayList::new, this::addIfNotDuplicate, ArrayList::addAll);
    }

    private void addIfNotDuplicate(List<ResolvedCandidate> accepted, ResolvedCandidate next) {
        boolean duplicate = accepted.stream()
                .anyMatch(existing -> existing.candidate().kind() == next.candidate().kind()
                        && existing.candidate().normalizedValue().equals(next.candidate().normalizedValue()));
        if (!duplicate) {
            accepted.add(next);
        }
    }

    private String rejectionReason(Candidate candidate, List<Candidate> candidates) {
        if (isEmptySemanticValue(candidate.normalizedValue())) {
            return "empty-or-generic-value";
        }
        if (candidate.kind() == AlertJourneyReferenceKind.JOURNEY_NAME
                && !looksNumeric(candidate.normalizedValue())) {
            return "journey-name-without-number";
        }
        if (candidate.kind() != AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR) {
            return null;
        }
        if (!isDescriptorLike(candidate.rawText(), candidate.normalizedValue())) {
            return "generic-entity-without-descriptor";
        }
        if (looksNumeric(candidate.normalizedValue())) {
            return "numeric-value-is-journey-name";
        }
        if (overlapsAcceptedExplicit(candidate, candidates)) {
            return "overlaps-explicit-qualification";
        }
        if (sameJourneyPhraseHasExplicitQualifier(candidate, candidates)) {
            return "explicit-qualification-consumes-journey-phrase";
        }
        return null;
    }

    private boolean overlapsAcceptedExplicit(Candidate candidate, List<Candidate> candidates) {
        return candidates.stream()
                .filter(Candidate::explicit)
                .filter(explicit -> explicit.kind() != AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR)
                .filter(explicit -> rejectionReasonWithoutOverlap(explicit) == null)
                .anyMatch(explicit -> rangesOverlap(candidate.start(), candidate.end(), explicit.start(), explicit.end()));
    }

    private String rejectionReasonWithoutOverlap(Candidate candidate) {
        if (isEmptySemanticValue(candidate.normalizedValue())) {
            return "empty-or-generic-value";
        }
        if (candidate.kind() == AlertJourneyReferenceKind.JOURNEY_NAME
                && !looksNumeric(candidate.normalizedValue())) {
            return "journey-name-without-number";
        }
        return null;
    }

    private boolean sameJourneyPhraseHasExplicitQualifier(Candidate candidate, List<Candidate> candidates) {
        return candidates.stream()
                .filter(Candidate::explicit)
                .filter(explicit -> explicit.kind() != AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR)
                .filter(explicit -> rejectionReasonWithoutOverlap(explicit) == null)
                .anyMatch(explicit -> explicit.start() >= candidate.end()
                        && explicit.start() - candidate.end() <= 28);
    }

    private boolean rangesOverlap(int start, int end, int otherStart, int otherEnd) {
        return start < otherEnd && otherStart < end;
    }

    private int precedence(AlertJourneyReferenceKind kind) {
        return switch (kind) {
            case JOURNEY_NAME -> 1;
            case LINE -> 2;
            case SERVICE_CATEGORY -> 3;
            case TRANSPORT_OPERATOR -> 4;
            case UNQUALIFIED_DESCRIPTOR -> 5;
        };
    }

    private NormalizedText normalize(String value) {
        Map<String, String> originalByToken = new LinkedHashMap<>();
        String deaccented = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('’', '\'')
                .replace('`', '\'')
                .replaceAll("[,;:!?()\\[\\]{}]", " ")
                .replaceAll("['’]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
        for (String token : value.split("[\\s,;:!?()\\[\\]{}'’`]+")) {
            String normalizedToken = cleanValue(Normalizer.normalize(token, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase(Locale.ROOT));
            if (!normalizedToken.isBlank()) {
                originalByToken.putIfAbsent(normalizedToken, cleanValue(token));
            }
        }
        return new NormalizedText(deaccented, value, originalByToken);
    }

    private String cleanValue(String value) {
        return value == null ? "" : value.replaceAll("^[\"'`]+|[\"'`.]+$", "").trim();
    }

    private boolean looksNumeric(String value) {
        return value != null && value.matches("\\d+");
    }

    private boolean isEmptySemanticValue(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return isArticleOrDeterminer(normalized)
                || isGenericJourneyNoun(normalized)
                || isFunctionalToken(normalized)
                || isExplicitQualifierLabel(normalized);
    }

    private boolean isFunctionalToken(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return isArticleOrDeterminer(normalized)
                || isRelationWord(normalized)
                || isAuxiliaryOrStateWord(normalized)
                || isExplicitQualifierLabel(normalized);
    }

    private boolean isArticleOrDeterminer(String value) {
        return switch (value) {
            case "a", "an", "the", "un", "una", "uno", "la", "il", "lo", "le", "i", "gli" -> true;
            default -> false;
        };
    }

    private boolean isGenericJourneyNoun(String value) {
        return switch (value) {
            case "journey", "service", "train", "trip", "bus", "corsa", "treno", "autobus" -> true;
            default -> false;
        };
    }

    private boolean isRelationWord(String value) {
        return switch (value) {
            case "in", "da", "a", "from", "to", "of", "di", "della", "del", "dell", "con", "with",
                    "by", "on", "per", "for" -> true;
            default -> false;
        };
    }

    private boolean isAuxiliaryOrStateWord(String value) {
        return switch (value) {
            case "e", "è", "is", "are", "be", "ha", "has", "have", "parte", "parti", "partono",
                    "partenza", "partendo", "passa", "passano", "passera", "passerà", "transita",
                    "arrivo", "arriva", "arrivano", "arrival", "departure", "departs", "depart",
                    "departing", "passes", "pass", "passing", "arrives", "arrive", "arriving",
                    "cancelled", "canceled", "suppressed", "ritardo", "delay" -> true;
            default -> false;
        };
    }

    private boolean isExplicitQualifierLabel(String value) {
        return switch (value) {
            case "line", "linea", "ligne", "linee", "lineas", "category", "categoria",
                    "operator", "operatore", "gestore", "esercente", "transport" -> true;
            default -> false;
        };
    }

    private boolean isDescriptorLike(String rawText, String normalizedValue) {
        if (isEmptySemanticValue(normalizedValue) || normalizedValue.length() < 2) {
            return false;
        }
        String raw = rawText == null ? normalizedValue : rawText.trim();
        String normalizedRaw = normalizePhrase(raw);
        if (isGenericEntityPhrase(normalizedRaw)) {
            return false;
        }
        return normalizedValue.matches(".*\\d.*")
                || normalizedValue.matches("[A-Z][A-Z0-9._/-]{1,31}")
                || raw.matches("[A-Z0-9._/-]{2,}")
                || raw.matches("[A-Z][a-z]+[A-Z0-9._/-].*");
    }

    private boolean isGenericEntityPhrase(String normalizedRaw) {
        if (normalizedRaw == null || normalizedRaw.isBlank()) {
            return true;
        }
        String[] tokens = normalizedRaw.split("\\s+");
        boolean hasJourneyNoun = false;
        boolean hasDescriptor = false;
        for (String token : tokens) {
            if (isArticleOrDeterminer(token)) {
                continue;
            }
            if (isGenericJourneyNoun(token)) {
                hasJourneyNoun = true;
                continue;
            }
            hasDescriptor = true;
        }
        return hasJourneyNoun && !hasDescriptor;
    }

    private String normalizePhrase(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{Alnum}._/-]+", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void logCandidate(ResolvedCandidate resolved) {
        Candidate candidate = resolved.candidate();
        System.out.println("[IIA][ALERT_JOURNEY_REFERENCE][CANDIDATE]\n"
                + "kind=" + candidate.kind() + "\n"
                + "rawText=" + candidate.rawText() + "\n"
                + "normalizedValue=" + (resolved.accepted() ? candidate.normalizedValue() : "") + "\n"
                + "span=" + candidate.start() + ".." + candidate.end() + "\n"
                + "explicit=" + candidate.explicit() + "\n"
                + "accepted=" + resolved.accepted()
                + (resolved.reason() == null ? "" : "\nreason=" + resolved.reason()));
    }

    private void logResult(List<ResolvedCandidate> resolved) {
        String constraints = resolved.stream()
                .filter(ResolvedCandidate::accepted)
                .map(candidate -> candidate.candidate().kind() + ":" + candidate.candidate().normalizedValue())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        System.out.println("[IIA][ALERT_JOURNEY_REFERENCE][RESULT] constraints=[" + constraints + "]");
    }

    private record Candidate(
            AlertJourneyReferenceKind kind,
            String rawText,
            String normalizedValue,
            int start,
            int end,
            int valueStart,
            int valueEnd,
            boolean explicit,
            double confidence) {
    }

    private record ResolvedCandidate(Candidate candidate, boolean accepted, String reason) {
    }

    private record NormalizedText(String text, String originalText, Map<String, String> originalByToken) {
        private String originalSpan(int normalizedStart, int normalizedEnd) {
            // Normalization is length-preserving enough for compact diagnostics after punctuation folding is not guaranteed.
            // Fall back to the normalized span when direct original indexing is unsafe.
            if (normalizedStart >= 0 && normalizedEnd <= originalText.length()) {
                return originalText.substring(normalizedStart, normalizedEnd);
            }
            if (normalizedStart >= 0 && normalizedEnd <= text.length()) {
                return text.substring(normalizedStart, normalizedEnd);
            }
            return "";
        }
    }
}
