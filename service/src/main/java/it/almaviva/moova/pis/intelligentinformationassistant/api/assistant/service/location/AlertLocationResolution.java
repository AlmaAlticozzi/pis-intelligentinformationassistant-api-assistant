package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import java.util.List;

public record AlertLocationResolution(
        AlertLocationMention mention,
        PointResolutionResult pointResolutionResult,
        List<String> selectedPointIds,
        boolean fallbackToNameLong,
        double confidenceImpact) {
}
