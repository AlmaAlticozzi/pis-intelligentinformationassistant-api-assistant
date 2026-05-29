package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record AlertVerificationPromptData(
        String alertId,
        String name,
        String description,
        String prompt,
        AlertVerificationLocationContext locationResolutionContext
) {
    public AlertVerificationPromptData(String alertId, String name, String description, String prompt) {
        this(alertId, name, description, prompt, AlertVerificationLocationContext.empty());
    }

    public AlertVerificationPromptData {
        locationResolutionContext = locationResolutionContext == null
                ? AlertVerificationLocationContext.empty()
                : locationResolutionContext;
    }
}
