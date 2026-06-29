package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationDownstreamException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationService;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentActivationB3ApiTest {

    @Test
    void returnsAssistantDetailAfterAcceptedActivation() {
        AgentActivationService service = mock(AgentActivationService.class);
        AgentDefinitionDetail detail = new AgentDefinitionDetail().id("AGDF1");
        when(service.activate("AGDF1", null)).thenReturn(detail);
        assertThat(api(service).activateAgentDefinition("AGDF1", null)).isSameAs(detail);
    }

    @Test
    void mapsDownstream409And422() {
        assertMapping(409, 409, "IIA-AGD-ACT-409-001");
        assertMapping(422, 422, "IIA-AGD-ACT-422-001");
    }

    @Test
    void mapsRetryableAndIntegrationFailuresTo503() {
        assertMapping(503, 429, "IIA-AGD-ACT-503-001");
        assertMapping(503, 500, "IIA-AGD-ACT-503-001");
        assertMapping(503, 401, "IIA-AGD-ACT-503-001");
    }

    private void assertMapping(int assistantStatus, int downstreamStatus, String expectedCode) {
        AgentActivationService service = mock(AgentActivationService.class);
        when(service.activate("AGDF1", null)).thenThrow(new AgentActivationDownstreamException(
                assistantStatus, downstreamStatus, "DOWNSTREAM", "trace-1", "safe detail"));
        assertThatThrownBy(() -> api(service).activateAgentDefinition("AGDF1", null))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(assistantStatus);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo(expectedCode);
                    assertThat(error.getDetail()).isEqualTo("safe detail");
                });
    }

    private AssistantV1Api api(AgentActivationService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentActivationService = service;
        return api;
    }
}
