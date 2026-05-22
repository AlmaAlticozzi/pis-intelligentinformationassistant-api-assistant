package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error.AssistantApiErrors;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public final class AssistantApiInputValidator {

    private static final int ALERT_SEARCH_TEXT_MAX_LENGTH = 200;
    private static final int ALERT_ID_MAX_LENGTH = 50;
    private static final int ALERT_NAME_MAX_LENGTH = 120;
    private static final int ALERT_DESCRIPTION_MAX_LENGTH = 1000;
    private static final int ALERT_PROMPT_MIN_LENGTH = 10;
    private static final int ALERT_PROMPT_MAX_LENGTH = 8000;
    private static final int TEXT_IMPROVE_INPUT_MAX_LENGTH = 8000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AssistantApiInputValidator() {
    }

    public static AlertSearchCriteria validateAlertSearch(String status, String enabled, String interpreterType, String text) {
        return new AlertSearchCriteria(
                parseAlertStatus(status),
                parseBoolean(enabled),
                parseAlertInterpreterType(interpreterType),
                validateAlertSearchText(text));
    }

    public static String validateAlertIdForGet(String alertId) {
        if (alertId == null || alertId.isBlank()) {
            throw badRequest(AssistantApiErrors.alertGetBlankAlertId());
        }
        if (alertId.length() > ALERT_ID_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertGetAlertIdTooLong());
        }
        return alertId;
    }

    public static String validateAlertIdForVerify(String alertId) {
        if (alertId == null || alertId.isBlank()) {
            throw badRequest(AssistantApiErrors.alertVerifyBlankAlertId());
        }
        if (alertId.length() > ALERT_ID_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertVerifyAlertIdTooLong());
        }
        return alertId;
    }

    public static String validateAlertIdForUpdate(String alertId) {
        if (alertId == null || alertId.isBlank()) {
            throw badRequest(AssistantApiErrors.alertUpdateBlankAlertId());
        }
        if (alertId.length() > ALERT_ID_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertUpdateAlertIdTooLong());
        }
        return alertId;
    }

    public static AlertCreateRequest validateAlertCreate(AlertCreateRequest request) {
        if (request == null) {
            throw badRequest(AssistantApiErrors.alertCreateMissingBody());
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw badRequest(AssistantApiErrors.alertCreateBlankName());
        }
        if (request.getName().length() > ALERT_NAME_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertCreateNameTooLong());
        }
        if (request.getDescription() != null && request.getDescription().length() > ALERT_DESCRIPTION_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertCreateDescriptionTooLong());
        }
        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            throw badRequest(AssistantApiErrors.alertCreateBlankPrompt());
        }
        String trimmedPrompt = request.getPrompt().trim();
        if (trimmedPrompt.length() < ALERT_PROMPT_MIN_LENGTH || trimmedPrompt.length() > ALERT_PROMPT_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertCreatePromptInvalidLength());
        }
        if (request.getVerifyImmediately() == null) {
            throw badRequest(AssistantApiErrors.alertCreateInvalidVerifyImmediately());
        }
        if (request.getEnableAfterVerification() == null) {
            throw badRequest(AssistantApiErrors.alertCreateInvalidEnableAfterVerification());
        }
        return request;
    }

    public static AlertUpdateRequest validateAlertUpdate(AlertUpdateRequest request) {
        if (request == null) {
            throw badRequest(AssistantApiErrors.alertUpdateMissingBody());
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw badRequest(AssistantApiErrors.alertUpdateBlankName());
        }
        if (request.getName().length() > ALERT_NAME_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertUpdateNameTooLong());
        }
        if (request.getDescription() != null && request.getDescription().length() > ALERT_DESCRIPTION_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertUpdateDescriptionTooLong());
        }
        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            throw badRequest(AssistantApiErrors.alertUpdatePromptInvalid());
        }
        String trimmedPrompt = request.getPrompt().trim();
        if (trimmedPrompt.length() < ALERT_PROMPT_MIN_LENGTH || trimmedPrompt.length() > ALERT_PROMPT_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertUpdatePromptInvalid());
        }
        return request;
    }

    public static String validateTextImproveInput(String inputText) {
        if (inputText == null) {
            throw badRequest(AssistantApiErrors.textImproveMissingBody());
        }

        String normalizedInput = normalizeJsonStringBody(inputText);
        if (normalizedInput.isEmpty()) {
            throw badRequest(AssistantApiErrors.textImproveBlankText());
        }
        if (normalizedInput.length() > TEXT_IMPROVE_INPUT_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.textImproveTextTooLong());
        }

        return normalizedInput;
    }

    private static String normalizeJsonStringBody(String inputText) {
        String trimmedBody = inputText.trim();
        if (trimmedBody.isEmpty()) {
            return trimmedBody;
        }

        if (trimmedBody.startsWith("\"")) {
            try {
                return OBJECT_MAPPER.readValue(trimmedBody, String.class).trim();
            } catch (JsonProcessingException ex) {
                throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
            }
        }

        return trimmedBody;
    }

    private static AlertStatus parseAlertStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return AlertStatus.fromString(status);
        } catch (IllegalArgumentException ex) {
            throw badRequest(AssistantApiErrors.alertSearchInvalidStatus());
        }
    }

    private static Boolean parseBoolean(String enabled) {
        if (enabled == null) {
            return null;
        }
        if ("true".equalsIgnoreCase(enabled)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(enabled)) {
            return Boolean.FALSE;
        }
        throw badRequest(AssistantApiErrors.alertSearchInvalidEnabled());
    }

    private static AlertInterpreterType parseAlertInterpreterType(String interpreterType) {
        if (interpreterType == null) {
            return null;
        }
        try {
            return AlertInterpreterType.fromString(interpreterType);
        } catch (IllegalArgumentException ex) {
            throw badRequest(AssistantApiErrors.alertSearchInvalidInterpreterType());
        }
    }

    private static String validateAlertSearchText(String text) {
        if (text != null && text.length() > ALERT_SEARCH_TEXT_MAX_LENGTH) {
            throw badRequest(AssistantApiErrors.alertSearchTextTooLong());
        }
        return text;
    }

    private static WebApplicationException badRequest(Error error) {
        return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }
}
