package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record AlertRouteUnderstandingResult(
        AlertRouteDecision decision,
        List<String> dataDomains,
        String primaryDataDomain,
        AlertRouteInterpreterType interpreterType,
        AlertRouteAccessMode accessMode,
        AlertRouteIntentKind intentKind,
        AlertRouteOutputMode outputMode,
        boolean requiresPolling,
        boolean requiresServiceDataApi,
        boolean requiresKafkaEvent,
        boolean hasAggregation,
        boolean hasCardinalityThreshold,
        boolean hasReportIntent,
        Double confidence,
        String summary,
        String rejectedReason,
        List<String> warnings
) {
    public static AlertRouteUnderstandingResult rejected(String reason) {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.REJECTED,
                List.of(),
                null,
                AlertRouteInterpreterType.UNKNOWN,
                AlertRouteAccessMode.NONE,
                AlertRouteIntentKind.UNSUPPORTED,
                AlertRouteOutputMode.NONE,
                false,
                false,
                false,
                false,
                false,
                false,
                0.0,
                "Alert route understanding rejected the prompt.",
                reason,
                reason == null || reason.isBlank() ? List.of() : List.of(reason));
    }
}
