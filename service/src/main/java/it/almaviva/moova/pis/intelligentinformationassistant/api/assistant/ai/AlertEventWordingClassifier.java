package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.Locale;

@ApplicationScoped
public class AlertEventWordingClassifier {

    public record MainEventWording(
            AlertLocationMainEventIntent intent,
            AlertEventPhase phase,
            String mainEventPhrase,
            String accessoryStatePhrase) {
    }

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

    public MainEventWording classifyExplicitMainEvent(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null) {
            return null;
        }
        ClauseParts parts = splitMainAndAccessoryClauses(normalized);
        if (!hasExplicitMovementSubject(parts.mainClause())) {
            return null;
        }
        AlertEventPhase arrivalPhase = classifyArrival(parts.mainClause());
        AlertEventPhase departurePhase = classifyDeparture(parts.mainClause());
        boolean hasArrival = arrivalPhase != AlertEventPhase.AMBIGUOUS;
        boolean hasDeparture = departurePhase != AlertEventPhase.AMBIGUOUS;
        if (hasArrival == hasDeparture) {
            return null;
        }
        if (hasArrival) {
            return new MainEventWording(
                    AlertLocationMainEventIntent.ARRIVAL,
                    arrivalPhase,
                    parts.mainClause().trim(),
                    parts.accessoryClause().trim());
        }
        return new MainEventWording(
                AlertLocationMainEventIntent.DEPARTURE,
                departurePhase,
                parts.mainClause().trim(),
                parts.accessoryClause().trim());
    }

    private AlertEventPhase classifyDeparture(String normalized) {
        if (containsAny(normalized, "treno in partenza", "corsa in partenza",
                "servizio in partenza", "train in departure", "departing train",
                " e in partenza", " in partenza", "sta partendo",
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
        if (containsAny(normalized, "treno in arrivo", "corsa in arrivo",
                "servizio in arrivo", "train in arrival", "arriving train",
                " e in arrivo", " in arrivo", "sta arrivando",
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

    private boolean hasExplicitMovementSubject(String value) {
        return containsAny(value,
                "treno in arrivo", "treno in partenza",
                "corsa in arrivo", "corsa in partenza",
                "servizio in arrivo", "servizio in partenza",
                "treno e in arrivo", "treno e in partenza",
                "corsa e in arrivo", "corsa e in partenza",
                "servizio e in arrivo", "servizio e in partenza",
                "sta arrivando", "sta partendo",
                "corsa arriva", "corsa parte",
                "treno arriva", "treno parte",
                "arriving train", "departing train",
                "train is arriving", "train is departing",
                "journey is arriving", "journey is departing",
                "service is arriving", "service is departing",
                "train arrives", "train departs",
                "journey arrives", "journey departs");
    }

    private ClauseParts splitMainAndAccessoryClauses(String normalized) {
        String[] markers = {
                " con una ",
                " con un ",
                " con ",
                " che ha ",
                " avente ",
                " with ",
                " having "
        };
        int index = -1;
        String marker = "";
        for (String candidate : markers) {
            int candidateIndex = normalized.indexOf(candidate);
            if (candidateIndex >= 0 && (index < 0 || candidateIndex < index)) {
                index = candidateIndex;
                marker = candidate;
            }
        }
        if (index < 0) {
            return new ClauseParts(normalized, "");
        }
        return new ClauseParts(
                normalized.substring(0, index),
                normalized.substring(index + marker.length()));
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

    private record ClauseParts(String mainClause, String accessoryClause) {
    }
}
