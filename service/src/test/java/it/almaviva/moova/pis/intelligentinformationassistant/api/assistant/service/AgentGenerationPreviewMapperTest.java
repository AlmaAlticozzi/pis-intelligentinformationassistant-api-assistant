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
    void scheduledVerifiedArtifactsProduceDeterministicPreviewWithScheduledDsl() {
        AgentGenerationPreviewResponse response = mapper.toResponse(scheduledPreviewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getRecommendedGenerationMode()).isEqualTo(AgentGenerationMode.DSL);
        assertThat(response.getRequiredSources()).containsExactly(AgentDataSource.SERVICE_DATA);
        assertThat(response.getRequiredPermissions()).containsExactly("READ_SERVICE_DATA");
        assertThat(response.getBlueprint().getTargetTypes())
                .containsExactly(SuggestionTargetType.SERVICE_DATA_JOURNEY_AGGREGATE);
        assertThat(response.getBlueprint().getParameters())
                .containsKeys("serviceDataQuery", "snapshotEvaluation", "outputPolicy", "schedule", "conditionType");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("trigger:\n  type: SCHEDULE")
                .contains("frequencySeconds: 600")
                .contains("operation: POST /v2/stoppointjourneys")
                .contains("inputModel: ServiceDataStopPointJourneysV2")
                .contains("- TNPNTS00000000000009")
                .contains("lookaheadMinutes: 180")
                .contains("evaluationMode: SCHEDULED_SNAPSHOT_MATCH")
                .contains("journeyPath: stopPointsJourneyDetails[]")
                .contains("mode: REPORT_COUNT")
                .contains("conditionType: SERVICE_DATA_SCHEDULED_FIELD_MATCH")
                .contains("field: callStart.stopPoint.id")
                .contains("operator: EQUALS")
                .contains("value: TNPNTS00000000000122")
                .contains("requiresScheduler: true")
                .contains("supportedByRuntime: true");
        assertThat(metadata(response, "runtimeContract"))
                .containsEntry("executionModel", "SCHEDULED_POLLING")
                .containsEntry("triggerType", "SCHEDULE")
                .containsEntry("source", "SERVICE_DATA")
                .containsEntry("inputModel", "ServiceDataStopPointJourneysV2")
                .containsEntry("outputModel", "AgentOutput.CANDIDATE_SUGGESTION")
                .containsEntry("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH")
                .containsEntry("requiresScheduler", true)
                .containsEntry("requiresState", false)
                .containsEntry("requiresExternalTools", false)
                .containsEntry("requiredPermissions", List.of("READ_SERVICE_DATA"))
                .containsEntry("allowedSources", List.of("SERVICE_DATA"));
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
    void arrivalOnlyCancellationPreviewIsRuntimeSupported() {
        AgentGenerationPreviewResponse response = mapper.toResponse(arrivalOnlyCancellationPreviewData(), null);

        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("field: payload.ongroundServiceEvent.eventsType")
                .contains("operator: CONTAINS")
                .contains("value: ARRIVAL_CANCELLATION")
                .contains("field: arrivalStatuses[].status")
                .contains("value: ARRIVAL_CANCELLATION")
                .contains("field: departureStatuses[].status")
                .contains("operator: NOT_CONTAINS")
                .contains("value: DEPARTURE_CANCELLATION");
    }

    @Test
    void dslPreviewRendersStopPointIdEquals() {
        AgentGenerationPreviewResponse response = mapper.toResponse(stopPointIdEqualsPreviewData(), null);

        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("field: payload.ongroundServiceEvent.stopPoint.id")
                .contains("operator: EQUALS")
                .contains("value: TNPNTS00000000005467 # RHO FIERAMILANO");
    }

    @Test
    void dslPreviewRendersStopPointIdIn() {
        AgentGenerationPreviewResponse response = mapper.toResponse(stopPointIdInPreviewData(), null);

        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("field: payload.ongroundServiceEvent.stopPoint.id")
                .contains("operator: IN")
                .contains("- TNPNTS00000000000028 # MALPENSA AEROPORTO T.1")
                .contains("- TNPNTS00000000000029 # MALPENSA AEROPORTO T.2");
    }

    @Test
    void dslPreviewRendersPlatformComparisonOtherField() {
        AgentGenerationPreviewResponse response = mapper.toResponse(platformFieldComparisonPreviewData(), null);

        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("field: timetabledDeparturePlatform.dsc")
                .contains("operator: PLATFORM_NOT_EQUALS_FIELD")
                .contains("otherField: actualDeparturePlatform.platform.dsc");
    }

    @Test
    void dslPreviewRendersHumanPlatformOperatorsAndCurrentChangedEvent() {
        AgentGenerationPreviewResponse response = mapper.toResponse(platformOperatorsPreviewData(), null);

        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("field: payload.ongroundServiceEvent.eventsType")
                .contains("operator: CONTAINS_ANY")
                .contains("- DEPARTURE_PLATFORM_CHANGED")
                .contains("- ARRIVAL_PLATFORM_CHANGED")
                .contains("operator: EQUAL_PLATFORM")
                .contains("value: 5")
                .contains("operator: NOT_EQUAL_PLATFORM")
                .contains("operator: IN_PLATFORMS")
                .contains("- 7")
                .contains("- 8")
                .contains("operator: NOT_IN_PLATFORMS")
                .contains("- 1")
                .contains("- 12")
                .contains("operator: PLATFORM_NOT_EQUALS_FIELD")
                .contains("otherField: actualDeparturePlatform.platform.dsc");
    }

    @Test
    void dslPreviewRendersAdvancedPlatformNumericOperators() {
        AgentGenerationPreviewResponse response = mapper.toResponse(advancedPlatformOperatorsPreviewData(), null);

        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("field: payload.ongroundServiceEvent.eventsType")
                .contains("field: actualDeparturePlatform.platform.dsc")
                .contains("field: actualArrivalPlatform.platform.dsc")
                .contains("operator: PLATFORM_NUMBER_GREATER_THAN")
                .contains("value: 5")
                .contains("operator: PLATFORM_NUMBER_BETWEEN")
                .contains("min: 3")
                .contains("max: 8")
                .contains("operator: PLATFORM_NUMBER_EVEN")
                .contains("operator: PLATFORM_HAS_LETTER_SUFFIX");
    }

    @Test
    void realisticDeparturePlatformChangeProducesReadOnlyRuntimeSupportedPreview() {
        AgentGenerationPreviewResponse response = mapper.toResponse(realisticDeparturePlatformChangePreviewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getDslPreview()).isNotNull();
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getValidationPlan()).isNotNull();
        assertThat(response.getDslPreview().getDsl())
                .contains("field: payload.stopPointJourney.stopPoint.id")
                .contains("value: TNPNTS00000000005442")
                .contains("field: payload.ongroundServiceEvent.eventsType")
                .contains("value: DEPARTURE_PLATFORM_CHANGED")
                .contains("operator: PLATFORM_NOT_EQUALS_FIELD")
                .contains("otherField: actualDeparturePlatform.platform.dsc");
        assertThat(response.getValidationPlan().getPositiveExamples())
                .extracting(example -> example.getDescription())
                .contains("ServiceData current eventsType contains DEPARTURE_PLATFORM_CHANGED and timetabled departure platform differs from actual departure platform.");
        assertThat(response.getWarnings())
                .contains("Read-only preview generated from verified Alert artifacts; no Agent Definition has been created.");
    }

    @Test
    void validationPlanMentionsIdBasedLocationMatch() {
        AgentGenerationPreviewResponse response = mapper.toResponse(stopPointIdEqualsPreviewData(), null);

        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("event.stopPoint.id equal to resolved PIS candidate TNPNTS00000000005467");
        assertThat(response.getValidationPlan().getNegativeExamples().getFirst().getDescription())
                .contains("event.stopPoint.id different from resolved PIS candidate");
        assertThat(response.getValidationPlan().getEdgeCases())
                .anyMatch(edgeCase -> edgeCase.contains("similar stopPoint.nameLong")
                        && edgeCase.contains("different stopPoint.id"));
    }

    @Test
    void previewValidatorRejectsStopPointIdInEmptyValues() {
        assertThatThrownBy(() -> mapper.toResponse(stopPointIdEmptyInPreviewData(), null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class);
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
                .contains("- anyElement:\n        path: payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]")
                .contains("field: stopPoint.nameLong\n            operator: EQUALS_NORMALIZED\n            value: Gorla")
                .contains("field: departureTime\n            operator: LOCAL_TIME_BETWEEN")
                .contains("start: \"11:30:00\"\n              end: \"12:35:00\"\n              timezone: Europe/Rome")
                .contains("supportedByRuntime: true");
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("same next call", "Gorla", "departureTime", "11:30:00-12:35:00");
        assertThat(response.getValidationPlan().getNegativeExamples())
                .extracting(example -> example.getDescription())
                .anyMatch(description -> description.contains("Gorla") && description.contains("outside"))
                .anyMatch(description -> description.contains("different from Gorla"));
        assertThat(response.getValidationPlan().getEdgeCases())
                .anyMatch(description -> description.contains("different next calls"));
    }

    @Test
    void temporalDayOfWeekAndTimeConditionProducesRuntimeSupportedPreviewAndPlan() {
        AgentGenerationPreviewResponse response = mapper.toResponse(temporalDayOfWeekPreviewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("operator: LOCAL_TIME_BETWEEN")
                .contains("operator: LOCAL_DAY_OF_WEEK_NOT_IN")
                .contains("days:\n                - SATURDAY\n                - SUNDAY")
                .contains("timezone: Europe/Rome")
                .contains("supportedByRuntime: true");
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("11:20:00-11:25:00", "not included in [SATURDAY, SUNDAY]");
        assertThat(response.getValidationPlan().getNegativeExamples())
                .extracting(example -> example.getDescription())
                .anyMatch(description -> description.contains("outside 11:20:00-11:25:00")
                        || description.contains("excluded by [SATURDAY, SUNDAY]"));
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .doesNotContain("nextCall")
                .contains("same stopPointsJourneyDetails element");
    }

    @Test
    void nestedAnyElementConditionProducesRuntimeSupportedDslAndPlan() {
        AgentGenerationPreviewResponse response = mapper.toResponse(nestedTransitTuesdayPreviewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getBlueprint().getAgentName())
                .isEqualTo("GenovaPPToGenovaNerviTuesdayTransitAgent");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getDsl())
                .contains("anyElement:\n    path: payload.stopPointJourney.stopPointsJourneyDetails[]")
                .contains("- anyElement:\n          path: nextTransitCalls[]\n          all:")
                .contains("field: stopPoint.nameLong\n              operator: CONTAINS_NORMALIZED\n              value: Genova Nervi")
                .contains("field: passingTime\n              operator: LOCAL_DAY_OF_WEEK_IN")
                .contains("days:\n                  - TUESDAY")
                .contains("supportedByRuntime: true");
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("same stopPointsJourneyDetails element has origin stopPoint Genova P.P")
                .contains("contains a same nextTransitCalls element with stopPoint Genova Nervi")
                .contains("passingTime local day included in [TUESDAY]");
        assertThat(response.getValidationPlan().getNegativeExamples())
                .extracting(example -> example.getDescription())
                .anyMatch(description -> description.contains("origin stopPoint different from Genova P.P")
                        && description.contains("nextTransitCalls element for Genova Nervi"))
                .anyMatch(description -> description.contains("origin stopPoint Genova P.P")
                        && description.contains("no nextTransitCalls element for Genova Nervi"))
                .anyMatch(description -> description.contains("nextTransitCalls stopPoint Genova Nervi")
                        && description.contains("passingTime local day is not included in [TUESDAY]"));
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
                .contains("field: payload.ongroundServiceEvent.eventsType")
                .contains("operator: CONTAINS_ANY\n    values:\n      - DEPARTURE_PLATFORM_CHANGED\n      - ARRIVAL_PLATFORM_CHANGED")
                .contains("reasonTemplate: Train ${payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName} received a platform change at ${payload.stopPointJourney.stopPoint.nameLong}.")
                .contains("operatorAdviceTemplate: Verify the platform change and update passenger information procedures.")
                .doesNotContain("field: payload.stopPointJourney.stopPoint.nameLong");
        assertThat(response.getValidationPlan().getPositiveExamples())
                .extracting(example -> example.getDescription())
                .containsExactly(
                        "ServiceData current eventsType contains DEPARTURE_PLATFORM_CHANGED and timetabled departure platform differs from actual departure platform.");
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

    private AlertAgentGenerationPreviewData scheduledPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "callStart.stopPoint.id",
                        "operator", "EQUALS",
                        "value", "TNPNTS00000000000122")));
        Map<String, Object> serviceDataQuery = Map.of(
                "operation", "POST /v2/stoppointjourneys",
                "stopPoints", List.of("TNPNTS00000000000009"),
                "timeWindow", Map.of(
                        "startMode", "NOW_TRUNCATED_TO_MINUTE",
                        "endMode", "NOW_PLUS_DURATION",
                        "rawText", "nelle prossime 3 ore",
                        "defaulted", false,
                        "lookaheadMinutes", 180),
                "monitoringScope", "EXPLICIT_STOP_POINTS",
                "requiresAllKnownStopPoints", false);
        Map<String, Object> snapshotEvaluation = new LinkedHashMap<>();
        snapshotEvaluation.put("mode", "REPORT_COUNT");
        snapshotEvaluation.put("journeyPath", "stopPointsJourneyDetails[]");
        snapshotEvaluation.put("condition", condition);
        snapshotEvaluation.put("threshold", null);
        Map<String, Object> outputPolicy = Map.of(
                "emit", "EVERY_RUN",
                "includeCount", true,
                "includeMatchingJourneys", false);
        Map<String, Object> schedule = Map.of(
                "rawText", "Ogni 10 minuti",
                "defaulted", false,
                "frequencySeconds", 600);
        Map<String, Object> technicalSpecification = new LinkedHashMap<>();
        technicalSpecification.put("source", "SERVICE_DATA");
        technicalSpecification.put("schedule", schedule);
        technicalSpecification.put("accessMode", "SERVICE_DATA_API_SNAPSHOT");
        technicalSpecification.put("inputModel", "ServiceDataStopPointJourneysV2");
        technicalSpecification.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        technicalSpecification.put("triggerType", "SCHEDULE");
        technicalSpecification.put("outputPolicy", outputPolicy);
        technicalSpecification.put("schemaVersion", "iia.alert.technical-specification/v2");
        technicalSpecification.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        technicalSpecification.put("interpreterType", "SCHEDULED_INTERPRETER");
        technicalSpecification.put("serviceDataQuery", serviceDataQuery);
        technicalSpecification.put("snapshotEvaluation", snapshotEvaluation);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ScheduledServiceDataSnapshotAlertAgent",
                "description", "Reports scheduled journeys from a ServiceData snapshot.",
                "triggerType", "SCHEDULE",
                "requiredSources", List.of("SERVICE_DATA"),
                "targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "parameters", Map.of(
                        "serviceDataQuery", serviceDataQuery,
                        "snapshotEvaluation", snapshotEvaluation,
                        "outputPolicy", outputPolicy,
                        "schedule", schedule),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT_SCHEDULED",
                "Scheduled count",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                1,
                "Ogni 10 minuti dimmi quante corse a Garibaldi FS hanno origine Monza nelle prossime 3 ore",
                "Verified.",
                null,
                null,
                "SCHEDULED_INTERPRETER",
                "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification,
                blueprint,
                List.of("SERVICE_DATA_SCHEDULED_FIELD_MATCH"),
                List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY_AGGREGATE));
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

    private AlertAgentGenerationPreviewData arrivalOnlyCancellationPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.id",
                                "operator", "EQUALS",
                                "value", "TNPNTS00000000005467"),
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "ARRIVAL_CANCELLATION"),
                        Map.of("anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of("all", List.of(
                                        Map.of(
                                                "field", "arrivalStatuses[].status",
                                                "operator", "CONTAINS",
                                                "value", "ARRIVAL_CANCELLATION"),
                                        Map.of(
                                                "field", "departureStatuses[].status",
                                                "operator", "NOT_CONTAINS",
                                                "value", "DEPARTURE_CANCELLATION")))))));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Arrival-only cancellation.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
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

    private AlertAgentGenerationPreviewData nestedTransitTuesdayPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("all", List.of(
                                Map.of(
                                        "field", "timetabledCallStart.stopPoint.nameLong",
                                        "operator", "EQUALS_NORMALIZED",
                                        "value", "Genova P.P"),
                                Map.of(
                                        "anyElement", Map.of(
                                                "path", "nextTransitCalls[]",
                                                "conditions", Map.of("all", List.of(
                                                        Map.of(
                                                                "field", "stopPoint.nameLong",
                                                                "operator", "CONTAINS_NORMALIZED",
                                                                "value", "Genova Nervi"),
                                                        Map.of(
                                                                "field", "passingTime",
                                                                "operator", "LOCAL_DAY_OF_WEEK_IN",
                                                                "value", Map.of(
                                                                        "days", List.of("TUESDAY"),
                                                                        "timezone", "Europe/Rome"))))))))));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "DepartureGenovaTransitNerviTuesdayAgent",
                "description", "Detects Tuesday transits at Genova Nervi for departures from Genova P.P.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT1", "Temporal transit Tuesday", "VERIFIED", "VERIFIED", false, null, 1,
                "Avvertimi quando una corsa che parte da Genova P.P e transitera a Genova Nervi il martedi",
                "Verified.", null, null, "EVENT_INTERPRETER", "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION", technicalSpecification, blueprint,
                List.of("SERVICE_DATA_FIELD_MATCH"), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData platformChangeAnyLocationPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "field", "payload.ongroundServiceEvent.eventsType",
                "operator", "CONTAINS_ANY",
                "values", List.of("DEPARTURE_PLATFORM_CHANGED", "ARRIVAL_PLATFORM_CHANGED"));
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

    private AlertAgentGenerationPreviewData stopPointIdEqualsPreviewData() {
        return stopPointIdPreviewData(Map.of(
                "field", "payload.ongroundServiceEvent.stopPoint.id",
                "operator", "EQUALS",
                "value", "TNPNTS00000000005467"));
    }

    private AlertAgentGenerationPreviewData platformFieldComparisonPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "field", "timetabledDeparturePlatform.dsc",
                                "operator", "PLATFORM_NOT_EQUALS_FIELD",
                                "otherField", "actualDeparturePlatform.platform.dsc")));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Detects a departure platform change.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return dataWithArtifacts(technicalSpecification, blueprint);
    }

    private AlertAgentGenerationPreviewData platformOperatorsPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS_ANY",
                                "values", List.of("DEPARTURE_PLATFORM_CHANGED", "ARRIVAL_PLATFORM_CHANGED")),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of("all", List.of(
                                                Map.of(
                                                        "field", "previousDeparturePlatform.platform.dsc",
                                                        "operator", "EQUAL_PLATFORM",
                                                        "value", "5"),
                                                Map.of(
                                                        "field", "actualDeparturePlatform.platform.dsc",
                                                        "operator", "IN_PLATFORMS",
                                                        "values", List.of("7", "8")),
                                                Map.of(
                                                        "field", "timetabledDeparturePlatform.dsc",
                                                        "operator", "NOT_IN_PLATFORMS",
                                                        "values", List.of("1", "12")),
                                                Map.of(
                                                        "field", "timetabledDeparturePlatform.dsc",
                                                        "operator", "PLATFORM_NOT_EQUALS_FIELD",
                                                        "otherField", "actualDeparturePlatform.platform.dsc"),
                                                Map.of(
                                                        "field", "timetabledArrivalPlatform.dsc",
                                                        "operator", "NOT_EQUAL_PLATFORM",
                                                        "value", "3")))))));
        return platformPreviewData(condition, "Detects platform movement.");
    }

    private AlertAgentGenerationPreviewData realisticDeparturePlatformChangePreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.id",
                                "operator", "EQUALS",
                                "value", "TNPNTS00000000005442"),
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "DEPARTURE_PLATFORM_CHANGED"),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of(
                                                "field", "timetabledDeparturePlatform.dsc",
                                                "operator", "PLATFORM_NOT_EQUALS_FIELD",
                                                "otherField", "actualDeparturePlatform.platform.dsc")))));
        return platformPreviewData(condition, "Detects a current departure platform change.");
    }

    private AlertAgentGenerationPreviewData advancedPlatformOperatorsPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS_ANY",
                                "values", List.of("DEPARTING", "ARRIVED")),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of("all", List.of(
                                                Map.of(
                                                        "field", "actualDeparturePlatform.platform.dsc",
                                                        "operator", "PLATFORM_NUMBER_GREATER_THAN",
                                                        "value", 5),
                                                Map.of(
                                                        "field", "actualArrivalPlatform.platform.dsc",
                                                        "operator", "PLATFORM_NUMBER_BETWEEN",
                                                        "value", Map.of("min", 3, "max", 8)),
                                                Map.of(
                                                        "field", "actualDeparturePlatform.platform.dsc",
                                                        "operator", "PLATFORM_NUMBER_EVEN"),
                                                Map.of(
                                                        "field", "actualDeparturePlatform.platform.dsc",
                                                        "operator", "PLATFORM_HAS_LETTER_SUFFIX")))))));
        return platformPreviewData(condition, "Detects advanced numeric platform properties.");
    }

    private AlertAgentGenerationPreviewData platformPreviewData(
            Map<String, Object> condition,
            String description) {
        Map<String, Object> technicalSpecification = Map.of(
                "source", "SERVICE_DATA",
                "schemaVersion", "iia.alert.technical-specification/v2",
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition,
                "deduplicationKeyTemplate", "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}");
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", description,
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return dataWithArtifacts(technicalSpecification, blueprint);
    }

    private AlertAgentGenerationPreviewData stopPointIdInPreviewData() {
        return stopPointIdPreviewData(Map.of(
                "field", "payload.ongroundServiceEvent.stopPoint.id",
                "operator", "IN",
                "values", List.of("TNPNTS00000000000028", "TNPNTS00000000000029")));
    }

    private AlertAgentGenerationPreviewData stopPointIdEmptyInPreviewData() {
        return stopPointIdPreviewData(Map.of(
                "field", "payload.ongroundServiceEvent.stopPoint.id",
                "operator", "IN",
                "values", List.of()));
    }

    private AlertAgentGenerationPreviewData stopPointIdPreviewData(Map<String, Object> stopPointCondition) {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(stopPointCondition));
        Map<String, Object> locationResolution = Map.of(
                "resolutions", List.of(Map.of(
                        "rawText", "Rho Fieramilano",
                        "status", "RESOLVED",
                        "candidates", List.of(
                                Map.of(
                                        "id", "TNPNTS00000000005467",
                                        "nameLong", "RHO FIERAMILANO"),
                                Map.of(
                                        "id", "TNPNTS00000000000028",
                                        "nameLong", "MALPENSA AEROPORTO T.1"),
                                Map.of(
                                        "id", "TNPNTS00000000000029",
                                        "nameLong", "MALPENSA AEROPORTO T.2")))));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "locationResolution", locationResolution,
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Detects matching ServiceData events using stopPoint.id.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of(
                        "conditionType", "SERVICE_DATA_FIELD_MATCH",
                        "locationResolution", locationResolution,
                        "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return dataWithArtifacts(technicalSpecification, blueprint);
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
