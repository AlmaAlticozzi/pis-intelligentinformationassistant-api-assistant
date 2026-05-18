package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRunSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentDefinitionSummary")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentDefinitionSummary   {
  private String id;
  private String name;
  private String description;
  private AgentDefinitionStatus status;
  private AlertReference alert;
  private Integer alertVersion;
  private AgentProfile profile;
  private AgentGenerationMode generationMode;
  private AgentActivationPolicy activationPolicy;
  private AgentCompilationSummary compilation;
  private AgentRunSummary latestRun;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public AgentDefinitionSummary() {
  }

  @JsonCreator
  public AgentDefinitionSummary(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "status") AgentDefinitionStatus status,
    @JsonProperty(required = true, value = "alert") AlertReference alert,
    @JsonProperty(required = true, value = "profile") AgentProfile profile,
    @JsonProperty(required = true, value = "generationMode") AgentGenerationMode generationMode,
    @JsonProperty(required = true, value = "createdAt") OffsetDateTime createdAt
  ) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.alert = alert;
    this.profile = profile;
    this.generationMode = generationMode;
    this.createdAt = createdAt;
  }

  /**
   **/
  public AgentDefinitionSummary id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "AGDF2026251400000001", required = true, value = "")
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
  public AgentDefinitionSummary name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Cancelled metro trains at Ionio", required = true, value = "")
  @JsonProperty(required = true, value = "name")
  @NotNull public String getName() {
    return name;
  }

  @JsonProperty(required = true, value = "name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AgentDefinitionSummary description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public AgentDefinitionSummary status(AgentDefinitionStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public AgentDefinitionStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(AgentDefinitionStatus status) {
    this.status = status;
  }

  /**
   **/
  public AgentDefinitionSummary alert(AlertReference alert) {
    this.alert = alert;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "alert")
  @NotNull @Valid public AlertReference getAlert() {
    return alert;
  }

  @JsonProperty(required = true, value = "alert")
  public void setAlert(AlertReference alert) {
    this.alert = alert;
  }

  /**
   **/
  public AgentDefinitionSummary alertVersion(Integer alertVersion) {
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

  /**
   **/
  public AgentDefinitionSummary profile(AgentProfile profile) {
    this.profile = profile;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "profile")
  @NotNull @Valid public AgentProfile getProfile() {
    return profile;
  }

  @JsonProperty(required = true, value = "profile")
  public void setProfile(AgentProfile profile) {
    this.profile = profile;
  }

  /**
   **/
  public AgentDefinitionSummary generationMode(AgentGenerationMode generationMode) {
    this.generationMode = generationMode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "generationMode")
  @NotNull public AgentGenerationMode getGenerationMode() {
    return generationMode;
  }

  @JsonProperty(required = true, value = "generationMode")
  public void setGenerationMode(AgentGenerationMode generationMode) {
    this.generationMode = generationMode;
  }

  /**
   **/
  public AgentDefinitionSummary activationPolicy(AgentActivationPolicy activationPolicy) {
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

  /**
   **/
  public AgentDefinitionSummary compilation(AgentCompilationSummary compilation) {
    this.compilation = compilation;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("compilation")
  @Valid public AgentCompilationSummary getCompilation() {
    return compilation;
  }

  @JsonProperty("compilation")
  public void setCompilation(AgentCompilationSummary compilation) {
    this.compilation = compilation;
  }

  /**
   **/
  public AgentDefinitionSummary latestRun(AgentRunSummary latestRun) {
    this.latestRun = latestRun;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("latestRun")
  @Valid public AgentRunSummary getLatestRun() {
    return latestRun;
  }

  @JsonProperty("latestRun")
  public void setLatestRun(AgentRunSummary latestRun) {
    this.latestRun = latestRun;
  }

  /**
   **/
  public AgentDefinitionSummary createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "createdAt")
  @NotNull public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty(required = true, value = "createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public AgentDefinitionSummary updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentDefinitionSummary agentDefinitionSummary = (AgentDefinitionSummary) o;
    return Objects.equals(this.id, agentDefinitionSummary.id) &&
        Objects.equals(this.name, agentDefinitionSummary.name) &&
        Objects.equals(this.description, agentDefinitionSummary.description) &&
        Objects.equals(this.status, agentDefinitionSummary.status) &&
        Objects.equals(this.alert, agentDefinitionSummary.alert) &&
        Objects.equals(this.alertVersion, agentDefinitionSummary.alertVersion) &&
        Objects.equals(this.profile, agentDefinitionSummary.profile) &&
        Objects.equals(this.generationMode, agentDefinitionSummary.generationMode) &&
        Objects.equals(this.activationPolicy, agentDefinitionSummary.activationPolicy) &&
        Objects.equals(this.compilation, agentDefinitionSummary.compilation) &&
        Objects.equals(this.latestRun, agentDefinitionSummary.latestRun) &&
        Objects.equals(this.createdAt, agentDefinitionSummary.createdAt) &&
        Objects.equals(this.updatedAt, agentDefinitionSummary.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, status, alert, alertVersion, profile, generationMode, activationPolicy, compilation, latestRun, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentDefinitionSummary {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    alert: ").append(toIndentedString(alert)).append("\n");
    sb.append("    alertVersion: ").append(toIndentedString(alertVersion)).append("\n");
    sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
    sb.append("    generationMode: ").append(toIndentedString(generationMode)).append("\n");
    sb.append("    activationPolicy: ").append(toIndentedString(activationPolicy)).append("\n");
    sb.append("    compilation: ").append(toIndentedString(compilation)).append("\n");
    sb.append("    latestRun: ").append(toIndentedString(latestRun)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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
