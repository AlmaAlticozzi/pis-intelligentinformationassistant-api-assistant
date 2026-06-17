package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AgentCanonicalJsonService {

    private static final ObjectMapper CANONICAL_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public AgentCanonicalJsonHashResult hash(Object value) {
        try {
            String canonicalJson = CANONICAL_MAPPER.writeValueAsString(value);
            byte[] bytes = canonicalJson.getBytes(StandardCharsets.UTF_8);
            return new AgentCanonicalJsonHashResult(
                    canonicalJson,
                    "sha256:" + hex(MessageDigest.getInstance("SHA-256").digest(bytes)),
                    "SHA-256",
                    bytes.length);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate canonical JSON SHA-256 hash.", e);
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
