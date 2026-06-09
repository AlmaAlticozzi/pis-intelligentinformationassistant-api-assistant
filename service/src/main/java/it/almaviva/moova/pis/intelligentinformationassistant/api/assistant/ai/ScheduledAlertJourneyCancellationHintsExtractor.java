package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertJourneyCancellationHintsExtractor {

    private static final Pattern NEGATIVE = Pattern.compile(
            "\\b(non|senza|without|not)\\b.{0,30}\\b(cancellat\\w*|soppress\\w*|cancellazione|cancelled|cancellation|suppressed)\\b");
    private static final Pattern ARRIVAL_ONLY = Pattern.compile(
            "\\b(soppressione\\s+in\\s+arrivo|soppress\\w+\\s+in\\s+arrivo|cancellazione\\s+in\\s+arrivo|cancellat\\w+\\s+in\\s+arrivo|arrival\\s+cancellation|arrival\\s+suppressed|arrival\\s+cancelled|arrival-only\\s+cancellation|cancelled\\s+on\\s+arrival)\\b");
    private static final Pattern DEPARTURE_ONLY = Pattern.compile(
            "\\b(soppressione\\s+in\\s+partenza|soppress\\w+\\s+in\\s+partenza|cancellazione\\s+in\\s+partenza|cancellat\\w+\\s+in\\s+partenza|departure\\s+cancellation|departure\\s+suppressed|departure\\s+cancelled|departure-only\\s+cancellation|cancelled\\s+on\\s+departure)\\b");
    private static final Pattern GENERIC = Pattern.compile(
            "\\b(corse?\\s+soppress\\w*|treni\\s+soppress\\w*|corse?\\s+cancellat\\w*|treni\\s+cancellat\\w*|cancelled\\s+journeys|suppressed\\s+journeys|cancelled\\s+trains|suppressed\\s+trains)\\b");

    public ScheduledAlertJourneyCancellationHints extract(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null || normalized.isBlank()
                || ScheduledAlertCancelledCallHintsExtractor.containsCancelledCallWording(prompt)) {
            return ScheduledAlertJourneyCancellationHints.empty();
        }
        ScheduledAlertJourneyCancellationConstraint.Polarity polarity = NEGATIVE.matcher(normalized).find()
                ? ScheduledAlertJourneyCancellationConstraint.Polarity.EXCLUDE
                : ScheduledAlertJourneyCancellationConstraint.Polarity.INCLUDE;
        if (ARRIVAL_ONLY.matcher(normalized).find()) {
            ScheduledAlertJourneyCancellationConstraint constraint = new ScheduledAlertJourneyCancellationConstraint(
                    prompt,
                    ScheduledAlertJourneyCancellationConstraint.CancellationIntent.ARRIVAL_JOURNEY_CANCELLATION,
                    ScheduledAlertJourneyCancellationConstraint.Direction.ARRIVAL,
                    polarity,
                    0.9);
            logDerivation(constraint);
            return hints(constraint);
        }
        if (DEPARTURE_ONLY.matcher(normalized).find()) {
            ScheduledAlertJourneyCancellationConstraint constraint = new ScheduledAlertJourneyCancellationConstraint(
                    prompt,
                    ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_JOURNEY_CANCELLATION,
                    ScheduledAlertJourneyCancellationConstraint.Direction.DEPARTURE,
                    polarity,
                    0.9);
            logDerivation(constraint);
            return hints(constraint);
        }
        if (GENERIC.matcher(normalized).find()) {
            ScheduledAlertJourneyCancellationConstraint constraint = new ScheduledAlertJourneyCancellationConstraint(
                    prompt,
                    ScheduledAlertJourneyCancellationConstraint.CancellationIntent.GENERIC_JOURNEY_CANCELLATION,
                    ScheduledAlertJourneyCancellationConstraint.Direction.UNSPECIFIED,
                    polarity,
                    0.9);
            logDerivation(constraint);
            return hints(constraint);
        }
        return ScheduledAlertJourneyCancellationHints.empty();
    }

    private void logDerivation(ScheduledAlertJourneyCancellationConstraint constraint) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][JOURNEY_CANCELLATION_DERIVATION] direction="
                + constraint.direction()
                + " intent=" + constraint.cancellationIntent()
                + " rawText=" + constraint.rawText());
    }

    private ScheduledAlertJourneyCancellationHints hints(ScheduledAlertJourneyCancellationConstraint constraint) {
        return new ScheduledAlertJourneyCancellationHints(true, List.of(constraint), List.of());
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
}
