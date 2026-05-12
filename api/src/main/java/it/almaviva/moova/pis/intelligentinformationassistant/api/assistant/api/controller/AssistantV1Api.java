package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.IAssistantV1Api;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssistantV1Api implements IAssistantV1Api {
    @Override
    @POST
    @Path("/suggestions/{suggestionId}/approve")
    public Suggestion approveSuggestion(
            @HeaderParam("X-WTF-PROFILE") @NotNull String X_WTF_PROFILE,
            @PathParam("suggestionId") UUID suggestionId,
            @Valid SuggestionApprovalRequest suggestionApprovalRequest) {
        return new Suggestion();
    }

    @Override
    @POST
    @Path("/assistant/questions")
    public AssistantQuestionResponse askAssistantQuestion(
            @HeaderParam("X-WTF-PROFILE") @NotNull String X_WTF_PROFILE,
            @Valid @NotNull AssistantQuestionRequest assistantQuestionRequest) {
        return new AssistantQuestionResponse();
    }

    @Override
    @GET
    @Path("/suggestions/{suggestionId}")
    public Suggestion getSuggestionById(
            @HeaderParam("X-WTF-PROFILE") @NotNull String X_WTF_PROFILE,
            @PathParam("suggestionId") UUID suggestionId) {
        return new Suggestion();
    }

    @Override
    @POST
    @Path("/suggestions/{suggestionId}/reject")
    public Suggestion rejectSuggestion(
            @HeaderParam("X-WTF-PROFILE") @NotNull String X_WTF_PROFILE,
            @PathParam("suggestionId") UUID suggestionId,
            @Valid SuggestionRejectionRequest suggestionRejectionRequest) {
        return new Suggestion();
    }

    @Override
    @GET
    @Path("/suggestions")
    public List<Suggestion> searchSuggestions(
            @HeaderParam("X-WTF-PROFILE") @NotNull String X_WTF_PROFILE,
            @QueryParam("pageNumber") @NotNull @Min(0) Integer pageNumber,
            @QueryParam("pageSize") @NotNull @Min(1) @Max(200) Integer pageSize,
            @QueryParam("sort") String sort,
            @QueryParam("type") SuggestionType type,
            @QueryParam("status") SuggestionStatus status,
            @QueryParam("severity") SuggestionSeverity severity,
            @QueryParam("sourceEventType") SourceEventType sourceEventType,
            @QueryParam("vehicleJourneyName") String vehicleJourneyName,
            @QueryParam("infomobilityVehicleJourneyId") String infomobilityVehicleJourneyId,
            @QueryParam("stopPointName") String stopPointName,
            @QueryParam("createdFrom") OffsetDateTime createdFrom,
            @QueryParam("createdTo") OffsetDateTime createdTo) {
        return List.of();
    }
}
