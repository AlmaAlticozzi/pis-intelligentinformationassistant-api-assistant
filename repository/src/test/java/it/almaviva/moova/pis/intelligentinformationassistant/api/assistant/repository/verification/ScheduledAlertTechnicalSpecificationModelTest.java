package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertTechnicalSpecificationModelTest {

    @Test
    void minimalScheduledTechnicalSpecificationModelContainsExpectedSnapshotMetadata() {
        ScheduledAlertTechnicalSpecification specification = new ScheduledAlertTechnicalSpecification(
                ScheduledAlertTechnicalSpecificationConstants.TECHNICAL_SPECIFICATION_SCHEMA_VERSION,
                ScheduledAlertTechnicalSpecificationConstants.SOURCE,
                ScheduledAlertTechnicalSpecificationConstants.INTERPRETER_TYPE,
                ScheduledAlertTechnicalSpecificationConstants.ACCESS_MODE,
                ScheduledAlertTechnicalSpecificationConstants.INPUT_MODEL,
                ScheduledAlertTechnicalSpecificationConstants.OUTPUT_MODEL,
                ScheduledAlertTechnicalSpecificationConstants.TRIGGER_TYPE,
                ScheduledAlertTechnicalSpecificationConstants.EVALUATION_MODE,
                new ScheduledSchedulePolicy(600, false, "Ogni 10 minuti"),
                new ScheduledServiceDataQuery(
                        ScheduledAlertTechnicalSpecificationConstants.DEFAULT_OPERATION,
                        "EXPLICIT_STOP_POINTS",
                        List.of("TNPNTS00000000005439"),
                        false,
                        new ScheduledServiceDataQueryTimeWindow(
                                ScheduledServiceDataQueryWindowStartMode.NOW_TRUNCATED_TO_MINUTE,
                                ScheduledServiceDataQueryWindowEndMode.NOW_PLUS_DEFAULT_LOOKAHEAD,
                                120,
                                true,
                                null)),
                new ScheduledSnapshotEvaluation(
                        ScheduledSnapshotEvaluationMode.COUNT_MATCHING_JOURNEYS,
                        "stopPointsJourneyDetails[]",
                        new ScheduledConditionNode(
                                "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                                List.of(),
                                List.of(),
                                null,
                                null,
                                "stopPointsJourneyDetails[].arrivalStatuses[].status",
                                "CONTAINS",
                                "ARRIVING",
                                null,
                                null),
                        new ScheduledThreshold(ScheduledThresholdOperator.GREATER_OR_EQUAL, 2)),
                new ScheduledOutputPolicy(ScheduledOutputEmitMode.ON_MATCH, true, true),
                ScheduledAlertTechnicalSpecificationConstants.DEFAULT_DEDUPLICATION_KEY_TEMPLATE);

        assertThat(specification.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(specification.accessMode()).isEqualTo("SERVICE_DATA_API_SNAPSHOT");
        assertThat(specification.triggerType()).isEqualTo("SCHEDULE");
        assertThat(specification.evaluationMode()).isEqualTo("SCHEDULED_SNAPSHOT_MATCH");
        assertThat(specification.inputModel()).isEqualTo("ServiceDataStopPointJourneysV2");
        assertThat(specification.serviceDataQuery().operation()).isEqualTo("POST /v2/stoppointjourneys");
        assertThat(specification.serviceDataQuery().stopPoints()).containsExactly("TNPNTS00000000005439");
        assertThat(specification.snapshotEvaluation().mode())
                .isEqualTo(ScheduledSnapshotEvaluationMode.COUNT_MATCHING_JOURNEYS);
        assertThat(specification.snapshotEvaluation().threshold().operator())
                .isEqualTo(ScheduledThresholdOperator.GREATER_OR_EQUAL);
        assertThat(specification.toString()).doesNotContain("payload.ongroundServiceEvent");
    }

    @Test
    void minimalScheduledAgentBlueprintPreviewModelContainsExpectedMetadata() {
        ScheduledAgentBlueprintPreview preview = new ScheduledAgentBlueprintPreview(
                ScheduledAlertTechnicalSpecificationConstants.AGENT_BLUEPRINT_SCHEMA_VERSION,
                ScheduledAlertTechnicalSpecificationConstants.DEFAULT_AGENT_NAME,
                "Periodically queries ServiceData stop point journeys and evaluates a snapshot condition.",
                ScheduledAlertTechnicalSpecificationConstants.TRIGGER_TYPE,
                List.of(ScheduledAlertTechnicalSpecificationConstants.SOURCE),
                ScheduledAlertTechnicalSpecificationConstants.EVALUATION_MODE,
                List.of(ScheduledAlertTechnicalSpecificationConstants.DEFAULT_TARGET_TYPE),
                Map.of(
                        "serviceDataQuery", Map.of(),
                        "snapshotEvaluation", Map.of(),
                        "outputPolicy", Map.of()),
                new ScheduledAgentBlueprintPreview.ScheduledAgentStateRequirements(false),
                new ScheduledAgentBlueprintPreview.ScheduledAgentOutput("CANDIDATE_SUGGESTION"));

        assertThat(preview.agentName()).isEqualTo("ScheduledServiceDataSnapshotAlertAgent");
        assertThat(preview.triggerType()).isEqualTo("SCHEDULE");
        assertThat(preview.evaluationMode()).isEqualTo("SCHEDULED_SNAPSHOT_MATCH");
        assertThat(preview.requiredSources()).containsExactly("SERVICE_DATA");
        assertThat(preview.targetTypes()).containsExactly("SERVICE_DATA_JOURNEY_AGGREGATE");
        assertThat(preview.parameters()).containsKeys("serviceDataQuery", "snapshotEvaluation", "outputPolicy");
        assertThat(preview.stateRequirements().requiresState()).isFalse();
        assertThat(preview.output().type()).isEqualTo("CANDIDATE_SUGGESTION");
    }
}
