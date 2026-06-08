package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledServiceDataCapabilityCatalogTest {

    private static final List<String> EVENT_ONLY_FIELDS = List.of(
            "payload.ongroundServiceEvent.eventsType",
            "payload.ongroundServiceEvent.stopPoint.id",
            "payload.ongroundServiceEvent.eventGenerationTime");

    private static final List<String> SNAPSHOT_FIELDS = List.of(
            "stopPoint.id",
            "stopPointsJourneyDetails[].vehicleJourneyName",
            "stopPointsJourneyDetails[].arrivalDelay.delay",
            "stopPointsJourneyDetails[].departureDelay.delay",
            "stopPointsJourneyDetails[].arrivalStatuses[].status",
            "stopPointsJourneyDetails[].departureStatuses[].status",
            "stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc",
            "stopPointsJourneyDetails[].nextCalls[].stopPoint.id",
            "stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id",
            "stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id",
            "stopPointsJourneyDetails[].changes",
            "stopPointsJourneyDetails[].exclusion.totalExclusion");

    @Test
    void catalogDoesNotContainEventOnlyFields() {
        assertThat(EVENT_ONLY_FIELDS)
                .allSatisfy(field -> assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedField(field))
                        .as(field)
                        .isFalse());
    }

    @Test
    void catalogContainsSnapshotFields() {
        assertThat(SNAPSHOT_FIELDS)
                .allSatisfy(field -> assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedField(field))
                        .as(field)
                        .isTrue());
    }

    @Test
    void catalogContainsQueryCoverageAliasesOnlyForRequirementCoverage() {
        assertThat(List.of(
                "serviceDataQuery.stopPoints",
                "serviceDataQuery.stopPoints[]",
                "body.stopPoints[]",
                "serviceDataQuery.timeWindow",
                "serviceDataQuery.timeWindow.lookaheadMinutes",
                "serviceDataQuery.timeWindow.startMode",
                "serviceDataQuery.timeWindow.endMode",
                "serviceDataQuery.timeWindow.defaulted",
                "serviceDataQuery.timeWindow.rawText"))
                .allSatisfy(field -> {
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedQueryCoverageField(field))
                            .as(field)
                            .isTrue();
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedRequirementCoverageField(field))
                            .as(field)
                            .isTrue();
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedField(field))
                            .as(field)
                            .isFalse();
                });
    }

    @Test
    void catalogContainsTechnicalSpecCoverageFieldsOnlyForRequirementCoverage() {
        assertThat(List.of(
                "snapshotEvaluation.mode",
                "snapshotEvaluation.threshold",
                "snapshotEvaluation.threshold.operator",
                "snapshotEvaluation.threshold.value",
                "outputPolicy.emit",
                "outputPolicy.includeCount",
                "outputPolicy.includeMatchingJourneys",
                "schedule.frequencySeconds",
                "schedule.defaulted",
                "schedule.rawText"))
                .allSatisfy(field -> {
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedTechnicalSpecCoverageField(field))
                            .as(field)
                            .isTrue();
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedRequirementCoverageField(field))
                            .as(field)
                            .isTrue();
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedField(field))
                            .as(field)
                            .isFalse();
                });
    }

    @Test
    void platformFieldsExposePlatformOperators() {
        assertThat(ScheduledServiceDataCapabilityCatalog.findField(
                "stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc"))
                .isPresent()
                .get()
                .satisfies(capability -> {
                    assertThat(capability.type()).isEqualTo(ScheduledServiceDataCapabilityCatalog.FieldType.PLATFORM);
                    assertThat(capability.operators()).containsExactlyInAnyOrder(
                            "EQUAL_PLATFORM", "NOT_EQUAL_PLATFORM", "IN_PLATFORMS", "NOT_IN_PLATFORMS",
                            "PLATFORM_EQUALS_FIELD", "PLATFORM_NOT_EQUALS_FIELD",
                            "PLATFORM_NUMBER_GREATER_THAN", "PLATFORM_NUMBER_GREATER_OR_EQUAL",
                            "PLATFORM_NUMBER_LESS_THAN", "PLATFORM_NUMBER_LESS_OR_EQUAL",
                            "PLATFORM_NUMBER_BETWEEN", "PLATFORM_NUMBER_EVEN", "PLATFORM_NUMBER_ODD",
                            "PLATFORM_NUMBER_DOUBLE_DIGIT", "PLATFORM_HAS_LETTER_SUFFIX",
                            "PLATFORM_NUMBER_MULTIPLE_OF");
                });
    }

    @Test
    void stopPointIdFieldsExposeEqualsInAndNotIn() {
        assertThat(List.of(
                "stopPoint.id",
                "stopPointsJourneyDetails[].callStart.stopPoint.id",
                "stopPointsJourneyDetails[].nextCalls[].stopPoint.id"))
                .allSatisfy(field -> {
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedOperator(field, "EQUALS"))
                            .as(field)
                            .isTrue();
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedOperator(field, "IN"))
                            .as(field)
                            .isTrue();
                    assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedOperator(field, "NOT_IN"))
                            .as(field)
                            .isTrue();
                });
    }

    @Test
    void delayFieldsExposeNumericComparisonOperators() {
        assertThat(ScheduledServiceDataCapabilityCatalog.findField("stopPointsJourneyDetails[].arrivalDelay.delay"))
                .isPresent()
                .get()
                .extracting(ScheduledServiceDataCapabilityCatalog.FieldCapability::operators)
                .satisfies(operators -> assertThat(operators).containsExactlyInAnyOrder(
                        "EXISTS", "GREATER_THAN", "GREATER_OR_EQUAL", "LESS_THAN", "LESS_OR_EQUAL", "EQUALS"));
    }

    @Test
    void statusFieldsExposeContainsOperatorsAndKnownValues() {
        assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedOperator(
                "stopPointsJourneyDetails[].arrivalStatuses[].status", "CONTAINS"))
                .isTrue();
        assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedOperator(
                "stopPointsJourneyDetails[].departureStatuses[].status", "CONTAINS_ANY"))
                .isTrue();
        assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedEnumValue(
                "stopPointsJourneyDetails[].arrivalStatuses[].status", "ARRIVAL_PLATFORM_CHANGED"))
                .isTrue();
        assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedEnumValue(
                "stopPointsJourneyDetails[].departureStatuses[].status", "DEPARTURE_CANCELLATION"))
                .isTrue();
        assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedEnumValue(
                "stopPointsJourneyDetails[].departureStatuses[].status", "UNKNOWN_EVENT"))
                .isFalse();
    }

    @Test
    void compactPromptCatalogDescribesScheduledSnapshotAndExcludesEventEnvelope() {
        String promptCatalog = ScheduledServiceDataCapabilityCatalog.compactPromptCatalog();
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][CATALOG] fields="
                + ScheduledServiceDataCapabilityCatalog.fields().size());

        assertThat(promptCatalog)
                .contains("ServiceData API snapshot")
                .contains("StopPointJourneyV2 snapshot response")
                .contains("ServiceDataStopPointJourneysV2")
                .contains("Use stopPointsJourneyDetails[] anyElement")
                .contains("Use nested anyElement")
                .doesNotContain("payload.ongroundServiceEvent");
    }

    @Test
    void changesContainScheduledSnapshotValues() {
        assertThat(List.of(
                "CANCELLATION",
                "CHANGED_ORIGIN",
                "CHANGED_DESTINATION",
                "CHANGED_PATH",
                "EXTRA_JOURNEY",
                "PLATFORM_CHANGED",
                "PARTIALLY_CANCELLATION",
                "OTHER"))
                .allSatisfy(value -> assertThat(ScheduledServiceDataCapabilityCatalog.isAllowedEnumValue(
                        "stopPointsJourneyDetails[].changes", value))
                        .as(value)
                        .isTrue());
    }
}
