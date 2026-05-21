package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record AlertVerificationPromptData(
        String alertId,
        String name,
        String description,
        String prompt
) {
}
