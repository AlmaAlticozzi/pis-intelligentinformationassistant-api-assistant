package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertLocationTargetFieldMapperTest {

    private final AlertLocationTargetFieldMapper mapper = new AlertLocationTargetFieldMapper();

    @Test
    void mapsMainEventToCurrentAndEventStopFields() {
        assertThat(mapper.targetFieldHints(AlertLocationRole.MAIN_EVENT_LOCATION))
                .containsExactly(
                        "payload.stopPointJourney.stopPoint.id",
                        "payload.ongroundServiceEvent.stopPoint.id");
    }

    @Test
    void mapsOriginAndDestinationToCallBoundaryFields() {
        assertThat(mapper.targetFieldHints(AlertLocationRole.ORIGIN_LOCATION))
                .contains(
                        "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id",
                        "payload.stopPointJourney.stopPointsJourneyDetails[].callStart.stopPoint.id");
        assertThat(mapper.targetFieldHints(AlertLocationRole.DESTINATION_LOCATION))
                .contains(
                        "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
                        "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id");
    }

    @Test
    void mapsRouteTransitCancelledAndReplacementToDedicatedFields() {
        assertThat(mapper.targetFieldHints(AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION))
                .containsExactly("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id");
        assertThat(mapper.targetFieldHints(AlertLocationRole.TRANSIT_LOCATION))
                .contains(
                        "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id",
                        "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id",
                        "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].passingType");
        assertThat(mapper.targetFieldHints(AlertLocationRole.CANCELLED_CALL_LOCATION))
                .containsExactly("payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id");
        assertThat(mapper.targetFieldHints(AlertLocationRole.REPLACEMENT_LOCATION))
                .containsExactly("payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id");
    }
}
