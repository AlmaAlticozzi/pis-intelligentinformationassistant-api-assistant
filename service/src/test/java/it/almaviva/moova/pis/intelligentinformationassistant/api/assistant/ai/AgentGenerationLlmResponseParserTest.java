package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentGenerationLlmResponseParserTest {

    private final AgentGenerationLlmResponseParser parser = new AgentGenerationLlmResponseParser();

    @Test
    void parsesPureJsonBlueprintCandidate() {
        var result = parser.parse("""
                {
                  "canGenerate": true,
                  "recommendedGenerationMode": "DSL",
                  "estimatedComplexity": "LOW",
                  "requiredSources": ["SERVICE_DATA"],
                  "requiredPermissions": ["READ_SERVICE_DATA"],
                  "blueprint": {"agentName": "GeneratedAgent"},
                  "warnings": ["Review preview."]
                }
                """);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().canGenerate()).isTrue();
        assertThat(result.orElseThrow().blueprint()).containsEntry("agentName", "GeneratedAgent");
    }

    @Test
    void rejectsInvalidJsonAndMarkdownFence() {
        assertThat(parser.parse("not-json")).isEmpty();
        assertThat(parser.parse("```json\n{\"blueprint\": {}}\n```")).isEmpty();
    }
}
