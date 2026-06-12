package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentContinuousActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import org.junit.jupiter.api.Test;

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
}
