package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentDisableDownstreamException extends RuntimeException {
    private final int assistantHttpStatus;
    private final Integer downstreamHttpStatus;
    private final String downstreamCode;
    private final String downstreamTraceId;

    public AgentDisableDownstreamException(int assistantHttpStatus, Integer downstreamHttpStatus,
            String downstreamCode, String downstreamTraceId, String detail) {
        super(detail);
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
