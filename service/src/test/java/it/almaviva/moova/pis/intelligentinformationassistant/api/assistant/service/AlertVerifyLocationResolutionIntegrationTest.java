package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertJourneyReferenceKind;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertJourneyReferenceValueCombination;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationMainEventIntent;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationNonLocationConstraintType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRelation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingLocation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingMainEvent;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingNonLocationConstraint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationMockEngine;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationResolverService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.SimpleAlertLocationMentionExtractor;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertVerifyLocationResolutionIntegrationTest {

    private static final String RHO_ID = "TNPNTS00000000005467";
    private static final String MALPENSA_T1_ID = "TNPNTS00000000000028";
    private static final String MALPENSA_T2_ID = "TNPNTS00000000000029";
    private static final String LUNIGIANA_ID = "TNPNTS00000000000031";
    private static final String GARIBALDI_FS_ID = "TNPNTS00000000000009";
    private static final String BICOCCA_ID = "TNPNTS00000000000003";
    private static final String ZARA_ID = "TNPNTS00000000000007";
    private static final String MARCHE_ID = "TNPNTS00000000000006";

    @Test
    void rhoExactProducesStopPointIdEquals() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                verifiedOutcomeJson(stopPointEqualsCondition(RHO_ID), coverage("payload.ongroundServiceEvent.stopPoint.id")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("payload.ongroundServiceEvent.stopPoint.id", "EQUALS", RHO_ID);
        assertThat(outcome.confidence()).isEqualTo(0.8);
    }

    @Test
    void malpensaProducesStopPointIdInAndLightWarning() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Malpensa",
                verifiedOutcomeJson(
                        stopPointInCondition(List.of(MALPENSA_T1_ID, MALPENSA_T2_ID)),
                        coverage("payload.ongroundServiceEvent.stopPoint.id")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("payload.ongroundServiceEvent.stopPoint.id", "IN", MALPENSA_T1_ID, MALPENSA_T2_ID);
        assertThat(outcome.warnings())
                .contains("One or more alert locations resolved to multiple stopPoint.id candidates; confidence slightly reduced.");
        assertThat(outcome.confidence()).isEqualTo(0.75);
    }

    @Test
    void genovaNerviUnresolvedUsesNameLongFallbackAndLowConfidence() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando un treno passa da Genova Nervi",
                verifiedOutcomeJson(
                        nextCallNameLongFallbackCondition("Genova Nervi"),
                        coverage("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.nameLong")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("nextCalls", "stopPoint.nameLong", "CONTAINS_NORMALIZED", "Genova Nervi");
        assertThat(outcome.warnings())
                .contains("One or more alert locations were unresolved and require nameLong/nameShort fallback; confidence reduced.");
        assertThat(outcome.confidence()).isEqualTo(0.25);
    }

    @Test
    void noLocationPromptDoesNotInventLocationFilter() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Dimmi quando una metro e in ritardo di oltre 10 min",
                verifiedOutcomeJson(
                        eventsTypeCondition("DEPARTED"),
                        coverage("payload.ongroundServiceEvent.eventsType")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().get("condition").toString()).doesNotContain("stopPoint");
        assertThat(outcome.warnings()).isEmpty();
    }

    @Test
    void bareGenericJourneyEventAlternativesDiscardSpuriousJourneyReference() {
        VerificationRun run = verifyWithLlmOutcomeAndUnderstanding(
                "Avvertimi quando una corsa \u00e8 partita o \u00e8 arrivata",
                verifiedOutcomeJson(
                        eventsTypeAnyCondition(List.of("DEPARTED", "ARRIVED")),
                        coverage("payload.ongroundServiceEvent.eventsType")),
                bareEntityJourneyReferenceUnderstanding(
                        "una corsa",
                        "corsa",
                        "corsa",
                        AlertLocationMainEventIntent.DEPARTURE_OR_ARRIVAL));
        AlertVerificationOutcome outcome = run.outcome();

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("DEPARTED", "ARRIVED")
                .doesNotContain("vehicleJourneyName", "line.dsc", "serviceCategory.dsc", "transportOperator.dsc");
        assertThat(run.logs())
                .contains("reason=no-semantic-value-beyond-entity-head")
                .contains("source=LLM_UNDERSTANDING")
                .contains("constraints=[]");
    }

    @Test
    void bareProgressiveArrivalDoesNotCreateJourneyReference() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcomeAndUnderstanding(
                "Avvertimi quando una corsa \u00e8 in arrivo",
                verifiedOutcomeJson(
                        eventsTypeCondition("ARRIVING"),
                        coverage("payload.ongroundServiceEvent.eventsType")),
                bareEntityJourneyReferenceUnderstanding(
                        "una corsa",
                        "corsa",
                        "corsa",
                        AlertLocationMainEventIntent.ARRIVAL)).outcome();

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("ARRIVING")
                .doesNotContain("vehicleJourneyName", "line.dsc", "serviceCategory.dsc", "transportOperator.dsc");
    }

    @Test
    void bareDelayDoesNotCreateJourneyReference() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcomeAndUnderstanding(
                "Avvertimi quando una corsa ha pi\u00f9 di 10 minuti di ritardo",
                verifiedOutcomeJson(
                        genericDelayGreaterThanCondition(600),
                        coverage(
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay")),
                bareEntityJourneyReferenceUnderstanding(
                        "una corsa",
                        "corsa",
                        "corsa",
                        AlertLocationMainEventIntent.DELAY)).outcome();

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("ARRIVAL_DELAY", "arrivalDelay.delay", "GREATER_THAN", "600")
                .doesNotContain("vehicleJourneyName", "line.dsc", "serviceCategory.dsc", "transportOperator.dsc");
    }

    @Test
    void englishBareGenericJourneyEventAlternativesDiscardSpuriousJourneyReference() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcomeAndUnderstanding(
                "Notify me when a journey has departed or arrived",
                verifiedOutcomeJson(
                        eventsTypeAnyCondition(List.of("DEPARTED", "ARRIVED")),
                        coverage("payload.ongroundServiceEvent.eventsType")),
                bareEntityJourneyReferenceUnderstanding(
                        "a journey",
                        "journey",
                        "journey",
                        AlertLocationMainEventIntent.DEPARTURE_OR_ARRIVAL)).outcome();

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("DEPARTED", "ARRIVED")
                .doesNotContain("vehicleJourneyName", "line.dsc", "serviceCategory.dsc", "transportOperator.dsc");
    }

    @Test
    void unknownStopPointIdFromFakeLlmIsRejectedByValidator() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                verifiedOutcomeJson(
                        stopPointEqualsCondition("TNPNTS99999999999999"),
                        coverage("payload.ongroundServiceEvent.stopPoint.id")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason())
                .contains("Unsupported or unknown stopPoint id in technicalSpecification: TNPNTS99999999999999");
    }

    @Test
    void nameLongUsedDespiteResolvedLocationIsRejectedByLocationCoverage() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                verifiedOutcomeJson(
                        nameLongFallbackCondition("Rho Fieramilano"),
                        coverage("payload.ongroundServiceEvent.stopPoint.nameLong"),
                        locationResolution(RHO_ID)));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Location coverage validation failed");
    }

    @Test
    void numericDeparturePlatformPromptDoesNotInventLocationFilter() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando un treno \u00e8 in partenza da binario maggiore di 5",
                verifiedOutcomeJson(
                        numericGreaterThanDeparturePlatformCondition(),
                        coverage(
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().get("condition").toString())
                .contains("DEPARTING", "actualDeparturePlatform.platform.dsc", "PLATFORM_NUMBER_GREATER_THAN")
                .doesNotContain("stopPoint.id");
    }

    @Test
    void postPlatformLocationPromptKeepsResolvedCurrentStopAndEvenPredicate() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da un binario pari a Lunigiana",
                verifiedOutcomeJson(
                        evenDeparturePlatformAtStopCondition(LUNIGIANA_ID),
                        coverage(
                                "payload.stopPointJourney.stopPoint.id",
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().get("condition").toString())
                .contains(LUNIGIANA_ID, "DEPARTED", "PLATFORM_NUMBER_EVEN");
    }

    @Test
    void preciseEnglishDepartureSuffixPromptUsesSingleCompletedEventWithoutLocation() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Notify me when a train departs from a platform with a letter",
                verifiedOutcomeJson(
                        letterSuffixDeparturePlatformCondition(),
                        coverage(
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().get("condition").toString())
                .contains("operator=CONTAINS", "value=DEPARTED", "PLATFORM_HAS_LETTER_SUFFIX")
                .doesNotContain("CONTAINS_ANY", "stopPoint.id");
    }

    @Test
    void unqualifiedJourneyDescriptorVehicleJourneyNameLlmOutputIsNormalizedBeforePersistence() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa M2 \u00e8 in arrivo a Garibaldi FS",
                verifiedOutcomeJson(
                        m2VehicleJourneyNameAtGaribaldiCondition(),
                        coverage(
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.ongroundServiceEvent.stopPoint.id",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("line.dsc", "serviceCategory.dsc", "transportOperator.dsc")
                .doesNotContain("field=vehicleJourneyName, operator=CONTAINS_NORMALIZED, value=M2");
        assertThat(outcome.agentBlueprintPreview().toString())
                .contains("line.dsc", "serviceCategory.dsc", "transportOperator.dsc")
                .doesNotContain("field=vehicleJourneyName, operator=CONTAINS_NORMALIZED, value=M2");
    }

    @Test
    void alternativeUnqualifiedJourneyDescriptorsAreNormalizedBeforePersistence() {
        VerificationRun run = verifyWithLlmOutcomeAndUnderstanding(
                "Avvertimi quando una corsa M2 o M3 \u00e8 in arrivo a Garibaldi FS",
                verifiedOutcomeJson(
                        m2M3VehicleJourneyNameAtGaribaldiCondition(),
                        coverageRequirements(
                                requirement(
                                        "M2 or M3",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName"),
                                requirement(
                                        "arrival at Garibaldi",
                                        "payload.ongroundServiceEvent.eventsType",
                                        "payload.ongroundServiceEvent.stopPoint.id"))),
                understandingWithAlternativeJourneyReference());
        AlertVerificationOutcome outcome = run.outcome();

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("ARRIVING", GARIBALDI_FS_ID)
                .contains("value=M2", "value=M3")
                .contains("line.dsc", "serviceCategory.dsc", "transportOperator.dsc")
                .doesNotContain("vehicleJourneyName");
        assertThat(outcome.requirementCoverage().toString())
                .contains("semanticOwner=JOURNEY_REFERENCE")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].line.dsc")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].serviceCategory.dsc")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].transportOperator.dsc")
                .doesNotContain("payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName");
        assertThat(run.logs())
                .contains("[IIA][ALERT_JOURNEY_REFERENCE][SOURCE]")
                .contains("source=LLM_UNDERSTANDING")
                .contains("values=[M2, M3]")
                .contains("combination=ANY")
                .doesNotContain("source=DETERMINISTIC_FALLBACK");
    }

    @Test
    void staleMappedByIsReconciledAfterUnqualifiedDescriptorNormalization() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando un treno MXP ha un ritardo arrotondato in partenza maggiore di 5 minuti",
                verifiedOutcomeJson(
                        mxpVehicleJourneyNameWithRoundedDepartureDelayCondition(),
                        coverageRequirements(
                                requirement(
                                        "train MXP",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName"),
                                requirement(
                                        "rounded departure delay greater than 5 minutes",
                                        "payload.ongroundServiceEvent.eventsType",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.roundedDelay"))));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("DEPARTURE_DELAY", "departureDelay.roundedDelay", "GREATER_THAN", "300")
                .contains("line.dsc", "serviceCategory.dsc", "transportOperator.dsc")
                .doesNotContain("field=vehicleJourneyName, operator=CONTAINS_NORMALIZED, value=MXP");
        assertThat(outcome.requirementCoverage().toString())
                .contains("semanticOwner=JOURNEY_REFERENCE")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].line.dsc")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].serviceCategory.dsc")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].transportOperator.dsc")
                .doesNotContain("payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName");
        assertThat(outcome.agentBlueprintPreview().toString())
                .contains("line.dsc", "serviceCategory.dsc", "transportOperator.dsc")
                .doesNotContain("field=vehicleJourneyName, operator=CONTAINS_NORMALIZED, value=MXP");
    }

    @Test
    void explicitTransportOperatorDoesNotCreateUnqualifiedDescriptorConstraint() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa dell'operatore di trasporto ATM \u00e8 in arrivo a Garibaldi FS",
                verifiedOutcomeJson(
                        transportOperatorAtGaribaldiCondition(),
                        coverage(
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.ongroundServiceEvent.stopPoint.id",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].transportOperator.dsc")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("transportOperator.dsc", "EQUALS_NORMALIZED", "ATM")
                .doesNotContain("serviceCategory.dsc", "line.dsc");
    }

    @Test
    void journeyNameEqualsNormalizedLlmOutputRemainsVerified() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando la corsa 125 \u00e8 in arrivo a Bicocca o a Zara o Marche",
                verifiedOutcomeJson(
                        journeyName125AtMultipleStopsCondition("EQUALS_NORMALIZED"),
                        coverage(
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.ongroundServiceEvent.stopPoint.id",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("vehicleJourneyName", "EQUALS_NORMALIZED", "125")
                .doesNotContain("line.dsc", "serviceCategory.dsc", "transportOperator.dsc");
    }

    @Test
    void journeyNameContainsNormalizedLlmOutputRemainsVerified() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando la corsa 125 \u00e8 in arrivo a Bicocca o a Zara o Marche",
                verifiedOutcomeJson(
                        journeyName125AtMultipleStopsCondition("CONTAINS_NORMALIZED"),
                        coverage(
                                "payload.ongroundServiceEvent.eventsType",
                                "payload.ongroundServiceEvent.stopPoint.id",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("vehicleJourneyName", "CONTAINS_NORMALIZED", "125")
                .doesNotContain("line.dsc", "serviceCategory.dsc", "transportOperator.dsc");
    }

    @SuppressWarnings("unchecked")
    private AlertVerificationOutcome verifyWithLlmOutcome(String prompt, String llmJson) {
        return verifyWithLlmOutcomeAndLogs(prompt, llmJson, null).outcome();
    }

    private VerificationRun verifyWithLlmOutcomeAndUnderstanding(
            String prompt,
            String llmJson,
            AlertLocationUnderstandingResult understanding) {
        return verifyWithLlmOutcomeAndLogs(prompt, llmJson, understanding);
    }

    private VerificationRun verifyWithLlmOutcomeAndLogs(
            String prompt,
            String llmJson,
            AlertLocationUnderstandingResult understanding) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream previousOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            AlertVerificationOutcome outcome = executeVerifyWithLlmOutcome(prompt, llmJson, understanding);
            return new VerificationRun(outcome, output.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(previousOut);
        }
    }

    @SuppressWarnings("unchecked")
    private AlertVerificationOutcome executeVerifyWithLlmOutcome(
            String prompt,
            String llmJson,
            AlertLocationUnderstandingResult understanding) {
        AlertService service = new AlertService();
        service.alertRepository = mock(AlertRepository.class);
        when(service.alertRepository.getAlertVerificationPromptData("ALRT1"))
                .thenReturn(Optional.of(new AlertVerificationPromptData("ALRT1", "Alert", null, prompt)));
        when(service.alertRepository.verifyAlert(any(), any(), any())).thenReturn(Optional.empty());

        service.alertVerificationPromptBuilder = mock(AlertVerificationPromptBuilder.class);
        when(service.alertVerificationPromptBuilder.build(any())).thenReturn(
                new LlmRequest(AiUseCase.ALERT_VERIFY, "system", "user", "gpt-4.1-mini", 0.1, 5000, "ALRT1"));
        service.alertVerificationLlmResponseParser = new AlertVerificationLlmResponseParser();
        service.alertVerificationOutcomeValidator = new AlertVerificationOutcomeValidator();
        service.alertVerificationMockEngine = mock(AlertVerificationMockEngine.class);
        service.alertLocationMentionExtractor = new SimpleAlertLocationMentionExtractor();
        service.alertLocationResolverService = new AlertLocationResolverService();
        if (understanding != null) {
            service.locationUnderstandingEnabled = true;
            service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
            when(service.alertLocationUnderstandingService.understandLocations(prompt, "ALRT1"))
                    .thenReturn(understanding);
        }
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse(llmJson, "FAKE", "fake-model", null, null, null));
        service.aiConfiguration = mock(AiConfiguration.class);
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(alertVerify.simulateProviderTimeout()).thenReturn(false);
        when(service.aiConfiguration.alertVerify()).thenReturn(alertVerify);
        service.fallbackOnInvalidLlm = false;

        service.verifyAlert("ALRT1", new AlertVerificationRequest());

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(any(), any(), outcome.capture());
        return outcome.getValue();
    }

    private AlertLocationUnderstandingResult understandingWithAlternativeJourneyReference() {
        return new AlertLocationUnderstandingResult(
                true,
                "it",
                new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.ARRIVAL, 0.95),
                List.of(new AlertLocationUnderstandingLocation(
                        "Garibaldi FS",
                        "Garibaldi FS",
                        AlertLocationRole.MAIN_EVENT_LOCATION,
                        AlertLocationRelation.EVENT_LOCATION,
                        true,
                        AlertLocationPolarity.INCLUDE,
                        "G1",
                        0.96)),
                List.of(new AlertLocationUnderstandingNonLocationConstraint(
                        AlertLocationNonLocationConstraintType.JOURNEY_REFERENCE,
                        "M2 o M3",
                        AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR,
                        "M2",
                        List.of("M2", "M3"),
                        AlertJourneyReferenceValueCombination.ANY,
                        true,
                        0.94)),
                List.of());
    }

    private AlertLocationUnderstandingResult bareEntityJourneyReferenceUnderstanding(
            String rawText,
            String entityHeadText,
            String normalizedValue,
            AlertLocationMainEventIntent eventIntent) {
        return new AlertLocationUnderstandingLlmResponseParser().parse("""
                {
                  "hasLocations": false,
                  "language": "it",
                  "mainEvent": {"eventIntent": "%s", "confidence": 0.9},
                  "locations": [],
                  "nonLocationConstraints": [
                    {
                      "type": "JOURNEY_REFERENCE",
                      "kind": "UNQUALIFIED_DESCRIPTOR",
                      "rawText": "%s",
                      "entityHeadText": "%s",
                      "descriptorValueTexts": [],
                      "normalizedValue": "%s",
                      "normalizedValues": ["%s"],
                      "requiredCoverage": true,
                      "confidence": 0.92
                    }
                  ],
                  "warnings": []
                }
                """.formatted(eventIntent.name(), rawText, entityHeadText, normalizedValue, normalizedValue));
    }

    private record VerificationRun(AlertVerificationOutcome outcome, String logs) {
    }

    private String verifiedOutcomeJson(String condition, String requirementCoverage) {
        return verifiedOutcomeJson(condition, requirementCoverage, "");
    }

    private String verifiedOutcomeJson(String condition, String requirementCoverage, String extraTechnicalSpecification) {
        return """
                {
                  "decision": "VERIFIED",
                  "summary": "The alert can be evaluated on realtime ServiceData events.",
                  "confidence": 0.80,
                  "provider": "fake",
                  "model": "fake-model",
                  "promptVersion": "alert-verify-mvp-v1",
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "EVENT_INTERPRETER",
                  "inputModel": "ServiceDataV2",
                  "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                  "triggerType": "EVENT",
                  "evaluationMode": "STATELESS_EVENT_MATCH",
                  "interpretedEventNames": ["SERVICE_DATA_FIELD_MATCH"],
                  "interpretedTargetTypes": ["SERVICE_DATA_JOURNEY"],
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v2",
                    "source": "SERVICE_DATA",
                    "inputModel": "ServiceDataV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "EVENT",
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "condition": %s,
                    "deduplicationKeyTemplate": "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}"%s
                  },
                  "agentBlueprintPreview": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "ServiceDataFieldMatchAlertAgent",
                    "triggerType": "EVENT",
                    "requiredSources": ["SERVICE_DATA"],
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "targetTypes": ["SERVICE_DATA_JOURNEY"],
                    "stateRequirements": {"requiresState": false},
                    "parameters": {"conditionType": "SERVICE_DATA_FIELD_MATCH", "condition": %s},
                    "output": {"type": "CANDIDATE_SUGGESTION"}
                  },
                  "requirementCoverage": %s,
                  "warnings": [],
                  "safetyChecks": ["No executable code generated.", "No Agent Definition created.", "No Suggestion created."]
                }
                """.formatted(condition, extraTechnicalSpecification, condition, requirementCoverage);
    }

    private String stopPointEqualsCondition(String id) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "EQUALS", "value": "%s"}
                  ]
                }
                """.formatted(id);
    }

    private String stopPointInCondition(List<String> ids) {
        String values = ids.stream()
                .map(id -> "\"" + id + "\"")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "IN", "values": [%s]}
                  ]
                }
                """.formatted(values);
    }

    private String nameLongFallbackCondition(String value) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.nameLong", "operator": "CONTAINS_NORMALIZED", "value": "%s"}
                  ]
                }
                """.formatted(value);
    }

    private String nextCallNameLongFallbackCondition(String value) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "anyElement": {
                    "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                    "conditions": {"all": [
                      {"anyElement": {
                        "path": "nextCalls[]",
                        "conditions": {"all": [
                          {"field": "stopPoint.nameLong", "operator": "CONTAINS_NORMALIZED", "value": "%s"}
                        ]}
                      }}
                    ]}
                  }
                }
                """.formatted(value);
    }

    private String eventsTypeCondition(String value) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "%s"}
                  ]
                }
                """.formatted(value);
    }

    private String eventsTypeAnyCondition(List<String> values) {
        String leaves = values.stream()
                .map(value -> "{\"field\": \"payload.ongroundServiceEvent.eventsType\", \"operator\": \"CONTAINS\", \"value\": \"%s\"}"
                        .formatted(value))
                .reduce((left, right) -> left + ",\n" + right)
                .orElse("");
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "any": [
                    %s
                  ]
                }
                """.formatted(leaves);
    }

    private String genericDelayGreaterThanCondition(int seconds) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "ARRIVAL_DELAY"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {"field": "arrivalDelay.delay", "operator": "GREATER_THAN", "value": %d}
                    }}
                  ]
                }
                """.formatted(seconds);
    }

    private String numericGreaterThanDeparturePlatformCondition() {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTING"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {"field": "actualDeparturePlatform.platform.dsc", "operator": "PLATFORM_NUMBER_GREATER_THAN", "value": 5}
                    }}
                  ]
                }
                """;
    }

    private String evenDeparturePlatformAtStopCondition(String id) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.stopPointJourney.stopPoint.id", "operator": "EQUALS", "value": "%s"},
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {"field": "actualDeparturePlatform.platform.dsc", "operator": "PLATFORM_NUMBER_EVEN"}
                    }}
                  ]
                }
                """.formatted(id);
    }

    private String letterSuffixDeparturePlatformCondition() {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {"field": "actualDeparturePlatform.platform.dsc", "operator": "PLATFORM_HAS_LETTER_SUFFIX"}
                    }}
                  ]
                }
                """;
    }

    private String m2VehicleJourneyNameAtGaribaldiCondition() {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "ARRIVING"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "EQUALS", "value": "%s"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {"field": "vehicleJourneyName", "operator": "CONTAINS_NORMALIZED", "value": "M2"}
                    }}
                  ]
                }
                """.formatted(GARIBALDI_FS_ID);
    }

    private String m2M3VehicleJourneyNameAtGaribaldiCondition() {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "ARRIVING"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "EQUALS", "value": "%s"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {
                        "any": [
                          {"field": "vehicleJourneyName", "operator": "CONTAINS_NORMALIZED", "value": "M2"},
                          {"field": "vehicleJourneyName", "operator": "CONTAINS_NORMALIZED", "value": "M3"}
                        ]
                      }
                    }}
                  ]
                }
                """.formatted(GARIBALDI_FS_ID);
    }

    private String transportOperatorAtGaribaldiCondition() {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "ARRIVING"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "EQUALS", "value": "%s"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {"field": "transportOperator.dsc", "operator": "EQUALS_NORMALIZED", "value": "ATM"}
                    }}
                  ]
                }
                """.formatted(GARIBALDI_FS_ID);
    }

    private String mxpVehicleJourneyNameWithRoundedDepartureDelayCondition() {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTURE_DELAY"},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {
                        "all": [
                          {"field": "departureDelay.roundedDelay", "operator": "GREATER_THAN", "value": 300},
                          {"field": "vehicleJourneyName", "operator": "CONTAINS_NORMALIZED", "value": "MXP"}
                        ]
                      }
                    }}
                  ]
                }
                """;
    }

    private String journeyName125AtMultipleStopsCondition(String operator) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "ARRIVING"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "IN", "values": ["%s", "%s", "%s"]},
                    {"anyElement": {
                      "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                      "conditions": {"field": "vehicleJourneyName", "operator": "%s", "value": "125"}
                    }}
                  ]
                }
                """.formatted(BICOCCA_ID, ZARA_ID, MARCHE_ID, operator);
    }

    private String coverage(String... fields) {
        String mappedBy = java.util.Arrays.stream(fields)
                .map(field -> "\"" + field + "\"")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return """
                {
                  "requirements": [
                    {"text": "condition", "required": true, "mappable": true, "mappedBy": [%s], "reason": ""}
                  ],
                  "allRequiredRequirementsMapped": true
                }
                """.formatted(mappedBy);
    }

    private String coverageRequirements(String... requirements) {
        String joined = java.util.Arrays.stream(requirements)
                .reduce((left, right) -> left + ",\n        " + right)
                .orElse("");
        return """
                {
                  "requirements": [
                    %s
                  ],
                  "allRequiredRequirementsMapped": true
                }
                """.formatted(joined);
    }

    private String requirement(String text, String... fields) {
        String mappedBy = java.util.Arrays.stream(fields)
                .map(field -> "\"" + field + "\"")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return """
                {"text": "%s", "required": true, "mappable": true, "mappedBy": [%s], "reason": ""}
                """.formatted(text, mappedBy);
    }

    private String locationResolution(String id) {
        return """
                ,
                    "locationResolution": {
                      "resolutions": [
                        {"rawText": "Rho Fieramilano", "status": "RESOLVED", "selectedPointIds": ["%s"]}
                      ]
                    }
                """.formatted(id);
    }
}
