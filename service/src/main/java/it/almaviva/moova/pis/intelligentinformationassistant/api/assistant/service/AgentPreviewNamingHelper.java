package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.text.Normalizer;

class AgentPreviewNamingHelper {

    String agentName(
            AlertAgentGenerationPreviewData data,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        if (conditionSummary.cancellation() && hasText(conditionSummary.location())) {
            String location = Normalizer.normalize(conditionSummary.location(), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replaceAll("[^A-Za-z0-9]", "");
            if (!location.isBlank()) {
                String result = "JourneyCancellation" + location + "Agent";
                return result.length() <= 120 ? result : result.substring(0, 115) + "Agent";
            }
        }
        String persistedName = stringValue(data.agentBlueprintPreview().get("agentName"));
        return hasText(persistedName) ? persistedName : "ServiceDataFieldMatchAlertAgent";
    }

    String description(
            AlertAgentGenerationPreviewData data,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        if (conditionSummary.cancellation() && hasText(conditionSummary.location())) {
            return "Detects cancelled journeys at " + conditionSummary.location()
                    + " from realtime ServiceData events.";
        }
        String persistedDescription = stringValue(data.agentBlueprintPreview().get("description"));
        if (hasText(persistedDescription)) {
            return persistedDescription;
        }
        return hasText(data.verificationSummary())
                ? data.verificationSummary()
                : "Detects matching ServiceData events using the verified stateless condition.";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
