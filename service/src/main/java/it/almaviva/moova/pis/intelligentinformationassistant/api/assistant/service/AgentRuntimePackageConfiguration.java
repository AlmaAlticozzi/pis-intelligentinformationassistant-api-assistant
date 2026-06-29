package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentRuntimePackageConfiguration(
        String controlPlaneComponent,
        String defaultRuntimeClass,
        String artifactCanonicalization,
        String artifactMediaType,
        String bindingSchemaVersion,
        ConnectorConfiguration eventServiceDataConnector,
        ConnectorConfiguration scheduledServiceDataConnector) {

    public static AgentRuntimePackageConfiguration defaults() {
        return new AgentRuntimePackageConfiguration(
                "pis-intelligentinformationassistant-api-assistant",
                "STANDARD_DSL_RUNTIME",
                "RFC8785_JSON",
                "application/json",
                "iia.runtime.data-source-binding/v1",
                new ConnectorConfiguration(
                        "servicedata-realtime-v2",
                        "KAFKA",
                        "EVENT_STREAM",
                        "ServiceDataV2",
                        "v2",
                        null,
                        "SERVICEDATA_EVENTS"),
                new ConnectorConfiguration(
                        "servicedata-stoppointjourneys-v2",
                        "HTTP_REST",
                        "SCHEDULED_QUERY",
                        "ServiceDataStopPointJourneysV2",
                        "v2",
                        "searchStopPointJourneysV2",
                        "SERVICEDATA_STOPPOINTJOURNEYS"));
    }

    public AgentRuntimePackageConfiguration {
        require(controlPlaneComponent, "controlPlaneComponent");
        require(defaultRuntimeClass, "defaultRuntimeClass");
        require(artifactCanonicalization, "artifactCanonicalization");
        require(artifactMediaType, "artifactMediaType");
        require(bindingSchemaVersion, "bindingSchemaVersion");
        if (eventServiceDataConnector == null) {
            throw new AgentRuntimePackageBuildException("Event ServiceData connector configuration is missing.");
        }
        if (scheduledServiceDataConnector == null) {
            throw new AgentRuntimePackageBuildException("Scheduled ServiceData connector configuration is missing.");
        }
    }

    public record ConnectorConfiguration(
            String connectorRef,
            String connectorType,
            String accessMode,
            String inputModel,
            String inputSchemaVersion,
            String operationRef,
            String subscriptionProfile) {

        public ConnectorConfiguration {
            require(connectorRef, "connectorRef");
            require(connectorType, "connectorType");
            require(accessMode, "accessMode");
            require(inputModel, "inputModel");
            require(inputSchemaVersion, "inputSchemaVersion");
        }
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new AgentRuntimePackageBuildException(field + " configuration is missing.");
        }
    }
}
