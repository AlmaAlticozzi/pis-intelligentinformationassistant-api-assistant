package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.interfaces.IAssistantInternalV1Api;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;

import java.time.OffsetDateTime;
import java.util.Set;

@ApplicationScoped
@Path("/internal/v1")
public class AssistantInternalV1Api implements IAssistantInternalV1Api {


    @GET
    @Path("/runtime-agent-catalog")
    @Produces({ "application/json" })
    @Override
    public DesiredRuntimeCatalogResponse getDesiredRuntimeAgentCatalog(@QueryParam("mode") DesiredRuntimeCatalogMode mode, @QueryParam("changedAfter") OffsetDateTime changedAfter, @QueryParam("checkpoint")  String checkpoint, @QueryParam("agentDefinitionId") Set<String> agentDefinitionId, @QueryParam("cursor")  String cursor, @QueryParam("limit")  Integer limit) {
        System.out.println("AssistantInternalV1Api.DesiredRuntimeCatalogResponse");
        return new DesiredRuntimeCatalogResponse();
    }
}
