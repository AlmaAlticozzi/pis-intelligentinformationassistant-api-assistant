package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AlertAgentGenerationPreviewData(
        String alertId,
        String name,
        String status,
        String verificationStatus,
        Boolean enabled,
        OffsetDateTime deletedAt,
        Integer version,
        String prompt,
        String verificationSummary,
        String rejectedReason,
        BigDecimal verificationConfidence,
        String interpreterType,
        String inputModel,
        String outputModel,
        Map<String, Object> technicalSpecification,
        Map<String, Object> agentBlueprintPreview,
        List<String> interpretedEventNames,
        List<String> warnings,
        List<SuggestionTargetType> targetTypes) {
}
