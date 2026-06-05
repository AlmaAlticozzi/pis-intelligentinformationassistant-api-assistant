package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledUnsupportedConstraintDetector {

    private static final List<Rule> RULES = List.of(
            new Rule("wifi",
                    Pattern.compile("\\b(wifi|wi-fi|wireless)\\b"),
                    "wifi/on-board connectivity is not available in the ServiceData scheduled snapshot catalog"),
            new Rule("carriages/composition",
                    Pattern.compile("\\b(carrozz\\w*|vagon\\w*|coach(?:es)?|carriage(?:s)?|composition|composizione)\\b"),
                    "carriage count or train composition is not available in the ServiceData scheduled snapshot catalog"),
            new Rule("passenger occupancy",
                    Pattern.compile("\\b(affollat\\w*|occupazione|occupancy|crowd(?:ed|ing)?|passenger count|numero passeggeri|posti disponibili|seat availability)\\b"),
                    "passenger occupancy, crowding or seat availability is not available in the ServiceData scheduled snapshot catalog"),
            new Rule("air conditioning",
                    Pattern.compile("\\b(aria condizionata|climatizz\\w*|air conditioning)\\b"),
                    "air conditioning state is not available in the ServiceData scheduled snapshot catalog"),
            new Rule("bike spaces",
                    Pattern.compile("\\b(bici|biciclette|bike spaces?|posti bici)\\b"),
                    "bike-space availability is not available in the ServiceData scheduled snapshot catalog"),
            new Rule("accessibility equipment",
                    Pattern.compile("\\b(accessibilit\\w*|pedana|ascensore|elevator|wheelchair|sedia a rotelle)\\b"),
                    "accessibility equipment state is not available in the ServiceData scheduled snapshot catalog"),
            new Rule("weather",
                    Pattern.compile("\\b(meteo|weather|piove|pioggia|rain|raining|neve|snow|snowing)\\b"),
                    "weather data is not part of the ServiceData scheduled snapshot catalog"),
            new Rule("device status",
                    Pattern.compile("\\b(device|dispositivo|sensore|sensor|status dispositivo)\\b"),
                    "device status is not part of the ServiceData scheduled snapshot catalog"),
            new Rule("audio messages",
                    Pattern.compile("\\b(audio|annunci?o? sonor\\w*|speaker|messaggi?o? audio|announcement)\\b"),
                    "audio announcements are not part of the ServiceData scheduled snapshot catalog"),
            new Rule("CMS/display/broadcast",
                    Pattern.compile("\\b(cms|display|monitor|palinsesto|broadcast|contenut\\w*)\\b"),
                    "CMS/display/broadcast content is not part of the ServiceData scheduled snapshot catalog"),
            new Rule("historical trend",
                    Pattern.compile("\\b(trend|aumentando|diminuendo|in aumento|in diminuzione|increasing|decreasing|rispetto a ieri|ieri|yesterday|last week|settimana scorsa)\\b"),
                    "historical trends or comparisons across runs are not supported by the Scheduled ServiceData snapshot MVP"),
            new Rule("absence over duration",
                    Pattern.compile("\\b(per\\s+\\d+\\s*(minuti|min|ore|h)\\s+non|no train(?:s)?\\s+for\\s+\\d+\\s*(minutes?|hours?)|nessun treno\\s+per\\s+\\d+)\\b"),
                    "absence over a continuous duration requires state/history and is not supported by the Scheduled ServiceData snapshot MVP"));

    public List<ScheduledUnsupportedConstraint> detect(String originalPrompt) {
        String normalized = normalize(originalPrompt);
        if (normalized.isBlank()) {
            return List.of();
        }
        List<ScheduledUnsupportedConstraint> detected = new ArrayList<>();
        for (Rule rule : RULES) {
            if (rule.pattern().matcher(normalized).find()) {
                detected.add(new ScheduledUnsupportedConstraint(rule.concept(), rule.concept(), rule.reason()));
            }
        }
        return List.copyOf(detected);
    }

    public String rejectionReason(String originalPrompt) {
        List<ScheduledUnsupportedConstraint> detected = detect(originalPrompt);
        if (detected.isEmpty()) {
            return null;
        }
        ScheduledUnsupportedConstraint first = detected.getFirst();
        return "The scheduled alert contains a required constraint that is not supported by the ServiceData scheduled snapshot catalog: "
                + first.concept() + ". " + first.reason() + ".";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }

    private record Rule(String concept, Pattern pattern, String reason) {
    }
}
