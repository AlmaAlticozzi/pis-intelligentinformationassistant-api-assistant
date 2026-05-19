package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;

public final class AssistantApiErrors {

    private AssistantApiErrors() {
    }

    public static Error alertSearchInvalidStatus() {
        return invalidParameter(
                "IIA-ALT-SEA-400-001",
                "status",
                "The status query parameter is not a valid value of AlertStatus.");
    }

    public static Error alertSearchInvalidEnabled() {
        return invalidParameter(
                "IIA-ALT-SEA-400-002",
                "enabled",
                "The enabled query parameter is not a valid boolean value.");
    }

    public static Error alertSearchInvalidInterpreterType() {
        return invalidParameter(
                "IIA-ALT-SEA-400-003",
                "interpreterType",
                "The interpreterType query parameter is not a valid value of AlertInterpreterType.");
    }

    public static Error alertSearchTextTooLong() {
        return invalidParameter(
                "IIA-ALT-SEA-400-004",
                "text",
                "The text query parameter exceeds 200 characters.");
    }

    public static Error alertSearchUnexpectedError() {
        return new Error()
                .code("IIA-ALT-SEA-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while searching alert definitions.");
    }

    public static Error alertGetBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-GET-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertGetAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-GET-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertGetNotFound() {
        return new Error()
                .code("IIA-ALT-GET-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertGetUnexpectedError() {
        return new Error()
                .code("IIA-ALT-GET-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while reading the alert definition.");
    }

    public static Error alertCreateMissingBody() {
        return invalidParameter(
                "IIA-ALT-CRT-400-001",
                "body",
                "The request body is missing.");
    }

    public static Error alertCreateBlankName() {
        return invalidParameter(
                "IIA-ALT-CRT-400-003",
                "name",
                "The name field is missing, empty or contains only whitespace characters.");
    }

    public static Error alertCreateNameTooLong() {
        return invalidParameter(
                "IIA-ALT-CRT-400-004",
                "name",
                "The name field exceeds 120 characters.");
    }

    public static Error alertCreateDescriptionTooLong() {
        return invalidParameter(
                "IIA-ALT-CRT-400-005",
                "description",
                "The description field exceeds 1000 characters.");
    }

    public static Error alertCreateBlankPrompt() {
        return invalidParameter(
                "IIA-ALT-CRT-400-006",
                "prompt",
                "The prompt field is missing, empty or contains only whitespace characters.");
    }

    public static Error alertCreatePromptInvalidLength() {
        return invalidParameter(
                "IIA-ALT-CRT-400-007",
                "prompt",
                "The prompt field contains fewer than 10 characters or exceeds 8000 characters.");
    }

    public static Error alertCreateInvalidVerifyImmediately() {
        return invalidParameter(
                "IIA-ALT-CRT-400-008",
                "verifyImmediately",
                "The verifyImmediately field is not a boolean value.");
    }

    public static Error alertCreateInvalidEnableAfterVerification() {
        return invalidParameter(
                "IIA-ALT-CRT-400-009",
                "enableAfterVerification",
                "The enableAfterVerification field is not a boolean value.");
    }

    public static Error alertCreateDuplicateName() {
        return new Error()
                .code("IIA-ALT-CRT-409-001")
                .title("Alert already exists")
                .detail("An active alert with the same normalized name already exists.")
                .source("name");
    }

    public static Error alertCreateVerificationUnsupported() {
        return new Error()
                .code("IIA-ALT-CRT-422-001")
                .title("Verification unsupported")
                .detail("Immediate verification is not available yet for alert creation.")
                .source("verifyImmediately");
    }

    public static Error alertCreateUnexpectedError() {
        return new Error()
                .code("IIA-ALT-CRT-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while creating the alert definition.");
    }

    private static Error invalidParameter(String code, String source, String detail) {
        return new Error()
                .code(code)
                .title("Invalid request")
                .detail(detail)
                .source(source);
    }
}
