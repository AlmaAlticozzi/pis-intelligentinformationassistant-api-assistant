package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDslPreview;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationExample;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AgentGenerationPreviewMapper {

    private static final Set<String> BLUEPRINT_FIELDS = Set.of(
            "agentName",
            "description",
            "triggerType",
            "requiredSources",
            "targetTypes",
            "parameters",
            "stateRequirements",
            "suggestionIntent");

    public AgentGenerationPreviewResponse toResponse(
            AlertAgentGenerationPreviewData data,
            AgentGenerationPreviewRequest request) {
        List<String> warnings = new ArrayList<>(data.warnings() == null ? List.of() : data.warnings());
        if (request != null && AgentGenerationMode.JAVA_TEMPLATE.equals(request.getPreferredGenerationMode())) {
            warnings.add("JAVA_TEMPLATE is not generated in the read-only preview MVP; DSL preview is returned when possible.");
        }

        AgentGenerationPreviewResponse response = new AgentGenerationPreviewResponse()
                .alert(new AlertReference().id(data.alertId()).name(data.name()))
                .canGenerate(true)
                .recommendedGenerationMode(AgentGenerationMode.DSL)
                .estimatedComplexity(AgentComplexity.LOW)
                .requiredSources(List.of(AgentDataSource.SERVICE_DATA))
                .requiredPermissions(List.of("READ_SERVICE_DATA"))
                .blueprint(toBlueprint(data))
                .warnings(warnings)
                .rejectedReason(null);

        if (included(request == null ? null : request.getIncludeDslPreview())) {
            response.dslPreview(toDslPreview(data));
        }
        if (included(request == null ? null : request.getIncludeValidationPlan())) {
            response.validationPlan(toValidationPlan(data));
        }
        return response;
    }

    private AgentBlueprint toBlueprint(AlertAgentGenerationPreviewData data) {
        Map<String, Object> persisted = data.agentBlueprintPreview();
        AgentBlueprint blueprint = new AgentBlueprint()
                .agentName(stringValue(persisted.get("agentName")))
                .description(firstString(persisted.get("description"), data.verificationSummary()))
                .triggerType(AgentBlueprint.TriggerTypeEnum.fromString(
                        firstString(persisted.get("triggerType"), data.technicalSpecification().get("triggerType"), "EVENT")))
                .requiredSources(List.of(AgentDataSource.SERVICE_DATA))
                .targetTypes(data.targetTypes() == null ? List.of() : data.targetTypes())
                .parameters(mapValue(persisted.get("parameters")))
                .stateRequirements(mapValue(persisted.get("stateRequirements")))
                .suggestionIntent(mapValue(persisted.get("suggestionIntent")));

        persisted.forEach((key, value) -> {
            if (!BLUEPRINT_FIELDS.contains(key)) {
                blueprint.putAdditionalProperty(key, value);
            }
        });
        return blueprint;
    }

    private AgentDslPreview toDslPreview(AlertAgentGenerationPreviewData data) {
        String triggerType = firstString(data.technicalSpecification().get("triggerType"), "EVENT");
        String inputModel = firstString(data.inputModel(), data.technicalSpecification().get("inputModel"), "ServiceDataV2");
        String outputModel = firstString(data.outputModel(), data.technicalSpecification().get("outputModel"), "AgentOutput.CANDIDATE_SUGGESTION");
        String evaluationMode = firstString(data.technicalSpecification().get("evaluationMode"), "STATELESS_EVENT_MATCH");
        List<String> events = data.interpretedEventNames() == null ? List.of() : data.interpretedEventNames();
        String eventLines = events.isEmpty()
                ? "    events: []\n"
                : "    events:\n" + events.stream().map(event -> "      - " + event + "\n").reduce("", String::concat);

        String dsl = "schemaVersion: iia.agent.dsl/v1\n"
                + "trigger:\n"
                + "  type: " + triggerType + "\n"
                + "  source: SERVICE_DATA\n"
                + "  inputModel: " + inputModel + "\n"
                + "match:\n"
                + "  evaluationMode: " + evaluationMode + "\n"
                + eventLines
                + "output:\n"
                + "  type: " + outputModel + "\n";
        return new AgentDslPreview()
                .schemaVersion("iia.agent.dsl/v1")
                .summary("Read-only DSL preview derived from verified alert artifacts.")
                .dsl(dsl)
                .supportedByRuntime(true);
    }

    private AgentValidationPlan toValidationPlan(AlertAgentGenerationPreviewData data) {
        String eventName = data.interpretedEventNames() == null || data.interpretedEventNames().isEmpty()
                ? "a matching interpreted event"
                : data.interpretedEventNames().getFirst();
        return new AgentValidationPlan()
                .positiveExamples(List.of(new AgentValidationExample()
                        .description("ServiceData event with eventName " + eventName + ".")
                        .expectedOutput(AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                .negativeExamples(List.of(new AgentValidationExample()
                        .description("ServiceData event with an eventName outside the interpreted event set.")
                        .expectedOutput(AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                .edgeCases(List.of("ServiceData event without journeyId or with an incomplete target is rejected safely."));
    }

    private boolean included(Boolean value) {
        return value == null || value;
    }

    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
        return result;
    }

    private String firstString(Object... values) {
        for (Object value : values) {
            String stringValue = stringValue(value);
            if (stringValue != null && !stringValue.isBlank()) {
                return stringValue;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
