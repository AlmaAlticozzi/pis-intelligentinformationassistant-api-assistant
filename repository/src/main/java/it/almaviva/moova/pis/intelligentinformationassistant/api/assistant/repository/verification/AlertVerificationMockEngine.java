package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class AlertVerificationMockEngine {

    private static final String PROVIDER = "mock";
    private static final String MODEL = "mock-alert-verify";
    private static final String PROMPT_VERSION = "alert-verify-mvp-v1";
    private static final String INPUT_MODEL = "ServiceDataV2";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final String TRIGGER_TYPE = "EVENT";
    private static final String EVALUATION_MODE = "STATELESS_EVENT_MATCH";

    public AlertVerificationOutcome verify(String alertId, String prompt) {
        AlertVerificationOutcome outcome;

        if (prompt == null || prompt.isBlank()) {
            outcome = rejected("The alert prompt is empty.");
        } else {
            String normalizedPrompt = normalize(prompt);
            if (prompt.trim().length() < 10) {
                outcome = rejected("The alert prompt is too short to be verified.");
            } else if (isNonOperationalEncyclopedicPrompt(normalizedPrompt)) {
                outcome = rejected("The request is not an operational PIS alert.");
            } else if (containsAny(normalizedPrompt, "piove", "rain", "weather", "meteo")) {
                outcome = rejected("The request is outside the supported PIS domain.");
            } else if (containsAny(normalizedPrompt, "video", "dispositivo", "device", "display", "audio")) {
                outcome = rejected("The request belongs to the PIS domain but requires a data source not available in the current MVP.");
            } else if (containsAny(normalizedPrompt, "non ci sono corse", "nessuna corsa", "no journeys", "no departures", "assenza di", "non arriva nessun")) {
                outcome = rejected("The request requires stateful or time-window evaluation, which is not supported by the current stateless ServiceData interpreter.");
            } else if (containsAny(normalizedPrompt, "passeggeri", "passengers", "colore", "color", "rosso", "wi-fi", "wifi")) {
                outcome = rejected("The request contains a required constraint that is not available in the ServiceData capability catalog.");
            } else if (isServiceDataPrompt(normalizedPrompt)) {
                outcome = verified(deriveCondition(normalizedPrompt));
            } else {
                outcome = rejected("The request is not an operational PIS alert.");
            }
        }

        System.out.println("[IIA][ALERT_VERIFY][MOCK_ENGINE] decision=" + outcome.decision()
                + " reason=" + outcome.rejectedReason());
        return outcome;
    }

    private AlertVerificationOutcome verified(MockCondition condition) {
        List<String> requiredSources = List.of("SERVICE_DATA");
        List<String> interpretedTargetTypes = List.of("SERVICE_DATA_JOURNEY");
        Map<String, Object> technicalSpecification = Map.of(
                "schemaVersion", "iia.alert.technical-specification/v2",
                "source", "SERVICE_DATA",
                "inputModel", INPUT_MODEL,
                "outputModel", OUTPUT_MODEL,
                "triggerType", TRIGGER_TYPE,
                "evaluationMode", EVALUATION_MODE,
                "condition", condition.condition(),
                "deduplicationKeyTemplate", "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}");
        Map<String, Object> agentBlueprintPreview = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "MockVerifiedAlertAgent",
                "triggerType", TRIGGER_TYPE,
                "requiredSources", requiredSources,
                "evaluationMode", EVALUATION_MODE,
                "targetTypes", interpretedTargetTypes,
                "parameters", Map.of(
                        "conditionType", "SERVICE_DATA_FIELD_MATCH",
                        "condition", condition.condition()),
                "stateRequirements", Map.of(
                        "requiresState", false),
                "output", Map.of(
                        "type", "CANDIDATE_SUGGESTION"));

        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "Mock alert verification completed successfully.",
                null,
                0.80,
                PROVIDER,
                MODEL,
                PROMPT_VERSION,
                requiredSources,
                "EVENT_INTERPRETER",
                INPUT_MODEL,
                OUTPUT_MODEL,
                TRIGGER_TYPE,
                EVALUATION_MODE,
                List.of(condition.eventName()),
                interpretedTargetTypes,
                technicalSpecification,
                agentBlueprintPreview,
                requirementCoverage(condition),
                List.of(),
                safetyChecks());
    }

    private AlertVerificationOutcome rejected(String reason) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Mock alert verification rejected the prompt.",
                reason,
                0.0,
                PROVIDER,
                MODEL,
                PROMPT_VERSION,
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                Map.of(
                        "schemaVersion", "iia.alert.technical-specification/v1",
                        "decision", "REJECTED",
                        "rejectedReason", reason),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "canGenerate", false,
                        "rejectedReason", reason),
                Map.of(
                        "requirements", List.of(Map.of(
                                "text", reason,
                                "required", true,
                                "mappable", false,
                                "mappedBy", List.of(),
                                "reason", reason)),
                        "allRequiredRequirementsMapped", false),
                List.of(reason),
                safetyChecks());
    }

    private Map<String, Object> requirementCoverage(MockCondition condition) {
        return Map.of(
                "requirements", condition.fields().stream()
                        .map(field -> {
                            Map<String, Object> requirement = new LinkedHashMap<>();
                            requirement.put("text", "Mapped ServiceData condition on " + field);
                            requirement.put("required", true);
                            requirement.put("mappable", true);
                            requirement.put("mappedBy", List.of(field));
                            requirement.put("reason", null);
                            return requirement;
                        })
                        .toList(),
                "allRequiredRequirementsMapped", true);
    }

    private MockCondition deriveCondition(String normalizedPrompt) {
        List<Map<String, Object>> checks = new ArrayList<>();
        String eventName = "SERVICE_DATA_FIELD_MATCH";

        if (containsAny(normalizedPrompt, "cancelled", "cancellata", "cancellato", "soppressa", "soppresso", "suppression")) {
            eventName = "JOURNEY_CANCELLED";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"));
        }
        if (containsAny(normalizedPrompt, "ritardo", "delayed", "delay")) {
            eventName = "JOURNEY_DELAYED";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].departureDelay.delay", "EXISTS", null));
        }
        if (containsAny(normalizedPrompt, "binario", "platform")) {
            eventName = "PLATFORM_EVENT";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.displayPlatform.id", "EQUALS_NORMALIZED", platformValue(normalizedPrompt)));
        }
        if (containsAny(normalizedPrompt, "transito", "transit", "non si ferma", "passing through")) {
            eventName = "JOURNEY_TRANSIT";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].passingType", "EQUALS", "TRANSIT"));
        }
        if (containsAny(normalizedPrompt, "replacement", "sostituzione")) {
            eventName = "JOURNEY_REPLACEMENT";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].replacement", "EXISTS", null));
        }
        if (containsAny(normalizedPrompt, "origine", "origin")) {
            eventName = "JOURNEY_ORIGIN";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].passingType", "EQUALS", "ORIGIN"));
        }
        if (containsAny(normalizedPrompt, "almeno due transiti", "at least two transits")) {
            eventName = "JOURNEY_ROUTE_MATCH";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls", "SIZE_GREATER_OR_EQUAL", 2));
        }
        if (containsAny(normalizedPrompt, "passa da siena", "via siena", "transita da siena")) {
            eventName = "JOURNEY_ROUTE_MATCH";
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.nameLong", "CONTAINS_NORMALIZED", "Siena"));
        }
        if (containsAny(normalizedPrompt, "firenze")) {
            checks.add(leaf("payload.stopPointJourney.stopPoint.nameLong", "CONTAINS_NORMALIZED", "Firenze"));
        }
        if (containsAny(normalizedPrompt, "genova p.p", "genova pp", "genova p p")) {
            checks.add(leaf("payload.stopPointJourney.stopPoint.nameLong", "CONTAINS_NORMALIZED", "Genova P.P."));
        }
        if (checks.isEmpty() && containsAny(normalizedPrompt, "annunciata", "announced", "was announced")) {
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].deliveryData.wasAnnounced", "EQUALS", true));
        }
        if (checks.isEmpty() && containsAny(normalizedPrompt, "treno", "train")) {
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].transportMode.dsc", "CONTAINS_NORMALIZED", "train"));
        }
        if (checks.isEmpty()) {
            checks.add(leaf("payload.stopPointJourney.stopPointsJourneyDetails[].monitored", "EQUALS", true));
        }

        List<String> fields = checks.stream()
                .map(check -> String.valueOf(check.get("field")))
                .toList();
        return new MockCondition(eventName, Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", checks), fields);
    }

    private Map<String, Object> leaf(String field, String operator, Object value) {
        if (value == null) {
            return Map.of(
                    "field", field,
                    "operator", operator);
        }
        return Map.of(
                "field", field,
                "operator", operator,
                "value", value);
    }

    private String platformValue(String normalizedPrompt) {
        if (containsAny(normalizedPrompt, "binario 1", "platform 1")) {
            return "1";
        }
        return "platform";
    }

    private boolean isNonOperationalEncyclopedicPrompt(String normalizedPrompt) {
        return containsAny(normalizedPrompt, "quando e nato", "chi e", "wikipedia", "garibaldi")
                && !containsAny(normalizedPrompt, "corsa", "journey", "treno", "train", "stazione", "stop point");
    }

    private boolean isServiceDataPrompt(String normalizedPrompt) {
        return containsAny(
                normalizedPrompt,
                "cancelled",
                "cancellata",
                "cancellato",
                "soppressa",
                "soppresso",
                "suppression",
                "ritardo",
                "delayed",
                "delay",
                "binario",
                "platform",
                "corsa",
                "journey",
                "treno",
                "train",
                "transito",
                "transit",
                "non si ferma",
                "replacement",
                "sostituzione",
                "origine",
                "origin",
                "passa da",
                "annunciata",
                "announced");
    }

    private List<String> safetyChecks() {
        return List.of(
                "No executable code generated.",
                "No Agent Definition created.",
                "No Suggestion created.");
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }

    private record MockCondition(String eventName, Map<String, Object> condition, List<String> fields) {
    }
}
