package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRuntimePackageCanonicalIdentityTest {
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
        JsonNode agentDefinition = fixture.path("runtimePackage").path("agentDefinition");

        assertThat(actual.fingerprint()).isEqualTo(fixture.path("expectedFingerprint").asText());
        assertThat(actual.canonicalJson())
                .isEqualTo(new AgentCanonicalJsonService().hash(identity.payload(submission)).canonicalJson());
        assertThat(agentDefinition.at("/dataSourceBindings/0/metadata").isMissingNode()).isTrue();
        assertThat(agentDefinition.at("/dataSourceBindings/1/metadata").isMissingNode()).isTrue();
        assertThat(agentDefinition.at("/dataSourceBindings/1/configuration/subscriptionProfile").asText())
                .isEqualTo("SERVICEDATA_EVENTS");
        assertThat(submission.agentDefinition().artifact().hash()).isEqualTo(fixture.path("expectedArtifactHash").asText());
        assertThat(RuntimeAgentPackageCanonicalIdentity.CANONICALIZATION).isEqualTo(fixture.path("canonicalization").asText());
        assertThat(RuntimeAgentPackageCanonicalIdentity.HASH_ALGORITHM).isEqualTo(fixture.path("hashAlgorithm").asText());

        AgentRuntimeSubmission changedTransport = mapper.treeToValue(fixture.path("runtimePackage"), AgentRuntimeSubmission.class);
        JsonNode changedTransportJson = mapper.valueToTree(changedTransport);
        ((com.fasterxml.jackson.databind.node.ObjectNode) changedTransportJson).put("submissionId", "changed-transport-id");
        ((com.fasterxml.jackson.databind.node.ObjectNode) changedTransportJson).put("note", "changed transport note");
        ((com.fasterxml.jackson.databind.node.ObjectNode) changedTransportJson).put("submittedBy", "different-operator");
        RuntimeAgentPackageCanonicalIdentity.Identity afterTransportChange =
                identity.identify(mapper.treeToValue(changedTransportJson, AgentRuntimeSubmission.class));
        assertThat(afterTransportChange.canonicalJson()).isEqualTo(actual.canonicalJson());
        assertThat(afterTransportChange.fingerprint()).isEqualTo(actual.fingerprint());
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
