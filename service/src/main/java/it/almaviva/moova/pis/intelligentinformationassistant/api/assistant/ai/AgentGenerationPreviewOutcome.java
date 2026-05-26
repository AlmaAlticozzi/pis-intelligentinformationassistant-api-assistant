package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;
import java.util.Map;

public record AgentGenerationPreviewOutcome(
        boolean canGenerate,
        String recommendedGenerationMode,
        String estimatedComplexity,
        List<String> requiredSources,
        List<String> requiredPermissions,
        Map<String, Object> blueprint,
        List<String> warnings,
        String rejectedReason) {
}
