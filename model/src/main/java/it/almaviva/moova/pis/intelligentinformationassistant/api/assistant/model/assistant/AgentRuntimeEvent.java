package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimeEventSeverity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimeEventType;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentRuntimeEvent")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentRuntimeEvent   {
  private String id;
  private AgentRuntimeEventType eventType;
  private String agentRunId;
  private String agentDefinitionId;
  private AgentRuntimeEventSeverity severity;
  private String reason;
  private @Valid Map<String, Object> details = new HashMap<>();
  private OffsetDateTime occurredAt;

  public AgentRuntimeEvent() {
  }

  @JsonCreator
  public AgentRuntimeEvent(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "eventType") AgentRuntimeEventType eventType,
    @JsonProperty(required = true, value = "agentRunId") String agentRunId,
    @JsonProperty(required = true, value = "severity") AgentRuntimeEventSeverity severity,
    @JsonProperty(required = true, value = "occurredAt") OffsetDateTime occurredAt
  ) {
    this.id = id;
    this.eventType = eventType;
    this.agentRunId = agentRunId;
    this.severity = severity;
    this.occurredAt = occurredAt;
  }

  /**
   **/
  public AgentRuntimeEvent id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "id")
  @NotNull  @Size(max=50)public String getId() {
    return id;
  }

  @JsonProperty(required = true, value = "id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public AgentRuntimeEvent eventType(AgentRuntimeEventType eventType) {
    this.eventType = eventType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "eventType")
  @NotNull public AgentRuntimeEventType getEventType() {
    return eventType;
  }

  @JsonProperty(required = true, value = "eventType")
  public void setEventType(AgentRuntimeEventType eventType) {
    this.eventType = eventType;
  }

  /**
   **/
  public AgentRuntimeEvent agentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentRunId")
  @NotNull  @Size(max=50)public String getAgentRunId() {
    return agentRunId;
  }

  @JsonProperty(required = true, value = "agentRunId")
  public void setAgentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
  }

  /**
   **/
  public AgentRuntimeEvent agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("agentDefinitionId")
   @Size(max=50)public String getAgentDefinitionId() {
    return agentDefinitionId;
  }

  @JsonProperty("agentDefinitionId")
  public void setAgentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
  }

  /**
   **/
  public AgentRuntimeEvent severity(AgentRuntimeEventSeverity severity) {
    this.severity = severity;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "severity")
  @NotNull public AgentRuntimeEventSeverity getSeverity() {
    return severity;
  }

  @JsonProperty(required = true, value = "severity")
  public void setSeverity(AgentRuntimeEventSeverity severity) {
    this.severity = severity;
  }

  /**
   **/
  public AgentRuntimeEvent reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("reason")
  public String getReason() {
    return reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   **/
  public AgentRuntimeEvent details(Map<String, Object> details) {
    this.details = details;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("details")
  public Map<String, Object> getDetails() {
    return details;
  }

  @JsonProperty("details")
  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }

  public AgentRuntimeEvent putDetailsItem(String key, Object detailsItem) {
    if (this.details == null) {
      this.details = new HashMap<>();
    }

    this.details.put(key, detailsItem);
    return this;
  }

  public AgentRuntimeEvent removeDetailsItem(String key) {
    if (this.details != null) {
      this.details.remove(key);
    }

    return this;
  }
  /**
   **/
  public AgentRuntimeEvent occurredAt(OffsetDateTime occurredAt) {
    this.occurredAt = occurredAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "occurredAt")
  @NotNull public OffsetDateTime getOccurredAt() {
    return occurredAt;
  }

  @JsonProperty(required = true, value = "occurredAt")
  public void setOccurredAt(OffsetDateTime occurredAt) {
    this.occurredAt = occurredAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRuntimeEvent agentRuntimeEvent = (AgentRuntimeEvent) o;
    return Objects.equals(this.id, agentRuntimeEvent.id) &&
        Objects.equals(this.eventType, agentRuntimeEvent.eventType) &&
        Objects.equals(this.agentRunId, agentRuntimeEvent.agentRunId) &&
        Objects.equals(this.agentDefinitionId, agentRuntimeEvent.agentDefinitionId) &&
        Objects.equals(this.severity, agentRuntimeEvent.severity) &&
        Objects.equals(this.reason, agentRuntimeEvent.reason) &&
        Objects.equals(this.details, agentRuntimeEvent.details) &&
        Objects.equals(this.occurredAt, agentRuntimeEvent.occurredAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, eventType, agentRunId, agentDefinitionId, severity, reason, details, occurredAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRuntimeEvent {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
    sb.append("    agentRunId: ").append(toIndentedString(agentRunId)).append("\n");
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
    sb.append("    occurredAt: ").append(toIndentedString(occurredAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }


}
