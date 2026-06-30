package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgentOrchestratorDisableResultInterpreter {
    public DisableAcceptance validate(AgentOrchestratorDisableRequest request,
            AgentOrchestratorRuntimeAgentResult result, PersistedRuntimePackageSnapshot runtimePackage) {
        if (result == null) throw technical(null, "Agent Orchestrator returned no transport result.");
        if (!result.responseReceived()) throw failure(503, result, "Agent Orchestrator is unavailable.");
        if (result.httpStatus() != 200) throw map(result);
        JsonNode body = result.parsedResponseBody();
        if (body == null || !body.isObject()) throw technical(result, "Agent Orchestrator returned a malformed successful response.");
        String id = text(body, "agentDefinitionId");
        String desired = text(body, "desiredStatus");
        String runtime = text(body, "runtimeStatus");
        Long version = number(body, "packageVersion");
        String submission = optional(body, "submissionId");
        String artifactHash = body.path("artifact").path("hash").isTextual() ? body.path("artifact").path("hash").textValue() : null;
        String localHash = artifactHash(runtimePackage.payload());
        if (!request.agentDefinitionId().equals(id) || !"DISABLED".equals(desired) || !"DISABLED".equals(runtime)
                || version == null || version != runtimePackage.packageVersion()
                || (submission != null && !submission.equals(runtimePackage.submissionId()))
                || (artifactHash != null && !artifactHash.equals(localHash))) {
            throw technical(result, "Agent Orchestrator successful disable response identity or status is inconsistent.");
        }
        return new DisableAcceptance(version, submission, artifactHash);
    }

    private AgentDisableDownstreamException map(AgentOrchestratorRuntimeAgentResult result) {
        int status = switch (result.httpStatus()) {
            case 404, 409 -> 409;
            case 422 -> 422;
            case 401, 403, 429, 500, 502, 503, 504 -> 503;
            default -> 500;
        };
        return failure(status, result, safeDetail(result));
    }
    private AgentDisableDownstreamException technical(AgentOrchestratorRuntimeAgentResult result, String detail) {
        return new AgentDisableDownstreamException(500, result == null ? null : result.httpStatus(),
                safe(result, "code"), safe(result, "traceId"), detail);
    }
    private AgentDisableDownstreamException failure(int status, AgentOrchestratorRuntimeAgentResult result, String detail) {
        System.out.println("[IIA][AGENT_DISABLE] downstream failure mapped downstreamHttpStatus=" + result.httpStatus()
                + " assistantHttpStatus=" + status + " stateChangeApplied=false");
        return new AgentDisableDownstreamException(status, result.httpStatus(), safe(result, "code"), safe(result, "traceId"), detail);
    }
    private String safeDetail(AgentOrchestratorRuntimeAgentResult r) { String v = safe(r, "detail"); return v == null ? "Agent Orchestrator rejected disable." : v; }
    private String safe(AgentOrchestratorRuntimeAgentResult r, String f) { return r == null || r.parsedResponseBody() == null ? null : optional(r.parsedResponseBody(), f); }
    private String text(JsonNode b, String f) { String v = optional(b, f); return v == null || v.isBlank() ? null : v; }
    private String optional(JsonNode b, String f) { JsonNode v=b.get(f); return v != null && v.isTextual() && !v.textValue().isBlank() ? v.textValue().substring(0, Math.min(500, v.textValue().length())) : null; }
    private Long number(JsonNode b, String f) { JsonNode v=b.get(f); return v != null && v.isIntegralNumber() ? v.longValue() : null; }
    private String artifactHash(java.util.Map<String,Object> payload) { Object a=payload.get("artifact"); if (a instanceof java.util.Map<?,?> m && m.get("hash") instanceof String s) return s; Object d=payload.get("agentDefinition"); if (d instanceof java.util.Map<?,?> dm && dm.get("artifact") instanceof java.util.Map<?,?> am && am.get("hash") instanceof String s) return s; return null; }
    public record DisableAcceptance(long packageVersion, String submissionId, String artifactHash) {}
}
