package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentArtifactHashServiceTest {

    private final AgentArtifactHashService service = new AgentArtifactHashService();

    @Test
    void generatesStableHashForSameArtifactWithDifferentKeyOrder() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("schemaVersion", "iia.agent.dsl/v1");
        first.put("artifactType", "DSL");
        first.put("runtime", Map.of(
                "engine", "STANDARD_AGENT_DSL_EVALUATOR",
                "interpreterType", "EVENT_INTERPRETER"));
        first.put("evaluation", Map.of(
                "condition", Map.of(
                        "field", "payload.status",
                        "operator", "EQUALS",
                        "value", "ARRIVING")));

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("evaluation", Map.of(
                "condition", Map.of(
                        "operator", "EQUALS",
                        "value", "ARRIVING",
                        "field", "payload.status")));
        second.put("runtime", Map.of(
                "interpreterType", "EVENT_INTERPRETER",
                "engine", "STANDARD_AGENT_DSL_EVALUATOR"));
        second.put("artifactType", "DSL");
        second.put("schemaVersion", "iia.agent.dsl/v1");

        AgentArtifactHashResult firstHash = service.hashDslArtifact("AGDF1", "AGCP1", first);
        AgentArtifactHashResult secondHash = service.hashDslArtifact("AGDF1", "AGCP1", second);

        assertThat(firstHash.artifactHash()).isEqualTo(secondHash.artifactHash());
        assertThat(firstHash.artifactHash()).startsWith("sha256:");
        assertThat(firstHash.hashAlgorithm()).isEqualTo("SHA-256");
        assertThat(firstHash.artifactSizeBytes()).isPositive();
        assertThat(firstHash.canonicalJson()).doesNotContain("\n");
        assertThat(firstHash.canonicalJson().indexOf("\"artifactType\""))
                .isLessThan(firstHash.canonicalJson().indexOf("\"evaluation\""));
    }

    @Test
    void generatesDifferentHashForDifferentArtifact() {
        Map<String, Object> base = Map.of(
                "schemaVersion", "iia.agent.dsl/v1",
                "artifactType", "DSL",
                "allowedTools", List.of());
        Map<String, Object> changed = Map.of(
                "schemaVersion", "iia.agent.dsl/v1",
                "artifactType", "DSL",
                "allowedTools", List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));

        AgentArtifactHashResult baseHash = service.hashDslArtifact("AGDF1", "AGCP1", base);
        AgentArtifactHashResult changedHash = service.hashDslArtifact("AGDF1", "AGCP1", changed);

        assertThat(baseHash.artifactHash()).isNotEqualTo(changedHash.artifactHash());
    }
}
