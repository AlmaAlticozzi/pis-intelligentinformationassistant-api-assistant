package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AlertLocationTargetFieldMapper {

    public List<String> targetFieldHints(AlertLocationRole role) {
        return configuredFieldHints(role).stream()
                .filter(ServiceDataCapabilityCatalog::isAllowedField)
                .toList();
    }

    public boolean hasSupportedFields(AlertLocationRole role) {
        return !targetFieldHints(role).isEmpty();
    }

    private List<String> configuredFieldHints(AlertLocationRole role) {
        AlertLocationRole safeRole = role == null ? AlertLocationRole.GENERIC_LOCATION : role;
        return switch (safeRole) {
            case MAIN_EVENT_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPoint.id",
                    "payload.ongroundServiceEvent.stopPoint.id");
            case ORIGIN_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id",
                    "payload.stopPointJourney.stopPointsJourneyDetails[].callStart.stopPoint.id");
            case DESTINATION_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
                    "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id");
            case ROUTE_OR_NEXT_CALL_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id");
            case TRANSIT_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id",
                    "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id",
                    "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].passingType");
            case CANCELLED_CALL_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id");
            case REPLACEMENT_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id");
            case GENERIC_LOCATION -> List.of(
                    "payload.stopPointJourney.stopPoint.id",
                    "payload.ongroundServiceEvent.stopPoint.id",
                    "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id");
        };
    }
}
