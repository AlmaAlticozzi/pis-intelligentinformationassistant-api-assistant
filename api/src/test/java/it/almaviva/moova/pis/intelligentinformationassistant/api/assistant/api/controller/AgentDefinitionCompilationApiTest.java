package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatusResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionInvalidRequestException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionNotFoundException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentDefinitionCompilationApiTest {

    @Test
    void getCompilationEndpointIsGet() throws Exception {
        assertThat(AssistantV1Api.class
                .getMethod("getAgentCompilationStatus", String.class)
                .isAnnotationPresent(GET.class)).isTrue();
    }

    @Test
    void mapsInvalidAgentDefinitionIdToBadRequest() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        when(service.getAgentDefinitionCompilation(" "))
                .thenThrow(new AgentDefinitionInvalidRequestException(
                        "agentDefinitionId",
                        "The agentDefinitionId path parameter is empty or contains only whitespace characters."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.getAgentCompilationStatus(" "))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(400);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-CMS-400-001");
                    assertThat(error.getSource()).isEqualTo("agentDefinitionId");
                });
    }

    @Test
    void mapsMissingCompilationToNotFound() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        when(service.getAgentDefinitionCompilation("AGDF1"))
                .thenThrow(new AgentDefinitionNotFoundException(
                        "agentDefinitionId",
                        "No compilation found for Agent Definition AGDF1."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.getAgentCompilationStatus("AGDF1"))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(404);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-CMS-404-001");
                    assertThat(error.getDetail()).isEqualTo("No compilation found for Agent Definition AGDF1.");
                });
    }

    @Test
    void returnsCompilationStatusFromService() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentCompilationStatusResponse response = new AgentCompilationStatusResponse()
                .agentDefinitionId("AGDF1")
                .status(AgentCompilationStatus.READY);
        when(service.getAgentDefinitionCompilation("AGDF1")).thenReturn(response);
        AssistantV1Api api = api(service);

        assertThat(api.getAgentCompilationStatus("AGDF1")).isSameAs(response);
    }

    private AssistantV1Api api(AgentDefinitionService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentDefinitionService = service;
        return api;
    }
}
