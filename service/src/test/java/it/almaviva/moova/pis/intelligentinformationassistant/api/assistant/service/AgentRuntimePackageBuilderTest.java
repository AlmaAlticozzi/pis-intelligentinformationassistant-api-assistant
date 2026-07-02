package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRuntimePackageBuilderTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final Instant SUBMITTED_AT = Instant.parse("2026-06-17T10:00:00Z");
    private static final String TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";

    private final AgentRuntimePackageBuilder builder = new AgentRuntimePackageBuilder();

    @Test
    void buildsCompleteEventPackageWithKafkaBindingInlineArtifactProfilePolicyAndContract() throws Exception {
        AgentRuntimePackageBuildResult result = builder.build(
                fixture().build(),
                new AgentActivationCommand("AGDF1", " start ", true),
                context(1));

        AgentRuntimeSubmission submission = result.submission();
        assertThat(submission.submissionId()).startsWith("ACTIVATE:AGDF1:");
        assertThat(submission.desiredStatus()).isEqualTo("ACTIVE");
        assertThat(submission.startImmediatelyIfAllowed()).isTrue();
        assertThat(submission.note()).isEqualTo("start");
        assertThat(submission.agentDefinition().source().controlPlaneComponent())
                .isEqualTo("pis-intelligentinformationassistant-api-assistant");
        assertThat(submission.agentDefinition().source().agentCompilationId()).isEqualTo("AGCP1");
        assertThat(submission.agentDefinition().profile().runtimeClass()).isEqualTo("STANDARD_DSL_RUNTIME");
        assertThat(submission.agentDefinition().profile().networkPolicy()).isEqualTo("REGISTERED_DATA_SOURCES_ONLY");
        assertThat(submission.agentDefinition().activationPolicy().type()).isEqualTo("CONTINUOUS");
        assertThat(submission.agentDefinition().runtimeContract().runtimeExecutionModel())
                .isEqualTo("STANDARD_DSL_EVALUATOR");
        assertThat(submission.agentDefinition().artifact().content())
                .extractingByKey("runtime")
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                .containsEntry("executionModel", "STANDARD_DSL_EVALUATOR");
        assertThat(submission.agentDefinition().runtimeContract().requiredOperators()).containsExactly("CONTAINS");
        assertThat(submission.agentDefinition().runtimeContract().minimumRuntimeVersion()).isEqualTo("0.0.2");
        assertThat(submission.agentDefinition().runtimeContract().sdkVersion()).isEqualTo("1.0.0");
        assertThat(submission.agentDefinition().runtimeContract().networkPolicy()).isEqualTo("REGISTERED_DATA_SOURCES_ONLY");
        assertThat(submission.agentDefinition().runtimeContract().compatibility())
                .containsEntry("canonicalization", "RFC8785_JSON");
        assertThat(submission.agentDefinition().dataSourceBindings()).singleElement()
                .satisfies(binding -> {
                    assertThat(binding.accessMode()).isEqualTo("EVENT_STREAM");
                    assertThat(binding.connectorType()).isEqualTo("KAFKA");
                    assertThat(binding.connectorRef()).isEqualTo("servicedata-realtime-v2");
                    assertThat(binding.operationRef()).isNull();
                    assertThat(binding.configuration()).containsEntry("subscriptionProfile", "SERVICEDATA_EVENTS");
                    assertThat(binding.failoverConnectorRefs()).isEmpty();
                });
        assertThat(submission.agentDefinition().artifact().deliveryMode()).isEqualTo("INLINE");
        assertThat(submission.agentDefinition().artifact().mediaType()).isEqualTo("application/json");
        assertThat(submission.agentDefinition().artifact().signature().type()).isEqualTo("LOGICAL_MVP");
        assertThat(submission.agentDefinition().artifact().schemaVersion()).isEqualTo("iia.agent.dsl/v1");
        assertThat(submission.agentDefinition().artifact().content()).containsEntry("schemaVersion", "iia.agent.dsl/v1");

        JsonNode json = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(submission));
        assertThat(json.get("agentDefinition").get("activationPolicy").get("type").asText()).isEqualTo("CONTINUOUS");
        assertThat(json.toString()).doesNotContain("agentCompilationVersion", "operationRef", "technicalSpecification", "prompt");
    }

    @Test
    void buildsCompleteScheduledPackageWithHttpBindingDailyWindowAndOperationRef() throws Exception {
        AgentRuntimePackageBuildResult result = builder.build(
                fixture().scheduled().dailyWindow().build(),
                new AgentActivationCommand("AGDF1", null, false),
                context(1));

        AgentRuntimeSubmission submission = result.submission();
        assertThat(submission.startImmediatelyIfAllowed()).isFalse();
        assertThat(submission.note()).isNull();
        assertThat(submission.agentDefinition().interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(submission.agentDefinition().activationPolicy().daysOfWeek()).containsExactly("MONDAY", "WEDNESDAY");
        assertThat(submission.agentDefinition().runtimeContract().runtimeExecutionModel())
                .isEqualTo("STANDARD_DSL_EVALUATOR");
        assertThat(submission.agentDefinition().artifact().content())
                .extractingByKey("runtime")
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                .containsEntry("executionModel", "STANDARD_DSL_EVALUATOR");
        assertThat(submission.agentDefinition().runtimeContract().requiredOperators()).containsExactly("EXISTS");
        assertThat(submission.agentDefinition().profile().networkPolicy()).isEqualTo("REGISTERED_DATA_SOURCES_ONLY");
        assertThat(submission.agentDefinition().runtimeContract().networkPolicy()).isEqualTo("REGISTERED_DATA_SOURCES_ONLY");
        assertThat(submission.agentDefinition().runtimeContract().minimumRuntimeVersion()).isEqualTo("0.0.2");
        assertThat(submission.agentDefinition().runtimeContract().sdkVersion()).isEqualTo("1.0.0");
        assertThat(submission.agentDefinition().runtimeContract().allowedTools()).extracting(
                AgentRuntimeSubmission.RuntimeToolReference::name).containsExactly(TOOL);
        assertThat(submission.agentDefinition().dataSourceBindings()).singleElement()
                .satisfies(binding -> {
                    assertThat(binding.accessMode()).isEqualTo("SCHEDULED_QUERY");
                    assertThat(binding.connectorType()).isEqualTo("HTTP_REST");
                    assertThat(binding.connectorRef()).isEqualTo("servicedata-stoppointjourneys-v2");
                    assertThat(binding.operationRef()).isEqualTo("searchStopPointJourneysV2");
                    assertThat(binding.configuration()).containsEntry("subscriptionProfile", "SERVICEDATA_STOPPOINTJOURNEYS");
                });

        JsonNode json = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(submission));
        assertThat(json.get("agentDefinition").get("activationPolicy").get("type").asText()).isEqualTo("DAILY_WINDOW");
        assertThat(json.get("agentDefinition").get("activationPolicy").has("validFrom")).isFalse();
    }

    @Test
    void packageHashAndSubmissionIdAreDeterministicAndIgnoreTransportFields() {
        AgentActivationSnapshot snapshot = fixture().build();
        AgentRuntimePackageBuildResult first = builder.build(snapshot, new AgentActivationCommand("AGDF1", "a", true), context(1));
        AgentRuntimePackageBuildResult second = builder.build(snapshot, new AgentActivationCommand("AGDF1", "b", true),
                new AgentRuntimePackageBuildContext(1, Instant.parse("2026-06-18T10:00:00Z"), "operator2"));

        assertThat(first.canonicalPackageHash()).isEqualTo(second.canonicalPackageHash());
        assertThat(first.submission().submissionId()).isEqualTo(second.submission().submissionId());
        assertThat(first.submission().submissionId()).hasSizeLessThanOrEqualTo(100);
        assertThat(builder.build(snapshot, new AgentActivationCommand("AGDF1", "a", true), context(2)).submission().submissionId())
                .isEqualTo(first.submission().submissionId());
    }

    @Test
    void governedSubscriptionProfileIsTransmittedWithoutBindingMetadataAndChangesPackageIdentity() throws Exception {
        AgentRuntimePackageBuildResult baseline = fixture().buildWithLocalBuilder();
        AgentRuntimePackageBuildResult changed = fixture().subscriptionProfile("SERVICEDATA_EVENTS_V2").buildWithLocalBuilder();

        assertThat(baseline.submission().agentDefinition().dataSourceBindings()).singleElement()
                .satisfies(binding -> {
                    assertThat(binding.configuration())
                            .containsEntry("subscriptionProfile", "SERVICEDATA_EVENTS");
                    assertThat(binding.getClass().getRecordComponents())
                            .extracting(java.lang.reflect.RecordComponent::getName)
                            .doesNotContain("metadata");
                });

        JsonNode agentDefinition = OBJECT_MAPPER.readTree(baseline.canonicalPackageJson()).path("agentDefinition");
        assertThat(agentDefinition.at("/dataSourceBindings/0/metadata").isMissingNode()).isTrue();
        assertThat(agentDefinition.at("/dataSourceBindings/0/configuration/subscriptionProfile").asText())
                .isEqualTo("SERVICEDATA_EVENTS");
        assertThat(agentDefinition.at("/metadata/requiredPermissions").isArray()).isTrue();
        assertThat(agentDefinition.at("/metadata/requiredPermissions").isEmpty()).isFalse();
        assertThat(changed.canonicalPackageHash()).isNotEqualTo(baseline.canonicalPackageHash());
    }

    @Test
    void packageHashChangesForRuntimeRelevantChanges() {
        String base = builder.build(fixture().build(), new AgentActivationCommand("AGDF1", null, true), context(1))
                .canonicalPackageHash();

        assertThat(builder.build(fixture().profileCpu(300).build(), new AgentActivationCommand("AGDF1", null, true), context(1))
                .canonicalPackageHash()).isNotEqualTo(base);
        assertThat(builder.build(fixture().continuousTo("2026-08-01T10:00:00Z").build(), new AgentActivationCommand("AGDF1", null, true), context(1))
                .canonicalPackageHash()).isNotEqualTo(base);
        assertThat(fixture().connectorRef("different-logical-connector").buildWithLocalBuilder()
                .canonicalPackageHash()).isNotEqualTo(base);
        assertThat(builder.build(fixture().dslExtra("runtimeRelevant", true).build(), new AgentActivationCommand("AGDF1", null, true), context(1))
                .canonicalPackageHash()).isNotEqualTo(base);
    }

    @Test
    void correctedRuntimeContractArtifactAndCanonicalizationChangeLegacyFingerprint() throws Exception {
        AgentRuntimePackageBuildResult corrected = builder.build(
                fixture().build(), new AgentActivationCommand("AGDF1", null, true), context(1));
        JsonNode legacyPayload = OBJECT_MAPPER.readTree(corrected.canonicalPackageJson());
        com.fasterxml.jackson.databind.node.ObjectNode definition =
                (com.fasterxml.jackson.databind.node.ObjectNode) legacyPayload.path("agentDefinition");
        com.fasterxml.jackson.databind.node.ObjectNode contract =
                (com.fasterxml.jackson.databind.node.ObjectNode) definition.path("runtimeContract");
        contract.put("minimumRuntimeVersion", "1.0.0");
        contract.put("sdkVersion", "iia.agent.dsl/v1");
        contract.put("networkPolicy", "TOOL_GATEWAY_ONLY");
        ((com.fasterxml.jackson.databind.node.ObjectNode) definition.path("profile"))
                .put("networkPolicy", "TOOL_GATEWAY_ONLY");
        contract.put("runtimeExecutionModel", "KAFKA_EVENT");
        contract.putArray("requiredOperators");
        ((com.fasterxml.jackson.databind.node.ObjectNode) contract.path("compatibility"))
                .put("canonicalization", "JACKSON_SORT_PROPERTIES_AND_MAP_ENTRIES");
        com.fasterxml.jackson.databind.node.ObjectNode artifact =
                (com.fasterxml.jackson.databind.node.ObjectNode) definition.path("artifact");
        artifact.put("canonicalization", "JACKSON_SORT_PROPERTIES_AND_MAP_ENTRIES");
        ((com.fasterxml.jackson.databind.node.ObjectNode) artifact.path("content").path("runtime"))
                .put("executionModel", "KAFKA_EVENT");

        assertThat(new AgentCanonicalJsonService().hash(legacyPayload).hash())
                .isNotEqualTo(corrected.canonicalPackageHash());
        assertThat(corrected.submission().agentDefinition().runtimeContract().runtimeExecutionModel())
                .isEqualTo(corrected.submission().agentDefinition().artifact().content()
                        .get("runtime") instanceof Map<?, ?> runtime ? runtime.get("executionModel") : null);
        assertThat(corrected.submission().agentDefinition().artifact().signatureStatus()).isEqualTo("SIGNED");
    }

    @Test
    void configuredRuntimeCapabilitiesOverrideSnapshotProjectionConsistently() {
        AgentRuntimePackageBuildResult result = fixture()
                .runtimeCapabilities("2.3.4", "5.6.7", "REGISTERED_DATA_SOURCES_ONLY")
                .buildWithLocalBuilder();

        assertThat(result.submission().agentDefinition().runtimeContract().minimumRuntimeVersion()).isEqualTo("2.3.4");
        assertThat(result.submission().agentDefinition().runtimeContract().sdkVersion()).isEqualTo("5.6.7");
        assertThat(result.submission().agentDefinition().profile().networkPolicy())
                .isEqualTo("REGISTERED_DATA_SOURCES_ONLY");
        assertThat(result.submission().agentDefinition().runtimeContract().networkPolicy())
                .isEqualTo(result.submission().agentDefinition().profile().networkPolicy());
        assertThat(result.submission().agentDefinition().artifact().schemaVersion()).isEqualTo("iia.agent.dsl/v1");
    }

    @Test
    void invalidRuntimeCapabilityConfigurationIsRejected() {
        assertInvalidCapabilities(" ", "1.0.0", "REGISTERED_DATA_SOURCES_ONLY", "minimumRuntimeVersion");
        assertInvalidCapabilities("0.2", "1.0.0", "REGISTERED_DATA_SOURCES_ONLY", "MAJOR.MINOR.PATCH");
        assertInvalidCapabilities("0.x.2", "1.0.0", "REGISTERED_DATA_SOURCES_ONLY", "MAJOR.MINOR.PATCH");
        assertInvalidCapabilities("0.0.2", " ", "REGISTERED_DATA_SOURCES_ONLY", "sdkVersion");
        assertInvalidCapabilities("0.0.2", "1.0.0", " ", "networkPolicy");
        assertInvalidCapabilities("0.0.2", "1.0.0", "TOOL_GATEWAY_ONLY", "Unsupported runtime networkPolicy");
    }

    private void assertInvalidCapabilities(String minimum, String sdk, String policy, String message) {
        AgentRuntimePackageConfiguration defaults = AgentRuntimePackageConfiguration.defaults();
        assertThatThrownBy(() -> new AgentRuntimePackageConfiguration(
                defaults.controlPlaneComponent(), defaults.defaultRuntimeClass(), defaults.artifactCanonicalization(),
                defaults.artifactMediaType(), defaults.bindingSchemaVersion(), defaults.eventServiceDataConnector(),
                defaults.scheduledServiceDataConnector(), minimum, sdk, policy))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining(message);
    }

    @Test
    void artifactMapsHashSchemaVersionSizeSignatureAndDoesNotUseDslPreview() throws Exception {
        AgentRuntimePackageBuildResult result = builder.build(
                fixture().build(),
                new AgentActivationCommand("AGDF1", null, true),
                context(1));

        AgentRuntimeSubmission.AgentRuntimeArtifact artifact = result.submission().agentDefinition().artifact();
        assertThat(artifact.hash()).doesNotStartWith("sha256:");
        assertThat(artifact.hashAlgorithm()).isEqualTo("SHA-256");
        assertThat(artifact.schemaVersion()).isEqualTo("iia.agent.dsl/v1");
        assertThat(artifact.sizeBytes()).isPositive();
        assertThat(artifact.canonicalization()).isEqualTo("RFC8785_JSON");
        assertThat(artifact.signatureStatus()).isEqualTo("SIGNED");
        assertThat(artifact.signature().algorithm()).isEqualTo("SHA256_WITH_CONTROL_PLANE_ATTESTATION");
        assertThat(artifact.content()).containsEntry("agentDefinitionId", "AGDF1");
        assertThat(artifact.content()).doesNotContainKey("dslPreviewOnly");

        JsonNode json = OBJECT_MAPPER.valueToTree(result.submission());
        JsonNode artifactJson = json.path("agentDefinition").path("artifact");
        assertThat(artifactJson.path("signatureStatus").asText()).isEqualTo("SIGNED");
        assertThat(artifactJson.path("signature").has("signatureStatus")).isFalse();
        assertThat(artifactJson.path("signature")).isEqualTo(OBJECT_MAPPER.valueToTree(artifact.signature()));

        JsonNode legacyPayload = OBJECT_MAPPER.readTree(result.canonicalPackageJson());
        ((com.fasterxml.jackson.databind.node.ObjectNode) legacyPayload.path("agentDefinition").path("artifact"))
                .remove("signatureStatus");
        assertThat(new AgentCanonicalJsonService().hash(legacyPayload).hash())
                .isNotEqualTo(result.canonicalPackageHash());
    }

    @Test
    void artifactSignatureStatusMustComeFromAuthoritativeSnapshotAndCannotDefault() {
        assertThatThrownBy(() -> builder.build(
                fixture().signatureStatus(null).build(),
                new AgentActivationCommand("AGDF1", null, true),
                context(1)))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("signatureStatus is required");

        assertThatThrownBy(() -> builder.build(
                fixture().signatureStatus("NOT_SIGNED").build(),
                new AgentActivationCommand("AGDF1", null, true),
                context(1)))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("must be SIGNED");
    }

    @Test
    void resolverRegistryRejectsNoResolverMultipleResolversAndDuplicateBindingIds() {
        AgentActivationSnapshot snapshot = fixture().build();
        assertThatThrownBy(() -> new RuntimeDataSourceBindingResolverRegistry(List.of()).resolve(snapshot))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("No RuntimeDataSourceBindingResolver");
        RuntimeDataSourceBindingResolver matching = new EventServiceDataRuntimeDataSourceBindingResolver(AgentRuntimePackageConfiguration.defaults());
        assertThatThrownBy(() -> new RuntimeDataSourceBindingResolverRegistry(List.of(matching, matching)).resolve(snapshot))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("Multiple");
        RuntimeDataSourceBindingResolver duplicateResolver = new RuntimeDataSourceBindingResolver() {
            @Override
            public boolean supports(AgentActivationSnapshot snapshot) {
                return true;
            }

            @Override
            public List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> resolve(AgentActivationSnapshot snapshot) {
                AgentRuntimeSubmission.AgentRuntimeDataSourceBinding binding = new AgentRuntimeSubmission.AgentRuntimeDataSourceBinding(
                        "primaryInput", "SERVICE_DATA", "EVENT_STREAM", "KAFKA", "ref", "ServiceDataV2", "v2", "schema", null, true, Map.of(), List.of());
                return List.of(binding, binding);
            }
        };
        assertThatThrownBy(() -> new RuntimeDataSourceBindingResolverRegistry(List.of(duplicateResolver)).resolve(snapshot))
                .isInstanceOf(AgentRuntimePackageBuildException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void concreteResolversSupportOnlyTheirTechnicalShapeAndRejectMissingConnectorConfiguration() {
        AgentRuntimePackageConfiguration config = AgentRuntimePackageConfiguration.defaults();
        EventServiceDataRuntimeDataSourceBindingResolver eventResolver = new EventServiceDataRuntimeDataSourceBindingResolver(config);
        ScheduledServiceDataRuntimeDataSourceBindingResolver scheduledResolver = new ScheduledServiceDataRuntimeDataSourceBindingResolver(config);

        assertThat(eventResolver.supports(fixture().build())).isTrue();
        assertThat(eventResolver.supports(fixture().scheduled().build())).isFalse();
        assertThat(scheduledResolver.supports(fixture().scheduled().build())).isTrue();
        assertThat(scheduledResolver.supports(fixture().build())).isFalse();
        assertThatThrownBy(() -> new AgentRuntimePackageConfiguration(
                config.controlPlaneComponent(),
                config.defaultRuntimeClass(),
                config.artifactCanonicalization(),
                config.artifactMediaType(),
                config.bindingSchemaVersion(),
                null,
                config.scheduledServiceDataConnector()))
                .isInstanceOf(AgentRuntimePackageBuildException.class);
    }

    @Test
    void buildContextRejectsInvalidValues() {
        assertThatThrownBy(() -> new AgentRuntimePackageBuildContext(0, SUBMITTED_AT, "operator"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AgentRuntimePackageBuildContext(1, null, "operator"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AgentRuntimePackageBuildContext(1, SUBMITTED_AT, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void serializedPackageContainsNoPhysicalUrlsCredentialsOrMutableCollections() {
        AgentRuntimePackageBuildResult result = builder.build(
                fixture().scheduled().dailyWindow().build(),
                new AgentActivationCommand("AGDF1", "operator note", true),
                context(1));
        String json = write(result.submission());

        assertThat(json).doesNotContain("http://", "https://", "bootstrap", "topic", "password", "token", "secret",
                "technicalSpecification", "blueprint", "validationPlan");
        assertThatThrownBy(() -> result.submission().agentDefinition().dataSourceBindings().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.submission().agentDefinition().artifact().content().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private AgentRuntimePackageBuildContext context(long version) {
        return new AgentRuntimePackageBuildContext(version, SUBMITTED_AT, "operator1");
    }

    private String write(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Fixture fixture() {
        return new Fixture();
    }

    private class Fixture {
        private String interpreterType = "EVENT_INTERPRETER";
        private String triggerType = "EVENT";
        private String inputModel = "ServiceDataV2";
        private Map<String, Object> runtimeContract = eventRuntimeContract();
        private Map<String, Object> dsl = eventDsl();
        private AgentActivationSnapshot.AgentActivationPolicySnapshot policy = continuousPolicy("2026-07-01T10:00:00Z");
        private List<AgentActivationSnapshot.AgentActivationAllowedToolSnapshot> tools = List.of();
        private AgentRuntimePackageConfiguration configuration = AgentRuntimePackageConfiguration.defaults();
        private Integer cpuRequest = 250;
        private String signatureStatus = "SIGNED";

        Fixture scheduled() {
            interpreterType = "SCHEDULED_INTERPRETER";
            triggerType = "SCHEDULE";
            inputModel = "ServiceDataStopPointJourneysV2";
            runtimeContract = scheduledRuntimeContract();
            dsl = scheduledDsl();
            tools = List.of(new AgentActivationSnapshot.AgentActivationAllowedToolSnapshot(TOOL, Map.of("operations", List.of("POST /v2/stoppointjourneys"))));
            return this;
        }

        Fixture dailyWindow() {
            policy = new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                    "DAILY_WINDOW", "Europe/Rome", null, null,
                    LocalDate.parse("2026-06-01"), LocalDate.parse("2026-06-30"),
                    LocalTime.parse("08:00"), LocalTime.parse("18:00"),
                    List.of("MONDAY", "WEDNESDAY"), Map.of("type", "DAILY_WINDOW", "timezone", "Europe/Rome"));
            return this;
        }

        Fixture profileCpu(int value) {
            cpuRequest = value;
            return this;
        }

        Fixture continuousTo(String validTo) {
            policy = continuousPolicy(validTo);
            return this;
        }

        Fixture dslExtra(String key, Object value) {
            dsl = new LinkedHashMap<>(dsl);
            dsl.put(key, value);
            return this;
        }

        Fixture signatureStatus(String value) {
            signatureStatus = value;
            return this;
        }

        Fixture connectorRef(String value) {
            configuration = new AgentRuntimePackageConfiguration(
                    "pis-intelligentinformationassistant-api-assistant",
                    "STANDARD_DSL_RUNTIME",
                    "RFC8785_JSON",
                    "application/json",
                    "iia.runtime.data-source-binding/v1",
                    new AgentRuntimePackageConfiguration.ConnectorConfiguration(value, "KAFKA", "EVENT_STREAM", "ServiceDataV2", "v2", null, "SERVICEDATA_EVENTS"),
                    AgentRuntimePackageConfiguration.defaults().scheduledServiceDataConnector());
            return this;
        }

        Fixture subscriptionProfile(String value) {
            AgentRuntimePackageConfiguration current = configuration;
            AgentRuntimePackageConfiguration.ConnectorConfiguration event = current.eventServiceDataConnector();
            configuration = new AgentRuntimePackageConfiguration(
                    current.controlPlaneComponent(), current.defaultRuntimeClass(), current.artifactCanonicalization(),
                    current.artifactMediaType(), current.bindingSchemaVersion(),
                    new AgentRuntimePackageConfiguration.ConnectorConfiguration(event.connectorRef(), event.connectorType(),
                            event.accessMode(), event.inputModel(), event.inputSchemaVersion(), event.operationRef(), value),
                    current.scheduledServiceDataConnector(), current.minimumRuntimeVersion(), current.sdkVersion(), current.networkPolicy());
            return this;
        }

        Fixture runtimeCapabilities(String minimum, String sdk, String policy) {
            AgentRuntimePackageConfiguration current = configuration;
            configuration = new AgentRuntimePackageConfiguration(
                    current.controlPlaneComponent(), current.defaultRuntimeClass(), current.artifactCanonicalization(),
                    current.artifactMediaType(), current.bindingSchemaVersion(), current.eventServiceDataConnector(),
                    current.scheduledServiceDataConnector(), minimum, sdk, policy);
            return this;
        }

        AgentActivationSnapshot build() {
            AgentCanonicalJsonHashResult hash = new AgentCanonicalJsonService().hash(dsl);
            return snapshot(hash.hash());
        }

        private AgentActivationSnapshot snapshot(String artifactHash) {
            return new AgentActivationSnapshot(
                    "AGDF1", "Runtime Agent", "Runtime package fixture", "READY", "DSL", "MEDIUM",
                    interpreterType, triggerType, inputModel, "AgentOutput.CANDIDATE_SUGGESTION", "operator1",
                    OffsetDateTime.parse("2026-06-16T10:00:00Z"), OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                    new AgentActivationSnapshot.AgentActivationAlertSnapshot("ALRT1", "Alert", 7),
                    new AgentActivationSnapshot.AgentActivationProfileSnapshot(
                            "MEDIUM", "Medium", "Profile", true, List.of("EVENT_INTERPRETER"),
                            cpuRequest, 500, 512, 1024, "TOOL_GATEWAY_ONLY", 2,
                            OffsetDateTime.parse("2026-06-01T10:00:00Z"), OffsetDateTime.parse("2026-06-10T10:00:00Z")),
                    policy,
                    new AgentActivationSnapshot.AgentActivationRequirementsSnapshot(
                            List.of(new AgentActivationSnapshot.AgentActivationSourceSnapshot("SERVICE_DATA", true, null)),
                            List.of(Map.of("permission", "READ_SERVICE_DATA")),
                            tools,
                            tools.stream()
                                    .map(AgentActivationSnapshot.AgentActivationAllowedToolSnapshot::toolName)
                                    .map(Object.class::cast)
                                    .toList(),
                            List.of(),
                            runtimeContract,
                            Map.of("dslPreviewOnly", true)),
                    new AgentActivationSnapshot.AgentActivationArtifactSnapshot(
                            "DSL",
                            "iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl",
                            artifactHash,
                            signatureStatus,
                            "STANDARD_AGENT_DSL_EVALUATOR",
                            "iia.agent.dsl/v1",
                            "Compiled DSL."),
                    new AgentActivationSnapshot.AgentActivationCompilationSummarySnapshot(
                            "AGCP1", "READY", "READY", OffsetDateTime.parse("2026-06-17T09:02:00Z")),
                    new AgentActivationSnapshot.AgentActivationCompilationSnapshot(
                            "AGCP1", "AGDF1", "READY", "READY", "DSL", false,
                            Map.of("requestedMode", "DSL"),
                            Map.of("artifactHash", artifactHash, "dslArtifact", dsl),
                            null, "operator1",
                            OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                            OffsetDateTime.parse("2026-06-17T09:01:00Z"),
                            OffsetDateTime.parse("2026-06-17T09:02:00Z"),
                            OffsetDateTime.parse("2026-06-17T09:02:00Z"),
                            dsl));
        }

        AgentRuntimePackageBuildResult buildWithLocalBuilder() {
            return new AgentRuntimePackageBuilder(configuration, null, new AgentCanonicalJsonService())
                    .build(build(), new AgentActivationCommand("AGDF1", null, true), context(1));
        }
    }

    private AgentActivationSnapshot.AgentActivationPolicySnapshot continuousPolicy(String validTo) {
        return new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                "CONTINUOUS", "Europe/Rome",
                OffsetDateTime.parse("2026-06-01T10:00:00Z"), OffsetDateTime.parse(validTo),
                null, null, null, null, List.of(), Map.of("type", "CONTINUOUS", "timezone", "Europe/Rome"));
    }

    private Map<String, Object> eventRuntimeContract() {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("interpreterType", "EVENT_INTERPRETER");
        contract.put("triggerType", "EVENT");
        contract.put("inputModel", "ServiceDataV2");
        contract.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        contract.put("evaluationMode", "STATELESS_EVENT_MATCH");
        contract.put("runtimeExecutionModel", "STANDARD_DSL_EVALUATOR");
        contract.put("executionModel", "KAFKA_EVENT");
        contract.put("source", "SERVICE_DATA");
        contract.put("requiresState", false);
        contract.put("requiresScheduler", false);
        contract.put("allowedTools", List.of());
        contract.put("forbiddenCapabilities", List.of("LLM_RUNTIME_EXECUTION", "EXTERNAL_CODE_EXECUTION"));
        contract.put("orchestratorCompatibility", Map.of(
                "minimumRuntimeVersion", "0.0.3",
                "runtimeClass", "STANDARD_DSL_RUNTIME",
                "canonicalization", "RFC8785_JSON"));
        return contract;
    }

    private Map<String, Object> scheduledRuntimeContract() {
        Map<String, Object> contract = eventRuntimeContract();
        contract.put("interpreterType", "SCHEDULED_INTERPRETER");
        contract.put("triggerType", "SCHEDULE");
        contract.put("inputModel", "ServiceDataStopPointJourneysV2");
        contract.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        contract.put("executionModel", "SCHEDULED_POLLING");
        contract.put("requiresScheduler", true);
        contract.put("allowedTools", List.of(TOOL));
        return contract;
    }

    private Map<String, Object> eventDsl() {
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("engine", "STANDARD_AGENT_DSL_EVALUATOR");
        runtime.put("executionModel", "KAFKA_EVENT");
        runtime.put("source", "SERVICE_DATA");
        runtime.put("interpreterType", "EVENT_INTERPRETER");
        runtime.put("triggerType", "EVENT");
        runtime.put("inputModel", "ServiceDataV2");
        runtime.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        runtime.put("evaluationMode", "STATELESS_EVENT_MATCH");
        runtime.put("requiresScheduler", false);
        runtime.put("requiresState", false);
        runtime.put("requiresExternalTools", false);
        runtime.put("allowedTools", List.of());
        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("schemaVersion", "iia.agent.dsl/v1");
        artifact.put("artifactType", "DSL");
        artifact.put("agentDefinitionId", "AGDF1");
        artifact.put("runtime", runtime);
        artifact.put("trigger", Map.of("type", "EVENT", "source", "SERVICE_DATA", "inputModel", "ServiceDataV2"));
        artifact.put("evaluation", Map.of(
                "mode", "STATELESS_EVENT_MATCH",
                "condition", Map.of("all", List.of(
                        Map.of("field", "payload.status", "operator", "CONTAINS", "value", "active"),
                        Map.of("field", "payload.message", "operator", "CONTAINS", "value", "delay")))));
        artifact.put("output", Map.of("type", "CANDIDATE_SUGGESTION", "outputModel", "AgentOutput.CANDIDATE_SUGGESTION"));
        artifact.put("governance", Map.of("llmRuntimeExecutionAllowed", false, "externalCodeExecutionAllowed", false));
        return artifact;
    }

    private Map<String, Object> scheduledDsl() {
        Map<String, Object> artifact = eventDsl();
        artifact.put("runtime", Map.of(
                "engine", "STANDARD_AGENT_DSL_EVALUATOR",
                "executionModel", "SCHEDULED_POLLING",
                "source", "SERVICE_DATA",
                "interpreterType", "SCHEDULED_INTERPRETER",
                "triggerType", "SCHEDULE",
                "inputModel", "ServiceDataStopPointJourneysV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "requiresScheduler", true,
                "allowedTools", List.of(TOOL)));
        artifact.put("trigger", Map.of("type", "SCHEDULE", "schedule", Map.of("frequencySeconds", 600)));
        artifact.put("toolAccess", Map.of("allowedTools", List.of(TOOL)));
        artifact.put("query", Map.of("operation", "POST /v2/stoppointjourneys"));
        artifact.put("evaluation", Map.of("mode", "SCHEDULED_SNAPSHOT_MATCH", "snapshotEvaluation", Map.of("condition", Map.of("field", "journey.status", "operator", "EXISTS"))));
        return artifact;
    }
}
