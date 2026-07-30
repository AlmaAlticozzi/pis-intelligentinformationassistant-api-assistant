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
        JsonNode fixture = fixture();
        AgentRuntimeSubmission submission = mapper.treeToValue(fixture.path("runtimePackage"), AgentRuntimeSubmission.class);
        RuntimeAgentPackageCanonicalIdentity.Identity actual = identity.identify(submission);
        JsonNode agentDefinition = fixture.path("runtimePackage").path("agentDefinition");

        assertThat(actual.fingerprint()).isEqualTo(fixture.path("expectedFingerprint").asText());
        assertThat(actual.canonicalJson())
                .isEqualTo(new AgentCanonicalJsonService().hash(identity.payload(submission)).canonicalJson());
        assertThat(agentDefinition.at("/dataSourceBindings/0/metadata").isMissingNode()).isTrue();
        assertThat(agentDefinition.at("/dataSourceBindings/1/metadata").isMissingNode()).isTrue();
        assertThat(agentDefinition.at("/dataSourceBindings/0/configuration").isEmpty()).isTrue();
        assertThat(agentDefinition.at("/dataSourceBindings/1/configuration").isEmpty()).isTrue();
        assertThat(agentDefinition.toString()).doesNotContain("subscriptionProfile");
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
    void scheduledEmptyAndAbsentConfigurationAreEquivalentButHistoricalInvalidKeyChangesIdentity() throws Exception {
        JsonNode fixture = fixture();
        com.fasterxml.jackson.databind.node.ObjectNode emptyPackage = fixture.path("runtimePackage").deepCopy();
        RuntimeAgentPackageCanonicalIdentity.Identity empty = identity.identify(
                mapper.treeToValue(emptyPackage, AgentRuntimeSubmission.class));

        com.fasterxml.jackson.databind.node.ObjectNode absentPackage = emptyPackage.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) absentPackage.at("/agentDefinition/dataSourceBindings/1"))
                .remove("configuration");
        RuntimeAgentPackageCanonicalIdentity.Identity absent = identity.identify(
                mapper.treeToValue(absentPackage, AgentRuntimeSubmission.class));

        com.fasterxml.jackson.databind.node.ObjectNode invalidPackage = emptyPackage.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) invalidPackage.at("/agentDefinition/dataSourceBindings/1"))
                .putObject("configuration")
                .put("subscriptionProfile", "SERVICEDATA_STOPPOINTJOURNEYS");
        RuntimeAgentPackageCanonicalIdentity.Identity invalid = identity.identify(
                mapper.treeToValue(invalidPackage, AgentRuntimeSubmission.class));

        assertThat(absent.fingerprint()).isEqualTo(empty.fingerprint());
        assertThat(invalid.fingerprint()).isNotEqualTo(empty.fingerprint());

        java.util.Map<String, Object> firstOrder = new java.util.LinkedHashMap<>();
        firstOrder.put("z", 1);
        firstOrder.put("a", 2);
        java.util.Map<String, Object> secondOrder = new java.util.LinkedHashMap<>();
        secondOrder.put("a", 2);
        secondOrder.put("z", 1);
        assertThat(new AgentCanonicalJsonService().hash(firstOrder).hash())
                .isEqualTo(new AgentCanonicalJsonService().hash(secondOrder).hash());
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

    private JsonNode fixture() throws Exception {
        try (InputStream input = getClass().getResourceAsStream(
                "/contracts/desired-runtime-package-fingerprint-contract-v1.json")) {
            return mapper.readTree(input);
        }
    }
}
