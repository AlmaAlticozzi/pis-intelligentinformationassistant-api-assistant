package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.interfaces.IAssistantInternalV1Api;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error.AssistantApiErrors;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;

import java.time.OffsetDateTime;
import java.util.Set;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.DesiredRuntimeCatalogConsistencyException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.DesiredRuntimeCatalogCheckpointIncompatibleException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.DesiredRuntimeCatalogInvalidRequestException;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.DesiredRuntimeCatalogRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.DesiredRuntimeCatalogService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.DesiredRuntimeCatalogUnavailableException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/internal/v1")
public class AssistantInternalV1Api implements IAssistantInternalV1Api {

    @Inject
    DesiredRuntimeCatalogService desiredRuntimeCatalogService;

    @GET
    @Path("/runtime-agent-catalog")
    @Produces({ "application/json" })
    @Override
    public DesiredRuntimeCatalogResponse getDesiredRuntimeAgentCatalog(@QueryParam("mode") DesiredRuntimeCatalogMode mode, @QueryParam("changedAfter") OffsetDateTime changedAfter, @QueryParam("checkpoint")  String checkpoint, @QueryParam("agentDefinitionId") Set<String> agentDefinitionId, @QueryParam("cursor")  String cursor, @QueryParam("limit")  Integer limit) {
        try {
            return desiredRuntimeCatalogService.get(new DesiredRuntimeCatalogRequest(
                    mode, changedAfter, checkpoint, agentDefinitionId, cursor, limit));
        } catch (DesiredRuntimeCatalogInvalidRequestException ex) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(AssistantApiErrors.desiredRuntimeCatalogInvalidRequest(ex.source(), ex.getMessage())).build());
        } catch (DesiredRuntimeCatalogConsistencyException ex) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                    .entity(AssistantApiErrors.desiredRuntimeCatalogConflict(ex.getMessage())).build());
        } catch (DesiredRuntimeCatalogCheckpointIncompatibleException ex) {
            throw new WebApplicationException(Response.status(422)
                    .entity(AssistantApiErrors.desiredRuntimeCatalogCheckpointIncompatible(ex.getMessage())).build());
        } catch (DesiredRuntimeCatalogUnavailableException ex) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(AssistantApiErrors.desiredRuntimeCatalogUnavailable()).build());
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG] unexpected error type="
                    + ex.getClass().getSimpleName());
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(AssistantApiErrors.desiredRuntimeCatalogUnexpectedError()).build());
        }
    }
}
