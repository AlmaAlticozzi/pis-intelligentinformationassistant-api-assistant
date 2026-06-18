package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public enum LlmProviderFailureKind {
    AUTHENTICATION,
    AUTHORIZATION,
    INVALID_REQUEST,
    MODEL_NOT_FOUND,
    RATE_LIMIT,
    TIMEOUT,
    CONNECTION,
    PROVIDER_SERVICE_UNAVAILABLE,
    RESPONSE_PROCESSING,
    UNEXPECTED_INTERNAL
}
