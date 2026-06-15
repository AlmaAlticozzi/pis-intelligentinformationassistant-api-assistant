package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataApiQueryContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationResolutionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertDeterministicTechnicalSpecificationBuilderTest {

    private static final String GERUSALEMME = "TNPNTS00000000000012";
    private static final String BIGNAMI = "TNPNTS00000000000001";

    private final ScheduledAlertDeterministicTechnicalSpecificationBuilder builder =
            new ScheduledAlertDeterministicTechnicalSpecificationBuilder();
    private final ScheduledAlertVerificationOutcomeValidator validator =
            new ScheduledAlertVerificationOutcomeValidator();

    @Test
    void buildsVerifiedSpecificationForResolvedJourneyCancellationWithDestinationFilter() {
        ScheduledServiceDataLocationContext context = gerusalemmeWithDestinationFilterContext();
        ScheduledAlertTemporalHints temporalHints = temporalHints(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme con destinazione Bignami");
        ScheduledAlertJourneyCancellationHints cancellationHints = journeyCancellationHints(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme con destinazione Bignami");

        AlertVerificationOutcome outcome = builder.build(
                        "ALRT1",
                        "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme con destinazione Bignami",
                        route(),
                        context,
                        temporalHints,
                        ScheduledAlertPlatformHints.empty(),
                        ScheduledAlertChangeHints.empty(),
                        cancellationHints,
                        ScheduledAlertCancelledCallHints.empty(),
                        ScheduledAlertReplacementHints.empty(),
                        null,
                        "IIA-UTL-TXI-503-001",
                        "scheduled-model",
                        "alert-scheduled-verify-mvp-v1")
                .orElseThrow();

        AlertVerificationOutcome validated = validator.validate(
                outcome,
                context,
                route(),
                temporalHints,
                ScheduledAlertPlatformHints.empty(),
                ScheduledAlertChangeHints.empty(),
                cancellationHints,
                ScheduledAlertCancelledCallHints.empty(),
                ScheduledAlertReplacementHints.empty());

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(validated.technicalSpecification().get("schedule")))
                .containsEntry("frequencySeconds", 600)
                .containsEntry("defaulted", false);
        assertThat(map(validated.technicalSpecification().get("serviceDataQuery")).get("stopPoints"))
                .isEqualTo(List.of(GERUSALEMME));
        String snapshot = String.valueOf(validated.technicalSpecification().get("snapshotEvaluation"));
        assertThat(snapshot)
                .contains("callEnd.stopPoint.id", BIGNAMI)
                .contains("arrivalStatuses[].status", "ARRIVAL_CANCELLATION")
                .contains("departureStatuses[].status", "DEPARTURE_CANCELLATION")
                .doesNotContain("value=" + GERUSALEMME);
        assertThat(map(validated.agentBlueprintPreview().get("runtimeContract")))
                .containsEntry("requiresScheduler", true)
                .containsEntry("executionModel", "SCHEDULED_POLLING");
        assertThat(validated.warnings()).anySatisfy(warning ->
                assertThat(warning).contains("deterministic fallback", "IIA-UTL-TXI-503-001"));
    }

    @Test
    void buildsVerifiedSpecificationForResolvedJourneyCancellationWithoutDestinationFilter() {
        ScheduledServiceDataLocationContext context = gerusalemmeContext();
        ScheduledAlertTemporalHints temporalHints = temporalHints(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme");
        ScheduledAlertJourneyCancellationHints cancellationHints = journeyCancellationHints(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme");

        AlertVerificationOutcome outcome = builder.build(
                        "ALRT1",
                        "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme",
                        route(),
                        context,
                        temporalHints,
                        ScheduledAlertPlatformHints.empty(),
                        ScheduledAlertChangeHints.empty(),
                        cancellationHints,
                        ScheduledAlertCancelledCallHints.empty(),
                        ScheduledAlertReplacementHints.empty(),
                        null,
                        "IIA-UTL-TXI-503-001",
                        "scheduled-model",
                        "alert-scheduled-verify-mvp-v1")
                .orElseThrow();

        AlertVerificationOutcome validated = validator.validate(
                outcome,
                context,
                route(),
                temporalHints,
                ScheduledAlertPlatformHints.empty(),
                ScheduledAlertChangeHints.empty(),
                cancellationHints,
                ScheduledAlertCancelledCallHints.empty(),
                ScheduledAlertReplacementHints.empty());

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(map(validated.technicalSpecification().get("serviceDataQuery")).get("stopPoints"))
                .isEqualTo(List.of(GERUSALEMME));
        assertThat(String.valueOf(validated.technicalSpecification().get("snapshotEvaluation")))
                .contains("ARRIVAL_CANCELLATION", "DEPARTURE_CANCELLATION")
                .doesNotContain("callEnd.stopPoint.id");
    }

    @Test
    void doesNotApplyWhenRequiredLocationIsUnresolved() {
        ScheduledAlertTemporalHints temporalHints = temporalHints(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme");

        assertThat(builder.build(
                "ALRT1",
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme",
                route(),
                unresolvedContext(),
                temporalHints,
                ScheduledAlertPlatformHints.empty(),
                ScheduledAlertChangeHints.empty(),
                journeyCancellationHints("Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme"),
                ScheduledAlertCancelledCallHints.empty(),
                ScheduledAlertReplacementHints.empty(),
                null,
                "IIA-UTL-TXI-503-001",
                "scheduled-model",
                "alert-scheduled-verify-mvp-v1")).isEmpty();
    }

    private AlertRouteUnderstandingResult route() {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                AlertRouteIntentKind.SNAPSHOT_REPORT,
                AlertRouteOutputMode.EVERY_RUN_REPORT,
                true,
                true,
                false,
                true,
                false,
                true,
                0.65,
                "Scheduled route.",
                null,
                List.of());
    }

    private ScheduledAlertTemporalHints temporalHints(String prompt) {
        ScheduledAlertTemporalHintsExtractor extractor = new ScheduledAlertTemporalHintsExtractor();
        extractor.defaultFrequencySeconds = 600;
        extractor.minFrequencySeconds = 60;
        extractor.maxFrequencySeconds = 86400;
        extractor.defaultLookaheadMinutes = 480;
        extractor.minLookaheadMinutes = 1;
        extractor.maxLookaheadMinutes = 1440;
        return extractor.extract(prompt);
    }

    private ScheduledAlertJourneyCancellationHints journeyCancellationHints(String prompt) {
        return new ScheduledAlertJourneyCancellationHintsExtractor().extract(prompt);
    }

    private ScheduledServiceDataLocationContext gerusalemmeWithDestinationFilterContext() {
        return context(List.of(monitored("Gerusalemme", GERUSALEMME)), List.of(destination("Bignami", BIGNAMI)), false);
    }

    private ScheduledServiceDataLocationContext gerusalemmeContext() {
        return context(List.of(monitored("Gerusalemme", GERUSALEMME)), List.of(), false);
    }

    private ScheduledServiceDataLocationContext unresolvedContext() {
        return context(List.of(monitored("Gerusalemme", GERUSALEMME)), List.of(), true);
    }

    private ScheduledServiceDataLocationContext context(
            List<ScheduledServiceDataResolvedLocation> monitored,
            List<ScheduledServiceDataResolvedLocation> filters,
            boolean unresolved) {
        return new ScheduledServiceDataLocationContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored,
                filters,
                List.of(),
                List.of(GERUSALEMME),
                false,
                unresolved,
                unresolved ? List.of("Gerusalemme") : List.of(),
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        List.of(GERUSALEMME),
                        false));
    }

    private ScheduledServiceDataResolvedLocation monitored(String rawText, String id) {
        return new ScheduledServiceDataResolvedLocation(
                rawText,
                rawText,
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(id),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                "");
    }

    private ScheduledServiceDataResolvedLocation destination(String rawText, String id) {
        return new ScheduledServiceDataResolvedLocation(
                rawText,
                rawText,
                ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                false,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(id),
                List.of(),
                false,
                false,
                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id"),
                "");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
