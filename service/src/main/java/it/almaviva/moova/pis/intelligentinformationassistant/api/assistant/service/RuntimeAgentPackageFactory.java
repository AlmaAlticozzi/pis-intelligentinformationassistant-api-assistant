package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class RuntimeAgentPackageFactory {

    private static final ObjectMapper JSON_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private final AgentRuntimePackageBuilder runtimePackageBuilder;

    public RuntimeAgentPackageFactory() {
        this(new AgentRuntimePackageBuilder());
    }

    @Inject
    public RuntimeAgentPackageFactory(AgentRuntimePackageBuilder runtimePackageBuilder) {
        this.runtimePackageBuilder = runtimePackageBuilder;
    }

    public RuntimeAgentPackageBuild build(
            AgentActivationSnapshot snapshot,
            AgentActivationCommand command,
            AgentRuntimePackageBuildContext context) {
        AgentRuntimePackageBuildResult result = runtimePackageBuilder.build(snapshot, command, context);
        Map<String, Object> runtimePackageJson = JSON_MAPPER.convertValue(result.submission(), LinkedHashMap.class);
        return new RuntimeAgentPackageBuild(
                result.submission(),
                runtimePackageJson,
                stripSha256Prefix(result.canonicalPackageHash()),
                result.canonicalPackageHash(),
                result.canonicalPackageSizeBytes());
    }

    private String stripSha256Prefix(String hash) {
        if (hash == null || !hash.startsWith("sha256:") || hash.length() != 71) {
            throw new AgentRuntimePackageBuildException("SHA-256 package fingerprint is invalid.");
        }
        return hash.substring("sha256:".length()).toLowerCase();
    }
}
