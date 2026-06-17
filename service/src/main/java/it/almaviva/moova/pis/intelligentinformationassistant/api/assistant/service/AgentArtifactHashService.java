package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class AgentArtifactHashService {

    private final AgentCanonicalJsonService canonicalJsonService = new AgentCanonicalJsonService();

    public AgentArtifactHashResult hashDslArtifact(
            String agentDefinitionId,
            String compilationId,
            Map<String, Object> dslArtifact) {
        System.out.println("[IIA][AGENT_ARTIFACT][HASH] start agentDefinitionId="
                + agentDefinitionId + " compilationId=" + compilationId);
        AgentCanonicalJsonHashResult hashResult = canonicalJsonService.hash(dslArtifact);
        System.out.println("[IIA][AGENT_ARTIFACT][HASH] generated agentDefinitionId="
                + agentDefinitionId + " compilationId=" + compilationId + " hash=" + hashResult.hash());
        return new AgentArtifactHashResult(
                hashResult.canonicalJson(),
                hashResult.hash(),
                hashResult.hashAlgorithm(),
                hashResult.sizeBytes());
    }
}
