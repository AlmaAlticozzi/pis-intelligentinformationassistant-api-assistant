package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentRuntimePackageConfiguration(
        String controlPlaneComponent,
        String defaultRuntimeClass,
        String artifactCanonicalization,
        String artifactMediaType,
        String bindingSchemaVersion,
        ConnectorConfiguration eventServiceDataConnector,
        ConnectorConfiguration scheduledServiceDataConnector,
        String minimumRuntimeVersion,
        String sdkVersion,
        String networkPolicy) {

    public AgentRuntimePackageConfiguration(
            String controlPlaneComponent,
            String defaultRuntimeClass,
            String artifactCanonicalization,
            String artifactMediaType,
            String bindingSchemaVersion,
            ConnectorConfiguration eventServiceDataConnector,
            ConnectorConfiguration scheduledServiceDataConnector) {
        this(controlPlaneComponent, defaultRuntimeClass, artifactCanonicalization, artifactMediaType,
                bindingSchemaVersion, eventServiceDataConnector, scheduledServiceDataConnector,
                "0.0.2", "1.0.0", "REGISTERED_DATA_SOURCES_ONLY");
    }

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
                        "SERVICEDATA_STOPPOINTJOURNEYS"),
                "0.0.2",
                "1.0.0",
                "REGISTERED_DATA_SOURCES_ONLY");
    }

    public AgentRuntimePackageConfiguration {
        require(controlPlaneComponent, "controlPlaneComponent");
        require(defaultRuntimeClass, "defaultRuntimeClass");
        require(artifactCanonicalization, "artifactCanonicalization");
        require(artifactMediaType, "artifactMediaType");
        require(bindingSchemaVersion, "bindingSchemaVersion");
        require(minimumRuntimeVersion, "minimumRuntimeVersion");
        if (!minimumRuntimeVersion.matches("[0-9]+\\.[0-9]+\\.[0-9]+")) {
            throw new AgentRuntimePackageBuildException("minimumRuntimeVersion must use MAJOR.MINOR.PATCH format.");
        }
        require(sdkVersion, "sdkVersion");
        require(networkPolicy, "networkPolicy");
        if (!"REGISTERED_DATA_SOURCES_ONLY".equals(networkPolicy)) {
            throw new AgentRuntimePackageBuildException("Unsupported runtime networkPolicy " + networkPolicy + ".");
        }
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
