package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScheduledServiceDataCapabilityCatalog {

    private static final Set<String> QUERY_COVERAGE_FIELDS = Set.of(
            "serviceDataQuery.stopPoints",
            "serviceDataQuery.stopPoints[]",
            "body.stopPoints[]");

    private static final List<String> STOP_POINT_ID_FIELDS = List.of(
            "stopPoint.id",
            "stopPointsJourneyDetails[].callStart.stopPoint.id",
            "stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id",
            "stopPointsJourneyDetails[].callEnd.stopPoint.id",
            "stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
            "stopPointsJourneyDetails[].nextCalls[].stopPoint.id",
            "stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id",
            "stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id",
            "stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id",
            "stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].stopPointId.id");

    private static final List<FieldCapability> FIELDS = List.of(
            stopPointIdField("stopPoint.id"),
            field("stopPoint.nameLong", FieldType.STRING, locationNameOps(), List.of(),
                    "Snapshot query stop point long name."),
            field("stopPoint.nameShort", FieldType.STRING, locationNameOps(), List.of(),
                    "Snapshot query stop point short name."),

            field("stopPointsJourneyDetails[].vehicleJourneyName", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Journey public name."),
            field("stopPointsJourneyDetails[].infomobilityVehicleJourneyId", FieldType.STRING,
                    ops("EQUALS"), List.of(), "Infomobility journey identifier."),
            field("stopPointsJourneyDetails[].line.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Line description."),
            field("stopPointsJourneyDetails[].line.id", FieldType.STRING,
                    ops("EQUALS", "IN"), List.of(), "Line identifier."),
            field("stopPointsJourneyDetails[].serviceCategory.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Service category description."),
            field("stopPointsJourneyDetails[].serviceCategory.id", FieldType.STRING,
                    ops("EQUALS", "IN"), List.of(), "Service category identifier."),
            field("stopPointsJourneyDetails[].transportMode.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Transport mode description."),
            field("stopPointsJourneyDetails[].transportMode.id", FieldType.STRING,
                    ops("EQUALS", "IN"), List.of(), "Transport mode identifier."),
            field("stopPointsJourneyDetails[].transportOperator.dsc", FieldType.STRING,
                    ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED"), List.of(),
                    "Transport operator description."),
            field("stopPointsJourneyDetails[].transportOperator.id", FieldType.STRING,
                    ops("EQUALS", "IN"), List.of(), "Transport operator identifier."),
            field("stopPointsJourneyDetails[].passingType", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("ORIGIN", "DESTINATION", "TRANSIT", "STOP"),
                    "Journey passing type at the monitored stop point."),

            stopPointIdField("stopPointsJourneyDetails[].callStart.stopPoint.id"),
            field("stopPointsJourneyDetails[].callStart.stopPoint.nameLong", FieldType.STRING,
                    locationNameOps(), List.of(), "Actual/current origin stop point long name."),
            field("stopPointsJourneyDetails[].callStart.departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Actual/current origin departure timestamp."),
            stopPointIdField("stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id"),
            field("stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong", FieldType.STRING,
                    locationNameOps(), List.of(), "Timetabled origin stop point long name."),
            field("stopPointsJourneyDetails[].timetabledCallStart.departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled origin departure timestamp."),
            stopPointIdField("stopPointsJourneyDetails[].callEnd.stopPoint.id"),
            field("stopPointsJourneyDetails[].callEnd.stopPoint.nameLong", FieldType.STRING,
                    locationNameOps(), List.of(), "Actual/current destination stop point long name."),
            field("stopPointsJourneyDetails[].callEnd.arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Actual/current destination arrival timestamp."),
            stopPointIdField("stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id"),
            field("stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.nameLong", FieldType.STRING,
                    locationNameOps(), List.of(), "Timetabled destination stop point long name."),
            field("stopPointsJourneyDetails[].timetabledCallEnd.arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled destination arrival timestamp."),

            field("stopPointsJourneyDetails[].timetabledArrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled arrival timestamp."),
            field("stopPointsJourneyDetails[].timetabledDepartureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Timetabled departure timestamp."),
            field("stopPointsJourneyDetails[].estimatedArrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Estimated arrival timestamp."),
            field("stopPointsJourneyDetails[].estimatedDepartureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Estimated departure timestamp."),

            field("stopPointsJourneyDetails[].arrivalDelay.delay", FieldType.NUMBER,
                    numericOps(), List.of(), "Arrival delay in the snapshot."),
            field("stopPointsJourneyDetails[].arrivalDelay.roundedDelay", FieldType.NUMBER,
                    numericOps(), List.of(), "Rounded arrival delay in the snapshot."),
            field("stopPointsJourneyDetails[].arrivalDelay.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Arrival delay unknown flag."),
            field("stopPointsJourneyDetails[].departureDelay.delay", FieldType.NUMBER,
                    numericOps(), List.of(), "Departure delay in the snapshot."),
            field("stopPointsJourneyDetails[].departureDelay.roundedDelay", FieldType.NUMBER,
                    numericOps(), List.of(), "Rounded departure delay in the snapshot."),
            field("stopPointsJourneyDetails[].departureDelay.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Departure delay unknown flag."),

            field("stopPointsJourneyDetails[].arrivalStatuses[].status", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"),
                    List.of("ARRIVING", "ARRIVAL_DELAY", "ARRIVED", "ARRIVAL_PLATFORM_CONFIRMED",
                            "ARRIVAL_PLATFORM_CHANGED", "ARRIVAL_CANCELLATION"),
                    "Arrival status values in the snapshot."),
            field("stopPointsJourneyDetails[].departureStatuses[].status", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"),
                    List.of("DEPARTING", "DEPARTURE_DELAY", "DEPARTED", "DEPARTURE_PLATFORM_CONFIRMED",
                            "DEPARTURE_PLATFORM_CHANGED", "DEPARTURE_CANCELLATION"),
                    "Departure status values in the snapshot."),

            platformField("stopPointsJourneyDetails[].timetabledArrivalPlatform.dsc",
                    "Timetabled arrival platform description."),
            platformField("stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc",
                    "Timetabled departure platform description."),
            platformField("stopPointsJourneyDetails[].actualArrivalPlatform.platform.dsc",
                    "Actual arrival platform description."),
            platformField("stopPointsJourneyDetails[].actualArrivalPlatform.displayPlatform.dsc",
                    "Actual arrival display platform description."),
            field("stopPointsJourneyDetails[].actualArrivalPlatform.isConfirmed", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual arrival platform confirmed flag."),
            field("stopPointsJourneyDetails[].actualArrivalPlatform.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual arrival platform unknown flag."),
            field("stopPointsJourneyDetails[].actualArrivalPlatform.isBusy", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual arrival platform busy flag."),
            platformField("stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc",
                    "Actual departure platform description."),
            platformField("stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.dsc",
                    "Actual departure display platform description."),
            field("stopPointsJourneyDetails[].actualDeparturePlatform.isConfirmed", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual departure platform confirmed flag."),
            field("stopPointsJourneyDetails[].actualDeparturePlatform.isUnknown", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual departure platform unknown flag."),
            field("stopPointsJourneyDetails[].actualDeparturePlatform.isBusy", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Actual departure platform busy flag."),
            platformField("stopPointsJourneyDetails[].previousArrivalPlatform.platform.dsc",
                    "Previous arrival platform description."),
            platformField("stopPointsJourneyDetails[].previousDeparturePlatform.platform.dsc",
                    "Previous departure platform description."),

            field("stopPointsJourneyDetails[].changes", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"),
                    List.of("CANCELLATION", "CHANGED_ORIGIN", "CHANGED_DESTINATION", "CHANGED_PATH",
                            "EXTRA_JOURNEY", "PLATFORM_CHANGED", "PARTIALLY_CANCELLATION", "OTHER"),
                    "Journey snapshot changes."),
            field("stopPointsJourneyDetails[].exclusion.totalExclusion", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Total exclusion flag."),
            field("stopPointsJourneyDetails[].exclusion.timeBasedExclusion", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Time based exclusion flag."),
            field("stopPointsJourneyDetails[].deliveryData.wasAnnounced", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Announcement delivery flag."),
            field("stopPointsJourneyDetails[].deliveryData.wasInTimeTabledDisplay", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Timetabled display delivery flag."),
            field("stopPointsJourneyDetails[].deliveryData.wasInPlatformDisplay", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Platform display delivery flag."),

            stopPointIdField("stopPointsJourneyDetails[].nextCalls[].stopPoint.id"),
            field("stopPointsJourneyDetails[].nextCalls[].stopPoint.nameLong", FieldType.STRING,
                    locationNameArrayOps(), List.of(), "Next call stop point long name."),
            field("stopPointsJourneyDetails[].nextCalls[].passingType", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"), List.of("ORIGIN", "DESTINATION", "TRANSIT", "STOP"),
                    "Next call passing type values."),
            field("stopPointsJourneyDetails[].nextCalls[].arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Next call arrival timestamp."),
            field("stopPointsJourneyDetails[].nextCalls[].departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Next call departure timestamp."),
            field("stopPointsJourneyDetails[].nextCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_EQUALS", "SIZE_GREATER_OR_EQUAL"), List.of(),
                    "Next calls array."),

            stopPointIdField("stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id"),
            field("stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameLong", FieldType.STRING,
                    locationNameArrayOps(), List.of(), "Next transit call stop point long name."),
            field("stopPointsJourneyDetails[].nextTransitCalls[].passingTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Next transit call passing timestamp."),
            field("stopPointsJourneyDetails[].nextTransitCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_EQUALS", "SIZE_GREATER_OR_EQUAL"), List.of(),
                    "Next transit calls array."),

            stopPointIdField("stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id"),
            field("stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.nameLong", FieldType.STRING,
                    locationNameArrayOps(), List.of(), "Next cancelled call stop point long name."),
            field("stopPointsJourneyDetails[].nextCancelledCalls", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_EQUALS", "SIZE_GREATER_OR_EQUAL"), List.of(),
                    "Next cancelled calls array."),

            field("stopPointsJourneyDetails[].replacement", FieldType.OBJECT,
                    ops("EXISTS", "NOT_NULL"), List.of(), "Replacement object."),
            stopPointIdField("stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id"),
            field("stopPointsJourneyDetails[].replacement.stopPointReplacements[].replacementType", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("ARRIVAL", "DEPARTURE", "ARRIVALDEPARTURE"),
                    "Replacement stop point replacement type."),
            field("stopPointsJourneyDetails[].replacement.stopPointReplacements[].arrivalTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Replacement stop point arrival timestamp."),
            field("stopPointsJourneyDetails[].replacement.stopPointReplacements[].departureTime", FieldType.TEMPORAL,
                    temporalOps(), List.of(), "Replacement stop point departure timestamp."),
            field("stopPointsJourneyDetails[].externalReplacement", FieldType.OBJECT,
                    ops("EXISTS", "NOT_NULL"), List.of(), "External replacement object."),
            stopPointIdField("stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].stopPointId.id"),
            field("stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].replacementType", FieldType.ENUM,
                    ops("EQUALS", "IN"), List.of("ARRIVAL", "DEPARTURE", "ARRIVALDEPARTURE"),
                    "External replacement stop point replacement type."),
            field("stopPointsJourneyDetails[].isReplacementOf", FieldType.ARRAY,
                    ops("NOT_EMPTY", "SIZE_GREATER_OR_EQUAL"), List.of(), "Replacement source array."),

            field("stopPointsJourneyDetails[].info", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"),
                    List.of("FREE_TEXT_MESSAGE", "COMPOSITION", "REPLACEMENT", "DELAY_REASON", "CHANGE_REASON"),
                    "Internal info categories."),
            field("stopPointsJourneyDetails[].externalInfo", FieldType.ENUM_ARRAY,
                    ops("CONTAINS", "CONTAINS_ANY"),
                    List.of("FREE_TEXT_MESSAGE", "COMPOSITION", "REPLACEMENT", "DELAY_REASON", "CHANGE_REASON"),
                    "External info categories."),
            field("stopPointsJourneyDetails[].monitored", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Monitored flag."),
            field("stopPointsJourneyDetails[].watched", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Watched flag."),
            field("stopPointsJourneyDetails[].missedEvents", FieldType.BOOLEAN,
                    ops("EQUALS"), List.of(), "Missed events flag when present in the snapshot model."));

    private static final Map<String, FieldCapability> FIELD_BY_NAME = FIELDS.stream()
            .collect(Collectors.toUnmodifiableMap(FieldCapability::field, Function.identity()));

    private ScheduledServiceDataCapabilityCatalog() {
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

    public static Set<String> queryCoverageFields() {
        return QUERY_COVERAGE_FIELDS;
    }

    public static boolean isAllowedQueryCoverageField(String field) {
        return field != null && QUERY_COVERAGE_FIELDS.contains(field.trim());
    }

    public static boolean isAllowedRequirementCoverageField(String field) {
        return isAllowedQueryCoverageField(field) || isAllowedField(field);
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

    public static List<String> stopPointIdFields() {
        return STOP_POINT_ID_FIELDS;
    }

    public static String compactPromptCatalog() {
        StringBuilder builder = new StringBuilder();
        builder.append("This catalog is for ServiceData API snapshot verification.\n")
                .append("Fields are relative to the StopPointJourneyV2 snapshot response and the input model ServiceDataStopPointJourneysV2.\n")
                .append("Query coverage fields may be used only in requirementCoverage.mappedBy, not in snapshotEvaluation.condition: ")
                .append(QUERY_COVERAGE_FIELDS).append(".\n")
                .append("Do not use Kafka event field roots or event-message envelopes.\n")
                .append("Use stopPointsJourneyDetails[] anyElement to correlate constraints on the same journey.\n")
                .append("Use nested anyElement for nextCalls[], nextTransitCalls[], nextCancelledCalls[], and replacement.stopPointReplacements[].\n");
        for (FieldCapability field : FIELDS) {
            builder.append("- field: ").append(field.field()).append('\n');
            builder.append("  type: ").append(field.type()).append('\n');
            builder.append("  operators: ").append(field.operators()).append('\n');
            if (!field.enumValues().isEmpty()) {
                builder.append("  enumValues: ").append(field.enumValues()).append('\n');
            }
            builder.append("  description: ").append(field.description()).append('\n');
        }
        return builder.toString();
    }

    private static FieldCapability field(
            String field,
            FieldType type,
            Set<String> operators,
            List<String> enumValues,
            String description) {
        return new FieldCapability(field, type, operators, enumValues, description);
    }

    private static FieldCapability stopPointIdField(String field) {
        if (!STOP_POINT_ID_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Unsupported scheduled stop point id field: " + field);
        }
        return field(
                field,
                FieldType.STOP_POINT_ID,
                ops("EQUALS", "IN", "NOT_IN"),
                List.of(),
                "Stable PIS stop point identifier resolved from points.json.");
    }

    private static FieldCapability platformField(String field, String description) {
        return field(field, FieldType.PLATFORM, platformOps(), List.of(), description);
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

    private static Set<String> locationNameOps() {
        return ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED", "NOT_EQUALS_NORMALIZED", "NOT_CONTAINS_NORMALIZED");
    }

    private static Set<String> locationNameArrayOps() {
        return ops("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED", "NOT_EQUALS_NORMALIZED", "NOT_CONTAINS_NORMALIZED", "ANY_MATCH");
    }

    private static Set<String> platformOps() {
        return ops(
                "EQUAL_PLATFORM",
                "NOT_EQUAL_PLATFORM",
                "IN_PLATFORMS",
                "NOT_IN_PLATFORMS",
                "PLATFORM_EQUALS_FIELD",
                "PLATFORM_NOT_EQUALS_FIELD",
                "PLATFORM_NUMBER_GREATER_THAN",
                "PLATFORM_NUMBER_GREATER_OR_EQUAL",
                "PLATFORM_NUMBER_LESS_THAN",
                "PLATFORM_NUMBER_LESS_OR_EQUAL",
                "PLATFORM_NUMBER_BETWEEN",
                "PLATFORM_NUMBER_EVEN",
                "PLATFORM_NUMBER_ODD",
                "PLATFORM_NUMBER_DOUBLE_DIGIT",
                "PLATFORM_HAS_LETTER_SUFFIX",
                "PLATFORM_NUMBER_MULTIPLE_OF");
    }

    public enum FieldType {
        STRING,
        NUMBER,
        BOOLEAN,
        ENUM,
        ENUM_ARRAY,
        STOP_POINT_ID,
        ARRAY,
        OBJECT,
        TEMPORAL,
        PLATFORM
    }

    public record FieldCapability(
            String field,
            FieldType type,
            Set<String> operators,
            List<String> enumValues,
            String description) {

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
