package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AssistantQuestionRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AssistantQuestionResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import java.time.OffsetDateTime;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SourceEventType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Suggestion;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionApprovalRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionRejectionRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionSeverity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionType;
import java.util.UUID;

import jakarta.ws.rs.*;

import io.swagger.annotations.*;

import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/v1")
@Api(description = "the v1 API")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public interface IAssistantV1Api {

    /**
     * Approves a suggestion. When `finalMessage` is provided, the suggestion is considered edited and approved. This operation does not publish the message to passenger channels. 
     *
     * @param X_WTF_PROFILE Profile chosen by the user.
     * @param suggestionId Suggestion identifier.
     * @param suggestionApprovalRequest 
     * @return Suggestion approved
     * @return Bad request
     * @return Missing or invalid authentication token
     * @return Forbidden
     * @return Resource not found
     * @return Invalid resource state transition
     * @return Unexpected error
     */
    @POST
    @Path("/suggestions/{suggestionId}/approve")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Approve a suggestion", notes = "Approves a suggestion. When `finalMessage` is provided, the suggestion is considered edited and approved. This operation does not publish the message to passenger channels. ", tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion approved", response = Suggestion.class),
        @ApiResponse(code = 400, message = "Bad request", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication token", response = Error.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Error.class),
        @ApiResponse(code = 404, message = "Resource not found", response = Error.class),
        @ApiResponse(code = 409, message = "Invalid resource state transition", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Error.class) })
    Suggestion approveSuggestion(@HeaderParam("X-WTF-PROFILE") @NotNull   @ApiParam("Profile chosen by the user.") String X_WTF_PROFILE,@PathParam("suggestionId") @ApiParam("Suggestion identifier.") UUID suggestionId,@Valid SuggestionApprovalRequest suggestionApprovalRequest);


    /**
     * Sends a natural language question to the Intelligent Information Assistant.  The request intentionally contains only the `question` field. The UI is not required to provide `stopPointId`, selected journey, time window or any other operational context.  ## Backend behavior  Upon receiving the question, the backend should:  - validate the request body; - use deterministic rules and/or an LLM to detect the intent and extract entities; - resolve ambiguous entities when possible, for example by using location, stop point, journey or timetable tools; - select the allowed backend tools required to answer the question; - call the required domain APIs/tools; - build a structured response grounded on retrieved data; - optionally use an LLM to format the final natural language answer; - store the question, detected intent, used tools and answer for audit/debug purposes.  ## Examples  The same endpoint can support questions that require different tools:  - \"Quali corse partono da Torino alle 13?\" may require place resolution and timetable/Service Data tools. - \"La corsa AV 304 è in ritardo?\" may require journey resolution and Service Data tools. - \"Che annunci hai fatto per la corsa AV 304 oggi?\" may require journey, announcement and broadcast history tools. - \"Ci sono corse cancellate oggi senza annunci?\" may require Service Data and announcement tools.  ## AI responsibility boundary  The LLM must not invent operational information. The final answer must be based on retrieved tool results. If the question is ambiguous and the backend cannot resolve the missing context with sufficient confidence, the response should ask for clarification or return an unsupported/ambiguous status. 
     *
     * @param X_WTF_PROFILE Profile chosen by the user.
     * @param assistantQuestionRequest 
     * @return Assistant answer
     * @return Bad request
     * @return Missing or invalid authentication token
     * @return Forbidden
     * @return Unsupported or ambiguous question
     * @return Unexpected error
     * @return Downstream dependency not available
     */
    @POST
    @Path("/assistant/questions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Ask a generic operational question to the assistant", notes = "Sends a natural language question to the Intelligent Information Assistant.  The request intentionally contains only the `question` field. The UI is not required to provide `stopPointId`, selected journey, time window or any other operational context.  ## Backend behavior  Upon receiving the question, the backend should:  - validate the request body; - use deterministic rules and/or an LLM to detect the intent and extract entities; - resolve ambiguous entities when possible, for example by using location, stop point, journey or timetable tools; - select the allowed backend tools required to answer the question; - call the required domain APIs/tools; - build a structured response grounded on retrieved data; - optionally use an LLM to format the final natural language answer; - store the question, detected intent, used tools and answer for audit/debug purposes.  ## Examples  The same endpoint can support questions that require different tools:  - \"Quali corse partono da Torino alle 13?\" may require place resolution and timetable/Service Data tools. - \"La corsa AV 304 è in ritardo?\" may require journey resolution and Service Data tools. - \"Che annunci hai fatto per la corsa AV 304 oggi?\" may require journey, announcement and broadcast history tools. - \"Ci sono corse cancellate oggi senza annunci?\" may require Service Data and announcement tools.  ## AI responsibility boundary  The LLM must not invent operational information. The final answer must be based on retrieved tool results. If the question is ambiguous and the backend cannot resolve the missing context with sufficient confidence, the response should ask for clarification or return an unsupported/ambiguous status. ", tags={ "Assistant" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Assistant answer", response = AssistantQuestionResponse.class),
        @ApiResponse(code = 400, message = "Bad request", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication token", response = Error.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Error.class),
        @ApiResponse(code = 422, message = "Unsupported or ambiguous question", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Error.class),
        @ApiResponse(code = 503, message = "Downstream dependency not available", response = Error.class) })
    AssistantQuestionResponse askAssistantQuestion(@HeaderParam("X-WTF-PROFILE") @NotNull   @ApiParam("Profile chosen by the user.") String X_WTF_PROFILE,@Valid @NotNull AssistantQuestionRequest assistantQuestionRequest);


    /**
     * Returns the detail of a single assistant suggestion.
     *
     * @param X_WTF_PROFILE Profile chosen by the user.
     * @param suggestionId Suggestion identifier.
     * @return Suggestion detail
     * @return Bad request
     * @return Missing or invalid authentication token
     * @return Forbidden
     * @return Resource not found
     * @return Unexpected error
     */
    @GET
    @Path("/suggestions/{suggestionId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a suggestion by identifier", notes = "Returns the detail of a single assistant suggestion.", tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion detail", response = Suggestion.class),
        @ApiResponse(code = 400, message = "Bad request", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication token", response = Error.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Error.class),
        @ApiResponse(code = 404, message = "Resource not found", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Error.class) })
    Suggestion getSuggestionById(@HeaderParam("X-WTF-PROFILE") @NotNull   @ApiParam("Profile chosen by the user.") String X_WTF_PROFILE,@PathParam("suggestionId") @ApiParam("Suggestion identifier.") UUID suggestionId);


    /**
     * Rejects a suggestion and stores the optional operator note.
     *
     * @param X_WTF_PROFILE Profile chosen by the user.
     * @param suggestionId Suggestion identifier.
     * @param suggestionRejectionRequest 
     * @return Suggestion rejected
     * @return Bad request
     * @return Missing or invalid authentication token
     * @return Forbidden
     * @return Resource not found
     * @return Invalid resource state transition
     * @return Unexpected error
     */
    @POST
    @Path("/suggestions/{suggestionId}/reject")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Reject a suggestion", notes = "Rejects a suggestion and stores the optional operator note.", tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion rejected", response = Suggestion.class),
        @ApiResponse(code = 400, message = "Bad request", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication token", response = Error.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Error.class),
        @ApiResponse(code = 404, message = "Resource not found", response = Error.class),
        @ApiResponse(code = 409, message = "Invalid resource state transition", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Error.class) })
    Suggestion rejectSuggestion(@HeaderParam("X-WTF-PROFILE") @NotNull   @ApiParam("Profile chosen by the user.") String X_WTF_PROFILE,@PathParam("suggestionId") @ApiParam("Suggestion identifier.") UUID suggestionId,@Valid SuggestionRejectionRequest suggestionRejectionRequest);


    /**
     * Returns a paginated list of suggestions generated by the Intelligent Information Assistant.  Suggestions are generated from deterministic rules applied to relevant operational events. The backend searches the Assistant suggestion store only. It does not query a local replicated Service Data model.  The result set can be filtered by type, status, severity, source event, business correlation fields and creation date. 
     *
     * @param X_WTF_PROFILE Profile chosen by the user.
     * @param pageNumber Page number, starting from 0.
     * @param pageSize Number of elements per page.
     * @param sort Sort criteria using &#x60;property,(asc|desc)&#x60; syntax.
     * @param type Filter by suggestion type.
     * @param status Filter by suggestion status.
     * @param severity Filter by suggestion severity.
     * @param sourceEventType Filter by source event type.
     * @param vehicleJourneyName Filter by journey name/number using partial match.
     * @param infomobilityVehicleJourneyId Filter by Infomobility Vehicle Journey identifier.
     * @param stopPointName Filter by stop point name using partial match, when available in suggestion context.
     * @param createdFrom Lower bound for suggestion creation date-time.
     * @param createdTo Upper bound for suggestion creation date-time.
     * @return Suggestions found
     * @return Bad request
     * @return Missing or invalid authentication token
     * @return Forbidden
     * @return Unexpected error
     */
    @GET
    @Path("/suggestions")
    @Produces({ "application/json" })
    @ApiOperation(value = "Search assistant suggestions", notes = "Returns a paginated list of suggestions generated by the Intelligent Information Assistant.  Suggestions are generated from deterministic rules applied to relevant operational events. The backend searches the Assistant suggestion store only. It does not query a local replicated Service Data model.  The result set can be filtered by type, status, severity, source event, business correlation fields and creation date. ", tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestions found", response = Suggestion.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad request", response = Error.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Missing or invalid authentication token", response = Error.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = Error.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected error", response = Error.class, responseContainer = "List") })
    List<Suggestion> searchSuggestions(@HeaderParam("X-WTF-PROFILE") @NotNull   @ApiParam("Profile chosen by the user.") String X_WTF_PROFILE,@QueryParam("pageNumber") @NotNull @Min(0)  @ApiParam("Page number, starting from 0.")  Integer pageNumber,@QueryParam("pageSize") @NotNull @Min(1) @Max(200)  @ApiParam("Number of elements per page.")  Integer pageSize,@QueryParam("sort")  @ApiParam("Sort criteria using &#x60;property,(asc|desc)&#x60; syntax.")  String sort,@QueryParam("type")  @ApiParam("Filter by suggestion type.")  SuggestionType type,@QueryParam("status")  @ApiParam("Filter by suggestion status.")  SuggestionStatus status,@QueryParam("severity")  @ApiParam("Filter by suggestion severity.")  SuggestionSeverity severity,@QueryParam("sourceEventType")  @ApiParam("Filter by source event type.")  SourceEventType sourceEventType,@QueryParam("vehicleJourneyName")  @ApiParam("Filter by journey name/number using partial match.")  String vehicleJourneyName,@QueryParam("infomobilityVehicleJourneyId")  @ApiParam("Filter by Infomobility Vehicle Journey identifier.")  String infomobilityVehicleJourneyId,@QueryParam("stopPointName")  @ApiParam("Filter by stop point name using partial match, when available in suggestion context.")  String stopPointName,@QueryParam("createdFrom")  @ApiParam("Lower bound for suggestion creation date-time.")  OffsetDateTime createdFrom,@QueryParam("createdTo")  @ApiParam("Upper bound for suggestion creation date-time.")  OffsetDateTime createdTo);

}
