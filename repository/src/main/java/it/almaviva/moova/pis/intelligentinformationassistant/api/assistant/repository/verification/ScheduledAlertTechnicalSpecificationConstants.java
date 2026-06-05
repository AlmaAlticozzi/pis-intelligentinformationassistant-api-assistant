package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public final class ScheduledAlertTechnicalSpecificationConstants {

    public static final String TECHNICAL_SPECIFICATION_SCHEMA_VERSION = "iia.alert.technical-specification/v2";
    public static final String AGENT_BLUEPRINT_SCHEMA_VERSION = "iia.agent.blueprint/v1";
    public static final String INPUT_MODEL = "ServiceDataStopPointJourneysV2";
    public static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    public static final String TRIGGER_TYPE = "SCHEDULE";
    public static final String EVALUATION_MODE = "SCHEDULED_SNAPSHOT_MATCH";
    public static final String SOURCE = "SERVICE_DATA";
    public static final String INTERPRETER_TYPE = "SCHEDULED_INTERPRETER";
    public static final String ACCESS_MODE = "SERVICE_DATA_API_SNAPSHOT";
    public static final String DEFAULT_OPERATION = "POST /v2/stoppointjourneys";
    public static final String DEFAULT_DEDUPLICATION_KEY_TEMPLATE =
            "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}";
    public static final String DEFAULT_AGENT_NAME = "ScheduledServiceDataSnapshotAlertAgent";
    public static final String DEFAULT_TARGET_TYPE = "SERVICE_DATA_JOURNEY_AGGREGATE";

    private ScheduledAlertTechnicalSpecificationConstants() {
    }
}
