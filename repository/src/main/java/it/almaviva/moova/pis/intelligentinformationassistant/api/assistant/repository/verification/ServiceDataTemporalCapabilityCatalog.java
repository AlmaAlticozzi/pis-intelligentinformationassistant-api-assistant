package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ServiceDataTemporalCapabilityCatalog {

    public static final String LOCAL_TIME_BETWEEN = "LOCAL_TIME_BETWEEN";
    public static final String LOCAL_DAY_OF_WEEK_IN = "LOCAL_DAY_OF_WEEK_IN";
    public static final String LOCAL_DAY_OF_WEEK_NOT_IN = "LOCAL_DAY_OF_WEEK_NOT_IN";

    private static final Set<String> TEMPORAL_OPERATORS = Set.of(
            LOCAL_TIME_BETWEEN,
            LOCAL_DAY_OF_WEEK_IN,
            LOCAL_DAY_OF_WEEK_NOT_IN);

    private static final List<TemporalFieldCapability> FIELDS = List.of(
            field("payload.ongroundServiceEvent.eventGenerationTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.departureTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.arrivalTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].callStart.departureTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.arrivalTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDepartureTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].estimatedArrivalTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].estimatedDepartureTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].arrivalTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].passingTime"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf[].timetabledCallStart"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf[].timetabledCallEnd"));

    private static final Map<String, TemporalFieldCapability> FIELD_BY_PATH = FIELDS.stream()
            .collect(Collectors.toUnmodifiableMap(TemporalFieldCapability::field, Function.identity()));

    private ServiceDataTemporalCapabilityCatalog() {
    }

    public static boolean isTemporalOperator(String operator) {
        return operator != null && TEMPORAL_OPERATORS.contains(operator);
    }

    public static boolean isAllowedTemporalField(String field) {
        return FIELD_BY_PATH.containsKey(field);
    }

    public static boolean isAllowedOperator(String field, String operator) {
        return Optional.ofNullable(FIELD_BY_PATH.get(field))
                .map(capability -> capability.operators().contains(operator))
                .orElse(false);
    }

    public static Optional<TemporalFieldCapability> findField(String field) {
        return Optional.ofNullable(FIELD_BY_PATH.get(field));
    }

    public static Set<String> temporalFields() {
        return FIELD_BY_PATH.keySet();
    }

    public static Set<String> temporalOperators() {
        return TEMPORAL_OPERATORS;
    }

    private static TemporalFieldCapability field(String field) {
        return new TemporalFieldCapability(field, TEMPORAL_OPERATORS);
    }

    public record TemporalFieldCapability(String field, Set<String> operators) {
    }
}
