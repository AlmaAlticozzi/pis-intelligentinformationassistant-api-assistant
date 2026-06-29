package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentActivationDownstreamException extends RuntimeException {

    private final int assistantHttpStatus;
    private final Integer downstreamHttpStatus;
    private final String downstreamCode;
    private final String downstreamTraceId;

    public AgentActivationDownstreamException(
            int assistantHttpStatus,
            Integer downstreamHttpStatus,
            String downstreamCode,
            String downstreamTraceId,
            String safeDetail) {
        super(safeDetail);
        this.assistantHttpStatus = assistantHttpStatus;
        this.downstreamHttpStatus = downstreamHttpStatus;
        this.downstreamCode = downstreamCode;
        this.downstreamTraceId = downstreamTraceId;
    }

    public int assistantHttpStatus() { return assistantHttpStatus; }
    public Integer downstreamHttpStatus() { return downstreamHttpStatus; }
    public String downstreamCode() { return downstreamCode; }
    public String downstreamTraceId() { return downstreamTraceId; }
}
