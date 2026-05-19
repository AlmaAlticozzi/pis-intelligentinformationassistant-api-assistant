package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.error;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmProviderException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps temporary LLM provider failures to HTTP 503.
 */
@Provider
public class TextImproveLlmProviderExceptionMapper implements ExceptionMapper<LlmProviderException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(LlmProviderException exception) {
        System.out.println("[IIA-AI-TEST] LLM provider unavailable mapped");
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(isTextImprovePath()
                        ? AssistantApiErrors.textImproveLlmProviderUnavailable()
                        : new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Error()
                                .code("IIA-503-001")
                                .title("Service unavailable")
                                .detail("A required provider is temporarily unavailable."))
                .build();
    }

    private boolean isTextImprovePath() {
        return uriInfo != null && uriInfo.getPath() != null && uriInfo.getPath().endsWith("utilities/text/improve");
    }
}
