package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;

public final class ServiceDataStopPointIdCapabilityCatalog {

    private static final List<String> FIELDS = List.of(
            "payload.ongroundServiceEvent.stopPoint.id",
            "payload.stopPointJourney.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].callStart.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id");

    private ServiceDataStopPointIdCapabilityCatalog() {
    }

    public static List<String> fields() {
        return FIELDS;
    }
}
