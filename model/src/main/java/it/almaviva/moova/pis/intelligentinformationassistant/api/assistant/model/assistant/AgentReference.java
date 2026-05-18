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
 * Compact Agent reference attached to suggestions and outputs.
 **/
@ApiModel(description = "Compact Agent reference attached to suggestions and outputs.")
@JsonTypeName("AgentReference")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentReference   {
  private String agentDefinitionId;
  private String agentRunId;
  private String agentName;
  private Integer alertVersion;

  public AgentReference() {
  }

  /**
   **/
  public AgentReference agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(example = "AGDF2026251400000001", value = "")
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
  public AgentReference agentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
    return this;
  }

  
  @ApiModelProperty(example = "AGRN2026251400000001", value = "")
  @JsonProperty("agentRunId")
   @Size(max=50)public String getAgentRunId() {
    return agentRunId;
  }

  @JsonProperty("agentRunId")
  public void setAgentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
  }

  /**
   **/
  public AgentReference agentName(String agentName) {
    this.agentName = agentName;
    return this;
  }

  
  @ApiModelProperty(example = "Cancelled metro trains at Ionio", value = "")
  @JsonProperty("agentName")
  public String getAgentName() {
    return agentName;
  }

  @JsonProperty("agentName")
  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  /**
   **/
  public AgentReference alertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("alertVersion")
  public Integer getAlertVersion() {
    return alertVersion;
  }

  @JsonProperty("alertVersion")
  public void setAlertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentReference agentReference = (AgentReference) o;
    return Objects.equals(this.agentDefinitionId, agentReference.agentDefinitionId) &&
        Objects.equals(this.agentRunId, agentReference.agentRunId) &&
        Objects.equals(this.agentName, agentReference.agentName) &&
        Objects.equals(this.alertVersion, agentReference.alertVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(agentDefinitionId, agentRunId, agentName, alertVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentReference {\n");
    
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    agentRunId: ").append(toIndentedString(agentRunId)).append("\n");
    sb.append("    agentName: ").append(toIndentedString(agentName)).append("\n");
    sb.append("    alertVersion: ").append(toIndentedString(alertVersion)).append("\n");
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
