package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationPreconditionCode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationPreconditionFailedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationPreconditionViolation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationRuntimeAcceptanceNotSupportedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionInvalidRequestException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDefinitionNotFoundException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentOrchestratorOperation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentOrchestratorUnavailableException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentDefinitionActivateApiTest {

    @Test
    void activateEndpointIsPost() throws Exception {
        assertThat(AssistantV1Api.class
                .getMethod("activateAgentDefinition", String.class, AgentActivationRequest.class)
                .isAnnotationPresent(POST.class)).isTrue();
    }

    @Test
    void mapsInvalidPathToBadRequest() {
        AgentActivationService service = mock(AgentActivationService.class);
        when(service.activate(" ", null)).thenThrow(new AgentDefinitionInvalidRequestException(
                "agentDefinitionId",
                "The agentDefinitionId path parameter is empty or contains only whitespace characters."));

        assertError(api(service), " ", null, 400, "IIA-AGD-ACT-400-001");
    }

    @Test
    void mapsMissingDefinitionToNotFound() {
        AgentActivationService service = mock(AgentActivationService.class);
        when(service.activate("AGDF404", null))
                .thenThrow(new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));

        assertError(api(service), "AGDF404", null, 404, "IIA-AGD-ACT-404-001");
    }

    @Test
    void mapsLifecycleConflictTo409() {
        AgentActivationService service = mock(AgentActivationService.class);
        when(service.activate("AGDF1", null))
                .thenThrow(new AgentActivationRejectedException("DRAFT",
                        "Agent Definition must be successfully compiled before activation"));

        assertError(api(service), "AGDF1", null, 409, "IIA-AGD-ACT-409-001");
    }

    @Test
    void mapsPreconditionFailureTo422WithoutSensitiveData() {
        AgentActivationService service = mock(AgentActivationService.class);
        when(service.activate("AGDF1", null)).thenThrow(new AgentActivationPreconditionFailedException(List.of(
                new AgentActivationPreconditionViolation(
                        AgentActivationPreconditionCode.DSL_ARTIFACT_MISSING,
                        "dslArtifact",
                        "DSL artifact is missing."))));

        assertThatThrownBy(() -> api(service).activateAgentDefinition("AGDF1", null))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(422);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-ACT-422-001");
                    assertThat(error.getDetail()).contains("DSL_ARTIFACT_MISSING", "dslArtifact");
                    assertThat(error.getDetail()).doesNotContain("runtimeContract", "artifactHash", "dslSecretMarker");
                });
    }

    @Test
    void mapsUnavailableOrchestratorTo503WithControlledMessage() {
        AgentActivationService service = mock(AgentActivationService.class);
        when(service.activate("AGDF1", null)).thenThrow(new AgentOrchestratorUnavailableException(
                AgentOrchestratorOperation.ACTIVATE,
                "AGDF1",
                "PUT",
                "/v1/runtime-agent-definitions/AGDF1",
                false));

        assertThatThrownBy(() -> api(service).activateAgentDefinition("AGDF1", null))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(503);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo("IIA-AGD-ACT-503-001");
                    assertThat(error.getDetail()).contains("state is unchanged");
                    assertThat(error.getDetail()).doesNotContain("/v1/runtime-agent-definitions", "sha256:", "dslSecretMarker");
                });
    }

    @Test
    void mapsUnexpectedRuntimeAcceptanceTo500() {
        AgentActivationService service = mock(AgentActivationService.class);
        when(service.activate("AGDF1", null)).thenThrow(
                new AgentActivationRuntimeAcceptanceNotSupportedException("Runtime acceptance not supported."));

        assertError(api(service), "AGDF1", null, 500, "IIA-AGD-ACT-500-001");
    }

    @Test
    void delegatesSuccessfulResultIfFutureServiceReturnsIt() {
        AgentActivationService service = mock(AgentActivationService.class);
        AgentDefinitionDetail detail = new AgentDefinitionDetail().id("AGDF1");
        when(service.activate("AGDF1", null)).thenReturn(detail);

        assertThat(api(service).activateAgentDefinition("AGDF1", null)).isSameAs(detail);
    }

    private void assertError(
            AssistantV1Api api,
            String agentDefinitionId,
            AgentActivationRequest request,
            int expectedStatus,
            String expectedCode) {
        assertThatThrownBy(() -> api.activateAgentDefinition(agentDefinitionId, request))
                .isInstanceOfSatisfying(WebApplicationException.class, ex -> {
                    assertThat(ex.getResponse().getStatus()).isEqualTo(expectedStatus);
                    Error error = (Error) ex.getResponse().getEntity();
                    assertThat(error.getCode()).isEqualTo(expectedCode);
                });
    }

    private AssistantV1Api api(AgentActivationService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.agentActivationService = service;
        return api;
    }
}
