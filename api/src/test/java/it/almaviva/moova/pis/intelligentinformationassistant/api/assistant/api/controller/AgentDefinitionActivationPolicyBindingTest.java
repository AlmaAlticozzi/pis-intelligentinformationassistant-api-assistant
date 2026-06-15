package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentContinuousActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDailyWindowActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionCreateRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionInvalidRequestException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentDefinitionActivationPolicyBindingTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String COMPILATION_NOT_IMPLEMENTED =
            "Agent compilation pipeline is not implemented yet. Create the Agent Definition with compileImmediately=false.";

    @Test
    void deserializesContinuousActivationPolicy() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(continuousRequestJson(), AgentDefinitionCreateRequest.class);

        assertThat(request.getActivationPolicy()).isInstanceOf(AgentContinuousActivationPolicy.class);
        AgentContinuousActivationPolicy policy = (AgentContinuousActivationPolicy) request.getActivationPolicy();
        assertThat(policy.getTimezone()).isEqualTo("Europe/Rome");
        assertThat(policy.getValidFrom()).isEqualTo(OffsetDateTime.parse("2026-06-12T10:00:00+02:00"));
        assertThat(policy.getValidTo()).isEqualTo(OffsetDateTime.parse("2026-12-31T23:59:59+01:00"));
    }

    @Test
    void deserializesContinuousActivationPolicyWithLocalDateTimesUsingTimezone() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(continuousLocalDateTimeRequestJson(), AgentDefinitionCreateRequest.class);

        assertThat(request.getActivationPolicy()).isInstanceOf(AgentContinuousActivationPolicy.class);
        AgentContinuousActivationPolicy policy = (AgentContinuousActivationPolicy) request.getActivationPolicy();
        assertThat(policy.getTimezone()).isEqualTo("Europe/Rome");
        assertThat(policy.getValidFrom()).isEqualTo(OffsetDateTime.parse("2026-06-12T00:00:00+02:00"));
        assertThat(policy.getValidTo()).isEqualTo(OffsetDateTime.parse("2026-12-31T23:59:00+01:00"));
    }

    @Test
    void createEndpointReachesServiceWithContinuousLocalDateTimes() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(realContinuousLocalDateTimeRequestJson(), AgentDefinitionCreateRequest.class);
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        when(service.createAgentDefinition(argThat(actual -> actual == request
                && actual.getActivationPolicy() instanceof AgentContinuousActivationPolicy
                && ((AgentContinuousActivationPolicy) actual.getActivationPolicy()).getValidFrom()
                .equals(OffsetDateTime.parse("2026-06-12T00:00:00+02:00")))))
                .thenReturn(new AgentDefinitionDetail().id("AGDF1").name("Ambiguous Location Resolution Malpensa T2 Agent"));
        AssistantV1Api api = new AssistantV1Api();
        api.agentDefinitionService = service;

        AgentDefinitionDetail detail = api.createAgentDefinition(request);

        assertThat(detail.getId()).isEqualTo("AGDF1");
    }

    @Test
    void rejectsContinuousLocalDateTimeWithInvalidTimezoneDuringBinding() {
        assertThatThrownBy(() -> OBJECT_MAPPER.readValue(continuousLocalDateTimeRequestJson().replace("Europe/Rome", "Europe/Fake"), AgentDefinitionCreateRequest.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("validFrom must be a valid date-time with offset or a local date-time interpreted using activationPolicy.timezone.");
    }

    @Test
    void rejectsContinuousDateWithoutTimeDuringBinding() {
        assertThatThrownBy(() -> OBJECT_MAPPER.readValue(continuousLocalDateTimeRequestJson().replace("2026-06-12T00:00", "2026-06-12"), AgentDefinitionCreateRequest.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("validFrom must be a valid date-time with offset or a local date-time interpreted using activationPolicy.timezone.");
    }

    @Test
    void createEndpointReturns400ApplicationErrorForContinuousValidToBeforeValidFrom() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(continuousInvalidRangeRequestJson(), AgentDefinitionCreateRequest.class);
        AssistantV1Api api = apiWithRealServiceValidation();

        assertThatThrownBy(() -> api.createAgentDefinition(request))
                .isInstanceOf(WebApplicationException.class)
                .satisfies(ex -> {
                    WebApplicationException webException = (WebApplicationException) ex;
                    assertThat(webException.getResponse().getStatus()).isEqualTo(400);
                    assertThat(webException.getResponse().getEntity().toString())
                            .contains("CONTINUOUS activation policy requires validFrom, validTo and validTo > validFrom.");
                });
    }

    @Test
    void beanValidationDoesNotCascadeIntoDailyWindowFieldsForContinuousPolicy() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(continuousRequestJson(), AgentDefinitionCreateRequest.class);

        assertThat(VALIDATOR.validate(request))
                .noneMatch(violation -> violation.getPropertyPath().toString().contains("validFromDate"))
                .noneMatch(violation -> violation.getPropertyPath().toString().contains("dailyStartTime"))
                .noneMatch(violation -> violation.getPropertyPath().toString().contains("dailyEndTime"));
    }

    @Test
    void deserializesDailyWindowActivationPolicy() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(dailyWindowRequestJson(), AgentDefinitionCreateRequest.class);

        assertThat(request.getActivationPolicy()).isInstanceOf(AgentDailyWindowActivationPolicy.class);
        AgentDailyWindowActivationPolicy policy = (AgentDailyWindowActivationPolicy) request.getActivationPolicy();
        assertThat(policy.getTimezone()).isEqualTo("Europe/Rome");
        assertThat(policy.getValidFromDate()).isEqualTo(LocalDate.parse("2026-06-12"));
        assertThat(policy.getValidToDate()).isEqualTo(LocalDate.parse("2026-12-31"));
        assertThat(policy.getDailyStartTime()).isEqualTo("07:00:00");
        assertThat(policy.getDailyEndTime()).isEqualTo("10:30:00");
        assertThat(policy.getDaysOfWeek()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
    }

    @Test
    void beanValidationDoesNotCascadeIntoContinuousFieldsForDailyWindowPolicy() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(dailyWindowRequestJson(), AgentDefinitionCreateRequest.class);

        assertThat(VALIDATOR.validate(request))
                .noneMatch(violation -> violation.getPropertyPath().toString().contains("validFrom"))
                .noneMatch(violation -> violation.getPropertyPath().toString().contains("validTo"));
    }

    @Test
    void createEndpointReachesServiceAndReturns409ForContinuousCompileImmediately() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(continuousRequestJson(), AgentDefinitionCreateRequest.class);
        AssistantV1Api api = apiRejectingCompilation(request);

        assertThatThrownBy(() -> api.createAgentDefinition(request))
                .isInstanceOf(WebApplicationException.class)
                .satisfies(ex -> {
                    WebApplicationException webException = (WebApplicationException) ex;
                    assertThat(webException.getResponse().getStatus()).isEqualTo(409);
                    assertThat(webException.getResponse().getEntity().toString()).contains(COMPILATION_NOT_IMPLEMENTED);
                });
    }

    @Test
    void createEndpointReachesServiceAndReturns409ForDailyWindowCompileImmediately() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(dailyWindowRequestJson(), AgentDefinitionCreateRequest.class);
        AssistantV1Api api = apiRejectingCompilation(request);

        assertThatThrownBy(() -> api.createAgentDefinition(request))
                .isInstanceOf(WebApplicationException.class)
                .satisfies(ex -> {
                    WebApplicationException webException = (WebApplicationException) ex;
                    assertThat(webException.getResponse().getStatus()).isEqualTo(409);
                    assertThat(webException.getResponse().getEntity().toString()).contains(COMPILATION_NOT_IMPLEMENTED);
                });
    }

    @Test
    void createEndpointReturns400ApplicationErrorForContinuousMissingValidToWhenCompileFalse() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(continuousMissingValidToRequestJson(), AgentDefinitionCreateRequest.class);
        AssistantV1Api api = apiWithRealServiceValidation();

        assertThatThrownBy(() -> api.createAgentDefinition(request))
                .isInstanceOf(WebApplicationException.class)
                .satisfies(ex -> {
                    WebApplicationException webException = (WebApplicationException) ex;
                    assertThat(webException.getResponse().getStatus()).isEqualTo(400);
                    assertThat(webException.getResponse().getEntity().toString())
                            .contains("CONTINUOUS activation policy requires validFrom, validTo and validTo > validFrom.");
                });
    }

    @Test
    void createEndpointReturns400ApplicationErrorForDailyWindowMissingDailyStartTimeWhenCompileFalse() throws Exception {
        AgentDefinitionCreateRequest request = OBJECT_MAPPER.readValue(dailyWindowMissingDailyStartTimeRequestJson(), AgentDefinitionCreateRequest.class);
        AssistantV1Api api = apiWithRealServiceValidation();

        assertThatThrownBy(() -> api.createAgentDefinition(request))
                .isInstanceOf(WebApplicationException.class)
                .satisfies(ex -> {
                    WebApplicationException webException = (WebApplicationException) ex;
                    assertThat(webException.getResponse().getStatus()).isEqualTo(400);
                    assertThat(webException.getResponse().getEntity().toString())
                            .contains("A local time is required.");
                });
    }

    private AssistantV1Api apiRejectingCompilation(AgentDefinitionCreateRequest expectedRequest) {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        when(service.createAgentDefinition(argThat(actual -> actual == expectedRequest
                && actual.getActivationPolicy() != null)))
                .thenThrow(new AgentDefinitionCreateRejectedException(
                        AgentDefinitionCreateRejectedException.Reason.COMPILATION_NOT_IMPLEMENTED,
                        COMPILATION_NOT_IMPLEMENTED));

        AssistantV1Api api = new AssistantV1Api();
        api.agentDefinitionService = service;
        return api;
    }

    private AssistantV1Api apiWithRealServiceValidation() {
        AssistantV1Api api = new AssistantV1Api();
        api.agentDefinitionService = new AgentDefinitionService();
        return api;
    }

    private String continuousRequestJson() {
        return """
                {
                  "alertId": "ALRTA2EEB011D1C44877A51DE91E234929AB",
                  "agentProfileId": "MEDIUM",
                  "name": "Agent Definition Test Compile Blocked",
                  "description": "Should reject compileImmediately because compilation is not implemented yet.",
                  "generationMode": "AUTO",
                  "compileImmediately": true,
                  "activationPolicy": {
                    "type": "CONTINUOUS",
                    "timezone": "Europe/Rome",
                    "validFrom": "2026-06-12T10:00:00+02:00",
                    "validTo": "2026-12-31T23:59:59+01:00"
                  }
                }
                """;
    }

    private String continuousLocalDateTimeRequestJson() {
        return """
                {
                  "alertId": "ALRTA2EEB011D1C44877A51DE91E234929AB",
                  "agentProfileId": "MEDIUM",
                  "name": "Agent Definition Test Local Date Time",
                  "description": "Should bind local date-time using timezone.",
                  "generationMode": "AUTO",
                  "compileImmediately": true,
                  "activationPolicy": {
                    "type": "CONTINUOUS",
                    "timezone": "Europe/Rome",
                    "validFrom": "2026-06-12T00:00",
                    "validTo": "2026-12-31T23:59"
                  }
                }
                """;
    }

    private String realContinuousLocalDateTimeRequestJson() {
        return """
                {
                  "alertId": "ALRT8BDC03E9A49143ADA2D071CE2383BE86",
                  "name": "Ambiguous Location Resolution Malpensa T2 Agent",
                  "agentProfileId": "MEDIUM",
                  "generationMode": "DSL",
                  "compileImmediately": false,
                  "activationPolicy": {
                    "type": "CONTINUOUS",
                    "timezone": "Europe/Rome",
                    "validFrom": "2026-06-12T00:00",
                    "validTo": "2026-12-31T23:59"
                  },
                  "description": "Agent Definition generated from verified Alert \\"Ambiguous Location Resolution Malpensa T2\\".",
                  "alertVersion": 1
                }
                """;
    }

    private String continuousInvalidRangeRequestJson() {
        return """
                {
                  "alertId": "ALRTA2EEB011D1C44877A51DE91E234929AB",
                  "agentProfileId": "MEDIUM",
                  "generationMode": "AUTO",
                  "compileImmediately": false,
                  "activationPolicy": {
                    "type": "CONTINUOUS",
                    "timezone": "Europe/Rome",
                    "validFrom": "2026-12-31T23:59",
                    "validTo": "2026-06-12T00:00"
                  }
                }
                """;
    }

    private String dailyWindowRequestJson() {
        return """
                {
                  "alertId": "ALRTA2EEB011D1C44877A51DE91E234929AB",
                  "agentProfileId": "MEDIUM",
                  "generationMode": "AUTO",
                  "compileImmediately": true,
                  "activationPolicy": {
                    "type": "DAILY_WINDOW",
                    "timezone": "Europe/Rome",
                    "validFromDate": "2026-06-12",
                    "validToDate": "2026-12-31",
                    "dailyStartTime": "07:00:00",
                    "dailyEndTime": "10:30:00",
                    "daysOfWeek": ["MONDAY", "TUESDAY"]
                  }
                }
                """;
    }

    private String continuousMissingValidToRequestJson() {
        return """
                {
                  "alertId": "ALRTA2EEB011D1C44877A51DE91E234929AB",
                  "agentProfileId": "MEDIUM",
                  "generationMode": "AUTO",
                  "compileImmediately": false,
                  "activationPolicy": {
                    "type": "CONTINUOUS",
                    "timezone": "Europe/Rome",
                    "validFrom": "2026-06-12T10:00:00+02:00"
                  }
                }
                """;
    }

    private String dailyWindowMissingDailyStartTimeRequestJson() {
        return """
                {
                  "alertId": "ALRTA2EEB011D1C44877A51DE91E234929AB",
                  "agentProfileId": "MEDIUM",
                  "generationMode": "AUTO",
                  "compileImmediately": false,
                  "activationPolicy": {
                    "type": "DAILY_WINDOW",
                    "timezone": "Europe/Rome",
                    "validFromDate": "2026-06-12",
                    "validToDate": "2026-12-31",
                    "dailyEndTime": "10:30:00"
                  }
                }
                """;
    }
}
