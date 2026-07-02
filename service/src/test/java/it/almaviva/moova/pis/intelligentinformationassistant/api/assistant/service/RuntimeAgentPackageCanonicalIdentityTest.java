package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuntimeAgentPackageCanonicalIdentityTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final RuntimeAgentPackageCanonicalIdentity identity = new RuntimeAgentPackageCanonicalIdentity();

    @Test
    void contractFixtureHasHardCodedCanonicalFingerprintAndArtifactHash() throws Exception {
        JsonNode fixture;
        try (InputStream input = getClass().getResourceAsStream("/contracts/desired-runtime-package-fingerprint-contract-v1.json")) {
            fixture = mapper.readTree(input);
        }
        AgentRuntimeSubmission submission = mapper.treeToValue(fixture.path("runtimePackage"), AgentRuntimeSubmission.class);
        RuntimeAgentPackageCanonicalIdentity.Identity actual = identity.identify(submission);

        assertThat(actual.fingerprint()).isEqualTo(fixture.path("expectedFingerprint").asText());
        assertThat(submission.agentDefinition().artifact().hash()).isEqualTo(fixture.path("expectedArtifactHash").asText());
        assertThat(RuntimeAgentPackageCanonicalIdentity.CANONICALIZATION).isEqualTo(fixture.path("canonicalization").asText());
        assertThat(RuntimeAgentPackageCanonicalIdentity.HASH_ALGORITHM).isEqualTo(fixture.path("hashAlgorithm").asText());
    }

    @Test
    void semanticHashAcceptsSupportedFormsAndRejectsInvalidOrUnsupportedValues() {
        String digest = "a".repeat(64);
        assertThat(SemanticSha256Hash.equal("sha256:" + digest.toUpperCase(), digest)).isTrue();
        assertThatThrownBy(() -> SemanticSha256Hash.digest("sha512:" + digest))
                .isInstanceOf(AgentRuntimePackageBuildException.class);
        assertThatThrownBy(() -> SemanticSha256Hash.digest(" "))
                .isInstanceOf(AgentRuntimePackageBuildException.class);
    }
}
