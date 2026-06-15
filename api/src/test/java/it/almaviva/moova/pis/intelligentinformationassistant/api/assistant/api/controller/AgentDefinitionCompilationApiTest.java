package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatusResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentCompilationRejectedException;
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

    @Test
    void compileMapsBlankIdToBadRequest() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentCompilationRequest request = new AgentCompilationRequest();
        when(service.compileAgentDefinition(" ", request))
                .thenThrow(new AgentDefinitionInvalidRequestException(
                        "agentDefinitionId",
                        "The agentDefinitionId path parameter is empty or contains only whitespace characters."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.compileAgentDefinition(" ", request))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(400);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-CMP-400-001");
                });
    }

    @Test
    void compileMapsMissingDefinitionToNotFound() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentCompilationRequest request = new AgentCompilationRequest();
        when(service.compileAgentDefinition("AGDF404", request))
                .thenThrow(new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.compileAgentDefinition("AGDF404", request))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(404);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-CMP-404-001");
                });
    }

    @Test
    void compileMapsConflictTo409() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentCompilationRequest request = new AgentCompilationRequest();
        when(service.compileAgentDefinition("AGDF1", request))
                .thenThrow(new AgentCompilationRejectedException(
                        AgentCompilationRejectedException.Reason.CONFLICT,
                        "A compilation is already running for Agent Definition AGDF1."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.compileAgentDefinition("AGDF1", request))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(409);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-CMP-409-001");
                });
    }

    @Test
    void compileMapsUnprocessableTo422() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentCompilationRequest request = new AgentCompilationRequest();
        when(service.compileAgentDefinition("AGDF1", request))
                .thenThrow(new AgentCompilationRejectedException(
                        AgentCompilationRejectedException.Reason.UNPROCESSABLE,
                        "Generation mode JAVA_TEMPLATE is not supported by the DSL compilation MVP."));
        AssistantV1Api api = api(service);

        assertThatThrownBy(() -> api.compileAgentDefinition("AGDF1", request))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(422);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-CMP-422-001");
                });
    }

    @Test
    void compileReturnsSkeletonResponseFromService() {
        AgentDefinitionService service = mock(AgentDefinitionService.class);
        AgentCompilationRequest request = new AgentCompilationRequest();
        AgentCompilationStatusResponse response = new AgentCompilationStatusResponse()
                .agentDefinitionId("AGDF1")
                .status(AgentCompilationStatus.FAILED)
                .currentStep("GENERATING_ARTIFACT");
        when(service.compileAgentDefinition("AGDF1", request)).thenReturn(response);
        AssistantV1Api api = api(service);

        assertThat(api.compileAgentDefinition("AGDF1", request)).isSameAs(response);
    }

    private AssistantV1Api api(AgentDefinitionService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentDefinitionService = service;
        return api;
    }
}
