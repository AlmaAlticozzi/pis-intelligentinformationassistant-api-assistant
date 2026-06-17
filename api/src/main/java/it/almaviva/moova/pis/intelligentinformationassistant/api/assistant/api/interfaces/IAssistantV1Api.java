package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.interfaces;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.*;

import java.time.OffsetDateTime;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error;
import jakarta.ws.rs.*;

import io.swagger.annotations.*;

import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/v1")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public interface IAssistantV1Api {

    /**
     * Activates a READY Agent definition.  Activation tells the Agent Orchestrator that the definition is eligible for runtime execution according to its activation policy. The operation does not necessarily create a running pod immediately. For `CONTINUOUS`, a run is created if the current instant is inside the validity interval. For `DAILY_WINDOW`, a run is created only when the current local time is inside an active daily window.  ```text READY Agent Definition     -> activate     -> validate artifact hash/signature metadata     -> validate resource profile and namespace quota     -> mark Agent Definition ACTIVE     -> Agent Orchestrator evaluates activation policy     -> create Agent Run when policy is currently active ```  ## Errors  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-ACT-404-001`  - `HTTP Status Code 409`, when the Agent definition is not in READY or DISABLED status. Error code: `IIA-AGD-ACT-409-001`  - `HTTP Status Code 422`, when the Agent artifact is missing, unsigned or incompatible with the runtime SDK. Error code: `IIA-AGD-ACT-422-001`  - `HTTP Status Code 503`, when the Agent Orchestrator or Kubernetes integration is not available. Error code: `IIA-AGD-ACT-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while activating the Agent definition. Error code: `IIA-AGD-ACT-500-001` 
     *
     * @param agentDefinitionId Agent definition identifier.
     * @param agentActivationRequest 
     * @return Agent definition activated successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @POST
    @Path("/agent-definitions/{agentDefinitionId}/activate")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Activate an Agent definition", notes = "Activates a READY Agent definition.  Activation tells the Agent Orchestrator that the definition is eligible for runtime execution according to its activation policy. The operation does not necessarily create a running pod immediately. For `CONTINUOUS`, a run is created if the current instant is inside the validity interval. For `DAILY_WINDOW`, a run is created only when the current local time is inside an active daily window.  ```text READY Agent Definition     -> activate     -> validate artifact hash/signature metadata     -> validate resource profile and namespace quota     -> mark Agent Definition ACTIVE     -> Agent Orchestrator evaluates activation policy     -> create Agent Run when policy is currently active ```  ## Errors  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-ACT-404-001`  - `HTTP Status Code 409`, when the Agent definition is not in READY or DISABLED status. Error code: `IIA-AGD-ACT-409-001`  - `HTTP Status Code 422`, when the Agent artifact is missing, unsigned or incompatible with the runtime SDK. Error code: `IIA-AGD-ACT-422-001`  - `HTTP Status Code 503`, when the Agent Orchestrator or Kubernetes integration is not available. Error code: `IIA-AGD-ACT-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while activating the Agent definition. Error code: `IIA-AGD-ACT-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent definition activated successfully.", response = AgentDefinitionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 503, message = "A downstream system, tool or LLM provider is not available.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentDefinitionDetail activateAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) @ApiParam("Agent definition identifier.") String agentDefinitionId,@Valid AgentActivationRequest agentActivationRequest);


    /**
     * Approves a suggestion currently in `TO_REVIEW`.  Approval stores the final operator decision. If `passengerMessage` or `operatorAdvice` are supplied in the request, they are applied as last-minute edits before approval. If they are omitted, the current persisted texts are used.  Target status: `APPROVED`. Invalid workflow transitions return HTTP `409`.          ## Additional notes  - This operation approves a suggestion currently in `TO_REVIEW`.  - If `operatorAdvice` or `passengerMessage` are provided, they are applied as final edits before approval.  - If `operatorAdvice` or `passengerMessage` are omitted, the current persisted texts are used.  - Approval stores the operator action metadata and changes the suggestion status to `APPROVED`.  - Approval does not automatically publish passenger-facing content unless a downstream implementation explicitly supports and controls that behavior.  - Invalid workflow transitions return HTTP Status Code `409`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-APR-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-APR-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `SuggestionApprovalRequest`. Error code: `IIA-SUG-APR-400-003`  - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-APR-404-001`  - `HTTP Status Code 409`, when the suggestion status is not `TO_REVIEW`. Error code: `IIA-SUG-APR-409-001`  - `HTTP Status Code 409`, when the suggestion has already been approved, rejected, expired or marked as error by another operator or process. Error code: `IIA-SUG-APR-409-002`  - `HTTP Status Code 500`, when an unexpected error occurs while approving the suggestion. Error code: `IIA-SUG-APR-500-001` 
     *
     * @param suggestionId Suggestion identifier.
     * @param suggestionApprovalRequest 
     * @return Suggestion approved successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return Unexpected error.
     */
    @POST
    @Path("/suggestions/{suggestionId}/approve")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Approve a suggestion", notes = "Approves a suggestion currently in `TO_REVIEW`.  Approval stores the final operator decision. If `passengerMessage` or `operatorAdvice` are supplied in the request, they are applied as last-minute edits before approval. If they are omitted, the current persisted texts are used.  Target status: `APPROVED`. Invalid workflow transitions return HTTP `409`.          ## Additional notes  - This operation approves a suggestion currently in `TO_REVIEW`.  - If `operatorAdvice` or `passengerMessage` are provided, they are applied as final edits before approval.  - If `operatorAdvice` or `passengerMessage` are omitted, the current persisted texts are used.  - Approval stores the operator action metadata and changes the suggestion status to `APPROVED`.  - Approval does not automatically publish passenger-facing content unless a downstream implementation explicitly supports and controls that behavior.  - Invalid workflow transitions return HTTP Status Code `409`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-APR-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-APR-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `SuggestionApprovalRequest`. Error code: `IIA-SUG-APR-400-003`  - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-APR-404-001`  - `HTTP Status Code 409`, when the suggestion status is not `TO_REVIEW`. Error code: `IIA-SUG-APR-409-001`  - `HTTP Status Code 409`, when the suggestion has already been approved, rejected, expired or marked as error by another operator or process. Error code: `IIA-SUG-APR-409-002`  - `HTTP Status Code 500`, when an unexpected error occurs while approving the suggestion. Error code: `IIA-SUG-APR-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion approved successfully.", response = SuggestionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    SuggestionDetail approveSuggestion(@PathParam("suggestionId") @Size(max=50) @ApiParam("Suggestion identifier.") String suggestionId,@Valid SuggestionApprovalRequest suggestionApprovalRequest);


    /**
     * Sends an operator question to the Intelligent Information Assistant.  The request intentionally requires only `question`. The UI is not required to provide `stopPointId`, selected journey, time window or any operational identifier.  ## Execution flow  ```text Question   -> create/resume session   -> store user message   -> resolve intent and entities   -> build validated tool plan   -> execute domain tools   -> build verified context   -> optionally call LLM for formatting   -> store answer and tool traces   -> return answer and structured items ```  ## Examples  - `Quali corse partono da Torino alle 13?` - `La corsa AV 304 è in ritardo?` - `Che annunci hai fatto per la corsa AV 304 oggi?` - `Perché non è stato annunciato il ritardo della corsa AV 304?`  Unsupported questions return HTTP `422` or a response with status `UNSUPPORTED`, depending on whether the request can still be represented as an assistant answer.  ## Additional notes  - This operation sends an operator natural-language question to the Intelligent Information Assistant.  - The UI is required to provide only `question`.  - The UI is not required to provide `stopPointId`, selected journey, time window or operational identifiers.  - When `sessionId` is omitted, the backend creates a new assistant session automatically.  - When `sessionId` is provided, the question is appended to the existing session and can use previous conversational context.  - `uiContext` is optional and must never be the only source of truth for operational data.  - Operational facts must be retrieved through controlled Moova domain tools and must not be invented by the assistant.  - `includeItems = true` asks the backend to return structured answer items when available.  - `includeToolExecutions = true` returns compact tool execution summaries only. Full sanitized traces remain available through the diagnostics endpoint.  - Unsupported questions can return HTTP Status Code `422` or a response with status `UNSUPPORTED`, depending on whether the request can still be represented as an assistant answer.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-AST-ASK-400-001`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AssistantQuestionRequest`. Error code: `IIA-AST-ASK-400-002`  - `HTTP Status Code 400`, when the `question` field is missing, empty or contains only whitespace characters. Error code: `IIA-AST-ASK-400-003`  - `HTTP Status Code 400`, when the `question` field exceeds 2000 characters. Error code: `IIA-AST-ASK-400-004`  - `HTTP Status Code 400`, when the `sessionId` field exceeds 50 characters. Error code: `IIA-AST-ASK-400-005`  - `HTTP Status Code 400`, when the `preferredLanguage` field is not supported by the implementation. Error code: `IIA-AST-ASK-400-006`  - `HTTP Status Code 400`, when the `uiContext` field contains unsupported, unsafe or non-serializable values. Error code: `IIA-AST-ASK-400-007`  - `HTTP Status Code 404`, when `sessionId` is provided and no assistant session with the given `sessionId` is found. Error code: `IIA-AST-ASK-404-001`  - `HTTP Status Code 409`, when `sessionId` is provided and the session is closed or cannot accept new questions. Error code: `IIA-AST-ASK-409-001`  - `HTTP Status Code 422`, when the question is outside the supported operational scope of the assistant. Error code: `IIA-AST-ASK-422-001`  - `HTTP Status Code 422`, when the question requires operational data sources that are not available through the Assistant Tools Layer. Error code: `IIA-AST-ASK-422-002`  - `HTTP Status Code 422`, when the question cannot be answered safely because the intent or required entities are ambiguous and cannot be represented as a clarification response. Error code: `IIA-AST-ASK-422-003`  - `HTTP Status Code 500`, when an unexpected error occurs while processing the assistant question. Error code: `IIA-AST-ASK-500-001`  - `HTTP Status Code 503`, when the LLM provider or one or more required domain tools are temporarily unavailable. Error code: `IIA-AST-ASK-503-001` 
     *
     * @param assistantQuestionRequest 
     * @return Question processed successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     * @return A downstream system, tool or LLM provider is not available.
     */
    @POST
    @Path("/assistant/questions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Ask a natural-language operational question", notes = "Sends an operator question to the Intelligent Information Assistant.  The request intentionally requires only `question`. The UI is not required to provide `stopPointId`, selected journey, time window or any operational identifier.  ## Execution flow  ```text Question   -> create/resume session   -> store user message   -> resolve intent and entities   -> build validated tool plan   -> execute domain tools   -> build verified context   -> optionally call LLM for formatting   -> store answer and tool traces   -> return answer and structured items ```  ## Examples  - `Quali corse partono da Torino alle 13?` - `La corsa AV 304 è in ritardo?` - `Che annunci hai fatto per la corsa AV 304 oggi?` - `Perché non è stato annunciato il ritardo della corsa AV 304?`  Unsupported questions return HTTP `422` or a response with status `UNSUPPORTED`, depending on whether the request can still be represented as an assistant answer.  ## Additional notes  - This operation sends an operator natural-language question to the Intelligent Information Assistant.  - The UI is required to provide only `question`.  - The UI is not required to provide `stopPointId`, selected journey, time window or operational identifiers.  - When `sessionId` is omitted, the backend creates a new assistant session automatically.  - When `sessionId` is provided, the question is appended to the existing session and can use previous conversational context.  - `uiContext` is optional and must never be the only source of truth for operational data.  - Operational facts must be retrieved through controlled Moova domain tools and must not be invented by the assistant.  - `includeItems = true` asks the backend to return structured answer items when available.  - `includeToolExecutions = true` returns compact tool execution summaries only. Full sanitized traces remain available through the diagnostics endpoint.  - Unsupported questions can return HTTP Status Code `422` or a response with status `UNSUPPORTED`, depending on whether the request can still be represented as an assistant answer.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-AST-ASK-400-001`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AssistantQuestionRequest`. Error code: `IIA-AST-ASK-400-002`  - `HTTP Status Code 400`, when the `question` field is missing, empty or contains only whitespace characters. Error code: `IIA-AST-ASK-400-003`  - `HTTP Status Code 400`, when the `question` field exceeds 2000 characters. Error code: `IIA-AST-ASK-400-004`  - `HTTP Status Code 400`, when the `sessionId` field exceeds 50 characters. Error code: `IIA-AST-ASK-400-005`  - `HTTP Status Code 400`, when the `preferredLanguage` field is not supported by the implementation. Error code: `IIA-AST-ASK-400-006`  - `HTTP Status Code 400`, when the `uiContext` field contains unsupported, unsafe or non-serializable values. Error code: `IIA-AST-ASK-400-007`  - `HTTP Status Code 404`, when `sessionId` is provided and no assistant session with the given `sessionId` is found. Error code: `IIA-AST-ASK-404-001`  - `HTTP Status Code 409`, when `sessionId` is provided and the session is closed or cannot accept new questions. Error code: `IIA-AST-ASK-409-001`  - `HTTP Status Code 422`, when the question is outside the supported operational scope of the assistant. Error code: `IIA-AST-ASK-422-001`  - `HTTP Status Code 422`, when the question requires operational data sources that are not available through the Assistant Tools Layer. Error code: `IIA-AST-ASK-422-002`  - `HTTP Status Code 422`, when the question cannot be answered safely because the intent or required entities are ambiguous and cannot be represented as a clarification response. Error code: `IIA-AST-ASK-422-003`  - `HTTP Status Code 500`, when an unexpected error occurs while processing the assistant question. Error code: `IIA-AST-ASK-500-001`  - `HTTP Status Code 503`, when the LLM provider or one or more required domain tools are temporarily unavailable. Error code: `IIA-AST-ASK-503-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Assistant" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Question processed successfully.", response = AssistantQuestionResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class),
        @ApiResponse(code = 503, message = "A downstream system, tool or LLM provider is not available.", response = Error.class) })
    AssistantQuestionResponse askAssistantQuestion(@Valid @NotNull AssistantQuestionRequest assistantQuestionRequest);


    /**
     * Starts or restarts the controlled Agent compilation pipeline.  Compilation means producing a validated, versioned and signed Agent artifact. The artifact can be a DSL document interpreted by the standard runtime or a generated Java JAR produced from approved templates.  ```text Agent Definition     -> Agent Compiler     -> generate Agent Blueprint     -> validate Blueprint     -> generate DSL or Java template code     -> static analysis     -> compilation, when Java     -> automatic tests     -> optional historical/synthetic simulation     -> hash and sign artifact     -> update Agent Definition status to READY or REJECTED ```  The Agent Runtime never compiles code. It only loads artifacts that have already passed this pipeline.  ## Errors  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AgentCompilationRequest`. Error code: `IIA-AGD-CMP-400-001`  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-CMP-404-001`  - `HTTP Status Code 409`, when the Agent definition is already active or another compilation is already running. Error code: `IIA-AGD-CMP-409-001`  - `HTTP Status Code 422`, when the definition cannot be compiled because the Alert verification is incomplete or inconsistent. Error code: `IIA-AGD-CMP-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while starting Agent compilation. Error code: `IIA-AGD-CMP-500-001` 
     *
     * @param agentDefinitionId Agent definition identifier.
     * @param agentCompilationRequest 
     * @return Agent compilation started successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @POST
    @Path("/agent-definitions/{agentDefinitionId}/compile")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Compile an Agent definition", notes = "Starts or restarts the controlled Agent compilation pipeline.  Compilation means producing a validated, versioned and signed Agent artifact. The artifact can be a DSL document interpreted by the standard runtime or a generated Java JAR produced from approved templates.  ```text Agent Definition     -> Agent Compiler     -> generate Agent Blueprint     -> validate Blueprint     -> generate DSL or Java template code     -> static analysis     -> compilation, when Java     -> automatic tests     -> optional historical/synthetic simulation     -> hash and sign artifact     -> update Agent Definition status to READY or REJECTED ```  The Agent Runtime never compiles code. It only loads artifacts that have already passed this pipeline.  ## Errors  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AgentCompilationRequest`. Error code: `IIA-AGD-CMP-400-001`  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-CMP-404-001`  - `HTTP Status Code 409`, when the Agent definition is already active or another compilation is already running. Error code: `IIA-AGD-CMP-409-001`  - `HTTP Status Code 422`, when the definition cannot be compiled because the Alert verification is incomplete or inconsistent. Error code: `IIA-AGD-CMP-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while starting Agent compilation. Error code: `IIA-AGD-CMP-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent compilation started successfully.", response = AgentCompilationStatusResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentCompilationStatusResponse compileAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) @ApiParam("Agent definition identifier.") String agentDefinitionId,@Valid AgentCompilationRequest agentCompilationRequest);


    /**
     * Creates an Agent definition from a verified Alert.  This operation is the practical injection point between Alert verification and Agent runtime creation. The API does not receive executable code from the UI. It receives a request that selects the verified Alert, the desired resource profile, activation policy and generation mode. The backend then creates a persistent Agent definition and starts the controlled compilation pipeline when required.  ```text POST /v1/agent-definitions     -> validate Alert exists and is verified     -> validate Alert version     -> validate Agent profile     -> validate activation policy and timezone     -> create Agent Definition in DRAFT or COMPILATION_PENDING     -> enqueue Agent compilation request when requested     -> return Agent Definition ```  If `compileImmediately` is true, the response can already include a compilation status in `PENDING` or `GENERATING_BLUEPRINT`. Runtime execution never starts from this operation. Activation requires `POST /v1/agent-definitions/{agentDefinitionId}/activate`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-AGD-CRE-400-001`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AgentDefinitionCreateRequest`. Error code: `IIA-AGD-CRE-400-002`  - `HTTP Status Code 400`, when the activation policy is not coherent with its declared type. Error code: `IIA-AGD-CRE-400-003`  - `HTTP Status Code 404`, when no Alert or Agent profile referenced by the request is found. Error code: `IIA-AGD-CRE-404-001`  - `HTTP Status Code 409`, when the Alert is not verified or the requested Alert version is no longer current. Error code: `IIA-AGD-CRE-409-001`  - `HTTP Status Code 422`, when the Alert cannot be transformed into the requested generation mode. Error code: `IIA-AGD-CRE-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while creating the Agent definition. Error code: `IIA-AGD-CRE-500-001` 
     *
     * @param agentDefinitionCreateRequest 
     * @return Agent definition created successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @POST
    @Path("/agent-definitions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create an Agent definition from a verified Alert", notes = "Creates an Agent definition from a verified Alert.  This operation is the practical injection point between Alert verification and Agent runtime creation. The API does not receive executable code from the UI. It receives a request that selects the verified Alert, the desired resource profile, activation policy and generation mode. The backend then creates a persistent Agent definition and starts the controlled compilation pipeline when required.  ```text POST /v1/agent-definitions     -> validate Alert exists and is verified     -> validate Alert version     -> validate Agent profile     -> validate activation policy and timezone     -> create Agent Definition in DRAFT or COMPILATION_PENDING     -> enqueue Agent compilation request when requested     -> return Agent Definition ```  If `compileImmediately` is true, the response can already include a compilation status in `PENDING` or `GENERATING_BLUEPRINT`. Runtime execution never starts from this operation. Activation requires `POST /v1/agent-definitions/{agentDefinitionId}/activate`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-AGD-CRE-400-001`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AgentDefinitionCreateRequest`. Error code: `IIA-AGD-CRE-400-002`  - `HTTP Status Code 400`, when the activation policy is not coherent with its declared type. Error code: `IIA-AGD-CRE-400-003`  - `HTTP Status Code 404`, when no Alert or Agent profile referenced by the request is found. Error code: `IIA-AGD-CRE-404-001`  - `HTTP Status Code 409`, when the Alert is not verified or the requested Alert version is no longer current. Error code: `IIA-AGD-CRE-409-001`  - `HTTP Status Code 422`, when the Alert cannot be transformed into the requested generation mode. Error code: `IIA-AGD-CRE-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while creating the Agent definition. Error code: `IIA-AGD-CRE-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent definition created successfully.", response = AgentDefinitionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentDefinitionDetail createAgentDefinition(@Valid @NotNull AgentDefinitionCreateRequest agentDefinitionCreateRequest);


    /**
     * Creates an alert definition from a free-text operator prompt and starts the verification flow.  The backend should create the alert with status `VERIFYING`, run the verification synchronously when possible, and return either a `VERIFIED` or `REJECTED` alert. If verification is asynchronous, the backend may return `VERIFYING` with HTTP `202` only if the implementation supports background verification.  The alert is not enabled automatically unless `enableAfterVerification` is true and verification succeeds.  ## Additional notes  - This operation creates an alert definition from a free-text operator prompt and starts the verification flow.  - The backend should create the alert with status `VERIFYING`, run verification and return the resulting alert detail.  - If verification succeeds, the returned alert can have status `VERIFIED`.  - If verification fails because the prompt cannot be safely or deterministically implemented, the returned alert can have status `REJECTED`.  - The alert is not enabled automatically unless `enableAfterVerification` is `true` and verification succeeds.  - A verified alert must expose controlled interpreter metadata only. The API must not expose arbitrary executable code.  - `schedule` is required only when the selected or inferred interpreter type is `SCHEDULED_INTERPRETER`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-ALT-CRT-400-001`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AlertCreateRequest`. Error code: `IIA-ALT-CRT-400-002`  - `HTTP Status Code 400`, when the `name` field is missing, empty or contains only whitespace characters. Error code: `IIA-ALT-CRT-400-003`  - `HTTP Status Code 400`, when the `name` field exceeds 120 characters. Error code: `IIA-ALT-CRT-400-004`  - `HTTP Status Code 400`, when the `description` field exceeds 1000 characters. Error code: `IIA-ALT-CRT-400-005`  - `HTTP Status Code 400`, when the `prompt` field is missing, empty or contains only whitespace characters. Error code: `IIA-ALT-CRT-400-006`  - `HTTP Status Code 400`, when the `prompt` field contains fewer than 10 characters or exceeds 8000 characters. Error code: `IIA-ALT-CRT-400-007`  - `HTTP Status Code 400`, when `preferredInterpreterType` is not a valid value of `AlertInterpreterType`. Error code: `IIA-ALT-CRT-400-008`  - `HTTP Status Code 400`, when `preferredInterpreterType = SCHEDULED_INTERPRETER` and `schedule` is missing. Error code: `IIA-ALT-CRT-400-009`  - `HTTP Status Code 400`, when `schedule.frequencySeconds` is lower than `30` or greater than `86400`. Error code: `IIA-ALT-CRT-400-010`  - `HTTP Status Code 400`, when `schedule.timeWindowMinutes` is lower than `1` or greater than `1440`. Error code: `IIA-ALT-CRT-400-011`  - `HTTP Status Code 400`, when `schedule.cronExpression` is provided but is not supported or is not valid. Error code: `IIA-ALT-CRT-400-012`  - `HTTP Status Code 409`, when an active alert with the same normalized name already exists. Error code: `IIA-ALT-CRT-409-001`  - `HTTP Status Code 422`, when the prompt cannot be transformed into a controlled interpreter because it is ambiguous, unsafe or unsupported by available tools. Error code: `IIA-ALT-CRT-422-001`  - `HTTP Status Code 422`, when the prompt requires operational data sources that are not available through the Assistant Tools Layer. Error code: `IIA-ALT-CRT-422-002`  - `HTTP Status Code 500`, when an unexpected error occurs while creating the alert. Error code: `IIA-ALT-CRT-500-001`  - `HTTP Status Code 503`, when the verification service, LLM provider or required backend tool is temporarily unavailable. Error code: `IIA-ALT-CRT-503-001` 
     *
     * @param alertCreateRequest 
     * @return Alert created and verification completed or started.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     * @return A downstream system, tool or LLM provider is not available.
     */
    @POST
    @Path("/alerts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create and verify an alert definition", notes = "Creates an alert definition from a free-text operator prompt and starts the verification flow.  The backend should create the alert with status `VERIFYING`, run the verification synchronously when possible, and return either a `VERIFIED` or `REJECTED` alert. If verification is asynchronous, the backend may return `VERIFYING` with HTTP `202` only if the implementation supports background verification.  The alert is not enabled automatically unless `enableAfterVerification` is true and verification succeeds.  ## Additional notes  - This operation creates an alert definition from a free-text operator prompt and starts the verification flow.  - The backend should create the alert with status `VERIFYING`, run verification and return the resulting alert detail.  - If verification succeeds, the returned alert can have status `VERIFIED`.  - If verification fails because the prompt cannot be safely or deterministically implemented, the returned alert can have status `REJECTED`.  - The alert is not enabled automatically unless `enableAfterVerification` is `true` and verification succeeds.  - A verified alert must expose controlled interpreter metadata only. The API must not expose arbitrary executable code.  - `schedule` is required only when the selected or inferred interpreter type is `SCHEDULED_INTERPRETER`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-ALT-CRT-400-001`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AlertCreateRequest`. Error code: `IIA-ALT-CRT-400-002`  - `HTTP Status Code 400`, when the `name` field is missing, empty or contains only whitespace characters. Error code: `IIA-ALT-CRT-400-003`  - `HTTP Status Code 400`, when the `name` field exceeds 120 characters. Error code: `IIA-ALT-CRT-400-004`  - `HTTP Status Code 400`, when the `description` field exceeds 1000 characters. Error code: `IIA-ALT-CRT-400-005`  - `HTTP Status Code 400`, when the `prompt` field is missing, empty or contains only whitespace characters. Error code: `IIA-ALT-CRT-400-006`  - `HTTP Status Code 400`, when the `prompt` field contains fewer than 10 characters or exceeds 8000 characters. Error code: `IIA-ALT-CRT-400-007`  - `HTTP Status Code 400`, when `preferredInterpreterType` is not a valid value of `AlertInterpreterType`. Error code: `IIA-ALT-CRT-400-008`  - `HTTP Status Code 400`, when `preferredInterpreterType = SCHEDULED_INTERPRETER` and `schedule` is missing. Error code: `IIA-ALT-CRT-400-009`  - `HTTP Status Code 400`, when `schedule.frequencySeconds` is lower than `30` or greater than `86400`. Error code: `IIA-ALT-CRT-400-010`  - `HTTP Status Code 400`, when `schedule.timeWindowMinutes` is lower than `1` or greater than `1440`. Error code: `IIA-ALT-CRT-400-011`  - `HTTP Status Code 400`, when `schedule.cronExpression` is provided but is not supported or is not valid. Error code: `IIA-ALT-CRT-400-012`  - `HTTP Status Code 409`, when an active alert with the same normalized name already exists. Error code: `IIA-ALT-CRT-409-001`  - `HTTP Status Code 422`, when the prompt cannot be transformed into a controlled interpreter because it is ambiguous, unsafe or unsupported by available tools. Error code: `IIA-ALT-CRT-422-001`  - `HTTP Status Code 422`, when the prompt requires operational data sources that are not available through the Assistant Tools Layer. Error code: `IIA-ALT-CRT-422-002`  - `HTTP Status Code 500`, when an unexpected error occurs while creating the alert. Error code: `IIA-ALT-CRT-500-001`  - `HTTP Status Code 503`, when the verification service, LLM provider or required backend tool is temporarily unavailable. Error code: `IIA-ALT-CRT-503-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Alert created and verification completed or started.", response = AlertDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class),
        @ApiResponse(code = 503, message = "A downstream system, tool or LLM provider is not available.", response = Error.class) })
    AlertDetail createAlert(@Valid @NotNull AlertCreateRequest alertCreateRequest);


    /**
     * Creates a conversation session for the Assistant Chat UI.  Sessions group multiple operator messages and allow follow-up questions. The question endpoint can also create a session automatically when `sessionId` is omitted.  ## Additional notes  - This operation creates a conversation session for the Assistant Chat UI.  - Sessions group multiple operator messages and allow follow-up questions.  - The question endpoint can also create a session automatically when `sessionId` is omitted.  - `uiContext` is optional and can help follow-up questions, but it must never be mandatory for answering a question.  - The session starts with status `ACTIVE`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AssistantSessionCreateRequest`. Error code: `IIA-AST-CRT-400-001`  - `HTTP Status Code 400`, when the `title` field exceeds 200 characters. Error code: `IIA-AST-CRT-400-002`  - `HTTP Status Code 400`, when the `uiContext` field contains unsupported, unsafe or non-serializable values. Error code: `IIA-AST-CRT-400-003`  - `HTTP Status Code 500`, when an unexpected error occurs while creating the assistant session. Error code: `IIA-AST-CRT-500-001` 
     *
     * @param assistantSessionCreateRequest 
     * @return Session created successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Unexpected error.
     */
    @POST
    @Path("/assistant/sessions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create an assistant session", notes = "Creates a conversation session for the Assistant Chat UI.  Sessions group multiple operator messages and allow follow-up questions. The question endpoint can also create a session automatically when `sessionId` is omitted.  ## Additional notes  - This operation creates a conversation session for the Assistant Chat UI.  - Sessions group multiple operator messages and allow follow-up questions.  - The question endpoint can also create a session automatically when `sessionId` is omitted.  - `uiContext` is optional and can help follow-up questions, but it must never be mandatory for answering a question.  - The session starts with status `ACTIVE`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AssistantSessionCreateRequest`. Error code: `IIA-AST-CRT-400-001`  - `HTTP Status Code 400`, when the `title` field exceeds 200 characters. Error code: `IIA-AST-CRT-400-002`  - `HTTP Status Code 400`, when the `uiContext` field contains unsupported, unsafe or non-serializable values. Error code: `IIA-AST-CRT-400-003`  - `HTTP Status Code 500`, when an unexpected error occurs while creating the assistant session. Error code: `IIA-AST-CRT-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Assistant" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Session created successfully.", response = AssistantSession.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AssistantSession createAssistantSession(@Valid AssistantSessionCreateRequest assistantSessionCreateRequest);


    /**
     * Deletes or logically removes an alert definition.  Implementations may perform a soft delete to preserve audit history and suggestion relationships. Deleting an alert must disable runtime execution and stop future suggestion generation.  ## Additional notes  - Implementations may perform a soft delete to preserve audit history and suggestion relationships.  - Deleting an alert must disable runtime execution and stop future suggestion generation.  - Existing suggestions generated by the deleted alert must remain available for audit and historical review.  - A deleted alert must not be enabled, verified or executed again unless the implementation explicitly supports restore semantics.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-DEL-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-DEL-400-002`  - `HTTP Status Code 401`, when the request is not authenticated. Error code: `IIA-ALT-DEL-401-001`  - `HTTP Status Code 403`, when the authenticated user is not allowed to delete alert definitions. Error code: `IIA-ALT-DEL-403-001`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-DEL-404-001`  - `HTTP Status Code 409`, when the alert is already deleted. Error code: `IIA-ALT-DEL-409-001`  - `HTTP Status Code 409`, when the alert is currently verifying or deploying and cannot be deleted safely. Error code: `IIA-ALT-DEL-409-002`  - `HTTP Status Code 500`, when an unexpected error occurs while deleting the alert definition. Error code: `IIA-ALT-DEL-500-001` 
     *
     * @param alertId Alert identifier.
     * @return Alert deleted successfully.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return Unexpected error.
     */
    @DELETE
    @Path("/alerts/{alertId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an alert definition", notes = "Deletes or logically removes an alert definition.  Implementations may perform a soft delete to preserve audit history and suggestion relationships. Deleting an alert must disable runtime execution and stop future suggestion generation.  ## Additional notes  - Implementations may perform a soft delete to preserve audit history and suggestion relationships.  - Deleting an alert must disable runtime execution and stop future suggestion generation.  - Existing suggestions generated by the deleted alert must remain available for audit and historical review.  - A deleted alert must not be enabled, verified or executed again unless the implementation explicitly supports restore semantics.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-DEL-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-DEL-400-002`  - `HTTP Status Code 401`, when the request is not authenticated. Error code: `IIA-ALT-DEL-401-001`  - `HTTP Status Code 403`, when the authenticated user is not allowed to delete alert definitions. Error code: `IIA-ALT-DEL-403-001`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-DEL-404-001`  - `HTTP Status Code 409`, when the alert is already deleted. Error code: `IIA-ALT-DEL-409-001`  - `HTTP Status Code 409`, when the alert is currently verifying or deploying and cannot be deleted safely. Error code: `IIA-ALT-DEL-409-002`  - `HTTP Status Code 500`, when an unexpected error occurs while deleting the alert definition. Error code: `IIA-ALT-DEL-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Alert deleted successfully.", response = Void.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    void deleteAlert(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId);


    /**
     * Disables an Agent definition and prevents new Agent Runs from being created.  If `stopRunningAgents` is true, active Agent Runs are gracefully stopped through the Agent Orchestrator. If they do not stop within the configured grace period, they can be killed according to backend policy.  ## Errors  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-DIS-404-001`  - `HTTP Status Code 409`, when the Agent definition cannot be disabled because a conflicting lifecycle transition is already in progress. Error code: `IIA-AGD-DIS-409-001`  - `HTTP Status Code 503`, when an `ACTIVE` Agent Definition cannot be disabled because the Agent Orchestrator or a mandatory runtime subsystem is unavailable. Error code: `IIA-AGD-DIS-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while disabling the Agent definition. Error code: `IIA-AGD-DIS-500-001` 
     *
     * @param agentDefinitionId Agent definition identifier.
     * @param agentDisableRequest 
     * @return Agent definition disabled successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @POST
    @Path("/agent-definitions/{agentDefinitionId}/disable")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Disable an Agent definition", notes = "Disables an Agent definition and prevents new Agent Runs from being created.  If `stopRunningAgents` is true, active Agent Runs are gracefully stopped through the Agent Orchestrator. If they do not stop within the configured grace period, they can be killed according to backend policy.  ## Errors  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-DIS-404-001`  - `HTTP Status Code 409`, when the Agent definition cannot be disabled because a conflicting lifecycle transition is already in progress. Error code: `IIA-AGD-DIS-409-001`  - `HTTP Status Code 503`, when an `ACTIVE` Agent Definition cannot be disabled because the Agent Orchestrator or a mandatory runtime subsystem is unavailable. Error code: `IIA-AGD-DIS-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while disabling the Agent definition. Error code: `IIA-AGD-DIS-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent definition disabled successfully.", response = AgentDefinitionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 503, message = "A downstream system, tool or LLM provider is not available.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentDefinitionDetail disableAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) @ApiParam("Agent definition identifier.") String agentDefinitionId,@Valid AgentDisableRequest agentDisableRequest);


    /**
     * Disables runtime execution for an alert without deleting its configuration or historical suggestions.  ## Additional notes  - This operation disables runtime execution for an alert without deleting its configuration.  - Disabling an alert stops future suggestion generation but does not modify existing suggestions.  - Disabled alerts remain available for reading, editing, verification and future re-enabling when allowed.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-DIS-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-DIS-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AlertDisableRequest`. Error code: `IIA-ALT-DIS-400-003`  - `HTTP Status Code 400`, when the `operatorNote` field exceeds 1000 characters. Error code: `IIA-ALT-DIS-400-004`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-DIS-404-001`  - `HTTP Status Code 409`, when the alert is deleted and cannot be disabled. Error code: `IIA-ALT-DIS-409-001`  - `HTTP Status Code 409`, when the alert is already disabled. Error code: `IIA-ALT-DIS-409-002`  - `HTTP Status Code 409`, when the alert is currently verifying or deploying and cannot be disabled safely. Error code: `IIA-ALT-DIS-409-003`  - `HTTP Status Code 500`, when an unexpected error occurs while disabling the alert. Error code: `IIA-ALT-DIS-500-001`
     *
     * @param alertId Alert identifier.
     * @return Alert disabled successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return Unexpected error.
     */
    @PATCH
    @Path("/alerts/{alertId}/disable")
    @Produces({ "application/json" })
    @ApiOperation(value = "Disable an alert", notes = "Disables runtime execution for an alert without deleting its configuration or historical suggestions.  ## Additional notes  - This operation disables runtime execution for an alert without deleting its configuration.  - Disabling an alert stops future suggestion generation but does not modify existing suggestions.  - Disabled alerts remain available for reading, editing, verification and future re-enabling when allowed.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-DIS-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-DIS-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AlertDisableRequest`. Error code: `IIA-ALT-DIS-400-003`  - `HTTP Status Code 400`, when the `operatorNote` field exceeds 1000 characters. Error code: `IIA-ALT-DIS-400-004`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-DIS-404-001`  - `HTTP Status Code 409`, when the alert is deleted and cannot be disabled. Error code: `IIA-ALT-DIS-409-001`  - `HTTP Status Code 409`, when the alert is already disabled. Error code: `IIA-ALT-DIS-409-002`  - `HTTP Status Code 409`, when the alert is currently verifying or deploying and cannot be disabled safely. Error code: `IIA-ALT-DIS-409-003`  - `HTTP Status Code 500`, when an unexpected error occurs while disabling the alert. Error code: `IIA-ALT-DIS-500-001` ", authorizations = {

            @Authorization(value = "bearerAuth")
    }, tags={ "Alerts" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Alert disabled successfully.", response = AlertDetail.class),
            @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
            @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
            @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
            @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AlertDetail disableAlert(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId);


    /**
     * Enables runtime execution for a verified alert.  Allowed source status: `VERIFIED`. The alert must have `verification.status = VERIFIED`, a valid controlled interpreter and `enabled = false`. This operation does not change the alert lifecycle status. On success, the alert remains in status `VERIFIED` and only `enabled` becomes `true`.   ## Additional notes  - This operation enables runtime execution for a verified alert.  - Allowed source status is `VERIFIED.  - Alerts with status `DRAFT`, `VERIFYING`, `REJECTED`, `ERROR` or `DELETED` cannot be enabled.  - Enabling an alert must not regenerate historical suggestions.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-ENA-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-ENA-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AlertEnableRequest`. Error code: `IIA-ALT-ENA-400-003`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-ENA-404-001`  - `HTTP Status Code 409`, when the alert status is not `VERIFIED`. Error code: `IIA-ALT-ENA-409-001`  - `HTTP Status Code 409`, when the alert has no valid verified interpreter. Error code: `IIA-ALT-ENA-409-002`  - `HTTP Status Code 409`, when the alert is already enabled. Error code: `IIA-ALT-ENA-409-003`  - `HTTP Status Code 500`, when an unexpected error occurs while enabling the alert. Error code: `IIA-ALT-ENA-500-001`
     *
     * @param alertId Alert identifier.
     * @return Alert enabled successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return Unexpected error.
     */
    @PATCH
    @Path("/alerts/{alertId}/enable")
    @Produces({ "application/json" })
    @ApiOperation(value = "Enable a verified alert", notes = "Enables runtime execution for a verified alert.  Allowed source status: `VERIFIED`. The alert must have `verification.status = VERIFIED`, a valid controlled interpreter and `enabled = false`. This operation does not change the alert lifecycle status. On success, the alert remains in status `VERIFIED` and only `enabled` becomes `true`.   ## Additional notes  - This operation enables runtime execution for a verified alert.  - Allowed source status is `VERIFIED.  - Alerts with status `DRAFT`, `VERIFYING`, `REJECTED`, `ERROR` or `DELETED` cannot be enabled.  - Enabling an alert must not regenerate historical suggestions.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-ENA-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-ENA-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AlertEnableRequest`. Error code: `IIA-ALT-ENA-400-003`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-ENA-404-001`  - `HTTP Status Code 409`, when the alert status is not `VERIFIED`. Error code: `IIA-ALT-ENA-409-001`  - `HTTP Status Code 409`, when the alert has no valid verified interpreter. Error code: `IIA-ALT-ENA-409-002`  - `HTTP Status Code 409`, when the alert is already enabled. Error code: `IIA-ALT-ENA-409-003`  - `HTTP Status Code 500`, when an unexpected error occurs while enabling the alert. Error code: `IIA-ALT-ENA-500-001` ", authorizations = {

            @Authorization(value = "bearerAuth")
    }, tags={ "Alerts" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Alert enabled successfully.", response = AlertDetail.class),
            @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
            @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
            @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
            @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AlertDetail enableAlert(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId);


    /**
     * Returns the latest compilation status for an Agent definition.  The UI uses this endpoint while the Agent Compiler runs asynchronous generation, validation, testing and signing steps.  ## Errors  - `HTTP Status Code 404`, when no Agent definition or compilation state is found for the given identifier. Error code: `IIA-AGD-CMS-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent compilation status. Error code: `IIA-AGD-CMS-500-001` 
     *
     * @param agentDefinitionId Agent definition identifier.
     * @return Agent compilation status returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-definitions/{agentDefinitionId}/compilation")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent compilation status", notes = "Returns the latest compilation status for an Agent definition.  The UI uses this endpoint while the Agent Compiler runs asynchronous generation, validation, testing and signing steps.  ## Errors  - `HTTP Status Code 404`, when no Agent definition or compilation state is found for the given identifier. Error code: `IIA-AGD-CMS-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent compilation status. Error code: `IIA-AGD-CMS-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent compilation status returned successfully.", response = AgentCompilationStatusResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentCompilationStatusResponse getAgentCompilationStatus(@PathParam("agentDefinitionId") @Size(max=50) @ApiParam("Agent definition identifier.") String agentDefinitionId);


    /**
     * Returns complete Agent definition information, including originating Alert reference, activation policy, compilation status, artifact metadata and latest runtime summary.  The response does not expose raw generated source code. It exposes artifact metadata such as type, hash, signature state and implementation summary.  ## Errors  - `HTTP Status Code 400`, when the `agentDefinitionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGD-GET-400-001`  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the Agent definition. Error code: `IIA-AGD-GET-500-001` 
     *
     * @param agentDefinitionId Agent definition identifier.
     * @return Agent definition returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-definitions/{agentDefinitionId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent definition detail", notes = "Returns complete Agent definition information, including originating Alert reference, activation policy, compilation status, artifact metadata and latest runtime summary.  The response does not expose raw generated source code. It exposes artifact metadata such as type, hash, signature state and implementation summary.  ## Errors  - `HTTP Status Code 400`, when the `agentDefinitionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGD-GET-400-001`  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the Agent definition. Error code: `IIA-AGD-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent definition returned successfully.", response = AgentDefinitionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentDefinitionDetail getAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) @ApiParam("Agent definition identifier.") String agentDefinitionId);


    /**
     * Returns the complete Agent Output detail.  The response includes raw normalized output fields, validation status, policy status, deduplication key, linked Suggestion and validation errors. It is intended for diagnostics and for explaining why an Agent produced or did not produce an operator-facing Suggestion.  ## Errors  - `HTTP Status Code 400`, when the `agentOutputId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGO-GET-400-001`  - `HTTP Status Code 404`, when no Agent output with the given identifier is found. Error code: `IIA-AGO-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent output detail. Error code: `IIA-AGO-GET-500-001` 
     *
     * @param agentOutputId Agent output identifier.
     * @return Agent output returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-outputs/{agentOutputId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent output detail", notes = "Returns the complete Agent Output detail.  The response includes raw normalized output fields, validation status, policy status, deduplication key, linked Suggestion and validation errors. It is intended for diagnostics and for explaining why an Agent produced or did not produce an operator-facing Suggestion.  ## Errors  - `HTTP Status Code 400`, when the `agentOutputId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGO-GET-400-001`  - `HTTP Status Code 404`, when no Agent output with the given identifier is found. Error code: `IIA-AGO-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent output detail. Error code: `IIA-AGO-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Outputs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent output returned successfully.", response = AgentOutputDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentOutputDetail getAgentOutput(@PathParam("agentOutputId") @Size(max=50) @ApiParam("Agent output identifier.") String agentOutputId);


    /**
     * Returns the complete definition of a single Agent profile.  The response includes human-readable limits and the technical resource mapping used by the Agent Orchestrator when creating Kubernetes workloads.  ## Errors  - `HTTP Status Code 400`, when the `agentProfileId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGP-GET-400-001`  - `HTTP Status Code 404`, when no Agent profile with the given identifier is found. Error code: `IIA-AGP-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the Agent profile. Error code: `IIA-AGP-GET-500-001` 
     *
     * @param agentProfileId Agent profile identifier.
     * @return Agent profile returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-profiles/{agentProfileId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent profile detail", notes = "Returns the complete definition of a single Agent profile.  The response includes human-readable limits and the technical resource mapping used by the Agent Orchestrator when creating Kubernetes workloads.  ## Errors  - `HTTP Status Code 400`, when the `agentProfileId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGP-GET-400-001`  - `HTTP Status Code 404`, when no Agent profile with the given identifier is found. Error code: `IIA-AGP-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the Agent profile. Error code: `IIA-AGP-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Profiles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent profile returned successfully.", response = AgentProfile.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentProfile getAgentProfile(@PathParam("agentProfileId") @Size(max=50) @ApiParam("Agent profile identifier.") String agentProfileId);


    /**
     * Returns the complete Agent Run monitor view.  The response includes lifecycle state, Kubernetes reference, resource usage, heartbeat, runtime metrics, functional counters, quality indicators, latest runtime events and links to outputs and suggestions.  ## Errors  - `HTTP Status Code 400`, when the `agentRunId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGR-GET-400-001`  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent run detail. Error code: `IIA-AGR-GET-500-001` 
     *
     * @param agentRunId Agent run identifier.
     * @return Agent run returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-runs/{agentRunId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent run detail", notes = "Returns the complete Agent Run monitor view.  The response includes lifecycle state, Kubernetes reference, resource usage, heartbeat, runtime metrics, functional counters, quality indicators, latest runtime events and links to outputs and suggestions.  ## Errors  - `HTTP Status Code 400`, when the `agentRunId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AGR-GET-400-001`  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent run detail. Error code: `IIA-AGR-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent run returned successfully.", response = AgentRunDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentRunDetail getAgentRun(@PathParam("agentRunId") @Size(max=50) @ApiParam("Agent run identifier.") String agentRunId);


    /**
     * Returns the runtime event timeline for one Agent Run.  Runtime events include start, ready, heartbeat, artifact loading, degradation, recovery, output generation, tool errors, resource warnings, stop, kill and failure events.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-EVT-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent runtime events. Error code: `IIA-AGR-EVT-500-001` 
     *
     * @param agentRunId Agent run identifier.
     * @param severity Optional exact filter by event severity.
     * @param eventType Optional exact filter by runtime event type.
     * @return Agent runtime events returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-runs/{agentRunId}/events")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent runtime events", notes = "Returns the runtime event timeline for one Agent Run.  Runtime events include start, ready, heartbeat, artifact loading, degradation, recovery, output generation, tool errors, resource warnings, stop, kill and failure events.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-EVT-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent runtime events. Error code: `IIA-AGR-EVT-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent runtime events returned successfully.", response = AgentRuntimeEventListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentRuntimeEventListResponse getAgentRunEvents(@PathParam("agentRunId") @Size(max=50) @ApiParam("Agent run identifier.") String agentRunId,@QueryParam("severity")  @ApiParam("Optional exact filter by event severity.")  AgentRuntimeEventSeverity severity,@QueryParam("eventType")  @ApiParam("Optional exact filter by runtime event type.")  AgentRuntimeEventType eventType);


    /**
     * Returns the latest application log lines or log references for one Agent Run.  The endpoint is intended for troubleshooting from the Agent Monitor detail view. Implementations can return stored log excerpts, links to a centralized log platform, or both. Logs must not expose secrets, raw credentials, bearer tokens or uncontrolled payload dumps.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-LOG-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent logs. Error code: `IIA-AGR-LOG-500-001` 
     *
     * @param agentRunId Agent run identifier.
     * @param limit Maximum number of log entries.
     * @return Agent run logs returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-runs/{agentRunId}/logs")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent run logs", notes = "Returns the latest application log lines or log references for one Agent Run.  The endpoint is intended for troubleshooting from the Agent Monitor detail view. Implementations can return stored log excerpts, links to a centralized log platform, or both. Logs must not expose secrets, raw credentials, bearer tokens or uncontrolled payload dumps.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-LOG-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent logs. Error code: `IIA-AGR-LOG-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent run logs returned successfully.", response = AgentRunLogListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentRunLogListResponse getAgentRunLogs(@PathParam("agentRunId") @Size(max=50) @ApiParam("Agent run identifier.") String agentRunId,@QueryParam("limit") @Min(1) @Max(500) @DefaultValue("100")  @ApiParam("Maximum number of log entries.")  Integer limit);


    /**
     * Returns historical metric snapshots for one Agent Run.  The endpoint is used by the Agent Monitor charts and can return CPU, memory, network, processed events, generated outputs, invalid outputs and error counters.  ## Errors  - `HTTP Status Code 400`, when the date range or granularity is not valid. Error code: `IIA-AGR-MET-400-001`  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-MET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent metrics. Error code: `IIA-AGR-MET-500-001` 
     *
     * @param agentRunId Agent run identifier.
     * @param from Optional lower bound for sample time.
     * @param to Optional upper bound for sample time.
     * @param granularity Optional aggregation granularity.
     * @return Agent run metrics returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-runs/{agentRunId}/metrics")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Agent run metric snapshots", notes = "Returns historical metric snapshots for one Agent Run.  The endpoint is used by the Agent Monitor charts and can return CPU, memory, network, processed events, generated outputs, invalid outputs and error counters.  ## Errors  - `HTTP Status Code 400`, when the date range or granularity is not valid. Error code: `IIA-AGR-MET-400-001`  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-MET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent metrics. Error code: `IIA-AGR-MET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent run metrics returned successfully.", response = AgentMetricSnapshotListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentMetricSnapshotListResponse getAgentRunMetrics(@PathParam("agentRunId") @Size(max=50) @ApiParam("Agent run identifier.") String agentRunId,@QueryParam("from")  @ApiParam("Optional lower bound for sample time.")  OffsetDateTime from,@QueryParam("to")  @ApiParam("Optional upper bound for sample time.")  OffsetDateTime to,@QueryParam("granularity") @DefaultValue("RAW")  @ApiParam("Optional aggregation granularity.")  String granularity);


    /**
     * Returns Agent Outputs generated by a specific Agent Run.  Agent Outputs are technical candidates. Some become Suggestions, some are deduplicated, some are rejected by validation or policy, and some fail processing. This endpoint explains what happened before the operator-facing Suggestion was created.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-OUT-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent outputs. Error code: `IIA-AGR-OUT-500-001` 
     *
     * @param agentRunId Agent run identifier.
     * @param status Optional exact filter by Agent output status.
     * @return Agent outputs returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-runs/{agentRunId}/outputs")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get outputs produced by an Agent run", notes = "Returns Agent Outputs generated by a specific Agent Run.  Agent Outputs are technical candidates. Some become Suggestions, some are deduplicated, some are rejected by validation or policy, and some fail processing. This endpoint explains what happened before the operator-facing Suggestion was created.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-OUT-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading Agent outputs. Error code: `IIA-AGR-OUT-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs", "Agent Outputs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent outputs returned successfully.", response = AgentOutputListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentOutputListResponse getAgentRunOutputs(@PathParam("agentRunId") @Size(max=50) @ApiParam("Agent run identifier.") String agentRunId,@QueryParam("status")  @ApiParam("Optional exact filter by Agent output status.")  AgentOutputStatus status);


    /**
     * Returns complete alert metadata, verification result and runtime configuration.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-GET-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-GET-400-002`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the alert definition. Error code: `IIA-ALT-GET-500-001` 
     *
     * @param alertId Alert identifier.
     * @return Alert detail returned successfully.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Unexpected error.
     */
    @GET
    @Path("/alerts/{alertId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get alert detail", notes = "Returns complete alert metadata, verification result and runtime configuration.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-GET-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-GET-400-002`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the alert definition. Error code: `IIA-ALT-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Alert detail returned successfully.", response = AlertDetail.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AlertDetail getAlert(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId);


    /**
     * Returns an assistant session with recent messages and the last known conversational context.  ## Additional notes  - This operation returns an assistant session with recent messages and the last known conversational context.  - The returned messages can include user, assistant, system and tool messages depending on what the implementation stores and exposes.  - Tool messages and structured content must be sanitized before being returned to the UI.  - Closed or errored sessions can still be returned for history and audit if they are retained by the implementation.  ## Errors  - `HTTP Status Code 400`, when the `sessionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AST-GET-400-001`  - `HTTP Status Code 400`, when the `sessionId` path parameter exceeds 50 characters. Error code: `IIA-AST-GET-400-002`  - `HTTP Status Code 404`, when no assistant session with the given `sessionId` is found. Error code: `IIA-AST-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the assistant session. Error code: `IIA-AST-GET-500-001` 
     *
     * @param sessionId Assistant session identifier.
     * @return Session returned successfully.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Unexpected error.
     */
    @GET
    @Path("/assistant/sessions/{sessionId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get assistant session", notes = "Returns an assistant session with recent messages and the last known conversational context.  ## Additional notes  - This operation returns an assistant session with recent messages and the last known conversational context.  - The returned messages can include user, assistant, system and tool messages depending on what the implementation stores and exposes.  - Tool messages and structured content must be sanitized before being returned to the UI.  - Closed or errored sessions can still be returned for history and audit if they are retained by the implementation.  ## Errors  - `HTTP Status Code 400`, when the `sessionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-AST-GET-400-001`  - `HTTP Status Code 400`, when the `sessionId` path parameter exceeds 50 characters. Error code: `IIA-AST-GET-400-002`  - `HTTP Status Code 404`, when no assistant session with the given `sessionId` is found. Error code: `IIA-AST-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the assistant session. Error code: `IIA-AST-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Assistant" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Session returned successfully.", response = AssistantSession.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AssistantSession getAssistantSession(@PathParam("sessionId") @Size(max=50) @ApiParam("Assistant session identifier.") String sessionId);


    /**
     * Returns sanitized tool execution traces recorded while answering an operator question.  ## Additional notes  - This operation returns sanitized tool execution traces recorded while answering an operator question.  - The endpoint is intended for diagnostics, audit and troubleshooting.  - Input and output payloads must be sanitized and compacted before being returned.  - Sensitive data, credentials, raw prompts, raw model responses and internal stack traces must not be exposed.  - The response can be empty when the question did not require tool execution or when traces were not retained.  - Compact tool execution summaries can also be returned directly by `POST /v1/assistant/questions` when `includeToolExecutions = true`.  ## Errors  - `HTTP Status Code 400`, when the `questionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-DIA-GET-400-001`  - `HTTP Status Code 404`, when no assistant question with the given `questionId` is found. Error code: `IIA-DIA-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading tool execution traces. Error code: `IIA-DIA-GET-500-001` 
     *
     * @param questionId Assistant question identifier.
     * @return Tool executions returned successfully.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Unexpected error.
     */
    @GET
    @Path("/assistant/questions/{questionId}/tool-executions")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get tool execution traces for a question", notes = "Returns sanitized tool execution traces recorded while answering an operator question.  ## Additional notes  - This operation returns sanitized tool execution traces recorded while answering an operator question.  - The endpoint is intended for diagnostics, audit and troubleshooting.  - Input and output payloads must be sanitized and compacted before being returned.  - Sensitive data, credentials, raw prompts, raw model responses and internal stack traces must not be exposed.  - The response can be empty when the question did not require tool execution or when traces were not retained.  - Compact tool execution summaries can also be returned directly by `POST /v1/assistant/questions` when `includeToolExecutions = true`.  ## Errors  - `HTTP Status Code 400`, when the `questionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-DIA-GET-400-001`  - `HTTP Status Code 404`, when no assistant question with the given `questionId` is found. Error code: `IIA-DIA-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading tool execution traces. Error code: `IIA-DIA-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Diagnostics" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Tool executions returned successfully.", response = ToolExecutionListResponse.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    ToolExecutionListResponse getQuestionToolExecutions(@PathParam("questionId") @Size(max=50) @ApiParam("Assistant question identifier.") String questionId);


    /**
     * Returns the complete detail of a suggestion.  The backend must not regenerate the suggestion during a simple GET. It returns the persisted target, reason, operator advice, passenger message proposal, edit flags, operator action data and compact diagnostic metadata.  ## Additional notes  - This operation returns the persisted suggestion detail and must not regenerate the suggestion.  - The response includes target details, source metadata, generated texts, operator edits, operator action data and compact diagnostic context.  - The `context` field is intended for diagnostics and troubleshooting, not for primary UI layout.  - The original generated texts are returned in `generatedOperatorAdvice` and `generatedPassengerMessage` when available.  - The editable current texts are returned in `operatorAdvice` and `passengerMessage`.  ## Errors  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-GET-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-GET-400-002`  - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the suggestion. Error code: `IIA-SUG-GET-500-001` 
     *
     * @param suggestionId Suggestion identifier.
     * @return Suggestion detail returned successfully.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Unexpected error.
     */
    @GET
    @Path("/suggestions/{suggestionId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get suggestion detail", notes = "Returns the complete detail of a suggestion.  The backend must not regenerate the suggestion during a simple GET. It returns the persisted target, reason, operator advice, passenger message proposal, edit flags, operator action data and compact diagnostic metadata.  ## Additional notes  - This operation returns the persisted suggestion detail and must not regenerate the suggestion.  - The response includes target details, source metadata, generated texts, operator edits, operator action data and compact diagnostic context.  - The `context` field is intended for diagnostics and troubleshooting, not for primary UI layout.  - The original generated texts are returned in `generatedOperatorAdvice` and `generatedPassengerMessage` when available.  - The editable current texts are returned in `operatorAdvice` and `passengerMessage`.  ## Errors  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-GET-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-GET-400-002`  - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-GET-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the suggestion. Error code: `IIA-SUG-GET-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion detail returned successfully.", response = SuggestionDetail.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    SuggestionDetail getSuggestionDetail(@PathParam("suggestionId") @Size(max=50) @ApiParam("Suggestion identifier.") String suggestionId);


    /**
     * Terminates an Agent Run through a governed lifecycle transition.  Kill is not modeled as a blind Kubernetes delete. The backend stores the operator request, asks the Agent Orchestrator to stop the runtime, waits for the configured grace period and records the final state.  ```text RUNNING Agent Run     -> kill requested     -> status STOPPING     -> graceful stop command     -> wait grace period     -> force delete if necessary     -> status KILLED or FAILED     -> runtime event and audit trace ```  ## Errors  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AgentRunKillRequest`. Error code: `IIA-AGR-KIL-400-001`  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-KIL-404-001`  - `HTTP Status Code 409`, when the Agent run is already stopped, failed, killed or expired. Error code: `IIA-AGR-KIL-409-001`  - `HTTP Status Code 503`, when the Agent Orchestrator or Kubernetes integration is not available. Error code: `IIA-AGR-KIL-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while killing the Agent run. Error code: `IIA-AGR-KIL-500-001` 
     *
     * @param agentRunId Agent run identifier.
     * @param agentRunKillRequest 
     * @return Agent run kill requested successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @POST
    @Path("/agent-runs/{agentRunId}/kill")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Kill an Agent run", notes = "Terminates an Agent Run through a governed lifecycle transition.  Kill is not modeled as a blind Kubernetes delete. The backend stores the operator request, asks the Agent Orchestrator to stop the runtime, waits for the configured grace period and records the final state.  ```text RUNNING Agent Run     -> kill requested     -> status STOPPING     -> graceful stop command     -> wait grace period     -> force delete if necessary     -> status KILLED or FAILED     -> runtime event and audit trace ```  ## Errors  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AgentRunKillRequest`. Error code: `IIA-AGR-KIL-400-001`  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-KIL-404-001`  - `HTTP Status Code 409`, when the Agent run is already stopped, failed, killed or expired. Error code: `IIA-AGR-KIL-409-001`  - `HTTP Status Code 503`, when the Agent Orchestrator or Kubernetes integration is not available. Error code: `IIA-AGR-KIL-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while killing the Agent run. Error code: `IIA-AGR-KIL-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent run kill requested successfully.", response = AgentRunDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentRunDetail killAgentRun(@PathParam("agentRunId") @Size(max=50) @ApiParam("Agent run identifier.") String agentRunId,@Valid AgentRunKillRequest agentRunKillRequest);


    /**
     * Returns the predefined Agent resource profiles available to the operator.  Agent profiles are not free-form CPU/RAM inputs. They are controlled platform presets used to map business-level choices to Kubernetes resource requests, limits and runtime policies.  Typical values are `SMALL`, `MEDIUM`, `LARGE` and `CRITICAL`, but the backend is the source of truth.  ## Errors  - `HTTP Status Code 500`, when an unexpected error occurs while listing Agent profiles. Error code: `IIA-AGP-SEA-500-001` 
     *
     * @return Agent profiles returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-profiles")
    @Produces({ "application/json" })
    @ApiOperation(value = "List Agent profiles", notes = "Returns the predefined Agent resource profiles available to the operator.  Agent profiles are not free-form CPU/RAM inputs. They are controlled platform presets used to map business-level choices to Kubernetes resource requests, limits and runtime policies.  Typical values are `SMALL`, `MEDIUM`, `LARGE` and `CRITICAL`, but the backend is the source of truth.  ## Errors  - `HTTP Status Code 500`, when an unexpected error occurs while listing Agent profiles. Error code: `IIA-AGP-SEA-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Profiles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent profiles returned successfully.", response = AgentProfileListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentProfileListResponse listAgentProfiles();


    /**
     * Updates mutable metadata of an Agent definition.  Only non-runtime fields can be changed. The operation can update name, description, activation policy or profile while the definition is not active. Changes that affect generation semantics require a new Agent definition instead of mutating the existing one.  An active Agent definition cannot be patched in a way that changes runtime behavior. Disable it first or create a new definition.  ## Errors  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AgentDefinitionPatchRequest`. Error code: `IIA-AGD-PAT-400-001`  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-PAT-404-001`  - `HTTP Status Code 409`, when the Agent definition is active and the patch would change runtime behavior. Error code: `IIA-AGD-PAT-409-001`  - `HTTP Status Code 500`, when an unexpected error occurs while patching the Agent definition. Error code: `IIA-AGD-PAT-500-001` 
     *
     * @param agentDefinitionId Agent definition identifier.
     * @param agentDefinitionPatchRequest 
     * @return Agent definition updated successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @PATCH
    @Path("/agent-definitions/{agentDefinitionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Patch an Agent definition", notes = "Updates mutable metadata of an Agent definition.  Only non-runtime fields can be changed. The operation can update name, description, activation policy or profile while the definition is not active. Changes that affect generation semantics require a new Agent definition instead of mutating the existing one.  An active Agent definition cannot be patched in a way that changes runtime behavior. Disable it first or create a new definition.  ## Errors  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `AgentDefinitionPatchRequest`. Error code: `IIA-AGD-PAT-400-001`  - `HTTP Status Code 404`, when no Agent definition with the given identifier is found. Error code: `IIA-AGD-PAT-404-001`  - `HTTP Status Code 409`, when the Agent definition is active and the patch would change runtime behavior. Error code: `IIA-AGD-PAT-409-001`  - `HTTP Status Code 500`, when an unexpected error occurs while patching the Agent definition. Error code: `IIA-AGD-PAT-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent definition updated successfully.", response = AgentDefinitionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentDefinitionDetail patchAgentDefinition(@PathParam("agentDefinitionId") @Size(max=50) @ApiParam("Agent definition identifier.") String agentDefinitionId,@Valid @NotNull AgentDefinitionPatchRequest agentDefinitionPatchRequest);


    /**
     * Updates an existing Alert definition.  The request body has the same business shape as `AlertCreateRequest`: `name`, `description`, `prompt`, `verifyImmediately` and `enableAfterVerification`.  ## Update behaviour  The backend must compare the incoming `prompt` with the currently persisted prompt.  - If the prompt is unchanged, the operation updates only mutable descriptive   fields such as `name` and `description`.   No AI-assisted verification is started.   Existing lifecycle status, verification result, interpreter metadata,   technical specification, agent blueprint preview and enabled flag are preserved.   In this case `verifyImmediately` and `enableAfterVerification` are ignored.  - If the prompt changes and `verifyImmediately = false` or is omitted, the   Alert is updated and reset to `DRAFT`.   The verification status becomes `PENDING`.   The Alert is disabled.   Previous interpreter metadata, interpreted target types, interpreted event names,   technical specification, agent blueprint preview, warnings and safety checks   must be cleared because they refer to the previous prompt.   A new explicit verification can be started later through   `POST /v1/alerts/{alertId}/verify`.  - If the prompt changes and `verifyImmediately = true`, the Alert is updated   and reset to `VERIFYING`.   The verification status becomes `PENDING`.   The Alert is disabled while the new verification is running.   The backend starts AI-assisted verification using the same governed verification   pipeline used by `POST /v1/alerts` with `verifyImmediately = true`.   If the implementation is asynchronous, this endpoint can return immediately   with status `VERIFYING`; the UI must poll `GET /v1/alerts/{alertId}` to read   the final result.  - `enableAfterVerification` is accepted for contract symmetry with   `AlertCreateRequest`, but during update it must not be used to enable an   Alert that was disabled before the prompt change.   If the Alert was enabled before the update, the backend may restore   `enabled = true` only after the new verification completes with status   `VERIFIED`.   If the previous Alert was not enabled, the final Alert must remain disabled   even when verification succeeds.  - A verified interpreter must never remain active after its source prompt has   changed without successful re-verification.  - Updating an Alert must not create Agent Definitions, Agent Runs, Agent Outputs   or Suggestions.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains   only whitespace characters. Error code: `IIA-ALT-UPD-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters.   Error code: `IIA-ALT-UPD-400-002`  - `HTTP Status Code 400`, when the request body is missing.   Error code: `IIA-ALT-UPD-400-003`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization   of schema `AlertUpdateRequest`. Error code: `IIA-ALT-UPD-400-004`  - `HTTP Status Code 400`, when the `name` field is missing, empty or contains   only whitespace characters. Error code: `IIA-ALT-UPD-400-005`  - `HTTP Status Code 400`, when the `name` field exceeds 120 characters.   Error code: `IIA-ALT-UPD-400-006`  - `HTTP Status Code 400`, when the `description` field exceeds 1000 characters.   Error code: `IIA-ALT-UPD-400-007`  - `HTTP Status Code 400`, when the `prompt` field is missing, empty, contains   fewer than 10 characters or exceeds 8000 characters.   Error code: `IIA-ALT-UPD-400-008`  - `HTTP Status Code 400`, when `verifyImmediately` is not a boolean value.   Error code: `IIA-ALT-UPD-400-009`  - `HTTP Status Code 400`, when `enableAfterVerification` is not a boolean value.   Error code: `IIA-ALT-UPD-400-010`  - `HTTP Status Code 404`, when no Alert with the given `alertId` is found.   Error code: `IIA-ALT-UPD-404-001`  - `HTTP Status Code 409`, when the Alert is deleted and cannot be updated.   Error code: `IIA-ALT-UPD-409-001`  - `HTTP Status Code 409`, when an active Alert with the same normalized name   already exists. Error code: `IIA-ALT-UPD-409-002`  - `HTTP Status Code 409`, when the Alert is currently verifying and cannot be   updated. Error code: `IIA-ALT-UPD-409-003`  - `HTTP Status Code 500`, when an unexpected error occurs while updating the   Alert definition. Error code: `IIA-ALT-UPD-500-001`  - `HTTP Status Code 503`, when `verifyImmediately = true` and the verification   service or LLM provider is temporarily unavailable.   Error code: `IIA-ALT-UPD-503-001`
     *
     * @param alertId Alert identifier.
     * @param alertUpdateRequest
     * @return Alert updated successfully. If the prompt changed and immediate verification was requested, the returned Alert can be in `VERIFYING` while background verification completes.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return Unexpected error.
     * @return A downstream system, tool or LLM provider is not available.
     */
    @PUT
    @Path("/alerts/{alertId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update alert definition", notes = "Updates an existing Alert definition.  The request body has the same business shape as `AlertCreateRequest`: `name`, `description`, `prompt`, `verifyImmediately` and `enableAfterVerification`.  ## Update behaviour  The backend must compare the incoming `prompt` with the currently persisted prompt.  - If the prompt is unchanged, the operation updates only mutable descriptive   fields such as `name` and `description`.   No AI-assisted verification is started.   Existing lifecycle status, verification result, interpreter metadata,   technical specification, agent blueprint preview and enabled flag are preserved.   In this case `verifyImmediately` and `enableAfterVerification` are ignored.  - If the prompt changes and `verifyImmediately = false` or is omitted, the   Alert is updated and reset to `DRAFT`.   The verification status becomes `PENDING`.   The Alert is disabled.   Previous interpreter metadata, interpreted target types, interpreted event names,   technical specification, agent blueprint preview, warnings and safety checks   must be cleared because they refer to the previous prompt.   A new explicit verification can be started later through   `POST /v1/alerts/{alertId}/verify`.  - If the prompt changes and `verifyImmediately = true`, the Alert is updated   and reset to `VERIFYING`.   The verification status becomes `PENDING`.   The Alert is disabled while the new verification is running.   The backend starts AI-assisted verification using the same governed verification   pipeline used by `POST /v1/alerts` with `verifyImmediately = true`.   If the implementation is asynchronous, this endpoint can return immediately   with status `VERIFYING`; the UI must poll `GET /v1/alerts/{alertId}` to read   the final result.  - `enableAfterVerification` is accepted for contract symmetry with   `AlertCreateRequest`, but during update it must not be used to enable an   Alert that was disabled before the prompt change.   If the Alert was enabled before the update, the backend may restore   `enabled = true` only after the new verification completes with status   `VERIFIED`.   If the previous Alert was not enabled, the final Alert must remain disabled   even when verification succeeds.  - A verified interpreter must never remain active after its source prompt has   changed without successful re-verification.  - Updating an Alert must not create Agent Definitions, Agent Runs, Agent Outputs   or Suggestions.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains   only whitespace characters. Error code: `IIA-ALT-UPD-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters.   Error code: `IIA-ALT-UPD-400-002`  - `HTTP Status Code 400`, when the request body is missing.   Error code: `IIA-ALT-UPD-400-003`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization   of schema `AlertUpdateRequest`. Error code: `IIA-ALT-UPD-400-004`  - `HTTP Status Code 400`, when the `name` field is missing, empty or contains   only whitespace characters. Error code: `IIA-ALT-UPD-400-005`  - `HTTP Status Code 400`, when the `name` field exceeds 120 characters.   Error code: `IIA-ALT-UPD-400-006`  - `HTTP Status Code 400`, when the `description` field exceeds 1000 characters.   Error code: `IIA-ALT-UPD-400-007`  - `HTTP Status Code 400`, when the `prompt` field is missing, empty, contains   fewer than 10 characters or exceeds 8000 characters.   Error code: `IIA-ALT-UPD-400-008`  - `HTTP Status Code 400`, when `verifyImmediately` is not a boolean value.   Error code: `IIA-ALT-UPD-400-009`  - `HTTP Status Code 400`, when `enableAfterVerification` is not a boolean value.   Error code: `IIA-ALT-UPD-400-010`  - `HTTP Status Code 404`, when no Alert with the given `alertId` is found.   Error code: `IIA-ALT-UPD-404-001`  - `HTTP Status Code 409`, when the Alert is deleted and cannot be updated.   Error code: `IIA-ALT-UPD-409-001`  - `HTTP Status Code 409`, when an active Alert with the same normalized name   already exists. Error code: `IIA-ALT-UPD-409-002`  - `HTTP Status Code 409`, when the Alert is currently verifying and cannot be   updated. Error code: `IIA-ALT-UPD-409-003`  - `HTTP Status Code 500`, when an unexpected error occurs while updating the   Alert definition. Error code: `IIA-ALT-UPD-500-001`  - `HTTP Status Code 503`, when `verifyImmediately = true` and the verification   service or LLM provider is temporarily unavailable.   Error code: `IIA-ALT-UPD-503-001` ", authorizations = {

            @Authorization(value = "bearerAuth")
    }, tags={ "Alerts" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Alert updated successfully. If the prompt changed and immediate verification was requested, the returned Alert can be in `VERIFYING` while background verification completes. ", response = AlertDetail.class),
            @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
            @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
            @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
            @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class),
            @ApiResponse(code = 503, message = "A downstream system, tool or LLM provider is not available.", response = Error.class) })
    AlertDetail updateAlert(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId,@Valid @NotNull AlertUpdateRequest alertUpdateRequest);

    /**
     * Updates the editable operator-facing and passenger-facing texts of a suggestion while it is still under review.  Allowed source status: `TO_REVIEW`.  This operation does not approve the suggestion. It only stores the operator edits and updates the edit flags:  - `operatorAdviceEdited` is true when `operatorAdvice` is provided and differs from the generated advice. - `passengerMessageEdited` is true when `passengerMessage` is provided and differs from the generated passenger message.   ## Additional notes  - This operation only updates editable texts and does not approve or reject the suggestion.  - Text editing is allowed only when the suggestion status is `TO_REVIEW`.  - `operatorAdviceEdited` is set to `true` when `operatorAdvice` is provided and differs from the original generated advice.  - `passengerMessageEdited` is set to `true` when `passengerMessage` is provided and differs from the original generated passenger message.  - Sending `passengerMessage = null` explicitly requests removal of the passenger-facing message, if the implementation allows it.  - Omitted fields must keep their current persisted value unchanged.  - `operatorNote` can be used to store the reason for the manual edit.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-EDT-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-EDT-400-002`  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-SUG-EDT-400-003`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `SuggestionTextPatchRequest`. Error code: `IIA-SUG-EDT-400-004`  - `HTTP Status Code 400`, when none of `operatorAdvice`, `passengerMessage` or `operatorNote` is provided. Error code: `IIA-SUG-EDT-400-005`  - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-EDT-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while updating suggestion texts. Error code: `IIA-SUG-EDT-500-001` 
     *
     * @param suggestionId Suggestion identifier.
     * @param suggestionTextPatchRequest 
     * @return Suggestion texts updated successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return Unexpected error.
     */
    @PATCH
    @Path("/suggestions/{suggestionId}/texts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Edit suggestion texts", notes = "Updates the editable operator-facing and passenger-facing texts of a suggestion while it is still under review.  Allowed source status: `TO_REVIEW`.  This operation does not approve the suggestion. It only stores the operator edits and updates the edit flags:  - `operatorAdviceEdited` is true when `operatorAdvice` is provided and differs from the generated advice. - `passengerMessageEdited` is true when `passengerMessage` is provided and differs from the generated passenger message.   ## Additional notes  - This operation only updates editable texts and does not approve or reject the suggestion.  - Text editing is allowed only when the suggestion status is `TO_REVIEW`.  - `operatorAdviceEdited` is set to `true` when `operatorAdvice` is provided and differs from the original generated advice.  - `passengerMessageEdited` is set to `true` when `passengerMessage` is provided and differs from the original generated passenger message.  - Sending `passengerMessage = null` explicitly requests removal of the passenger-facing message, if the implementation allows it.  - Omitted fields must keep their current persisted value unchanged.  - `operatorNote` can be used to store the reason for the manual edit.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-EDT-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-EDT-400-002`  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-SUG-EDT-400-003`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema `SuggestionTextPatchRequest`. Error code: `IIA-SUG-EDT-400-004`  - `HTTP Status Code 400`, when none of `operatorAdvice`, `passengerMessage` or `operatorNote` is provided. Error code: `IIA-SUG-EDT-400-005`  - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-EDT-404-001`  - `HTTP Status Code 500`, when an unexpected error occurs while updating suggestion texts. Error code: `IIA-SUG-EDT-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion texts updated successfully.", response = SuggestionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    SuggestionDetail patchSuggestionTexts(@PathParam("suggestionId") @Size(max=50) @ApiParam("Suggestion identifier.") String suggestionId,@Valid @NotNull SuggestionTextPatchRequest suggestionTextPatchRequest);


    /**
     * Produces an Agent generation preview for a verified Alert without creating an Agent definition.  The operation asks the backend generation flow to transform the verified Alert interpretation into a structured Agent Blueprint preview. The preview is used by the UI to show the operator what the system would create before the Agent definition is persisted.  ```text Verified Alert     -> load latest verification result     -> build generation prompt with allowed sources, tools and DSL functions     -> generate Agent Blueprint     -> validate Blueprint     -> estimate complexity and recommended generation mode     -> return preview to UI ```  The response can include a DSL preview when the Alert can be expressed through the controlled DSL. It can also report that the Agent requires generated Java, that the request is not supported, or that clarification is required.  No Agent definition, artifact or runtime object is created by this operation.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-AGP-400-001`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AgentGenerationPreviewRequest`. Error code: `IIA-ALT-AGP-400-002`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-AGP-404-001`  - `HTTP Status Code 409`, when the alert is not verified. Error code: `IIA-ALT-AGP-409-001`  - `HTTP Status Code 422`, when the verified alert cannot be transformed into an Agent Blueprint with the available sources and tools. Error code: `IIA-ALT-AGP-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while generating the Agent preview. Error code: `IIA-ALT-AGP-500-001` 
     *
     * @param alertId Alert identifier.
     * @param agentGenerationPreviewRequest 
     * @return Agent generation preview produced successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @POST
    @Path("/alerts/{alertId}/agent-generation-preview")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Preview Agent generation for an alert", notes = "Produces an Agent generation preview for a verified Alert without creating an Agent definition.  The operation asks the backend generation flow to transform the verified Alert interpretation into a structured Agent Blueprint preview. The preview is used by the UI to show the operator what the system would create before the Agent definition is persisted.  ```text Verified Alert     -> load latest verification result     -> build generation prompt with allowed sources, tools and DSL functions     -> generate Agent Blueprint     -> validate Blueprint     -> estimate complexity and recommended generation mode     -> return preview to UI ```  The response can include a DSL preview when the Alert can be expressed through the controlled DSL. It can also report that the Agent requires generated Java, that the request is not supported, or that clarification is required.  No Agent definition, artifact or runtime object is created by this operation.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-AGP-400-001`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AgentGenerationPreviewRequest`. Error code: `IIA-ALT-AGP-400-002`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-AGP-404-001`  - `HTTP Status Code 409`, when the alert is not verified. Error code: `IIA-ALT-AGP-409-001`  - `HTTP Status Code 422`, when the verified alert cannot be transformed into an Agent Blueprint with the available sources and tools. Error code: `IIA-ALT-AGP-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while generating the Agent preview. Error code: `IIA-ALT-AGP-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent generation preview produced successfully.", response = AgentGenerationPreviewResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentGenerationPreviewResponse previewAgentGenerationForAlert(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId,@Valid AgentGenerationPreviewRequest agentGenerationPreviewRequest);


    /**
     * Rejects a suggestion currently in `TO_REVIEW` and stores an optional operator note.  Target status: `REJECTED`. Invalid workflow transitions return HTTP `409`.   ## Additional notes  - This operation rejects a suggestion currently in `TO_REVIEW`.  - Rejection stores the operator action metadata and changes the suggestion status to `REJECTED`.  - `operatorNote` can be used to explain why the suggestion was rejected.  - Rejected suggestions must remain available for audit and historical review.  - Invalid workflow transitions return HTTP Status Code `409`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-REJ-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-REJ-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `SuggestionRejectionRequest`. Error code: `IIA-SUG-REJ-400-003`   - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-REJ-404-001`  - `HTTP Status Code 409`, when the suggestion status is not `TO_REVIEW`. Error code: `IIA-SUG-REJ-409-001`  - `HTTP Status Code 409`, when the suggestion has already been approved, rejected, expired or marked as error by another operator or process. Error code: `IIA-SUG-REJ-409-002`  - `HTTP Status Code 500`, when an unexpected error occurs while rejecting the suggestion. Error code: `IIA-SUG-REJ-500-001` 
     *
     * @param suggestionId Suggestion identifier.
     * @param suggestionRejectionRequest 
     * @return Suggestion rejected successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return Unexpected error.
     */
    @POST
    @Path("/suggestions/{suggestionId}/reject")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Reject a suggestion", notes = "Rejects a suggestion currently in `TO_REVIEW` and stores an optional operator note.  Target status: `REJECTED`. Invalid workflow transitions return HTTP `409`.   ## Additional notes  - This operation rejects a suggestion currently in `TO_REVIEW`.  - Rejection stores the operator action metadata and changes the suggestion status to `REJECTED`.  - `operatorNote` can be used to explain why the suggestion was rejected.  - Rejected suggestions must remain available for audit and historical review.  - Invalid workflow transitions return HTTP Status Code `409`.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `suggestionId` path parameter is empty or contains only whitespace characters. Error code: `IIA-SUG-REJ-400-001`  - `HTTP Status Code 400`, when the `suggestionId` path parameter exceeds 50 characters. Error code: `IIA-SUG-REJ-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `SuggestionRejectionRequest`. Error code: `IIA-SUG-REJ-400-003`   - `HTTP Status Code 404`, when no suggestion with the given `suggestionId` is found. Error code: `IIA-SUG-REJ-404-001`  - `HTTP Status Code 409`, when the suggestion status is not `TO_REVIEW`. Error code: `IIA-SUG-REJ-409-001`  - `HTTP Status Code 409`, when the suggestion has already been approved, rejected, expired or marked as error by another operator or process. Error code: `IIA-SUG-REJ-409-002`  - `HTTP Status Code 500`, when an unexpected error occurs while rejecting the suggestion. Error code: `IIA-SUG-REJ-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion rejected successfully.", response = SuggestionDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    SuggestionDetail rejectSuggestion(@PathParam("suggestionId") @Size(max=50) @ApiParam("Suggestion identifier.") String suggestionId,@Valid SuggestionRejectionRequest suggestionRejectionRequest);


    /**
     * Restarts an Agent Run when the owning Agent definition is still active and its activation policy currently allows execution.  The operation creates a new runtime execution after stopping the current one. The returned resource is the new or updated Agent Run monitor detail, depending on the implementation strategy.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-RST-404-001`  - `HTTP Status Code 409`, when the owning Agent definition is disabled or the activation policy is currently inactive. Error code: `IIA-AGR-RST-409-001`  - `HTTP Status Code 503`, when the Agent Orchestrator or Kubernetes integration is not available. Error code: `IIA-AGR-RST-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while restarting the Agent run. Error code: `IIA-AGR-RST-500-001` 
     *
     * @param agentRunId Agent run identifier.
     * @param agentRunRestartRequest 
     * @return Agent run restart requested successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @POST
    @Path("/agent-runs/{agentRunId}/restart")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Restart an Agent run", notes = "Restarts an Agent Run when the owning Agent definition is still active and its activation policy currently allows execution.  The operation creates a new runtime execution after stopping the current one. The returned resource is the new or updated Agent Run monitor detail, depending on the implementation strategy.  ## Errors  - `HTTP Status Code 404`, when no Agent run with the given identifier is found. Error code: `IIA-AGR-RST-404-001`  - `HTTP Status Code 409`, when the owning Agent definition is disabled or the activation policy is currently inactive. Error code: `IIA-AGR-RST-409-001`  - `HTTP Status Code 503`, when the Agent Orchestrator or Kubernetes integration is not available. Error code: `IIA-AGR-RST-503-001`  - `HTTP Status Code 500`, when an unexpected error occurs while restarting the Agent run. Error code: `IIA-AGR-RST-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent run restart requested successfully.", response = AgentRunDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentRunDetail restartAgentRun(@PathParam("agentRunId") @Size(max=50) @ApiParam("Agent run identifier.") String agentRunId,@Valid AgentRunRestartRequest agentRunRestartRequest);


    /**
     * Returns Agent definitions created from verified Alerts.  An Agent definition is a versioned executable model. It references the Alert and Alert version used to generate it, the activation policy, the selected resource profile, the generation mode and the artifact metadata.  The endpoint is used by the Agent creation menu and by the Agent governance views. It does not return low-level runtime metrics; use Agent Run endpoints for runtime monitoring.  ## Additional notes  - `status`, `alertId`, `generationMode` and `profileId` are exact-match filters.  - `text` is a partial-match filter over Agent name, description and originating Alert name.  - Disabled and superseded Agent definitions are returned unless the UI filters them out.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `status` query parameter is not a valid value of `AgentDefinitionStatus`. Error code: `IIA-AGD-SEA-400-001`  - `HTTP Status Code 400`, when the `generationMode` query parameter is not a valid value of `AgentGenerationMode`. Error code: `IIA-AGD-SEA-400-002`  - `HTTP Status Code 400`, when the `text` query parameter exceeds 200 characters. Error code: `IIA-AGD-SEA-400-003`  - `HTTP Status Code 500`, when an unexpected error occurs while searching Agent definitions. Error code: `IIA-AGD-SEA-500-001` 
     *
     * @param status Optional exact filter by Agent definition status.
     * @param alertId Optional exact filter by originating Alert identifier.
     * @param generationMode Optional exact filter by generation mode.
     * @param profileId Optional exact filter by Agent profile identifier.
     * @param text Optional partial text filter.
     * @return Agent definitions returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-definitions")
    @Produces({ "application/json" })
    @ApiOperation(value = "Search Agent definitions", notes = "Returns Agent definitions created from verified Alerts.  An Agent definition is a versioned executable model. It references the Alert and Alert version used to generate it, the activation policy, the selected resource profile, the generation mode and the artifact metadata.  The endpoint is used by the Agent creation menu and by the Agent governance views. It does not return low-level runtime metrics; use Agent Run endpoints for runtime monitoring.  ## Additional notes  - `status`, `alertId`, `generationMode` and `profileId` are exact-match filters.  - `text` is a partial-match filter over Agent name, description and originating Alert name.  - Disabled and superseded Agent definitions are returned unless the UI filters them out.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `status` query parameter is not a valid value of `AgentDefinitionStatus`. Error code: `IIA-AGD-SEA-400-001`  - `HTTP Status Code 400`, when the `generationMode` query parameter is not a valid value of `AgentGenerationMode`. Error code: `IIA-AGD-SEA-400-002`  - `HTTP Status Code 400`, when the `text` query parameter exceeds 200 characters. Error code: `IIA-AGD-SEA-400-003`  - `HTTP Status Code 500`, when an unexpected error occurs while searching Agent definitions. Error code: `IIA-AGD-SEA-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Definitions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent definitions returned successfully.", response = AgentDefinitionListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentDefinitionListResponse searchAgentDefinitions(@QueryParam("status")  @ApiParam("Optional exact filter by Agent definition status.")  AgentDefinitionStatus status,@QueryParam("alertId") @Size(max=50)  @ApiParam("Optional exact filter by originating Alert identifier.")  String alertId,@QueryParam("generationMode")  @ApiParam("Optional exact filter by generation mode.")  AgentGenerationMode generationMode,@QueryParam("profileId") @Size(max=50)  @ApiParam("Optional exact filter by Agent profile identifier.")  String profileId,@QueryParam("text") @Size(max=200)  @ApiParam("Optional partial text filter.")  String text);


    /**
     * Returns runtime Agent executions for the Agent Monitor menu.  The result combines functional state stored by the Assistant API with the last known Kubernetes and monitoring snapshots collected by the Agent Monitor Processor.  ## Additional notes  - `activeOnly=true` returns runs in `PENDING`, `STARTING`, `RUNNING`, `IDLE`, `DEGRADED` or `STOPPING`.  - `healthStatus` filters the technical health of the run.  - `qualityStatus` filters the usefulness of outputs and suggestions produced by the run.  - This endpoint returns compact rows. Use `GET /v1/agent-runs/{agentRunId}` for the detailed monitor view.  ## Errors  - `HTTP Status Code 400`, when one or more filters are not valid. Error code: `IIA-AGR-SEA-400-001`  - `HTTP Status Code 500`, when an unexpected error occurs while searching Agent runs. Error code: `IIA-AGR-SEA-500-001` 
     *
     * @param agentDefinitionId Optional exact filter by Agent definition identifier.
     * @param alertId Optional exact filter by originating Alert identifier.
     * @param status Optional exact filter by Agent run status.
     * @param healthStatus Optional exact filter by health status.
     * @param activeOnly If true, returns only runs that are currently active or stopping.
     * @param from Optional lower bound for run start time.
     * @param to Optional upper bound for run start time.
     * @return Agent runs returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/agent-runs")
    @Produces({ "application/json" })
    @ApiOperation(value = "Search Agent runs", notes = "Returns runtime Agent executions for the Agent Monitor menu.  The result combines functional state stored by the Assistant API with the last known Kubernetes and monitoring snapshots collected by the Agent Monitor Processor.  ## Additional notes  - `activeOnly=true` returns runs in `PENDING`, `STARTING`, `RUNNING`, `IDLE`, `DEGRADED` or `STOPPING`.  - `healthStatus` filters the technical health of the run.  - `qualityStatus` filters the usefulness of outputs and suggestions produced by the run.  - This endpoint returns compact rows. Use `GET /v1/agent-runs/{agentRunId}` for the detailed monitor view.  ## Errors  - `HTTP Status Code 400`, when one or more filters are not valid. Error code: `IIA-AGR-SEA-400-001`  - `HTTP Status Code 500`, when an unexpected error occurs while searching Agent runs. Error code: `IIA-AGR-SEA-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Agent Runs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Agent runs returned successfully.", response = AgentRunListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AgentRunListResponse searchAgentRuns(@QueryParam("agentDefinitionId") @Size(max=50)  @ApiParam("Optional exact filter by Agent definition identifier.")  String agentDefinitionId,@QueryParam("alertId") @Size(max=50)  @ApiParam("Optional exact filter by originating Alert identifier.")  String alertId,@QueryParam("status")  @ApiParam("Optional exact filter by Agent run status.")  AgentRunStatus status,@QueryParam("healthStatus")  @ApiParam("Optional exact filter by health status.")  AgentHealthStatus healthStatus,@QueryParam("activeOnly")  @ApiParam("If true, returns only runs that are currently active or stopping.")  Boolean activeOnly,@QueryParam("from")  @ApiParam("Optional lower bound for run start time.")  OffsetDateTime from,@QueryParam("to")  @ApiParam("Optional upper bound for run start time.")  OffsetDateTime to);


    /**
     * Returns alert definitions configured by operators.  This endpoint is intended for the future Alerts menu. It returns configuration and runtime metadata, not suggestion instances.  ## Additional notes  - This operation returns alert definitions configured by operators, not generated suggestion instances.  - The `status`, `enabled` and `interpreterType` filters are exact-match filters.  - The `text` filter is a partial-match filter over alert name and prompt.  - Deleted alerts should not be returned unless the implementation explicitly supports an audit-oriented view.  - Runtime metadata, when present, represents the last known execution state and must not be considered a real-time execution trace.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `status` query parameter is not a valid value of `AlertStatus`. Error code: `IIA-ALT-SEA-400-001`  - `HTTP Status Code 400`, when the `enabled` query parameter is not a valid boolean value. Error code: `IIA-ALT-SEA-400-002`  - `HTTP Status Code 400`, when the `interpreterType` query parameter is not a valid value of `AlertInterpreterType`. Error code: `IIA-ALT-SEA-400-003`  - `HTTP Status Code 400`, when the `text` query parameter exceeds 200 characters. Error code: `IIA-ALT-SEA-400-004`  - `HTTP Status Code 500`, when an unexpected error occurs while searching alert definitions. Error code: `IIA-ALT-SEA-500-001` 
     *
     * @param status Optional exact filter by alert lifecycle status.
     * @param enabled Optional filter by enabled flag.
     * @param interpreterType Optional filter by interpreter type.
     * @param text Optional partial-match filter over alert name and prompt.
     * @return Alert definitions returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Unexpected error.
     */
    @GET
    @Path("/alerts")
    @Produces({ "application/json" })
    @ApiOperation(value = "Search alert definitions", notes = "Returns alert definitions configured by operators.  This endpoint is intended for the future Alerts menu. It returns configuration and runtime metadata, not suggestion instances.  ## Additional notes  - This operation returns alert definitions configured by operators, not generated suggestion instances.  - The `status`, `enabled` and `interpreterType` filters are exact-match filters.  - The `text` filter is a partial-match filter over alert name and prompt.  - Deleted alerts should not be returned unless the implementation explicitly supports an audit-oriented view.  - Runtime metadata, when present, represents the last known execution state and must not be considered a real-time execution trace.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `status` query parameter is not a valid value of `AlertStatus`. Error code: `IIA-ALT-SEA-400-001`  - `HTTP Status Code 400`, when the `enabled` query parameter is not a valid boolean value. Error code: `IIA-ALT-SEA-400-002`  - `HTTP Status Code 400`, when the `interpreterType` query parameter is not a valid value of `AlertInterpreterType`. Error code: `IIA-ALT-SEA-400-003`  - `HTTP Status Code 400`, when the `text` query parameter exceeds 200 characters. Error code: `IIA-ALT-SEA-400-004`  - `HTTP Status Code 500`, when an unexpected error occurs while searching alert definitions. Error code: `IIA-ALT-SEA-500-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Alert definitions returned successfully.", response = AlertSummaryListResponse.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AlertSummaryListResponse searchAlerts(@QueryParam("status")  @ApiParam("Optional exact filter by alert lifecycle status.")  String status,@QueryParam("enabled")  @ApiParam("Optional filter by enabled flag.")  String enabled,@QueryParam("interpreterType")  @ApiParam("Optional filter by interpreter type.")  String interpreterType,@QueryParam("text")  @ApiParam("Optional partial-match filter over alert name and prompt.")  String text);


    /**
     * Returns the suggestion list used by the suggestion dashboard.  ## Additional notes  - The `lookbackHours` parameter is mandatory and is evaluated using the backend server time as reference.  - The response includes suggestions with `generatedAt >= now - lookbackHours`.  - Results are ordered by `generatedAt DESC`.  - The `status` filter supports multiple values and can be sent as repeated query parameters.  - The `alertId`, `targetType` and `eventName` filters are exact-match filters.  - The `text` filter is a partial-match filter over searchable suggestion fields such as target title, target subtitle, reason, operator advice preview and passenger message preview.  - Suggestions generated by scheduled interpreters and not linked to a specific operational event use `eventName = AUTO`.  - The response returns compact suggestion headers only. Use `GET /v1/suggestions/{suggestionId}` to retrieve full details, diagnostic context and editable texts.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `lookbackHours` query parameter is missing. Error code: `IIA-SUG-SEA-400-001`  - `HTTP Status Code 400`, when the `lookbackHours` query parameter is lower than `1` or greater than `4800`. Error code: `IIA-SUG-SEA-400-002`  - `HTTP Status Code 400`, when one or more `status` values are not valid values of `SuggestionStatus`. Error code: `IIA-SUG-SEA-400-003`  - `HTTP Status Code 400`, when the `alertId` query parameter exceeds 50 characters. Error code: `IIA-SUG-SEA-400-004`  - `HTTP Status Code 400`, when the `targetType` query parameter is not a valid value of `SuggestionTargetType`. Error code: `IIA-SUG-SEA-400-005`  - `HTTP Status Code 400`, when the `eventName` query parameter exceeds the supported maximum length. Error code: `IIA-SUG-SEA-400-006`  - `HTTP Status Code 400`, when the `text` query parameter exceeds 200 characters. Error code: `IIA-SUG-SEA-400-007`  - `HTTP Status Code 500`, when an unexpected error occurs while searching suggestions. Error code: `IIA-SUG-SEA-500-001`   - The `agentDefinitionId` and `agentRunId` filters allow the UI to navigate from Agent Monitor to the suggestions generated by a specific digital worker.  - `HTTP Status Code 400`, when `agentDefinitionId` or `agentRunId` exceeds 50 characters. Error code: `IIA-SUG-SEA-400-008` 
     *
     * @param lookbackHours Number of hours to look back from server &#x60;now&#x60;. The response includes suggestions with &#x60;generatedAt &gt;&#x3D; now - lookbackHours&#x60;.
     * @param status Optional exact filter by suggestion status. Can be repeated by clients that support repeated query parameters.
     * @param alertId Optional exact filter by the alert that generated the suggestion.
     * @param targetType Optional exact filter by target type.
     * @param eventName Optional exact filter by operational event name. When a suggestion was generated by a scheduled interpreter and not by a specific event, the backend uses &#x60;AUTO&#x60;.
     * @param text Generic partial-match UI filter over header fields.
     * @param agentDefinitionId Optional exact filter by Agent definition that generated the suggestion through an Agent output.
     * @param agentRunId Optional exact filter by Agent run that generated the source Agent output.
     * @return Suggestion headers returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Unexpected error.
     */
    @GET
    @Path("/suggestions")
    @Produces({ "application/json" })
    @ApiOperation(value = "Search suggestion headers", notes = "Returns the suggestion list used by the suggestion dashboard.  ## Additional notes  - The `lookbackHours` parameter is mandatory and is evaluated using the backend server time as reference.  - The response includes suggestions with `generatedAt >= now - lookbackHours`.  - Results are ordered by `generatedAt DESC`.  - The `status` filter supports multiple values and can be sent as repeated query parameters.  - The `alertId`, `targetType` and `eventName` filters are exact-match filters.  - The `text` filter is a partial-match filter over searchable suggestion fields such as target title, target subtitle, reason, operator advice preview and passenger message preview.  - Suggestions generated by scheduled interpreters and not linked to a specific operational event use `eventName = AUTO`.  - The response returns compact suggestion headers only. Use `GET /v1/suggestions/{suggestionId}` to retrieve full details, diagnostic context and editable texts.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `lookbackHours` query parameter is missing. Error code: `IIA-SUG-SEA-400-001`  - `HTTP Status Code 400`, when the `lookbackHours` query parameter is lower than `1` or greater than `4800`. Error code: `IIA-SUG-SEA-400-002`  - `HTTP Status Code 400`, when one or more `status` values are not valid values of `SuggestionStatus`. Error code: `IIA-SUG-SEA-400-003`  - `HTTP Status Code 400`, when the `alertId` query parameter exceeds 50 characters. Error code: `IIA-SUG-SEA-400-004`  - `HTTP Status Code 400`, when the `targetType` query parameter is not a valid value of `SuggestionTargetType`. Error code: `IIA-SUG-SEA-400-005`  - `HTTP Status Code 400`, when the `eventName` query parameter exceeds the supported maximum length. Error code: `IIA-SUG-SEA-400-006`  - `HTTP Status Code 400`, when the `text` query parameter exceeds 200 characters. Error code: `IIA-SUG-SEA-400-007`  - `HTTP Status Code 500`, when an unexpected error occurs while searching suggestions. Error code: `IIA-SUG-SEA-500-001`   - The `agentDefinitionId` and `agentRunId` filters allow the UI to navigate from Agent Monitor to the suggestions generated by a specific digital worker.  - `HTTP Status Code 400`, when `agentDefinitionId` or `agentRunId` exceeds 50 characters. Error code: `IIA-SUG-SEA-400-008` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Suggestions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Suggestion headers returned successfully.", response = SuggestionHeader.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class, responseContainer = "List") })
    List<SuggestionHeader> searchSuggestionHeaders(@QueryParam("lookbackHours") @NotNull @Min(1) @Max(4800)  @ApiParam("Number of hours to look back from server &#x60;now&#x60;. The response includes suggestions with &#x60;generatedAt &gt;&#x3D; now - lookbackHours&#x60;.")  Integer lookbackHours,@QueryParam("status")  @ApiParam("Optional exact filter by suggestion status. Can be repeated by clients that support repeated query parameters.")  List<SuggestionStatus> status,@QueryParam("alertId") @Size(max=50)  @ApiParam("Optional exact filter by the alert that generated the suggestion.")  String alertId,@QueryParam("targetType")  @ApiParam("Optional exact filter by target type.")  SuggestionTargetType targetType,@QueryParam("eventName") @Size(max=100)  @ApiParam("Optional exact filter by operational event name. When a suggestion was generated by a scheduled interpreter and not by a specific event, the backend uses &#x60;AUTO&#x60;.")  String eventName,@QueryParam("text") @Size(max=200)  @ApiParam("Generic partial-match UI filter over header fields.")  String text,@QueryParam("agentDefinitionId") @Size(max=50)  @ApiParam("Optional exact filter by Agent definition that generated the suggestion through an Agent output.")  String agentDefinitionId,@QueryParam("agentRunId") @Size(max=50)  @ApiParam("Optional exact filter by Agent run that generated the source Agent output.")  String agentRunId);


    /**
     * Re-runs the AI-assisted alert verification flow.  Use this operation after editing a prompt, after a rejected verification, or when the available tool/model contract has changed.  ## Additional notes  - This operation re-runs the AI-assisted alert verification flow.  - Use this operation after editing a prompt, after a rejected verification or when the available tool/model contract has changed.  - If `force = false`, the backend may skip re-verification for an already verified alert whose prompt and verification contract did not change.  - If `force = true`, the backend must re-run verification even if the alert is already verified.  - Verification can update the alert status, verification metadata, interpreter metadata, required data and runtime deployment state.  - Verification must not expose arbitrary executable code through the API.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-VER-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-VER-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AlertVerificationRequest`. Error code: `IIA-ALT-VER-400-003`  - `HTTP Status Code 400`, when `preferredInterpreterType` is not a valid value of `AlertInterpreterType`. Error code: `IIA-ALT-VER-400-004`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-VER-404-001`  - `HTTP Status Code 409`, when the alert is deleted and cannot be verified. Error code: `IIA-ALT-VER-409-001`  - `HTTP Status Code 409`, when the alert is already verifying or deploying and a concurrent verification cannot be started. Error code: `IIA-ALT-VER-409-002`  - `HTTP Status Code 422`, when the alert prompt cannot be transformed into a controlled interpreter because it is ambiguous, unsafe or unsupported by available tools. Error code: `IIA-ALT-VER-422-001`  - `HTTP Status Code 422`, when the alert prompt requires operational data sources that are not available through the Assistant Tools Layer. Error code: `IIA-ALT-VER-422-002`  - `HTTP Status Code 500`, when an unexpected error occurs while verifying the alert. Error code: `IIA-ALT-VER-500-001`  - `HTTP Status Code 503`, when the verification service, LLM provider or required backend tool is temporarily unavailable. Error code: `IIA-ALT-VER-503-001` 
     *
     * @param alertId Alert identifier.
     * @param alertVerificationRequest 
     * @return Alert verification completed.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     * @return A downstream system, tool or LLM provider is not available.
     */
    @POST
    @Path("/alerts/{alertId}/verify")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Re-run alert verification", notes = "Re-runs the AI-assisted alert verification flow.  Use this operation after editing a prompt, after a rejected verification, or when the available tool/model contract has changed.  ## Additional notes  - This operation re-runs the AI-assisted alert verification flow.  - Use this operation after editing a prompt, after a rejected verification or when the available tool/model contract has changed.  - If `force = false`, the backend may skip re-verification for an already verified alert whose prompt and verification contract did not change.  - If `force = true`, the backend must re-run verification even if the alert is already verified.  - Verification can update the alert status, verification metadata, interpreter metadata, required data and runtime deployment state.  - Verification must not expose arbitrary executable code through the API.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only whitespace characters. Error code: `IIA-ALT-VER-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters. Error code: `IIA-ALT-VER-400-002`  - `HTTP Status Code 400`, when the request body is provided but is not a valid JSON serialization of schema `AlertVerificationRequest`. Error code: `IIA-ALT-VER-400-003`  - `HTTP Status Code 400`, when `preferredInterpreterType` is not a valid value of `AlertInterpreterType`. Error code: `IIA-ALT-VER-400-004`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found. Error code: `IIA-ALT-VER-404-001`  - `HTTP Status Code 409`, when the alert is deleted and cannot be verified. Error code: `IIA-ALT-VER-409-001`  - `HTTP Status Code 409`, when the alert is already verifying or deploying and a concurrent verification cannot be started. Error code: `IIA-ALT-VER-409-002`  - `HTTP Status Code 422`, when the alert prompt cannot be transformed into a controlled interpreter because it is ambiguous, unsafe or unsupported by available tools. Error code: `IIA-ALT-VER-422-001`  - `HTTP Status Code 422`, when the alert prompt requires operational data sources that are not available through the Assistant Tools Layer. Error code: `IIA-ALT-VER-422-002`  - `HTTP Status Code 500`, when an unexpected error occurs while verifying the alert. Error code: `IIA-ALT-VER-500-001`  - `HTTP Status Code 503`, when the verification service, LLM provider or required backend tool is temporarily unavailable. Error code: `IIA-ALT-VER-503-001` ", authorizations = {
        
        @Authorization(value = "bearerAuth")
         }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Alert verification completed.", response = AlertDetail.class),
        @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
        @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
        @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
        @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
        @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
        @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
        @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class),
        @ApiResponse(code = 503, message = "A downstream system, tool or LLM provider is not available.", response = Error.class) })
    AlertDetail verifyAlert(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId,@Valid AlertVerificationRequest alertVerificationRequest);



    /**
     * Utility endpoint that sends a single plain string to the AI provider and returns a single improved string.  The operation is intentionally minimal and is meant to be used first as a safe integration test for AI invocation and later by the application to make operator prompts more ordered, readable and grammatically correct.  The backend should preserve the original meaning, correct spelling and grammar, improve clarity and readability, and avoid adding operational facts, IDs, constraints or domain details that are not present in the input text.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-UTL-TXI-400-001`  - `HTTP Status Code 400`, when the request body is not a JSON string. Error code: `IIA-UTL-TXI-400-002`  - `HTTP Status Code 400`, when the text is empty or contains only whitespace characters. Error code: `IIA-UTL-TXI-400-003`  - `HTTP Status Code 400`, when the text exceeds 8000 characters. Error code: `IIA-UTL-TXI-400-004`  - `HTTP Status Code 500`, when an unexpected error occurs while improving the text. Error code: `IIA-UTL-TXI-500-001`  - `HTTP Status Code 503`, when the LLM provider is temporarily unavailable. Error code: `IIA-UTL-TXI-503-001`
     *
     * @param body
     * @return Text improved successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Unexpected error.
     * @return A downstream system, tool or LLM provider is not available.
     */
    @POST
    @Path("/utilities/text/improve")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Improve a text with AI", notes = "Utility endpoint that sends a single plain string to the AI provider and returns a single improved string.  The operation is intentionally minimal and is meant to be used first as a safe integration test for AI invocation and later by the application to make operator prompts more ordered, readable and grammatically correct.  The backend should preserve the original meaning, correct spelling and grammar, improve clarity and readability, and avoid adding operational facts, IDs, constraints or domain details that are not present in the input text.  ## Errors  Invalid parameters should raise an HTTP Status Code 400 according with the following list:  - `HTTP Status Code 400`, when the request body is missing. Error code: `IIA-UTL-TXI-400-001`  - `HTTP Status Code 400`, when the request body is not a JSON string. Error code: `IIA-UTL-TXI-400-002`  - `HTTP Status Code 400`, when the text is empty or contains only whitespace characters. Error code: `IIA-UTL-TXI-400-003`  - `HTTP Status Code 400`, when the text exceeds 8000 characters. Error code: `IIA-UTL-TXI-400-004`  - `HTTP Status Code 500`, when an unexpected error occurs while improving the text. Error code: `IIA-UTL-TXI-500-001`  - `HTTP Status Code 503`, when the LLM provider is temporarily unavailable. Error code: `IIA-UTL-TXI-503-001` ", authorizations = {

            @Authorization(value = "bearerAuth")
    }, tags={ "Utilities" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Text improved successfully.", response = String.class),
            @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
            @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class),
            @ApiResponse(code = 503, message = "A downstream system, tool or LLM provider is not available.", response = Error.class) })
    String improveText(@Valid @NotNull String body);

    /**
     * Replaces the current technical specification of a verified Alert with an expert-provided technical specification.  This operation is intentionally restricted to expert users and must be treated as a governed manual correction workflow. The submitted JSON must not be persisted as-is without validation: the backend must validate it with the same controlled validation layer used by the Alert verification pipeline.  ## Source of truth and derived artifacts  The submitted technical specification becomes the new source of truth only if backend validation succeeds.  After a successful replacement:  - the Alert remains verified; - the technical specification is replaced; - the Alert is marked as manually edited through `technicalSpecificationEdited = true`; - the Agent Blueprint preview must be regenerated, refreshed or realigned as a derived   artifact from the updated technical specification; - if the Alert was enabled, the backend must disable it until the operator explicitly   enables it again, because the runtime interpretation has changed through a manual action  The API does not accept an Agent Blueprint preview in input. The blueprint preview is not an independent source of truth and must not be manually edited through this endpoint.  ## Validation contract  The backend must validate the submitted specification according to the declared interpreter and data contract.  For `EVENT_INTERPRETER`, validation must ensure that the specification is compatible with realtime `ServiceDataV2` event evaluation and with the supported condition DSL.  For `SCHEDULED_INTERPRETER`, validation must ensure that the specification is compatible with Service Data API snapshot evaluation, including schedule, service data query, snapshot evaluation, output policy, supported fields, supported operators, supported enum values, location resolution and array-correlation rules.  The validator must reject:  - executable code or script-like payloads; - unsupported data sources; - unsupported interpreter types; - unsupported fields or operators; - invalid stop point identifiers; - malformed condition trees; - Scheduled specifications contaminated by Event-only fields; - Event specifications contaminated by Scheduled-only structures; - specifications that ignore required constraints encoded by the controlled contract.  ## Behaviour  - The Alert must exist. - The Alert must not be logically deleted. - The Alert must be verified before manual replacement. - The request body must contain a `technicalSpecification` object. - The backend must validate the submitted specification before saving it. - The backend must not invoke the Alert verification LLM flow. - The backend must not use this operation to reinterpret the original prompt. - The backend must not create Agent Definitions, Agent Runs, Agent Outputs or Suggestions. - A successful replacement increments the Alert version. - A successful replacement must be auditable through Alert version history or equivalent   persistence. - A later `POST /v1/alerts/{alertId}/verify` recalculates the specification from the   original prompt and resets `technicalSpecificationEdited` to `false`.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only   whitespace characters. Error code: `IIA-ALT-TSP-PUT-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters.   Error code: `IIA-ALT-TSP-PUT-400-002`  - `HTTP Status Code 400`, when the request body is missing.   Error code: `IIA-ALT-TSP-PUT-400-003`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema   `AlertTechnicalSpecificationUpdateRequest`. Error code: `IIA-ALT-TSP-PUT-400-004`  - `HTTP Status Code 400`, when `technicalSpecification` is missing or is not a JSON object.   Error code: `IIA-ALT-TSP-PUT-400-005`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found.   Error code: `IIA-ALT-TSP-PUT-404-001`  - `HTTP Status Code 409`, when the alert is deleted and cannot be modified.   Error code: `IIA-ALT-TSP-PUT-409-001`  - `HTTP Status Code 409`, when the alert is not verified. Error code:   `IIA-ALT-TSP-PUT-409-002`  - `HTTP Status Code 409`, when the alert is currently verifying or another technical   specification update is already in progress. Error code: `IIA-ALT-TSP-PUT-409-003`  - `HTTP Status Code 422`, when the submitted technical specification is syntactically valid   JSON but fails backend validation. Error code: `IIA-ALT-TSP-PUT-422-001`  - `HTTP Status Code 422`, when the submitted technical specification uses unsupported   sources, fields, operators, enum values, interpreter types or evaluation modes.   Error code: `IIA-ALT-TSP-PUT-422-002`  - `HTTP Status Code 500`, when an unexpected error occurs while replacing the technical   specification. Error code: `IIA-ALT-TSP-PUT-500-001`
     *
     *
     * @param alertId Alert identifier.
     * @param alertTechnicalSpecificationUpdateRequest
     * @return Alert technical specification replaced successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @PUT
    @Path("/alerts/{alertId}/technical-specification")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Replace alert technical specification", notes = "Replaces the current technical specification of a verified Alert with an expert-provided technical specification.  This operation is intentionally restricted to expert users and must be treated as a governed manual correction workflow. The submitted JSON must not be persisted as-is without validation: the backend must validate it with the same controlled validation layer used by the Alert verification pipeline.  ## Source of truth and derived artifacts  The submitted technical specification becomes the new source of truth only if backend validation succeeds.  After a successful replacement:  - the Alert remains verified; - the technical specification is replaced; - the Alert is marked as manually edited through `technicalSpecificationEdited = true`; - the Agent Blueprint preview must be regenerated, refreshed or realigned as a derived   artifact from the updated technical specification; - if the Alert was enabled, the backend must disable it until the operator explicitly   enables it again, because the runtime interpretation has changed through a manual action  The API does not accept an Agent Blueprint preview in input. The blueprint preview is not an independent source of truth and must not be manually edited through this endpoint.  ## Validation contract  The backend must validate the submitted specification according to the declared interpreter and data contract.  For `EVENT_INTERPRETER`, validation must ensure that the specification is compatible with realtime `ServiceDataV2` event evaluation and with the supported condition DSL.  For `SCHEDULED_INTERPRETER`, validation must ensure that the specification is compatible with Service Data API snapshot evaluation, including schedule, service data query, snapshot evaluation, output policy, supported fields, supported operators, supported enum values, location resolution and array-correlation rules.  The validator must reject:  - executable code or script-like payloads; - unsupported data sources; - unsupported interpreter types; - unsupported fields or operators; - invalid stop point identifiers; - malformed condition trees; - Scheduled specifications contaminated by Event-only fields; - Event specifications contaminated by Scheduled-only structures; - specifications that ignore required constraints encoded by the controlled contract.  ## Behaviour  - The Alert must exist. - The Alert must not be logically deleted. - The Alert must be verified before manual replacement. - The request body must contain a `technicalSpecification` object. - The backend must validate the submitted specification before saving it. - The backend must not invoke the Alert verification LLM flow. - The backend must not use this operation to reinterpret the original prompt. - The backend must not create Agent Definitions, Agent Runs, Agent Outputs or Suggestions. - A successful replacement increments the Alert version. - A successful replacement must be auditable through Alert version history or equivalent   persistence. - A later `POST /v1/alerts/{alertId}/verify` recalculates the specification from the   original prompt and resets `technicalSpecificationEdited` to `false`.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only   whitespace characters. Error code: `IIA-ALT-TSP-PUT-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters.   Error code: `IIA-ALT-TSP-PUT-400-002`  - `HTTP Status Code 400`, when the request body is missing.   Error code: `IIA-ALT-TSP-PUT-400-003`  - `HTTP Status Code 400`, when the request body is not a valid JSON serialization of schema   `AlertTechnicalSpecificationUpdateRequest`. Error code: `IIA-ALT-TSP-PUT-400-004`  - `HTTP Status Code 400`, when `technicalSpecification` is missing or is not a JSON object.   Error code: `IIA-ALT-TSP-PUT-400-005`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found.   Error code: `IIA-ALT-TSP-PUT-404-001`  - `HTTP Status Code 409`, when the alert is deleted and cannot be modified.   Error code: `IIA-ALT-TSP-PUT-409-001`  - `HTTP Status Code 409`, when the alert is not verified. Error code:   `IIA-ALT-TSP-PUT-409-002`  - `HTTP Status Code 409`, when the alert is currently verifying or another technical   specification update is already in progress. Error code: `IIA-ALT-TSP-PUT-409-003`  - `HTTP Status Code 422`, when the submitted technical specification is syntactically valid   JSON but fails backend validation. Error code: `IIA-ALT-TSP-PUT-422-001`  - `HTTP Status Code 422`, when the submitted technical specification uses unsupported   sources, fields, operators, enum values, interpreter types or evaluation modes.   Error code: `IIA-ALT-TSP-PUT-422-002`  - `HTTP Status Code 500`, when an unexpected error occurs while replacing the technical   specification. Error code: `IIA-ALT-TSP-PUT-500-001` ", authorizations = {

            @Authorization(value = "bearerAuth")
    }, tags={ "Alerts" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Alert technical specification replaced successfully.", response = AlertTechnicalSpecificationResponse.class),
            @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
            @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
            @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
            @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
            @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AlertTechnicalSpecificationResponse updateAlertTechnicalSpecification(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId,@Valid @NotNull AlertTechnicalSpecificationUpdateRequest alertTechnicalSpecificationUpdateRequest);

    /**
     * Returns the current backend-validated technical specification associated with a verified Alert.  The technical specification is the controlled, non-executable interpretation produced by the Alert verification pipeline. It describes how the Alert condition is represented through supported PIS data sources, interpreter type, trigger type, input model, output model and evaluation rules.  This endpoint is intended for diagnostics, expert review and UI inspection. It exposes the technical specification as a structured JSON artifact without exposing executable code.  ## Source of truth  For a verified Alert, the technical specification is the source of truth for future Agent generation flows. The Agent Blueprint preview is considered a derived artifact/cache built from the latest valid technical specification.  ## Supported Alert types  The endpoint supports technical specifications generated for controlled Alert interpreters, including:  - `EVENT_INTERPRETER` specifications based on realtime `ServiceDataV2` event messages. - `SCHEDULED_INTERPRETER` specifications based on Service Data API snapshot evaluation,   currently represented through `POST /v2/stoppointjourneys`.  ## Behaviour  - The Alert must exist. - The Alert must not be logically deleted. - The Alert must be verified. - The Alert must contain a non-empty validated technical specification. - The operation is read-only. - The operation must not invoke the AI provider. - The operation must not regenerate the Agent Blueprint preview. - The operation must not create Agent Definitions, Agent Runs, Agent Outputs or Suggestions.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only   whitespace characters. Error code: `IIA-ALT-TSP-GET-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters.   Error code: `IIA-ALT-TSP-GET-400-002`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found.   Error code: `IIA-ALT-TSP-GET-404-001`  - `HTTP Status Code 409`, when the alert is deleted and its technical specification cannot   be exposed as an operational artifact. Error code: `IIA-ALT-TSP-GET-409-001`  - `HTTP Status Code 409`, when the alert is not verified. Error code:   `IIA-ALT-TSP-GET-409-002`  - `HTTP Status Code 422`, when the alert is verified but does not contain a valid persisted   technical specification. Error code: `IIA-ALT-TSP-GET-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the technical   specification. Error code: `IIA-ALT-TSP-GET-500-001`
     *
     * @param alertId Alert identifier.
     * @return Alert technical specification returned successfully.
     * @return Invalid request or invalid parameter.
     * @return Missing or invalid authentication.
     * @return Authenticated user is not allowed to perform the operation.
     * @return Requested resource was not found.
     * @return Conflict or invalid workflow transition.
     * @return The request is syntactically valid but cannot be processed in the assistant domain.
     * @return Unexpected error.
     */
    @GET
    @Path("/alerts/{alertId}/technical-specification")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get alert technical specification", notes = "Returns the current backend-validated technical specification associated with a verified Alert.  The technical specification is the controlled, non-executable interpretation produced by the Alert verification pipeline. It describes how the Alert condition is represented through supported PIS data sources, interpreter type, trigger type, input model, output model and evaluation rules.  This endpoint is intended for diagnostics, expert review and UI inspection. It exposes the technical specification as a structured JSON artifact without exposing executable code.  ## Source of truth  For a verified Alert, the technical specification is the source of truth for future Agent generation flows. The Agent Blueprint preview is considered a derived artifact/cache built from the latest valid technical specification.  ## Supported Alert types  The endpoint supports technical specifications generated for controlled Alert interpreters, including:  - `EVENT_INTERPRETER` specifications based on realtime `ServiceDataV2` event messages. - `SCHEDULED_INTERPRETER` specifications based on Service Data API snapshot evaluation,   currently represented through `POST /v2/stoppointjourneys`.  ## Behaviour  - The Alert must exist. - The Alert must not be logically deleted. - The Alert must be verified. - The Alert must contain a non-empty validated technical specification. - The operation is read-only. - The operation must not invoke the AI provider. - The operation must not regenerate the Agent Blueprint preview. - The operation must not create Agent Definitions, Agent Runs, Agent Outputs or Suggestions.  ## Errors  - `HTTP Status Code 400`, when the `alertId` path parameter is empty or contains only   whitespace characters. Error code: `IIA-ALT-TSP-GET-400-001`  - `HTTP Status Code 400`, when the `alertId` path parameter exceeds 50 characters.   Error code: `IIA-ALT-TSP-GET-400-002`  - `HTTP Status Code 404`, when no alert with the given `alertId` is found.   Error code: `IIA-ALT-TSP-GET-404-001`  - `HTTP Status Code 409`, when the alert is deleted and its technical specification cannot   be exposed as an operational artifact. Error code: `IIA-ALT-TSP-GET-409-001`  - `HTTP Status Code 409`, when the alert is not verified. Error code:   `IIA-ALT-TSP-GET-409-002`  - `HTTP Status Code 422`, when the alert is verified but does not contain a valid persisted   technical specification. Error code: `IIA-ALT-TSP-GET-422-001`  - `HTTP Status Code 500`, when an unexpected error occurs while reading the technical   specification. Error code: `IIA-ALT-TSP-GET-500-001` ", authorizations = {

            @Authorization(value = "bearerAuth")
    }, tags={ "Alerts" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Alert technical specification returned successfully.", response = AlertTechnicalSpecificationResponse.class),
            @ApiResponse(code = 400, message = "Invalid request or invalid parameter.", response = Error.class),
            @ApiResponse(code = 401, message = "Missing or invalid authentication.", response = Error.class),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation.", response = Error.class),
            @ApiResponse(code = 404, message = "Requested resource was not found.", response = Error.class),
            @ApiResponse(code = 409, message = "Conflict or invalid workflow transition.", response = Error.class),
            @ApiResponse(code = 422, message = "The request is syntactically valid but cannot be processed in the assistant domain.", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error.", response = Error.class) })
    AlertTechnicalSpecificationResponse getAlertTechnicalSpecification(@PathParam("alertId") @Size(max=50) @ApiParam("Alert identifier.") String alertId);
}
