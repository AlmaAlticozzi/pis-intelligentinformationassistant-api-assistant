package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error;

import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps service unavailable failures from the text improvement flow to HTTP 503.
 */
@Provider
public class TextImproveServiceUnavailableExceptionMapper implements ExceptionMapper<ServiceUnavailableException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ServiceUnavailableException exception) {
        if (!isTextImprovePath()) {
            return exception.getResponse();
        }

        System.out.println("[IIA-AI-TEST] LLM provider unavailable mapped");
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(AssistantApiErrors.textImproveLlmProviderUnavailable())
                .build();
    }

    private boolean isTextImprovePath() {
        return uriInfo != null && uriInfo.getPath() != null && uriInfo.getPath().endsWith("utilities/text/improve");
    }
}
