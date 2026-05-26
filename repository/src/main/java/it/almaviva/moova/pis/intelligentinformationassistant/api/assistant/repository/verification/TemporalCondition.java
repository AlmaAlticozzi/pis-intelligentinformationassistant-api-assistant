package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.time.LocalTime;

public record TemporalCondition(
        TemporalScope scope,
        String field,
        TemporalOperator operator,
        LocalTime startLocalTime,
        LocalTime endLocalTime,
        String timezone,
        String relatedStopPointName,
        String sourceExpression) {
}
