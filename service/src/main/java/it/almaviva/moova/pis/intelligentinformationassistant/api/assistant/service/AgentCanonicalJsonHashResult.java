package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentCanonicalJsonHashResult(
        String canonicalJson,
        String hash,
        String hashAlgorithm,
        Integer sizeBytes) {
}
