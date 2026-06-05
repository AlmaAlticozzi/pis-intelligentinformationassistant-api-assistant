package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertCancelledCallHintsExtractor {

    private static final Pattern CANCELLED_CALL = Pattern.compile(
            "\\b(fermat[ae]\\s+soppress\\w*|fermat[ae]\\s+cancellat\\w*|fermat[ae]\\s+saltat\\w*|salta(?:no)?(?:\\s+la\\s+fermata)?|skipped\\s+stop|cancelled\\s+stop|suppressed\\s+stop|cancelled\\s+call|next\\s+cancelled\\s+call|stop\\s+skipped)\\b");
    private static final Pattern NEGATIVE = Pattern.compile(
            "\\b(senza|non|without|not)\\b.{0,35}\\b(fermat[ae]\\s+soppress\\w*|fermat[ae]\\s+cancellat\\w*|salta(?:no)?\\s+la\\s+fermata|skipped\\s+stop|cancelled\\s+stop|suppressed\\s+stop)\\b");
    private static final Pattern SPECIFIC_STOP = Pattern.compile(
            "\\b(?:fermat[ae]\\s+(?:soppress[ae]|cancellat[ae]|saltat[ae])|salta(?:no)?(?:\\s+(?:la\\s+)?fermata)?|skips\\s+stop|cancelled\\s+stop|suppressed\\s+stop)\\s+([a-z0-9][a-z0-9 .'-]*(?:\\s+[a-z0-9][a-z0-9 .'-]*){0,5})\\b");

    public ScheduledAlertCancelledCallHints extract(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null || normalized.isBlank()) {
            return ScheduledAlertCancelledCallHints.empty();
        }
        Matcher matcher = CANCELLED_CALL.matcher(normalized);
        if (!matcher.find()) {
            return ScheduledAlertCancelledCallHints.empty();
        }

        ScheduledAlertCancelledCallConstraint.Polarity polarity = NEGATIVE.matcher(normalized).find()
                ? ScheduledAlertCancelledCallConstraint.Polarity.EXCLUDE
                : ScheduledAlertCancelledCallConstraint.Polarity.INCLUDE;
        Matcher specific = SPECIFIC_STOP.matcher(normalized);
        boolean hasSpecific = specific.find();
        String stop = hasSpecific ? specific.group(1).trim() : "";
        ScheduledAlertCancelledCallConstraint constraint = new ScheduledAlertCancelledCallConstraint(
                rawText(prompt, matcher),
                hasSpecific,
                stop,
                polarity,
                !hasSpecific,
                0.9);
        return new ScheduledAlertCancelledCallHints(true, List.of(constraint), List.of());
    }

    public static boolean containsCancelledCallWording(String prompt) {
        String normalized = normalizeStatic(prompt);
        return normalized != null && CANCELLED_CALL.matcher(normalized).find();
    }

    private String normalize(String value) {
        return normalizeStatic(value);
    }

    private static String normalizeStatic(String value) {
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
