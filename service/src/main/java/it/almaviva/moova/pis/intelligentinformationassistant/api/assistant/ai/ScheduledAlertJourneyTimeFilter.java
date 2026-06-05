package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record ScheduledAlertJourneyTimeFilter(
        String rawText,
        TimeRelation timeRelation,
        String startLocalTime,
        String endLocalTime,
        String singleLocalTime,
        Direction direction,
        TimetabledPreference timetabledPreference,
        TargetRoleHint targetRoleHint,
        String timezone) {

    public enum TimeRelation {
        BETWEEN,
        AFTER,
        BEFORE,
        AT_OR_AROUND
    }

    public enum Direction {
        DEPARTURE,
        ARRIVAL,
        PASSING,
        UNKNOWN
    }

    public enum TimetabledPreference {
        TIMETABLED,
        ACTUAL_OR_EFFECTIVE,
        UNSPECIFIED
    }

    public enum TargetRoleHint {
        MONITORED_CURRENT_CALL,
        ORIGIN_CALL,
        DESTINATION_CALL,
        NEXT_CALL,
        TRANSIT_CALL,
        UNKNOWN
    }
}
