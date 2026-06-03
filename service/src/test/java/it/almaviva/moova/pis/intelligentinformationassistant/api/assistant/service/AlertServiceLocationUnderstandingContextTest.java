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
        assertThat(context.nonLocationConstraints()).hasSize(2);
        assertThat(context.nonLocationConstraints().getFirst().type()).isEqualTo("PLATFORM");
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
                .contains("PLATFORM", "MAIN_EVENT_INTENT");
        assertThat(context.nonLocationConstraints())
                .anySatisfy(constraint -> {
                    assertThat(constraint.type()).isEqualTo("MAIN_EVENT_INTENT");
                    assertThat(constraint.rawText()).isEqualTo("ARRIVAL");
                });
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
        AlertLocationUnderstandingService understandingService = mock(AlertLocationUnderstandingService.class);
        when(understandingService.understandLocations(any(), any())).thenReturn(understanding);
        return verifyAndCaptureContext("Prompt", understandingService);
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
}
