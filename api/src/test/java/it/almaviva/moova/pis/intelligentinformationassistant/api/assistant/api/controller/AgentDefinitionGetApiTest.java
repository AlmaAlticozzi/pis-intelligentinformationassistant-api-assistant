package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionInvalidRequestException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionNotFoundException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionService;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentDefinitionGetApiTest {

    @Test
    void mapsBlankIdToBadRequest() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        when(service.getAgentDefinition(" "))
                .thenThrow(new AgentDefinitionInvalidRequestException(
                        "agentDefinitionId",
                        "The agentDefinitionId path parameter is empty or contains only whitespace characters."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.getAgentDefinition(" "))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(400);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-GET-400-001");
                    assertThat(error.getSource()).isEqualTo("agentDefinitionId");
                });
    }

    @Test
    void mapsMissingDefinitionToNotFound() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        when(service.getAgentDefinition("AGDF404"))
                .thenThrow(new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.getAgentDefinition("AGDF404"))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(404);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-GET-404-001");
                    assertThat(error.getSource()).isEqualTo("agentDefinitionId");
                });
    }

    @Test
    void returnsDetailFromService() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentDefinitionDetail detail = new AgentDefinitionDetail().id("AGDF1").name("Agent");
        when(service.getAgentDefinition("AGDF1")).thenReturn(detail);
        AssistantV1Api api = api(service);

        assertThat(api.getAgentDefinition("AGDF1")).isSameAs(detail);
    }

    private AssistantV1Api api(AgentDefinitionService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentDefinitionService = service;
        return api;
    }
}
