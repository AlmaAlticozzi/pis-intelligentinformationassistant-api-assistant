package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

/**
 * Identifies the AI workflow that is requesting text generation.
 */
public enum AiUseCase {
    TEXT_IMPROVE,
    ALERT_VERIFY,
    ALERT_LOCATION_UNDERSTANDING,
    AGENT_BLUEPRINT_GENERATE,
    AGENT_DSL_GENERATE,
    SUGGESTION_TEXT_NORMALIZE,
    ASSISTANT_ANSWER
}
