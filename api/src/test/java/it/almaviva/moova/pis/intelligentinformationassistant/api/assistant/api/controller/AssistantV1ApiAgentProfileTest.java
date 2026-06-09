package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfileListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentProfileService;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssistantV1ApiAgentProfileTest {

    @Test
    void listAgentProfilesReturnsServiceResponse() {
        AgentProfileService service = mock(AgentProfileService.class);
        when(service.listAgentProfiles()).thenReturn(new AgentProfileListResponse()
                .items(List.of(new AgentProfile().id("MEDIUM"))));

        AssistantV1Api api = api(service);

        AgentProfileListResponse response = api.listAgentProfiles();

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getId()).isEqualTo("MEDIUM");
    }

    @Test
    void getAgentProfileReturnsExistingProfile() {
        AgentProfileService service = mock(AgentProfileService.class);
        when(service.getAgentProfile("MEDIUM")).thenReturn(Optional.of(new AgentProfile().id("MEDIUM")));

        AssistantV1Api api = api(service);

        assertThat(api.getAgentProfile("MEDIUM").getId()).isEqualTo("MEDIUM");
    }

    @Test
    void getAgentProfileReturns404WhenMissing() {
        AgentProfileService service = mock(AgentProfileService.class);
        when(service.getAgentProfile("UNKNOWN")).thenReturn(Optional.empty());

        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.getAgentProfile("UNKNOWN"))
                .isInstanceOf(WebApplicationException.class)
                .extracting(ex -> ((WebApplicationException) ex).getResponse().getStatus())
                .isEqualTo(404);
    }

    private AssistantV1Api api(AgentProfileService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentProfileService = service;
        return api;
    }
}
