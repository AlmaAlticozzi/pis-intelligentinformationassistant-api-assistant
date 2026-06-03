package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.Locale;

@ApplicationScoped
public class AlertEventWordingClassifier {

    public AlertEventPhase classify(String prompt, AlertLocationMainEventIntent intent) {
        String normalized = normalize(prompt);
        if (normalized == null || intent == null) {
            return AlertEventPhase.AMBIGUOUS;
        }
        return switch (intent) {
            case DEPARTURE -> classifyDeparture(normalized);
            case ARRIVAL -> classifyArrival(normalized);
            default -> AlertEventPhase.AMBIGUOUS;
        };
    }

    public String expectedEventType(AlertLocationMainEventIntent intent, AlertEventPhase phase) {
        if (intent == null || phase == null) {
            return null;
        }
        return switch (intent) {
            case DEPARTURE -> switch (phase) {
                case PROGRESSIVE -> "DEPARTING";
                case COMPLETED -> "DEPARTED";
                case AMBIGUOUS -> null;
            };
            case ARRIVAL -> switch (phase) {
                case PROGRESSIVE -> "ARRIVING";
                case COMPLETED -> "ARRIVED";
                case AMBIGUOUS -> null;
            };
            default -> null;
        };
    }

    private AlertEventPhase classifyDeparture(String normalized) {
        if (containsAny(normalized, " e in partenza", " in partenza", "sta partendo",
                "is departing", "about to depart")) {
            return AlertEventPhase.PROGRESSIVE;
        }
        if (containsAny(normalized, " e partita", " e partito", "ha lasciato", "parte da",
                "parte ", " parte", "partita", "partito", "departs", "has departed", "departed")) {
            return AlertEventPhase.COMPLETED;
        }
        return AlertEventPhase.AMBIGUOUS;
    }

    private AlertEventPhase classifyArrival(String normalized) {
        if (containsAny(normalized, " e in arrivo", " in arrivo", "sta arrivando",
                "is arriving", "about to arrive")) {
            return AlertEventPhase.PROGRESSIVE;
        }
        if (containsAny(normalized, " e arrivata", " e arrivato", "arriva a", "arriva ",
                " arriva", "arrivata", "arrivato", "arrives", "has arrived", "arrived")) {
            return AlertEventPhase.COMPLETED;
        }
        return AlertEventPhase.AMBIGUOUS;
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return " " + Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim() + " ";
    }
}
