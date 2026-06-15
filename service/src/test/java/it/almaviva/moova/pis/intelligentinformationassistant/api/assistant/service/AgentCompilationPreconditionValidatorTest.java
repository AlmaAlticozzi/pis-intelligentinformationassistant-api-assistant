package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentCompilationPreconditionValidatorTest {

    private final AgentCompilationPreconditionValidator validator = new AgentCompilationPreconditionValidator();

    @Test
    void validatesEventAgentPreconditions() {
        AgentDefinition definition = eventDefinition();

        AgentCompilationPreconditionValidationResult result = validator.validate(definition, "DSL");

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.interpreterType()).isEqualTo("EVENT_INTERPRETER");
        assertThat(result.triggerType()).isEqualTo("EVENT");
        assertThat(result.inputModel()).isEqualTo("ServiceDataV2");
        assertThat(result.evaluationMode()).isEqualTo("STATELESS_EVENT_MATCH");
    }

    @Test
    void validatesScheduledAgentPreconditions() {
        AgentDefinition definition = scheduledDefinition();

        AgentCompilationPreconditionValidationResult result = validator.validate(definition, "DSL");

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(result.triggerType()).isEqualTo("SCHEDULE");
        assertThat(result.inputModel()).isEqualTo("ServiceDataStopPointJourneysV2");
        assertThat(result.evaluationMode()).isEqualTo("SCHEDULED_SNAPSHOT_MATCH");
    }

    @Test
    void rejectsMissingBlueprint() {
        AgentDefinition definition = eventDefinition();
        definition.setJsnBlueprint(null);

        AgentCompilationPreconditionValidationResult result = validator.validate(definition, "DSL");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Agent Definition blueprint is missing; compilation cannot continue.");
    }

    @Test
    void rejectsMissingRuntimeContract() {
        AgentDefinition definition = eventDefinition();
        definition.setJsnRuntimecontract(null);

        AgentCompilationPreconditionValidationResult result = validator.validate(definition, "DSL");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Agent Definition runtimeContract is missing; compilation cannot continue.");
    }

    @Test
    void rejectsEventWithAllowedTools() {
        AgentDefinition definition = eventDefinition();
        definition.setJsnAllowedtools(List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));

        AgentCompilationPreconditionValidationResult result = validator.validate(definition, "DSL");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("EVENT_INTERPRETER Agent cannot require external tools in the DSL MVP.");
    }

    @Test
    void rejectsScheduledWithoutServiceDataTool() {
        AgentDefinition definition = scheduledDefinition();
        definition.setJsnAllowedtools(List.of("OTHER_TOOL"));
        Map<String, Object> runtimeContract = new LinkedHashMap<>(definition.getJsnRuntimecontract());
        runtimeContract.put("allowedTools", List.of("OTHER_TOOL"));
        definition.setJsnRuntimecontract(runtimeContract);

        AgentCompilationPreconditionValidationResult result = validator.validate(definition, "DSL");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("SCHEDULED_INTERPRETER Agent requires SERVICE_DATA_API.POST_/v2/stoppointjourneys tool access.");
    }

    @Test
    void rejectsUnsupportedContract() {
        AgentDefinition definition = eventDefinition();
        definition.setDscInputmodel("UnknownModel");

        AgentCompilationPreconditionValidationResult result = validator.validate(definition, "DSL");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Unsupported Agent runtime contract: interpreterType=EVENT_INTERPRETER, triggerType=EVENT, inputModel=UnknownModel, evaluationMode=STATELESS_EVENT_MATCH.");
    }

    private AgentDefinition eventDefinition() {
        AgentDefinition definition = baseDefinition("EVENT_INTERPRETER");
        definition.setDscInputmodel("ServiceDataV2");
        definition.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        definition.setJsnAllowedtools(List.of());
        definition.setJsnRuntimecontract(new LinkedHashMap<>(Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "EVENT_INTERPRETER",
                "triggerType", "EVENT",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "executionModel", "KAFKA_EVENT",
                "requiresScheduler", false,
                "allowedTools", List.of())));
        definition.setJsnBlueprint(Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of(
                        "condition", Map.of(
                                "type", "SERVICE_DATA_FIELD_MATCH",
                                "field", "payload.status",
                                "operator", "EQUALS",
                                "value", "ARRIVING"))));
        return definition;
    }

    private AgentDefinition scheduledDefinition() {
        AgentDefinition definition = baseDefinition("SCHEDULED_INTERPRETER");
        definition.setDscInputmodel("ServiceDataStopPointJourneysV2");
        definition.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        definition.setJsnAllowedtools(List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));
        definition.setJsnRuntimecontract(new LinkedHashMap<>(Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "SCHEDULED_INTERPRETER",
                "triggerType", "SCHEDULE",
                "inputModel", "ServiceDataStopPointJourneysV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "executionModel", "SCHEDULED_POLLING",
                "accessMode", "SERVICE_DATA_API_SNAPSHOT",
                "requiresScheduler", true,
                "allowedTools", List.of(Map.of(
                        "toolName", "SERVICE_DATA_API.POST_/v2/stoppointjourneys",
                        "operations", List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"))))));
        definition.setJsnBlueprint(Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "triggerType", "SCHEDULE",
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "parameters", Map.of(
                        "serviceDataQuery", Map.of("operation", "POST /v2/stoppointjourneys"),
                        "snapshotEvaluation", Map.of("mode", "REPORT_COUNT"),
                        "outputPolicy", Map.of("emit", "ON_MATCH"),
                        "schedule", Map.of("frequencySeconds", 600))));
        return definition;
    }

    private AgentDefinition baseDefinition(String interpreterTypeValue) {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        definition.setDscName("Compiled Agent");
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus("DRAFT");
        definition.setSglStatus(status);
        AgentGenerationMode generationMode = new AgentGenerationMode();
        generationMode.setSglGenerationmode("AUTO");
        definition.setSglGenerationmode(generationMode);
        Alert alert = new Alert();
        alert.setCodAlert("ALRT1");
        AlertInterpreterType interpreterType = new AlertInterpreterType();
        interpreterType.setSglInterpretertype(interpreterTypeValue);
        alert.setSglInterpretertype(interpreterType);
        definition.setCodAlert(alert);
        definition.setNumAlertversion(1);
        AgentProfile profile = new AgentProfile();
        profile.setCodAgentprofile("MEDIUM");
        profile.setFlgEnabled(true);
        definition.setCodAgentprofile(profile);
        AgentActivationType activationType = new AgentActivationType();
        activationType.setSglActivationtype("CONTINUOUS");
        definition.setSglActivationtype(activationType);
        definition.setJsnActivationpolicy(Map.of("type", "CONTINUOUS", "timezone", "Europe/Rome"));
        return definition;
    }
}
