package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AgentRuntimePackageConfigurationProvider {

    @ConfigProperty(name = "iia.agent-runtime-package.control-plane-component", defaultValue = "pis-intelligentinformationassistant-api-assistant")
    String controlPlaneComponent;

    @ConfigProperty(name = "iia.agent-runtime-package.default-runtime-class", defaultValue = "STANDARD_DSL_RUNTIME")
    String defaultRuntimeClass;

    @ConfigProperty(name = "iia.agent-runtime-package.artifact-canonicalization", defaultValue = "JACKSON_SORT_PROPERTIES_AND_MAP_ENTRIES")
    String artifactCanonicalization;

    @ConfigProperty(name = "iia.agent-runtime-package.artifact-media-type", defaultValue = "application/json")
    String artifactMediaType;

    @ConfigProperty(name = "iia.agent-runtime-package.binding-schema-version", defaultValue = "iia.runtime.data-source-binding/v1")
    String bindingSchemaVersion;

    @ConfigProperty(name = "iia.agent-runtime-package.event-service-data.connector-ref", defaultValue = "servicedata-realtime-v2")
    String eventConnectorRef;

    @ConfigProperty(name = "iia.agent-runtime-package.event-service-data.connector-type", defaultValue = "KAFKA")
    String eventConnectorType;

    @ConfigProperty(name = "iia.agent-runtime-package.event-service-data.access-mode", defaultValue = "EVENT_STREAM")
    String eventAccessMode;

    @ConfigProperty(name = "iia.agent-runtime-package.event-service-data.input-model", defaultValue = "ServiceDataV2")
    String eventInputModel;

    @ConfigProperty(name = "iia.agent-runtime-package.event-service-data.input-schema-version", defaultValue = "v2")
    String eventInputSchemaVersion;

    @ConfigProperty(name = "iia.agent-runtime-package.event-service-data.subscription-profile", defaultValue = "SERVICEDATA_EVENTS")
    String eventSubscriptionProfile;

    @ConfigProperty(name = "iia.agent-runtime-package.scheduled-service-data.connector-ref", defaultValue = "servicedata-stoppointjourneys-v2")
    String scheduledConnectorRef;

    @ConfigProperty(name = "iia.agent-runtime-package.scheduled-service-data.connector-type", defaultValue = "HTTP_REST")
    String scheduledConnectorType;

    @ConfigProperty(name = "iia.agent-runtime-package.scheduled-service-data.access-mode", defaultValue = "SCHEDULED_QUERY")
    String scheduledAccessMode;

    @ConfigProperty(name = "iia.agent-runtime-package.scheduled-service-data.input-model", defaultValue = "ServiceDataStopPointJourneysV2")
    String scheduledInputModel;

    @ConfigProperty(name = "iia.agent-runtime-package.scheduled-service-data.input-schema-version", defaultValue = "v2")
    String scheduledInputSchemaVersion;

    @ConfigProperty(name = "iia.agent-runtime-package.scheduled-service-data.operation-ref", defaultValue = "searchStopPointJourneysV2")
    String scheduledOperationRef;

    @ConfigProperty(name = "iia.agent-runtime-package.scheduled-service-data.subscription-profile", defaultValue = "SERVICEDATA_STOPPOINTJOURNEYS")
    String scheduledSubscriptionProfile;

    public AgentRuntimePackageConfiguration configuration() {
        return new AgentRuntimePackageConfiguration(
                controlPlaneComponent,
                defaultRuntimeClass,
                artifactCanonicalization,
                artifactMediaType,
                bindingSchemaVersion,
                new AgentRuntimePackageConfiguration.ConnectorConfiguration(
                        eventConnectorRef,
                        eventConnectorType,
                        eventAccessMode,
                        eventInputModel,
                        eventInputSchemaVersion,
                        null,
                        eventSubscriptionProfile),
                new AgentRuntimePackageConfiguration.ConnectorConfiguration(
                        scheduledConnectorRef,
                        scheduledConnectorType,
                        scheduledAccessMode,
                        scheduledInputModel,
                        scheduledInputSchemaVersion,
                        scheduledOperationRef,
                        scheduledSubscriptionProfile));
    }
}
