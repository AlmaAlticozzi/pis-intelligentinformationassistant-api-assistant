package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertJourneyCancellationHints(
        boolean hasJourneyCancellationConstraint,
        List<ScheduledAlertJourneyCancellationConstraint> constraints,
        List<String> warnings) {

    public static ScheduledAlertJourneyCancellationHints empty() {
        return new ScheduledAlertJourneyCancellationHints(false, List.of(), List.of());
    }

    public boolean hasGenericJourneyCancellation() {
        return constraints != null && constraints.stream()
                .anyMatch(constraint -> constraint.cancellationIntent()
                        == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.GENERIC_JOURNEY_CANCELLATION);
    }

    public String compactPromptSection() {
        return """
                Backend-derived journey cancellation/suppression hints:
                - hasJourneyCancellationConstraint: %s
                - constraints: %s
                - warnings: %s

                Journey cancellation hint rules:
                - Generic cancelled/suppressed journeys are full journey cancellation semantics, not generic changes.
                - Generic cancelled/suppressed journey conditions must use arrivalStatuses[].status, departureStatuses[].status and passingType.
                - Do not use changes CONTAINS CANCELLATION or changes CONTAINS PARTIALLY_CANCELLATION for generic cancelled/suppressed journey count/report alerts.
                - Arrival-only or departure-only cancellation requires NOT_CONTAINS on the opposite status field.
                """.formatted(hasJourneyCancellationConstraint, constraints, warnings);
    }
}
