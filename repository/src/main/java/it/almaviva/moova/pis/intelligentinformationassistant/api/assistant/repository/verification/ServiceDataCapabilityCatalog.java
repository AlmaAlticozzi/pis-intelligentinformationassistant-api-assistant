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
            field("payload.stopPointJourney.stopPoint.nameLong", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Current stop point long name.", "station name", "fermata", "stazione", "Genova P.P.", "Firenze"),
            field("payload.stopPointJourney.stopPoint.nameShort", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Current stop point short name.", "short station name", "Genova PP"),
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
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual departure platform id.", "binario partenza", "departure platform", "platform 1", "binario 1"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual departure platform description.", "binario partenza", "departure platform description"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.displayPlatform.id", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual arrival platform id.", "binario arrivo", "arrival platform"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.displayPlatform.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Actual arrival platform description.", "binario arrivo", "arrival platform description"),
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
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next call stop long name.", "passes by", "passa da", "via Siena"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.nameShort", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next call stop short name.", "next stop short name"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].passingType", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("DESTINATION", "ORIGIN", "TRANSIT", "STOP"),
                    "Next call passing types.", "next transit", "future stops"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL", "SIZE_EQUALS"), List.of(),
                    "Next calls array.", "has next calls", "route length"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameLong", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next transit call long name.", "transit through", "transita da", "passa da Siena"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameShort", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next transit call short name.", "transit stop short name"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL", "SIZE_EQUALS"), List.of(),
                    "Next transit calls array.", "at least two transits", "almeno due transiti"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.nameLong", FieldType.STRING,
                    ops("CONTAINS_NORMALIZED", "ANY_MATCH"), List.of(), "Next cancelled call stop name.", "cancelled next stop"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL", "SIZE_EQUALS"), List.of(),
                    "Next cancelled calls array.", "cancelled calls"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].replacement", FieldType.OBJECT,
                    ops("EXISTS", "NOT_NULL"), List.of(), "Replacement object.", "replacement", "sostituzione"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement", FieldType.OBJECT,
                    ops("EXISTS", "NOT_NULL"), List.of(), "External replacement object.", "external replacement"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].isReplacementOf", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL"), List.of(), "Replacement source array.", "is replacement of"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].info", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("FREE_TEXT_MESSAGE", "COMPOSITION", "REPLACEMENT", "DELAY_REASON", "CHANGE_REASON"),
                    "Internal info categories.", "free text", "composition", "replacement info", "delay reason"),
            field("payload.stopPointJourney.stopPointsJourneyDetails[].externalInfo", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("FREE_TEXT_MESSAGE", "COMPOSITION", "REPLACEMENT", "DELAY_REASON", "CHANGE_REASON"),
                    "External info categories.", "external info"),
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

    public enum FieldType {
        STRING,
        NUMBER,
        BOOLEAN,
        ENUM,
        ENUM_ARRAY,
        ARRAY,
        OBJECT
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
