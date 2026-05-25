package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error.AssistantApiErrors;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.interfaces.IAssistantV1Api;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.validation.AssistantApiInputValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.*;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AlertAgentGenerationPreviewRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AlertDeleteRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AlertRuntimeStateChangeRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AlertService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AlertUpdateRejectedException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.utility.TextImproveUseCase;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/v1")
public class AssistantV1Api implements IAssistantV1Api {

    @Inject
    AlertService alertService;

    @Inject
    TextImproveUseCase textImproveUseCase;

    @POST
    @Path("/agent-definitions/{agentDefinitionId}/activate")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentDefinitionDetail activateAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) String agentDefinitionId, @Valid AgentActivationRequest agentActivationRequest) {
        System.out.println("activateAgentDefinition: " + "agentDefinitionId=" + agentDefinitionId + ", " + "agentActivationRequest=" + agentActivationRequest);
        return new AgentDefinitionDetail();
    }

    @POST
    @Path("/suggestions/{suggestionId}/approve")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public SuggestionDetail approveSuggestion(@PathParam("suggestionId") @Size(max=50) String suggestionId, @Valid SuggestionApprovalRequest suggestionApprovalRequest) {
        System.out.println("approveSuggestion: " + "suggestionId=" + suggestionId + ", " + "suggestionApprovalRequest=" + suggestionApprovalRequest);
        return new SuggestionDetail();
    }

    @POST
    @Path("/assistant/questions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AssistantQuestionResponse askAssistantQuestion(@Valid @NotNull AssistantQuestionRequest assistantQuestionRequest) {
        System.out.println("askAssistantQuestion: " + "assistantQuestionRequest=" + assistantQuestionRequest);
        return new AssistantQuestionResponse();
    }

    @POST
    @Path("/agent-definitions/{agentDefinitionId}/compile")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentCompilationStatusResponse compileAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) String agentDefinitionId, @Valid AgentCompilationRequest agentCompilationRequest) {
        System.out.println("compileAgentDefinition: " + "agentDefinitionId=" + agentDefinitionId + ", " + "agentCompilationRequest=" + agentCompilationRequest);
        return new AgentCompilationStatusResponse();
    }

    @POST
    @Path("/agent-definitions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentDefinitionDetail createAgentDefinition(@Valid @NotNull AgentDefinitionCreateRequest agentDefinitionCreateRequest) {
        System.out.println("createAgentDefinition: " + "agentDefinitionCreateRequest=" + agentDefinitionCreateRequest);
        return new AgentDefinitionDetail();
    }

    @POST
    @Path("/alerts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AlertDetail createAlert(AlertCreateRequest alertCreateRequest) {
        try {
            AlertCreateRequest validatedRequest = AssistantApiInputValidator.validateAlertCreate(alertCreateRequest);
            System.out.println("[IIA][ALERT_CREATE] Create requested");

            if (alertService.existsActiveAlertWithNormalizedName(validatedRequest.getName())) {
                throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                        .entity(AssistantApiErrors.alertCreateDuplicateName())
                        .build());
            }

            return alertService.createAlert(validatedRequest);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_CREATE] Unexpected error error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertCreateUnexpectedError())
                    .build());
        }
    }

    @POST
    @Path("/assistant/sessions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AssistantSession createAssistantSession(@Valid AssistantSessionCreateRequest assistantSessionCreateRequest) {
        System.out.println("createAssistantSession: " + "assistantSessionCreateRequest=" + assistantSessionCreateRequest);
        return new AssistantSession();
    }

    @DELETE
    @Path("/alerts/{alertId}")
    @Produces({ "application/json" })
    @Override
    public void deleteAlert(@PathParam("alertId") @Size(max=50) String alertId) {
        try {
            String validatedAlertId = AssistantApiInputValidator.validateAlertIdForDelete(alertId);
            System.out.println("[IIA][ALERT_DELETE] Delete requested for alertId=" + validatedAlertId);
            if (!alertService.deleteAlert(validatedAlertId)) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(AssistantApiErrors.alertDeleteNotFound())
                        .build());
            }
        } catch (AlertDeleteRejectedException ex) {
            throw alertDeleteRejected(ex);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_DELETE] Unexpected error alertId=" + alertId + " error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertDeleteUnexpectedError())
                    .build());
        }
    }

    @POST
    @Path("/agent-definitions/{agentDefinitionId}/disable")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentDefinitionDetail disableAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) String agentDefinitionId, @Valid AgentDisableRequest agentDisableRequest) {
        System.out.println("disableAgentDefinition: " + "agentDefinitionId=" + agentDefinitionId + ", " + "agentDisableRequest=" + agentDisableRequest);
        return new AgentDefinitionDetail();
    }

    @PATCH
    @Path("/alerts/{alertId}/disable")
    @Produces({ "application/json" })
    @Override
    public AlertDetail disableAlert(@PathParam("alertId") @Size(max=50) String alertId) {
        try {
            String validatedAlertId = AssistantApiInputValidator.validateAlertIdForDisable(alertId);
            System.out.println("[IIA][ALERT_DISABLE] requested alertId=" + validatedAlertId);
            return alertService.disableAlert(validatedAlertId)
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(AssistantApiErrors.alertDisableNotFound())
                            .build()));
        } catch (AlertRuntimeStateChangeRejectedException ex) {
            throw alertDisableRejected(ex);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_DISABLE] Unexpected error alertId=" + alertId + " error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertDisableUnexpectedError())
                    .build());
        }
    }

    @PATCH
    @Path("/alerts/{alertId}/enable")
    @Produces({ "application/json" })
    @Override
    public AlertDetail enableAlert(@PathParam("alertId") @Size(max=50) String alertId) {
        try {
            String validatedAlertId = AssistantApiInputValidator.validateAlertIdForEnable(alertId);
            System.out.println("[IIA][ALERT_ENABLE] requested alertId=" + validatedAlertId);
            return alertService.enableAlert(validatedAlertId)
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(AssistantApiErrors.alertEnableNotFound())
                            .build()));
        } catch (AlertRuntimeStateChangeRejectedException ex) {
            throw alertEnableRejected(ex);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_ENABLE] Unexpected error alertId=" + alertId + " error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertEnableUnexpectedError())
                    .build());
        }
    }

    @PATCH
    @Path("/agent-definitions/{agentDefinitionId}/compilation")
    @Produces({ "application/json" })
    @Override
    public AgentCompilationStatusResponse getAgentCompilationStatus(@PathParam("agentDefinitionId") @Size(max=50) String agentDefinitionId) {
        System.out.println("getAgentCompilationStatus: " + "agentDefinitionId=" + agentDefinitionId);
        return new AgentCompilationStatusResponse();
    }

    @GET
    @Path("/agent-definitions/{agentDefinitionId}")
    @Produces({ "application/json" })
    @Override
    public AgentDefinitionDetail getAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) String agentDefinitionId) {
        System.out.println("getAgentDefinition: " + "agentDefinitionId=" + agentDefinitionId);
        return new AgentDefinitionDetail();
    }

    @GET
    @Path("/agent-outputs/{agentOutputId}")
    @Produces({ "application/json" })
    @Override
    public AgentOutputDetail getAgentOutput(@PathParam("agentOutputId") @Size(max=50) String agentOutputId) {
        System.out.println("getAgentOutput: " + "agentOutputId=" + agentOutputId);
        return new AgentOutputDetail();
    }

    @GET
    @Path("/agent-profiles/{agentProfileId}")
    @Produces({ "application/json" })
    @Override
    public AgentProfile getAgentProfile(@PathParam("agentProfileId") @Size(max=50) String agentProfileId) {
        System.out.println("getAgentProfile: " + "agentProfileId=" + agentProfileId);
        return new AgentProfile();
    }

    @GET
    @Path("/agent-runs/{agentRunId}")
    @Produces({ "application/json" })
    @Override
    public AgentRunDetail getAgentRun(@PathParam("agentRunId") @Size(max=50) String agentRunId) {
        System.out.println("getAgentRun: " + "agentRunId=" + agentRunId);
        return new AgentRunDetail();
    }

    @GET
    @Path("/agent-runs/{agentRunId}/events")
    @Produces({ "application/json" })
    @Override
    public AgentRuntimeEventListResponse getAgentRunEvents(@PathParam("agentRunId") @Size(max=50) String agentRunId, @QueryParam("severity") AgentRuntimeEventSeverity severity, @QueryParam("eventType") AgentRuntimeEventType eventType) {
        System.out.println("getAgentRunEvents: " + "agentRunId=" + agentRunId + ", " + "severity=" + severity + ", " + "eventType=" + eventType);
        return new AgentRuntimeEventListResponse();
    }

    @GET
    @Path("/agent-runs/{agentRunId}/logs")
    @Produces({ "application/json" })
    @Override
    public AgentRunLogListResponse getAgentRunLogs(@PathParam("agentRunId") @Size(max=50) String agentRunId, @QueryParam("limit") @Min(1) @Max(500) @DefaultValue("100") Integer limit) {
        System.out.println("getAgentRunLogs: " + "agentRunId=" + agentRunId + ", " + "limit=" + limit);
        return new AgentRunLogListResponse();
    }

    @GET
    @Path("/agent-runs/{agentRunId}/metrics")
    @Produces({ "application/json" })
    @Override
    public AgentMetricSnapshotListResponse getAgentRunMetrics(@PathParam("agentRunId") @Size(max=50) String agentRunId, @QueryParam("from") OffsetDateTime from, @QueryParam("to") OffsetDateTime to, @QueryParam("granularity") @DefaultValue("RAW") String granularity) {
        System.out.println("getAgentRunMetrics: " + "agentRunId=" + agentRunId + ", " + "from=" + from + ", " + "to=" + to + ", " + "granularity=" + granularity);
        return new AgentMetricSnapshotListResponse();
    }

    @GET
    @Path("/agent-runs/{agentRunId}/outputs")
    @Produces({ "application/json" })
    @Override
    public AgentOutputListResponse getAgentRunOutputs(@PathParam("agentRunId") @Size(max=50) String agentRunId, @QueryParam("status") AgentOutputStatus status) {
        System.out.println("getAgentRunOutputs: " + "agentRunId=" + agentRunId + ", " + "status=" + status);
        return new AgentOutputListResponse();
    }

    @GET
    @Path("/alerts/{alertId}")
    @Produces({ "application/json" })
    @Override
    public AlertDetail getAlert(@PathParam("alertId") @Size(max=50) String alertId) {
        try {
            String validatedAlertId = AssistantApiInputValidator.validateAlertIdForGet(alertId);
            System.out.println("[IIA][ALERT_GET] Get requested alertId=" + validatedAlertId);
            return alertService.getAlert(validatedAlertId)
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(AssistantApiErrors.alertGetNotFound())
                            .build()));
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_GET] Unexpected error alertId=" + alertId + " error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertGetUnexpectedError())
                    .build());
        }
    }

    @GET
    @Path("/assistant/sessions/{sessionId}")
    @Produces({ "application/json" })
    @Override
    public AssistantSession getAssistantSession(@PathParam("sessionId") @Size(max=50) String sessionId) {
        System.out.println("getAssistantSession: " + "sessionId=" + sessionId);
        return new AssistantSession();
    }

    @GET
    @Path("/assistant/questions/{questionId}/tool-executions")
    @Produces({ "application/json" })
    @Override
    public ToolExecutionListResponse getQuestionToolExecutions(@PathParam("questionId") @Size(max=50) String questionId) {
        System.out.println("getQuestionToolExecutions: " + "questionId=" + questionId);
        return new ToolExecutionListResponse();
    }

    @GET
    @Path("/suggestions/{suggestionId}")
    @Produces({ "application/json" })
    @Override
    public SuggestionDetail getSuggestionDetail(@PathParam("suggestionId") @Size(max=50) String suggestionId) {
        System.out.println("getSuggestionDetail: " + "suggestionId=" + suggestionId);
        return new SuggestionDetail();
    }

    @POST
    @Path("/agent-runs/{agentRunId}/kill")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentRunDetail killAgentRun(@PathParam("agentRunId") @Size(max=50) String agentRunId, @Valid AgentRunKillRequest agentRunKillRequest) {
        System.out.println("killAgentRun: " + "agentRunId=" + agentRunId + ", " + "agentRunKillRequest=" + agentRunKillRequest);
        return new AgentRunDetail();
    }

    @GET
    @Path("/agent-profiles")
    @Produces({ "application/json" })
    @Override
    public AgentProfileListResponse listAgentProfiles() {
        System.out.println("listAgentProfiles");
        return new AgentProfileListResponse();
    }

    @PATCH
    @Path("/agent-definitions/{agentDefinitionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentDefinitionDetail patchAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) String agentDefinitionId, @Valid @NotNull AgentDefinitionPatchRequest agentDefinitionPatchRequest) {
        System.out.println("patchAgentDefinition: " + "agentDefinitionId=" + agentDefinitionId + ", " + "agentDefinitionPatchRequest=" + agentDefinitionPatchRequest);
        return new AgentDefinitionDetail();
    }

    @PUT
    @Path("/alerts/{alertId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AlertDetail updateAlert(@PathParam("alertId") @Size(max = 50) String alertId, @Valid @NotNull AlertUpdateRequest alertUpdateRequest) {
        try {
            String validatedAlertId = AssistantApiInputValidator.validateAlertIdForUpdate(alertId);
            AlertUpdateRequest validatedRequest = AssistantApiInputValidator.validateAlertUpdate(alertUpdateRequest);
            System.out.println("[IIA][ALERT_UPDATE] Update requested alertId=" + validatedAlertId);

            AlertDetail updatedAlert = alertService.updateAlert(validatedAlertId, validatedRequest)
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(AssistantApiErrors.alertUpdateNotFound())
                            .build()));

            System.out.println("[IIA][ALERT_UPDATE] Returning alert detail alertId=" + validatedAlertId + " status=" + updatedAlert.getStatus());
            return updatedAlert;
        } catch (AlertUpdateRejectedException ex) {
            throw alertUpdateRejected(ex);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_UPDATE] Unexpected error alertId=" + alertId + " error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertUpdateUnexpectedError())
                    .build());
        }
    }

    private WebApplicationException alertUpdateRejected(AlertUpdateRejectedException ex) {
        return switch (ex.reason()) {
            case DELETED -> new WebApplicationException(Response.status(Response.Status.CONFLICT)
                    .entity(AssistantApiErrors.alertUpdateDeletedAlert())
                    .build());
            case VERIFYING -> new WebApplicationException(Response.status(Response.Status.CONFLICT)
                    .entity(AssistantApiErrors.alertUpdateVerifyingAlert())
                    .build());
            case DUPLICATE_NAME -> new WebApplicationException(Response.status(Response.Status.CONFLICT)
                    .entity(AssistantApiErrors.alertUpdateDuplicateName())
                    .build());
        };
    }

    private WebApplicationException alertEnableRejected(AlertRuntimeStateChangeRejectedException ex) {
        return switch (ex.reason()) {
            case DELETED -> conflict(AssistantApiErrors.alertEnableDeletedAlert());
            case STATUS_NOT_VERIFIED -> conflict(AssistantApiErrors.alertEnableInvalidStatus());
            case VERIFICATION_NOT_VERIFIED -> conflict(AssistantApiErrors.alertEnableInvalidVerificationStatus());
            case ALREADY_ENABLED -> conflict(AssistantApiErrors.alertEnableAlreadyEnabled());
            case MISSING_OPERATIONAL_METADATA -> conflict(AssistantApiErrors.alertEnableMissingOperationalMetadata());
            case VERIFYING, ALREADY_DISABLED -> throw new IllegalStateException("Unexpected enable rejection reason: " + ex.reason());
        };
    }

    private WebApplicationException alertDisableRejected(AlertRuntimeStateChangeRejectedException ex) {
        return switch (ex.reason()) {
            case DELETED -> conflict(AssistantApiErrors.alertDisableDeletedAlert());
            case VERIFYING -> conflict(AssistantApiErrors.alertDisableVerifyingAlert());
            case ALREADY_DISABLED -> conflict(AssistantApiErrors.alertDisableAlreadyDisabled());
            case STATUS_NOT_VERIFIED, VERIFICATION_NOT_VERIFIED, ALREADY_ENABLED, MISSING_OPERATIONAL_METADATA ->
                    throw new IllegalStateException("Unexpected disable rejection reason: " + ex.reason());
        };
    }

    private WebApplicationException alertDeleteRejected(AlertDeleteRejectedException ex) {
        return switch (ex.reason()) {
            case DELETED -> conflict(AssistantApiErrors.alertDeleteDeletedAlert());
            case VERIFYING, DEPLOYING -> conflict(AssistantApiErrors.alertDeleteTransitionInProgress());
        };
    }

    private WebApplicationException conflict(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error error) {
        return new WebApplicationException(Response.status(Response.Status.CONFLICT)
                .entity(error)
                .build());
    }

    @PATCH
    @Path("/suggestions/{suggestionId}/texts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public SuggestionDetail patchSuggestionTexts(@PathParam("suggestionId") @Size(max=50) String suggestionId, @Valid @NotNull SuggestionTextPatchRequest suggestionTextPatchRequest) {
        System.out.println("patchSuggestionTexts: " + "suggestionId=" + suggestionId + ", " + "suggestionTextPatchRequest=" + suggestionTextPatchRequest);
        return new SuggestionDetail();
    }

    @POST
    @Path("/alerts/{alertId}/agent-generation-preview")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentGenerationPreviewResponse previewAgentGenerationForAlert(@PathParam("alertId") @Size(max=50) String alertId, @Valid AgentGenerationPreviewRequest agentGenerationPreviewRequest) {
        try {
            String validatedAlertId = AssistantApiInputValidator.validateAlertIdForAgentGenerationPreview(alertId);
            System.out.println("[IIA][AGENT_PREVIEW] Preview requested for alertId=" + validatedAlertId);
            return alertService.previewAgentGenerationForAlert(validatedAlertId, agentGenerationPreviewRequest)
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(AssistantApiErrors.alertAgentGenerationPreviewNotFound())
                            .build()));
        } catch (AlertAgentGenerationPreviewRejectedException ex) {
            throw alertAgentGenerationPreviewRejected(ex);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][AGENT_PREVIEW] Unexpected error alertId=" + alertId + " error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertAgentGenerationPreviewUnexpectedError())
                    .build());
        }
    }

    private WebApplicationException alertAgentGenerationPreviewRejected(AlertAgentGenerationPreviewRejectedException ex) {
        return switch (ex.reason()) {
            case NOT_VERIFIED -> conflict(AssistantApiErrors.alertAgentGenerationPreviewAlertNotVerified());
            case MISSING_TECHNICAL_ARTIFACTS -> new WebApplicationException(
                    Response.status(422)
                            .entity(AssistantApiErrors.alertAgentGenerationPreviewMissingTechnicalArtifacts())
                            .build());
            case INVALID_BLUEPRINT -> new WebApplicationException(
                    Response.status(422)
                            .entity(AssistantApiErrors.alertAgentGenerationPreviewInvalidBlueprint())
                            .build());
        };
    }

    @POST
    @Path("/suggestions/{suggestionId}/reject")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public SuggestionDetail rejectSuggestion(@PathParam("suggestionId") @Size(max=50) String suggestionId, @Valid SuggestionRejectionRequest suggestionRejectionRequest) {
        System.out.println("rejectSuggestion: " + "suggestionId=" + suggestionId + ", " + "suggestionRejectionRequest=" + suggestionRejectionRequest);
        return new SuggestionDetail();
    }

    @POST
    @Path("/agent-runs/{agentRunId}/restart")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AgentRunDetail restartAgentRun(@PathParam("agentRunId") @Size(max=50) String agentRunId, @Valid AgentRunRestartRequest agentRunRestartRequest) {
        System.out.println("restartAgentRun: " + "agentRunId=" + agentRunId + ", " + "agentRunRestartRequest=" + agentRunRestartRequest);
        return new AgentRunDetail();
    }

    @GET
    @Path("/agent-definitions")
    @Produces({ "application/json" })
    @Override
    public AgentDefinitionListResponse searchAgentDefinitions(@QueryParam("status") AgentDefinitionStatus status, @QueryParam("alertId") @Size(max=50) String alertId, @QueryParam("generationMode") AgentGenerationMode generationMode, @QueryParam("profileId") @Size(max=50) String profileId, @QueryParam("text") @Size(max=200) String text) {
        System.out.println("searchAgentDefinitions: " + "status=" + status + ", " + "alertId=" + alertId + ", " + "generationMode=" + generationMode + ", " + "profileId=" + profileId + ", " + "text=" + text);
        return new AgentDefinitionListResponse();
    }

    @GET
    @Path("/agent-runs")
    @Produces({ "application/json" })
    @Override
    public AgentRunListResponse searchAgentRuns(@QueryParam("agentDefinitionId") @Size(max=50) String agentDefinitionId, @QueryParam("alertId") @Size(max=50) String alertId, @QueryParam("status") AgentRunStatus status, @QueryParam("healthStatus") AgentHealthStatus healthStatus, @QueryParam("activeOnly") Boolean activeOnly, @QueryParam("from") OffsetDateTime from, @QueryParam("to") OffsetDateTime to) {
        System.out.println("searchAgentRuns: " + "agentDefinitionId=" + agentDefinitionId + ", " + "alertId=" + alertId + ", " + "status=" + status + ", " + "healthStatus=" + healthStatus + ", " + "activeOnly=" + activeOnly + ", " + "from=" + from + ", " + "to=" + to);
        return new AgentRunListResponse();
    }

    @GET
    @Path("/alerts")
    @Produces({ "application/json" })
    @Override
    public AlertSummaryListResponse searchAlerts(@QueryParam("status") String status, @QueryParam("enabled") String enabled, @QueryParam("interpreterType") String interpreterType, @QueryParam("text") String text) {
        try {
            AlertSearchCriteria criteria = AssistantApiInputValidator.validateAlertSearch(status, enabled, interpreterType, text);
            System.out.println("[IIA][ALERT_SEARCH] Search requested criteria=" + criteria);
            return alertService.searchAlerts(criteria);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_SEARCH] Unexpected error error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertSearchUnexpectedError())
                    .build());
        }
    }

    @GET
    @Path("/suggestions")
    @Produces({ "application/json" })
    @Override
    public List<SuggestionHeader> searchSuggestionHeaders(@QueryParam("lookbackHours") @NotNull @Min(1) @Max(4800) Integer lookbackHours, @QueryParam("status") List<SuggestionStatus> status, @QueryParam("alertId") @Size(max=50) String alertId, @QueryParam("targetType") SuggestionTargetType targetType, @QueryParam("eventName") @Size(max=100) String eventName, @QueryParam("text") @Size(max=200) String text, @QueryParam("agentDefinitionId") @Size(max=50) String agentDefinitionId, @QueryParam("agentRunId") @Size(max=50) String agentRunId) {
        System.out.println("searchSuggestionHeaders: " + "lookbackHours=" + lookbackHours + ", " + "status=" + status + ", " + "alertId=" + alertId + ", " + "targetType=" + targetType + ", " + "eventName=" + eventName + ", " + "text=" + text + ", " + "agentDefinitionId=" + agentDefinitionId + ", " + "agentRunId=" + agentRunId);
        return List.of();
    }

    @POST
    @Path("/alerts/{alertId}/verify")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AlertDetail verifyAlert(@PathParam("alertId") @Size(max=50) String alertId, @Valid AlertVerificationRequest alertVerificationRequest) {
        try {
            String validatedAlertId = AssistantApiInputValidator.validateAlertIdForVerify(alertId);
            if (alertService.existsDeletedAlert(validatedAlertId)) {
                throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                        .entity(AssistantApiErrors.alertVerifyDeletedAlert())
                        .build());
            }
            return alertService.verifyAlert(validatedAlertId, alertVerificationRequest)
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(AssistantApiErrors.alertVerifyNotFound())
                            .build()));
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_VERIFY] Unexpected error alertId=" + alertId + " error=" + ex.getMessage());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.alertVerifyUnexpectedError())
                    .build());
        }
    }
    @POST
    @Path("/utilities/text/improve")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public String improveText(String body) {
        System.out.println("[IIA-AI-TEST] improveText endpoint invoked");
        return textImproveUseCase.improve(body);
    }

}
