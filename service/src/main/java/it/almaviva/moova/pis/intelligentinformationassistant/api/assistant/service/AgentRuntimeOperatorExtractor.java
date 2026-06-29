package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class AgentRuntimeOperatorExtractor {

    private final AgentGenerationCapabilityCatalog capabilityCatalog;

    public AgentRuntimeOperatorExtractor() {
        this(new AgentGenerationCapabilityCatalog());
    }

    AgentRuntimeOperatorExtractor(AgentGenerationCapabilityCatalog capabilityCatalog) {
        this.capabilityCatalog = capabilityCatalog;
    }

    public List<String> extract(Map<String, Object> artifact) {
        TreeSet<String> operators = new TreeSet<>();
        if (artifact != null) {
            visit(artifact.get("evaluation"), operators);
        }
        return List.copyOf(operators);
    }

    private void visit(Object node, TreeSet<String> operators) {
        if (node instanceof Map<?, ?> map) {
            if (map.containsKey("operator")) {
                Object raw = map.get("operator");
                if (!(raw instanceof String operator) || operator.isBlank()) {
                    throw new AgentRuntimePackageBuildException("DSL condition operator is null or blank.");
                }
                if (!capabilityCatalog.isSupportedDslOperator(operator)) {
                    throw new AgentRuntimePackageBuildException("Unsupported DSL condition operator " + operator + ".");
                }
                operators.add(operator);
            }
            map.values().forEach(value -> visit(value, operators));
        } else if (node instanceof Collection<?> collection) {
            collection.forEach(value -> visit(value, operators));
        }
    }
}
