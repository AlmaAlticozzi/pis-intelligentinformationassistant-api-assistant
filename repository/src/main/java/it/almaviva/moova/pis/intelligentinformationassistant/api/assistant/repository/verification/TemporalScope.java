package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.Optional;

public enum TemporalScope {
    EVENT_GENERATION_TIME("payload.ongroundServiceEvent.eventGenerationTime"),
    TIMETABLED_CALL_START_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.departureTime"),
    TIMETABLED_CALL_END_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.arrivalTime"),
    CALL_START_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].callStart.departureTime"),
    CALL_END_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.arrivalTime"),
    TIMETABLED_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalTime"),
    TIMETABLED_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDepartureTime"),
    ESTIMATED_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].estimatedArrivalTime"),
    ESTIMATED_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].estimatedDepartureTime"),
    NEXT_CALL_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime"),
    NEXT_CALL_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].arrivalTime"),
    NEXT_TRANSIT_CALL_PASSING_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].passingTime"),
    REPLACEMENT_TIMETABLED_CALL_START("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf[].timetabledCallStart"),
    REPLACEMENT_TIMETABLED_CALL_END("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf[].timetabledCallEnd"),
    REPLACEMENT_STOP_POINT_REPLACEMENT_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].arrivalTime"),
    REPLACEMENT_STOP_POINT_REPLACEMENT_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].departureTime"),
    EXTERNAL_REPLACEMENT_STOP_POINT_REPLACEMENT_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].arrivalTime"),
    EXTERNAL_REPLACEMENT_STOP_POINT_REPLACEMENT_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].departureTime");

    private final String field;

    TemporalScope(String field) {
        this.field = field;
    }

    public String field() {
        return field;
    }

    public static Optional<TemporalScope> fromField(String field) {
        for (TemporalScope scope : values()) {
            if (scope.field.equals(field)) {
                return Optional.of(scope);
            }
        }
        return Optional.empty();
    }
}
