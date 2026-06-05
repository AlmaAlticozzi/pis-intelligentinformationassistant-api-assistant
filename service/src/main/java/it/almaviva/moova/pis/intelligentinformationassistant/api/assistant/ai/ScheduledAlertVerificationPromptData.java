package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;

public record ScheduledAlertVerificationPromptData(
        String alertId,
        String name,
        String description,
        String originalPrompt,
        AlertRouteUnderstandingResult route,
        ScheduledServiceDataLocationContext locationContext,
        ScheduledAlertTemporalHints temporalHints,
        ScheduledAlertPlatformHints platformHints,
        ScheduledAlertChangeHints changeHints) {

    public ScheduledAlertVerificationPromptData(
            String alertId,
            String name,
            String description,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints) {
        this(alertId, name, description, originalPrompt, route, locationContext, temporalHints, ScheduledAlertPlatformHints.empty());
    }

    public ScheduledAlertVerificationPromptData(
            String alertId,
            String name,
            String description,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints) {
        this(alertId, name, description, originalPrompt, route, locationContext, temporalHints, platformHints, ScheduledAlertChangeHints.empty());
    }
}
