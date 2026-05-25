package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
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

    private final AgentGenerationCapabilityCatalog capabilityCatalog;
    private final AgentPreviewConditionExtractor conditionExtractor;
    private final AgentPreviewNamingHelper namingHelper;
    private final AgentDslPreviewBuilder dslPreviewBuilder;
    private final AgentValidationPlanBuilder validationPlanBuilder;

    public AgentGenerationPreviewMapper() {
        this(new AgentGenerationCapabilityCatalog());
    }

    @Inject
    public AgentGenerationPreviewMapper(AgentGenerationCapabilityCatalog capabilityCatalog) {
        this.capabilityCatalog = capabilityCatalog;
        this.conditionExtractor = new AgentPreviewConditionExtractor();
        this.namingHelper = new AgentPreviewNamingHelper();
        this.dslPreviewBuilder = new AgentDslPreviewBuilder(capabilityCatalog);
        this.validationPlanBuilder = new AgentValidationPlanBuilder();
    }

    public AgentGenerationPreviewResponse toResponse(
            AlertAgentGenerationPreviewData data,
            AgentGenerationPreviewRequest request) {
        AgentPreviewConditionExtractor.ConditionSummary conditionSummary = conditionExtractor.extract(data);
        System.out.println("[IIA][AGENT_PREVIEW] Extracted preview condition summary alertId=" + data.alertId()
                + ", conditionType=" + conditionSummary.conditionType()
                + ", location=" + conditionSummary.location()
                + ", cancellation=" + conditionSummary.cancellation());

        List<String> sourceNames = resolveSourceNames(data);
        List<AgentDataSource> requiredSources = sourceNames.stream()
                .map(this::toDataSource)
                .filter(value -> value != null)
                .toList();
        List<String> requiredPermissions = capabilityCatalog.permissionsForSources(sourceNames);
        AgentGenerationMode requestedMode = request == null ? null : request.getPreferredGenerationMode();
        AgentGenerationMode recommendedMode = capabilityCatalog.recommendedDefaultGenerationMode();

        AgentBlueprint blueprint = toBlueprint(data, conditionSummary, requiredSources);
        System.out.println("[IIA][AGENT_PREVIEW] Rendering deterministic DSL preview alertId=" + data.alertId());
        AgentDslPreviewBuilder.BuildResult dslResult = dslPreviewBuilder.build(
                data,
                blueprint,
                conditionSummary,
                requestedMode == null ? null : requestedMode.toString(),
                recommendedMode.toString(),
                sourceNames,
                requiredPermissions);
        LinkedHashSet<String> warnings = new LinkedHashSet<>(data.warnings() == null ? List.of() : data.warnings());
        warnings.add("Read-only preview generated from verified Alert artifacts; no Agent Definition has been created.");
        warnings.add("DSL preview is diagnostic and has not been compiled or executed.");
        if (requestedMode != null && capabilityCatalog.isPreviewOnlyGenerationMode(requestedMode.toString())) {
            System.out.println("[IIA][AGENT_CAPABILITY] Requested generation mode=" + requestedMode
                    + " is preview-only; falling back to " + recommendedMode);
            warnings.add("JAVA_TEMPLATE was requested but is not generated in the read-only preview MVP; DSL preview is returned when possible.");
        }
        if (dslResult.partial()) {
            warnings.add("DSL preview is partial because some condition nodes are not supported by the deterministic renderer.");
        }
        if (!dslResult.supportedByRuntime()) {
            dslResult.unsupportedCapabilities().forEach(capability ->
                    warnings.add("Capability '" + capability + "' is not supported by the current Agent Generation MVP."));
            warnings.add("The preview can be displayed, but the current runtime catalog does not fully support all required capabilities.");
        }

        AgentGenerationPreviewResponse response = new AgentGenerationPreviewResponse()
                .alert(new AlertReference().id(data.alertId()).name(data.name()))
                .canGenerate(true)
                .recommendedGenerationMode(recommendedMode)
                .estimatedComplexity(capabilityCatalog.defaultMvpComplexity())
                .requiredSources(requiredSources)
                .requiredPermissions(requiredPermissions)
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
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary,
            List<AgentDataSource> requiredSources) {
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
                .requiredSources(requiredSources)
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

    private List<String> resolveSourceNames(AlertAgentGenerationPreviewData data) {
        Object persistedSources = data.agentBlueprintPreview().get("requiredSources");
        List<String> result = new ArrayList<>();
        if (persistedSources instanceof List<?> sources) {
            sources.stream()
                    .map(this::stringValue)
                    .filter(value -> value != null && !value.isBlank())
                    .forEach(result::add);
        }
        if (result.isEmpty()) {
            String technicalSource = stringValue(data.technicalSpecification().get("source"));
            if (technicalSource != null && !technicalSource.isBlank()) {
                result.add(technicalSource);
            }
        }
        return result.isEmpty() ? List.of(capabilityCatalog.defaultSource().toString()) : List.copyOf(result);
    }

    private AgentDataSource toDataSource(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return AgentDataSource.fromString(source);
        } catch (IllegalArgumentException exception) {
            return null;
        }
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
