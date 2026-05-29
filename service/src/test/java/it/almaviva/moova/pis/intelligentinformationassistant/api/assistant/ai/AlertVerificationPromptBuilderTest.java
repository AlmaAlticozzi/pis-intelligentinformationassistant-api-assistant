package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertVerificationPromptBuilderTest {

    @Test
    void promptContainsSemanticMappingForWeekdaysAndFeriali() {
        LlmRequest request = builder().build(promptData());

        assertThat(request.userPrompt())
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

        assertThat(request.userPrompt())
                .contains("The existence of an array element is represented by anyElement")
                .contains("anyElement on nextTransitCalls[] with stopPoint.nameLong already represents existence of a matching transit call")
                .contains("transitera a <stop>\" without a time/day predicate means use anyElement on nextTransitCalls[] with stopPoint.nameLong only. Do not add passingTime");
    }

    @Test
    void promptForbidsExistsOnTimestampFields() {
        LlmRequest request = builder().build(promptData());

        assertThat(request.userPrompt())
                .contains("Do not use EXISTS on timestamp fields such as passingTime, departureTime, arrivalTime, eventGenerationTime or timetabledCallStart.departureTime")
                .contains("{\"field\":\"passingTime\",\"operator\":\"EXISTS\"}")
                .contains("EXISTS on passingTime is not needed and not allowed");
    }

    @Test
    void promptContainsChangeEnumMappings() {
        LlmRequest request = builder().build(promptData());

        assertThat(request.userPrompt())
                .contains("\"cambia origine\", \"cambio origine\" and \"origine cambiata\" mean changes CONTAINS CHANGED_ORIGIN")
                .contains("\"cambia destinazione\", \"cambio destinazione\" and \"destinazione cambiata\" mean changes CONTAINS CHANGED_DESTINATION")
                .contains("\"cambia binario\", \"cambio binario\" and \"platform changed\" mean changes CONTAINS PLATFORM_CHANGED")
                .contains("\"field\":\"changes\",\"operator\":\"CONTAINS\",\"value\":\"CHANGED_ORIGIN\"")
                .contains("Do not reject change prompts as stateful comparison when the requested change is directly represented by the ServiceData changes enum");
    }

    @Test
    void promptStatesInRequiresValuesArray() {
        LlmRequest request = builder().build(promptData());

        assertThat(request.userPrompt())
                .contains("For operator IN, always use \"values\": [...] and never \"value\"")
                .contains("Do not generate IN with empty values")
                .contains("If multiple enum values are acceptable, use IN with non-empty values")
                .contains("{\"field\":\"replacementType\",\"operator\":\"IN\",\"values\":[]}")
                .contains("{\"field\":\"replacementType\",\"operator\":\"IN\",\"value\":\"DEPARTURE\"}");
    }

    @Test
    void promptContainsReplacementWeekdayExample() {
        LlmRequest request = builder().build(promptData());

        assertThat(request.userPrompt())
                .contains("\"corsa sostitutiva\" can be represented as isReplacementOf NOT_EMPTY")
                .contains("\"fermata sostitutiva in partenza\" can be represented with replacement.stopPointReplacements[]")
                .contains("\"nei feriali\" on a replacement departure means LOCAL_DAY_OF_WEEK_NOT_IN SATURDAY/SUNDAY on replacement.stopPointReplacements[].departureTime")
                .contains("Prompt: \"Avvertimi quando una corsa sostitutiva ha una fermata sostitutiva in partenza nei feriali\"")
                .contains("{\"field\":\"isReplacementOf\",\"operator\":\"NOT_EMPTY\"}")
                .contains("{\"field\":\"replacementType\",\"operator\":\"IN\",\"values\":[\"DEPARTURE\",\"ARRIVALDEPARTURE\"]}")
                .contains("{\"field\":\"departureTime\",\"operator\":\"LOCAL_DAY_OF_WEEK_NOT_IN\"");
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
                .contains("PIS location resolution")
                .contains("rawText: \"Rho Fieramilano\"")
                .contains("semanticRole: DEPARTURE_EVENT_STOP_POINT")
                .contains("id: TNPNTS00000000005467")
                .contains("If a location has one resolved candidate, use stopPoint.id with EQUALS")
                .contains("Do not use stopPoint.nameLong/nameShort for resolved locations");
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

        assertThat(request.userPrompt())
                .contains("rawText: \"Malpensa\"")
                .contains("status: RESOLVED_AMBIGUOUS")
                .contains("TNPNTS00000000000028")
                .contains("TNPNTS00000000000029")
                .contains("If a location has multiple selected candidates, use stopPoint.id with IN");
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

        assertThat(request.userPrompt())
                .contains("rawText: \"Genova Nervi\"")
                .contains("status: UNRESOLVED")
                .contains("fallback: use nameLong CONTAINS_NORMALIZED")
                .contains("If a location is unresolved, use nameLong CONTAINS_NORMALIZED as fallback and lower confidence");
    }

    @Test
    void promptBuilderSaysDoNotInventLocationWhenNoMentions() {
        LlmRequest request = builder().build(promptDataWithLocation(AlertVerificationLocationContext.empty()));

        assertThat(request.userPrompt())
                .contains("No PIS location mentions were detected")
                .contains("If the user did not mention a location, do not add any location condition")
                .contains("Never invent stopPoint ids")
                .contains("Never use stopPoint ids not listed in the resolved candidates section");
    }

    @Test
    void builderContainsPositiveExampleForRhoStopPointId() {
        LlmRequest request = builder().build(promptDataWithLocation(rhoContext()));

        assertThat(request.userPrompt())
                .contains("Positive resolved single candidate")
                .contains("User prompt: \"Avvertimi quando una corsa parte da Rho Fieramilano\"")
                .contains("Resolved location: Rho Fieramilano -> TNPNTS00000000005467")
                .contains("payload.ongroundServiceEvent.eventsType CONTAINS DEPARTED")
                .contains("payload.ongroundServiceEvent.stopPoint.id EQUALS TNPNTS00000000005467")
                .contains("requirementCoverage: location constraint mappedBy [payload.ongroundServiceEvent.stopPoint.id]");
    }

    @Test
    void builderContainsPositiveExampleForMalpensaInStopPointIds() {
        LlmRequest request = builder().build(promptDataWithLocation(malpensaContext()));

        assertThat(request.userPrompt())
                .contains("Positive resolved multiple candidates")
                .contains("Resolved locations: Malpensa -> TNPNTS00000000000028, TNPNTS00000000000029")
                .contains("payload.ongroundServiceEvent.stopPoint.id IN [TNPNTS00000000000028, TNPNTS00000000000029]")
                .contains("RESOLVED_AMBIGUOUS with multiple selected candidates -> use IN on the correct stopPoint.id field");
    }

    @Test
    void builderContainsFallbackExampleForUnresolvedLocation() {
        LlmRequest request = builder().build(promptDataWithLocation(unresolvedGenovaNerviContext()));

        assertThat(request.userPrompt())
                .contains("Fallback unresolved location")
                .contains("User prompt: \"Avvertimi quando un treno passa da Genova Nervi\"")
                .contains("Location unresolved.")
                .contains("Expected condition may use nameLong CONTAINS_NORMALIZED \"Genova Nervi\"")
                .contains("Expected warning and low confidence; requirementCoverage must mention fallback text matching")
                .contains("UNRESOLVED -> fallback to nameLong CONTAINS_NORMALIZED and lower confidence");
    }

    @Test
    void builderContainsNegativeRuleAgainstNameLongForResolvedLocation() {
        LlmRequest request = builder().build(promptDataWithLocation(rhoContext()));

        assertThat(request.userPrompt())
                .contains("Negative resolved location")
                .contains("If a location was resolved, do not emit payload.ongroundServiceEvent.stopPoint.nameLong CONTAINS_NORMALIZED \"Rho Fieramilano\"")
                .contains("Do not use stopPoint.nameLong/nameShort for resolved locations");
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
}
