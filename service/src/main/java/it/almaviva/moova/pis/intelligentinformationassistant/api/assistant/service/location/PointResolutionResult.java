package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import java.util.List;

public record PointResolutionResult(
        String rawText,
        String normalizedText,
        PointResolutionStatus status,
        List<PointCandidate> candidates,
        double bestScore,
        String fallbackReason) {
}
