package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class ServiceDataEventIntentDetector {

    private static final List<EventFamily> FAMILIES = List.of(
            family("DEPARTURE_PROGRESSIVE", List.of("DEPARTING"),
                    "\\b(in partenza|sta(?:nno)? partendo|partenza in corso|departing|starting departure)\\b",
                    "\\b(?:corsa|corse|treno|treni|journey|journeys|train|trains|service|services)\\s+(?:parte|parta|partono|partano|depart|departs|departing)\\b"),
            family("DEPARTURE_COMPLETED", List.of("DEPARTED"),
                    "\\b(e partita|e partito|sono partite|sono partiti|partita|partito|departed|has departed|left)\\b"),
            family("ARRIVAL_PROGRESSIVE", List.of("ARRIVING"),
                    "\\b(in arrivo|sta(?:nno)? arrivando|arrivo in corso|arriving)\\b",
                    "\\b(?:corsa|corse|treno|treni|journey|journeys|train|trains|service|services)\\s+(?:arriva|arrivi|arrivano|arrivino|arrive|arrives|arriving)\\b"),
            family("ARRIVAL_COMPLETED", List.of("ARRIVED"),
                    "\\b(e arrivata|e arrivato|sono arrivate|sono arrivati|arrivata|arrivato|arrived|has arrived)\\b"),
            family("DELAY", List.of("ARRIVAL_DELAY", "DEPARTURE_DELAY"),
                    "\\b(ritardo|in ritardo|delay|delayed|late)\\b"),
            family("CANCELLATION", List.of("CANCELLATION", "ARRIVAL_CANCELLATION", "DEPARTURE_CANCELLATION"),
                    "\\b(cancellazione|cancellata|cancellato|cancellate|cancellati|soppressione|soppressa|soppresso|soppresse|soppressi|cancelled|canceled|cancellation|suppressed)\\b"),
            family("PLATFORM_CHANGE", List.of("ARRIVAL_PLATFORM_CHANGED", "DEPARTURE_PLATFORM_CHANGED"),
                    "\\b(cambia(?:to)? binario|cambio binario|cambio di binario|binario cambiato|platform change|platform changed|track change|track changed)\\b"),
            family("PLATFORM_UPDATE", List.of("ARRIVAL_PLATFORM_UPDATE", "DEPARTURE_PLATFORM_UPDATE"),
                    "\\b(aggiorna(?:to)? binario|binario aggiornato|platform update|platform updated|track update|track updated)\\b"),
            family("PLATFORM_CONFIRMATION", List.of("ARRIVAL_PLATFORM_CONFIRMED", "DEPARTURE_PLATFORM_CONFIRMED"),
                    "\\b(binario confermato|conferma(?:to)? binario|platform confirmed|track confirmed)\\b"),
            family("CHANGED_ORIGIN", List.of("CHANGED_ORIGIN"),
                    "\\b(cambio origine|origine cambiata|changed origin|origin changed)\\b"),
            family("CHANGED_DESTINATION", List.of("CHANGED_DESTINATION"),
                    "\\b(cambio destinazione|destinazione cambiata|changed destination|destination changed)\\b"),
            family("CHANGED_PATH", List.of("CHANGED_PATH"),
                    "\\b(cambio percorso|percorso cambiato|itinerario cambiato|changed path|path changed|route changed)\\b"),
            family("RELOAD_JOURNEY", List.of("RELOAD_JOURNEY"),
                    "\\b(reload journey|ricarica(?:ta)? corsa|rilettura corsa|journey reload)\\b")
    );

    public Optional<ServiceDataEventIntent> detect(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null) {
            return Optional.empty();
        }
        return FAMILIES.stream()
                .filter(family -> family.matches(normalized))
                .findFirst()
                .map(family -> new ServiceDataEventIntent(family.name(), family.eventTypes()));
    }

    public static Optional<ServiceDataEventIntent> detectIntent(String prompt) {
        return new ServiceDataEventIntentDetector().detect(prompt);
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{Alnum}']+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static EventFamily family(String name, List<String> eventTypes, String... aliases) {
        return new EventFamily(name, eventTypes, aliases);
    }

    private record EventFamily(String name, List<String> eventTypes, List<Pattern> aliases) {
        EventFamily(String name, List<String> eventTypes, String... aliases) {
            this(name, List.copyOf(eventTypes), compile(aliases));
        }

        boolean matches(String normalized) {
            return aliases.stream().anyMatch(pattern -> pattern.matcher(normalized).find());
        }

        private static List<Pattern> compile(String... aliases) {
            return java.util.Arrays.stream(aliases)
                    .map(Pattern::compile)
                    .toList();
        }
    }
}
