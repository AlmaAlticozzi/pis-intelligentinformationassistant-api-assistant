package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;
import java.util.Map;

public record AlertVerificationOutcome(
        AlertVerificationDecision decision,
        String summary,
        String rejectedReason,
        Double confidence,
        String provider,
        String model,
        String promptVersion,
        List<String> requiredSources,
        String interpreterType,
        String inputModel,
        String outputModel,
        String triggerType,
        String evaluationMode,
        List<String> interpretedEventNames,
        List<String> interpretedTargetTypes,
        Map<String, Object> technicalSpecification,
        Map<String, Object> agentBlueprintPreview,
        List<String> warnings,
        List<String> safetyChecks
) {
    public AlertVerificationOutcome withAdditionalWarning(String warning) {
        java.util.ArrayList<String> updatedWarnings = new java.util.ArrayList<>(warnings == null ? List.of() : warnings);
        updatedWarnings.add(warning);
        return new AlertVerificationOutcome(
                decision,
                summary,
                rejectedReason,
                confidence,
                provider,
                model,
                promptVersion,
                requiredSources,
                interpreterType,
                inputModel,
                outputModel,
                triggerType,
                evaluationMode,
                interpretedEventNames,
                interpretedTargetTypes,
                technicalSpecification,
                agentBlueprintPreview,
                List.copyOf(updatedWarnings),
                safetyChecks);
    }
}
