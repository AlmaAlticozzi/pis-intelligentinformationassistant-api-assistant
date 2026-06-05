package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertReplacementHints(
        boolean hasReplacementConstraint,
        List<ScheduledAlertReplacementConstraint> constraints,
        List<String> warnings) {

    public static ScheduledAlertReplacementHints empty() {
        return new ScheduledAlertReplacementHints(false, List.of(), List.of());
    }

    public boolean hasUnsupportedSourceRoute() {
        return constraints.stream()
                .anyMatch(constraint -> constraint.replacementIntent()
                        == ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_SOURCE_ROUTE
                        && constraint.unsupportedReason() != null
                        && !constraint.unsupportedReason().isBlank());
    }

    public String compactPromptSection() {
        return """
                Backend-derived replacement/substitute service hints:
                - hasReplacementConstraint: %s
                - constraints: %s
                - warnings: %s
                Rules:
                - Generic replacement/substitute services are represented by isReplacementOf NOT_EMPTY when catalog-supported.
                - Replacement object checks use replacement NOT_NULL/EXISTS or externalReplacement NOT_NULL/EXISTS.
                - Specific replacement stop constraints must use replacement.stopPointReplacements[].stopPointId.id.
                - Replacement type constraints must use replacementType ARRIVAL/DEPARTURE/ARRIVALDEPARTURE.
                - Replacement source route start/end stop points are unsupported unless the Scheduled catalog exposes stop point id fields.
                """.formatted(hasReplacementConstraint, constraints, warnings);
    }
}
