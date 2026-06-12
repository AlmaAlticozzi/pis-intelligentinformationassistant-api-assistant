package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentContinuousActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDailyWindowActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionCreateRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionService;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentDefinitionActivationPolicyBindingTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
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
}
