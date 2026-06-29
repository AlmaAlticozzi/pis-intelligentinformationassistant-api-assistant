package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.erdtman.jcs.JsonCanonicalizer;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AgentCanonicalJsonService {

    private static final ObjectMapper CANONICAL_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public AgentCanonicalJsonHashResult hash(Object value) {
        try {
            String logicalJson = CANONICAL_MAPPER.writeValueAsString(value);
            byte[] bytes = new JsonCanonicalizer(logicalJson).getEncodedUTF8();
            String canonicalJson = new String(bytes, StandardCharsets.UTF_8);
            return new AgentCanonicalJsonHashResult(
                    canonicalJson,
                    "sha256:" + hex(MessageDigest.getInstance("SHA-256").digest(bytes)),
                    "SHA-256",
                    bytes.length);
        } catch (NoSuchAlgorithmException | IOException e) {
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
