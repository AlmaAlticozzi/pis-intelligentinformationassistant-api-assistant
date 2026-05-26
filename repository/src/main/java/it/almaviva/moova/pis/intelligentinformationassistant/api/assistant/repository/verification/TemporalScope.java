package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.Optional;

public enum TemporalScope {
    EVENT_GENERATION_TIME("payload.ongroundServiceEvent.eventGenerationTime"),
    NEXT_CALL_DEPARTURE_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime"),
    NEXT_CALL_ARRIVAL_TIME("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].arrivalTime");

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
