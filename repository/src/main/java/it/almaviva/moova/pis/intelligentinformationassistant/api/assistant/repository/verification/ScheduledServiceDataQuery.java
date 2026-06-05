package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;

public record ScheduledServiceDataQuery(
        String operation,
        String monitoringScope,
        List<String> stopPoints,
        boolean requiresAllKnownStopPoints,
        ScheduledServiceDataQueryTimeWindow timeWindow) {
}
