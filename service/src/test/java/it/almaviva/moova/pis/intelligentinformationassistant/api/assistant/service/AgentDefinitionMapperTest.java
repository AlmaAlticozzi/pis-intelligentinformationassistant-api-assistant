package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentContinuousActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDailyWindowActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentHealthStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentQualityStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRun;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRunStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentDefinitionMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    void mapsAgentDefinitionDetailContractAndReferences() {
        AgentDefinitionMapper mapper = new AgentDefinitionMapper();
        mapper.agentProfileMapper = new AgentProfileMapper();

        AgentDefinitionDetail detail = mapper.toDto(
                definition(),
                "EVENT_INTERPRETER",
                "EVENT");

        assertThat(detail.getId()).isEqualTo("AGDF1");
        assertThat(detail.getInterpreterType().toString()).isEqualTo("EVENT_INTERPRETER");
        assertThat(detail.getTriggerType()).isEqualTo("EVENT");
        assertThat(detail.getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(detail.getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(detail.getRuntimeContract()).isNotNull();
        assertThat(detail.getRuntimeContract().getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(detail.getRuntimeContract().getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(detail.getRuntimeContract().getAllowedTools()).isEmpty();
        assertThat(detail.getActivationPolicy()).isInstanceOf(AgentContinuousActivationPolicy.class);
        assertThat(((AgentContinuousActivationPolicy) detail.getActivationPolicy()).getTimezone()).isEqualTo("Europe/Rome");
        assertThat(detail.getProfile()).isNotNull();
        assertThat(detail.getProfile().getId()).isEqualTo("MEDIUM");
        assertThat(detail.getAlertVersion()).isEqualTo(3);
    }

    @Test
    void serializesContinuousActivationPolicyWithSingleTypeAndOnlyContinuousFields() throws Exception {
        AgentDefinitionDetail detail = mapper().toDto(definition(continuousActivationPolicy()), "EVENT_INTERPRETER", "EVENT");

        String json = OBJECT_MAPPER.writeValueAsString(detail);
        JsonNode activationPolicy = OBJECT_MAPPER.readTree(json).get("activationPolicy");

        assertThat(countOccurrences(activationPolicy.toString(), "\"type\"")).isEqualTo(1);
        assertThat(activationPolicy.get("type").asText()).isEqualTo("CONTINUOUS");
        assertThat(activationPolicy.hasNonNull("validFrom")).isTrue();
        assertThat(activationPolicy.hasNonNull("validTo")).isTrue();
        assertThat(activationPolicy.has("validFromDate")).isFalse();
        assertThat(activationPolicy.has("validToDate")).isFalse();
        assertThat(activationPolicy.has("dailyStartTime")).isFalse();
        assertThat(activationPolicy.has("dailyEndTime")).isFalse();
    }

    @Test
    void serializesDailyWindowActivationPolicyWithSingleTypeAndOnlyDailyWindowFields() throws Exception {
        AgentDefinitionDetail detail = mapper().toDto(definition(dailyWindowActivationPolicy()), "SCHEDULED_INTERPRETER", "SCHEDULE");

        String json = OBJECT_MAPPER.writeValueAsString(detail);
        JsonNode activationPolicy = OBJECT_MAPPER.readTree(json).get("activationPolicy");

        assertThat(countOccurrences(activationPolicy.toString(), "\"type\"")).isEqualTo(1);
        assertThat(activationPolicy.get("type").asText()).isEqualTo("DAILY_WINDOW");
        assertThat(activationPolicy.hasNonNull("validFromDate")).isTrue();
        assertThat(activationPolicy.hasNonNull("validToDate")).isTrue();
        assertThat(activationPolicy.get("dailyStartTime").asText()).isEqualTo("07:00:00");
        assertThat(activationPolicy.get("dailyEndTime").asText()).isEqualTo("10:30:00");
        assertThat(activationPolicy.get("daysOfWeek")).hasSize(2);
        assertThat(activationPolicy.has("validFrom")).isFalse();
        assertThat(activationPolicy.has("validTo")).isFalse();
    }

    @Test
    void serializesSummaryActivationPoliciesWithSingleTypeAndOnlyBranchFields() throws Exception {
        AgentDefinitionSummary continuous = mapper().toSummary(definition(continuousActivationPolicy()), "EVENT_INTERPRETER", "EVENT");
        AgentDefinitionSummary dailyWindow = mapper().toSummary(definition(dailyWindowActivationPolicy()), "SCHEDULED_INTERPRETER", "SCHEDULE");

        JsonNode continuousPolicy = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(continuous)).get("activationPolicy");
        JsonNode dailyWindowPolicy = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(dailyWindow)).get("activationPolicy");

        assertThat(countOccurrences(continuousPolicy.toString(), "\"type\"")).isEqualTo(1);
        assertThat(continuousPolicy.get("type").asText()).isEqualTo("CONTINUOUS");
        assertThat(continuousPolicy.hasNonNull("validFrom")).isTrue();
        assertThat(continuousPolicy.hasNonNull("validTo")).isTrue();
        assertThat(continuousPolicy.has("dailyStartTime")).isFalse();
        assertThat(continuousPolicy.has("dailyEndTime")).isFalse();
        assertThat(countOccurrences(dailyWindowPolicy.toString(), "\"type\"")).isEqualTo(1);
        assertThat(dailyWindowPolicy.get("type").asText()).isEqualTo("DAILY_WINDOW");
        assertThat(dailyWindowPolicy.hasNonNull("validFromDate")).isTrue();
        assertThat(dailyWindowPolicy.get("dailyStartTime").asText()).isEqualTo("07:00:00");
        assertThat(dailyWindowPolicy.has("validFrom")).isFalse();
        assertThat(dailyWindowPolicy.has("validTo")).isFalse();
    }

    @Test
    void serializesSummaryAsLightweightGovernanceRowWithoutDetailOnlyFields() throws Exception {
        AgentDefinition definition = definition(continuousActivationPolicy());

        AgentDefinitionSummary summary = mapper().toSummary(definition, "EVENT_INTERPRETER", "EVENT");
        JsonNode json = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(summary));

        assertThat(summary.getId()).isEqualTo("AGDF1");
        assertThat(summary.getName()).isEqualTo("Agent");
        assertThat(summary.getStatus().toString()).isEqualTo("DRAFT");
        assertThat(summary.getAlert().getId()).isEqualTo("ALRT1");
        assertThat(summary.getAlert().getName()).isEqualTo("Verified alert");
        assertThat(summary.getAlertVersion()).isEqualTo(3);
        assertThat(summary.getProfile().getId()).isEqualTo("MEDIUM");
        assertThat(summary.getGenerationMode().toString()).isEqualTo("AUTO");
        assertThat(summary.getActivationPolicy()).isNotNull();
        assertThat(summary.getCreatedAt()).isNotNull();
        assertThat(summary.getUpdatedAt()).isNotNull();
        assertThat(summary.getInterpreterType().toString()).isEqualTo("EVENT_INTERPRETER");
        assertThat(summary.getTriggerType()).isEqualTo("EVENT");
        assertThat(summary.getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(summary.getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(json.has("blueprint")).isFalse();
        assertThat(json.has("artifact")).isFalse();
        assertThat(json.has("runtimeContract")).isFalse();
    }

    @Test
    void mapsScheduledSummaryContractWithoutRuntimeContractPayload() throws Exception {
        AgentDefinition definition = definition(dailyWindowActivationPolicy());
        definition.setDscInputmodel("ServiceDataStopPointJourneysV2");

        AgentDefinitionSummary summary = mapper().toSummary(definition, "SCHEDULED_INTERPRETER", "SCHEDULE");
        JsonNode json = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(summary));

        assertThat(summary.getInterpreterType().toString()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(summary.getTriggerType()).isEqualTo("SCHEDULE");
        assertThat(summary.getInputModel()).isEqualTo("ServiceDataStopPointJourneysV2");
        assertThat(summary.getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(json.has("runtimeContract")).isFalse();
    }

    @Test
    void mapsSummaryActivationPolicyFromStructuredColumnsWhenJsonIsMissing() {
        AgentDefinition definition = definition();
        definition.setJsnActivationpolicy(null);
        definition.setSglActivationtype(activationTypeRef("DAILY_WINDOW"));
        definition.setDscTimezone("Europe/Rome");
        definition.setDValidfromdate(LocalDate.parse("2026-06-12"));
        definition.setDValidtodate(LocalDate.parse("2026-12-31"));
        definition.setTDailystarttime(LocalTime.parse("07:00:00"));
        definition.setTDailyendtime(LocalTime.parse("10:30:00"));

        AgentDefinitionSummary summary = mapper().toSummary(definition, "SCHEDULED_INTERPRETER", "SCHEDULE");

        assertThat(summary.getActivationPolicy()).isInstanceOf(AgentDailyWindowActivationPolicy.class);
        AgentDailyWindowActivationPolicy policy = (AgentDailyWindowActivationPolicy) summary.getActivationPolicy();
        assertThat(policy.getTimezone()).isEqualTo("Europe/Rome");
        assertThat(policy.getValidFromDate()).isEqualTo(LocalDate.parse("2026-06-12"));
        assertThat(policy.getValidToDate()).isEqualTo(LocalDate.parse("2026-12-31"));
        assertThat(policy.getDailyStartTime()).isEqualTo("07:00");
        assertThat(policy.getDailyEndTime()).isEqualTo("10:30");
    }

    @Test
    void handlesSummaryMissingOptionalValuesWithoutNpe() {
        AgentDefinition definition = definition();
        definition.setDscDescription(null);
        definition.setDtUpdatedat(null);
        definition.setCodLatestcompilation(null);
        definition.setCodLatestrun(null);
        definition.setJsnActivationpolicy(null);
        definition.setJsnRuntimecontract(null);
        definition.setJsnAllowedtools(null);

        AgentDefinitionSummary summary = mapper().toSummary(definition, "EVENT_INTERPRETER", "EVENT");

        assertThat(summary.getDescription()).isNull();
        assertThat(summary.getUpdatedAt()).isNull();
        assertThat(summary.getCompilation()).isNull();
        assertThat(summary.getLatestRun()).isNull();
        assertThat(summary.getActivationPolicy()).isInstanceOf(AgentContinuousActivationPolicy.class);
    }

    @Test
    void mapsReadySummaryWithCompilationSummaryWithoutSteps() throws Exception {
        AgentDefinition definition = definition();
        definition.setSglStatus(statusRef("READY"));
        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentcompilation("AGCP1");
        compilation.setSglStatus(compilationStatusRef("READY"));
        compilation.setDscCurrentstep("READY");
        compilation.setDtStartedat(OffsetDateTime.parse("2026-06-12T10:10:00Z"));
        compilation.setDtCompletedat(OffsetDateTime.parse("2026-06-12T10:11:00Z"));
        definition.setCodLatestcompilation(compilation);

        AgentDefinitionSummary summary = mapper().toSummary(definition, "EVENT_INTERPRETER", "EVENT");
        JsonNode json = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(summary));

        assertThat(summary.getStatus().toString()).isEqualTo("READY");
        assertThat(summary.getCompilation()).isNotNull();
        assertThat(summary.getCompilation().getStatus().toString()).isEqualTo("READY");
        assertThat(summary.getCompilation().getCurrentStep()).isEqualTo("READY");
        assertThat(summary.getCompilation().getCompletedAt()).isEqualTo(OffsetDateTime.parse("2026-06-12T10:11:00Z"));
        assertThat(json.get("compilation").has("steps")).isFalse();
    }

    @Test
    void mapsScheduledDailyWindowDetailWithToolAndNoDuplicatedActivationType() throws Exception {
        AgentDefinition definition = definition(dailyWindowActivationPolicy());
        definition.setSglGenerationmode(generationModeRef("DSL"));
        definition.setDscInputmodel("ServiceDataStopPointJourneysV2");
        definition.setJsnAllowedtools(List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));
        definition.setJsnRuntimecontract(Map.of(
                "runtimeExecutionModel", "STANDARD_DSL_EVALUATOR",
                "interpreterType", "SCHEDULED_INTERPRETER",
                "triggerType", "SCHEDULE",
                "inputModel", "ServiceDataStopPointJourneysV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "allowedTools", List.of(Map.of(
                        "toolName", "SERVICE_DATA_API.POST_/v2/stoppointjourneys",
                        "operations", List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"))),
                "networkPolicy", "TOOL_GATEWAY_ONLY"));

        AgentDefinitionDetail detail = mapper().toDto(definition, "SCHEDULED_INTERPRETER", "SCHEDULE");
        String json = OBJECT_MAPPER.writeValueAsString(detail);
        JsonNode activationPolicy = OBJECT_MAPPER.readTree(json).get("activationPolicy");

        assertThat(detail.getGenerationMode().toString()).isEqualTo("DSL");
        assertThat(detail.getActivationPolicy()).isInstanceOf(AgentDailyWindowActivationPolicy.class);
        assertThat(detail.getRuntimeContract().getAllowedTools())
                .extracting(tool -> tool.getToolName())
                .containsExactly("SERVICE_DATA_API.POST_/v2/stoppointjourneys");
        assertThat(countOccurrences(activationPolicy.toString(), "\"type\"")).isEqualTo(1);
        assertThat(activationPolicy.get("daysOfWeek")).hasSize(2);
        assertThat(activationPolicy.has("validFrom")).isFalse();
        assertThat(activationPolicy.has("validTo")).isFalse();
    }

    @Test
    void mapsRuntimeImageAndSdkVersionFromCompiledArtifactMetadata() {
        AgentDefinition definition = definition();
        definition.setSglStatus(statusRef("READY"));
        definition.setSglArtifacttype(artifactTypeRef("DSL"));
        definition.setDscArtifacturi("iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl");
        definition.setDscArtifacthash("sha256:abc");
        definition.setSglSignaturestatus(signatureRef("SIGNED"));
        definition.setDscRuntimeimage("STANDARD_AGENT_DSL_EVALUATOR");
        definition.setDscSdkversion("iia.agent.dsl/v1");

        AgentDefinitionDetail detail = mapper().toDto(definition, "EVENT_INTERPRETER", "EVENT");

        assertThat(detail.getArtifact()).isNotNull();
        assertThat(detail.getArtifact().getRuntimeImage()).isEqualTo("STANDARD_AGENT_DSL_EVALUATOR");
        assertThat(detail.getArtifact().getSdkVersion()).isEqualTo("iia.agent.dsl/v1");
        assertThat(detail.getRuntimeContract().getRuntimeImage()).isEqualTo("STANDARD_AGENT_DSL_EVALUATOR");
        assertThat(detail.getRuntimeContract().getSdkVersion()).isEqualTo("iia.agent.dsl/v1");
    }

    @Test
    void mapsRuntimeImageAndSdkVersionFallbackForDslArtifact() {
        AgentDefinition definition = definition();
        definition.setSglArtifacttype(artifactTypeRef("DSL"));
        definition.setDscRuntimeimage(null);
        definition.setDscSdkversion(null);

        AgentDefinitionDetail detail = mapper().toDto(definition, "EVENT_INTERPRETER", "EVENT");

        assertThat(detail.getRuntimeContract().getRuntimeImage()).isEqualTo("STANDARD_AGENT_DSL_EVALUATOR");
        assertThat(detail.getRuntimeContract().getSdkVersion()).isEqualTo("iia.agent.dsl/v1");
    }

    @Test
    void handlesMissingOptionalJsonWithoutNpeAndUsesStructuredFallbacks() {
        AgentDefinition definition = definition();
        definition.setJsnActivationpolicy(null);
        definition.setJsnBlueprint(null);
        definition.setJsnRuntimecontract(null);
        definition.setJsnAllowedtools(null);
        definition.setJsnRequiredsources(null);

        AgentDefinitionDetail detail = mapper().toDto(definition, "EVENT_INTERPRETER", "EVENT");

        assertThat(detail.getActivationPolicy()).isNull();
        assertThat(detail.getBlueprint()).isNull();
        assertThat(detail.getRuntimeContract()).isNotNull();
        assertThat(detail.getRuntimeContract().getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(detail.getRuntimeContract().getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(detail.getRuntimeContract().getAllowedTools()).isEmpty();
        assertThat(detail.getCompilation()).isNull();
        assertThat(detail.getLatestRun()).isNull();
    }

    @Test
    void mapsLatestCompilationSummaryWhenPresent() {
        AgentDefinition definition = definition();
        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentcompilation("AGCP1");
        compilation.setSglStatus(compilationStatusRef("FAILED"));
        compilation.setDscCurrentstep("STATIC_ANALYSIS");
        compilation.setDscErrormessage("Compilation failed.");
        compilation.setDtStartedat(OffsetDateTime.parse("2026-06-12T10:10:00Z"));
        compilation.setDtCompletedat(OffsetDateTime.parse("2026-06-12T10:11:00Z"));
        definition.setCodLatestcompilation(compilation);

        AgentDefinitionDetail detail = mapper().toDto(definition, "EVENT_INTERPRETER", "EVENT");

        assertThat(detail.getCompilation()).isNotNull();
        assertThat(detail.getCompilation().getAgentDefinitionId()).isEqualTo("AGDF1");
        assertThat(detail.getCompilation().getStatus().toString()).isEqualTo("FAILED");
        assertThat(detail.getCompilation().getCurrentStep()).isEqualTo("STATIC_ANALYSIS");
        assertThat(detail.getCompilation().getErrors()).containsExactly("Compilation failed.");
    }

    @Test
    void mapsLatestRunSummaryWhenPresent() {
        AgentDefinition definition = definition();
        AgentRun run = new AgentRun();
        run.setCodAgentrun("AGRN1");
        run.setSglStatus(runStatusRef("RUNNING"));
        run.setSglHealthstatus(healthStatusRef("HEALTHY"));
        run.setNumHealthscore(91);
        run.setSglQualitystatus(qualityStatusRef("GOOD"));
        run.setNumQualityscore(84);
        run.setCodProfile("MEDIUM");
        run.setNumCpuusagepercent(new BigDecimal("12.5"));
        run.setNumMemoryusagepercent(new BigDecimal("44.2"));
        run.setNumCandidateoutputs(7L);
        run.setNumCreatedsuggestions(3L);
        run.setDtStartedat(OffsetDateTime.parse("2026-06-12T10:20:00Z"));
        run.setDtCreatedat(OffsetDateTime.parse("2026-06-12T10:19:00Z"));
        definition.setCodLatestrun(run);

        AgentDefinitionDetail detail = mapper().toDto(definition, "EVENT_INTERPRETER", "EVENT");

        assertThat(detail.getLatestRun()).isNotNull();
        assertThat(detail.getLatestRun().getId()).isEqualTo("AGRN1");
        assertThat(detail.getLatestRun().getAgentDefinitionId()).isEqualTo("AGDF1");
        assertThat(detail.getLatestRun().getStatus().toString()).isEqualTo("RUNNING");
        assertThat(detail.getLatestRun().getGeneratedOutputs()).isEqualTo(7L);
        assertThat(detail.getLatestRun().getCreatedSuggestions()).isEqualTo(3L);
    }

    private AgentDefinitionMapper mapper() {
        AgentDefinitionMapper mapper = new AgentDefinitionMapper();
        mapper.agentProfileMapper = new AgentProfileMapper();
        return mapper;
    }

    private int countOccurrences(String value, String needle) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private AgentDefinition definition() {
        return definition(continuousActivationPolicy());
    }

    private AgentDefinition definition(Map<String, Object> activationPolicy) {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        definition.setDscName("Agent");
        definition.setDscDescription("Agent description");
        definition.setCodAlert(alert());
        definition.setNumAlertversion(3);
        definition.setCodAgentprofile(profile());
        definition.setSglStatus(statusRef("DRAFT"));
        definition.setSglGenerationmode(generationModeRef("AUTO"));
        definition.setJsnActivationpolicy(activationPolicy);
        definition.setJsnBlueprint(Map.of("name", "Agent"));
        definition.setSglActivationtype(activationTypeRef("CONTINUOUS"));
        definition.setSglArtifacttype(artifactTypeRef("NONE"));
        definition.setSglSignaturestatus(signatureRef("NOT_SIGNED"));
        definition.setDscInputmodel("ServiceDataV2");
        definition.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        definition.setJsnRuntimecontract(Map.of(
                "runtimeExecutionModel", "STANDARD_DSL_EVALUATOR",
                "interpreterType", "EVENT_INTERPRETER",
                "triggerType", "EVENT",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "allowedTools", List.of(),
                "networkPolicy", "TOOL_GATEWAY_ONLY",
                "forbiddenCapabilities", List.of(
                        "ARBITRARY_CODE_EXECUTION",
                        "EXTERNAL_HTTP",
                        "DB_QUERY",
                        "FILESYSTEM",
                        "SHELL"),
                "orchestratorCompatibility", Map.of(
                        "minimumRuntimeVersion", "1.0.0",
                        "runtimeClass", "STANDARD_DSL_RUNTIME")));
        OffsetDateTime now = OffsetDateTime.parse("2026-06-12T10:00:00Z");
        definition.setDtCreatedat(now);
        definition.setDtUpdatedat(now);
        return definition;
    }

    private Map<String, Object> continuousActivationPolicy() {
        return Map.of(
                "type", "CONTINUOUS",
                "timezone", "Europe/Rome",
                "validFrom", "2026-06-12T10:00:00+02:00",
                "validTo", "2026-12-31T23:59:59+01:00");
    }

    private Map<String, Object> dailyWindowActivationPolicy() {
        return Map.of(
                "type", "DAILY_WINDOW",
                "timezone", "Europe/Rome",
                "validFromDate", "2026-06-12",
                "validToDate", "2026-12-31",
                "dailyStartTime", "07:00:00",
                "dailyEndTime", "10:30:00",
                "daysOfWeek", List.of("MONDAY", "TUESDAY"));
    }

    private Alert alert() {
        Alert alert = new Alert();
        alert.setCodAlert("ALRT1");
        alert.setDscName("Verified alert");
        return alert;
    }

    private AgentProfile profile() {
        AgentProfile profile = new AgentProfile();
        profile.setCodAgentprofile("MEDIUM");
        profile.setDscName("Medium Agent");
        profile.setFlgEnabled(true);
        profile.setJsnRecommendedfor(List.of("Agent definitions"));
        profile.setNumCpurequestmillicores(250);
        profile.setNumCpulimitmillicores(700);
        profile.setNumMemoryrequestmib(256);
        profile.setNumMemorylimitmib(768);
        profile.setDscNetworkpolicy("TOOL_GATEWAY_ONLY");
        profile.setNumMaxruntimeconcurrency(1);
        return profile;
    }

    private AgentDefinitionStatus statusRef(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentGenerationMode generationModeRef(String value) {
        AgentGenerationMode mode = new AgentGenerationMode();
        mode.setSglGenerationmode(value);
        return mode;
    }

    private AgentActivationType activationTypeRef(String value) {
        AgentActivationType type = new AgentActivationType();
        type.setSglActivationtype(value);
        return type;
    }

    private AgentArtifactType artifactTypeRef(String value) {
        AgentArtifactType type = new AgentArtifactType();
        type.setSglArtifacttype(value);
        return type;
    }

    private AgentArtifactSignatureStatus signatureRef(String value) {
        AgentArtifactSignatureStatus status = new AgentArtifactSignatureStatus();
        status.setSglSignaturestatus(value);
        return status;
    }

    private AgentCompilationStatus compilationStatusRef(String value) {
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentRunStatus runStatusRef(String value) {
        AgentRunStatus status = new AgentRunStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentHealthStatus healthStatusRef(String value) {
        AgentHealthStatus status = new AgentHealthStatus();
        status.setSglHealthstatus(value);
        return status;
    }

    private AgentQualityStatus qualityStatusRef(String value) {
        AgentQualityStatus status = new AgentQualityStatus();
        status.setSglQualitystatus(value);
        return status;
    }
}
