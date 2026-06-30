package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class DesiredRuntimeCatalogFullTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.ofEntries(
                Map.entry("fnd.mode", "wso2"),
                Map.entry("fnd.multi-tenant", "false"),
                Map.entry("fnd.security.profile-retriever", "profile-manager"),
                Map.entry("quarkus.http.test-port", "0"),
                Map.entry("quarkus.langchain4j.openai.api-key", "test-only-not-a-secret"),
                Map.entry("quarkus.oidc.tenant-enabled", "false"),
                Map.entry("quarkus.oidc.auth-server-url", "http://localhost:1/test-oidc"),
                Map.entry("quarkus.oidc.discovery-enabled", "false"),
                Map.entry("quarkus.oidc.client-id", "test-client"),
                Map.entry("quarkus.oidc.credentials.secret", "test-secret"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.client-enabled", "false"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.auth-server-url", "http://localhost:1/test-oidc"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.discovery-enabled", "false"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.token-path", "/oauth2/token"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.client-id", "test-client"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.credentials.secret", "test-secret"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.grant.type", "password"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.grant-options.password.username", "test-user"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.grant-options.password.password", "test-password"),
                Map.entry("quarkus.oidc-client.agent-orchestrator.grant-options.password.profile", "test-profile"),
                Map.entry("iia.agent-orchestrator.client.enabled", "false"),
                Map.entry("iia.agent-orchestrator.client.base-url", "http://localhost:1"),
                Map.entry("iia.agent-orchestrator.client.api-base-path", "/test-agent-orchestrator"),
                Map.entry("iia.agent-orchestrator.client.oidc-enabled", "false"),
                Map.entry("iia.alert-verification.scheduled.enabled", "false"),
                Map.entry("iia.alert.scheduled-verify.enabled", "false"),
                Map.entry("iia.agent-generation-preview.use-llm", "false"));
    }
}
