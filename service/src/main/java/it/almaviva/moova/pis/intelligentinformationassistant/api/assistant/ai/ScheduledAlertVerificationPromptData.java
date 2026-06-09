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
        ScheduledAlertChangeHints changeHints,
        ScheduledAlertJourneyCancellationHints journeyCancellationHints,
        ScheduledAlertCancelledCallHints cancelledCallHints,
        ScheduledAlertReplacementHints replacementHints) {

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

    public ScheduledAlertVerificationPromptData(
            String alertId,
            String name,
            String description,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints) {
        this(alertId, name, description, originalPrompt, route, locationContext, temporalHints, platformHints, changeHints, ScheduledAlertJourneyCancellationHints.empty());
    }

    public ScheduledAlertVerificationPromptData(
            String alertId,
            String name,
            String description,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertJourneyCancellationHints journeyCancellationHints) {
        this(alertId, name, description, originalPrompt, route, locationContext, temporalHints, platformHints, changeHints, journeyCancellationHints, ScheduledAlertCancelledCallHints.empty());
    }

    public ScheduledAlertVerificationPromptData(
            String alertId,
            String name,
            String description,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertCancelledCallHints cancelledCallHints) {
        this(alertId, name, description, originalPrompt, route, locationContext, temporalHints, platformHints, changeHints, ScheduledAlertJourneyCancellationHints.empty(), cancelledCallHints, ScheduledAlertReplacementHints.empty());
    }

    public ScheduledAlertVerificationPromptData(
            String alertId,
            String name,
            String description,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertJourneyCancellationHints journeyCancellationHints,
            ScheduledAlertCancelledCallHints cancelledCallHints) {
        this(alertId, name, description, originalPrompt, route, locationContext, temporalHints, platformHints, changeHints, journeyCancellationHints, cancelledCallHints, ScheduledAlertReplacementHints.empty());
    }
}
