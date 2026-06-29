package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.fnd.core.lib.quarkuscommon.http.rest.FNDRestClient;
import it.almaviva.fnd.core.lib.quarkuscommon.http.rest.util.FNDRequestForResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FndAgentOrchestratorGatewayTest {

    @Test
    void preservesAcceptedAndValidationErrorResponsesAndBuildsFoundationPutFromPersistedPayload() {
        FNDRestClient client = mock(FNDRestClient.class);
        FndAgentOrchestratorGateway gateway = new FndAgentOrchestratorGateway(
                client, new ObjectMapper(), null, "https://orchestrator.example/", "/api/", "agent-orchestrator");
        Map<String, Object> payload = payload();
        AgentOrchestratorActivationRequest activation = request(payload);
        Response created = mock(Response.class);
        when(created.getStatus()).thenReturn(201);
        when(created.hasEntity()).thenReturn(true);
        when(created.readEntity(String.class)).thenReturn("{\"runtimeStatus\":\"LOADED\"}");
        when(client.requestForResponse(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        AgentOrchestratorRuntimeAgentResult accepted = gateway.activate(activation);

        ArgumentCaptor<FNDRequestForResponse> requestCaptor = ArgumentCaptor.forClass(FNDRequestForResponse.class);
        verify(client).requestForResponse(requestCaptor.capture());
        FNDRequestForResponse sent = requestCaptor.getValue();
        assertThat(sent.getHttpMethod().name()).isEqualTo("PUT");
        assertThat(sent.getUrl()).isEqualTo("https://orchestrator.example/api/v1/runtime-agent-definitions/{agentDefinitionId}");
        assertThat(sent.getPathParams()).containsEntry("agentDefinitionId", "AGDF1");
        assertThat(sent.getBody()).isSameAs(activation.persistedPayload());
        assertThat(sent.getOidcClientId()).isEqualTo("agent-orchestrator");
        assertThat(accepted.httpStatus()).isEqualTo(201);
        assertThat(accepted.rawResponseBody()).contains("LOADED");
        assertThat(accepted.parsedResponseBody().path("runtimeStatus").asText()).isEqualTo("LOADED");
        verify(created).close();

        Response invalid = mock(Response.class);
        when(invalid.getStatus()).thenReturn(422);
        when(invalid.hasEntity()).thenReturn(true);
        when(invalid.readEntity(String.class)).thenReturn("{\"code\":\"INVALID_PACKAGE\"}");
        when(client.requestForResponse(org.mockito.ArgumentMatchers.any())).thenReturn(invalid);

        AgentOrchestratorRuntimeAgentResult rejected = gateway.activate(activation);

        assertThat(rejected.httpStatus()).isEqualTo(422);
        assertThat(rejected.outcomeCategory()).isEqualTo("HTTP_422");
        assertThat(rejected.rawResponseBody()).contains("INVALID_PACKAGE");
        verify(invalid).close();
    }

    private AgentOrchestratorActivationRequest request(Map<String, Object> payload) {
        AgentRuntimeSubmission submission = new ObjectMapper().convertValue(payload, AgentRuntimeSubmission.class);
        return new AgentOrchestratorActivationRequest(
                "AGDF1", submission, "sha256:" + "a".repeat(64), "RTPK1", payload);
    }

    private Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("submissionId", "ACTIVATE:AGDF1:1:test");
        payload.put("desiredStatus", "ACTIVE");
        payload.put("packageVersion", 1L);
        payload.put("submittedAt", Instant.parse("2026-06-29T10:00:00Z").toString());
        payload.put("submittedBy", "test");
        payload.put("agentDefinition", Map.of("id", "AGDF1"));
        payload.put("artifact", Map.of());
        payload.put("runtimeProfile", Map.of());
        payload.put("dataSources", java.util.List.of());
        payload.put("activationPolicy", Map.of());
        return payload;
    }
}
