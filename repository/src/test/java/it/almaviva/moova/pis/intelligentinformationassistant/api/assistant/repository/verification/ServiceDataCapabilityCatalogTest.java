package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceDataCapabilityCatalogTest {

    private static final List<String> STOP_POINT_ID_FIELDS = List.of(
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

    private static final List<String> NEW_FIELDS = List.of(
            "payload.ongroundServiceEvent.stopPoint.id",
            "payload.ongroundServiceEvent.stopPoint.nameShort",
            "payload.stopPointJourney.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameShort",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.nameLong",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.nameShort",
            "payload.stopPointJourney.stopPointsJourneyDetails[].callStart.stopPoint.nameLong",
            "payload.stopPointJourney.stopPointsJourneyDetails[].callStart.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.nameLong",
            "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isConfirmed",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isUnknown",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isBusy",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.isConfirmed",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.isUnknown",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.isBusy",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.source",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.source",
            "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.roundedDelay",
            "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.roundedDelay",
            "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.isUnknown",
            "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.isUnknown",
            "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.source",
            "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.source",
            "payload.stopPointJourney.stopPointsJourneyDetails[].missedEvents",
            "payload.stopPointJourney.stopPointsJourneyDetails[].changes",
            "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].arrivalTime",
            "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].departureTime",
            "payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].arrivalTime",
            "payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].departureTime",
            "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].replacementType",
            "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].replacementType");

    private static final List<String> PLATFORM_DESCRIPTION_FIELDS = List.of(
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.platform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.displayPlatform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousArrivalPlatform.platform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousArrivalPlatform.displayPlatform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousDeparturePlatform.platform.dsc",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousDeparturePlatform.displayPlatform.dsc");

    private static final List<String> PLATFORM_TECHNICAL_ID_FIELDS = List.of(
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.displayPlatform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.platform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.platform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousArrivalPlatform.displayPlatform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousArrivalPlatform.platform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousDeparturePlatform.displayPlatform.id",
            "payload.stopPointJourney.stopPointsJourneyDetails[].previousDeparturePlatform.platform.id");

    @Test
    void allowedFieldCountIncludesControlledExpansion() {
        assertThat(ServiceDataCapabilityCatalog.allowedFieldCount()).isEqualTo(107);
    }

    @Test
    void allNewFieldsAreAllowed() {
        assertThat(NEW_FIELDS)
                .allSatisfy(field -> assertThat(ServiceDataCapabilityCatalog.isAllowedField(field))
                        .as(field)
                        .isTrue());
    }

    @Test
    void stringBooleanEnumNumberAndEnumArrayOperatorsAreCoherent() {
        assertOperators("payload.ongroundServiceEvent.stopPoint.id", "EQUALS", "IN", "NOT_IN");
        assertOperators("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.nameLong",
                "EQUALS_NORMALIZED", "CONTAINS_NORMALIZED");
        assertOperators("payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isConfirmed", "EQUALS");
        assertOperators("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.source", "EQUALS", "IN");
        assertOperators("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.roundedDelay",
                "EXISTS", "GREATER_THAN", "GREATER_OR_EQUAL", "LESS_THAN", "LESS_OR_EQUAL", "EQUALS");
        assertOperators("payload.stopPointJourney.stopPointsJourneyDetails[].changes", "CONTAINS", "CONTAINS_ANY");

        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.isUnknown",
                "CONTAINS"))
                .isFalse();
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.roundedDelay",
                "CONTAINS_NORMALIZED"))
                .isFalse();
    }

    @Test
    void catalogContainsAllStopPointIdFields() {
        assertThat(ServiceDataCapabilityCatalog.stopPointIdFields()).containsExactlyElementsOf(STOP_POINT_ID_FIELDS);
        assertThat(STOP_POINT_ID_FIELDS)
                .allSatisfy(field -> assertThat(ServiceDataCapabilityCatalog.findField(field))
                        .as(field)
                        .isPresent()
                        .get()
                        .satisfies(capability -> {
                            assertThat(capability.type()).isEqualTo(ServiceDataCapabilityCatalog.FieldType.STOP_POINT_ID);
                            assertThat(capability.description())
                                    .isEqualTo("Stable PIS stop point identifier resolved from points.json");
                            assertThat(capability.languageMappings())
                                    .contains("location", "stopPoint", "pointId", "station");
                        }));
    }

    @Test
    void catalogAllowsEqualsInAndNotInOnAllStopPointIdFields() {
        assertThat(STOP_POINT_ID_FIELDS).allSatisfy(field -> {
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "EQUALS")).as(field).isTrue();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "IN")).as(field).isTrue();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "NOT_IN")).as(field).isTrue();
        });
    }

    @Test
    void catalogRejectsTextOperatorsOnAllStopPointIdFields() {
        assertThat(STOP_POINT_ID_FIELDS).allSatisfy(field -> {
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "CONTAINS_NORMALIZED")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "CONTAINS")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "EQUALS_NORMALIZED")).as(field).isFalse();
        });
    }

    @Test
    void platformDescriptionFieldsAllowOnlyPlatformOperators() {
        assertThat(PLATFORM_DESCRIPTION_FIELDS).allSatisfy(field -> {
            assertThat(ServiceDataCapabilityCatalog.findField(field))
                    .as(field)
                    .isPresent()
                    .get()
                    .satisfies(capability -> {
                        assertThat(capability.type()).isEqualTo(ServiceDataCapabilityCatalog.FieldType.PLATFORM);
                        assertThat(capability.operators()).containsExactlyInAnyOrder(
                                "EQUAL_PLATFORM", "NOT_EQUAL_PLATFORM", "IN_PLATFORMS", "NOT_IN_PLATFORMS");
                    });
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "CONTAINS")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "CONTAINS_NORMALIZED")).as(field).isFalse();
        });
    }

    @Test
    void platformOperatorsAreNotAllowedOnTechnicalIds() {
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.ongroundServiceEvent.stopPoint.id",
                "EQUAL_PLATFORM"))
                .isFalse();
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.id",
                "EQUAL_PLATFORM"))
                .isFalse();
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.id",
                "CONTAINS_NORMALIZED"))
                .isFalse();
    }

    @Test
    void platformTechnicalIdsAreRecognizedAndCannotBeUsedForMatching() {
        assertThat(PLATFORM_TECHNICAL_ID_FIELDS).allSatisfy(field -> {
            assertThat(ServiceDataCapabilityCatalog.isPlatformTechnicalIdField(field)).as(field).isTrue();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "EQUALS")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "EQUALS_IGNORE_CASE")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "EQUALS_NORMALIZED")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "CONTAINS")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "CONTAINS_IGNORE_CASE")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "CONTAINS_NORMALIZED")).as(field).isFalse();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "IN")).as(field).isFalse();
        });
    }

    @Test
    void catalogStillAllowsContainsNormalizedOnStopPointNameLong() {
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.ongroundServiceEvent.stopPoint.nameLong",
                "CONTAINS_NORMALIZED"))
                .isTrue();
    }

    @Test
    void compactPromptCatalogMentionsStopPointIdResolution() {
        assertThat(ServiceDataCapabilityCatalog.compactPromptCatalog())
                .contains("use stopPoint.id when a user location has been resolved")
                .contains("use EQUALS for one resolved id")
                .contains("use NOT_IN to exclude resolved candidate ids")
                .contains("use nameLong/nameShort CONTAINS_NORMALIZED only as fallback")
                .contains("payload.ongroundServiceEvent.stopPoint.id")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id");
    }

    @Test
    void sourceAndReplacementTypeEnumValuesAreValidated() {
        assertThat(ServiceDataCapabilityCatalog.isAllowedEnumValue(
                "payload.stopPointJourney.stopPointsJourneyDetails[].actualArrivalPlatform.source",
                "MONITORED"))
                .isTrue();
        assertThat(ServiceDataCapabilityCatalog.isAllowedEnumValue(
                "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.source",
                "operator"))
                .isTrue();
        assertThat(ServiceDataCapabilityCatalog.isAllowedEnumValue(
                "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].replacementType",
                "ARRIVALDEPARTURE"))
                .isTrue();
        assertThat(ServiceDataCapabilityCatalog.isAllowedEnumValue(
                "payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].replacementType",
                "UNKNOWN"))
                .isFalse();
    }

    @Test
    void replacementTemporalFieldsAcceptTemporalOperators() {
        List<String> temporalFields = List.of(
                "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].arrivalTime",
                "payload.stopPointJourney.stopPointsJourneyDetails[].replacement.stopPointReplacements[].departureTime",
                "payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].arrivalTime",
                "payload.stopPointJourney.stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].departureTime");

        assertThat(temporalFields).allSatisfy(field -> {
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "LOCAL_TIME_BETWEEN")).as(field).isTrue();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "LOCAL_DAY_OF_WEEK_IN")).as(field).isTrue();
            assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(field, "LOCAL_DAY_OF_WEEK_NOT_IN")).as(field).isTrue();
            assertThat(ServiceDataTemporalCapabilityCatalog.isAllowedOperator(field, "LOCAL_TIME_BETWEEN")).as(field).isTrue();
            assertThat(ServiceDataTemporalCapabilityCatalog.isAllowedOperator(field, "LOCAL_DAY_OF_WEEK_IN")).as(field).isTrue();
            assertThat(ServiceDataTemporalCapabilityCatalog.isAllowedOperator(field, "LOCAL_DAY_OF_WEEK_NOT_IN")).as(field).isTrue();
        });
    }

    private void assertOperators(String field, String... operators) {
        assertThat(ServiceDataCapabilityCatalog.findField(field))
                .as(field)
                .isPresent()
                .get()
                .extracting(ServiceDataCapabilityCatalog.FieldCapability::operators)
                .satisfies(actual -> assertThat(actual).containsExactlyInAnyOrder(operators));
    }
}
