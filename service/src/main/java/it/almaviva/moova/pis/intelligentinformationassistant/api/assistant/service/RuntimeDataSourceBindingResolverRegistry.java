package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuntimeDataSourceBindingResolverRegistry {

    private final List<RuntimeDataSourceBindingResolver> resolvers;

    public RuntimeDataSourceBindingResolverRegistry(List<RuntimeDataSourceBindingResolver> resolvers) {
        this.resolvers = resolvers == null ? List.of() : List.copyOf(resolvers);
    }

    public List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> resolve(AgentActivationSnapshot snapshot) {
        List<RuntimeDataSourceBindingResolver> matching = resolvers.stream()
                .filter(resolver -> resolver.supports(snapshot))
                .toList();
        if (matching.isEmpty()) {
            throw new AgentRuntimePackageBuildException("No RuntimeDataSourceBindingResolver supports the activation snapshot.");
        }
        if (matching.size() > 1) {
            throw new AgentRuntimePackageBuildException("Multiple RuntimeDataSourceBindingResolvers support the activation snapshot.");
        }
        List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> bindings = matching.getFirst().resolve(snapshot).stream()
                .sorted(Comparator.comparing(AgentRuntimeSubmission.AgentRuntimeDataSourceBinding::bindingId))
                .toList();
        Set<String> bindingIds = new HashSet<>();
        for (AgentRuntimeSubmission.AgentRuntimeDataSourceBinding binding : bindings) {
            if (binding.bindingId() == null || binding.bindingId().isBlank() || !bindingIds.add(binding.bindingId())) {
                throw new AgentRuntimePackageBuildException("Runtime data-source bindings contain duplicate or blank bindingId.");
            }
        }
        return bindings;
    }
}
