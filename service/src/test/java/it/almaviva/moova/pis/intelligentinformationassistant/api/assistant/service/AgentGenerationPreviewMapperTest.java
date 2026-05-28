package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentGenerationPreviewMapperTest {

    private final AgentGenerationPreviewMapper mapper = new AgentGenerationPreviewMapper();

    @Test
    void defaultRequestIncludesDslAndValidationPlanUsingPersistedArtifacts() {
        AgentGenerationPreviewResponse response = mapper.toResponse(previewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getRecommendedGenerationMode()).isEqualTo(AgentGenerationMode.DSL);
        assertThat(response.getRequiredSources()).containsExactly(AgentDataSource.SERVICE_DATA);
        assertThat(response.getRequiredPermissions()).containsExactly("READ_SERVICE_DATA");
        assertThat(response.getBlueprint().getAgentName()).isEqualTo("CancelledJourneyServiceDataAgent");
        assertThat(response.getBlueprint().getTargetTypes()).containsExactly(SuggestionTargetType.SERVICE_DATA_JOURNEY);
        assertThat(response.getDslPreview().getDsl()).contains("EQUALS", "STATELESS_EVENT_MATCH");
        assertThat(response.getValidationPlan().getPositiveExamples()).hasSize(1);
        assertThat(response.getWarnings()).contains(
                "Read-only preview generated from verified Alert artifacts; no Agent Definition has been created.",
                "DSL preview is diagnostic and has not been compiled or executed.");
        assertThat(metadata(response, "generationContext"))
                .containsEntry("sourceAlertId", "ALRT1")
                .containsEntry("sourceAlertVersion", 1)
                .containsEntry("sourceAlertStatus", "VERIFIED")
                .containsEntry("verificationStatus", "VERIFIED")
                .containsEntry("generationMode", "DSL")
                .containsEntry("previewSource", "DETERMINISTIC");
        assertThat(metadata(response, "runtimeContract"))
                .containsEntry("schemaVersion", "iia.agent.runtime-contract/v1")
                .containsEntry("executionModel", "EVENT_DRIVEN")
                .containsEntry("triggerType", "EVENT")
                .containsEntry("source", "SERVICE_DATA")
                .containsEntry("inputModel", "ServiceDataV2")
                .containsEntry("outputModel", "AgentOutput.CANDIDATE_SUGGESTION")
                .containsEntry("evaluationMode", "STATELESS_EVENT_MATCH")
                .containsEntry("requiresState", false)
                .containsEntry("requiresExternalTools", false)
                .containsEntry("requiresNetworkAccess", false)
                .containsEntry("requiresFilesystemAccess", false)
                .containsEntry("requiredPermissions", List.of("READ_SERVICE_DATA"))
                .containsEntry("allowedSources", List.of("SERVICE_DATA"));
        assertThat(metadata(response, "generationReadiness"))
                .containsEntry("readyForAgentDefinition", true)
                .containsEntry("recommendedNextStep", "CREATE_AGENT_DEFINITION")
                .containsEntry("requiresHumanReview", true)
                .containsEntry("requiresCompilation", true)
                .containsEntry("requiresRuntimeActivation", true);
    }

    @Test
    void javaTemplatePreferenceDoesNotGenerateJavaAndOptionalSectionsCanBeExcluded() {
        AgentGenerationPreviewRequest request = new AgentGenerationPreviewRequest()
                .preferredGenerationMode(AgentGenerationMode.JAVA_TEMPLATE)
                .includeDslPreview(false)
                .includeValidationPlan(false);

        AgentGenerationPreviewResponse response = mapper.toResponse(previewData(), request);

        assertThat(response.getRecommendedGenerationMode()).isEqualTo(AgentGenerationMode.DSL);
        assertThat(response.getDslPreview()).isNull();
        assertThat(response.getValidationPlan()).isNull();
        assertThat(response.getWarnings()).anyMatch(warning -> warning.contains("JAVA_TEMPLATE"));
    }

    @Test
    void cancellationConditionProducesSpecificBlueprintDslAndValidationPlan() {
        AgentGenerationPreviewResponse response = mapper.toResponse(cancellationPreviewData(), null);

        assertThat(response.getBlueprint().getAgentName()).isEqualTo("JourneyCancellationMilanoMalpensaT1Agent");
        assertThat(response.getBlueprint().getDescription())
                .isEqualTo("Detects cancelled journeys at Milano Malpensa T1 from realtime ServiceData events.");
        assertThat(response.getBlueprint().getSuggestionIntent())
                .containsEntry("type", "INFORM_OPERATOR")
                .containsEntry("category", "JOURNEY_CANCELLATION")
                .containsEntry("candidateOutput", "CANDIDATE_SUGGESTION")
                .containsEntry("operatorAction", "CHECK_PASSENGER_INFORMATION_PROCEDURES");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getSummary())
                .isEqualTo("Deterministic DSL preview for a stateless ServiceData event-match Agent.");
        assertThat(response.getDslPreview().getDsl())
                .contains("agent:\n  name: JourneyCancellationMilanoMalpensaT1Agent")
                .contains("match:\n  evaluationMode: STATELESS_EVENT_MATCH\n  conditionType: SERVICE_DATA_FIELD_MATCH\n  all:\n    - any:\n        - field:")
                .contains("      operator: EQUALS_NORMALIZED\n      value: Milano Malpensa T1")
                .contains("runtime:\n  requiresState: false\n  supportedByRuntime: true");
        assertThat(response.getValidationPlan().getPositiveExamples()).hasSize(2);
        assertThat(response.getValidationPlan().getNegativeExamples()).hasSize(2);
        assertThat(response.getValidationPlan().getEdgeCases()).hasSize(3);
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("ARRIVAL_CANCELLATION", "Milano Malpensa T1");
    }

    @Test
    void unsupportedConditionNodeIsRejectedAsUnprocessableBlueprint() {
        Map<String, Object> technicalSpecification = new LinkedHashMap<>(previewData().technicalSpecification());
        technicalSpecification.put("condition", Map.of("unsupportedExpression", "custom"));
        Map<String, Object> blueprint = new LinkedHashMap<>(previewData().agentBlueprintPreview());
        blueprint.put("parameters", Map.of(
                "conditionType", "SERVICE_DATA_FIELD_MATCH",
                "condition", Map.of("unsupportedExpression", "custom")));
        AlertAgentGenerationPreviewData data = dataWithArtifacts(
                technicalSpecification,
                blueprint);

        assertThatThrownBy(() -> mapper.toResponse(data, null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.INVALID_BLUEPRINT);
    }

    @Test
    void unsupportedArtifactSourceIsRejectedAsUnprocessableBlueprint() {
        Map<String, Object> blueprint = new LinkedHashMap<>(cancellationPreviewData().agentBlueprintPreview());
        blueprint.put("requiredSources", List.of("MONITORED_AUDIO_MESSAGE"));

        assertThatThrownBy(() -> mapper.toResponse(
                dataWithArtifacts(cancellationPreviewData().technicalSpecification(), blueprint),
                null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.INVALID_BLUEPRINT);
    }

    @Test
    void llmCandidateWithUnsupportedSourceStillPassesThroughStrictValidation() {
        Map<String, Object> blueprint = new LinkedHashMap<>(cancellationPreviewData().agentBlueprintPreview());
        blueprint.put("requiredSources", List.of("EXTERNAL_HTTP"));

        assertThatThrownBy(() -> mapper.toResponse(
                cancellationPreviewData(),
                null,
                blueprint,
                List.of("Agent Blueprint preview generated by LLM and validated by backend; no Agent Definition has been created.")))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.INVALID_BLUEPRINT);
    }

    @Test
    void containsAnyValuesProduceCompleteRuntimeSupportedDslAndSpecificNaming() {
        AgentGenerationPreviewResponse response = mapper.toResponse(genovaContainsAnyPreviewData(), null);

        assertThat(response.getBlueprint().getAgentName()).isEqualTo("JourneyCancellationGenovaPiazzaPrincipeAgent");
        assertThat(response.getBlueprint().getDescription())
                .isEqualTo("Detects cancelled journeys at Genova Piazza Principe from realtime ServiceData events.");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("    - any:\n        - field: payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status")
                .contains("          operator: CONTAINS_ANY\n          values:\n            - ARRIVAL_CANCELLATION")
                .contains("    - any:\n        - field: payload.stopPointJourney.stopPoint.nameLong")
                .doesNotContain("    - any:\n    - any:");
        assertThat(response.getValidationPlan().getPositiveExamples()).hasSize(2);
        assertThat(response.getWarnings())
                .doesNotContain("DSL preview is partial because some condition nodes are not supported by the deterministic renderer.");
    }

    @Test
    void delayIntercityConditionProducesSpecificPreviewAndRendersContainsIgnoreCaseValue() {
        AgentGenerationPreviewResponse response = mapper.toResponse(intercityDelayPreviewData(false), null);

        assertThat(response.getBlueprint().getAgentName()).isEqualTo("JourneyDelayIntercityGenovaNerviAgent");
        assertThat(response.getBlueprint().getDescription())
                .isEqualTo("Detects delayed Intercity journeys at Genova Nervi from realtime ServiceData events.");
        assertThat(response.getBlueprint().getSuggestionIntent())
                .containsEntry("type", "INFORM_OPERATOR")
                .containsEntry("category", "JOURNEY_DELAY")
                .containsEntry("candidateOutput", "CANDIDATE_SUGGESTION")
                .containsEntry("operatorAction", "CHECK_DELAY_AND_PASSENGER_INFORMATION_PROCEDURES");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("operator: CONTAINS_IGNORE_CASE\n      value: DELAYED");
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("Intercity", "delayed", "Genova Nervi");
        assertThat(response.getValidationPlan().getNegativeExamples()).hasSize(3);
        assertThat(response.getWarnings())
                .doesNotContain("DSL preview is partial because some condition nodes are not supported by the deterministic renderer.");
    }

    @Test
    void delayDslRendersContainsIgnoreCaseValuesWithoutBecomingPartial() {
        AgentGenerationPreviewResponse response = mapper.toResponse(intercityDelayPreviewData(true), null);

        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("operator: CONTAINS_IGNORE_CASE\n      values:\n        - DELAYED");
    }

    @Test
    void verifiedIntercityNumericDelayArtifactsProduceCompletePreview() {
        AgentGenerationPreviewResponse response = mapper.toResponse(verifiedIntercityNumericDelayPreviewData(), null);

        assertThat(response.getBlueprint().getAgentName()).isEqualTo("JourneyDelayIntercityGenovaNerviAgent");
        assertThat(response.getBlueprint().getDescription())
                .isEqualTo("Detects delayed Intercity journeys at Genova Nervi from realtime ServiceData events.");
        assertThat(response.getBlueprint().getSuggestionIntent())
                .containsEntry("category", "JOURNEY_DELAY")
                .containsEntry("operatorAction", "CHECK_DELAY_AND_PASSENGER_INFORMATION_PROCEDURES");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("operator: CONTAINS_NORMALIZED\n      value: intercity")
                .contains("operator: GREATER_THAN\n          value: 0")
                .contains("operator: CONTAINS_ANY\n          values:\n            - ARRIVAL_DELAY");
        assertThat(response.getValidationPlan().getPositiveExamples()).hasSize(2);
        assertThat(response.getValidationPlan().getNegativeExamples()).hasSize(3);
        assertThat(response.getWarnings())
                .doesNotContain("DSL preview is partial because some condition nodes are not supported by the deterministic renderer.");
    }

    @Test
    void temporalNextCallsConditionProducesRuntimeSupportedCorrelatedPreview() {
        AgentGenerationPreviewResponse response = mapper.toResponse(temporalNextCallsPreviewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("operator: LOCAL_TIME_BETWEEN")
                .contains("- anyElement:\n      path: payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]")
                .contains("field: stopPoint.nameLong\n          operator: EQUALS_NORMALIZED\n          value: Gorla")
                .contains("field: departureTime\n          operator: LOCAL_TIME_BETWEEN")
                .contains("start: \"11:30:00\"\n            end: \"12:35:00\"\n            timezone: Europe/Rome")
                .contains("supportedByRuntime: true");
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("same nextCall", "Gorla", "departureTime", "11:30:00-12:35:00");
        assertThat(response.getValidationPlan().getNegativeExamples())
                .extracting(example -> example.getDescription())
                .anyMatch(description -> description.contains("Gorla") && description.contains("outside"))
                .anyMatch(description -> description.contains("different from Gorla"));
        assertThat(response.getValidationPlan().getEdgeCases())
                .anyMatch(description -> description.contains("different nextCalls"));
    }

    @Test
    void temporalDayOfWeekAndTimeConditionProducesRuntimeSupportedPreviewAndPlan() {
        AgentGenerationPreviewResponse response = mapper.toResponse(temporalDayOfWeekPreviewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("operator: LOCAL_TIME_BETWEEN")
                .contains("operator: LOCAL_DAY_OF_WEEK_NOT_IN")
                .contains("days:\n              - SATURDAY\n              - SUNDAY")
                .contains("timezone: Europe/Rome")
                .contains("supportedByRuntime: true");
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("11:20:00-11:25:00", "not included in [SATURDAY, SUNDAY]");
        assertThat(response.getValidationPlan().getNegativeExamples())
                .extracting(example -> example.getDescription())
                .anyMatch(description -> description.contains("outside 11:20:00-11:25:00")
                        || description.contains("excluded by [SATURDAY, SUNDAY]"));
    }

    @Test
    void validatedLlmDelayCandidateNormalizesIntentAndOmitsDeterministicWarning() {
        AlertAgentGenerationPreviewData data = verifiedIntercityNumericDelayPreviewData();
        Map<String, Object> generatedBlueprint = new LinkedHashMap<>(data.agentBlueprintPreview());
        generatedBlueprint.put("suggestionIntent", Map.of(
                "type", "INFORM_OPERATOR",
                "category", "OPERATIONAL_ALERT",
                "candidateOutput", "CANDIDATE_SUGGESTION",
                "operatorAction", "CHECK_PASSENGER_INFORMATION_PROCEDURES"));

        AgentGenerationPreviewResponse response = mapper.toResponse(
                data,
                null,
                generatedBlueprint,
                List.of(AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING));

        assertThat(response.getBlueprint().getSuggestionIntent())
                .containsEntry("category", "JOURNEY_DELAY")
                .containsEntry("operatorAction", "CHECK_DELAY_AND_PASSENGER_INFORMATION_PROCEDURES");
        assertThat(response.getWarnings())
                .containsExactly(
                        AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING,
                        AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING)
                .doesNotContain(AgentGenerationPreviewMapper.DETERMINISTIC_PREVIEW_WARNING);
        assertThat(metadata(response, "generationContext"))
                .containsEntry("previewSource", "LLM_VALIDATED");
    }

    @Test
    void deterministicFallbackKeepsFallbackReadOnlyAndDiagnosticWarnings() {
        String fallbackWarning =
                "LLM Agent Blueprint generation failed or was rejected by backend validation; deterministic verified Alert artifacts were used instead.";

        AgentGenerationPreviewResponse response = mapper.toResponse(
                verifiedIntercityNumericDelayPreviewData(),
                null,
                null,
                List.of(fallbackWarning));

        assertThat(response.getWarnings()).containsExactly(
                fallbackWarning,
                AgentGenerationPreviewMapper.DETERMINISTIC_PREVIEW_WARNING,
                AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING);
        assertThat(metadata(response, "generationContext"))
                .containsEntry("previewSource", "LLM_FALLBACK");
    }

    @Test
    void validatedLlmPlatformChangeAnyLocationProducesSpecificReadOnlyPreview() {
        AlertAgentGenerationPreviewData data = platformChangeAnyLocationPreviewData();

        AgentGenerationPreviewResponse response = mapper.toResponse(
                data,
                null,
                data.agentBlueprintPreview(),
                List.of(AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING));

        assertThat(response.getBlueprint().getAgentName()).isEqualTo("PlatformChangeAnyLocationAgent");
        assertThat(response.getBlueprint().getDescription())
                .isEqualTo("Detects train platform changes at any location from realtime ServiceData events.");
        assertThat(response.getBlueprint().getSuggestionIntent())
                .containsEntry("category", "PLATFORM_CHANGE")
                .containsEntry("operatorAction", "CHECK_PLATFORM_CHANGE_AND_PASSENGER_INFORMATION_PROCEDURES");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("operator: CONTAINS\n      value: DEPARTURE_PLATFORM_CHANGED")
                .contains("operator: CONTAINS\n      value: ARRIVAL_PLATFORM_CHANGED")
                .contains("reasonTemplate: Train ${payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName} received a platform change at ${payload.stopPointJourney.stopPoint.nameLong}.")
                .contains("operatorAdviceTemplate: Verify the platform change and update passenger information procedures.")
                .doesNotContain("field: payload.stopPointJourney.stopPoint.nameLong");
        assertThat(response.getValidationPlan().getPositiveExamples())
                .extracting(example -> example.getDescription())
                .containsExactly(
                        "ServiceData event with departure status DEPARTURE_PLATFORM_CHANGED.",
                        "ServiceData event with arrival status ARRIVAL_PLATFORM_CHANGED.");
        assertThat(response.getWarnings()).containsExactly(
                AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING,
                AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> metadata(AgentGenerationPreviewResponse response, String section) {
        return (Map<String, Object>) response.getBlueprint().getParameters().get(section);
    }

    private AlertAgentGenerationPreviewData previewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "field", "payload.stopPointJourney.stopPoint.nameLong",
                "operator", "EQUALS",
                "value", "Milano Malpensa T1");
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Cancelled journeys",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                1,
                "Create a suggestion when a journey is cancelled.",
                "Verified.",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of(
                        "triggerType", "EVENT",
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "inputModel", "ServiceDataV2",
                        "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                        "condition", condition),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "agentName", "CancelledJourneyServiceDataAgent",
                        "description", "Detects cancelled journeys.",
                        "triggerType", "EVENT",
                        "requiredSources", List.of("SERVICE_DATA"),
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                        "stateRequirements", Map.of("requiresState", false),
                        "output", Map.of("type", "CANDIDATE_SUGGESTION")),
                List.of("JOURNEY_CANCELLED"),
                List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData cancellationPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("any", List.of(
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                                        "operator", "CONTAINS",
                                        "value", "ARRIVAL_CANCELLATION"),
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                                        "operator", "CONTAINS",
                                        "value", "DEPARTURE_CANCELLATION"))),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Milano Malpensa T1")));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Detects matching ServiceData events using the verified stateless condition.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of(
                        "type", "CANDIDATE_SUGGESTION",
                        "reasonTemplate", "Journey ${payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName} is cancelled at Milano Malpensa T1.",
                        "operatorAdviceTemplate", "Check journey cancellation and passenger information procedures."));
        return dataWithArtifacts(technicalSpecification, blueprint);
    }

    private AlertAgentGenerationPreviewData genovaContainsAnyPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("any", List.of(
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                                        "operator", "CONTAINS_ANY",
                                        "values", List.of("ARRIVAL_CANCELLATION")),
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                                        "operator", "CONTAINS_ANY",
                                        "values", List.of("DEPARTURE_CANCELLATION")))),
                        Map.of("any", List.of(
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPoint.nameLong",
                                        "operator", "EQUALS_NORMALIZED",
                                        "value", "Genova Piazza Principe"),
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPoint.nameShort",
                                        "operator", "EQUALS_NORMALIZED",
                                        "value", "Genova PP")))));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Generic.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return dataWithArtifacts(technicalSpecification, blueprint);
    }

    private AlertAgentGenerationPreviewData intercityDelayPreviewData(boolean values) {
        Map<String, Object> delayLeaf = values
                ? Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].delayStatus",
                        "operator", "CONTAINS_IGNORE_CASE",
                        "values", List.of("DELAYED"))
                : Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].delayStatus",
                        "operator", "CONTAINS_IGNORE_CASE",
                        "value", "DELAYED");
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        delayLeaf,
                        Map.of(
                                "field", "payload.stopPointJourney.serviceCategory",
                                "operator", "EQUALS_IGNORE_CASE",
                                "value", "Intercity"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova Nervi")));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Generic.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of(
                        "type", "CANDIDATE_SUGGESTION",
                        "reasonTemplate", "Intercity journey is delayed at Genova Nervi."));
        return new AlertAgentGenerationPreviewData(
                "ALRT1", "Intercity delay", "VERIFIED", "VERIFIED", false, null, 1,
                "Avverti quando un treno Intercity e in ritardo a Genova Nervi.", "Verified.",
                null, null, "EVENT_INTERPRETER", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification, blueprint, List.of("JOURNEY_DELAYED"), List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData verifiedIntercityNumericDelayPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].serviceCategory.dsc",
                                "operator", "CONTAINS_NORMALIZED",
                                "value", "intercity"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "genova nervi"),
                        Map.of("any", List.of(
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay",
                                        "operator", "GREATER_THAN",
                                        "value", 0),
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                                        "operator", "CONTAINS_ANY",
                                        "values", List.of("ARRIVAL_DELAY"))))));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Generic.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT1", "Intercity delay", "VERIFIED", "VERIFIED", false, null, 1,
                "Avverti quando un treno Intercity e in ritardo a Genova Nervi.", "Verified.",
                null, null, "EVENT_INTERPRETER", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification, blueprint, List.of("JOURNEY_DELAYED"), List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData temporalNextCallsPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "ARRIVED"),
                        Map.of(
                                "field", "payload.ongroundServiceEvent.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova"),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                                        "conditions", Map.of(
                                                "all", List.of(
                                                        Map.of(
                                                                "field", "stopPoint.nameLong",
                                                                "operator", "EQUALS_NORMALIZED",
                                                                "value", "Gorla"),
                                                        Map.of(
                                                                "field", "departureTime",
                                                                "operator", "LOCAL_TIME_BETWEEN",
                                                                "value", Map.of(
                                                                        "start", "11:30:00",
                                                                        "end", "12:35:00",
                                                                        "timezone", "Europe/Rome"))))))));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ArrivalGenovaNextDepartureGorlaAgent",
                "description", "Detects temporal next calls.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT1", "Temporal next call", "VERIFIED", "VERIFIED", false, null, 1,
                "Fammi sapere quando una corsa arriva a Genova e partira a Gorla tra le 11:30 e le 12:35",
                "Verified.", null, null, "EVENT_INTERPRETER", "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION", technicalSpecification, blueprint, List.of("ARRIVED"), List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData temporalDayOfWeekPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of(
                                        "all", List.of(
                                                Map.of(
                                                        "field", "timetabledCallStart.stopPoint.nameLong",
                                                        "operator", "EQUALS_NORMALIZED",
                                                        "value", "Genova P.P"),
                                                Map.of(
                                                        "field", "timetabledCallStart.departureTime",
                                                        "operator", "LOCAL_TIME_BETWEEN",
                                                        "value", Map.of(
                                                                "start", "11:20:00",
                                                                "end", "11:25:00",
                                                                "timezone", "Europe/Rome")),
                                                Map.of(
                                                        "field", "timetabledCallStart.departureTime",
                                                        "operator", "LOCAL_DAY_OF_WEEK_NOT_IN",
                                                        "value", Map.of(
                                                                "days", List.of("SATURDAY", "SUNDAY"),
                                                                "timezone", "Europe/Rome"))))))));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "DepartureGenovaWeekdayWindowAgent",
                "description", "Detects weekday departures at Genova P.P.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT1", "Temporal weekday departure", "VERIFIED", "VERIFIED", false, null, 1,
                "Avvertimi quando una corsa parte da Genova P.P tra le 11:20 e le 11:25 non il weekend",
                "Verified.", null, null, "EVENT_INTERPRETER", "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION", technicalSpecification, blueprint,
                List.of("SERVICE_DATA_FIELD_MATCH"), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData platformChangeAnyLocationPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "any", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                                "operator", "CONTAINS",
                                "value", "DEPARTURE_PLATFORM_CHANGED"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                                "operator", "CONTAINS",
                                "value", "ARRIVAL_PLATFORM_CHANGED")));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Generic.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "suggestionIntent", Map.of(
                        "type", "INFORM_OPERATOR",
                        "category", "OPERATIONAL_ALERT",
                        "candidateOutput", "CANDIDATE_SUGGESTION",
                        "operatorAction", "CHECK_PASSENGER_INFORMATION_PROCEDURES"),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT1", "Platform change", "VERIFIED", "VERIFIED", false, null, 1,
                "Avverti quando un treno riceve un cambio di binario in qualsiasi localita.", "Verified.",
                null, null, "EVENT_INTERPRETER", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification, blueprint, List.of("PLATFORM_CHANGED"), List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData dataWithArtifacts(
            Map<String, Object> technicalSpecification,
            Map<String, Object> blueprint) {
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Cancelled journeys",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                1,
                "Create a suggestion when a journey is cancelled.",
                "Verified.",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification,
                blueprint,
                List.of("SERVICE_DATA_FIELD_MATCH"),
                List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }
}
