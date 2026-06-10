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
    private static final Pattern ARRIVAL_DIRECTION = Pattern.compile(
            "\\b(soppressione\\s+in\\s+arrivo|soppress\\w+\\s+in\\s+arrivo|cancellazione\\s+in\\s+arrivo|cancellat\\w+\\s+in\\s+arrivo|arrival\\s+cancellation|arrival\\s+suppressed|arrival\\s+cancelled|cancelled\\s+on\\s+arrival|suppressed\\s+on\\s+arrival)\\b");
    private static final Pattern DEPARTURE_DIRECTION = Pattern.compile(
            "\\b(soppressione\\s+in\\s+partenza|soppress\\w+\\s+in\\s+partenza|cancellazione\\s+in\\s+partenza|cancellat\\w+\\s+in\\s+partenza|departure\\s+cancellation|departure\\s+suppressed|departure\\s+cancelled|cancelled\\s+on\\s+departure|suppressed\\s+on\\s+departure)\\b");
    private static final Pattern ARRIVAL_EXCLUSIVE = Pattern.compile(
            "\\b(solo\\s+[^.]{0,40}(arrivo|arrival)|arrival-only|only\\s+[^.]{0,40}(arrival|on\\s+arrival)|arrivo\\s+soltanto|arrival\\s+only)\\b");
    private static final Pattern DEPARTURE_EXCLUSIVE = Pattern.compile(
            "\\b(solo\\s+[^.]{0,40}(partenza|departure)|departure-only|only\\s+[^.]{0,40}(departure|on\\s+departure)|partenza\\s+soltanto|departure\\s+only)\\b");
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
        if (ARRIVAL_DIRECTION.matcher(normalized).find() || ARRIVAL_EXCLUSIVE.matcher(normalized).find()) {
            boolean exclusive = ARRIVAL_EXCLUSIVE.matcher(normalized).find();
            ScheduledAlertJourneyCancellationConstraint constraint = new ScheduledAlertJourneyCancellationConstraint(
                    prompt,
                    exclusive
                            ? ScheduledAlertJourneyCancellationConstraint.CancellationIntent.ARRIVAL_ONLY_JOURNEY_CANCELLATION
                            : ScheduledAlertJourneyCancellationConstraint.CancellationIntent.ARRIVAL_JOURNEY_CANCELLATION,
                    ScheduledAlertJourneyCancellationConstraint.Direction.ARRIVAL,
                    polarity,
                    0.9);
            logDerivation(constraint);
            return hints(constraint);
        }
        if (DEPARTURE_DIRECTION.matcher(normalized).find() || DEPARTURE_EXCLUSIVE.matcher(normalized).find()) {
            boolean exclusive = DEPARTURE_EXCLUSIVE.matcher(normalized).find();
            ScheduledAlertJourneyCancellationConstraint constraint = new ScheduledAlertJourneyCancellationConstraint(
                    prompt,
                    exclusive
                            ? ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_ONLY_JOURNEY_CANCELLATION
                            : ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_JOURNEY_CANCELLATION,
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
