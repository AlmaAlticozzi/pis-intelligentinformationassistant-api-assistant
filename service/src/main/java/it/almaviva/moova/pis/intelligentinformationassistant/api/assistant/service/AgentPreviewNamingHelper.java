package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

class AgentPreviewNamingHelper {

    String agentName(
            AlertAgentGenerationPreviewData data,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        String transitName = transitTemporalAgentName(conditionSummary);
        if (hasText(transitName)) {
            return transitName;
        }
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

    private String transitTemporalAgentName(AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        AgentPreviewConditionExtractor.ArrayElementCondition journeyDetail =
                conditionSummary.arrayConditions().stream()
                        .filter(condition -> condition.path().endsWith("stopPointsJourneyDetails[]"))
                        .findFirst()
                        .orElse(null);
        AgentPreviewConditionExtractor.ArrayElementCondition nextTransit =
                conditionSummary.arrayConditions().stream()
                        .filter(condition -> condition.path().endsWith("nextTransitCalls[]"))
                        .findFirst()
                        .orElse(null);
        if (journeyDetail == null || nextTransit == null) {
            return null;
        }
        String origin = journeyDetail.leaves().stream()
                .filter(leaf -> "timetabledCallStart.stopPoint.nameLong".equals(leaf.field()))
                .map(AgentPreviewConditionExtractor.ConditionLeaf::value)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
        String transitStop = nextTransit.leaves().stream()
                .filter(leaf -> "stopPoint.nameLong".equals(leaf.field()))
                .map(AgentPreviewConditionExtractor.ConditionLeaf::value)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
        String day = nextTransit.leaves().stream()
                .filter(leaf -> "LOCAL_DAY_OF_WEEK_IN".equals(leaf.operator()))
                .map(leaf -> dayFromValue(leaf.rawValue()))
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
        if (!hasText(transitStop) || !hasText(day)) {
            return null;
        }
        String result = compact(origin) + "To" + compact(transitStop) + compact(day) + "TransitAgent";
        if (!hasText(origin)) {
            result = compact(transitStop) + compact(day) + "TransitAgent";
        }
        return result.length() <= 120 ? result : result.substring(0, 115) + "Agent";
    }

    private String dayFromValue(Object rawValue) {
        if (!(rawValue instanceof Map<?, ?> value) || !(value.get("days") instanceof List<?> days) || days.isEmpty()) {
            return null;
        }
        String day = String.valueOf(days.getFirst()).toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(day.charAt(0)) + day.substring(1);
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
