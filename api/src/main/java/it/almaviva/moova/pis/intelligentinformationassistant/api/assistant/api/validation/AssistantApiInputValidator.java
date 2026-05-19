package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.validation;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error.AssistantApiErrors;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
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
            if (!trimmedBody.endsWith("\"") || trimmedBody.length() < 2) {
                throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
            }
            return unescapeJsonString(trimmedBody.substring(1, trimmedBody.length() - 1)).trim();
        }

        throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
    }

    private static String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '\\') {
                if (current == '"') {
                    throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
                }
                result.append(current);
                continue;
            }

            if (++index >= value.length()) {
                throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
            }

            char escaped = value.charAt(index);
            switch (escaped) {
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'u' -> {
                    if (index + 4 >= value.length()) {
                        throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
                    }
                    String hex = value.substring(index + 1, index + 5);
                    try {
                        result.append((char) Integer.parseInt(hex, 16));
                    } catch (NumberFormatException ex) {
                        throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
                    }
                    index += 4;
                }
                default -> throw badRequest(AssistantApiErrors.textImproveBodyNotJsonString());
            }
        }
        return result.toString();
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
