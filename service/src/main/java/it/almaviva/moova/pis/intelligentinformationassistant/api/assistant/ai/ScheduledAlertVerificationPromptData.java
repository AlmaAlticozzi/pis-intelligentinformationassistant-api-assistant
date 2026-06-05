package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;

public record ScheduledAlertVerificationPromptData(
        String alertId,
        String name,
        String description,
        String originalPrompt,
        AlertRouteUnderstandingResult route,
        ScheduledServiceDataLocationContext locationContext) {
}
