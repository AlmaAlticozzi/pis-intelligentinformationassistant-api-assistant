package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ServiceDataEventIntent(
        String family,
        List<String> eventTypes
) {
    public ServiceDataEventIntent {
        eventTypes = eventTypes == null ? List.of() : List.copyOf(eventTypes);
    }
}
