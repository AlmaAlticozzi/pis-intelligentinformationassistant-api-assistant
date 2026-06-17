package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentActivationPreconditionValidatorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-17T10:00:00Z"), ZoneOffset.UTC);
    private static final String TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";

    private final AgentArtifactHashService hashService = new AgentArtifactHashService();
    private final AgentActivationPreconditionValidator validator = new AgentActivationPreconditionValidator(
            hashService,
            new AgentDslRuntimeCompatibilityValidator(),
            CLOCK);

    @Test
    void validEventAgentPassesPreconditionValidation() {
        AgentActivationPreconditionValidationResult result = validator.validate(fixture().build());

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void validScheduledAgentPassesPreconditionValidation() {
        AgentActivationPreconditionValidationResult result = validator.validate(fixture().scheduled().build());

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void continuousPolicyWithFutureValidFromIsValidWarning() {
        AgentActivationPreconditionValidationResult result = validator.validate(fixture()
                .continuousPolicy(
                        OffsetDateTime.parse("2026-06-18T10:00:00Z"),
                        OffsetDateTime.parse("2026-07-01T10:00:00Z"))
                .build());

        assertThat(result.valid()).isTrue();
        assertThat(result.warnings()).extracting(AgentActivationPreconditionViolation::code)
                .contains(AgentActivationPreconditionCode.ACTIVATION_POLICY_RANGE_INVALID);
    }

    @Test
    void dailyWindowPolicyValidatesDatesTimesTimezoneAndDays() {
        AgentActivationPreconditionValidationResult result = validator.validate(fixture()
                .scheduled()
                .dailyWindowPolicy(
                        LocalDate.parse("2026-06-01"),
                        LocalDate.parse("2026-06-30"),
                        LocalTime.parse("08:00"),
                        LocalTime.parse("18:00"),
                        List.of("MONDAY", "WEDNESDAY"))
                .build());

        assertThat(result.valid()).isTrue();
    }

    @Test
    void detectsLatestCompilationProblems() {
        assertCodes(fixture().compilationReference(null).build(),
                AgentActivationPreconditionCode.LATEST_COMPILATION_REFERENCE_MISSING);
        assertCodes(fixture().withoutCompilation().build(),
                AgentActivationPreconditionCode.LATEST_COMPILATION_NOT_FOUND);
        assertCodes(fixture().compilationId("AGCP2").build(),
                AgentActivationPreconditionCode.LATEST_COMPILATION_REFERENCE_MISMATCH);
        assertCodes(fixture().compilationAgentDefinitionId("OTHER").build(),
                AgentActivationPreconditionCode.COMPILATION_AGENT_DEFINITION_MISMATCH);
        assertCodes(fixture().compilationStatus("FAILED", "SIGNING").build(),
                AgentActivationPreconditionCode.COMPILATION_NOT_READY);
        assertCodes(fixture().withoutCompilationResult().build(),
                AgentActivationPreconditionCode.COMPILATION_RESULT_MISSING,
                AgentActivationPreconditionCode.DSL_ARTIFACT_MISSING);
        assertCodes(fixture().withoutDslArtifact().build(),
                AgentActivationPreconditionCode.DSL_ARTIFACT_MISSING);
        assertCodes(fixture().dslArtifact(Map.of()).build(),
                AgentActivationPreconditionCode.DSL_ARTIFACT_INVALID);
    }

    @Test
    void detectsArtifactMetadataAndSignatureProblems() {
        assertCodes(fixture().artifactType("JAVA").build(),
                AgentActivationPreconditionCode.ARTIFACT_TYPE_UNSUPPORTED);
        assertCodes(fixture().artifactUri(null).build(),
                AgentActivationPreconditionCode.ARTIFACT_URI_MISSING);
        assertCodes(fixture().artifactUri("artifact://bad").build(),
                AgentActivationPreconditionCode.ARTIFACT_URI_INVALID);
        assertCodes(fixture().artifactHash(null).build(),
                AgentActivationPreconditionCode.ARTIFACT_HASH_MISSING);
        assertCodes(fixture().artifactHash("sha256:not-hex").build(),
                AgentActivationPreconditionCode.ARTIFACT_HASH_FORMAT_INVALID);
        assertCodes(fixture().signatureStatus("NOT_SIGNED").build(),
                AgentActivationPreconditionCode.ARTIFACT_NOT_SIGNED);
        assertCodes(fixture().runtimeImage(null).build(),
                AgentActivationPreconditionCode.RUNTIME_IMAGE_MISSING);
        assertCodes(fixture().sdkVersion(null).build(),
                AgentActivationPreconditionCode.SDK_VERSION_MISSING);
        assertCodes(fixture().implementationSummary(null).build(),
                AgentActivationPreconditionCode.IMPLEMENTATION_SUMMARY_MISSING);
    }

    @Test
    void detectsArtifactHashMismatchesAndCanonicalizationFailure() {
        assertCodes(fixture().artifactHash(validDifferentHash()).build(),
                AgentActivationPreconditionCode.ARTIFACT_HASH_MISMATCH);
        assertCodes(fixture().compilationResultArtifactHash(validDifferentHash()).build(),
                AgentActivationPreconditionCode.COMPILATION_ARTIFACT_HASH_MISMATCH);

        Map<String, Object> cyclic = new LinkedHashMap<>(validEventDsl());
        cyclic.put("cycle", cyclic);
        assertCodes(fixture().dslArtifact(cyclic).artifactHash(validDifferentHash()).build(),
                AgentActivationPreconditionCode.ARTIFACT_CANONICALIZATION_FAILED);
    }

    @Test
    void detectsProfileProblems() {
        assertCodes(fixture().profile(null).build(),
                AgentActivationPreconditionCode.AGENT_PROFILE_MISSING);
        assertCodes(fixture().profileEnabled(false).build(),
                AgentActivationPreconditionCode.AGENT_PROFILE_DISABLED);
        assertCodes(fixture().cpu(800, 500).build(),
                AgentActivationPreconditionCode.AGENT_PROFILE_RESOURCES_INVALID);
        assertCodes(fixture().memory(2048, 1024).build(),
                AgentActivationPreconditionCode.AGENT_PROFILE_RESOURCES_INVALID);
        assertCodes(fixture().concurrency(0).build(),
                AgentActivationPreconditionCode.AGENT_PROFILE_RESOURCES_INVALID);
        assertCodes(fixture().networkPolicy(null).build(),
                AgentActivationPreconditionCode.AGENT_PROFILE_NETWORK_POLICY_MISSING);
    }

    @Test
    void detectsActivationPolicyProblems() {
        assertCodes(fixture().timezone("Europe/Invalid").build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_TIMEZONE_INVALID);
        assertCodes(fixture().continuousPolicy(
                        OffsetDateTime.parse("2026-06-20T10:00:00Z"),
                        OffsetDateTime.parse("2026-06-19T10:00:00Z"))
                .build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_RANGE_INVALID);
        assertCodes(fixture().continuousPolicy(
                        OffsetDateTime.parse("2026-06-01T10:00:00Z"),
                        OffsetDateTime.parse("2026-06-16T10:00:00Z"))
                .build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_EXPIRED);
        assertCodes(fixture().scheduled().dailyWindowPolicy(
                        LocalDate.parse("2026-06-01"),
                        LocalDate.parse("2026-06-16"),
                        LocalTime.parse("08:00"),
                        LocalTime.parse("18:00"),
                        List.of("MONDAY"))
                .build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_EXPIRED);
        assertCodes(fixture().scheduled().dailyWindowPolicy(
                        LocalDate.parse("2026-06-01"),
                        LocalDate.parse("2026-06-30"),
                        LocalTime.parse("18:00"),
                        LocalTime.parse("18:00"),
                        List.of("MONDAY"))
                .build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_DAILY_WINDOW_INVALID);
        assertCodes(fixture().scheduled().dailyWindowPolicy(
                        LocalDate.parse("2026-06-01"),
                        LocalDate.parse("2026-06-30"),
                        LocalTime.parse("08:00"),
                        LocalTime.parse("18:00"),
                        List.of("FUNDAY"))
                .build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_DAILY_WINDOW_INVALID);
        assertCodes(fixture().policyJson(Map.of("type", "DAILY_WINDOW", "timezone", "Europe/Rome")).build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_INCONSISTENT);
        assertCodes(fixture().policyJson(Map.of()).build(),
                AgentActivationPreconditionCode.ACTIVATION_POLICY_JSON_INVALID);
    }

    @Test
    void detectsRuntimeContractProblems() {
        assertCodes(fixture().runtimeContract(null).build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_MISSING);
        assertCodes(fixture().runtimeContractValue("interpreterType", "SCHEDULED_INTERPRETER").build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_INTERPRETER_MISMATCH);
        assertCodes(fixture().runtimeContractValue("triggerType", "SCHEDULE").build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_TRIGGER_MISMATCH);
        assertCodes(fixture().runtimeContractValue("inputModel", "OtherModel").build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_INPUT_MODEL_MISMATCH);
        assertCodes(fixture().runtimeContractValue("outputModel", "OtherOutput").build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_OUTPUT_MODEL_MISMATCH);
        assertCodes(fixture().runtimeContractValue("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH").build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_EVALUATION_MODE_MISMATCH);
        assertCodes(fixture().runtimeContractValue("executionModel", "UNSUPPORTED").build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_EXECUTION_MODEL_UNSUPPORTED);

        Map<String, Object> dsl = validEventDsl();
        dsl.put("governance", Map.of(
                "generationMode", "DSL",
                "compiledBy", "api-assistant",
                "llmRuntimeExecutionAllowed", true,
                "externalCodeExecutionAllowed", false));
        assertCodes(fixture()
                        .dslArtifact(dsl)
                        .runtimeContractValue("forbiddenCapabilities", List.of("LLM_RUNTIME_EXECUTION"))
                        .build(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_FORBIDDEN_CAPABILITY,
                AgentActivationPreconditionCode.DSL_RUNTIME_COMPATIBILITY_FAILED);
    }

    @Test
    void detectsDslRuntimeCompatibilityFailure() {
        Map<String, Object> dsl = validEventDsl();
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
        runtime.put("allowedTools", List.of("UNEXPECTED_TOOL"));
        dsl.put("runtime", runtime);

        assertCodes(fixture().dslArtifact(dsl).build(),
                AgentActivationPreconditionCode.DSL_RUNTIME_COMPATIBILITY_FAILED);
    }

    @Test
    void detectsRequiredSourcesAndAllowedToolsProblems() {
        assertCodes(fixture().requiredSources(List.of()).build(),
                AgentActivationPreconditionCode.REQUIRED_SOURCE_MISSING);
        assertCodes(fixture().requiredSources(List.of(
                        new AgentActivationSnapshot.AgentActivationSourceSnapshot("SERVICE_DATA", true, null),
                        new AgentActivationSnapshot.AgentActivationSourceSnapshot("SERVICE_DATA", true, null)))
                .build(),
                AgentActivationPreconditionCode.REQUIRED_SOURCE_INVALID);
        assertCodes(fixture().requiredSources(List.of(source("OTHER"))).build(),
                AgentActivationPreconditionCode.REQUIRED_SOURCE_MISMATCH);
        assertCodes(fixture().allowedTools(List.of(tool(""))).build(),
                AgentActivationPreconditionCode.ALLOWED_TOOL_INVALID);
        assertCodes(fixture().scheduled().allowedTools(List.of()).build(),
                AgentActivationPreconditionCode.ALLOWED_TOOL_MISMATCH);
        assertCodes(fixture().allowedTools(List.of(tool("EXTERNAL_TOOL"))).build(),
                AgentActivationPreconditionCode.ALLOWED_TOOL_MISMATCH);
    }

    @Test
    void collectsMultipleIndependentErrorsInSingleExecution() {
        AgentActivationPreconditionValidationResult result = validator.validate(fixture()
                .artifactHash(validDifferentHash())
                .profileEnabled(false)
                .timezone("Bad/Zone")
                .build());

        assertThat(codes(result)).contains(
                AgentActivationPreconditionCode.ARTIFACT_HASH_MISMATCH,
                AgentActivationPreconditionCode.AGENT_PROFILE_DISABLED,
                AgentActivationPreconditionCode.ACTIVATION_POLICY_TIMEZONE_INVALID);
    }

    @Test
    void doesNotModifySnapshotAndExposesImmutableResultCollections() {
        AgentActivationSnapshot snapshot = fixture().build();
        Map<String, Object> dsl = snapshot.dslArtifact();
        AgentActivationPreconditionValidationResult result = validator.validate(snapshot);

        assertThat(snapshot.dslArtifact()).isSameAs(dsl);
        assertThat(snapshot.dslArtifact()).isEqualTo(dsl);
        assertThatThrownBy(() -> result.errors().add(new AgentActivationPreconditionViolation(
                AgentActivationPreconditionCode.DSL_ARTIFACT_INVALID,
                "field",
                "message")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void validatorDoesNotDependOnRepositoriesEntitiesRestOrLlm() {
        assertThat(AgentActivationPreconditionValidator.class.getDeclaredFields())
                .filteredOn(field -> !Modifier.isStatic(field.getModifiers()))
                .extracting(field -> field.getType().getName())
                .containsOnly(
                        "it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentArtifactHashService",
                        "it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentDslRuntimeCompatibilityValidator",
                        "java.time.Clock");
        assertThat(AgentActivationPreconditionValidationResult.class.getRecordComponents())
                .extracting(component -> component.getType().getName())
                .doesNotContain("jakarta.ws.rs.core.Response");
    }

    private void assertCodes(AgentActivationSnapshot snapshot, AgentActivationPreconditionCode... expectedCodes) {
        AgentActivationPreconditionValidationResult result = validator.validate(snapshot);
        assertThat(result.valid()).isFalse();
        assertThat(codes(result)).contains(expectedCodes);
    }

    private List<AgentActivationPreconditionCode> codes(AgentActivationPreconditionValidationResult result) {
        return result.errors().stream().map(AgentActivationPreconditionViolation::code).toList();
    }

    private String validDifferentHash() {
        return "sha256:0000000000000000000000000000000000000000000000000000000000000000";
    }

    private Fixture fixture() {
        return new Fixture();
    }

    private AgentActivationSnapshot.AgentActivationSourceSnapshot source(String source) {
        return new AgentActivationSnapshot.AgentActivationSourceSnapshot(source, true, null);
    }

    private AgentActivationSnapshot.AgentActivationAllowedToolSnapshot tool(String name) {
        return new AgentActivationSnapshot.AgentActivationAllowedToolSnapshot(name, Map.of("operations", List.of(name)));
    }

    private Map<String, Object> validEventDsl() {
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
        artifact.put("agentDefinitionVersion", 1);
        artifact.put("source", Map.of("type", "AGENT_DEFINITION", "alertId", "ALRT1", "alertVersion", 1));
        artifact.put("runtime", runtime);
        artifact.put("trigger", Map.of("type", "EVENT", "source", "SERVICE_DATA", "inputModel", "ServiceDataV2"));
        artifact.put("evaluation", Map.of(
                "mode", "STATELESS_EVENT_MATCH",
                "condition", Map.of("field", "payload.status", "operator", "EXISTS")));
        artifact.put("output", Map.of(
                "type", "CANDIDATE_SUGGESTION",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "deduplicationKeyTemplate", "SERVICE_DATA_EVENT:${agentDefinitionId}:${eventId}:${conditionHash}"));
        artifact.put("governance", Map.of(
                "generationMode", "DSL",
                "compiledBy", "api-assistant",
                "llmRuntimeExecutionAllowed", false,
                "externalCodeExecutionAllowed", false));
        return artifact;
    }

    private Map<String, Object> validScheduledDsl() {
        Map<String, Object> artifact = validEventDsl();
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("engine", "STANDARD_AGENT_DSL_EVALUATOR");
        runtime.put("executionModel", "SCHEDULED_POLLING");
        runtime.put("source", "SERVICE_DATA");
        runtime.put("accessMode", "SERVICE_DATA_API_SNAPSHOT");
        runtime.put("interpreterType", "SCHEDULED_INTERPRETER");
        runtime.put("triggerType", "SCHEDULE");
        runtime.put("inputModel", "ServiceDataStopPointJourneysV2");
        runtime.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        runtime.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        runtime.put("requiresScheduler", true);
        runtime.put("requiresState", false);
        runtime.put("requiresExternalTools", false);
        runtime.put("allowedTools", List.of(TOOL));
        artifact.put("runtime", runtime);
        artifact.put("trigger", Map.of("type", "SCHEDULE", "schedule", Map.of("frequencySeconds", 600)));
        artifact.put("toolAccess", Map.of("allowedTools", List.of(TOOL)));
        artifact.put("query", Map.of(
                "operation", "POST /v2/stoppointjourneys",
                "serviceDataQuery", Map.of("monitoringScope", "ALL_STOP_POINTS")));
        artifact.put("evaluation", Map.of(
                "mode", "SCHEDULED_SNAPSHOT_MATCH",
                "snapshotEvaluation", Map.of("condition", Map.of("field", "journey.status", "operator", "EXISTS"))));
        artifact.put("output", Map.of(
                "type", "CANDIDATE_SUGGESTION",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "policy", Map.of("emit", "ON_MATCH"),
                "deduplicationKeyTemplate", "SERVICE_DATA_SCHEDULED:${agentDefinitionId}:${queryWindowStart}:${conditionHash}"));
        return artifact;
    }

    private Map<String, Object> eventRuntimeContract() {
        return new LinkedHashMap<>(Map.of(
                "interpreterType", "EVENT_INTERPRETER",
                "triggerType", "EVENT",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "executionModel", "KAFKA_EVENT",
                "source", "SERVICE_DATA",
                "requiresState", false,
                "requiresScheduler", false,
                "allowedTools", List.of()));
    }

    private Map<String, Object> scheduledRuntimeContract() {
        Map<String, Object> contract = eventRuntimeContract();
        contract.put("interpreterType", "SCHEDULED_INTERPRETER");
        contract.put("triggerType", "SCHEDULE");
        contract.put("inputModel", "ServiceDataStopPointJourneysV2");
        contract.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        contract.put("executionModel", "SCHEDULED_POLLING");
        contract.put("accessMode", "SERVICE_DATA_API_SNAPSHOT");
        contract.put("requiresScheduler", true);
        contract.put("allowedTools", List.of(TOOL));
        return contract;
    }

    private class Fixture {
        private String agentDefinitionId = "AGDF1";
        private String interpreterType = "EVENT_INTERPRETER";
        private String triggerType = "EVENT";
        private String inputModel = "ServiceDataV2";
        private String outputModel = "AgentOutput.CANDIDATE_SUGGESTION";
        private AgentActivationSnapshot.AgentActivationProfileSnapshot profile = profile(true, 250, 500, 512, 1024, 2, "TOOL_GATEWAY_ONLY");
        private AgentActivationSnapshot.AgentActivationPolicySnapshot policy = continuousPolicySnapshot(
                OffsetDateTime.parse("2026-06-01T10:00:00Z"),
                OffsetDateTime.parse("2026-07-01T10:00:00Z"),
                Map.of("type", "CONTINUOUS", "timezone", "Europe/Rome"));
        private List<AgentActivationSnapshot.AgentActivationSourceSnapshot> requiredSources = List.of(source("SERVICE_DATA"));
        private List<AgentActivationSnapshot.AgentActivationAllowedToolSnapshot> allowedTools = List.of();
        private Map<String, Object> runtimeContract = eventRuntimeContract();
        private Map<String, Object> dslArtifact = validEventDsl();
        private String compilationReference = "AGCP1";
        private String compilationId = "AGCP1";
        private String compilationAgentDefinitionId = "AGDF1";
        private String compilationStatus = "READY";
        private String compilationStep = "READY";
        private boolean compilationLoaded = true;
        private boolean resultPresent = true;
        private boolean includeDslArtifact = true;
        private String compilationResultArtifactHash;
        private String artifactType = "DSL";
        private String artifactUri = "iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl";
        private String artifactHash;
        private boolean autoHash = true;
        private String signatureStatus = "SIGNED";
        private String runtimeImage = "STANDARD_AGENT_DSL_EVALUATOR";
        private String sdkVersion = "iia.agent.dsl/v1";
        private String implementationSummary = "Compiled DSL.";

        Fixture scheduled() {
            interpreterType = "SCHEDULED_INTERPRETER";
            triggerType = "SCHEDULE";
            inputModel = "ServiceDataStopPointJourneysV2";
            runtimeContract = scheduledRuntimeContract();
            dslArtifact = validScheduledDsl();
            allowedTools = List.of(tool(TOOL));
            return this;
        }

        Fixture continuousPolicy(OffsetDateTime validFrom, OffsetDateTime validTo) {
            policy = continuousPolicySnapshot(validFrom, validTo, Map.of("type", "CONTINUOUS", "timezone", "Europe/Rome"));
            return this;
        }

        Fixture dailyWindowPolicy(LocalDate from, LocalDate to, LocalTime start, LocalTime end, List<String> days) {
            policy = new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                    "DAILY_WINDOW",
                    "Europe/Rome",
                    null,
                    null,
                    from,
                    to,
                    start,
                    end,
                    days,
                    Map.of("type", "DAILY_WINDOW", "timezone", "Europe/Rome"));
            return this;
        }

        Fixture policyJson(Map<String, Object> json) {
            policy = new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                    policy.activationType(),
                    policy.timezone(),
                    policy.validFrom(),
                    policy.validTo(),
                    policy.validFromDate(),
                    policy.validToDate(),
                    policy.dailyStartTime(),
                    policy.dailyEndTime(),
                    policy.daysOfWeek(),
                    json);
            return this;
        }

        Fixture timezone(String timezone) {
            policy = new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                    policy.activationType(),
                    timezone,
                    policy.validFrom(),
                    policy.validTo(),
                    policy.validFromDate(),
                    policy.validToDate(),
                    policy.dailyStartTime(),
                    policy.dailyEndTime(),
                    policy.daysOfWeek(),
                    Map.of("type", policy.activationType(), "timezone", timezone));
            return this;
        }

        Fixture compilationReference(String value) {
            compilationReference = value;
            return this;
        }

        Fixture withoutCompilation() {
            compilationLoaded = false;
            return this;
        }

        Fixture compilationId(String value) {
            compilationId = value;
            return this;
        }

        Fixture compilationAgentDefinitionId(String value) {
            compilationAgentDefinitionId = value;
            return this;
        }

        Fixture compilationStatus(String status, String step) {
            compilationStatus = status;
            compilationStep = step;
            return this;
        }

        Fixture withoutCompilationResult() {
            resultPresent = false;
            includeDslArtifact = false;
            return this;
        }

        Fixture withoutDslArtifact() {
            includeDslArtifact = false;
            return this;
        }

        Fixture dslArtifact(Map<String, Object> value) {
            dslArtifact = value;
            artifactHash = null;
            autoHash = true;
            return this;
        }

        Fixture artifactType(String value) {
            artifactType = value;
            return this;
        }

        Fixture artifactUri(String value) {
            artifactUri = value;
            return this;
        }

        Fixture artifactHash(String value) {
            artifactHash = value;
            autoHash = false;
            return this;
        }

        Fixture compilationResultArtifactHash(String value) {
            compilationResultArtifactHash = value;
            return this;
        }

        Fixture signatureStatus(String value) {
            signatureStatus = value;
            return this;
        }

        Fixture runtimeImage(String value) {
            runtimeImage = value;
            return this;
        }

        Fixture sdkVersion(String value) {
            sdkVersion = value;
            return this;
        }

        Fixture implementationSummary(String value) {
            implementationSummary = value;
            return this;
        }

        Fixture profile(AgentActivationSnapshot.AgentActivationProfileSnapshot value) {
            profile = value;
            return this;
        }

        Fixture profileEnabled(boolean value) {
            profile = profile(value, 250, 500, 512, 1024, 2, "TOOL_GATEWAY_ONLY");
            return this;
        }

        Fixture cpu(Integer request, Integer limit) {
            profile = profile(true, request, limit, 512, 1024, 2, "TOOL_GATEWAY_ONLY");
            return this;
        }

        Fixture memory(Integer request, Integer limit) {
            profile = profile(true, 250, 500, request, limit, 2, "TOOL_GATEWAY_ONLY");
            return this;
        }

        Fixture concurrency(Integer value) {
            profile = profile(true, 250, 500, 512, 1024, value, "TOOL_GATEWAY_ONLY");
            return this;
        }

        Fixture networkPolicy(String value) {
            profile = profile(true, 250, 500, 512, 1024, 2, value);
            return this;
        }

        Fixture runtimeContract(Map<String, Object> value) {
            runtimeContract = value;
            return this;
        }

        Fixture runtimeContractValue(String key, Object value) {
            runtimeContract = new LinkedHashMap<>(runtimeContract);
            runtimeContract.put(key, value);
            return this;
        }

        Fixture requiredSources(List<AgentActivationSnapshot.AgentActivationSourceSnapshot> value) {
            requiredSources = value;
            return this;
        }

        Fixture allowedTools(List<AgentActivationSnapshot.AgentActivationAllowedToolSnapshot> value) {
            allowedTools = value;
            return this;
        }

        AgentActivationSnapshot build() {
            String effectiveHash = artifactHash;
            if (autoHash && effectiveHash == null && dslArtifact != null && !dslArtifact.isEmpty()) {
                effectiveHash = hashService.hashDslArtifact(agentDefinitionId, compilationId, dslArtifact).artifactHash();
            }
            String resultHash = compilationResultArtifactHash == null ? effectiveHash : compilationResultArtifactHash;
            Map<String, Object> resultJson = null;
            if (resultPresent) {
                resultJson = new LinkedHashMap<>();
                resultJson.put("artifactHash", resultHash);
                resultJson.put("artifactUri", artifactUri);
                resultJson.put("artifactType", artifactType);
                resultJson.put("signatureStatus", signatureStatus);
                if (includeDslArtifact) {
                    resultJson.put("dslArtifact", dslArtifact);
                }
            }
            AgentActivationSnapshot.AgentActivationCompilationSnapshot compilation = compilationLoaded
                    ? new AgentActivationSnapshot.AgentActivationCompilationSnapshot(
                    compilationId,
                    compilationAgentDefinitionId,
                    compilationStatus,
                    compilationStep,
                    "DSL",
                    false,
                    Map.of("requestedMode", "DSL"),
                    resultJson,
                    null,
                    "operator1",
                    OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                    OffsetDateTime.parse("2026-06-17T09:01:00Z"),
                    OffsetDateTime.parse("2026-06-17T09:02:00Z"),
                    OffsetDateTime.parse("2026-06-17T09:02:00Z"),
                    includeDslArtifact ? dslArtifact : null)
                    : null;
            return new AgentActivationSnapshot(
                    agentDefinitionId,
                    "Activation Agent",
                    "Activation fixture",
                    "READY",
                    "DSL",
                    "MEDIUM",
                    interpreterType,
                    triggerType,
                    inputModel,
                    outputModel,
                    "operator1",
                    OffsetDateTime.parse("2026-06-16T10:00:00Z"),
                    OffsetDateTime.parse("2026-06-17T09:05:00Z"),
                    new AgentActivationSnapshot.AgentActivationAlertSnapshot("ALRT1", "Alert", 1),
                    profile,
                    policy,
                    new AgentActivationSnapshot.AgentActivationRequirementsSnapshot(
                            requiredSources,
                            List.of(Map.of("permission", "READ_SERVICE_DATA")),
                            allowedTools,
                            new ArrayList<>(allowedTools.stream().map(AgentActivationSnapshot.AgentActivationAllowedToolSnapshot::toolName).toList()),
                            List.of(),
                            runtimeContract,
                            Map.of("triggerType", triggerType)),
                    new AgentActivationSnapshot.AgentActivationArtifactSnapshot(
                            artifactType,
                            artifactUri,
                            effectiveHash,
                            signatureStatus,
                            runtimeImage,
                            sdkVersion,
                            implementationSummary),
                    new AgentActivationSnapshot.AgentActivationCompilationSummarySnapshot(
                            compilationReference,
                            compilationStatus,
                            compilationStep,
                            OffsetDateTime.parse("2026-06-17T09:02:00Z")),
                    compilation);
        }

        private AgentActivationSnapshot.AgentActivationProfileSnapshot profile(
                Boolean enabled,
                Integer cpuRequest,
                Integer cpuLimit,
                Integer memoryRequest,
                Integer memoryLimit,
                Integer concurrency,
                String networkPolicy) {
            return new AgentActivationSnapshot.AgentActivationProfileSnapshot(
                    "MEDIUM",
                    "Medium",
                    "Profile",
                    enabled,
                    List.of("EVENT_INTERPRETER", "SCHEDULED_INTERPRETER"),
                    cpuRequest,
                    cpuLimit,
                    memoryRequest,
                    memoryLimit,
                    networkPolicy,
                    concurrency,
                    OffsetDateTime.parse("2026-06-01T10:00:00Z"),
                    OffsetDateTime.parse("2026-06-10T10:00:00Z"));
        }

        private AgentActivationSnapshot.AgentActivationPolicySnapshot continuousPolicySnapshot(
                OffsetDateTime from,
                OffsetDateTime to,
                Map<String, Object> json) {
            return new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                    "CONTINUOUS",
                    "Europe/Rome",
                    from,
                    to,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    json);
        }
    }
}
