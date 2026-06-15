package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@ApplicationScoped
public class AgentArtifactHashService {

    private static final ObjectMapper CANONICAL_MAPPER = JsonMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();

    public AgentArtifactHashResult hashDslArtifact(
            String agentDefinitionId,
            String compilationId,
            Map<String, Object> dslArtifact) {
        System.out.println("[IIA][AGENT_ARTIFACT][HASH] start agentDefinitionId="
                + agentDefinitionId + " compilationId=" + compilationId);
        try {
            String canonicalJson = CANONICAL_MAPPER.writeValueAsString(dslArtifact);
            byte[] bytes = canonicalJson.getBytes(StandardCharsets.UTF_8);
            String artifactHash = "sha256:" + hex(MessageDigest.getInstance("SHA-256").digest(bytes));
            System.out.println("[IIA][AGENT_ARTIFACT][HASH] generated agentDefinitionId="
                    + agentDefinitionId + " compilationId=" + compilationId + " hash=" + artifactHash);
            return new AgentArtifactHashResult(canonicalJson, artifactHash, "SHA-256", bytes.length);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate canonical DSL artifact hash.", e);
        }
    }

    private String hex(byte[] digest) {
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
