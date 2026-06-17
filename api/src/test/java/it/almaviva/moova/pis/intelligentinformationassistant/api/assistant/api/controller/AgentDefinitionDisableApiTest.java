package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDisableRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionInvalidRequestException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionNotFoundException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDisableRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDisableRuntimeAcceptanceNotSupportedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDisableService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentOrchestratorCommandRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentOrchestratorOperation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentOrchestratorUnavailableException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentDefinitionDisableApiTest {

    @Test
    void disableEndpointIsPost() throws Exception {
        assertThat(AssistantV1Api.class
                .getMethod("disableAgentDefinition", String.class, AgentDisableRequest.class)
                .isAnnotationPresent(POST.class)).isTrue();
    }

    @Test
    void mapsInvalidRequestToBadRequest() {
        AgentDisableService service = mock(AgentDisableService.class);
        AgentDisableRequest request = new AgentDisableRequest().gracePeriodSeconds(-1);
        when(service.disable("AGDF1", request)).thenThrow(new AgentDefinitionInvalidRequestException(
                "gracePeriodSeconds",
                "gracePeriodSeconds must be greater than or equal to 0"));

        assertError(api(service), "AGDF1", request, 400, "IIA-AGD-DIS-400-001");
    }

    @Test
    void mapsMissingDefinitionToNotFound() {
        AgentDisableService service = mock(AgentDisableService.class);
        when(service.disable("AGDF404", null))
                .thenThrow(new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));

        assertError(api(service), "AGDF404", null, 404, "IIA-AGD-DIS-404-001");
    }

    @Test
    void mapsLifecycleConflictTo409() {
        AgentDisableService service = mock(AgentDisableService.class);
        when(service.disable("AGDF1", null))
                .thenThrow(new AgentDisableRejectedException("DRAFT", "Agent Definition cannot be disabled from DRAFT status"));

        assertError(api(service), "AGDF1", null, 409, "IIA-AGD-DIS-409-001");
    }

    @Test
    void mapsRuntimeRejectionTo422() {
        AgentDisableService service = mock(AgentDisableService.class);
        when(service.disable("AGDF1", null)).thenThrow(new AgentOrchestratorCommandRejectedException(
                AgentOrchestratorOperation.DISABLE,
                "AGDF1",
                "STOP_POLICY_REJECTED",
                "Runtime rejected stop policy."));

        assertError(api(service), "AGDF1", null, 422, "IIA-AGD-DIS-422-001");
    }

    @Test
    void mapsUnavailableOrchestratorTo503WithControlledMessage() {
        AgentDisableService service = mock(AgentDisableService.class);
        when(service.disable("AGDF1", null)).thenThrow(new AgentOrchestratorUnavailableException(
                AgentOrchestratorOperation.DISABLE,
                "AGDF1",
                "POST",
                "/v1/runtime-agent-definitions/AGDF1/disable",
                false));

        assertThatThrownBy(() -> api(service).disableAgentDefinition("AGDF1", null))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(503);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-DIS-503-001");
                    assertThat(error.getDetail()).contains("state is unchanged");
                    assertThat(error.getDetail()).doesNotContain("/v1/runtime-agent-definitions", "reason", "credential");
                });
    }

    @Test
    void mapsUnexpectedRuntimeAcceptanceTo500() {
        AgentDisableService service = mock(AgentDisableService.class);
        when(service.disable("AGDF1", null)).thenThrow(
                new AgentDisableRuntimeAcceptanceNotSupportedException("Runtime acceptance not supported."));

        assertError(api(service), "AGDF1", null, 500, "IIA-AGD-DIS-500-001");
    }

    @Test
    void returnsDetailForReadyOrDisabledSuccess() {
        AgentDisableService service = mock(AgentDisableService.class);
        AgentDefinitionDetail detail = new AgentDefinitionDetail()
                .id("AGDF1")
                .status(AgentDefinitionStatus.DISABLED);
        when(service.disable("AGDF1", null)).thenReturn(detail);

        assertThat(api(service).disableAgentDefinition("AGDF1", null)).isSameAs(detail);
    }

    private void assertError(
            AssistantV1Api api,
            String agentDefinitionId,
            AgentDisableRequest request,
            int expectedStatus,
            String expectedCode) {
        assertThatThrownBy(() -> api.disableAgentDefinition(agentDefinitionId, request))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(expectedStatus);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo(expectedCode);
                });
    }

    private AssistantV1Api api(AgentDisableService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentDisableService = service;
        return api;
    }
}
