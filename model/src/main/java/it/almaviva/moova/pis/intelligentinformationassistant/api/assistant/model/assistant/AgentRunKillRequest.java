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



@JsonTypeName("AgentRunKillRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentRunKillRequest   {
  private String reason;
  private Boolean force = false;
  private Integer gracePeriodSeconds = 60;

  public AgentRunKillRequest() {
  }

  /**
   **/
  public AgentRunKillRequest reason(String reason) {
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
  public AgentRunKillRequest force(Boolean force) {
    this.force = force;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("force")
  public Boolean getForce() {
    return force;
  }

  @JsonProperty("force")
  public void setForce(Boolean force) {
    this.force = force;
  }

  /**
   * minimum: 0
   * maximum: 600
   **/
  public AgentRunKillRequest gracePeriodSeconds(Integer gracePeriodSeconds) {
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
    AgentRunKillRequest agentRunKillRequest = (AgentRunKillRequest) o;
    return Objects.equals(this.reason, agentRunKillRequest.reason) &&
        Objects.equals(this.force, agentRunKillRequest.force) &&
        Objects.equals(this.gracePeriodSeconds, agentRunKillRequest.gracePeriodSeconds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reason, force, gracePeriodSeconds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRunKillRequest {\n");
    
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    force: ").append(toIndentedString(force)).append("\n");
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
