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
}
