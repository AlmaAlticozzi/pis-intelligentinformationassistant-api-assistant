package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
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
            } else if (containsAny(normalizedPrompt, "video", "dispositivo", "device", "display", "audio", "annuncio", "announcement")) {
                outcome = rejected("The request belongs to the PIS domain but requires a data source not available in the current MVP.");
            } else if (containsAny(normalizedPrompt, "non ci sono corse", "nessuna corsa", "no journeys", "no departures", "assenza di", "non arriva nessun")) {
                outcome = rejected("The request requires stateful or time-window evaluation, which is not supported by the current stateless ServiceData interpreter.");
            } else if (isServiceDataPrompt(normalizedPrompt)) {
                outcome = verified(alertId, prompt, deriveConditionType(normalizedPrompt));
            } else {
                outcome = rejected("The request is not an operational PIS alert.");
            }
        }

        System.out.println("[IIA][ALERT_VERIFY][MOCK_ENGINE] decision=" + outcome.decision()
                + " reason=" + outcome.rejectedReason());
        return outcome;
    }

    private AlertVerificationOutcome verified(String alertId, String prompt, String conditionType) {
        List<String> requiredSources = List.of("SERVICE_DATA");
        List<String> interpretedEventNames = List.of(conditionType);
        List<String> interpretedTargetTypes = List.of("SERVICE_DATA_JOURNEY");
        Map<String, Object> technicalSpecification = Map.of(
                "schemaVersion", "iia.alert.technical-specification/v1",
                "source", "SERVICE_DATA",
                "inputModel", INPUT_MODEL,
                "outputModel", OUTPUT_MODEL,
                "triggerType", TRIGGER_TYPE,
                "evaluationMode", EVALUATION_MODE,
                "condition", Map.of(
                        "type", conditionType),
                "deduplicationKeyTemplate", "MOCK:${alertId}:${eventId}");
        Map<String, Object> agentBlueprintPreview = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "MockVerifiedAlertAgent",
                "triggerType", TRIGGER_TYPE,
                "requiredSources", requiredSources,
                "evaluationMode", EVALUATION_MODE,
                "targetTypes", interpretedTargetTypes,
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
                interpretedEventNames,
                interpretedTargetTypes,
                technicalSpecification,
                agentBlueprintPreview,
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
                List.of(reason),
                safetyChecks());
    }

    private String deriveConditionType(String normalizedPrompt) {
        if (containsAny(normalizedPrompt, "cancelled", "cancellata", "cancellato", "soppressa", "soppresso", "suppression")) {
            return "JOURNEY_CANCELLED";
        }
        if (containsAny(normalizedPrompt, "ritardo", "delayed", "delay")) {
            return "JOURNEY_DELAYED";
        }
        if (containsAny(normalizedPrompt, "binario", "platform")) {
            return "PLATFORM_EVENT";
        }
        return "GENERIC_SERVICE_DATA_EVENT";
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
                "train");
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
}
