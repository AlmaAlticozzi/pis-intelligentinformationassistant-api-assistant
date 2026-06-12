package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionInvalidRequestException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionService;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentDefinitionSearchApiTest {

    @Test
    void returnsListResponseFromService() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentDefinitionListResponse response = new AgentDefinitionListResponse()
                .items(List.of(new AgentDefinitionSummary().id("AGDF1").name("Agent")));
        when(service.searchAgentDefinitions(new AgentDefinitionSearchCriteria(
                AgentDefinitionStatus.DRAFT,
                "ALRT1",
                AgentGenerationMode.AUTO,
                "MEDIUM",
                "test")))
                .thenReturn(response);
        AssistantV1Api api = api(service);

        AgentDefinitionListResponse result = api.searchAgentDefinitions(
                AgentDefinitionStatus.DRAFT,
                "ALRT1",
                AgentGenerationMode.AUTO,
                "MEDIUM",
                "test");

        assertThat(result).isSameAs(response);
    }

    @Test
    void mapsSearchTextTooLongToBadRequest() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        when(service.searchAgentDefinitions(new AgentDefinitionSearchCriteria(null, null, null, null, "x".repeat(201))))
                .thenThrow(new AgentDefinitionInvalidRequestException("text", "text must not exceed 200 characters."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.searchAgentDefinitions(null, null, null, null, "x".repeat(201)))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(400);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-SEA-400-003");
                    assertThat(error.getSource()).isEqualTo("text");
                });
    }

    private AssistantV1Api api(AgentDefinitionService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentDefinitionService = service;
        return api;
    }
}
