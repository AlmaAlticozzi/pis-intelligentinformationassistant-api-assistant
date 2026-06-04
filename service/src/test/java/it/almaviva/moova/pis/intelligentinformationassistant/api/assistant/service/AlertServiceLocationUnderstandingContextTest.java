package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationMainEventIntent;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRelation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingLocation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingMainEvent;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingNonLocationConstraint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationNonLocationConstraintType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationMockEngine;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationResolverService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationTargetFieldMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.SimpleAlertLocationMentionExtractor;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertServiceLocationUnderstandingContextTest {

    @Test
    void convertsMainEventAndRouteLocationsToRoleBasedContext() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(understanding(
                location("Garibaldi", AlertLocationRole.MAIN_EVENT_LOCATION, AlertLocationRelation.EVENT_STOP_POINT, "G1"),
                location("Venezia", AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION, AlertLocationRelation.FUTURE_ROUTE_CONSTRAINT, "G2")));

        assertThat(context.resolutions()).hasSize(2);
        AlertVerificationLocationContext.LocationResolution garibaldi = context.resolutions().getFirst();
        AlertVerificationLocationContext.LocationResolution venezia = context.resolutions().get(1);
        assertThat(garibaldi.semanticRole()).isEqualTo("MAIN_EVENT_LOCATION");
        assertThat(garibaldi.requiredCoverage()).isTrue();
        assertThat(garibaldi.polarity()).isEqualTo("INCLUDE");
        assertThat(garibaldi.targetFieldHints())
                .contains("payload.stopPointJourney.stopPoint.id", "payload.ongroundServiceEvent.stopPoint.id");
        assertThat(venezia.semanticRole()).isEqualTo("ROUTE_OR_NEXT_CALL_LOCATION");
        assertThat(venezia.targetFieldHints())
                .containsExactly("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id");
    }

    @Test
    void preservesOrAlternativeLogicalGroupForMainEventLocations() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(understanding(
                location("Valdisotto", AlertLocationRole.MAIN_EVENT_LOCATION, AlertLocationRelation.EVENT_STOP_POINT, "G1"),
                location("Monza", AlertLocationRole.MAIN_EVENT_LOCATION, AlertLocationRelation.EVENT_STOP_POINT, "G1"),
                location("Arcore", AlertLocationRole.MAIN_EVENT_LOCATION, AlertLocationRelation.EVENT_STOP_POINT, "G1")));

        assertThat(context.resolutions()).hasSize(3);
        assertThat(context.resolutions())
                .extracting(AlertVerificationLocationContext.LocationResolution::logicalGroup)
                .containsExactly("G1", "G1", "G1");
        assertThat(context.resolutions())
                .extracting(AlertVerificationLocationContext.LocationResolution::semanticRole)
                .containsOnly("MAIN_EVENT_LOCATION");
        assertThat(context.resolutions())
                .allSatisfy(resolution -> assertThat(resolution.status()).isNotBlank());
    }

    @Test
    void mapsOriginAndDestinationToBoundaryFields() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(understanding(
                location("Genova Sestri Ponente", AlertLocationRole.ORIGIN_LOCATION, AlertLocationRelation.ORIGIN_CONSTRAINT, "G1"),
                location("LECCO MAGGIANICO", AlertLocationRole.DESTINATION_LOCATION, AlertLocationRelation.DESTINATION_CONSTRAINT, "G2")));

        assertThat(context.resolutions().getFirst().targetFieldHints())
                .contains(
                        "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id",
                        "payload.stopPointJourney.stopPointsJourneyDetails[].callStart.stopPoint.id");
        assertThat(context.resolutions().get(1).targetFieldHints())
                .contains(
                        "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
                        "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id");
    }

    @Test
    void excludedDestinationLocationIsRequiredCoverageEvenWhenUnderstandingMarksItFalse() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(new AlertLocationUnderstandingResult(
                true,
                "it",
                new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ARRIVAL, 0.90),
                List.of(new AlertLocationUnderstandingLocation(
                        "Bologna",
                        "Bologna",
                        AlertLocationRole.DESTINATION_LOCATION,
                        AlertLocationRelation.DESTINATION_CONSTRAINT,
                        false,
                        AlertLocationPolarity.EXCLUDE,
                        "G1",
                        0.86)),
                List.of(),
                List.of()));

        AlertVerificationLocationContext.LocationResolution bologna = context.resolutions().getFirst();
        assertThat(bologna.semanticRole()).isEqualTo("DESTINATION_LOCATION");
        assertThat(bologna.polarity()).isEqualTo("EXCLUDE");
        assertThat(bologna.requiredCoverage()).isTrue();
    }

    @Test
    void normalizesDestinationExclusionWordingWithoutDeduplicatingSameLocationText() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Avvisami quando una corsa arriva a X ma non ha destinazione X",
                new AlertLocationUnderstandingResult(
                        true,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ARRIVAL, 0.90),
                        List.of(
                                new AlertLocationUnderstandingLocation(
                                        "X",
                                        "X",
                                        AlertLocationRole.MAIN_EVENT_LOCATION,
                                        AlertLocationRelation.EVENT_STOP_POINT,
                                        true,
                                        AlertLocationPolarity.INCLUDE,
                                        "G1",
                                        0.90),
                                new AlertLocationUnderstandingLocation(
                                        "X",
                                        "X",
                                        AlertLocationRole.MAIN_EVENT_LOCATION,
                                        AlertLocationRelation.EVENT_STOP_POINT,
                                        true,
                                        AlertLocationPolarity.EXCLUDE,
                                        "G2",
                                        0.80)),
                        List.of(),
                        List.of()));

        assertThat(context.resolutions()).hasSize(2);
        assertThat(context.resolutions())
                .anySatisfy(location -> {
                    assertThat(location.rawText()).isEqualTo("X");
                    assertThat(location.semanticRole()).isEqualTo("MAIN_EVENT_LOCATION");
                    assertThat(location.relationToMainEvent()).isEqualTo("EVENT_STOP_POINT");
                    assertThat(location.polarity()).isEqualTo("INCLUDE");
                    assertThat(location.targetFieldHints()).contains("payload.stopPointJourney.stopPoint.id");
                })
                .anySatisfy(location -> {
                    assertThat(location.rawText()).isEqualTo("X");
                    assertThat(location.semanticRole()).isEqualTo("DESTINATION_LOCATION");
                    assertThat(location.relationToMainEvent()).isEqualTo("DESTINATION_CONSTRAINT");
                    assertThat(location.polarity()).isEqualTo("EXCLUDE");
                    assertThat(location.targetFieldHints())
                            .contains("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id");
                });
    }

    @Test
    void mapsTransitWithoutTurningItIntoMainEventLocation() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(understanding(
                location("DUOMO", AlertLocationRole.TRANSIT_LOCATION, AlertLocationRelation.TRANSIT_CONSTRAINT, "G1"),
                location("SAN BABILA", AlertLocationRole.TRANSIT_LOCATION, AlertLocationRelation.TRANSIT_CONSTRAINT, "G1")));

        assertThat(context.resolutions())
                .extracting(AlertVerificationLocationContext.LocationResolution::semanticRole)
                .containsOnly("TRANSIT_LOCATION");
        assertThat(context.resolutions().getFirst().targetFieldHints())
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id")
                .doesNotContain("payload.stopPointJourney.stopPoint.id");
    }

    @Test
    void mapsCancelledCallToNextCancelledCalls() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(understanding(
                location("Grosio", AlertLocationRole.CANCELLED_CALL_LOCATION, AlertLocationRelation.CANCELLED_CALL_CONSTRAINT, "G1")));

        assertThat(context.resolutions().getFirst().semanticRole()).isEqualTo("CANCELLED_CALL_LOCATION");
        assertThat(context.resolutions().getFirst().targetFieldHints())
                .containsExactly("payload.stopPointJourney.stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id");
    }

    @Test
    void normalizesProgressiveDepartureFromOriginToMainEventLocation() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Dimmi quando una corsa e in partenza da Garibaldi e passera da Venezia",
                new AlertLocationUnderstandingResult(
                        true,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.DEPARTURE, 0.92),
                        List.of(
                                location("Garibaldi", AlertLocationRole.ORIGIN_LOCATION, AlertLocationRelation.ORIGIN_CONSTRAINT, "G1"),
                                location("Venezia", AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION, AlertLocationRelation.FUTURE_ROUTE_CONSTRAINT, "G2")),
                        List.of(),
                        List.of()));

        assertLocation(context, "Garibaldi", "MAIN_EVENT_LOCATION", "EVENT_LOCATION");
        assertLocation(context, "Venezia", "ROUTE_OR_NEXT_CALL_LOCATION", "FUTURE_ROUTE_CONSTRAINT");
        assertConstraint(context, "MAIN_EVENT_INTENT", "DEPARTURE");
        assertConstraint(context, "MAIN_EVENT_PHASE", "PROGRESSIVE");
        assertConstraint(context, "EXPECTED_MAIN_EVENT_TYPE", "DEPARTING");
    }

    @Test
    void normalizesCompletedDepartureFromOriginToMainEventLocation() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Dimmi quando una corsa parte da Garibaldi e passera da Venezia",
                new AlertLocationUnderstandingResult(
                        true,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.DEPARTURE, 0.92),
                        List.of(
                                location("Garibaldi", AlertLocationRole.ORIGIN_LOCATION, AlertLocationRelation.ORIGIN_CONSTRAINT, "G1"),
                                location("Venezia", AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION, AlertLocationRelation.FUTURE_ROUTE_CONSTRAINT, "G2")),
                        List.of(),
                        List.of()));

        assertLocation(context, "Garibaldi", "MAIN_EVENT_LOCATION", "EVENT_LOCATION");
        assertLocation(context, "Venezia", "ROUTE_OR_NEXT_CALL_LOCATION", "FUTURE_ROUTE_CONSTRAINT");
        assertConstraint(context, "MAIN_EVENT_PHASE", "COMPLETED");
        assertConstraint(context, "EXPECTED_MAIN_EVENT_TYPE", "DEPARTED");
    }

    @Test
    void preservesExplicitJourneyOriginWithRouteLocation() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Avvisami quando una corsa ha origine a Garibaldi e passera da Venezia",
                new AlertLocationUnderstandingResult(
                        true,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ROUTE_TRANSIT, 0.70),
                        List.of(
                                location("Garibaldi", AlertLocationRole.ORIGIN_LOCATION, AlertLocationRelation.ORIGIN_CONSTRAINT, "G1"),
                                location("Venezia", AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION, AlertLocationRelation.FUTURE_ROUTE_CONSTRAINT, "G2")),
                        List.of(),
                        List.of()));

        assertLocation(context, "Garibaldi", "ORIGIN_LOCATION", "ORIGIN_CONSTRAINT");
        assertLocation(context, "Venezia", "ROUTE_OR_NEXT_CALL_LOCATION", "FUTURE_ROUTE_CONSTRAINT");
        assertThat(context.resolutions())
                .extracting(AlertVerificationLocationContext.LocationResolution::semanticRole)
                .doesNotContain("MAIN_EVENT_LOCATION");
    }

    @Test
    void normalizesProgressiveArrivalDestinationToMainEventLocationAndPreservesExplicitDestination() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Avvisami quando una corsa e in arrivo a Monza e ha destinazione Lecco",
                new AlertLocationUnderstandingResult(
                        true,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ARRIVAL, 0.92),
                        List.of(
                                location("Monza", AlertLocationRole.DESTINATION_LOCATION, AlertLocationRelation.DESTINATION_CONSTRAINT, "G1"),
                                location("Lecco", AlertLocationRole.DESTINATION_LOCATION, AlertLocationRelation.DESTINATION_CONSTRAINT, "G2")),
                        List.of(),
                        List.of()));

        assertLocation(context, "Monza", "MAIN_EVENT_LOCATION", "EVENT_LOCATION");
        assertLocation(context, "Lecco", "DESTINATION_LOCATION", "DESTINATION_CONSTRAINT");
        assertConstraint(context, "MAIN_EVENT_PHASE", "PROGRESSIVE");
        assertConstraint(context, "EXPECTED_MAIN_EVENT_TYPE", "ARRIVING");
    }

    @Test
    void normalizesCompletedArrivalDestinationToMainEventLocationAndPreservesExplicitDestination() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Avvisami quando una corsa arriva a Monza e ha destinazione Lecco",
                new AlertLocationUnderstandingResult(
                        true,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ARRIVAL, 0.92),
                        List.of(
                                location("Monza", AlertLocationRole.DESTINATION_LOCATION, AlertLocationRelation.DESTINATION_CONSTRAINT, "G1"),
                                location("Lecco", AlertLocationRole.DESTINATION_LOCATION, AlertLocationRelation.DESTINATION_CONSTRAINT, "G2")),
                        List.of(),
                        List.of()));

        assertLocation(context, "Monza", "MAIN_EVENT_LOCATION", "EVENT_LOCATION");
        assertLocation(context, "Lecco", "DESTINATION_LOCATION", "DESTINATION_CONSTRAINT");
        assertConstraint(context, "MAIN_EVENT_PHASE", "COMPLETED");
        assertConstraint(context, "EXPECTED_MAIN_EVENT_TYPE", "ARRIVED");
    }

    @Test
    void preservesOriginAndDestinationWithoutInventingMainEventLocation() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Avvisami quando una corsa ha origine a Monza e destinazione Lecco",
                new AlertLocationUnderstandingResult(
                        true,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.UNKNOWN, 0.40),
                        List.of(
                                location("Monza", AlertLocationRole.ORIGIN_LOCATION, AlertLocationRelation.ORIGIN_CONSTRAINT, "G1"),
                                location("Lecco", AlertLocationRole.DESTINATION_LOCATION, AlertLocationRelation.DESTINATION_CONSTRAINT, "G2")),
                        List.of(),
                        List.of()));

        assertLocation(context, "Monza", "ORIGIN_LOCATION", "ORIGIN_CONSTRAINT");
        assertLocation(context, "Lecco", "DESTINATION_LOCATION", "DESTINATION_CONSTRAINT");
        assertThat(context.resolutions())
                .extracting(AlertVerificationLocationContext.LocationResolution::semanticRole)
                .doesNotContain("MAIN_EVENT_LOCATION");
    }

    @Test
    void keepsPlatformAsNonLocationConstraintWithoutInventingLocation() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(new AlertLocationUnderstandingResult(
                false,
                "it",
                new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ARRIVAL, 0.80),
                List.of(),
                List.of(new AlertLocationUnderstandingNonLocationConstraint(
                        AlertLocationNonLocationConstraintType.PLATFORM,
                        "binario 1")),
                List.of()));

        assertThat(context.hasLocationMentions()).isFalse();
        assertThat(context.resolutions()).isEmpty();
        assertThat(context.nonLocationConstraints()).hasSize(3);
        assertThat(context.nonLocationConstraints().getFirst().type()).isEqualTo("PLATFORM");
        assertThat(context.nonLocationConstraints())
                .anySatisfy(constraint -> {
                    assertThat(constraint.type()).isEqualTo("MAIN_EVENT_PHASE");
                    assertThat(constraint.rawText()).isEqualTo("AMBIGUOUS");
                });
    }

    @Test
    void addsMainEventIntentAsNonLocationConstraint() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(new AlertLocationUnderstandingResult(
                false,
                "it",
                new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ARRIVAL, 0.80),
                List.of(),
                List.of(new AlertLocationUnderstandingNonLocationConstraint(
                        AlertLocationNonLocationConstraintType.PLATFORM,
                        "binario 1")),
                List.of()));

        assertThat(context.nonLocationConstraints())
                .extracting(AlertVerificationLocationContext.NonLocationConstraint::type)
                .contains("PLATFORM", "MAIN_EVENT_INTENT", "MAIN_EVENT_PHASE");
        assertThat(context.nonLocationConstraints())
                .anySatisfy(constraint -> {
                    assertThat(constraint.type()).isEqualTo("MAIN_EVENT_INTENT");
                    assertThat(constraint.rawText()).isEqualTo("ARRIVAL");
                });
    }

    @Test
    void addsGenericDelayEventTypeForDelayWithoutArrivalOrDepartureDirection() {
        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Avvisami quando una corsa ha più di 15 minuti di ritardo",
                new AlertLocationUnderstandingResult(
                        false,
                        "it",
                        new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.DELAY, 0.90),
                        List.of(),
                        List.of(),
                        List.of()));

        assertThat(context.resolutions()).isEmpty();
        assertConstraint(context, "DELAY_EVENT_TYPE", "BOTH");
        assertConstraint(context, "DELAY_DIRECTION", "GENERIC");
    }

    @Test
    void fallsBackToLegacyExtractorWhenUnderstandingThrows() {
        AlertLocationUnderstandingService understandingService = mock(AlertLocationUnderstandingService.class);
        when(understandingService.understandLocations(any(), any())).thenThrow(new RuntimeException("boom"));

        AlertVerificationLocationContext context = verifyAndCaptureContext(
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                understandingService);

        assertThat(context.hasLocationMentions()).isTrue();
        assertThat(context.resolutions()).hasSize(1);
        assertThat(context.resolutions().getFirst().rawText()).isEqualTo("Rho Fieramilano");
        assertThat(context.resolutions().getFirst().semanticRole()).isEqualTo("DEPARTURE_EVENT_STOP_POINT");
    }

    private AlertVerificationLocationContext verifyAndCaptureContext(AlertLocationUnderstandingResult understanding) {
        return verifyAndCaptureContext("Prompt", understanding);
    }

    private AlertVerificationLocationContext verifyAndCaptureContext(
            String prompt,
            AlertLocationUnderstandingResult understanding) {
        AlertLocationUnderstandingService understandingService = mock(AlertLocationUnderstandingService.class);
        when(understandingService.understandLocations(any(), any())).thenReturn(understanding);
        return verifyAndCaptureContext(prompt, understandingService);
    }

    @SuppressWarnings("unchecked")
    private AlertVerificationLocationContext verifyAndCaptureContext(
            String prompt,
            AlertLocationUnderstandingService understandingService) {
        AlertService service = new AlertService();
        service.alertRepository = mock(AlertRepository.class);
        when(service.alertRepository.getAlertVerificationPromptData("ALRT1"))
                .thenReturn(Optional.of(new AlertVerificationPromptData("ALRT1", "Alert", null, prompt)));
        when(service.alertRepository.verifyAlert(any(), any(), any())).thenReturn(Optional.empty());
        service.alertVerificationPromptBuilder = mock(AlertVerificationPromptBuilder.class);
        when(service.alertVerificationPromptBuilder.build(any())).thenReturn(
                new LlmRequest(AiUseCase.ALERT_VERIFY, "system", "user", "gpt-4.1-mini", 0.1, 5000, "ALRT1"));
        service.alertVerificationLlmResponseParser = new AlertVerificationLlmResponseParser();
        service.alertVerificationOutcomeValidator = mock(AlertVerificationOutcomeValidator.class);
        when(service.alertVerificationOutcomeValidator.validate(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service.alertVerificationMockEngine = mock(AlertVerificationMockEngine.class);
        service.alertLocationMentionExtractor = new SimpleAlertLocationMentionExtractor();
        service.alertLocationResolverService = new AlertLocationResolverService();
        service.alertLocationUnderstandingService = understandingService;
        service.alertLocationTargetFieldMapper = new AlertLocationTargetFieldMapper();
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse("""
                {"decision":"REJECTED","summary":"Rejected.","rejectedReason":"Rejected.","confidence":0.0,"warnings":[],"safetyChecks":[]}
                """, "FAKE", "fake-model", null, null, null));
        service.aiConfiguration = mock(AiConfiguration.class);
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(alertVerify.simulateProviderTimeout()).thenReturn(false);
        when(service.aiConfiguration.alertVerify()).thenReturn(alertVerify);
        service.locationUnderstandingEnabled = true;

        service.verifyAlert("ALRT1", new AlertVerificationRequest());

        ArgumentCaptor<AlertVerificationPromptData> promptData = ArgumentCaptor.forClass(AlertVerificationPromptData.class);
        verify(service.alertVerificationPromptBuilder).build(promptData.capture());
        return promptData.getValue().locationResolutionContext();
    }

    private AlertLocationUnderstandingResult understanding(AlertLocationUnderstandingLocation... locations) {
        return new AlertLocationUnderstandingResult(
                true,
                "it",
                new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.DEPARTURE, 0.90),
                List.of(locations),
                List.of(),
                List.of());
    }

    private AlertLocationUnderstandingLocation location(
            String rawText,
            AlertLocationRole role,
            AlertLocationRelation relation,
            String logicalGroup) {
        return new AlertLocationUnderstandingLocation(
                rawText,
                rawText,
                role,
                relation,
                true,
                AlertLocationPolarity.INCLUDE,
                logicalGroup,
                0.90);
    }

    private void assertLocation(
            AlertVerificationLocationContext context,
            String rawText,
            String semanticRole,
            String relationToMainEvent) {
        assertThat(context.resolutions())
                .anySatisfy(location -> {
                    assertThat(location.rawText()).isEqualTo(rawText);
                    assertThat(location.semanticRole()).isEqualTo(semanticRole);
                    assertThat(location.relationToMainEvent()).isEqualTo(relationToMainEvent);
                    assertThat(location.requiredCoverage()).isTrue();
                    assertThat(location.polarity()).isEqualTo("INCLUDE");
                });
    }

    private void assertConstraint(
            AlertVerificationLocationContext context,
            String type,
            String rawText) {
        assertThat(context.nonLocationConstraints())
                .anySatisfy(constraint -> {
                    assertThat(constraint.type()).isEqualTo(type);
                    assertThat(constraint.rawText()).isEqualTo(rawText);
                });
    }
}
