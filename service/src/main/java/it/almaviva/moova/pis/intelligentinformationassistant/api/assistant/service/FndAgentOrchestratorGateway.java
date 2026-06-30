package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.fnd.core.lib.quarkuscommon.http.rest.FNDRestClient;
import it.almaviva.fnd.core.lib.quarkuscommon.http.rest.util.FNDRequestForResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class FndAgentOrchestratorGateway {

    private static final String RESOURCE_PATH = "/v1/runtime-agent-definitions/{agentDefinitionId}";

    private final FNDRestClient restClient;
    private final ObjectMapper objectMapper;
    private final PersistedRuntimePackageReader persistedRuntimePackageReader;
    private final String baseUrl;
    private final String apiBasePath;
    private final boolean oidcEnabled;
    private final String oidcClientId;

    @Inject
    public FndAgentOrchestratorGateway(
            FNDRestClient restClient,
            ObjectMapper objectMapper,
            PersistedRuntimePackageReader persistedRuntimePackageReader,
            @ConfigProperty(name = "iia.agent-orchestrator.client.base-url", defaultValue = "") String baseUrl,
            @ConfigProperty(name = "iia.agent-orchestrator.client.api-base-path", defaultValue = "") String apiBasePath,
            @ConfigProperty(name = "iia.agent-orchestrator.client.oidc-enabled", defaultValue = "true") boolean oidcEnabled,
            @ConfigProperty(name = "iia.agent-orchestrator.client.oidc-client-id", defaultValue = "agent-orchestrator") String oidcClientId) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.persistedRuntimePackageReader = persistedRuntimePackageReader;
        this.baseUrl = baseUrl;
        this.apiBasePath = apiBasePath == null ? "" : apiBasePath.trim();
        this.oidcEnabled = oidcEnabled;
        this.oidcClientId = oidcClientId;
    }

    FndAgentOrchestratorGateway(
            FNDRestClient restClient,
            ObjectMapper objectMapper,
            PersistedRuntimePackageReader persistedRuntimePackageReader,
            String baseUrl,
            String apiBasePath,
            String oidcClientId) {
        this(restClient, objectMapper, persistedRuntimePackageReader, baseUrl, apiBasePath, true, oidcClientId);
    }

    public AgentOrchestratorRuntimeAgentResult activate(AgentOrchestratorActivationRequest request) {
        String validatedBaseUrl = requireBaseUrl(baseUrl);
        request = withPersistedPayload(request);
        validateIdentity(request);
        String url = joinUrl(validatedBaseUrl, apiBasePath, RESOURCE_PATH);
        FNDRequestForResponse.Builder requestBuilder = FNDRequestForResponse.builder()
                .put()
                .url(url)
                .addPathParam("agentDefinitionId", request.agentDefinitionId())
                .body(request.persistedPayload());
        if (oidcEnabled) {
            requestBuilder.oidcClientId(oidcClientId);
        }
        FNDRequestForResponse foundationRequest = requestBuilder.build();
        try {
            Response response = restClient.requestForResponse(foundationRequest);
            if (response == null) {
                return outcome(request, 0, false, null, null, "CONNECTION_FAILURE");
            }
            try (response) {
                int status = response.getStatus();
                String rawBody;
                try {
                    rawBody = response.hasEntity() ? response.readEntity(String.class) : "";
                } catch (RuntimeException ex) {
                    return outcome(request, status, true, null, null, "RESPONSE_DECODING_FAILURE");
                }
                JsonNode parsedBody = parseJson(rawBody);
                AgentOrchestratorRuntimeAgentResult result = outcome(
                        request, status, true, rawBody, parsedBody, category(status));
                log(request, result);
                return result;
            }
        } catch (RuntimeException ex) {
            String category = hasCause(ex, SocketTimeoutException.class) || hasCause(ex, TimeoutException.class)
                    ? "TIMEOUT" : "CONNECTION_FAILURE";
            AgentOrchestratorRuntimeAgentResult result = outcome(request, 0, false, null, null, category);
            log(request, result);
            return result;
        }
    }

    public AgentOrchestratorRuntimeAgentResult disable(AgentOrchestratorDisableRequest request) {
        String url = joinUrl(requireBaseUrl(baseUrl), apiBasePath, RESOURCE_PATH + "/disable");
        Map<String, Object> body = new LinkedHashMap<>();
        if (request.reason() != null) body.put("reason", request.reason());
        body.put("stopRunningAgents", request.stopRunningAgents());
        body.put("gracePeriodSeconds", request.gracePeriodSeconds());
        body.put("requestedAt", request.requestedAt().toString());
        FNDRequestForResponse.Builder builder = FNDRequestForResponse.builder().post().url(url)
                .addPathParam("agentDefinitionId", request.agentDefinitionId()).body(body);
        if (oidcEnabled) builder.oidcClientId(oidcClientId);
        try {
            Response response = restClient.requestForResponse(builder.build());
            if (response == null) return disableOutcome(request, 0, false, null, null, "CONNECTION_FAILURE");
            try (response) {
                int status = response.getStatus();
                String raw;
                try { raw = response.hasEntity() ? response.readEntity(String.class) : ""; }
                catch (RuntimeException ex) { return disableOutcome(request, status, true, null, null, "RESPONSE_DECODING_FAILURE"); }
                AgentOrchestratorRuntimeAgentResult result = disableOutcome(request, status, true, raw, parseJson(raw), category(status));
                logDisable(request, result);
                return result;
            }
        } catch (RuntimeException ex) {
            String category = hasCause(ex, SocketTimeoutException.class) || hasCause(ex, TimeoutException.class)
                    ? "TIMEOUT" : "CONNECTION_FAILURE";
            AgentOrchestratorRuntimeAgentResult result = disableOutcome(request, 0, false, null, null, category);
            logDisable(request, result);
            return result;
        }
    }

    private AgentOrchestratorRuntimeAgentResult disableOutcome(AgentOrchestratorDisableRequest request, int status,
            boolean received, String raw, JsonNode parsed, String category) {
        return new AgentOrchestratorRuntimeAgentResult(request.agentDefinitionId(), "DISABLED", null, 0, null,
                null, null, null, null, status, received, raw, parsed, category);
    }

    private void logDisable(AgentOrchestratorDisableRequest request, AgentOrchestratorRuntimeAgentResult result) {
        JsonNode body = result.parsedResponseBody();
        System.out.println("[IIA][AGENT_ORCHESTRATOR][DISABLE_HTTP] agentDefinitionId=" + request.agentDefinitionId()
                + " stopRunningAgents=" + request.stopRunningAgents() + " gracePeriodSeconds=" + request.gracePeriodSeconds()
                + " method=POST oidcEnabled=" + oidcEnabled + " httpCallExecuted=true httpStatus=" + result.httpStatus()
                + " responseReceived=" + result.responseReceived() + " desiredStatus=" + safeField(body, "desiredStatus")
                + " runtimeStatus=" + safeField(body, "runtimeStatus") + " outcome=" + result.outcomeCategory());
    }

    private String safeField(JsonNode body, String name) {
        return body != null && body.path(name).isTextual() ? body.path(name).textValue() : null;
    }

    private AgentOrchestratorActivationRequest withPersistedPayload(AgentOrchestratorActivationRequest request) {
        if (request.persistedPayload() != null) return request;
        if (request.runtimePackageId() == null) {
            throw new AgentActivationTechnicalException("Persisted runtime package identity is missing.");
        }
        PersistedRuntimePackageSnapshot snapshot = persistedRuntimePackageReader.read(
                request.runtimePackageId(), request.agentDefinitionId());
        if (!request.submission().submissionId().equals(snapshot.submissionId())
                || request.submission().packageVersion() != snapshot.packageVersion()) {
            throw new AgentActivationTechnicalException("Runtime package reservation identity is inconsistent.");
        }
        return new AgentOrchestratorActivationRequest(
                request.agentDefinitionId(), request.submission(), request.canonicalPackageHash(),
                snapshot.runtimePackageId(), snapshot.payload());
    }

    private void validateIdentity(AgentOrchestratorActivationRequest request) {
        if (request == null || request.agentDefinitionId() == null || request.agentDefinitionId().isBlank()) {
            throw new AgentActivationTechnicalException("Activation path Agent Definition identity is missing.");
        }
        Map<String, Object> payload = request.persistedPayload();
        if (payload == null) {
            throw new AgentActivationTechnicalException("Persisted runtime package JSON is missing.");
        }
        Object definition = payload.get("agentDefinition");
        Object bodyId = definition instanceof Map<?, ?> map ? map.get("id") : null;
        if (!request.agentDefinitionId().equals(bodyId)) {
            throw new AgentActivationTechnicalException("Persisted runtime package Agent Definition identity is inconsistent.");
        }
        Object submissionId = payload.get("submissionId");
        if (!request.submission().submissionId().equals(submissionId)) {
            throw new AgentActivationTechnicalException("Persisted runtime package submission identity is inconsistent.");
        }
        Object version = payload.get("packageVersion");
        if (!(version instanceof Number number) || number.longValue() != request.submission().packageVersion()) {
            throw new AgentActivationTechnicalException("Persisted runtime package version is inconsistent.");
        }
    }

    private AgentOrchestratorRuntimeAgentResult outcome(
            AgentOrchestratorActivationRequest request, int status, boolean received,
            String rawBody, JsonNode parsedBody, String category) {
        return new AgentOrchestratorRuntimeAgentResult(
                request.agentDefinitionId(), request.submission().desiredStatus(), null,
                request.submission().packageVersion(), request.submission().submissionId(), null, null,
                null, null, status, received, rawBody, parsedBody, category);
    }

    private JsonNode parseJson(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            return objectMapper.readTree(body);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String category(int status) {
        if (status >= 200 && status < 300) return "ACCEPTED";
        return switch (status) {
            case 400, 401, 403, 404, 409, 422, 429, 500, 502, 503, 504 -> "HTTP_" + status;
            default -> "HTTP_OTHER";
        };
    }

    private void log(AgentOrchestratorActivationRequest request, AgentOrchestratorRuntimeAgentResult result) {
        System.out.println("[IIA][AGENT_ORCHESTRATOR][ACTIVATE_HTTP] agentDefinitionId=" + request.agentDefinitionId()
                + " runtimePackageId=" + request.runtimePackageId()
                + " submissionId=" + request.submission().submissionId()
                + " packageVersion=" + request.submission().packageVersion()
                + " method=PUT oidcEnabled=" + oidcEnabled
                + " httpCallExecuted=true httpStatus=" + result.httpStatus()
                + " responseReceived=" + result.responseReceived()
                + " outcome=" + result.outcomeCategory());
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (type.isInstance(current)) return true;
        }
        return false;
    }

    static String joinUrl(String baseUrl, String apiBasePath, String resourcePath) {
        return stripTrailingSlash(baseUrl) + normalizedPath(apiBasePath) + normalizedPath(resourcePath);
    }

    private static String requireBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new AgentActivationTechnicalException("Agent Orchestrator base URL is required when its client is enabled.");
        }
        return value.trim();
    }

    private static String stripTrailingSlash(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') end--;
        return value.substring(0, end);
    }

    private static String normalizedPath(String value) {
        if (value == null || value.isBlank() || "/".equals(value.trim())) return "";
        String path = value.trim();
        int start = 0;
        while (start < path.length() && path.charAt(start) == '/') start++;
        int end = path.length();
        while (end > start && path.charAt(end - 1) == '/') end--;
        return "/" + path.substring(start, end);
    }
}
