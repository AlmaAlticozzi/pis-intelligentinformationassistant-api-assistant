package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertPlatformHints(
        boolean hasPlatformConstraint,
        List<ScheduledAlertPlatformConstraint> constraints,
        List<String> warnings) {

    public static ScheduledAlertPlatformHints empty() {
        return new ScheduledAlertPlatformHints(false, List.of(), List.of());
    }

    public String compactPromptSection() {
        return """
                Backend-derived platform/binario hints:
                - hasPlatformConstraint: %s
                - constraints: %s
                - warnings: %s

                Platform hint rules:
                - Platform/binario constraints are non-location constraints.
                - Platform values must never be placed in serviceDataQuery.stopPoints.
                - Platform constraints must be represented inside snapshotEvaluation.condition over stopPointsJourneyDetails[].
                - If hasPlatformConstraint=true, VERIFIED output must contain a platform field/operator, platform status, or changes CONTAINS PLATFORM_CHANGED condition.
                """.formatted(
                hasPlatformConstraint,
                constraints == null ? List.of() : constraints,
                warnings == null ? List.of() : warnings);
    }
}
