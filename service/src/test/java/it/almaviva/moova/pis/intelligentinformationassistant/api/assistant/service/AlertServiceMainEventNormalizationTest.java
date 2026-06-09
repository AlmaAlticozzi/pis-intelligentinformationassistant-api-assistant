package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertServiceMainEventNormalizationTest {

    private static final String RHO_FIERAMILANO_ID = "TNPNTS00000000005467";
    private static final String EVENT_FIELD = "payload.ongroundServiceEvent.eventsType";
    private static final String STOP_FIELD = "payload.stopPointJourney.stopPoint.id";

    private final AlertService service = new AlertService();
    private final AlertVerificationOutcomeValidator validator = new AlertVerificationOutcomeValidator();

    @Test
    void normalizesDepartedToDepartingWhenExpectedProgressiveDeparture() {
        AlertVerificationLocationContext context = locationContext("DEPARTURE", "PROGRESSIVE", "DEPARTING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEvent("DEPARTED")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTING");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("DEPARTING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void normalizesArrivedToArrivingWhenExpectedProgressiveArrival() {
        AlertVerificationLocationContext context = locationContext("ARRIVAL", "PROGRESSIVE", "ARRIVING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEvent("ARRIVED")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("ARRIVING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void doesNotNormalizeAcrossArrivalDepartureDomains() {
        AlertVerificationLocationContext context = locationContext("DEPARTURE", "PROGRESSIVE", "DEPARTING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEvent("ARRIVED")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("ARRIVED");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("DEPARTING");
    }

    @Test
    void doesNotInventMissingEventsTypeCondition() {
        AlertVerificationLocationContext context = locationContext("DEPARTURE", "PROGRESSIVE", "DEPARTING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithoutEvent()),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(hasEventCondition(normalized.technicalSpecification())).isFalse();
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("DEPARTING");
    }

    @Test
    void insertsMissingEventsTypeWhenExpectedEventHasCoherentDeparturePredicate() {
        AlertVerificationLocationContext context = locationContext("DEPARTURE", "PROGRESSIVE", "DEPARTING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithDepartureDelayWithoutEvent()),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTING");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("DEPARTING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void eventPrimaryDelayNormalizesDepartureDelayBackToDeparting() {
        AlertVerificationLocationContext context = locationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "DEPARTURE"),
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", "PROGRESSIVE"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "DEPARTING"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "ACCESSORY_DELAY_PREDICATE"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=900;unit=SECONDS"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEventAndDepartureDelay("DEPARTURE_DELAY")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTING");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("DEPARTING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void eventPrimaryDelayNormalizesArrivalDelayBackToArriving() {
        AlertVerificationLocationContext context = locationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "ARRIVAL"),
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", "PROGRESSIVE"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "ARRIVING"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "ACCESSORY_DELAY_PREDICATE"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "ARRIVAL_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=900;unit=SECONDS"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEventAndArrivalDelay("ARRIVAL_DELAY")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("ARRIVING");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("ARRIVING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void delayEventTypeWinsOverExpectedMainEventType() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "DEPARTURE"),
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", "PROGRESSIVE"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "DEPARTING"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "DEPARTURE_DELAY"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEventAndDepartureDelay("DEPARTING")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTURE_DELAY");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("DEPARTURE_DELAY");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void normalizesArrivedToArrivalDelayWhenDelayEventTypeIsAuthoritative() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "ARRIVING"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "ARRIVAL_DELAY"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEventAndArrivalDelay("ARRIVED")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("ARRIVAL_DELAY");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("ARRIVAL_DELAY");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void insertsDelayEventTypeWhenDelayPredicateHasNoEventsType() {
        AlertVerificationLocationContext context = locationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "DEPARTURE_DELAY"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithDepartureDelayWithoutEvent()),
                promptData(context));

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTURE_DELAY");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("DEPARTURE_DELAY");
    }

    @Test
    void insertsMissingDepartureDelayThresholdPredicateWhenThresholdIsStructured() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=900;unit=SECONDS"));
        AlertVerificationOutcome outcome = outcomeWithConditionAndCoverage(
                conditionWithOnlyEvent("DEPARTURE_DELAY"),
                coverageFor(EVENT_FIELD));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(outcome, promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);
        Map<String, Object> leaf = findLeaf(normalized.technicalSpecification(), "departureDelay.delay");
        Map<String, Object> blueprintLeaf = findLeaf(normalized.agentBlueprintPreview(), "departureDelay.delay");

        assertThat(leaf).containsEntry("operator", "GREATER_THAN").containsEntry("value", 900);
        assertThat(blueprintLeaf).containsEntry("operator", "GREATER_THAN").containsEntry("value", 900);
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void insertsMissingGenericDelayThresholdPredicatesWhenThresholdIsStructured() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "BOTH"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=900;unit=SECONDS"));
        AlertVerificationOutcome outcome = outcomeWithConditionAndCoverage(
                conditionWithGenericDelayEventOnly(),
                coverageFor(EVENT_FIELD));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(outcome, promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(findLeaf(normalized.technicalSpecification(), "arrivalDelay.delay"))
                .containsEntry("operator", "GREATER_THAN")
                .containsEntry("value", 900);
        assertThat(findLeaf(normalized.technicalSpecification(), "departureDelay.delay"))
                .containsEntry("operator", "GREATER_THAN")
                .containsEntry("value", 900);
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void insertsGenericDelayEventTypesWhenDelayOnlyPredicateHasNoEventsType() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "BOTH"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_DIRECTION", "GENERIC"));
        AlertVerificationOutcome outcome = outcomeWithConditionAndCoverage(
                conditionWithGenericDelayWithoutEvent(),
                Map.of(
                        "requirements", List.of(
                                coverageRequirement("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay"),
                                coverageRequirement("payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay")),
                        "allRequiredRequirementsMapped", true));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(outcome, promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);
        Map<String, Object> eventLeaf = findLeaf(normalized.technicalSpecification(), "eventsType");
        Map<String, Object> blueprintEventLeaf = findLeaf(normalized.agentBlueprintPreview(), "eventsType");

        assertThat(eventLeaf).containsEntry("operator", "CONTAINS_ANY");
        assertThat(eventLeaf.get("values")).asList().containsExactly("ARRIVAL_DELAY", "DEPARTURE_DELAY");
        assertThat(blueprintEventLeaf).containsEntry("operator", "CONTAINS_ANY");
        assertThat(blueprintEventLeaf.get("values")).asList().containsExactly("ARRIVAL_DELAY", "DEPARTURE_DELAY");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void primaryRoundedDepartureDelayUsesDepartureDelayEventAndRoundedField() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "PRIMARY_DELAY_EVENT"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_MEASURE", "ROUNDED_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=300;unit=SECONDS"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithConditionAndCoverage(
                        conditionWithOnlyEvent("DEPARTING"),
                        coverageFor(EVENT_FIELD, "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.roundedDelay")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized,
                "Avvertimi quando una corsa ha un ritardo arrotondato in partenza maggiore di 5 minuti",
                context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTURE_DELAY");
        assertThat(findLeaf(normalized.technicalSpecification(), "departureDelay.roundedDelay"))
                .containsEntry("operator", "GREATER_THAN")
                .containsEntry("value", 300);
        assertThat(findLeaf(normalized.technicalSpecification(), "departureDelay.delay")).isNull();
        assertThat(findLeaf(normalized.technicalSpecification(), "arrivalDelay.delay")).isNull();
        assertThat(findLeaf(normalized.technicalSpecification(), "arrivalDelay.roundedDelay")).isNull();
        assertThat(findLeaf(normalized.technicalSpecification(), "eventsType")).doesNotContainEntry("value", "DEPARTING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void primaryRoundedArrivalDelayUsesArrivalDelayEventAndRoundedField() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "PRIMARY_DELAY_EVENT"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "ARRIVAL_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "ARRIVAL_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_MEASURE", "ROUNDED_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=300;unit=SECONDS"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithConditionAndCoverage(
                        conditionWithOnlyEvent("ARRIVING"),
                        coverageFor(EVENT_FIELD, "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.roundedDelay")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized,
                "Avvertimi quando una corsa ha un ritardo arrotondato in arrivo maggiore di 5 minuti",
                context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("ARRIVAL_DELAY");
        assertThat(findLeaf(normalized.technicalSpecification(), "arrivalDelay.roundedDelay"))
                .containsEntry("operator", "GREATER_THAN")
                .containsEntry("value", 300);
        assertThat(findLeaf(normalized.technicalSpecification(), "arrivalDelay.delay")).isNull();
        assertThat(findLeaf(normalized.technicalSpecification(), "departureDelay.delay")).isNull();
        assertThat(findLeaf(normalized.technicalSpecification(), "departureDelay.roundedDelay")).isNull();
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void primaryNormalDirectedDelaysUseNormalDelayField() {
        AlertVerificationLocationContext departure = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "PRIMARY_DELAY_EVENT"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=300;unit=SECONDS"));
        AlertVerificationLocationContext arrival = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "PRIMARY_DELAY_EVENT"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "ARRIVAL_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "ARRIVAL_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=300;unit=SECONDS"));

        AlertVerificationOutcome normalizedDeparture = service.normalizeExpectedMainEventType(
                outcomeWithConditionAndCoverage(
                        conditionWithOnlyEvent("DEPARTURE_DELAY"),
                        coverageFor(EVENT_FIELD, "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay")),
                promptData(departure));
        AlertVerificationOutcome normalizedArrival = service.normalizeExpectedMainEventType(
                outcomeWithConditionAndCoverage(
                        conditionWithOnlyEvent("ARRIVAL_DELAY"),
                        coverageFor(EVENT_FIELD, "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay")),
                promptData(arrival));

        assertThat(findLeaf(normalizedDeparture.technicalSpecification(), "departureDelay.delay"))
                .containsEntry("value", 300);
        assertThat(findLeaf(normalizedArrival.technicalSpecification(), "arrivalDelay.delay"))
                .containsEntry("value", 300);
        assertThat(validator.validate(normalizedDeparture, "Prompt", departure).decision())
                .isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(validator.validate(normalizedArrival, "Prompt", arrival).decision())
                .isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void primaryGenericRoundedDelayUsesBothRoundedDelayFields() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "PRIMARY_DELAY_EVENT"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "BOTH"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "BOTH"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_MEASURE", "ROUNDED_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=300;unit=SECONDS"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithConditionAndCoverage(
                        conditionWithGenericDelayEventOnly(),
                        coverageFor(EVENT_FIELD,
                                "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.roundedDelay",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.roundedDelay")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized,
                "Avvertimi quando una corsa ha un ritardo arrotondato maggiore di 5 minuti",
                context);

        Map<String, Object> eventLeaf = findLeaf(normalized.technicalSpecification(), "eventsType");
        assertThat(eventLeaf).containsEntry("operator", "CONTAINS_ANY");
        assertThat(eventLeaf.get("values")).asList().contains("ARRIVAL_DELAY", "DEPARTURE_DELAY");
        assertThat(findLeaf(normalized.technicalSpecification(), "arrivalDelay.roundedDelay"))
                .containsEntry("value", 300);
        assertThat(findLeaf(normalized.technicalSpecification(), "departureDelay.roundedDelay"))
                .containsEntry("value", 300);
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void movementDepartureWithAccessoryDelayKeepsMovementEventAndNormalDelayField() {
        AlertVerificationLocationContext context = locationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "ACCESSORY_DELAY_PREDICATE"),
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "DEPARTURE"),
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", "PROGRESSIVE"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "DEPARTING"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=300;unit=SECONDS"));

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithConditionAndCoverage(
                        conditionWithEvent("DEPARTING"),
                        coverageFor(EVENT_FIELD, STOP_FIELD,
                                "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay")),
                promptData(context));

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTING");
        assertThat(findLeaf(normalized.technicalSpecification(), "departureDelay.delay"))
                .containsEntry("value", 300);
        assertThat(findLeaf(normalized.technicalSpecification(), "DEPARTURE_DELAY")).isNull();
        assertThat(validator.validate(normalized,
                "Avvertimi quando una corsa e in partenza con un ritardo maggiore di 5 minuti",
                context).decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void validatorRejectsDirectedDelayContamination() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_ROLE", "PRIMARY_DELAY_EVENT"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "DEPARTURE_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_MEASURE", "ROUNDED_DELAY"),
                new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        "operator=GREATER_THAN;value=300;unit=SECONDS"));
        AlertVerificationOutcome contaminated = outcomeWithConditionAndCoverage(
                conditionWithEventAndDelayFields("DEPARTURE_DELAY", "departureDelay.roundedDelay", "arrivalDelay.roundedDelay"),
                coverageFor(EVENT_FIELD,
                        "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.roundedDelay",
                        "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.roundedDelay"));

        AlertVerificationOutcome validated = validator.validate(contaminated, "Prompt", context);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("arrivalDelay.roundedDelay");
    }

    @Test
    void validatorRejectsGenericDelayWithoutEventsTypeWhenNotNormalized() {
        AlertVerificationLocationContext context = noLocationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "BOTH"));
        AlertVerificationOutcome outcome = outcomeWithConditionAndCoverage(
                conditionWithGenericDelayWithoutEvent(),
                Map.of(
                        "requirements", List.of(
                                coverageRequirement("payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay"),
                                coverageRequirement("payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay")),
                        "allRequiredRequirementsMapped", true));

        AlertVerificationOutcome validated = validator.validate(outcome, "Prompt", context);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("generic delay");
    }

    @Test
    void normalizesSingleValueInToEqualsRecursively() {
        AlertVerificationOutcome normalized = service.normalizeSingleValueInOperators(
                outcomeWithCondition(conditionWithNestedSingleValueIn()),
                "ALRT1");

        Map<String, Object> leaf = findLeaf(normalized.technicalSpecification(), "stopPoint.id");
        Map<String, Object> blueprintLeaf = findLeaf(normalized.agentBlueprintPreview(), "stopPoint.id");

        assertThat(leaf).containsEntry("operator", "EQUALS").containsEntry("value", RHO_FIERAMILANO_ID);
        assertThat(leaf).doesNotContainKey("values");
        assertThat(blueprintLeaf).containsEntry("operator", "EQUALS").containsEntry("value", RHO_FIERAMILANO_ID);
        assertThat(blueprintLeaf).doesNotContainKey("values");
    }

    @Test
    void removesRedundantPassingTypeInsideNextTransitCallsAndNestedConditionType() {
        AlertVerificationOutcome normalized = service.normalizeSingleValueInOperators(
                outcomeWithCondition(conditionWithNestedTransitPassingTypeAndNestedType()),
                "ALRT1");

        assertThat(findLeaf(normalized.technicalSpecification(), "passingType")).isNull();
        assertThat(hasNestedConditionType(conditionFromTechnicalSpecification(normalized.technicalSpecification()))).isFalse();
        assertThat(findLeaf(normalized.agentBlueprintPreview(), "passingType")).isNull();
        assertThat(hasNestedConditionType(conditionFromBlueprint(normalized.agentBlueprintPreview()))).isFalse();
    }

    @Test
    void normalizesFalseRequirementCoverageWhenNoRequiredRequirementIsUnmappable() {
        AlertVerificationOutcome normalized = service.normalizeRequirementCoverage(
                outcomeWithConditionAndCoverage(
                        conditionWithEvent("DEPARTING"),
                        Map.of(
                                "requirements", List.of(coverageRequirement(STOP_FIELD)),
                                "allRequiredRequirementsMapped", false)),
                "ALRT1");

        assertThat(normalized.requirementCoverage()).containsEntry("allRequiredRequirementsMapped", true);
    }

    @Test
    void keepsFalseRequirementCoverageWhenRequiredRequirementIsUnmappable() {
        AlertVerificationOutcome normalized = service.normalizeRequirementCoverage(
                outcomeWithConditionAndCoverage(
                        conditionWithEvent("DEPARTING"),
                        Map.of(
                                "requirements", List.of(Map.of(
                                        "text", "passenger count",
                                        "required", true,
                                        "mappable", false,
                                        "mappedBy", List.of(),
                                        "reason", "Unsupported capability")),
                                "allRequiredRequirementsMapped", false)),
                "ALRT1");

        assertThat(normalized.requirementCoverage()).containsEntry("allRequiredRequirementsMapped", false);
    }

    @Test
    void normalizesSpuriousRouteTransitRequirementWhenRouteConditionIsMapped() {
        AlertVerificationOutcome normalized = service.normalizeRequirementCoverage(
                outcomeWithConditionAndCoverage(
                        conditionWithNestedSingleValueIn(),
                        Map.of(
                                "requirements", List.of(Map.of(
                                        "text", "MAIN_EVENT_INTENT ROUTE_TRANSIT",
                                        "required", true,
                                        "mappable", false,
                                        "mappedBy", List.of(),
                                        "reason", "ROUTE_TRANSIT is not a ServiceData event type")),
                                "allRequiredRequirementsMapped", false)),
                "ALRT1");

        assertThat(normalized.requirementCoverage()).containsEntry("allRequiredRequirementsMapped", true);
    }

    private AlertVerificationPromptData promptData(AlertVerificationLocationContext context) {
        return new AlertVerificationPromptData("ALRT1", "Alert", null, "Prompt", context);
    }

    private AlertVerificationLocationContext locationContext(String intent, String phase, String expectedEventType) {
        return locationContextWithConstraints(
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", intent),
                new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", phase),
                new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", expectedEventType));
    }

    private AlertVerificationLocationContext locationContextWithConstraints(
            AlertVerificationLocationContext.NonLocationConstraint... constraints) {
        return new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "X",
                        "X",
                        "MAIN_EVENT_LOCATION",
                        "EVENT_LOCATION",
                        true,
                        "INCLUDE",
                        "G1",
                        0.90,
                        "RESOLVED",
                        List.of(new AlertVerificationLocationContext.LocationCandidate(
                                RHO_FIERAMILANO_ID,
                                "RHO FIERAMILANO",
                                "RHO FIERAMILANO",
                                "RAIL",
                                1.0,
                                "EXACT_NORMALIZED",
                                true)),
                        List.of(RHO_FIERAMILANO_ID),
                        false,
                        false,
                        0.0,
                        "",
                        List.of(STOP_FIELD))),
                List.of(constraints),
                List.of());
    }

    private AlertVerificationLocationContext noLocationContextWithConstraints(
            AlertVerificationLocationContext.NonLocationConstraint... constraints) {
        return new AlertVerificationLocationContext(
                false,
                List.of(),
                List.of(constraints),
                List.of());
    }

    private Map<String, Object> conditionWithEvent(String eventType) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", EVENT_FIELD, "operator", "CONTAINS", "value", eventType),
                        Map.of("field", STOP_FIELD, "operator", "EQUALS", "value", RHO_FIERAMILANO_ID)));
    }

    private Map<String, Object> conditionWithOnlyEvent(String eventType) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "field", EVENT_FIELD,
                "operator", "CONTAINS",
                "value", eventType);
    }

    private Map<String, Object> conditionWithGenericDelayEventOnly() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "field", EVENT_FIELD,
                "operator", "CONTAINS_ANY",
                "values", List.of("ARRIVAL_DELAY", "DEPARTURE_DELAY"));
    }

    private Map<String, Object> conditionWithoutEvent() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of("field", STOP_FIELD, "operator", "EQUALS", "value", RHO_FIERAMILANO_ID)));
    }

    private Map<String, Object> conditionWithDepartureDelayWithoutEvent() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", STOP_FIELD, "operator", "EQUALS", "value", RHO_FIERAMILANO_ID),
                        Map.of("field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay",
                                "operator", "GREATER_THAN",
                                "value", 15)));
    }

    private Map<String, Object> conditionWithEventAndDepartureDelay(String eventType) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", EVENT_FIELD, "operator", "CONTAINS", "value", eventType),
                        Map.of("field", STOP_FIELD, "operator", "EQUALS", "value", RHO_FIERAMILANO_ID),
                        Map.of("field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay",
                                "operator", "GREATER_THAN",
                                "value", 15)));
    }

    private Map<String, Object> conditionWithEventAndArrivalDelay(String eventType) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", EVENT_FIELD, "operator", "CONTAINS", "value", eventType),
                        Map.of("field", STOP_FIELD, "operator", "EQUALS", "value", RHO_FIERAMILANO_ID),
                        Map.of("field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay",
                                "operator", "GREATER_OR_EQUAL",
                                "value", 12)));
    }

    private Map<String, Object> conditionWithEventAndDelayFields(
            String eventType,
            String firstField,
            String secondField) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", EVENT_FIELD, "operator", "CONTAINS", "value", eventType),
                        Map.of("anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of(
                                        "field", firstField,
                                        "operator", "GREATER_THAN",
                                        "value", 300))),
                        Map.of("anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of(
                                        "field", secondField,
                                        "operator", "GREATER_THAN",
                                        "value", 300)))));
    }

    private Map<String, Object> conditionWithGenericDelayWithoutEvent() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "any", List.of(
                        Map.of("anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of(
                                        "field", "arrivalDelay.delay",
                                        "operator", "GREATER_THAN",
                                        "value", 900))),
                        Map.of("anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of(
                                        "field", "departureDelay.delay",
                                        "operator", "GREATER_THAN",
                                        "value", 900)))));
    }

    private Map<String, Object> conditionWithNestedSingleValueIn() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of("anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("anyElement", Map.of(
                                "path", "nextCalls[]",
                                "conditions", Map.of(
                                        "field", "stopPoint.id",
                                        "operator", "IN",
                                        "values", List.of(RHO_FIERAMILANO_ID))))))));
    }

    private Map<String, Object> conditionWithNestedTransitPassingTypeAndNestedType() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "type", "SERVICE_DATA_FIELD_MATCH",
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of("anyElement", Map.of(
                                        "path", "nextTransitCalls[]",
                                        "conditions", Map.of("all", List.of(
                                                Map.of(
                                                        "field", "stopPoint.id",
                                                        "operator", "EQUALS",
                                                        "value", RHO_FIERAMILANO_ID),
                                                Map.of(
                                                        "field", "passingType",
                                                        "operator", "EQUALS",
                                                        "value", "TRANSIT")))))))));
    }

    private AlertVerificationOutcome outcomeWithCondition(Map<String, Object> condition) {
        return outcomeWithConditionAndCoverage(condition, coverageFor(condition));
    }

    private AlertVerificationOutcome outcomeWithConditionAndCoverage(
            Map<String, Object> condition,
            Map<String, Object> requirementCoverage) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "The alert can be evaluated on realtime ServiceData events.",
                null,
                0.80,
                "test",
                "test-model",
                "alert-verify-mvp-v1",
                List.of("SERVICE_DATA"),
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "EVENT",
                "STATELESS_EVENT_MATCH",
                List.of("SERVICE_DATA_FIELD_MATCH"),
                List.of("SERVICE_DATA_JOURNEY"),
                Map.of(
                        "schemaVersion", "iia.alert.technical-specification/v2",
                        "source", "SERVICE_DATA",
                        "inputModel", "ServiceDataV2",
                        "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                        "triggerType", "EVENT",
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "condition", condition,
                        "deduplicationKeyTemplate", "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}"),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "agentName", "ServiceDataFieldMatchAlertAgent",
                        "triggerType", "EVENT",
                        "requiredSources", List.of("SERVICE_DATA"),
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "targetTypes", List.of("SERVICE_DATA_JOURNEY"),
                        "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                        "stateRequirements", Map.of("requiresState", false),
                        "output", Map.of("type", "CANDIDATE_SUGGESTION")),
                requirementCoverage,
                List.of(),
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }

    private Map<String, Object> coverageFor(Map<String, Object> condition) {
        List<Map<String, Object>> requirements = hasEventLeaf(condition)
                ? List.of(coverageRequirement(EVENT_FIELD), coverageRequirement(STOP_FIELD))
                : List.of(coverageRequirement(STOP_FIELD));
        return Map.of("requirements", requirements, "allRequiredRequirementsMapped", true);
    }

    private Map<String, Object> coverageRequirement(String field) {
        return Map.of(
                "text", field,
                "required", true,
                "mappable", true,
                "mappedBy", List.of(field),
                "reason", "");
    }

    private Map<String, Object> coverageFor(String... fields) {
        return Map.of(
                "requirements",
                java.util.Arrays.stream(fields).map(this::coverageRequirement).toList(),
                "allRequiredRequirementsMapped",
                true);
    }

    @SuppressWarnings("unchecked")
    private String eventValue(Map<String, Object> technicalSpecification) {
        Map<String, Object> condition = (Map<String, Object>) technicalSpecification.get("condition");
        List<Map<String, Object>> all = (List<Map<String, Object>>) condition.get("all");
        return all.stream()
                .filter(leaf -> EVENT_FIELD.equals(leaf.get("field")))
                .map(leaf -> String.valueOf(leaf.get("value")))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private String blueprintEventValue(Map<String, Object> agentBlueprintPreview) {
        Map<String, Object> parameters = (Map<String, Object>) agentBlueprintPreview.get("parameters");
        return eventValue(Map.of("condition", parameters.get("condition")));
    }

    private boolean hasEventCondition(Map<String, Object> technicalSpecification) {
        return eventValue(technicalSpecification) != null;
    }

    private Object conditionFromTechnicalSpecification(Map<String, Object> technicalSpecification) {
        return technicalSpecification.get("condition");
    }

    @SuppressWarnings("unchecked")
    private Object conditionFromBlueprint(Map<String, Object> blueprint) {
        Map<String, Object> parameters = (Map<String, Object>) blueprint.get("parameters");
        return parameters.get("condition");
    }

    private boolean hasNestedConditionType(Object node) {
        return hasNestedConditionType(node, true);
    }

    private boolean hasNestedConditionType(Object node, boolean root) {
        if (node instanceof Map<?, ?> map) {
            if (!root && map.containsKey("type")) {
                return true;
            }
            for (Object value : map.values()) {
                if (hasNestedConditionType(value, false)) {
                    return true;
                }
            }
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (hasNestedConditionType(item, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findLeaf(Object node, String fieldSuffix) {
        if (node instanceof Map<?, ?> map) {
            Object field = map.get("field");
            if (field instanceof String fieldText && fieldText.endsWith(fieldSuffix)) {
                return (Map<String, Object>) map;
            }
            for (Object value : map.values()) {
                Map<String, Object> leaf = findLeaf(value, fieldSuffix);
                if (leaf != null) {
                    return leaf;
                }
            }
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                Map<String, Object> leaf = findLeaf(item, fieldSuffix);
                if (leaf != null) {
                    return leaf;
                }
            }
        }
        return null;
    }

    private boolean hasEventLeaf(Map<String, Object> condition) {
        Object all = condition.get("all");
        if (all instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (item instanceof Map<?, ?> leaf && EVENT_FIELD.equals(leaf.get("field"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
