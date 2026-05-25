package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    private final AgentPreviewConditionExtractor conditionExtractor = new AgentPreviewConditionExtractor();
    private final AgentPreviewNamingHelper namingHelper = new AgentPreviewNamingHelper();
    private final AgentDslPreviewBuilder dslPreviewBuilder = new AgentDslPreviewBuilder();
    private final AgentValidationPlanBuilder validationPlanBuilder = new AgentValidationPlanBuilder();

    public AgentGenerationPreviewResponse toResponse(
            AlertAgentGenerationPreviewData data,
            AgentGenerationPreviewRequest request) {
        AgentPreviewConditionExtractor.ConditionSummary conditionSummary = conditionExtractor.extract(data);
        System.out.println("[IIA][AGENT_PREVIEW] Extracted preview condition summary alertId=" + data.alertId()
                + ", conditionType=" + conditionSummary.conditionType()
                + ", location=" + conditionSummary.location()
                + ", cancellation=" + conditionSummary.cancellation());

        AgentBlueprint blueprint = toBlueprint(data, conditionSummary);
        System.out.println("[IIA][AGENT_PREVIEW] Rendering deterministic DSL preview alertId=" + data.alertId());
        AgentDslPreviewBuilder.BuildResult dslResult = dslPreviewBuilder.build(data, blueprint, conditionSummary);
        LinkedHashSet<String> warnings = new LinkedHashSet<>(data.warnings() == null ? List.of() : data.warnings());
        warnings.add("Read-only preview generated from verified Alert artifacts; no Agent Definition has been created.");
        warnings.add("DSL preview is diagnostic and has not been compiled or executed.");
        if (request != null && AgentGenerationMode.JAVA_TEMPLATE.equals(request.getPreferredGenerationMode())) {
            warnings.add("JAVA_TEMPLATE was requested but is not generated in the read-only preview MVP; DSL preview is returned when possible.");
        }
        if (dslResult.partial()) {
            warnings.add("DSL preview is partial because some condition nodes are not supported by the deterministic renderer.");
        }
        if (!dslResult.supportedByRuntime()) {
            warnings.add("DSL preview is diagnostic only because the verified artifacts do not match the supported stateless ServiceData runtime profile.");
        }

        AgentGenerationPreviewResponse response = new AgentGenerationPreviewResponse()
                .alert(new AlertReference().id(data.alertId()).name(data.name()))
                .canGenerate(true)
                .recommendedGenerationMode(AgentGenerationMode.DSL)
                .estimatedComplexity(AgentComplexity.LOW)
                .requiredSources(List.of(AgentDataSource.SERVICE_DATA))
                .requiredPermissions(List.of("READ_SERVICE_DATA"))
                .blueprint(blueprint)
                .warnings(List.copyOf(warnings))
                .rejectedReason(null);

        if (included(request == null ? null : request.getIncludeDslPreview())) {
            response.dslPreview(dslResult.preview());
        }
        if (included(request == null ? null : request.getIncludeValidationPlan())) {
            response.validationPlan(validationPlanBuilder.build(data, conditionSummary));
            System.out.println("[IIA][AGENT_PREVIEW] Validation plan generated alertId=" + data.alertId()
                    + ", positiveExamples=" + response.getValidationPlan().getPositiveExamples().size()
                    + ", negativeExamples=" + response.getValidationPlan().getNegativeExamples().size()
                    + ", edgeCases=" + response.getValidationPlan().getEdgeCases().size());
        }
        System.out.println("[IIA][AGENT_PREVIEW] Preview warnings alertId=" + data.alertId()
                + ", warningsCount=" + response.getWarnings().size());
        return response;
    }

    private AgentBlueprint toBlueprint(
            AlertAgentGenerationPreviewData data,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        Map<String, Object> persisted = data.agentBlueprintPreview();
        Map<String, Object> suggestionIntent = mapValue(persisted.get("suggestionIntent"));
        if (conditionSummary.cancellation()) {
            suggestionIntent.putIfAbsent("type", "INFORM_OPERATOR");
            suggestionIntent.putIfAbsent("category", "JOURNEY_CANCELLATION");
            suggestionIntent.putIfAbsent("candidateOutput", "CANDIDATE_SUGGESTION");
            suggestionIntent.putIfAbsent("operatorAction", "CHECK_PASSENGER_INFORMATION_PROCEDURES");
        }
        AgentBlueprint blueprint = new AgentBlueprint()
                .agentName(namingHelper.agentName(data, conditionSummary))
                .description(namingHelper.description(data, conditionSummary))
                .triggerType(AgentBlueprint.TriggerTypeEnum.fromString(
                        firstString(persisted.get("triggerType"), data.technicalSpecification().get("triggerType"), "EVENT")))
                .requiredSources(List.of(AgentDataSource.SERVICE_DATA))
                .targetTypes(data.targetTypes() == null ? List.of() : data.targetTypes())
                .parameters(mapValue(persisted.get("parameters")))
                .stateRequirements(mapValue(persisted.get("stateRequirements")))
                .suggestionIntent(suggestionIntent);

        persisted.forEach((key, value) -> {
            if (!BLUEPRINT_FIELDS.contains(key)) {
                blueprint.putAdditionalProperty(key, value);
            }
        });
        return blueprint;
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
