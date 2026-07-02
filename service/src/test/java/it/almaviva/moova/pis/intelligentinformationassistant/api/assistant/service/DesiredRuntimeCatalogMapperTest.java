package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeAgentSubmission;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRow;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesiredRuntimeCatalogMapperTest {
    private static final String FP = "a".repeat(64);
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            JsonMapper.builder().findAndAddModules().build();
    private final DesiredRuntimeCatalogMapper mapper = new DesiredRuntimeCatalogMapper(objectMapper);

    @Test void persistedJsonMapsWithoutRebuilding() {
        Map<String, Object> persistedJson = packageJson("AGDF1", 5, "ACTIVE");
        objectMapper.convertValue(persistedJson, DesiredRuntimeAgentSubmission.class);
        var item = mapper.map(row("AGDF1", 5, FP, persistedJson));
        assertThat(item.getRuntimePackage()).isNotNull();
        assertThat(item.getRuntimePackage().getSubmissionId()).isEqualTo("SUBMISSION-5");
        assertThat(item.getRuntimePackage().getPackageVersion()).isEqualTo(5L);
        assertThat(item.getRuntimePackage().getDesiredStatus()).isEqualTo(DesiredRuntimeAgentSubmission.DesiredStatusEnum.ACTIVE);
        assertThat(item.getRuntimePackage().getAgentDefinition().getId()).isEqualTo("AGDF1");
        assertThat(item.getRuntimePackage().getAgentDefinition().getSource().getControlPlaneComponent())
                .isEqualTo("pis-intelligentinformationassistant-api-assistant");
        assertThat(item.getRuntimePackage().getAgentDefinition().getProfile().getId()).isEqualTo("MEDIUM");
        assertThat(item.getRuntimePackage().getAgentDefinition().getRuntimeContract().getRuntimeExecutionModel().toString())
                .isEqualTo("STANDARD_DSL_EVALUATOR");
        assertThat(item.getRuntimePackage().getAgentDefinition().getArtifact().getDeliveryMode().toString()).isEqualTo("INLINE");
        assertThat(item.getRuntimePackage().getAgentDefinition().getDataSourceBindings()).hasSize(1);
        assertThat(item.getRuntimePackage().getAgentDefinition().getDataSourceBindings().getFirst().getConfiguration())
                .containsEntry("subscriptionProfile", "SERVICEDATA_EVENTS");
    }
    @Test void agentDefinitionMismatchIsRejected() { inconsistent(row("OTHER", 5, FP, packageJson("AGDF1", 5, "ACTIVE"))); }
    @Test void packageVersionMismatchIsRejected() { inconsistent(row("AGDF1", 6, FP, packageJson("AGDF1", 5, "ACTIVE"))); }
    @Test void fingerprintMismatchIsRejected() { inconsistent(row("AGDF1", 5, "b".repeat(64), packageJson("AGDF1", 5, "ACTIVE"))); }
    @Test void nonActiveDesiredStatusIsRejected() {
        Map<String, Object> json = new LinkedHashMap<>(packageJson("AGDF1", 5, "ACTIVE"));
        json.put("desiredStatus", "DISABLED");
        inconsistent(row("AGDF1", 5, FP, json));
    }

    private void inconsistent(DesiredRuntimeCatalogRow row) {
        assertThatThrownBy(() -> mapper.map(row)).isInstanceOf(DesiredRuntimeCatalogConsistencyException.class);
    }
    private DesiredRuntimeCatalogRow row(String packageOwner, long catalogVersion, String catalogFingerprint,
            Map<String, Object> json) {
        return new DesiredRuntimeCatalogRow(10, "RTCH1", "AGDF1", "UPSERT", "ACTIVE", null,
                OffsetDateTime.parse("2026-06-30T10:00:00Z"), "RTPK1", catalogVersion,
                catalogFingerprint, packageOwner, 5, "SUBMISSION-5", FP, json);
    }
    private Map<String, Object> packageJson(String id, long version, String status) {
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("id", id);
        definition.put("name", "Runtime catalog mapper test Agent");
        definition.put("description", "Complete persisted Runtime Agent Package fixture.");
        definition.put("source", Map.of(
                "controlPlaneComponent", "pis-intelligentinformationassistant-api-assistant",
                "alertId", "ALRT1",
                "alertName", "Mapper test Alert",
                "alertVersion", 1,
                "agentCompilationId", "AGCP1",
                "agentCompilationVersion", 1));
        definition.put("profile", Map.ofEntries(
                Map.entry("id", "MEDIUM"), Map.entry("name", "Medium Agent"), Map.entry("enabled", true),
                Map.entry("cpuRequestMillicores", 250), Map.entry("cpuLimitMillicores", 700),
                Map.entry("memoryRequestMiB", 256), Map.entry("memoryLimitMiB", 768),
                Map.entry("networkPolicy", "TOOL_GATEWAY_ONLY"), Map.entry("runtimeClass", "STANDARD_DSL_RUNTIME"),
                Map.entry("maxRuntimeConcurrency", 1), Map.entry("minScheduleIntervalSeconds", 60),
                Map.entry("maxConditionNodes", 100), Map.entry("supportsStatefulExecution", false),
                Map.entry("maxScheduledQueryItems", 1000), Map.entry("maxDataSourceBindings", 4)));
        definition.put("activationPolicy", Map.of(
                "type", "CONTINUOUS", "timezone", "Europe/Rome",
                "validFrom", "2026-06-30T00:00:00Z", "validTo", "2026-12-31T23:59:00Z"));
        definition.put("interpreterType", "EVENT_INTERPRETER");
        definition.put("triggerType", "EVENT");
        definition.put("inputModel", "ServiceDataV2");
        definition.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        definition.put("runtimeContract", Map.ofEntries(
                Map.entry("runtimeImage", "STANDARD_AGENT_DSL_EVALUATOR"),
                Map.entry("sdkVersion", "iia.agent.dsl/v1"), Map.entry("minimumRuntimeVersion", "0.0.2"),
                Map.entry("runtimeExecutionModel", "STANDARD_DSL_EVALUATOR"),
                Map.entry("interpreterType", "EVENT_INTERPRETER"), Map.entry("triggerType", "EVENT"),
                Map.entry("inputModel", "ServiceDataV2"),
                Map.entry("outputModel", "AgentOutput.CANDIDATE_SUGGESTION"),
                Map.entry("evaluationMode", "STATELESS_EVENT_MATCH"),
                Map.entry("requiredOperators", List.of("EQUALS")),
                Map.entry("allowedTools", List.of(Map.of("name", "SERVICE_DATA_EVENT_STREAM", "version", "v2", "required", true))),
                Map.entry("networkPolicy", "TOOL_GATEWAY_ONLY"),
                Map.entry("forbiddenCapabilities", List.of("ARBITRARY_CODE_EXECUTION", "EXTERNAL_HTTP", "DB_QUERY", "FILESYSTEM", "SHELL")),
                Map.entry("compatibility", Map.of("runtimeClass", "STANDARD_DSL_RUNTIME")),
                Map.entry("requiredDataSourceAccessModes", List.of("EVENT_STREAM")),
                Map.entry("requiredConnectorTypes", List.of("KAFKA")),
                Map.entry("allowedConnectorRefs", List.of("servicedata-realtime-v2"))));
        definition.put("artifact", Map.ofEntries(
                Map.entry("artifactType", "DSL"), Map.entry("schemaVersion", "iia.agent.dsl/v1"),
                Map.entry("mediaType", "application/json"), Map.entry("deliveryMode", "INLINE"),
                Map.entry("content", Map.of("schemaVersion", "iia.agent.dsl/v1",
                        "runtime", Map.of("executionModel", "STANDARD_DSL_EVALUATOR"))),
                Map.entry("hashAlgorithm", "SHA-256"), Map.entry("hash", FP),
                Map.entry("canonicalization", "RFC8785_JSON"), Map.entry("signatureStatus", "SIGNED"),
                Map.entry("signature", Map.ofEntries(
                        Map.entry("type", "LOGICAL_MVP"), Map.entry("algorithm", "SHA-256"),
                        Map.entry("keyId", "assistant-logical-signature"), Map.entry("value", FP),
                        Map.entry("signedAt", "2026-06-30T10:00:00Z"),
                        Map.entry("signedBy", "pis-intelligentinformationassistant-api-assistant"))),
                Map.entry("createdAt", "2026-06-30T10:00:00Z"), Map.entry("sizeBytes", 128)));
        definition.put("metadata", Map.of("testFixture", true));
        definition.put("sourceUpdatedAt", "2026-06-30T10:00:00Z");
        definition.put("dataDomain", "SERVICE_DATA");
        definition.put("dataSourceBindings", List.of(Map.ofEntries(
                Map.entry("bindingId", "primaryInput"), Map.entry("dataDomain", "SERVICE_DATA"),
                Map.entry("accessMode", "EVENT_STREAM"), Map.entry("connectorType", "KAFKA"),
                Map.entry("connectorRef", "servicedata-realtime-v2"), Map.entry("inputModel", "ServiceDataV2"),
                Map.entry("inputSchemaVersion", "service-data/v2"),
                Map.entry("bindingSchemaVersion", "iia.runtime.binding/v1"),
                Map.entry("operationRef", "consumeServiceDataV2"),
                Map.entry("configuration", Map.of("subscriptionProfile", "SERVICEDATA_EVENTS")),
                Map.entry("required", true), Map.entry("failoverConnectorRefs", List.of()))));
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("submissionId", "SUBMISSION-5"); value.put("desiredStatus", status);
        value.put("packageVersion", version); value.put("submittedAt", "2026-06-30T10:00:00Z");
        value.put("submittedBy", "TEST"); value.put("startImmediatelyIfAllowed", true);
        value.put("note", "Persisted mapper fixture");
        value.put("agentDefinition", definition);
        return value;
    }
}
