package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import org.junit.jupiter.api.Test;

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
}
