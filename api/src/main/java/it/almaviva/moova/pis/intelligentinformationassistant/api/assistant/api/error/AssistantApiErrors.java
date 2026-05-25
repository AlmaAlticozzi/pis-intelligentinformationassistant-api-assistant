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

    public static Error alertVerifyBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-VER-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertVerifyAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-VER-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertVerifyNotFound() {
        return new Error()
                .code("IIA-ALT-VER-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertVerifyDeletedAlert() {
        return new Error()
                .code("IIA-ALT-VER-409-001")
                .title("Alert deleted")
                .detail("The alert has been deleted and cannot be verified.")
                .source("alertId");
    }

    public static Error alertVerifyUnexpectedError() {
        return new Error()
                .code("IIA-ALT-VER-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while verifying the alert definition.");
    }

    public static Error alertUpdateBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-UPD-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertUpdateAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-UPD-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertUpdateMissingBody() {
        return invalidParameter(
                "IIA-ALT-UPD-400-003",
                "body",
                "The request body is missing.");
    }

    public static Error alertUpdateBlankName() {
        return invalidParameter(
                "IIA-ALT-UPD-400-005",
                "name",
                "The name field is missing, empty or contains only whitespace characters.");
    }

    public static Error alertUpdateNameTooLong() {
        return invalidParameter(
                "IIA-ALT-UPD-400-006",
                "name",
                "The name field exceeds 120 characters.");
    }

    public static Error alertUpdateDescriptionTooLong() {
        return invalidParameter(
                "IIA-ALT-UPD-400-007",
                "description",
                "The description field exceeds 1000 characters.");
    }

    public static Error alertUpdatePromptInvalid() {
        return invalidParameter(
                "IIA-ALT-UPD-400-008",
                "prompt",
                "The prompt field is missing, empty, contains fewer than 10 characters or exceeds 8000 characters.");
    }

    public static Error alertUpdateNotFound() {
        return new Error()
                .code("IIA-ALT-UPD-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertUpdateDeletedAlert() {
        return new Error()
                .code("IIA-ALT-UPD-409-001")
                .title("Alert deleted")
                .detail("The alert has been deleted and cannot be updated.")
                .source("alertId");
    }

    public static Error alertUpdateDuplicateName() {
        return new Error()
                .code("IIA-ALT-UPD-409-002")
                .title("Alert already exists")
                .detail("An active alert with the same normalized name already exists.")
                .source("name");
    }

    public static Error alertUpdateVerifyingAlert() {
        return new Error()
                .code("IIA-ALT-UPD-409-003")
                .title("Alert verifying")
                .detail("The alert is currently verifying and cannot be updated.")
                .source("alertId");
    }

    public static Error alertUpdateUnexpectedError() {
        return new Error()
                .code("IIA-ALT-UPD-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while updating the alert definition.");
    }

    public static Error alertEnableBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-ENA-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertEnableAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-ENA-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertEnableNotFound() {
        return new Error()
                .code("IIA-ALT-ENA-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertEnableDeletedAlert() {
        return conflict("IIA-ALT-ENA-409-001", "Alert deleted", "The alert has been deleted and cannot be enabled.");
    }

    public static Error alertEnableInvalidStatus() {
        return conflict("IIA-ALT-ENA-409-001", "Alert not verified", "Only an alert in VERIFIED status can be enabled.");
    }

    public static Error alertEnableInvalidVerificationStatus() {
        return conflict("IIA-ALT-ENA-409-001", "Alert verification incomplete", "Only an alert with VERIFIED verification status can be enabled.");
    }

    public static Error alertEnableMissingOperationalMetadata() {
        return conflict("IIA-ALT-ENA-409-002", "Alert interpreter unavailable", "The alert does not have the verified interpreter metadata required for runtime execution.");
    }

    public static Error alertEnableAlreadyEnabled() {
        return conflict("IIA-ALT-ENA-409-003", "Alert already enabled", "The alert is already enabled.");
    }

    public static Error alertEnableUnexpectedError() {
        return new Error()
                .code("IIA-ALT-ENA-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while enabling the alert.");
    }

    public static Error alertDisableBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-DIS-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertDisableAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-DIS-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertDisableNotFound() {
        return new Error()
                .code("IIA-ALT-DIS-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertDisableDeletedAlert() {
        return conflict("IIA-ALT-DIS-409-001", "Alert deleted", "The alert has been deleted and cannot be disabled.");
    }

    public static Error alertDisableAlreadyDisabled() {
        return conflict("IIA-ALT-DIS-409-002", "Alert already disabled", "The alert runtime execution is already disabled.");
    }

    public static Error alertDisableVerifyingAlert() {
        return conflict("IIA-ALT-DIS-409-003", "Alert verifying", "The alert is currently verifying and cannot be disabled.");
    }

    public static Error alertDisableUnexpectedError() {
        return new Error()
                .code("IIA-ALT-DIS-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while disabling the alert.");
    }

    public static Error alertDeleteBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-DEL-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertDeleteAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-DEL-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertDeleteNotFound() {
        return new Error()
                .code("IIA-ALT-DEL-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertDeleteDeletedAlert() {
        return conflict("IIA-ALT-DEL-409-001", "Alert deleted", "The alert has already been deleted.");
    }

    public static Error alertDeleteTransitionInProgress() {
        return conflict("IIA-ALT-DEL-409-002", "Alert transition in progress", "The alert is currently verifying or deploying and cannot be deleted safely.");
    }

    public static Error alertDeleteUnexpectedError() {
        return new Error()
                .code("IIA-ALT-DEL-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while deleting the alert definition.");
    }

    public static Error textImproveMissingBody() {
        return invalidParameter(
                "IIA-UTL-TXI-400-001",
                "body",
                "The request body is missing.");
    }

    public static Error textImproveBodyNotJsonString() {
        return invalidParameter(
                "IIA-UTL-TXI-400-002",
                "body",
                "The request body is not a JSON string.");
    }

    public static Error textImproveBlankText() {
        return invalidParameter(
                "IIA-UTL-TXI-400-003",
                "body",
                "The text is empty or contains only whitespace characters.");
    }

    public static Error textImproveTextTooLong() {
        return invalidParameter(
                "IIA-UTL-TXI-400-004",
                "body",
                "The text exceeds 8000 characters.");
    }

    public static Error textImproveUnexpectedError() {
        return new Error()
                .code("IIA-UTL-TXI-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while improving the text.");
    }

    public static Error textImproveLlmProviderUnavailable() {
        return new Error()
                .code("IIA-UTL-TXI-503-001")
                .title("LLM provider unavailable")
                .detail("The LLM provider is temporarily unavailable.");
    }

    private static Error invalidParameter(String code, String source, String detail) {
        return new Error()
                .code(code)
                .title("Invalid request")
                .detail(detail)
                .source(source);
    }

    private static Error conflict(String code, String title, String detail) {
        return new Error()
                .code(code)
                .title(title)
                .detail(detail)
                .source("alertId");
    }
}
