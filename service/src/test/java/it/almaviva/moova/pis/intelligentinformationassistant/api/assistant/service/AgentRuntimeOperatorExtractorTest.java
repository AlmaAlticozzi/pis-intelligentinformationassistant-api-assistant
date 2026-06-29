package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRuntimeOperatorExtractorTest {

    private final AgentRuntimeOperatorExtractor extractor = new AgentRuntimeOperatorExtractor();

    @Test
    void recursivelyExtractsSupportedOperatorsInDeterministicOrderWithoutDuplicates() {
        Map<String, Object> artifact = Map.of("evaluation", Map.of(
                "condition", Map.of("all", List.of(
                        leaf("CONTAINS"),
                        Map.of("any", List.of(leaf("EQUALS"), leaf("CONTAINS"))),
                        Map.of("not", leaf("EXISTS")),
                        Map.of("anyElement", Map.of("conditions", List.of(leaf("NOT_EMPTY")))),
                        Map.of("allElements", Map.of("conditions", List.of(leaf("IN"))))))));

        assertThat(extractor.extract(artifact))
                .containsExactly("CONTAINS", "EQUALS", "EXISTS", "IN", "NOT_EMPTY");
    }

    @Test
    void rejectsNullBlankAndUnsupportedOperators() {
        assertThatThrownBy(() -> extractor.extract(Map.of("evaluation", Map.of(
                "condition", mutableLeaf(null)))))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("null or blank");
        assertThatThrownBy(() -> extractor.extract(Map.of("evaluation", Map.of(
                "condition", leaf(" ")))))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("null or blank");
        assertThatThrownBy(() -> extractor.extract(Map.of("evaluation", Map.of(
                "condition", leaf("EXECUTE_SQL")))))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("Unsupported");
    }

    private Map<String, Object> leaf(String operator) {
        return Map.of("field", "payload.value", "operator", operator);
    }

    private Map<String, Object> mutableLeaf(String operator) {
        Map<String, Object> leaf = new java.util.LinkedHashMap<>();
        leaf.put("field", "payload.value");
        leaf.put("operator", operator);
        return leaf;
    }
}
