package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class AgentOrchestratorActivationResultInterpreter {

    private static final Set<String> ACCEPTED_RUNTIME_STATUSES = Set.of("LOADED", "ACTIVE", "IDLE");

    public ValidatedActivationAcceptance validate(
            AgentOrchestratorActivationRequest request,
            AgentOrchestratorRuntimeAgentResult result) {
        if (result == null) throw technical("Agent Orchestrator returned no transport result.", null);
        if (!result.responseReceived()) {
            throw downstream(503, result, "Agent Orchestrator is unavailable.");
        }
        if (result.httpStatus() != 200 && result.httpStatus() != 201) {
            throw mappedHttpFailure(result);
        }
        JsonNode body = result.parsedResponseBody();
        if (body == null || !body.isObject()) {
            throw technical("Agent Orchestrator returned a malformed successful response.", result);
        }
        String agentDefinitionId = text(body, "agentDefinitionId");
        String desiredStatus = text(body, "desiredStatus");
        String runtimeStatus = text(body, "runtimeStatus");
        Long packageVersion = longValue(body, "packageVersion");
        String submissionId = optionalText(body, "submissionId");
        if (!request.agentDefinitionId().equals(agentDefinitionId)
                || !"ACTIVE".equals(desiredStatus)
                || packageVersion == null
                || packageVersion != request.submission().packageVersion()
                || (submissionId != null && !submissionId.equals(request.submission().submissionId()))
                || !ACCEPTED_RUNTIME_STATUSES.contains(runtimeStatus)) {
            throw technical("Agent Orchestrator successful response identity or status is inconsistent.", result);
        }
        String remoteArtifactHash = body.path("artifact").path("hash").isTextual()
                ? body.path("artifact").path("hash").textValue() : null;
        String persistedArtifactHash = persistedArtifactHash(request.persistedPayload());
        if (remoteArtifactHash != null && !remoteArtifactHash.equals(persistedArtifactHash)) {
            throw technical("Agent Orchestrator successful response artifact hash is inconsistent.", result);
        }
        System.out.println("[IIA][AGENT_ACTIVATION] downstream accepted agentDefinitionId=" + agentDefinitionId
                + " runtimePackageId=" + request.runtimePackageId()
                + " packageVersion=" + packageVersion
                + " submissionId=" + request.submission().submissionId()
                + " httpStatus=" + result.httpStatus()
                + " runtimeStatus=" + runtimeStatus);
        return new ValidatedActivationAcceptance(runtimeStatus, result.httpStatus());
    }

    private AgentActivationDownstreamException mappedHttpFailure(AgentOrchestratorRuntimeAgentResult result) {
        int assistantStatus = switch (result.httpStatus()) {
            case 409 -> 409;
            case 422 -> 422;
            case 401, 403, 429, 500, 503 -> 503;
            case 400 -> 500;
            default -> 500;
        };
        String detail = switch (result.httpStatus()) {
            case 401, 403 -> "Agent Orchestrator service authentication failed.";
            case 429 -> "Agent Orchestrator runtime capacity is temporarily unavailable.";
            default -> safeDetail(result);
        };
        return downstream(assistantStatus, result, detail);
    }

    private AgentActivationDownstreamException technical(String detail, AgentOrchestratorRuntimeAgentResult result) {
        return new AgentActivationDownstreamException(500, result == null ? null : result.httpStatus(),
                safeText(result == null ? null : result.parsedResponseBody(), "code"),
                safeText(result == null ? null : result.parsedResponseBody(), "traceId"), detail);
    }

    private AgentActivationDownstreamException downstream(
            int assistantStatus, AgentOrchestratorRuntimeAgentResult result, String detail) {
        String code = safeText(result.parsedResponseBody(), "code");
        String traceId = safeText(result.parsedResponseBody(), "traceId");
        String safe = sanitize(detail);
        System.out.println("[IIA][AGENT_ACTIVATION] downstream failure mapped downstreamHttpStatus="
                + result.httpStatus() + " assistantHttpStatus=" + assistantStatus
                + " downstreamCode=" + code + " traceId=" + traceId + " stateChangeApplied=false");
        return new AgentActivationDownstreamException(
                assistantStatus, result.httpStatus(), code, traceId, safe);
    }

    private String safeDetail(AgentOrchestratorRuntimeAgentResult result) {
        String detail = safeText(result.parsedResponseBody(), "detail");
        return detail == null ? "Agent Orchestrator rejected activation." : detail;
    }

    private String persistedArtifactHash(java.util.Map<String, Object> payload) {
        if (payload == null) return null;
        Object definition = payload.get("agentDefinition");
        if (!(definition instanceof java.util.Map<?, ?> definitionMap)) return null;
        Object artifact = definitionMap.get("artifact");
        if (!(artifact instanceof java.util.Map<?, ?> artifactMap)) return null;
        Object hash = artifactMap.get("hash");
        return hash instanceof String value ? value : null;
    }

    private String text(JsonNode body, String field) {
        JsonNode value = body.get(field);
        return value != null && value.isTextual() && !value.textValue().isBlank() ? value.textValue() : null;
    }

    private String optionalText(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null || value.isNull()) return null;
        return value.isTextual() && !value.textValue().isBlank() ? value.textValue() : null;
    }

    private Long longValue(JsonNode body, String field) {
        JsonNode value = body.get(field);
        return value != null && value.isIntegralNumber() ? value.longValue() : null;
    }

    private String safeText(JsonNode body, String field) {
        if (body == null || !body.isObject()) return null;
        String value = optionalText(body, field);
        return value == null ? null : sanitize(value);
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) return "Agent Orchestrator activation failed.";
        String sanitized = value.replaceAll("(?i)(bearer|authorization|token|secret|password)\\s*[:=]\\s*\\S+", "$1=[redacted]");
        return sanitized.substring(0, Math.min(500, sanitized.length()));
    }

    public record ValidatedActivationAcceptance(String runtimeStatus, int downstreamHttpStatus) { }
}
