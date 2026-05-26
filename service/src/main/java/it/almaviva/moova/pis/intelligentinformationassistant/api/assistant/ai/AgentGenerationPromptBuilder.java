package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentGenerationCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AgentGenerationPromptBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AiConfiguration aiConfiguration;
    private final AgentGenerationCapabilityCatalog capabilityCatalog;

    @Inject
    public AgentGenerationPromptBuilder(
            AiConfiguration aiConfiguration,
            AgentGenerationCapabilityCatalog capabilityCatalog) {
        this.aiConfiguration = aiConfiguration;
        this.capabilityCatalog = capabilityCatalog;
    }

    public LlmRequest build(AlertAgentGenerationPreviewData alert, AgentGenerationPreviewRequest request) {
        AiConfiguration.AgentBlueprintGenerate configuration = aiConfiguration.agentBlueprintGenerate();
        System.out.println("[IIA][AGENT_PREVIEW_LLM] Building prompt alertId=" + alert.alertId()
                + ", model=" + configuration.model());
        return new LlmRequest(
                AiUseCase.AGENT_BLUEPRINT_GENERATE,
                systemPrompt(),
                userPrompt(alert, request),
                configuration.model(),
                configuration.temperature(),
                configuration.maxOutputTokens(),
                alert.alertId());
    }

    private String systemPrompt() {
        return """
                You generate a controlled Agent Blueprint preview for an already verified Alert.
                Return JSON only. Do not use markdown or text outside JSON.
                Do not produce dslPreview or validationPlan; the backend builds them deterministically.
                Do not generate Java code, executable code, Agent Definition, Agent Run, Suggestion records or runtime execution.
                Do not use HTTP, database queries, filesystem, shell, Kafka producer or unsafe capabilities.
                Use only the capability catalog included in the request.
                The backend will reject every blueprint that does not pass strict validation.
                """;
    }

    private String userPrompt(AlertAgentGenerationPreviewData alert, AgentGenerationPreviewRequest request) {
        return """
                Generate a refined Agent Blueprint candidate from the verified artifacts below.

                Alert:
                - alertId: %s
                - name: %s
                - prompt: %s
                - preferredGenerationMode: %s
                - includeDslPreview: %s
                - includeValidationPlan: %s

                Allowed Agent Generation MVP capabilities:
                - sources: %s
                - permissions: %s
                - generationModes: %s
                - previewOnlyGenerationModes: %s
                - dslOperators: %s
                - dslLogicalNodes: %s
                - dslFunctions: %s
                - temporalFields: %s
                - arrayPaths: %s
                - nextCallsRelativeFields: %s
                - forbiddenCapabilities: %s
                - triggerType: EVENT
                - evaluationMode: STATELESS_EVENT_MATCH
                - inputModel: ServiceDataV2
                - outputType: CANDIDATE_SUGGESTION
                - targetType: SERVICE_DATA_JOURNEY
                - requiresState: false

                Temporal and correlated-array rules:
                - Preserve every verified condition in the generated blueprint parameters.condition tree.
                - LOCAL_TIME_BETWEEN is stateless only for the verified temporal fields and must retain start, end and timezone.
                - Preserve anyElement for correlated nextCalls constraints; never flatten stopPoint and departureTime/arrivalTime conditions into independent leaves.
                - Inside anyElement on payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[], fields are relative to the same item.

                Verified technicalSpecification JSON:
                %s

                Existing verified agentBlueprintPreview JSON:
                %s

                Interpreted event names JSON:
                %s

                Return this JSON shape, with a complete valid condition tree using only allowed operators:
                {
                  "canGenerate": true,
                  "recommendedGenerationMode": "DSL",
                  "estimatedComplexity": "LOW",
                  "requiredSources": ["SERVICE_DATA"],
                  "requiredPermissions": ["READ_SERVICE_DATA"],
                  "blueprint": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "ReadableAlphanumericAgentNameAgent",
                    "description": "Readable deterministic behavior description.",
                    "triggerType": "EVENT",
                    "requiredSources": ["SERVICE_DATA"],
                    "targetTypes": ["SERVICE_DATA_JOURNEY"],
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "parameters": {
                      "conditionType": "SERVICE_DATA_FIELD_MATCH",
                      "condition": {}
                    },
                    "stateRequirements": {"requiresState": false},
                    "suggestionIntent": {
                      "type": "INFORM_OPERATOR",
                      "category": "OPERATIONAL_ALERT",
                      "candidateOutput": "CANDIDATE_SUGGESTION",
                      "operatorAction": "CHECK_PASSENGER_INFORMATION_PROCEDURES"
                    },
                    "output": {
                      "type": "CANDIDATE_SUGGESTION",
                      "reasonTemplate": "Reason for the candidate suggestion.",
                      "operatorAdviceTemplate": "Operator advice."
                    }
                  },
                  "warnings": []
                }
                """.formatted(
                alert.alertId(),
                alert.name(),
                alert.prompt(),
                request == null ? null : request.getPreferredGenerationMode(),
                request == null ? null : request.getIncludeDslPreview(),
                request == null ? null : request.getIncludeValidationPlan(),
                capabilityCatalog.supportedSources(),
                capabilityCatalog.supportedPermissions(),
                capabilityCatalog.supportedGenerationModes(),
                capabilityCatalog.previewOnlyGenerationModes(),
                capabilityCatalog.supportedDslOperators(),
                capabilityCatalog.supportedDslLogicalNodes(),
                capabilityCatalog.supportedDslFunctions(),
                capabilityCatalog.supportedTemporalFields(),
                capabilityCatalog.supportedArrayPaths(),
                capabilityCatalog.supportedArrayRelativeFields(
                        "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]"),
                capabilityCatalog.forbiddenCapabilities(),
                json(alert.technicalSpecification()),
                json(alert.agentBlueprintPreview()),
                json(alert.interpretedEventNames()));
    }

    private String json(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }
}
