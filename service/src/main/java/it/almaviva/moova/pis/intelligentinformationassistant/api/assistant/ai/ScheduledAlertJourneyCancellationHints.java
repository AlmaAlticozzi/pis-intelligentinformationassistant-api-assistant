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
                - Generic cancellation / suppressed journey / cancelled journey are full journey cancellation semantics, not generic changes.
                - Generic cancelled/suppressed journey conditions must use arrivalStatuses[].status, departureStatuses[].status and passingType.
                - Arrival cancellation / suppressed on arrival is non-exclusive and requires only arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION.
                - Departure cancellation / suppressed on departure is non-exclusive and requires only departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION.
                - Exclusive arrival cancellation / only suppressed on arrival requires arrival cancellation and departure NOT_CONTAINS departure cancellation.
                - Exclusive departure cancellation / only suppressed on departure requires departure cancellation and arrival NOT_CONTAINS arrival cancellation.
                - Do not use changes CONTAINS CANCELLATION or changes CONTAINS PARTIALLY_CANCELLATION for generic cancelled/suppressed journey count/report alerts.
                """.formatted(hasJourneyCancellationConstraint, constraints, warnings);
    }
}
