package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;

public record TemporalCondition(
        TemporalScope scope,
        String field,
        TemporalOperator operator,
        LocalTime startLocalTime,
        LocalTime endLocalTime,
        List<DayOfWeek> days,
        String timezone,
        String relatedStopPointName,
        String sourceExpression) {
}
