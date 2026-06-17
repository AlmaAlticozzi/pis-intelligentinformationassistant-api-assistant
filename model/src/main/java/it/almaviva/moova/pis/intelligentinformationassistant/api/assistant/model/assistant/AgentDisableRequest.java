package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Optional command body used to disable an Agent Definition. When omitted, &#x60;stopRunningAgents&#x60; defaults to &#x60;true&#x60; and &#x60;gracePeriodSeconds&#x60; defaults to &#x60;60&#x60;. The command is idempotent for definitions already in &#x60;DISABLED&#x60;. 
 **/
@ApiModel(description = "Optional command body used to disable an Agent Definition. When omitted, `stopRunningAgents` defaults to `true` and `gracePeriodSeconds` defaults to `60`. The command is idempotent for definitions already in `DISABLED`. ")
@JsonTypeName("AgentDisableRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-17T11:36:57.274521600+02:00[Europe/Rome]", comments = "Generator version: 7.23.0")
public class AgentDisableRequest   {
  private String reason;
  private Boolean stopRunningAgents = true;
  private Integer gracePeriodSeconds = 60;

  public AgentDisableRequest() {
  }

  /**
   * Optional operator or control-plane reason recorded for lifecycle audit and propagated to the Agent Orchestrator when runtime disable is required.
   **/
  public AgentDisableRequest reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(value = "Optional operator or control-plane reason recorded for lifecycle audit and propagated to the Agent Orchestrator when runtime disable is required.")
  @JsonProperty("reason")
   @Size(max=1000)public String getReason() {
    return reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Requests graceful termination of active Agent Runs associated with the runtime definition. It has no effect on a local &#x60;READY -&gt; DISABLED&#x60; transition because no runtime definition has been accepted yet.
   **/
  public AgentDisableRequest stopRunningAgents(Boolean stopRunningAgents) {
    this.stopRunningAgents = stopRunningAgents;
    return this;
  }

  
  @ApiModelProperty(value = "Requests graceful termination of active Agent Runs associated with the runtime definition. It has no effect on a local `READY -> DISABLED` transition because no runtime definition has been accepted yet.")
  @JsonProperty("stopRunningAgents")
  public Boolean getStopRunningAgents() {
    return stopRunningAgents;
  }

  @JsonProperty("stopRunningAgents")
  public void setStopRunningAgents(Boolean stopRunningAgents) {
    this.stopRunningAgents = stopRunningAgents;
  }

  /**
   * Maximum graceful-stop interval requested from the Agent Orchestrator. A value of zero requests no graceful wait; runtime policy still governs the final interruption behavior.
   * minimum: 0
   * maximum: 600
   **/
  public AgentDisableRequest gracePeriodSeconds(Integer gracePeriodSeconds) {
    this.gracePeriodSeconds = gracePeriodSeconds;
    return this;
  }

  
  @ApiModelProperty(value = "Maximum graceful-stop interval requested from the Agent Orchestrator. A value of zero requests no graceful wait; runtime policy still governs the final interruption behavior.")
  @JsonProperty("gracePeriodSeconds")
   @Min(0) @Max(600)public Integer getGracePeriodSeconds() {
    return gracePeriodSeconds;
  }

  @JsonProperty("gracePeriodSeconds")
  public void setGracePeriodSeconds(Integer gracePeriodSeconds) {
    this.gracePeriodSeconds = gracePeriodSeconds;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentDisableRequest agentDisableRequest = (AgentDisableRequest) o;
    return Objects.equals(this.reason, agentDisableRequest.reason) &&
        Objects.equals(this.stopRunningAgents, agentDisableRequest.stopRunningAgents) &&
        Objects.equals(this.gracePeriodSeconds, agentDisableRequest.gracePeriodSeconds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reason, stopRunningAgents, gracePeriodSeconds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentDisableRequest {\n");
    
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    stopRunningAgents: ").append(toIndentedString(stopRunningAgents)).append("\n");
    sb.append("    gracePeriodSeconds: ").append(toIndentedString(gracePeriodSeconds)).append("\n");
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
