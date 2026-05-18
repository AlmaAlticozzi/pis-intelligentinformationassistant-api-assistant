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



@JsonTypeName("AgentDisableRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentDisableRequest   {
  private String reason;
  private Boolean stopRunningAgents = true;
  private Integer gracePeriodSeconds = 60;

  public AgentDisableRequest() {
  }

  /**
   **/
  public AgentDisableRequest reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("reason")
   @Size(max=1000)public String getReason() {
    return reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   **/
  public AgentDisableRequest stopRunningAgents(Boolean stopRunningAgents) {
    this.stopRunningAgents = stopRunningAgents;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stopRunningAgents")
  public Boolean getStopRunningAgents() {
    return stopRunningAgents;
  }

  @JsonProperty("stopRunningAgents")
  public void setStopRunningAgents(Boolean stopRunningAgents) {
    this.stopRunningAgents = stopRunningAgents;
  }

  /**
   * minimum: 0
   * maximum: 600
   **/
  public AgentDisableRequest gracePeriodSeconds(Integer gracePeriodSeconds) {
    this.gracePeriodSeconds = gracePeriodSeconds;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
