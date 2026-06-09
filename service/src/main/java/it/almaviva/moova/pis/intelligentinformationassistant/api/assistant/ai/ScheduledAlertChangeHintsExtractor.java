package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertChangeHintsExtractor {

    private static final Pattern NEGATIVE = Pattern.compile(
            "\\b(non|senza|without|not)\\b.{0,30}\\b(cancellat\\w*|soppress\\w*|cancellazione|cambio|changed|cancelled|cancellation)\\b");
    private static final Pattern CHANGED_ORIGIN = Pattern.compile("\\b(cambio\\s+origine|origine\\s+cambiata|changed\\s+origin|origin\\s+changed)\\b");
    private static final Pattern CHANGED_DESTINATION = Pattern.compile("\\b(cambio\\s+destinazione|destinazione\\s+cambiata|changed\\s+destination|destination\\s+changed)\\b");
    private static final Pattern CHANGED_PATH = Pattern.compile("\\b(cambio\\s+percorso|percorso\\s+cambiato|itinerario\\s+cambiato|changed\\s+path|route\\s+changed|path\\s+changed)\\b");
    private static final Pattern EXTRA_JOURNEY = Pattern.compile("\\b(corsa\\s+straordinaria|corsa\\s+aggiuntiva|corsa\\s+extra|extra\\s+journey)\\b");
    private static final Pattern TOTAL_EXCLUSION = Pattern.compile("\\b(total\\s+exclusion|esclusione\\s+totale)\\b");
    private static final Pattern TIME_BASED_EXCLUSION = Pattern.compile("\\b(time[-\\s]?based\\s+exclusion|esclusione\\s+temporale|esclusione\\s+su\\s+fascia\\s+oraria)\\b");

    public ScheduledAlertChangeHints extract(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null || normalized.isBlank()) {
            return ScheduledAlertChangeHints.empty();
        }
        List<ScheduledAlertChangeConstraint> constraints = new ArrayList<>();
        ScheduledAlertChangeConstraint.Polarity polarity = NEGATIVE.matcher(normalized).find()
                ? ScheduledAlertChangeConstraint.Polarity.EXCLUDE
                : ScheduledAlertChangeConstraint.Polarity.INCLUDE;

        add(normalized, prompt, constraints, CHANGED_ORIGIN, ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_ORIGIN, polarity);
        add(normalized, prompt, constraints, CHANGED_DESTINATION, ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_DESTINATION, polarity);
        add(normalized, prompt, constraints, CHANGED_PATH, ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_PATH, polarity);
        add(normalized, prompt, constraints, EXTRA_JOURNEY, ScheduledAlertChangeConstraint.ChangeIntent.EXTRA_JOURNEY, polarity);
        add(normalized, prompt, constraints, TOTAL_EXCLUSION, ScheduledAlertChangeConstraint.ChangeIntent.TOTAL_EXCLUSION, polarity);
        add(normalized, prompt, constraints, TIME_BASED_EXCLUSION, ScheduledAlertChangeConstraint.ChangeIntent.TIME_BASED_EXCLUSION, polarity);

        return constraints.isEmpty()
                ? ScheduledAlertChangeHints.empty()
                : new ScheduledAlertChangeHints(true, List.copyOf(constraints), List.of());
    }

    private void add(
            String normalized,
            String prompt,
            List<ScheduledAlertChangeConstraint> constraints,
            Pattern pattern,
            ScheduledAlertChangeConstraint.ChangeIntent intent,
            ScheduledAlertChangeConstraint.Polarity polarity) {
        Matcher matcher = pattern.matcher(normalized);
        while (matcher.find()) {
            constraints.add(constraint(rawText(prompt, matcher), intent, polarity, 0.9));
        }
    }

    private ScheduledAlertChangeConstraint constraint(
            String rawText,
            ScheduledAlertChangeConstraint.ChangeIntent intent,
            ScheduledAlertChangeConstraint.Polarity polarity,
            double confidence) {
        return new ScheduledAlertChangeConstraint(
                rawText,
                intent,
                switch (intent) {
                    case ARRIVAL_CANCELLATION -> ScheduledAlertChangeConstraint.Direction.ARRIVAL;
                    case DEPARTURE_CANCELLATION -> ScheduledAlertChangeConstraint.Direction.DEPARTURE;
                    default -> ScheduledAlertChangeConstraint.Direction.UNSPECIFIED;
                },
                polarity,
                confidence);
    }

    private boolean overlapsExisting(String rawText, List<ScheduledAlertChangeConstraint> constraints) {
        String normalized = normalize(rawText);
        return normalized != null && constraints.stream()
                .map(ScheduledAlertChangeConstraint::rawText)
                .map(this::normalize)
                .anyMatch(existing -> existing != null && (existing.contains(normalized) || normalized.contains(existing)));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String rawText(String originalPrompt, Matcher matcher) {
        if (originalPrompt == null || matcher.start() < 0 || matcher.end() > originalPrompt.length()) {
            return matcher.group();
        }
        return originalPrompt.substring(matcher.start(), matcher.end());
    }
}
