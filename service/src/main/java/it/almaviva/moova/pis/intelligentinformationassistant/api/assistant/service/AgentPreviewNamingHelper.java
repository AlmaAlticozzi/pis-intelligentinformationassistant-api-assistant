package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.text.Normalizer;

class AgentPreviewNamingHelper {

    String agentName(
            AlertAgentGenerationPreviewData data,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        if (conditionSummary.platformChange()) {
            String location = compact(conditionSummary.location());
            String result = "PlatformChange" + (location.isBlank() ? "AnyLocation" : location) + "Agent";
            return result.length() <= 120 ? result : result.substring(0, 115) + "Agent";
        }
        if (conditionSummary.delay() && hasText(conditionSummary.location())) {
            String serviceType = compact(conditionSummary.serviceType());
            String location = compact(conditionSummary.location());
            String result = "JourneyDelay" + serviceType + location + "Agent";
            return result.length() <= 120 ? result : result.substring(0, 115) + "Agent";
        }
        if (conditionSummary.cancellation() && hasText(conditionSummary.location())) {
            String location = compact(conditionSummary.location());
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
        if (conditionSummary.platformChange()) {
            String location = hasText(conditionSummary.location()) ? conditionSummary.location() : "any location";
            return "Detects train platform changes at " + location + " from realtime ServiceData events.";
        }
        if (conditionSummary.delay() && hasText(conditionSummary.location())) {
            String service = hasText(conditionSummary.serviceType()) ? conditionSummary.serviceType() + " " : "";
            return "Detects delayed " + service + "journeys at " + conditionSummary.location()
                    + " from realtime ServiceData events.";
        }
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

    private String compact(String value) {
        return hasText(value)
                ? Normalizer.normalize(value, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .replaceAll("[^A-Za-z0-9]", "")
                : "";
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
