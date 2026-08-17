package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType;

import java.util.List;
import java.util.Map;

final class AgentCompilationTestFixtures {

    private AgentCompilationTestFixtures() {
    }

    static AgentDefinition eventDefinition() {
        AgentDefinition definition = baseDefinition("EVENT_INTERPRETER", "ServiceDataV2");
        definition.setJsnAllowedtools(List.of());
        definition.setJsnRuntimecontract(Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "EVENT_INTERPRETER",
                "triggerType", "EVENT",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "executionModel", "KAFKA_EVENT",
                "requiresScheduler", false,
                "allowedTools", List.of()));
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

    static AgentDefinition scheduledDefinition() {
        return scheduledDefinition("REPORT_COUNT");
    }

    static AgentDefinition scheduledDefinition(String mode) {
        AgentDefinition definition = baseDefinition("SCHEDULED_INTERPRETER", "ServiceDataStopPointJourneysV2");
        definition.setJsnAllowedtools(List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));
        definition.setJsnRuntimecontract(Map.of(
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
                        "operations", List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys")))));
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "field", "arrivalStatuses[].status",
                                "operator", "CONTAINS",
                                "value", "ARRIVAL_CANCELLATION")));
        Map<String, Object> snapshotEvaluation = new java.util.LinkedHashMap<>();
        snapshotEvaluation.put("mode", mode);
        snapshotEvaluation.put("journeyPath", "stopPointsJourneyDetails[]");
        snapshotEvaluation.put("condition", condition);
        if ("COUNT_MATCHING_JOURNEYS".equals(mode)) {
            snapshotEvaluation.put("threshold", Map.of("operator", "GREATER_OR_EQUAL", "value", 3));
        }
        String emit = "REPORT_COUNT".equals(mode) ? "EVERY_RUN" : "ON_MATCH";
        definition.setJsnBlueprint(Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "triggerType", "SCHEDULE",
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "parameters", Map.of(
                        "serviceDataQuery", Map.of("operation", "POST /v2/stoppointjourneys"),
                        "snapshotEvaluation", snapshotEvaluation,
                        "outputPolicy", Map.of(
                                "emit", emit,
                                "includeCount", true,
                                "includeMatchingJourneys", true),
                        "schedule", Map.of("frequencySeconds", 600))));
        return definition;
    }

    static AgentDefinition conditionlessScheduledReportDefinition() {
        AgentDefinition definition = scheduledDefinition("REPORT_COUNT");
        Map<String, Object> blueprint = new java.util.LinkedHashMap<>(definition.getJsnBlueprint());
        @SuppressWarnings("unchecked")
        Map<String, Object> originalParameters = (Map<String, Object>) blueprint.get("parameters");
        Map<String, Object> parameters = new java.util.LinkedHashMap<>(originalParameters);
        @SuppressWarnings("unchecked")
        Map<String, Object> originalSnapshot = (Map<String, Object>) parameters.get("snapshotEvaluation");
        Map<String, Object> snapshotEvaluation = new java.util.LinkedHashMap<>(originalSnapshot);
        snapshotEvaluation.remove("condition");
        parameters.put("snapshotEvaluation", snapshotEvaluation);
        parameters.put("outputPolicy", Map.of(
                "emit", "EVERY_RUN",
                "includeCount", true,
                "includeMatchingJourneys", false));
        parameters.put("schedule", Map.of("frequencySeconds", 10));
        blueprint.put("parameters", parameters);
        definition.setJsnBlueprint(blueprint);
        return definition;
    }

    private static AgentDefinition baseDefinition(String interpreterTypeValue, String inputModel) {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        definition.setDscName("Compiled Agent");
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus("DRAFT");
        definition.setSglStatus(status);
        AgentGenerationMode generationMode = new AgentGenerationMode();
        generationMode.setSglGenerationmode("AUTO");
        definition.setSglGenerationmode(generationMode);
        AgentArtifactType artifactType = new AgentArtifactType();
        artifactType.setSglArtifacttype("NONE");
        definition.setSglArtifacttype(artifactType);
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
        definition.setDscInputmodel(inputModel);
        definition.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        return definition;
    }
}
