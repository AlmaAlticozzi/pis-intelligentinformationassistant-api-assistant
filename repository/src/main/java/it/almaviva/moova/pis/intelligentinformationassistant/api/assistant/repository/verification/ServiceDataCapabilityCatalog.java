package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ServiceDataCapabilityCatalog {

    private static final List<FieldCapability> FIELDS = List.of(
            field("payload.ongroundServiceEvent.eventsType", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("DEPARTED", "ARRIVED"),
                    "Current event operational type.", "parte", "partenza", "arriva", "arrivo"),
            field("payload.ongroundServiceEvent.stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Current event stop point long name.", "event stop point", "Genova", "Firenze"),
            field("payload.ongroundServiceEvent.stopPoint.id", FieldType.STRING,
                    ops("EQUALS", "EQUALS_NORMALIZED"), List.of(),
                    "Current event stop point identifier.", "stop point id", "codice fermata", "id fermata"),
            field("payload.ongroundServiceEvent.stopPoint.nameShort", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Current event stop point short name.", "short stop point name", "nome breve fermata"),
            field("payload.ongroundServiceEvent.eventGenerationTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(),
                    "Timestamp of the current operational event.", "tra le 02:00 e le 10:00", "event time"),
            field("payload.stopPointJourney.stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Current stop point long name.", "station name", "fermata", "stazione", "Genova P.P.", "Firenze"),
            field("payload.stopPointJourney.stopPoint.nameShort", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Current stop point short name.", "short station name", "Genova PP"),
            field("payload.stopPointJourney.stopPoint.id", FieldType.STRING,
                    ops("EQUALS", "EQUALS_NORMALIZED"), List.of(),
                    "Current stop point identifier.", "stop point id", "codice fermata", "id fermata"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Journey public name.", "train name", "journey name", "nome treno"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].infomobilityVehicleJourneyId", FieldType.STRING,
                    ops("EQUALS"), List.of(),
                    "Infomobility journey identifier.", "journey id", "vehicle journey id"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].line.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Line description.", "linea", "line"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].serviceCategory.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Service category.", "service category", "categoria servizio"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].transportOperator.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Transport operator.", "operator", "operatore"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].transportMode.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Transport mode.", "train", "treno", "bus", "mode"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].passingType", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("DESTINATION", "ORIGIN", "TRANSIT", "STOP"),
                    "Current journey passing type.", "origin", "origine", "destination", "transit", "transito", "non si ferma", "passing through"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled call start stop point long name.", "departure stop", "stazione partenza"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id", FieldType.STRING,
                    ops("EQUALS", "EQUALS_NORMALIZED"), List.of(),
                    "Timetabled call start stop point identifier.", "stop point id", "codice fermata", "id fermata"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameShort", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled call start stop point short name.", "departure stop short name"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled call start departure timestamp.", "timetabled departure time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id", FieldType.STRING,
                    ops("EQUALS", "EQUALS_NORMALIZED"), List.of(),
                    "Timetabled call end stop point identifier.", "stop point id", "codice fermata", "id fermata"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled call end stop point long name.", "arrival stop", "stazione arrivo"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.nameShort", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled call end stop point short name.", "arrival stop short name"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled call end arrival timestamp.", "timetabled arrival time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].callStart.stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Call start stop point long name.", "actual departure stop"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].callStart.departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Call start departure timestamp.", "departure time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Call end stop point long name.", "actual arrival stop"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Call end arrival timestamp.", "arrival time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled arrival timestamp.", "timetabled arrival"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDepartureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled departure timestamp.", "timetabled departure"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].estimatedArrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Estimated arrival timestamp.", "estimated arrival"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].estimatedDepartureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Estimated departure timestamp.", "estimated departure"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual departure platform id.", "binario partenza", "departure platform", "platform 1", "binario 1"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual departure platform description.", "binario partenza", "departure platform description"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.isConfirmed", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual departure platform confirmed flag.", "binario partenza confermato"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual departure platform unknown flag.", "binario partenza sconosciuto"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.isBusy", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual departure platform busy flag.", "binario partenza occupato"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.source", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("MONITORED", "OPERATOR", "EMULATED"),
                    "Actual departure platform source.", "platform source", "origine binario partenza"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.displayPlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual arrival platform id.", "binario arrivo", "arrival platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.displayPlatform.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual arrival platform description.", "binario arrivo", "arrival platform description"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isConfirmed", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual arrival platform confirmed flag.", "binario arrivo confermato"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual arrival platform unknown flag.", "binario arrivo sconosciuto"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isBusy", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual arrival platform busy flag.", "binario arrivo occupato"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.source", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("MONITORED", "OPERATOR", "EMULATED"),
                    "Actual arrival platform source.", "platform source", "origine binario arrivo"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled arrival platform id.", "binario previsto", "piattaforma prevista", "timetabled platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled arrival platform description.", "binario previsto", "piattaforma prevista", "timetabled platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled departure platform id.", "binario previsto", "piattaforma prevista", "timetabled platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Timetabled departure platform description.", "binario previsto", "piattaforma prevista", "timetabled platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].monitoredData.departurePlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Monitored departure platform id.", "monitored departure platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].monitoredData.arrivalPlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Monitored arrival platform id.", "monitored arrival platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay", FieldType.NUMBER,
                    numericOps(), List.of(), "Arrival delay.", "arrival delay", "ritardo arrivo"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay", FieldType.NUMBER,
                    numericOps(), List.of(), "Departure delay.", "departure delay", "ritardo partenza", "ritardo superiore"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.roundedDelay", FieldType.NUMBER,
                    numericOps(), List.of(), "Arrival rounded delay.", "rounded arrival delay", "ritardo arrivo arrotondato"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.roundedDelay", FieldType.NUMBER,
                    numericOps(), List.of(), "Departure rounded delay.", "rounded departure delay", "ritardo partenza arrotondato"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Arrival delay unknown flag.", "ritardo arrivo sconosciuto"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Departure delay unknown flag.", "ritardo partenza sconosciuto"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.source", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("MONITORED", "OPERATOR", "EMULATED"),
                    "Arrival delay source.", "delay source", "origine ritardo arrivo"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.source", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("MONITORED", "OPERATOR", "EMULATED"),
                    "Departure delay source.", "delay source", "origine ritardo partenza"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].monitoredData.arrivalDelay", FieldType.NUMBER,
                    numericOps(), List.of(), "Monitored arrival delay.", "monitored arrival delay"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].monitoredData.departureDelay", FieldType.NUMBER,
                    numericOps(), List.of(), "Monitored departure delay.", "monitored departure delay"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("ARRIVING", "ARRIVAL_DELAY", "ARRIVED", "ARRIVAL_PLATFORM_CONFIRMED", "ARRIVAL_PLATFORM_CHANGED", "ARRIVAL_CANCELLATION"),
                    "Arrival statuses.", "arrival status", "cancellazione arrivo"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("DEPARTING", "DEPARTURE_DELAY", "DEPARTED", "DEPARTURE_PLATFORM_CONFIRMED", "DEPARTURE_PLATFORM_CHANGED", "DEPARTURE_CANCELLATION"),
                    "Departure statuses.", "departure status", "cancellazione partenza", "delay status"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next call stop long name.", "passes by", "passa da", "via Siena"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.nameShort", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next call stop short name.", "next stop short name"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id", FieldType.STRING,
                    ops("EQUALS", "EQUALS_NORMALIZED"), List.of(),
                    "Next call stop point identifier.", "stop point id", "codice fermata", "id fermata"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].passingType", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("DESTINATION", "ORIGIN", "TRANSIT", "STOP"),
                    "Next call passing types.", "next transit", "future stops"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Next call departure timestamp.", "next departure time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Next call arrival timestamp.", "next arrival time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL", "SIZE_EQUALS"), List.of(),
                    "Next calls array.", "has next calls", "route length"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameLong", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next transit call long name.", "transit through", "transita da", "passa da Siena"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameShort", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next transit call short name.", "transit stop short name"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id", FieldType.STRING,
                    ops("EQUALS", "EQUALS_NORMALIZED"), List.of(),
                    "Next transit call stop point identifier.", "stop point id", "codice fermata", "id fermata"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].passingTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Next transit call passing timestamp.", "next transit passing time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL", "SIZE_EQUALS"), List.of(),
                    "Next transit calls array.", "at least two transits", "almeno due transiti"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.nameLong", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next cancelled call stop name.", "cancelled next stop"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id", FieldType.STRING,
                    ops("EQUALS", "EQUALS_NORMALIZED"), List.of(),
                    "Next cancelled call stop point identifier.", "stop point id", "codice fermata", "id fermata"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL", "SIZE_EQUALS"), List.of(),
                    "Next cancelled calls array.", "cancelled calls"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].replacement", FieldType.OBJECT,
                    ops("EXISTS", "NOT_NULL"), List.of(), "Replacement object.", "replacement", "sostituzione"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Replacement stop point arrival timestamp.", "replacement arrival time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Replacement stop point departure timestamp.", "replacement departure time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].replacementType", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("ARRIVAL", "DEPARTURE", "ARRIVALDEPARTURE"),
                    "Replacement stop point replacement type.", "replacement type", "tipo sostituzione"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement", FieldType.OBJECT,
                    ops("EXISTS", "NOT_NULL"), List.of(), "External replacement object.", "external replacement"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "External replacement stop point arrival timestamp.", "external replacement arrival time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "External replacement stop point departure timestamp.", "external replacement departure time"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].replacementType", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("ARRIVAL", "DEPARTURE", "ARRIVALDEPARTURE"),
                    "External replacement stop point replacement type.", "external replacement type", "tipo sostituzione esterna"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL"), List.of(), "Replacement source array.", "is replacement of"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf[].timetabledCallStart", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Replacement source timetabled call start timestamp.", "replacement call start"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf[].timetabledCallEnd", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Replacement source timetabled call end timestamp.", "replacement call end"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].info", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("FREE_TEXT_MESSAGE", "COMPOSITION", "REPLACEMENT", "DELAY_REASON", "CHANGE_REASON"),
                    "Internal info categories.", "free text", "composition", "replacement info", "delay reason"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].externalInfo", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("FREE_TEXT_MESSAGE", "COMPOSITION", "REPLACEMENT", "DELAY_REASON", "CHANGE_REASON"),
                    "External info categories.", "external info"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].missedEvents", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Missed events flag.", "eventi mancati", "ha saltato eventi", "missed events"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].changes", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"),
                    List.of("CANCELLATION", "CHANGED_ORIGIN", "CHANGED_DESTINATION", "EXTRA_JOURNEY", "PLATFORM_CHANGED", "PARTIALLY_CANCELLATION", "OTHER"),
                    "Journey detail changes.", "cambio origine", "cambio destinazione", "cambio percorso", "platform changed", "variazione"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].exclusion.totalExclusion", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Total exclusion flag.", "total exclusion"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].exclusion.timeBasedExclusion", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Time based exclusion flag.", "time based exclusion"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].deliveryData.wasAnnounced", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Announcement delivery flag.", "was announced"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].deliveryData.wasInTimeTabledDisplay", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Timetable display delivery flag.", "timetable display"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].deliveryData.wasInPlatformDisplay", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Platform display delivery flag.", "platform display"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].monitored", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Monitored flag.", "monitored", "monitorata"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].watched", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Watched flag.", "watched", "osservata"));

    private static final Map<String, FieldCapability> FIELD_BY_NAME = FIELDS.stream()
            .collect(Collectors.toUnmodifiableMap(FieldCapability::field, Function.identity()));

    private ServiceDataCapabilityCatalog() {
    }

    public static List<FieldCapability> fields() {
        return FIELDS;
    }

    public static Optional<FieldCapability> findField(String field) {
        return Optional.ofNullable(FIELD_BY_NAME.get(field));
    }

    public static boolean isAllowedField(String field) {
        return FIELD_BY_NAME.containsKey(field);
    }

    public static boolean isAllowedMappedBy(String field) {
        return isAllowedField(field) && !isSuspiciousFieldName(field);
    }

    public static boolean isAllowedOperator(String field, String operator) {
        return findField(field)
                .map(capability -> capability.supportsOperator(operator))
                .orElse(false);
    }

    public static boolean isAllowedEnumValue(String field, Object value) {
        return findField(field)
                .filter(FieldCapability::isEnumLike)
                .map(capability -> capability.enumValues().contains(String.valueOf(value).trim().toUpperCase(Locale.ROOT)))
                .orElse(false);
    }

    public static int allowedFieldCount() {
        return FIELDS.size();
    }

    public static String compactPromptCatalog() {
        StringBuilder builder = new StringBuilder();
        for (FieldCapability field : FIELDS) {
            builder.append("- field: ").append(field.field()).append('\n');
            builder.append("  type: ").append(field.type()).append('\n');
            builder.append("  operators: ").append(field.operators()).append('\n');
            if (!field.enumValues().isEmpty()) {
                builder.append("  enumValues: ").append(field.enumValues()).append('\n');
            }
            builder.append("  description: ").append(field.description()).append('\n');
            builder.append("  languageMappings: ").append(field.languageMappings()).append('\n');
        }
        return builder.toString();
    }

    public static boolean isSuspiciousFieldName(String field) {
        if (field == null) {
            return true;
        }
        String lowered = field.toLowerCase(Locale.ROOT);
        return List.of("http", "jdbc", "sql", "kafka", "filesystem", "file:", "process", "runtime", "classloader")
                .stream()
                .anyMatch(lowered::contains);
    }

    private static FieldCapability field(
            String field,
            FieldType type,
            Set<String> operators,
            List<String> enumValues,
            String description,
            String... languageMappings) {
        return new FieldCapability(
                field,
                type,
                operators,
                enumValues,
                description,
                List.of(languageMappings));
    }

    private static Set<String> ops(String... operators) {
        return Set.of(operators);
    }

    private static Set<String> numericOps() {
        return ops("EXISTS", "GREATER_THAN", "GREATER_OR_EQUAL", "LESS_THAN", "LESS_OR_EQUAL", "EQUALS");
    }

    private static Set<String> temporalOps() {
        return ServiceDataTemporalCapabilityCatalog.temporalOperators();
    }

    public enum FieldType {
        STRING,
        NUMBER,
        BOOLEAN,
        ENUM,
        ENUM_ARRAY,
        ARRAY,
        OBJECT,
        TEMPORAL
    }

    public record FieldCapability(
            String field,
            FieldType type,
            Set<String> operators,
            List<String> enumValues,
            String description,
            List<String> languageMappings) {

        public boolean supportsOperator(String operator) {
            return operator != null && operators.contains(operator);
        }

        public boolean isEnumLike() {
            return type == FieldType.ENUM || type == FieldType.ENUM_ARRAY;
        }

        public List<String> normalizeEnumValues(List<?> values) {
            List<String> normalized = new ArrayList<>();
            for (Object value : values) {
                normalized.add(String.valueOf(value).trim().toUpperCase(Locale.ROOT));
            }
            return normalized;
        }
    }
}
