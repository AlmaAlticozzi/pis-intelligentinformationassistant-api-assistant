package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class AgentLifecyclePhase3RegressionTest {

    private static final List<String> LIFECYCLE_RESPONSE_CODES = List.of(
            "400",
            "401",
            "403",
            "404",
            "409",
            "422",
            "500",
            "503");

    @Test
    void openApiContractKeepsPhase3VersionResponsesAndErrorCodes() throws IOException {
        String openApi = Files.readString(resolveOpenApiPath());

        assertThat(openApi).contains("version: 0.0.10");

        String activate = pathSection(openApi, "/v1/agent-definitions/{agentDefinitionId}/activate:");
        String disable = pathSection(openApi, "/v1/agent-definitions/{agentDefinitionId}/disable:");

        assertResponses(activate);
        assertResponses(disable);

        assertThat(activate)
                .contains(
                        "IIA-AGD-ACT-404-001",
                        "IIA-AGD-ACT-409-001",
                        "IIA-AGD-ACT-422-001",
                        "IIA-AGD-ACT-500-001",
                        "IIA-AGD-ACT-503-001");
        assertThat(disable)
                .contains(
                        "IIA-AGD-DIS-404-001",
                        "IIA-AGD-DIS-409-001",
                        "IIA-AGD-DIS-422-001",
                        "IIA-AGD-DIS-500-001",
                        "IIA-AGD-DIS-503-001");
    }

    private static void assertResponses(String operationSection) {
        LIFECYCLE_RESPONSE_CODES.forEach(code -> assertThat(operationSection)
                .as("response %s", code)
                .containsPattern(Pattern.compile("(?m)^\\s{8}'" + code + "':\\s*$")));
    }

    private static String pathSection(String openApi, String path) {
        int start = openApi.indexOf("  " + path);
        assertThat(start).as(path).isNotNegative();
        int nextPath = openApi.indexOf("\n  /v1/", start + path.length());
        return nextPath < 0 ? openApi.substring(start) : openApi.substring(start, nextPath);
    }

    private static Path resolveOpenApiPath() {
        Path modulePath = Path.of("src/main/openapi/assistant-v1.yaml");
        if (Files.exists(modulePath)) {
            return modulePath;
        }
        return Path.of("api/src/main/openapi/assistant-v1.yaml");
    }
}
