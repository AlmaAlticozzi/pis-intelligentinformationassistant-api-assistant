package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentDefinitionCreateRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentDefinitionCreateRequest   {
  private String alertId;
  private Integer alertVersion;
  private String name;
  private String description;
  private String agentProfileId;
  private AgentActivationPolicy activationPolicy;
  private AgentGenerationMode generationMode;
  private Boolean compileImmediately = true;

  public AgentDefinitionCreateRequest() {
  }

  @JsonCreator
  public AgentDefinitionCreateRequest(
    @JsonProperty(required = true, value = "alertId") String alertId,
    @JsonProperty(required = true, value = "agentProfileId") String agentProfileId,
    @JsonProperty(required = true, value = "activationPolicy") AgentActivationPolicy activationPolicy
  ) {
    this.alertId = alertId;
    this.agentProfileId = agentProfileId;
    this.activationPolicy = activationPolicy;
  }

  /**
   **/
  public AgentDefinitionCreateRequest alertId(String alertId) {
    this.alertId = alertId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "alertId")
  @NotNull  @Size(max=50)public String getAlertId() {
    return alertId;
  }

  @JsonProperty(required = true, value = "alertId")
  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  /**
   * Expected Alert version. When omitted, backend uses latest verified version.
   **/
  public AgentDefinitionCreateRequest alertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
    return this;
  }

  
  @ApiModelProperty(value = "Expected Alert version. When omitted, backend uses latest verified version.")
  @JsonProperty("alertVersion")
  public Integer getAlertVersion() {
    return alertVersion;
  }

  @JsonProperty("alertVersion")
  public void setAlertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
  }

  /**
   **/
  public AgentDefinitionCreateRequest name(String name) {
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
  public AgentDefinitionCreateRequest description(String description) {
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
  public AgentDefinitionCreateRequest agentProfileId(String agentProfileId) {
    this.agentProfileId = agentProfileId;
    return this;
  }

  
  @ApiModelProperty(example = "MEDIUM", required = true, value = "")
  @JsonProperty(required = true, value = "agentProfileId")
  @NotNull  @Size(max=50)public String getAgentProfileId() {
    return agentProfileId;
  }

  @JsonProperty(required = true, value = "agentProfileId")
  public void setAgentProfileId(String agentProfileId) {
    this.agentProfileId = agentProfileId;
  }

  /**
   **/
  public AgentDefinitionCreateRequest activationPolicy(AgentActivationPolicy activationPolicy) {
    this.activationPolicy = activationPolicy;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "activationPolicy")
  @NotNull @Valid public AgentActivationPolicy getActivationPolicy() {
    return activationPolicy;
  }

  @JsonProperty(required = true, value = "activationPolicy")
  public void setActivationPolicy(AgentActivationPolicy activationPolicy) {
    this.activationPolicy = activationPolicy;
  }

  /**
   **/
  public AgentDefinitionCreateRequest generationMode(AgentGenerationMode generationMode) {
    this.generationMode = generationMode;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generationMode")
  public AgentGenerationMode getGenerationMode() {
    return generationMode;
  }

  @JsonProperty("generationMode")
  public void setGenerationMode(AgentGenerationMode generationMode) {
    this.generationMode = generationMode;
  }

  /**
   **/
  public AgentDefinitionCreateRequest compileImmediately(Boolean compileImmediately) {
    this.compileImmediately = compileImmediately;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("compileImmediately")
  public Boolean getCompileImmediately() {
    return compileImmediately;
  }

  @JsonProperty("compileImmediately")
  public void setCompileImmediately(Boolean compileImmediately) {
    this.compileImmediately = compileImmediately;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentDefinitionCreateRequest agentDefinitionCreateRequest = (AgentDefinitionCreateRequest) o;
    return Objects.equals(this.alertId, agentDefinitionCreateRequest.alertId) &&
        Objects.equals(this.alertVersion, agentDefinitionCreateRequest.alertVersion) &&
        Objects.equals(this.name, agentDefinitionCreateRequest.name) &&
        Objects.equals(this.description, agentDefinitionCreateRequest.description) &&
        Objects.equals(this.agentProfileId, agentDefinitionCreateRequest.agentProfileId) &&
        Objects.equals(this.activationPolicy, agentDefinitionCreateRequest.activationPolicy) &&
        Objects.equals(this.generationMode, agentDefinitionCreateRequest.generationMode) &&
        Objects.equals(this.compileImmediately, agentDefinitionCreateRequest.compileImmediately);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alertId, alertVersion, name, description, agentProfileId, activationPolicy, generationMode, compileImmediately);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentDefinitionCreateRequest {\n");
    
    sb.append("    alertId: ").append(toIndentedString(alertId)).append("\n");
    sb.append("    alertVersion: ").append(toIndentedString(alertVersion)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    agentProfileId: ").append(toIndentedString(agentProfileId)).append("\n");
    sb.append("    activationPolicy: ").append(toIndentedString(activationPolicy)).append("\n");
    sb.append("    generationMode: ").append(toIndentedString(generationMode)).append("\n");
    sb.append("    compileImmediately: ").append(toIndentedString(compileImmediately)).append("\n");
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
