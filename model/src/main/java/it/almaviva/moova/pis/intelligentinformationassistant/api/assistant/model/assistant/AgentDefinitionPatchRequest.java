package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentDefinitionPatchRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentDefinitionPatchRequest   {
  private String name;
  private String description;
  private String agentProfileId;
  private AgentActivationPolicy activationPolicy;

  public AgentDefinitionPatchRequest() {
  }

  /**
   **/
  public AgentDefinitionPatchRequest name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
   @Size(min=1,max=120)public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AgentDefinitionPatchRequest description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
   @Size(max=1000)public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public AgentDefinitionPatchRequest agentProfileId(String agentProfileId) {
    this.agentProfileId = agentProfileId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("agentProfileId")
   @Size(max=50)public String getAgentProfileId() {
    return agentProfileId;
  }

  @JsonProperty("agentProfileId")
  public void setAgentProfileId(String agentProfileId) {
    this.agentProfileId = agentProfileId;
  }

  /**
   **/
  public AgentDefinitionPatchRequest activationPolicy(AgentActivationPolicy activationPolicy) {
    this.activationPolicy = activationPolicy;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("activationPolicy")
  @Valid public AgentActivationPolicy getActivationPolicy() {
    return activationPolicy;
  }

  @JsonProperty("activationPolicy")
  public void setActivationPolicy(AgentActivationPolicy activationPolicy) {
    this.activationPolicy = activationPolicy;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentDefinitionPatchRequest agentDefinitionPatchRequest = (AgentDefinitionPatchRequest) o;
    return Objects.equals(this.name, agentDefinitionPatchRequest.name) &&
        Objects.equals(this.description, agentDefinitionPatchRequest.description) &&
        Objects.equals(this.agentProfileId, agentDefinitionPatchRequest.agentProfileId) &&
        Objects.equals(this.activationPolicy, agentDefinitionPatchRequest.activationPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, agentProfileId, activationPolicy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentDefinitionPatchRequest {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    agentProfileId: ").append(toIndentedString(agentProfileId)).append("\n");
    sb.append("    activationPolicy: ").append(toIndentedString(activationPolicy)).append("\n");
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
