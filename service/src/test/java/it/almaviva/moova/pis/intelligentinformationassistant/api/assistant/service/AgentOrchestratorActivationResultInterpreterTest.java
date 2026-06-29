package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentOrchestratorActivationResultInterpreterTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private final AgentOrchestratorActivationResultInterpreter interpreter =
            new AgentOrchestratorActivationResultInterpreter();

    @Test
    void accepts201LoadedAnd200LoadedReplay() throws Exception {
        assertThat(interpreter.validate(request(), result(201, acceptedBody("AGDF1", 1))).runtimeStatus())
                .isEqualTo("LOADED");
        assertThat(interpreter.validate(request(), result(200, acceptedBody("AGDF1", 1))).downstreamHttpStatus())
                .isEqualTo(200);
    }

    @Test
    void rejectsIdentityInconsistent2xx() throws Exception {
        assertThatThrownBy(() -> interpreter.validate(request(), result(201, acceptedBody("OTHER", 1))))
                .isInstanceOfSatisfying(AgentActivationDownstreamException.class,
                        ex -> assertThat(ex.assistantHttpStatus()).isEqualTo(500));
    }

    @Test
    void maps409And422WithoutAcceptingState() throws Exception {
        assertMapping(409, 409);
        assertMapping(422, 422);
    }

    @Test
    void maps429AndConnectionFailureTo503() throws Exception {
        assertMapping(429, 503);
        AgentOrchestratorRuntimeAgentResult unavailable = new AgentOrchestratorRuntimeAgentResult(
                "AGDF1", "ACTIVE", null, 1, "SUB1", null, null, null, List.of(),
                0, false, null, null, "CONNECTION_FAILURE");
        assertThatThrownBy(() -> interpreter.validate(request(), unavailable))
                .isInstanceOfSatisfying(AgentActivationDownstreamException.class,
                        ex -> assertThat(ex.assistantHttpStatus()).isEqualTo(503));
    }

    private void assertMapping(int downstream, int assistant) throws Exception {
        JsonNode body = JSON.readTree("{\"code\":\"DOWNSTREAM\",\"detail\":\"safe detail\",\"traceId\":\"trace-1\"}");
        assertThatThrownBy(() -> interpreter.validate(request(), result(downstream, body)))
                .isInstanceOfSatisfying(AgentActivationDownstreamException.class, ex -> {
                    assertThat(ex.assistantHttpStatus()).isEqualTo(assistant);
                    assertThat(ex.downstreamCode()).isEqualTo("DOWNSTREAM");
                    assertThat(ex.downstreamTraceId()).isEqualTo("trace-1");
                });
    }

    private JsonNode acceptedBody(String agentDefinitionId, long version) throws Exception {
        return JSON.readTree("""
                {"agentDefinitionId":"%s","desiredStatus":"ACTIVE","runtimeStatus":"LOADED",
                 "packageVersion":%d,"submissionId":"SUB1","artifact":{"hash":"sha256:artifact"}}
                """.formatted(agentDefinitionId, version));
    }

    private AgentOrchestratorRuntimeAgentResult result(int status, JsonNode body) {
        return new AgentOrchestratorRuntimeAgentResult(
                "AGDF1", "ACTIVE", null, 1, "SUB1", null, null, null, List.of(),
                status, true, body.toString(), body, status >= 200 && status < 300 ? "ACCEPTED" : "HTTP_" + status);
    }

    private AgentOrchestratorActivationRequest request() {
        AgentRuntimeSubmission submission = new AgentRuntimeSubmission(
                "SUB1", "ACTIVE", 1, Instant.parse("2026-06-29T10:00:00Z"), "test", true, null,
                new AgentRuntimeSubmission.AgentRuntimeDefinitionPackage(
                        "AGDF1", "Agent", null, null, null, null, null, null, null, null,
                        null, null, Map.of(), null, null, List.of()));
        Map<String, Object> payload = Map.of(
                "submissionId", "SUB1",
                "packageVersion", 1L,
                "agentDefinition", Map.of("id", "AGDF1", "artifact", Map.of("hash", "sha256:artifact")));
        return new AgentOrchestratorActivationRequest(
                "AGDF1", submission, "sha256:" + "a".repeat(64), "RTPK1", payload);
    }
}
