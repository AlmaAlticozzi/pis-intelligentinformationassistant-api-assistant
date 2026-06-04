package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.text.Normalizer;
import java.util.Locale;

public final class AlertDelayEventTypeNormalizer {

    public static final String ARRIVAL_DELAY = "ARRIVAL_DELAY";
    public static final String DEPARTURE_DELAY = "DEPARTURE_DELAY";
    public static final String BOTH = "BOTH";

    private AlertDelayEventTypeNormalizer() {
    }

    public static String normalize(String rawValue) {
        String normalized = normalizeToken(rawValue);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "ARRIVAL_DELAY", "ARRIVAL DELAY", "RITARDO IN ARRIVO", "RITARDO ALL ARRIVO", "RITARDO DI ARRIVO" ->
                    ARRIVAL_DELAY;
            case "DEPARTURE_DELAY", "DEPARTURE DELAY", "RITARDO IN PARTENZA", "RITARDO ALLA PARTENZA", "RITARDO DI PARTENZA" ->
                    DEPARTURE_DELAY;
            case "BOTH", "GENERIC_DELAY", "GENERIC DELAY", "RITARDO", "DELAY", "RITARDATO", "IN_RITARDO", "IN RITARDO" ->
                    BOTH;
            default -> null;
        };
    }

    private static String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }
}
