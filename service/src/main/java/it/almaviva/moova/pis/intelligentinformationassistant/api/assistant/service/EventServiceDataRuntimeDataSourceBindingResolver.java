package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.List;
import java.util.Map;

public class EventServiceDataRuntimeDataSourceBindingResolver implements RuntimeDataSourceBindingResolver {

    private final AgentRuntimePackageConfiguration configuration;

    public EventServiceDataRuntimeDataSourceBindingResolver(AgentRuntimePackageConfiguration configuration) {
        this.configuration = configuration == null ? AgentRuntimePackageConfiguration.defaults() : configuration;
    }

    @Override
    public boolean supports(AgentActivationSnapshot snapshot) {
        return snapshot != null
                && "EVENT_INTERPRETER".equals(snapshot.interpreterType())
                && "EVENT".equals(snapshot.triggerType())
                && "ServiceDataV2".equals(snapshot.inputModel())
                && requiredSources(snapshot).contains("SERVICE_DATA");
    }

    @Override
    public List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> resolve(AgentActivationSnapshot snapshot) {
        AgentRuntimePackageConfiguration.ConnectorConfiguration connector = configuration.eventServiceDataConnector();
        if (connector == null) {
            throw new AgentRuntimePackageBuildException("Event ServiceData connector configuration is missing.");
        }
        AgentRuntimeSubmission.AgentRuntimeDataSourceBinding binding = new AgentRuntimeSubmission.AgentRuntimeDataSourceBinding(
                "primaryInput",
                "SERVICE_DATA",
                connector.accessMode(),
                connector.connectorType(),
                connector.connectorRef(),
                connector.inputModel(),
                connector.inputSchemaVersion(),
                configuration.bindingSchemaVersion(),
                connector.operationRef(),
                true,
                Map.of("subscriptionProfile", connector.subscriptionProfile()));
        System.out.println("[IIA][AGENT_ACTIVATION][PACKAGE] binding resolved agentDefinitionId=" + snapshot.agentDefinitionId()
                + " dataDomain=SERVICE_DATA accessMode=" + binding.accessMode()
                + " connectorType=" + binding.connectorType()
                + " connectorRef=" + binding.connectorRef());
        return List.of(binding);
    }

    private List<String> requiredSources(AgentActivationSnapshot snapshot) {
        return snapshot.requirements().requiredSources().stream()
                .map(AgentActivationSnapshot.AgentActivationSourceSnapshot::source)
                .toList();
    }
}
