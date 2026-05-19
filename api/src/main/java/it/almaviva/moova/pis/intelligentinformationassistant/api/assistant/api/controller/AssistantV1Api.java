package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error.AssistantApiErrors;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.interfaces.IAssistantV1Api;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.validation.AssistantApiInputValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.*;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AlertService;

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
            System.out.println("createAlert: " + "alertCreateRequest=" + validatedRequest);

            if (Boolean.TRUE.equals(validatedRequest.getVerifyImmediately())) {
                throw new WebApplicationException(Response.status(422)
                        .entity(AssistantApiErrors.alertCreateVerificationUnsupported())
                        .build());
            }

            if (alertService.existsActiveAlertWithNormalizedName(validatedRequest.getName())) {
                throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                        .entity(AssistantApiErrors.alertCreateDuplicateName())
                        .build());
            }

            return alertService.createDraftAlert(validatedRequest);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
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
        System.out.println("deleteAlert: " + "alertId=" + alertId);
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

    @POST
    @Path("/alerts/{alertId}/disable")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AlertDetail disableAlert(@PathParam("alertId") @Size(max=50) String alertId, @Valid AlertDisableRequest alertDisableRequest) {
        System.out.println("disableAlert: " + "alertId=" + alertId + ", " + "alertDisableRequest=" + alertDisableRequest);
        return new AlertDetail();
    }

    @POST
    @Path("/alerts/{alertId}/enable")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public AlertDetail enableAlert(@PathParam("alertId") @Size(max=50) String alertId, @Valid AlertEnableRequest alertEnableRequest) {
        System.out.println("enableAlert: " + "alertId=" + alertId + ", " + "alertEnableRequest=" + alertEnableRequest);
        return new AlertDetail();
    }

    @GET
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
            System.out.println("getAlert: " + "alertId=" + validatedAlertId);
            return alertService.getAlert(validatedAlertId)
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(AssistantApiErrors.alertGetNotFound())
                            .build()));
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
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
    public AlertDetail patchAlert(@PathParam("alertId") @Size(max=50) String alertId, @Valid @NotNull AlertPatchRequest alertPatchRequest) {
        System.out.println("patchAlert: " + "alertId=" + alertId + ", " + "alertPatchRequest=" + alertPatchRequest);
        return new AlertDetail();
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
        System.out.println("previewAgentGenerationForAlert: " + "alertId=" + alertId + ", " + "agentGenerationPreviewRequest=" + agentGenerationPreviewRequest);
        return new AgentGenerationPreviewResponse();
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
            System.out.println("searchAlerts: " + "criteria=" + criteria);
            return alertService.searchAlerts(criteria);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
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
        System.out.println("verifyAlert: " + "alertId=" + alertId + ", " + "alertVerificationRequest=" + alertVerificationRequest);
        return new AlertDetail();
    }
    @POST
    @Path("/utilities/text/improve")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Override
    public String improveText(@Valid @NotNull String body) {
        return "TEST";
    }

}
