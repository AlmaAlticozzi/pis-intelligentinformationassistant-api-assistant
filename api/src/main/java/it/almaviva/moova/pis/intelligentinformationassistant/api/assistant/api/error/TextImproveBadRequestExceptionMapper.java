package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps text improvement validation failures to the generated API error model.
 */
@Provider
public class TextImproveBadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(BadRequestException exception) {
        if (!isTextImprovePath()) {
            return exception.getResponse();
        }

        System.out.println("[IIA-AI-TEST] Bad request mapped for text improve");
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(AssistantApiErrors.textImproveBodyNotJsonString())
                .build();
    }

    private boolean isTextImprovePath() {
        return uriInfo != null && uriInfo.getPath() != null && uriInfo.getPath().endsWith("utilities/text/improve");
    }
}
