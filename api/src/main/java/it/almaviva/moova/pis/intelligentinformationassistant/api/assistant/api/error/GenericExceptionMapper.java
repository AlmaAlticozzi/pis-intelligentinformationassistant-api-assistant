package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Minimal fallback mapper for unhandled API exceptions.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null
                && webApplicationException.getResponse().hasEntity()) {
            return webApplicationException.getResponse();
        }

        exception.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(isTextImprovePath()
                        ? AssistantApiErrors.textImproveUnexpectedError()
                        : new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error()
                                .code("IIA-500-001")
                                .title("Unexpected error")
                                .detail("An unexpected error occurred."))
                .build();
    }

    private boolean isTextImprovePath() {
        return uriInfo != null && uriInfo.getPath() != null && uriInfo.getPath().endsWith("utilities/text/improve");
    }
}
