package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertChangeHints(
        boolean hasChangeConstraint,
        List<ScheduledAlertChangeConstraint> constraints,
        List<String> warnings) {

    public static ScheduledAlertChangeHints empty() {
        return new ScheduledAlertChangeHints(false, List.of(), List.of());
    }

    public String compactPromptSection() {
        return """
                Backend-derived change/cancellation/exclusion hints:
                - hasChangeConstraint: %s
                - constraints: %s
                - warnings: %s

                Change hint rules:
                - Change, cancellation and exclusion constraints are journey snapshot constraints.
                - They must be represented inside snapshotEvaluation.condition over stopPointsJourneyDetails[].
                - They must never use Kafka/Event fields or payload.ongroundServiceEvent.*.
                - Negative change/cancellation constraints require a catalog-supported negative operator; otherwise VERIFIED must be rejected.
                """.formatted(
                hasChangeConstraint,
                constraints == null ? List.of() : constraints,
                warnings == null ? List.of() : warnings);
    }
}
