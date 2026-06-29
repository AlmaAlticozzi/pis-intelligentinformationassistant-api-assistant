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

    public static Error alertAgentGenerationPreviewInvalidAlertId() {
        return invalidParameter(
                "IIA-ALT-AGP-400-001",
                "alertId",
                "The alertId path parameter is empty, contains only whitespace characters or exceeds 50 characters.");
    }

    public static Error alertAgentGenerationPreviewNotFound() {
        return new Error()
                .code("IIA-ALT-AGP-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertAgentGenerationPreviewAlertNotVerified() {
        return conflict("IIA-ALT-AGP-409-001", "Alert not verified", "Only a technically verified alert can produce an Agent generation preview.");
    }

    public static Error alertAgentGenerationPreviewMissingTechnicalArtifacts() {
        return new Error()
                .code("IIA-ALT-AGP-422-001")
                .title("Agent preview unavailable")
                .detail("The verified alert does not contain the technical artifacts required to produce an Agent Blueprint preview.")
                .source("alertId");
    }

    public static Error alertAgentGenerationPreviewInvalidBlueprint() {
        return new Error()
                .code("IIA-ALT-AGP-422-001")
                .title("Agent preview unavailable")
                .detail("The verified alert cannot be transformed into an Agent Blueprint supported by the current runtime capabilities.")
                .source("alertId");
    }

    public static Error alertAgentGenerationPreviewUnexpectedError() {
        return new Error()
                .code("IIA-ALT-AGP-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while generating the Agent preview.");
    }

    public static Error alertTechnicalSpecificationGetBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-TSP-GET-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertTechnicalSpecificationGetAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-TSP-GET-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertTechnicalSpecificationGetNotFound() {
        return new Error()
                .code("IIA-ALT-TSP-GET-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertTechnicalSpecificationGetDeletedAlert() {
        return conflict("IIA-ALT-TSP-GET-409-001", "Alert deleted", "The alert has been deleted and its technical specification cannot be exposed.");
    }

    public static Error alertTechnicalSpecificationGetNotVerified() {
        return conflict("IIA-ALT-TSP-GET-409-002", "Alert not verified", "Only a verified alert can expose its technical specification.");
    }

    public static Error alertTechnicalSpecificationGetInvalidSpecification() {
        return new Error()
                .code("IIA-ALT-TSP-GET-422-001")
                .title("Technical specification unavailable")
                .detail("The verified alert does not contain a valid persisted technical specification object.")
                .source("technicalSpecification");
    }

    public static Error alertTechnicalSpecificationGetUnexpectedError() {
        return new Error()
                .code("IIA-ALT-TSP-GET-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while reading the alert technical specification.");
    }

    public static Error alertTechnicalSpecificationPutBlankAlertId() {
        return invalidParameter(
                "IIA-ALT-TSP-PUT-400-001",
                "alertId",
                "The alertId path parameter is empty or contains only whitespace characters.");
    }

    public static Error alertTechnicalSpecificationPutAlertIdTooLong() {
        return invalidParameter(
                "IIA-ALT-TSP-PUT-400-002",
                "alertId",
                "The alertId path parameter exceeds 50 characters.");
    }

    public static Error alertTechnicalSpecificationPutMissingBody() {
        return invalidParameter(
                "IIA-ALT-TSP-PUT-400-003",
                "body",
                "The request body is missing.");
    }

    public static Error alertTechnicalSpecificationPutInvalidSpecification() {
        return invalidParameter(
                "IIA-ALT-TSP-PUT-400-005",
                "technicalSpecification",
                "The technicalSpecification field is missing, empty or not a JSON object.");
    }

    public static Error alertTechnicalSpecificationPutNotFound() {
        return new Error()
                .code("IIA-ALT-TSP-PUT-404-001")
                .title("Alert not found")
                .detail("No alert with the given alertId was found.")
                .source("alertId");
    }

    public static Error alertTechnicalSpecificationPutDeletedAlert() {
        return conflict("IIA-ALT-TSP-PUT-409-001", "Alert deleted", "The alert has been deleted and cannot be modified.");
    }

    public static Error alertTechnicalSpecificationPutNotVerified() {
        return conflict("IIA-ALT-TSP-PUT-409-002", "Alert not verified", "Only a verified alert can have its technical specification replaced.");
    }

    public static Error alertTechnicalSpecificationPutConcurrentUpdate() {
        return conflict("IIA-ALT-TSP-PUT-409-003", "Alert update in progress", "The alert is currently verifying or another technical specification update is already in progress.");
    }

    public static Error alertTechnicalSpecificationPutValidationFailed() {
        return new Error()
                .code("IIA-ALT-TSP-PUT-422-001")
                .title("Technical specification validation failed")
                .detail("The submitted technical specification is syntactically valid JSON but fails backend validation.")
                .source("technicalSpecification");
    }

    public static Error alertTechnicalSpecificationPutUnsupportedSpecification() {
        return new Error()
                .code("IIA-ALT-TSP-PUT-422-002")
                .title("Unsupported technical specification")
                .detail("The submitted technical specification uses unsupported sources, fields, operators, enum values, interpreter types or evaluation modes.")
                .source("technicalSpecification");
    }

    public static Error alertTechnicalSpecificationPutUnexpectedError() {
        return new Error()
                .code("IIA-ALT-TSP-PUT-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while replacing the alert technical specification.");
    }

    public static Error agentProfileListUnexpectedError() {
        return new Error()
                .code("IIA-AGP-SEA-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while listing Agent profiles.");
    }

    public static Error agentProfileGetBlankProfileId() {
        return invalidParameter(
                "IIA-AGP-GET-400-001",
                "agentProfileId",
                "The agentProfileId path parameter is empty or contains only whitespace characters.");
    }

    public static Error agentProfileGetProfileIdTooLong() {
        return invalidParameter(
                "IIA-AGP-GET-400-002",
                "agentProfileId",
                "The agentProfileId path parameter exceeds 50 characters.");
    }

    public static Error agentProfileGetNotFound() {
        return new Error()
                .code("IIA-AGP-GET-404-001")
                .title("Agent profile not found")
                .detail("No Agent profile with the given agentProfileId was found.")
                .source("agentProfileId");
    }

    public static Error agentProfileGetUnexpectedError() {
        return new Error()
                .code("IIA-AGP-GET-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while reading the Agent profile.");
    }

    public static Error agentDefinitionCreateInvalidRequest(String source, String detail) {
        return invalidParameter(
                "IIA-AGD-CRE-400-003",
                source,
                detail);
    }

    public static Error agentDefinitionCreateNotFound(String source, String detail) {
        return new Error()
                .code("IIA-AGD-CRE-404-001")
                .title("Referenced resource not found")
                .detail(detail)
                .source(source);
    }

    public static Error agentDefinitionCreateConflict(String detail) {
        return new Error()
                .code("IIA-AGD-CRE-409-001")
                .title("Agent Definition creation rejected")
                .detail(detail);
    }

    public static Error agentDefinitionCreateUnprocessable(String detail) {
        return new Error()
                .code("IIA-AGD-CRE-422-001")
                .title("Agent Definition cannot be created")
                .detail(detail);
    }

    public static Error agentDefinitionCreateUnexpectedError() {
        return new Error()
                .code("IIA-AGD-CRE-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while creating the Agent definition.");
    }

    public static Error agentDefinitionCompileInvalidRequest(String source, String detail) {
        return invalidParameter(
                "IIA-AGD-CMP-400-001",
                source,
                detail);
    }

    public static Error agentDefinitionCompileNotFound(String source, String detail) {
        return new Error()
                .code("IIA-AGD-CMP-404-001")
                .title("Agent Definition not found")
                .detail(detail)
                .source(source);
    }

    public static Error agentDefinitionCompileConflict(String detail) {
        return new Error()
                .code("IIA-AGD-CMP-409-001")
                .title("Agent Definition compilation rejected")
                .detail(detail)
                .source("agentDefinitionId");
    }

    public static Error agentDefinitionCompileUnprocessable(String detail) {
        return new Error()
                .code("IIA-AGD-CMP-422-001")
                .title("Agent Definition cannot be compiled")
                .detail(detail);
    }

    public static Error agentDefinitionCompileUnexpectedError() {
        return new Error()
                .code("IIA-AGD-CMP-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while starting Agent compilation.");
    }

    public static Error agentDefinitionActivateInvalidRequest(String source, String detail) {
        return invalidParameter(
                "IIA-AGD-ACT-400-001",
                source,
                detail);
    }

    public static Error agentDefinitionActivateNotFound(String source, String detail) {
        return new Error()
                .code("IIA-AGD-ACT-404-001")
                .title("Agent Definition not found")
                .detail(detail)
                .source(source);
    }

    public static Error agentDefinitionActivateConflict(String detail) {
        return new Error()
                .code("IIA-AGD-ACT-409-001")
                .title("Agent Definition activation rejected")
                .detail(detail)
                .source("agentDefinitionId");
    }

    public static Error agentDefinitionActivateUnprocessable(String detail) {
        return new Error()
                .code("IIA-AGD-ACT-422-001")
                .title("Agent Definition cannot be activated")
                .detail(detail);
    }

    public static Error agentDefinitionActivateServiceUnavailable() {
        return agentDefinitionActivateServiceUnavailable("The Agent Definition is valid and compiled, but it could not be delivered to the Agent Orchestrator. Activation did not occur and the Agent Definition state is unchanged.");
    }

    public static Error agentDefinitionActivateServiceUnavailable(String detail) {
        return new Error()
                .code("IIA-AGD-ACT-503-001")
                .title("Agent Orchestrator unavailable")
                .detail(detail);
    }

    public static Error agentDefinitionActivateUnexpectedError() {
        return agentDefinitionActivateUnexpectedError("An unexpected error occurred while activating the Agent Definition.");
    }

    public static Error agentDefinitionActivateUnexpectedError(String detail) {
        return new Error()
                .code("IIA-AGD-ACT-500-001")
                .title("Unexpected error")
                .detail(detail);
    }

    public static Error agentDefinitionDisableInvalidRequest(String source, String detail) {
        return invalidParameter(
                "IIA-AGD-DIS-400-001",
                source,
                detail);
    }

    public static Error agentDefinitionDisableNotFound(String source, String detail) {
        return new Error()
                .code("IIA-AGD-DIS-404-001")
                .title("Agent Definition not found")
                .detail(detail)
                .source(source);
    }

    public static Error agentDefinitionDisableConflict(String detail) {
        return new Error()
                .code("IIA-AGD-DIS-409-001")
                .title("Agent Definition disable rejected")
                .detail(detail)
                .source("agentDefinitionId");
    }

    public static Error agentDefinitionDisableUnprocessable(String detail) {
        return new Error()
                .code("IIA-AGD-DIS-422-001")
                .title("Agent Definition disable rejected by runtime")
                .detail(detail);
    }

    public static Error agentDefinitionDisableServiceUnavailable() {
        return new Error()
                .code("IIA-AGD-DIS-503-001")
                .title("Agent Orchestrator unavailable")
                .detail("The Agent Definition is ACTIVE, but the disable command could not be delivered to the Agent Orchestrator. Disable did not occur and the Agent Definition state is unchanged.");
    }

    public static Error agentDefinitionDisableUnexpectedError() {
        return new Error()
                .code("IIA-AGD-DIS-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while disabling the Agent Definition.");
    }

    public static Error agentDefinitionGetInvalidRequest(String source, String detail) {
        return invalidParameter(
                "IIA-AGD-GET-400-001",
                source,
                detail);
    }

    public static Error agentDefinitionGetNotFound(String source, String detail) {
        return new Error()
                .code("IIA-AGD-GET-404-001")
                .title("Agent Definition not found")
                .detail(detail)
                .source(source);
    }

    public static Error agentDefinitionGetUnexpectedError() {
        return new Error()
                .code("IIA-AGD-GET-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while reading the Agent Definition.");
    }

    public static Error agentDefinitionCompilationInvalidRequest(String source, String detail) {
        return invalidParameter(
                "IIA-AGD-CMS-400-001",
                source,
                detail);
    }

    public static Error agentDefinitionCompilationNotFound(String source, String detail) {
        return new Error()
                .code("IIA-AGD-CMS-404-001")
                .title("Agent compilation not found")
                .detail(detail)
                .source(source);
    }

    public static Error agentDefinitionCompilationUnexpectedError() {
        return new Error()
                .code("IIA-AGD-CMS-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while reading the Agent Definition compilation.");
    }

    public static Error agentDefinitionSearchInvalidStatus() {
        return invalidParameter(
                "IIA-AGD-SEA-400-001",
                "status",
                "The status query parameter is not a valid Agent Definition status.");
    }

    public static Error agentDefinitionSearchInvalidGenerationMode() {
        return invalidParameter(
                "IIA-AGD-SEA-400-002",
                "generationMode",
                "The generationMode query parameter is not a valid Agent generation mode.");
    }

    public static Error agentDefinitionSearchTextTooLong() {
        return invalidParameter(
                "IIA-AGD-SEA-400-003",
                "text",
                "The text query parameter must not exceed 200 characters.");
    }

    public static Error agentDefinitionSearchInvalidRequest(String source, String detail) {
        return invalidParameter(
                "IIA-AGD-SEA-400-004",
                source,
                detail);
    }

    public static Error agentDefinitionSearchUnexpectedError() {
        return new Error()
                .code("IIA-AGD-SEA-500-001")
                .title("Unexpected error")
                .detail("An unexpected error occurred while searching Agent Definitions.");
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
