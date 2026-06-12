package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

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

    private AgentDefinition definition() {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        definition.setDscName("Agent");
        definition.setDscDescription("Agent description");
        definition.setCodAlert(alert());
        definition.setNumAlertversion(3);
        definition.setCodAgentprofile(profile());
        definition.setSglStatus(statusRef("DRAFT"));
        definition.setSglGenerationmode(generationModeRef("AUTO"));
        definition.setJsnActivationpolicy(Map.of(
                "type", "CONTINUOUS",
                "timezone", "Europe/Rome",
                "validFrom", "2026-06-12T10:00:00+02:00",
                "validTo", "2026-12-31T23:59:59+01:00"));
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
