package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledSnapshotEvaluationCanonicalizerTest {

    private final ScheduledSnapshotEvaluationCanonicalizer canonicalizer =
            new ScheduledSnapshotEvaluationCanonicalizer();

    @Test
    void canonicalizesAllRuntimeScheduledModesFromVerifiedTechnicalSpecification() {
        assertCanonical("REPORT_COUNT", null, "EVERY_RUN");
        assertCanonical("COUNT_MATCHING_JOURNEYS", Map.of("operator", "GREATER_OR_EQUAL", "value", 3), "ON_MATCH");
        assertCanonical("BOOLEAN_EXISTS", null, "ON_MATCH");
    }

    private void assertCanonical(String mode, Map<String, Object> threshold, String emit) {
        Map<String, Object> verifiedSnapshot = new LinkedHashMap<>();
        verifiedSnapshot.put("mode", mode);
        verifiedSnapshot.put("journeyPath", "stopPointsJourneyDetails[]");
        verifiedSnapshot.put("condition", condition());
        if (threshold != null) {
            verifiedSnapshot.put("threshold", threshold);
        }
        Map<String, Object> technicalSpecification = Map.of(
                "snapshotEvaluation", verifiedSnapshot,
                "outputPolicy", Map.of("emit", emit, "includeCount", true, "includeMatchingJourneys", true));
        Map<String, Object> partialSnapshot = new LinkedHashMap<>();
        partialSnapshot.put("mode", "BOOLEAN_EXISTS");
        partialSnapshot.put("condition", condition());
        partialSnapshot.put("threshold", Map.of("operator", "EQUALS", "value", 99));
        Map<String, Object> blueprint = Map.of(
                "previewMetadata", "preserved",
                "parameters", Map.of("snapshotEvaluation", partialSnapshot));

        AlertVerificationOutcome canonical = canonicalizer
                .normalizeScheduledTechnicalSpecificationAndBlueprint(outcome(technicalSpecification, blueprint), null)
                .outcome();

        Map<String, Object> parameters = map(canonical.agentBlueprintPreview().get("parameters"));
        Map<String, Object> snapshot = map(parameters.get("snapshotEvaluation"));
        assertThat(snapshot).containsEntry("mode", mode)
                .containsEntry("journeyPath", "stopPointsJourneyDetails[]")
                .containsEntry("condition", condition());
        if (threshold == null) {
            assertThat(snapshot).doesNotContainKey("threshold");
        } else {
            assertThat(map(snapshot.get("threshold")))
                    .containsExactlyInAnyOrderEntriesOf(threshold);
        }
        assertThat(parameters.get("outputPolicy"))
                .isEqualTo(technicalSpecification.get("outputPolicy"));
        assertThat(canonical.agentBlueprintPreview()).containsEntry("previewMetadata", "preserved");
    }

    private AlertVerificationOutcome outcome(
            Map<String, Object> technicalSpecification,
            Map<String, Object> blueprint) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED, "ok", null, 1.0, "test", "test", "test",
                List.of("SERVICE_DATA"), "SCHEDULED_INTERPRETER", "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION", "SCHEDULE", "SCHEDULED_SNAPSHOT_MATCH",
                List.of(), List.of(), technicalSpecification, blueprint, Map.of(), List.of(), List.of());
    }

    private Map<String, Object> condition() {
        return Map.of("type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH", "anyElement", Map.of(
                "path", "stopPointsJourneyDetails[]", "conditions", Map.of(
                        "field", "departureDelay.delay", "operator", "GREATER_THAN", "value", 0)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
