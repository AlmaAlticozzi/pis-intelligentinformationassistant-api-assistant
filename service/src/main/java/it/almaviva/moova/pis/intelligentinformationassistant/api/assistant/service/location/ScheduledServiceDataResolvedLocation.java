package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationRole;

import java.util.List;

public record ScheduledServiceDataResolvedLocation(
        String rawText,
        String normalizedText,
        ScheduledAlertLocationRole scheduledRole,
        ScheduledAlertLocationPolarity polarity,
        boolean requiredForApiQuery,
        boolean requiredCoverage,
        ScheduledServiceDataLocationResolutionStatus resolutionStatus,
        List<String> selectedPointIds,
        List<PointCandidate> candidates,
        boolean fallbackToNameLong,
        boolean fallbackAllowed,
        List<String> targetFieldHints,
        String warningReason) {

    public ScheduledServiceDataResolvedLocation {
        rawText = rawText == null ? "" : rawText;
        normalizedText = normalizedText == null ? "" : normalizedText;
        scheduledRole = scheduledRole == null ? ScheduledAlertLocationRole.UNKNOWN_LOCATION_ROLE : scheduledRole;
        polarity = polarity == null ? ScheduledAlertLocationPolarity.INCLUDE : polarity;
        resolutionStatus = resolutionStatus == null ? ScheduledServiceDataLocationResolutionStatus.UNRESOLVED : resolutionStatus;
        selectedPointIds = selectedPointIds == null ? List.of() : List.copyOf(selectedPointIds);
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        targetFieldHints = targetFieldHints == null ? List.of() : List.copyOf(targetFieldHints);
        warningReason = warningReason == null ? "" : warningReason;
    }
}
