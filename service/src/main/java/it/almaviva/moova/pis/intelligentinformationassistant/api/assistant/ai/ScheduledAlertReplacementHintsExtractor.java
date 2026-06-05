package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertReplacementHintsExtractor {

    private static final Pattern GENERIC_REPLACEMENT = Pattern.compile(
            "\\b(replacement\\s+service|substitute\\s+service|substitute\\s+journey|servizi?\\s+sostitutiv\\w*|cors[ae]\\s+sostitutiv\\w*|corse\\s+sostitutiv\\w*|mezzo\\s+sostitutiv\\w*|bus\\s+sostitutiv\\w*|navetta\\s+sostitutiv\\w*)\\b");
    private static final Pattern REPLACEMENT_OBJECT = Pattern.compile(
            "\\b(con\\s+replacement|ha\\s+replacement|replacement\\s+data|sostituzione\\s+esterna|replacement\\s+esterno|external\\s+replacement)\\b");
    private static final Pattern EXTERNAL_REPLACEMENT = Pattern.compile(
            "\\b(external\\s+replacement|replacement\\s+esterno|sostituzione\\s+esterna)\\b");
    private static final Pattern REPLACEMENT_STOP = Pattern.compile(
            "\\b(?:fermata\\s+sostitutiv\\w*|stop\\s+replacement|replacement\\s+stop|stop\\s+sostitutiv\\w*|sostituisce\\s+la\\s+fermata)\\s+([a-z0-9][a-z0-9 .'-]*(?:\\s+[a-z0-9][a-z0-9 .'-]*){0,5})\\b");
    private static final Pattern SOURCE_ROUTE = Pattern.compile(
            "\\b(tratta\\s+sostitutiv\\w*\\s+da\\s+.+\\s+a\\s+.+|percorso\\s+sostitutiv\\w*\\s+da\\s+.+\\s+a\\s+.+|sostituzione\\s+per\\s+la\\s+tratta\\s+da\\s+.+\\s+a\\s+.+|per\\s+la\\s+tratta\\s+che\\s+inizia\\s+a\\s+.+\\s+(?:fino\\s+)?a\\s+.+|replacement\\s+route\\s+from\\s+.+\\s+to\\s+.+)\\b");
    private static final Pattern NEGATIVE = Pattern.compile("\\b(senza|non|without|not)\\b.{0,35}\\b(replacement|sostitutiv|sostituzione)\\b");
    private static final Pattern DEPARTURE_TYPE = Pattern.compile("\\b(in\\s+partenza|departure\\s+replacement|replacement\\s+departure|sostituzione\\s+in\\s+partenza)\\b");
    private static final Pattern ARRIVAL_TYPE = Pattern.compile("\\b(in\\s+arrivo|arrival\\s+replacement|replacement\\s+arrival|sostituzione\\s+in\\s+arrivo)\\b");
    private static final Pattern BOTH_TYPE = Pattern.compile("\\b(arrivo\\s+e\\s+partenza|arrival\\s*/\\s*departure\\s+replacement)\\b");

    public ScheduledAlertReplacementHints extract(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null) {
            return ScheduledAlertReplacementHints.empty();
        }
        if (!containsReplacementWording(normalized)) {
            return ScheduledAlertReplacementHints.empty();
        }

        ScheduledAlertReplacementConstraint.Polarity polarity = NEGATIVE.matcher(normalized).find()
                ? ScheduledAlertReplacementConstraint.Polarity.EXCLUDE
                : ScheduledAlertReplacementConstraint.Polarity.INCLUDE;
        ScheduledAlertReplacementConstraint.ReplacementType replacementType = replacementType(normalized);

        if (SOURCE_ROUTE.matcher(normalized).find()) {
            return hints(new ScheduledAlertReplacementConstraint(
                    "replacement source route",
                    ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_SOURCE_ROUTE,
                    replacementType,
                    false,
                    null,
                    polarity,
                    "Replacement source route start/end stop points are not supported by the Scheduled ServiceData snapshot catalog.",
                    0.9));
        }

        Matcher stopMatcher = REPLACEMENT_STOP.matcher(normalized);
        if (stopMatcher.find()) {
            String rawStop = cleanupStop(stopMatcher.group(1));
            boolean hasType = replacementType != ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED;
            return hints(new ScheduledAlertReplacementConstraint(
                    "replacement stop",
                    hasType
                            ? ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP_WITH_TYPE
                            : ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP,
                    replacementType,
                    true,
                    rawStop,
                    polarity,
                    null,
                    0.9));
        }

        if (replacementType != ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED) {
            return hints(new ScheduledAlertReplacementConstraint(
                    "replacement type",
                    ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_TYPE,
                    replacementType,
                    false,
                    null,
                    polarity,
                    null,
                    0.85));
        }

        if (EXTERNAL_REPLACEMENT.matcher(normalized).find()) {
            return hints(new ScheduledAlertReplacementConstraint(
                    "external replacement",
                    ScheduledAlertReplacementConstraint.ReplacementIntent.HAS_EXTERNAL_REPLACEMENT_OBJECT,
                    replacementType,
                    false,
                    null,
                    polarity,
                    null,
                    0.85));
        }

        if (REPLACEMENT_OBJECT.matcher(normalized).find()) {
            return hints(new ScheduledAlertReplacementConstraint(
                    "replacement object",
                    ScheduledAlertReplacementConstraint.ReplacementIntent.HAS_REPLACEMENT_OBJECT,
                    replacementType,
                    false,
                    null,
                    polarity,
                    null,
                    0.85));
        }

        if (GENERIC_REPLACEMENT.matcher(normalized).find()) {
            return hints(new ScheduledAlertReplacementConstraint(
                    "replacement service",
                    ScheduledAlertReplacementConstraint.ReplacementIntent.GENERIC_REPLACEMENT_SERVICE,
                    replacementType,
                    false,
                    null,
                    polarity,
                    null,
                    0.85));
        }

        return ScheduledAlertReplacementHints.empty();
    }

    public static boolean containsReplacementWording(String prompt) {
        String normalized = normalize(prompt);
        return normalized != null
                && (GENERIC_REPLACEMENT.matcher(normalized).find()
                || REPLACEMENT_OBJECT.matcher(normalized).find()
                || REPLACEMENT_STOP.matcher(normalized).find()
                || SOURCE_ROUTE.matcher(normalized).find()
                || normalized.contains("sostitutiv")
                || normalized.contains("replacement"));
    }

    private ScheduledAlertReplacementHints hints(ScheduledAlertReplacementConstraint constraint) {
        return new ScheduledAlertReplacementHints(true, List.of(constraint), List.of());
    }

    private ScheduledAlertReplacementConstraint.ReplacementType replacementType(String normalized) {
        if (BOTH_TYPE.matcher(normalized).find()) {
            return ScheduledAlertReplacementConstraint.ReplacementType.ARRIVALDEPARTURE;
        }
        if (DEPARTURE_TYPE.matcher(normalized).find()) {
            return ScheduledAlertReplacementConstraint.ReplacementType.DEPARTURE;
        }
        if (ARRIVAL_TYPE.matcher(normalized).find()) {
            return ScheduledAlertReplacementConstraint.ReplacementType.ARRIVAL;
        }
        return ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED;
    }

    private String cleanupStop(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\b(in\\s+partenza|in\\s+arrivo|departure|arrival)\\b.*$", "")
                .trim();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
