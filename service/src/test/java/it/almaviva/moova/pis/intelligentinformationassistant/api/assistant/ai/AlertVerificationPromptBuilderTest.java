package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateLoader;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertVerificationPromptBuilderTest {

    private static final String SYSTEM_TEMPLATE_PATH = "iia/prompts/alert/event/system.md";
    private static final String USER_TEMPLATE_PATH = "iia/prompts/alert/event/user-template.md";

    @Test
    void eventSystemTemplateContainsStaticPromptTextDirectly() {
        String template = new PromptTemplateLoader().load(SYSTEM_TEMPLATE_PATH);

        assertThat(template)
                .contains("## Mission and fixed contract")
                .contains("## EVENT_INTERPRETER evaluation phases")
                .contains("Phase 1 - Current event scope")
                .contains("Phase 2 - Current event binding")
                .contains("Phase 3 - Journey state predicates")
                .contains("Phase 4 - Correlation")
                .contains("Phase 5 - Minimal verifiability")
                .contains("## ServiceDataV2 model")
                .contains("## Location rules compact")
                .contains("## Platform rules compact")
                .contains("## Temporal compact")
                .contains("## DSL construction rules")
                .contains("## Unsupported compact")
                .contains("## JSON output contract")
                .contains("EXPECTED_MAIN_EVENT_TYPE is authoritative")
                .contains("platform is not a location")
                .contains("Never invent stop point ids")
                .contains("anyElement.conditions MUST be a JSON object")
                .contains("never an array")
                .contains("Do not generate \"conditions\": [")
                .contains("\"conditions\":{\"field\":\"timetabledDeparturePlatform.dsc\"")
                .contains("\"conditions\":{\"all\":[")
                .contains("EVENT_INTERPRETER")
                .contains("ServiceDataV2")
                .contains("STATELESS_EVENT_MATCH")
                .contains("payload.ongroundServiceEvent")
                .contains("payload.stopPointJourney")
                .contains("{{SERVICE_DATA_CAPABILITY_CATALOG}}")
                .doesNotContain("## Generic examples")
                .doesNotContain("Expected condition:")
                .doesNotContain("{{MISSION_AND_SAFETY}}")
                .doesNotContain("{{EVENT_MVP_CONTRACT}}")
                .doesNotContain("{{OUTPUT_JSON_CONTRACT}}");
    }

    @Test
    void eventUserTemplateIsRuntimePayloadOnly() {
        String template = new PromptTemplateLoader().load(USER_TEMPLATE_PATH);

        assertThat(template)
                .contains("{{ALERT_INPUT}}")
                .contains("{{ROUTE_CONTEXT_JSON}}")
                .contains("{{LOCATION_CONTEXT_JSON}}")
                .contains("{{TEMPORAL_CONTEXT_JSON}}")
                .contains("{{BACKEND_DERIVED_NON_LOCATION_CONSTRAINTS_JSON}}")
                .contains("{{OUTPUT_INSTRUCTIONS}}")
                .doesNotContain("## Mission")
                .doesNotContain("EVENT_INTERPRETER");
    }

    @Test
    void eventSystemCompactV2PreservesManualRegressionRules() {
        String template = new PromptTemplateLoader().load(SYSTEM_TEMPLATE_PATH);

        assertThat(template)
                .contains("Use root all, not root any")
                .contains("event evidence and numeric delay predicate are both mandatory")
                .contains("eventsType alone is not sufficient")
                .contains("numeric delay predicate alone is not sufficient")
                .contains("use callStart.stopPoint.* and callEnd.stopPoint.* by default")
                .contains("Use timetabledCallStart/timetabledCallEnd only when the user explicitly says scheduled")
                .contains("Never use full child-array paths like payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[] as the anyElement path")
                .contains("outer path payload.stopPointJourney.stopPointsJourneyDetails[] and inner relative path nextCalls[]")
                .contains("For generic platform change, event evidence and structural evidence are both required")
                .contains("Platform change structural evidence means timetabled platform differs from actual platform")
                .contains("do not use changes CONTAINS PLATFORM_CHANGED as the only structural evidence")
                .contains("timetabledDeparturePlatform.id PLATFORM_NOT_EQUALS_FIELD actualDeparturePlatform.platform.id")
                .contains("timetabledArrivalPlatform.id PLATFORM_NOT_EQUALS_FIELD actualArrivalPlatform.platform.id")
                .contains("For \"cambio binario in partenza\", use root all [eventsType CONTAINS DEPARTURE_PLATFORM_CHANGED")
                .contains("Platform change has priority over movement event wording")
                .contains("the main event is a platform change event, not DEPARTING, DEPARTED, ARRIVING or ARRIVED")
                .contains("Do not use DEPARTING or DEPARTED as the main event")
                .contains("Do not use ARRIVING or ARRIVED as the main event")
                .contains("Event type alone is not sufficient for platform change verification")
                .contains("Compact platform-change shapes")
                .contains("PLATFORM_NOT_EQUALS_FIELD actualDeparturePlatform.platform.dsc")
                .contains("PLATFORM_NOT_EQUALS_FIELD actualArrivalPlatform.platform.dsc")
                .contains("Numeric/property platform predicates use actualDeparturePlatform.platform.dsc for departure")
                .contains("actualArrivalPlatform.platform.dsc for arrival by default")
                .contains("For generic cancellation/suppression at a monitored stop")
                .contains("arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION, departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION")
                .contains("arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION, passingType EQUALS DESTINATION")
                .contains("departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION, passingType EQUALS ORIGIN");
    }

    @Test
    void eventPromptFinalPromptContainsCoreContractRuntimeContextAndNoUnresolvedPlaceholders() {
        LlmRequest request = builder().build(promptDataWithLocation(rhoContext()));

        assertThat(fullPrompt(request))
                .isNotBlank()
                .contains("EVENT_INTERPRETER")
                .contains("ServiceDataV2")
                .contains("STATELESS_EVENT_MATCH")
                .contains("payload.ongroundServiceEvent")
                .contains("payload.stopPointJourney")
                .contains("Avvertimi quando una corsa parte da Rho Fieramilano")
                .contains("PIS location runtime context v2")
                .contains("Rho Fieramilano")
                .contains("ServiceData Capability Catalog")
                .doesNotContainPattern("\\{\\{[A-Z0-9_]+}}");
    }

    @Test
    void eventPromptContainsPlatformAndLocationRulesForRhoBinarioOne() {
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                true,
                List.of(richResolution(
                        "Rho Fieramilano",
                        "MAIN_EVENT_LOCATION",
                        "INCLUDE",
                        List.of("payload.stopPointJourney.stopPoint.id",
                                "payload.ongroundServiceEvent.stopPoint.id"))),
                List.of(
                        new AlertVerificationLocationContext.NonLocationConstraint("PLATFORM", "binario 1"),
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "DEPARTURE"),
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", "COMPLETED"),
                        new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", "DEPARTED")),
                List.of());
        LlmRequest request = builder().build(new AlertVerificationPromptData(
                "ALRT_RHO_PLATFORM",
                "Rho platform",
                "Runtime payload for platform/location prompt",
                "Avvertimi quando una corsa parte da Rho Fieramilano sul binario 1",
                context));

        assertThat(fullPrompt(request))
                .contains("Avvertimi quando una corsa parte da Rho Fieramilano sul binario 1")
                .contains("Rho Fieramilano")
                .contains("Platform/binario/track/quay/banchina/marciapiede are not locations")
                .contains("stopPoint.id")
                .contains("EVENT_INTERPRETER")
                .contains("ServiceDataV2");
        assertThat(request.userPrompt())
                .contains("selectedPointIds: [TNPNTS00000000005467]")
                .contains("targetFieldHints: [payload.stopPointJourney.stopPoint.id, payload.ongroundServiceEvent.stopPoint.id]")
                .contains("type: PLATFORM")
                .contains("rawText: \"binario 1\"")
                .contains("type: MAIN_EVENT_INTENT")
                .contains("rawText: \"DEPARTURE\"")
                .contains("type: MAIN_EVENT_PHASE")
                .contains("rawText: \"COMPLETED\"")
                .contains("type: EXPECTED_MAIN_EVENT_TYPE")
                .contains("rawText: \"DEPARTED\"")
                .doesNotContain("Minimal examples")
                .doesNotContain("Positive resolved single candidate")
                .doesNotContain("Rules:");
    }

    @Test
    void promptContainsSemanticMappingForWeekdaysAndFeriali() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("\"feriali\", \"giorni feriali\", \"nei feriali\", \"durante i feriali\"")
                .contains("\"dal lunedi al venerdi\"")
                .contains("\"dal lunedì al venerdì\"")
                .contains("LOCAL_DAY_OF_WEEK_NOT_IN with days [\"SATURDAY\",\"SUNDAY\"]")
                .contains("lunedi/lunedì -> MONDAY")
                .contains("martedi/martedì -> TUESDAY");
    }

    @Test
    void promptStatesAnyElementRepresentsArrayItemExistence() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("The existence of an array element is represented by anyElement")
                .contains("anyElement on nextTransitCalls[] with stopPoint.nameLong already represents existence of a matching transit call")
                .contains("transitera a <stop>\" without a time/day predicate means use anyElement on nextTransitCalls[] with stopPoint.nameLong only. Do not add passingTime");
    }

    @Test
    void promptForbidsExistsOnTimestampFields() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("Do not use EXISTS on timestamp fields such as passingTime, departureTime, arrivalTime, eventGenerationTime or timetabledCallStart.departureTime")
                .contains("{\"field\":\"passingTime\",\"operator\":\"EXISTS\"}")
                .contains("EXISTS on passingTime is not needed and not allowed");
    }

    @Test
    void promptContainsChangeEnumMappings() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("\"cambia origine\", \"cambio origine\" and \"origine cambiata\" mean changes CONTAINS CHANGED_ORIGIN")
                .contains("\"cambia destinazione\", \"cambio destinazione\" and \"destinazione cambiata\" mean changes CONTAINS CHANGED_DESTINATION")
                .contains("\"cambia binario\", \"cambio binario\" and \"platform changed\" are represented by current payload.ongroundServiceEvent.eventsType evidence")
                .contains("\"field\":\"changes\",\"operator\":\"CONTAINS\",\"value\":\"CHANGED_ORIGIN\"")
                .contains("Do not reject change prompts as stateful comparison when the requested change is directly represented by the ServiceData changes enum");
    }

    @Test
    void promptContainsPlatformSemanticsAndCorrelatedExamples() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("platform is not a location")
                .contains("EQUAL_PLATFORM")
                .contains("IN_PLATFORMS")
                .contains("NOT_IN_PLATFORMS")
                .contains("Do not use CONTAINS for platform")
                .contains("Use stopPoint.id for resolved locations")
                .contains("\"field\":\"timetabledArrivalPlatform.dsc\",\"operator\":\"EQUAL_PLATFORM\",\"value\":\"1\"")
                .contains("\"field\":\"timetabledDeparturePlatform.dsc\",\"operator\":\"IN_PLATFORMS\",\"values\":[\"1\",\"4\"]")
                .contains("\"field\":\"timetabledDeparturePlatform.dsc\",\"operator\":\"NOT_IN_PLATFORMS\",\"values\":[\"1\",\"12\"]");
    }

    @Test
    void promptDefaultsHumanPlatformsToTimetabledFieldsAndLimitsActualFields() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("use timetabledArrivalPlatform.dsc for arrival and timetabledDeparturePlatform.dsc for departure")
                .contains("Use actualArrivalPlatform.platform.dsc, actualArrivalPlatform.displayPlatform.dsc, actualDeparturePlatform.platform.dsc or actualDeparturePlatform.displayPlatform.dsc only when the user explicitly asks")
                .contains("Do not compare human platform values with platform technical id fields")
                .contains("prefer payload.stopPointJourney.stopPoint.id or payload.ongroundServiceEvent.stopPoint.id")
                .contains("Do not use timetabledCallStart.stopPoint.id for a current departure stop")
                .contains("Do not use timetabledCallEnd.stopPoint.id for a current arrival stop")
                .contains("Use timetabledCallStart.stopPoint.id and timetabledCallEnd.stopPoint.id only for explicit journey origin or destination constraints")
                .contains("A top-level current stop location and a platform constraint inside stopPointsJourneyDetails[] do not need to be inside the same anyElement");
    }

    @Test
    void promptAllowsCatalogSupportedNegativeFallbackForUnresolvedExcludedLocation() {
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "Bologna",
                        "Bologna",
                        "DESTINATION_LOCATION",
                        "DESTINATION_CONSTRAINT",
                        true,
                        "EXCLUDE",
                        "G1",
                        0.86,
                        "UNRESOLVED",
                        List.of(),
                        List.of(),
                        true,
                        true,
                        0.0,
                        "Location unresolved; textual fallback is allowed with lower confidence.",
                        List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
                                "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id"))),
                List.of(),
                List.of());

        LlmRequest request = builder().build(promptDataWithLocation(context));

        assertThat(fullPrompt(request))
                .contains("polarity: EXCLUDE")
                .contains("status: UNRESOLVED")
                .contains("fallbackToNameLong: true")
                .contains("fallbackAllowed: true")
                .contains("warningReason: \"Location unresolved; textual fallback is allowed with lower confidence.\"")
                .contains("For polarity=EXCLUDE and UNRESOLVED locations, use NOT_CONTAINS_NORMALIZED on the correct nameLong/nameShort field when the catalog supports it")
                .doesNotContain("Expected decision: REJECTED when no safe negative textual fallback exists")
                .doesNotContain("Excluded destination location 'Bologna' could not be resolved to stopPoint ids");
    }

    @Test
    void promptUsesMainEventIntentArrivalForPlatformDirection() {
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                true,
                List.of(richResolution(
                        "Pescara",
                        "MAIN_EVENT_LOCATION",
                        "INCLUDE",
                        List.of("payload.stopPointJourney.stopPoint.id",
                                "payload.ongroundServiceEvent.stopPoint.id"))),
                List.of(
                        new AlertVerificationLocationContext.NonLocationConstraint("PLATFORM", "binario 1"),
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "ARRIVAL")),
                List.of());

        LlmRequest request = builder().build(promptDataWithLocation(context));

        assertThat(fullPrompt(request))
                .contains("type: MAIN_EVENT_INTENT")
                .contains("rawText: \"ARRIVAL\"")
                .contains("If Location Understanding provides nonLocationConstraints MAIN_EVENT_INTENT=ARRIVAL")
                .contains("do not generate DEPARTING/DEPARTED or departure platform fields")
                .contains("use timetabledArrivalPlatform.dsc with EQUAL_PLATFORM")
                .contains("\"field\":\"timetabledArrivalPlatform.dsc\",\"operator\":\"EQUAL_PLATFORM\",\"value\":\"1\"");
    }

    @Test
    void promptMakesExpectedMainEventTypeAuthoritative() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("Authoritative main event constraints")
                .contains("EXPECTED_MAIN_EVENT_TYPE is authoritative")
                .contains("payload.ongroundServiceEvent.eventsType")
                .contains("\"operator\":\"CONTAINS\",\"value\":\"<EXPECTED_MAIN_EVENT_TYPE>\"")
                .contains("Few-shot examples are illustrative and must not override EXPECTED_MAIN_EVENT_TYPE");
    }

    @Test
    void promptSeparatesMetadataAndMapsGenericDelayThresholdFromBackendContext() {
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                false,
                List.of(),
                List.of(
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "DELAY"),
                        new AlertVerificationLocationContext.NonLocationConstraint("DELAY_EVENT_TYPE", "BOTH"),
                        new AlertVerificationLocationContext.NonLocationConstraint("DELAY_DIRECTION", "GENERIC"),
                        new AlertVerificationLocationContext.NonLocationConstraint(
                                "DELAY_THRESHOLD",
                                "operator=GREATER_THAN;value=900;unit=SECONDS"),
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", "AMBIGUOUS")),
                List.of());
        LlmRequest request = builder().build(new AlertVerificationPromptData(
                "ALRT_DELAY",
                "P5 THR 001 - generic delay no location",
                "Generic delay threshold must include both event type and delay predicates",
                "Avvisami quando una corsa ha piu di 15 minuti di ritardo",
                context));

        assertThat(fullPrompt(request))
                .contains("Metadata, not additional constraints")
                .contains("Use originalPrompt and backend-derived context as the authoritative sources of user intent")
                .contains("Do not reject because metadata mentions technical expectations")
                .contains("Backend-derived non-location constraints")
                .contains("- DELAY_EVENT_TYPE=BOTH")
                .contains("- DELAY_THRESHOLD=operator=GREATER_THAN;value=900;unit=SECONDS")
                .contains("operator: GREATER_THAN")
                .contains("value: 900")
                .contains("unit: SECONDS")
                .contains("Do not reject because originalPrompt does not mention ServiceData field names")
                .contains("The user is not expected to write payload.ongroundServiceEvent.eventsType, arrivalDelay.delay or departureDelay.delay")
                .contains("DELAY_EVENT_TYPE=BOTH plus DELAY_THRESHOLD means eventsType CONTAINS_ANY [\"ARRIVAL_DELAY\",\"DEPARTURE_DELAY\"]")
                .contains("DELAY_ROLE=PRIMARY_DELAY_EVENT means DELAY_EVENT_TYPE governs payload.ongroundServiceEvent.eventsType")
                .contains("DELAY_ROLE=ACCESSORY_DELAY_PREDICATE means the delay is only an extra predicate")
                .contains("OR over arrivalDelay.delay and departureDelay.delay with the threshold")
                .contains("\"operator\":\"CONTAINS_ANY\",\"values\":[\"ARRIVAL_DELAY\",\"DEPARTURE_DELAY\"]")
                .contains("\"field\":\"arrivalDelay.delay\",\"operator\":\"GREATER_THAN\"")
                .contains("\"field\":\"departureDelay.delay\",\"operator\":\"GREATER_THAN\"")
                .doesNotContain("Positive example - generic delay threshold with no location")
                .doesNotContain("Expected condition:");
    }

    @Test
    void promptDoesNotPrintRawDelayEventTypeText() {
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                false,
                List.of(),
                List.of(
                        new AlertVerificationLocationContext.NonLocationConstraint(
                                "DELAY_EVENT_TYPE",
                                "ha più di 15 minuti di ritardo"),
                        new AlertVerificationLocationContext.NonLocationConstraint(
                                "DELAY_THRESHOLD",
                                "operator=GREATER_THAN;value=900;unit=SECONDS")),
                List.of());

        LlmRequest request = builder().build(new AlertVerificationPromptData(
                "ALRT_DELAY",
                "P5 DLY 001",
                "Metadata only - generic delay threshold mapping",
                "Avvisami quando una corsa ha piu di 15 minuti di ritardo",
                context));

        assertThat(fullPrompt(request))
                .contains("- DELAY_EVENT_TYPE=BOTH")
                .doesNotContain("- DELAY_EVENT_TYPE=ha più di 15 minuti di ritardo");
    }

    @Test
    void promptExplainsAccessoryDelayUsesExpectedMainEventType() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("DELAY_EVENT_TYPE is authoritative only for delay-primary alerts")
                .contains("DELAY_ROLE=ACCESSORY_DELAY_PREDICATE")
                .contains("use EXPECTED_MAIN_EVENT_TYPE for payload.ongroundServiceEvent.eventsType")
                .contains("Do not replace DEPARTING/ARRIVING/DEPARTED/ARRIVED with DEPARTURE_DELAY/ARRIVAL_DELAY")
                .contains("e in partenza da X con ritardo")
                .contains("arriving train with departure delay")
                .contains("use eventsType CONTAINS ARRIVING and a departureDelay.* predicate");
    }

    @Test
    void promptExplainsMainEventMustBeatAccessoryStateFilters() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("Journey state filters introduced by \"with\", \"having\", \"con\", \"con una\", \"con un\", \"che ha\" or \"avente\" must not replace the main realtime event")
                .contains("Arrival/departure wording attached to a state filter only selects the coherent field inside stopPointsJourneyDetails[]")
                .contains("arriving train with departure cancellation")
                .contains("use eventsType CONTAINS ARRIVING and departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION")
                .contains("departing train with arrival cancellation")
                .contains("use eventsType CONTAINS DEPARTING and arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION");
    }

    @Test
    void promptRejectsFunctionalWordsAsLocationFallbacks() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("Never create stopPoint.nameLong/nameShort textual fallback values from functional words alone")
                .contains("\"in arrivo\", \"in partenza\", \"arrivo\", \"partenza\"")
                .contains("If Location Context mistakenly contains an unresolved location whose rawText is only a functional transport keyword")
                .contains("Never generate stopPoint.nameLong/nameShort conditions whose value is exactly \"in partenza\"");
    }

    @Test
    void promptContainsMainEventPhaseMappings() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("PROGRESSIVE DEPARTURE -> DEPARTING")
                .contains("COMPLETED DEPARTURE -> DEPARTED")
                .contains("PROGRESSIVE ARRIVAL -> ARRIVING")
                .contains("COMPLETED ARRIVAL -> ARRIVED")
                .contains("EXPECTED_MAIN_EVENT_TYPE=DEPARTING -> use DEPARTING, never DEPARTED")
                .contains("EXPECTED_MAIN_EVENT_TYPE=ARRIVING -> use ARRIVING, never ARRIVED");
    }

    @Test
    void promptExplainsRealtimeCancellationSemanticWorkflow() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("first identify the monitored stop/location, then the main realtime event, then journey state filters")
                .contains("Realtime cancellation workflow is language-independent")
                .contains("Generic cancellation / suppressed journey / cancelled journey at the monitored stop")
                .contains("payload.ongroundServiceEvent.eventsType CONTAINS_ANY [\"CANCELLATION\",\"ARRIVAL_CANCELLATION\",\"DEPARTURE_CANCELLATION\"]")
                .contains("Arrival cancellation / suppressed on arrival is a journey state filter")
                .contains("Do not add departureStatuses[].status NOT_CONTAINS DEPARTURE_CANCELLATION for non-exclusive arrival cancellation")
                .contains("Exclusive arrival cancellation / only suppressed on arrival")
                .contains("Departure cancellation / suppressed on departure is a journey state filter")
                .contains("Exclusive departure cancellation / only suppressed on departure")
                .contains("do not replace DEPARTING with ARRIVAL_CANCELLATION");
    }

    @Test
    void promptContainsPlatformFieldComparisonAndMovementSemantics() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("PLATFORM_EQUALS_FIELD")
                .contains("PLATFORM_NOT_EQUALS_FIELD")
                .contains("DEPARTURE_PLATFORM_CHANGED")
                .contains("ARRIVAL_PLATFORM_CHANGED")
                .contains("previousDeparturePlatform")
                .contains("previousArrivalPlatform")
                .contains("timetabled != actual")
                .contains("\"field\":\"timetabledDeparturePlatform.dsc\",\"operator\":\"PLATFORM_NOT_EQUALS_FIELD\",\"otherField\":\"actualDeparturePlatform.platform.dsc\"")
                .contains("payload.ongroundServiceEvent.eventsType CONTAINS DEPARTURE_PLATFORM_CHANGED")
                .contains("payload.ongroundServiceEvent.eventsType CONTAINS ARRIVAL_PLATFORM_CHANGED")
                .contains("payload.ongroundServiceEvent.eventsType CONTAINS_ANY [\"DEPARTURE_PLATFORM_CHANGED\",\"ARRIVAL_PLATFORM_CHANGED\"]")
                .contains("do not use departureStatuses[].status or arrivalStatuses[].status as the principal platform-change signal")
                .contains("keep the resolved Genova P.P. current stop at top level")
                .contains("\"field\":\"payload.ongroundServiceEvent.eventsType\",\"operator\":\"CONTAINS\",\"value\":\"DEPARTURE_PLATFORM_CHANGED\"")
                .contains("\"field\":\"payload.ongroundServiceEvent.eventsType\",\"operator\":\"CONTAINS_ANY\",\"values\":[\"DEPARTURE_PLATFORM_CHANGED\",\"ARRIVAL_PLATFORM_CHANGED\"]");
    }

    @Test
    void promptContainsAdvancedNumericPlatformMappingsExamplesAndBayRejection() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("PLATFORM_NUMBER_GREATER_THAN", "PLATFORM_NUMBER_GREATER_OR_EQUAL")
                .contains("PLATFORM_NUMBER_LESS_THAN", "PLATFORM_NUMBER_LESS_OR_EQUAL")
                .contains("PLATFORM_NUMBER_BETWEEN", "PLATFORM_NUMBER_EVEN", "PLATFORM_NUMBER_ODD")
                .contains("PLATFORM_NUMBER_DOUBLE_DIGIT", "PLATFORM_HAS_LETTER_SUFFIX")
                .contains("PLATFORM_NUMBER_MULTIPLE_OF")
                .contains("\"maggiore di N\" and \"superiore a N\"")
                .contains("\"tra X e Y\" and \"compreso tra X e Y\"")
                .contains("\"con una lettera\", \"con suffisso lettera\" and \"tipo 3A\"")
                .contains("Bay/terminal/dead-end platform is not available in the ServiceData Capability Catalog.")
                .contains("\"operator\":\"PLATFORM_NUMBER_GREATER_THAN\",\"value\":5")
                .contains("\"operator\":\"PLATFORM_NUMBER_BETWEEN\",\"value\":{\"min\":3,\"max\":8}")
                .contains("\"operator\":\"PLATFORM_NUMBER_EVEN\"")
                .contains("\"operator\":\"PLATFORM_HAS_LETTER_SUFFIX\"")
                .contains("Every numeric/property platform predicate must be accompanied by a top-level payload.ongroundServiceEvent.eventsType")
                .contains("\"departs\", \"has departed\" and \"departed\" -> CONTAINS DEPARTED")
                .contains("\"is departing\", \"departing\" and \"about to depart\" -> CONTAINS DEPARTING")
                .contains("\"arrives\", \"has arrived\" and \"arrived\" -> CONTAINS ARRIVED")
                .contains("\"is arriving\", \"arriving\" and \"about to arrive\" -> CONTAINS ARRIVING")
                .contains("Use CONTAINS_ANY for numeric/property platform predicates only when the prompt is truly ambiguous")
                .contains("use actualDeparturePlatform.platform.dsc for departure and actualArrivalPlatform.platform.dsc for arrival by default")
                .contains("use timetabledDeparturePlatform.dsc or timetabledArrivalPlatform.dsc only when the user explicitly says previsto, programmato, da orario")
                .contains("Do not invent a location requirement when the prompt contains only a platform constraint")
                .contains("\"field\":\"payload.ongroundServiceEvent.eventsType\",\"operator\":\"CONTAINS\",\"value\":\"DEPARTING\"")
                .contains("\"field\":\"actualDeparturePlatform.platform.dsc\",\"operator\":\"PLATFORM_NUMBER_GREATER_THAN\",\"value\":5")
                .contains("\"field\":\"actualArrivalPlatform.platform.dsc\",\"operator\":\"PLATFORM_NUMBER_BETWEEN\",\"value\":{\"min\":3,\"max\":8}")
                .contains("\"operator\":\"PLATFORM_HAS_LETTER_SUFFIX\"")
                .contains("\"field\":\"timetabledDeparturePlatform.dsc\",\"operator\":\"PLATFORM_NUMBER_GREATER_THAN\",\"value\":5")
                .doesNotContain("Prompt: \"Notify me when a train departs from a platform with a letter\"")
                .doesNotContain("Prompt: \"Avvertimi quando una corsa parte da un binario pari a Lunigiana\"");
    }

    @Test
    void promptMapsRequirementCoverageToActualCurrentStopAndPlatformFields() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("mappedBy must contain exactly the field used by the condition")
                .contains("mappedBy must list only the exact fields actually used in technicalSpecification")
                .contains("mappedBy must contain the exact platform description field used by the condition")
                .contains("For a platform change requirement, mappedBy must include payload.ongroundServiceEvent.eventsType")
                .contains("For a movement requirement such as \"spostato dal binario X al binario Y\", mappedBy must include payload.ongroundServiceEvent.eventsType")
                .contains("Excluded locations remain mappable=true when their resolved stopPoint ids are represented with NOT_IN")
                .contains("excluded resolved ids -> NOT_IN with values")
                .contains("MAIN_EVENT_PHASE=COMPLETED, use the completed current event value: DEPARTURE -> DEPARTED");
    }

    @Test
    void promptPrefersOneCorrelatedAnyElementForJourneyDetailConstraints() {
        LlmRequest request = builder().build(promptDataWithLocation(new AlertVerificationLocationContext(
                true,
                List.of(
                        richResolution(
                                "Pescara",
                                "MAIN_EVENT_LOCATION",
                                "INCLUDE",
                                List.of("payload.stopPointJourney.stopPoint.id",
                                        "payload.ongroundServiceEvent.stopPoint.id")),
                        new AlertVerificationLocationContext.LocationResolution(
                                "Bologna",
                                "Bologna",
                                "DESTINATION_LOCATION",
                                "DESTINATION_CONSTRAINT",
                                true,
                                "EXCLUDE",
                                "G2",
                                0.84,
                                "UNRESOLVED",
                                List.of(),
                                List.of(),
                                true,
                                true,
                                0.0,
                                "Location unresolved; textual fallback is allowed with lower confidence.",
                                List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id"))),
                List.of(
                        new AlertVerificationLocationContext.NonLocationConstraint("PLATFORM", "binario 1"),
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "ARRIVAL"),
                        new AlertVerificationLocationContext.NonLocationConstraint("VEHICLE_JOURNEY", "corsa 1278"),
                        new AlertVerificationLocationContext.NonLocationConstraint("DELAY", "ritardo di 14 min")),
                List.of())));

        assertThat(fullPrompt(request))
                .contains("When multiple requested constraints use fields under payload.stopPointJourney.stopPointsJourneyDetails[], put them in one anyElement")
                .contains("vehicleJourneyName, delay, platform and origin/destination constraints stay correlated to the same journey detail")
                .contains("For DESTINATION_LOCATION with polarity=EXCLUDE and status=UNRESOLVED, prefer the single canonical fallback field timetabledCallEnd.stopPoint.nameLong")
                .contains("Do not create any/OR branches across multiple negative textual fallback fields")
                .contains("mappedBy must list only the exact fields actually used in technicalSpecification");
    }

    @Test
    void promptStatesInRequiresValuesArray() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("For operator IN, always use \"values\": [...] and never \"value\"")
                .contains("Do not generate IN with empty values")
                .contains("If multiple enum values are acceptable, use IN with non-empty values")
                .contains("{\"field\":\"replacementType\",\"operator\":\"IN\",\"values\":[]}")
                .contains("{\"field\":\"replacementType\",\"operator\":\"IN\",\"value\":\"DEPARTURE\"}");
    }

    @Test
    void promptContainsReplacementWeekdayExample() {
        LlmRequest request = builder().build(promptData());

        assertThat(fullPrompt(request))
                .contains("\"corsa sostitutiva\" can be represented as isReplacementOf NOT_EMPTY")
                .contains("\"fermata sostitutiva in partenza\" can be represented with replacement.stopPointReplacements[]")
                .contains("\"nei feriali\" on a replacement departure means LOCAL_DAY_OF_WEEK_NOT_IN SATURDAY/SUNDAY on replacement.stopPointReplacements[].departureTime")
                .contains("{\"field\":\"isReplacementOf\",\"operator\":\"NOT_EMPTY\"}")
                .contains("replacementType IN DEPARTURE/ARRIVALDEPARTURE")
                .contains("LOCAL_DAY_OF_WEEK_NOT_IN SATURDAY/SUNDAY on replacement.stopPointReplacements[].departureTime")
                .doesNotContain("Prompt: \"Avvertimi quando una corsa sostitutiva ha una fermata sostitutiva in partenza nei feriali\"");
    }

    @Test
    void promptBuilderIncludesResolvedRhoLocationSection() {
        LlmRequest request = builder().build(promptDataWithLocation(new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "Rho Fieramilano",
                        "DEPARTURE_EVENT_STOP_POINT",
                        "RESOLVED",
                        List.of(new AlertVerificationLocationContext.LocationCandidate(
                                "TNPNTS00000000005467",
                                "RHO FIERAMILANO",
                                "RHO FIERAMILANO",
                                "RAIL",
                                1.0,
                                "EXACT_NORMALIZED",
                                true)),
                        false,
                        0.90)))));

        assertThat(request.userPrompt())
                .contains("PIS location runtime context v2")
                .contains("rawText: \"Rho Fieramilano\"")
                .contains("semanticRole: DEPARTURE_EVENT_STOP_POINT")
                .contains("selectedPointIds: [TNPNTS00000000005467]")
                .doesNotContain("Minimal examples")
                .doesNotContain("Positive resolved single candidate")
                .doesNotContain("Rules:");
    }

    @Test
    void promptBuilderIncludesMalpensaMultipleCandidatesWithInInstruction() {
        LlmRequest request = builder().build(promptDataWithLocation(new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "Malpensa",
                        "DEPARTURE_EVENT_STOP_POINT",
                        "RESOLVED_AMBIGUOUS",
                        List.of(
                                new AlertVerificationLocationContext.LocationCandidate(
                                        "TNPNTS00000000000028",
                                        "MALPENSA AEROPORTO T.1",
                                        "MALPENSA T1",
                                        "RAIL",
                                        0.85,
                                        "PARTIAL_TOKEN",
                                        true),
                                new AlertVerificationLocationContext.LocationCandidate(
                                        "TNPNTS00000000000029",
                                        "MALPENSA AEROPORTO T.2",
                                        "MALPENSA T2",
                                        "RAIL",
                                        0.85,
                                        "PARTIAL_TOKEN",
                                        true)),
                        false,
                        0.76)))));

        assertThat(fullPrompt(request))
                .contains("rawText: \"Malpensa\"")
                .contains("status: RESOLVED_AMBIGUOUS")
                .contains("TNPNTS00000000000028")
                .contains("TNPNTS00000000000029")
                .contains("selectedCandidates:")
                .contains("nameLong: \"MALPENSA AEROPORTO T.1\"")
                .contains("nameLong: \"MALPENSA AEROPORTO T.2\"");
    }

    @Test
    void promptBuilderIncludesUnresolvedGenovaNerviFallbackInstruction() {
        LlmRequest request = builder().build(promptDataWithLocation(new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "Genova Nervi",
                        "NEXT_TRANSIT_STOP_POINT",
                        "UNRESOLVED",
                        List.of(),
                        true,
                        0.0)))));

        assertThat(fullPrompt(request))
                .contains("rawText: \"Genova Nervi\"")
                .contains("status: UNRESOLVED")
                .contains("fallbackToNameLong: true")
                .contains("fallbackAllowed: true");
    }

    @Test
    void promptBuilderSaysDoNotInventLocationWhenNoMentions() {
        LlmRequest request = builder().build(promptDataWithLocation(AlertVerificationLocationContext.empty()));

        assertThat(request.userPrompt())
                .contains("hasLocationMentions: false")
                .contains("resolutions:\n  []")
                .contains("Never invent stopPoint ids")
                .doesNotContain("Minimal examples")
                .doesNotContain("Positive resolved single candidate")
                .doesNotContain("Rules:");
    }

    @Test
    void promptBuilderIncludesRichRoleBasedLocationSection() {
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                true,
                List.of(
                        richResolution(
                                "Garibaldi",
                                "MAIN_EVENT_LOCATION",
                                "INCLUDE",
                                List.of("payload.stopPointJourney.stopPoint.id",
                                        "payload.ongroundServiceEvent.stopPoint.id")),
                        richResolution(
                                "Venezia",
                                "ROUTE_OR_NEXT_CALL_LOCATION",
                                "INCLUDE",
                                List.of("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id")),
                        richResolution(
                                "Bologna",
                                "DESTINATION_LOCATION",
                                "EXCLUDE",
                                List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id"))),
                List.of(new AlertVerificationLocationContext.NonLocationConstraint("PLATFORM", "binario 1")),
                List.of("diagnostic warning"));

        LlmRequest request = builder().build(promptDataWithLocation(context));

        assertThat(fullPrompt(request))
                .contains("semanticRole: MAIN_EVENT_LOCATION")
                .contains("semanticRole: ROUTE_OR_NEXT_CALL_LOCATION")
                .contains("semanticRole: DESTINATION_LOCATION")
                .contains("polarity: EXCLUDE")
                .contains("requiredCoverage: true")
                .contains("targetFieldHints: [payload.stopPointJourney.stopPoint.id, payload.ongroundServiceEvent.stopPoint.id]")
                .contains("targetFieldHints: [payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id]")
                .contains("targetFieldHints: [payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id, payload.stopPointJourney.stopPointsJourneyDetails[].callEnd.stopPoint.id]")
                .contains("Every required resolved location must be represented in technicalSpecification")
                .contains("nonLocationConstraints:")
                .contains("type: PLATFORM")
                .contains("rawText: \"binario 1\"");
    }

    @Test
    void promptBuilderStatesMainEventLocationUsesCurrentStopForTest001() {
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                true,
                List.of(
                        richResolution(
                                "Garibaldi",
                                "MAIN_EVENT_LOCATION",
                                "INCLUDE",
                                List.of("payload.stopPointJourney.stopPoint.id",
                                        "payload.ongroundServiceEvent.stopPoint.id")),
                        richResolution(
                                "Venezia",
                                "ROUTE_OR_NEXT_CALL_LOCATION",
                                "INCLUDE",
                                List.of("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id"))),
                List.of(
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", "DEPARTURE"),
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", "COMPLETED")),
                List.of());

        LlmRequest request = builder().build(promptDataWithLocation(context));

        assertThat(fullPrompt(request))
                .contains("For current ARRIVED, ARRIVING, DEPARTED or DEPARTING events, the user location is the monitored current stop")
                .contains("Do not use timetabledCallStart.stopPoint.id for a current departure stop")
                .contains("rawText: \"Garibaldi\"")
                .contains("semanticRole: MAIN_EVENT_LOCATION")
                .contains("rawText: \"Venezia\"")
                .contains("semanticRole: ROUTE_OR_NEXT_CALL_LOCATION")
                .contains("payload.stopPointJourney.stopPoint.id")
                .contains("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.id")
                .contains("MAIN_EVENT_PHASE=COMPLETED, use the completed current event value: DEPARTURE -> DEPARTED");
    }

    @Test
    void eventRuntimeLocationContextDoesNotRepeatPositiveRhoFewShot() {
        LlmRequest request = builder().build(promptDataWithLocation(rhoContext()));

        assertThat(request.userPrompt())
                .contains("PIS location runtime context v2")
                .contains("selectedPointIds: [TNPNTS00000000005467]")
                .doesNotContain("Positive resolved single candidate")
                .doesNotContain("User prompt: \"Avvertimi quando una corsa parte da Rho Fieramilano\"")
                .doesNotContain("Resolved location: Rho Fieramilano -> TNPNTS00000000005467");
    }

    @Test
    void eventRuntimeLocationContextPreservesAmbiguousSelectedCandidatesWithoutExamples() {
        LlmRequest request = builder().build(promptDataWithLocation(malpensaContext()));

        assertThat(request.userPrompt())
                .contains("status: RESOLVED_AMBIGUOUS")
                .contains("selectedPointIds: [TNPNTS00000000000028, TNPNTS00000000000029]")
                .contains("selectedCandidates:")
                .doesNotContain("Positive resolved multiple candidates")
                .doesNotContain("payload.ongroundServiceEvent.stopPoint.id IN");
    }

    @Test
    void eventRuntimeLocationContextPreservesUnresolvedFallbackFlagsWithoutFallbackExample() {
        LlmRequest request = builder().build(promptDataWithLocation(unresolvedGenovaNerviContext()));

        assertThat(request.userPrompt())
                .contains("rawText: \"Genova Nervi\"")
                .contains("status: UNRESOLVED")
                .contains("fallbackToNameLong: true")
                .contains("fallbackAllowed: true")
                .doesNotContain("Fallback unresolved location")
                .doesNotContain("Expected condition may use nameLong CONTAINS_NORMALIZED");
    }

    @Test
    void eventRuntimeLocationContextDoesNotRepeatNegativeResolvedLocationExample() {
        LlmRequest request = builder().build(promptDataWithLocation(rhoContext()));

        assertThat(request.userPrompt())
                .doesNotContain("Negative resolved location")
                .doesNotContain("If a location was resolved, do not emit payload.ongroundServiceEvent.stopPoint.nameLong CONTAINS_NORMALIZED \"Rho Fieramilano\"");
    }

    private AlertVerificationPromptBuilder builder() {
        AiConfiguration configuration = mock(AiConfiguration.class);
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(configuration.alertVerify()).thenReturn(alertVerify);
        when(alertVerify.model()).thenReturn("test-model");
        when(alertVerify.temperature()).thenReturn(0.1);
        when(alertVerify.maxOutputTokens()).thenReturn(5000);

        TemporalConfiguration temporalConfiguration = mock(TemporalConfiguration.class);
        when(temporalConfiguration.defaultZone()).thenReturn("Europe/Rome");

        AlertVerificationPromptBuilder builder = new AlertVerificationPromptBuilder();
        builder.aiConfiguration = configuration;
        builder.temporalConfiguration = temporalConfiguration;
        builder.fallbackOnInvalidLlm = false;
        return builder;
    }

    private String fullPrompt(LlmRequest request) {
        return request.systemPrompt() + "\n\n" + request.userPrompt();
    }

    private AlertVerificationPromptData promptData() {
        return new AlertVerificationPromptData(
                "ALRT1",
                "Transit weekday",
                "Detect weekday transit",
                "Avvertimi quando una corsa che parte da Genova P.P nei feriali e transitera a Genova Nervi");
    }

    private AlertVerificationPromptData promptDataWithLocation(AlertVerificationLocationContext context) {
        return new AlertVerificationPromptData(
                "ALRT1",
                "Location alert",
                "Detect location alert",
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                context);
    }

    private AlertVerificationLocationContext rhoContext() {
        return new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "Rho Fieramilano",
                        "DEPARTURE_EVENT_STOP_POINT",
                        "RESOLVED",
                        List.of(new AlertVerificationLocationContext.LocationCandidate(
                                "TNPNTS00000000005467",
                                "RHO FIERAMILANO",
                                "RHO FIERAMILANO",
                                "RAIL",
                                1.0,
                                "EXACT_NORMALIZED",
                                true)),
                        false,
                        0.90)));
    }

    private AlertVerificationLocationContext malpensaContext() {
        return new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "Malpensa",
                        "DEPARTURE_EVENT_STOP_POINT",
                        "RESOLVED_AMBIGUOUS",
                        List.of(
                                new AlertVerificationLocationContext.LocationCandidate(
                                        "TNPNTS00000000000028",
                                        "MALPENSA AEROPORTO T.1",
                                        "MALPENSA T1",
                                        "RAIL",
                                        0.85,
                                        "PARTIAL_TOKEN",
                                        true),
                                new AlertVerificationLocationContext.LocationCandidate(
                                        "TNPNTS00000000000029",
                                        "MALPENSA AEROPORTO T.2",
                                        "MALPENSA T2",
                                        "RAIL",
                                        0.85,
                                        "PARTIAL_TOKEN",
                                        true)),
                        false,
                        0.76)));
    }

    private AlertVerificationLocationContext unresolvedGenovaNerviContext() {
        return new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "Genova Nervi",
                        "NEXT_TRANSIT_STOP_POINT",
                        "UNRESOLVED",
                        List.of(),
                        true,
                        0.0)));
    }

    private AlertVerificationLocationContext.LocationResolution richResolution(
            String rawText,
            String role,
            String polarity,
            List<String> targetFieldHints) {
        return new AlertVerificationLocationContext.LocationResolution(
                rawText,
                rawText,
                role,
                "EVENT_STOP_POINT",
                true,
                polarity,
                "G1",
                0.90,
                "RESOLVED",
                List.of(new AlertVerificationLocationContext.LocationCandidate(
                        "TNPNTS00000000005467",
                        rawText,
                        rawText,
                        "RAIL",
                        1.0,
                        "EXACT_NORMALIZED",
                        true)),
                List.of("TNPNTS00000000005467"),
                false,
                false,
                0.90,
                "",
                targetFieldHints);
    }
}
