package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceDataCapabilityCatalogTest {

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

    @Test
    void allowedFieldCountIncludesControlledExpansion() {
        assertThat(ServiceDataCapabilityCatalog.allowedFieldCount()).isEqualTo(101);
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
        assertOperators("payload.ongroundServiceEvent.stopPoint.id", "EQUALS", "IN");
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
    void catalogContainsOngroundStopPointId() {
        assertThat(ServiceDataCapabilityCatalog.findField("payload.ongroundServiceEvent.stopPoint.id"))
                .isPresent()
                .get()
                .satisfies(field -> {
                    assertThat(field.type()).isEqualTo(ServiceDataCapabilityCatalog.FieldType.STOP_POINT_ID);
                    assertThat(field.description()).isEqualTo("Stable PIS stop point identifier resolved from points.json");
                    assertThat(field.languageMappings()).contains("location", "stopPoint", "pointId", "station");
                });
    }

    @Test
    void catalogAllowsEqualsOnStopPointId() {
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.ongroundServiceEvent.stopPoint.id",
                "EQUALS"))
                .isTrue();
    }

    @Test
    void catalogAllowsInOnStopPointId() {
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.ongroundServiceEvent.stopPoint.id",
                "IN"))
                .isTrue();
    }

    @Test
    void catalogRejectsContainsNormalizedOnStopPointId() {
        assertThat(ServiceDataCapabilityCatalog.isAllowedOperator(
                "payload.ongroundServiceEvent.stopPoint.id",
                "CONTAINS_NORMALIZED"))
                .isFalse();
    }

    @Test
    void catalogStillAllowsContainsNormalizedOnNameLong() {
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
                .contains("use nameLong/nameShort normalized text only as fallback")
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
