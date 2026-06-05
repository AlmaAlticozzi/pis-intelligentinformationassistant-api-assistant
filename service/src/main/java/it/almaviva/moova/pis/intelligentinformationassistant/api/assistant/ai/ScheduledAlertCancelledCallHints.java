package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertCancelledCallHints(
        boolean hasCancelledCallConstraint,
        List<ScheduledAlertCancelledCallConstraint> constraints,
        List<String> warnings) {

    public static ScheduledAlertCancelledCallHints empty() {
        return new ScheduledAlertCancelledCallHints(false, List.of(), List.of());
    }

    public boolean genericAnyCancelledCallRequested() {
        return constraints != null && constraints.stream()
                .anyMatch(ScheduledAlertCancelledCallConstraint::genericAnyCancelledCallRequested);
    }

    public String compactPromptSection() {
        return """
                Backend-derived cancelled/suppressed/skipped stop hints:
                - hasCancelledCallConstraint: %s
                - constraints: %s
                - warnings: %s

                Cancelled call hint rules:
                - Cancelled/suppressed/skipped stop constraints are represented by nextCancelledCalls[].
                - They are different from full journey cancellation.
                - Specific cancelled stop constraints must use nextCancelledCalls[].stopPoint.id/nameLong.
                - Generic "has cancelled/suppressed stops" constraints must use nextCancelledCalls NOT_EMPTY/EXISTS when catalog-supported.
                """.formatted(
                hasCancelledCallConstraint,
                constraints == null ? List.of() : constraints,
                warnings == null ? List.of() : warnings);
    }
}
