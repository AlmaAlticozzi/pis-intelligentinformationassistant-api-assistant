package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AgentGenerationCapabilityCatalogTest {

    private final AgentGenerationCapabilityCatalog catalog = new AgentGenerationCapabilityCatalog();

    @Test
    void exposesSupportedMvpCapabilitiesAndForbiddenOperations() {
        assertThat(catalog.isSupportedSource("SERVICE_DATA")).isTrue();
        assertThat(catalog.isSupportedSource("UNKNOWN_SOURCE")).isFalse();
        assertThat(catalog.permissionForSource("SERVICE_DATA")).isEqualTo("READ_SERVICE_DATA");
        assertThat(catalog.isSupportedDslOperator("CONTAINS")).isTrue();
        assertThat(catalog.isSupportedDslOperator("EQUALS_NORMALIZED")).isTrue();
        assertThat(catalog.isSupportedDslOperator("RANDOM_OPERATOR")).isFalse();
        assertThat(catalog.isPreviewOnlyGenerationMode("JAVA_TEMPLATE")).isTrue();
        assertThat(catalog.isForbiddenCapability("EXTERNAL_HTTP")).isTrue();
    }

    @Test
    void acceptsStatelessServiceDataDslRuntimeProfile() {
        AgentGenerationCapabilitySnapshot snapshot = new AgentGenerationCapabilitySnapshot(
                List.of("SERVICE_DATA"),
                List.of("READ_SERVICE_DATA"),
                "EVENT",
                "STATELESS_EVENT_MATCH",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                List.of("SERVICE_DATA_JOURNEY"),
                Set.of("CONTAINS", "EQUALS_NORMALIZED"),
                true,
                "AUTO",
                "DSL");

        AgentGenerationCapabilityCatalog.RuntimeSupportEvaluation result =
                catalog.evaluateRuntimeSupport(snapshot);

        assertThat(result.supported()).isTrue();
        assertThat(result.unsupportedCapabilities()).isEmpty();
    }

    @Test
    void reportsUnsupportedOperatorWithoutBlockingPreviewContract() {
        AgentGenerationCapabilitySnapshot snapshot = new AgentGenerationCapabilitySnapshot(
                List.of("SERVICE_DATA"),
                List.of("READ_SERVICE_DATA"),
                "EVENT",
                "STATELESS_EVENT_MATCH",
                "ServiceDataV2",
                "CANDIDATE_SUGGESTION",
                List.of("SERVICE_DATA_JOURNEY"),
                Set.of("RANDOM_OPERATOR"),
                true,
                "DSL",
                "DSL");

        AgentGenerationCapabilityCatalog.RuntimeSupportEvaluation result =
                catalog.evaluateRuntimeSupport(snapshot);

        assertThat(result.supported()).isFalse();
        assertThat(result.unsupportedCapabilities()).containsExactly("RANDOM_OPERATOR");
    }
}
