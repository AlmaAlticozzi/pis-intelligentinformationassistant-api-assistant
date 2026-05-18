package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentHealthStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentQualityStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRunStatus;
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



@JsonTypeName("AgentRunSummary")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentRunSummary   {
  private String id;
  private String agentDefinitionId;
  private String agentName;
  private AlertReference alert;
  private AgentRunStatus status;
  private AgentHealthStatus healthStatus;
  private Integer healthScore;
  private AgentQualityStatus qualityStatus;
  private Integer qualityScore;
  private String profileId;
  private Double cpuUsagePercent;
  private Double memoryUsagePercent;
  private Long generatedOutputs;
  private Long createdSuggestions;
  private OffsetDateTime lastHeartbeatAt;
  private OffsetDateTime startedAt;
  private OffsetDateTime stoppedAt;

  public AgentRunSummary() {
  }

  @JsonCreator
  public AgentRunSummary(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "agentDefinitionId") String agentDefinitionId,
    @JsonProperty(required = true, value = "status") AgentRunStatus status,
    @JsonProperty(required = true, value = "startedAt") OffsetDateTime startedAt
  ) {
    this.id = id;
    this.agentDefinitionId = agentDefinitionId;
    this.status = status;
    this.startedAt = startedAt;
  }

  /**
   **/
  public AgentRunSummary id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "AGRN2026251400000001", required = true, value = "")
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
  public AgentRunSummary agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentDefinitionId")
  @NotNull  @Size(max=50)public String getAgentDefinitionId() {
    return agentDefinitionId;
  }

  @JsonProperty(required = true, value = "agentDefinitionId")
  public void setAgentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
  }

  /**
   **/
  public AgentRunSummary agentName(String agentName) {
    this.agentName = agentName;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public AgentRunSummary alert(AlertReference alert) {
    this.alert = alert;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("alert")
  @Valid public AlertReference getAlert() {
    return alert;
  }

  @JsonProperty("alert")
  public void setAlert(AlertReference alert) {
    this.alert = alert;
  }

  /**
   **/
  public AgentRunSummary status(AgentRunStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public AgentRunStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(AgentRunStatus status) {
    this.status = status;
  }

  /**
   **/
  public AgentRunSummary healthStatus(AgentHealthStatus healthStatus) {
    this.healthStatus = healthStatus;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("healthStatus")
  public AgentHealthStatus getHealthStatus() {
    return healthStatus;
  }

  @JsonProperty("healthStatus")
  public void setHealthStatus(AgentHealthStatus healthStatus) {
    this.healthStatus = healthStatus;
  }

  /**
   * minimum: 0
   * maximum: 100
   **/
  public AgentRunSummary healthScore(Integer healthScore) {
    this.healthScore = healthScore;
    return this;
  }

  
  @ApiModelProperty(example = "74", value = "")
  @JsonProperty("healthScore")
   @Min(0) @Max(100)public Integer getHealthScore() {
    return healthScore;
  }

  @JsonProperty("healthScore")
  public void setHealthScore(Integer healthScore) {
    this.healthScore = healthScore;
  }

  /**
   **/
  public AgentRunSummary qualityStatus(AgentQualityStatus qualityStatus) {
    this.qualityStatus = qualityStatus;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("qualityStatus")
  public AgentQualityStatus getQualityStatus() {
    return qualityStatus;
  }

  @JsonProperty("qualityStatus")
  public void setQualityStatus(AgentQualityStatus qualityStatus) {
    this.qualityStatus = qualityStatus;
  }

  /**
   * minimum: 0
   * maximum: 100
   **/
  public AgentRunSummary qualityScore(Integer qualityScore) {
    this.qualityScore = qualityScore;
    return this;
  }

  
  @ApiModelProperty(example = "82", value = "")
  @JsonProperty("qualityScore")
   @Min(0) @Max(100)public Integer getQualityScore() {
    return qualityScore;
  }

  @JsonProperty("qualityScore")
  public void setQualityScore(Integer qualityScore) {
    this.qualityScore = qualityScore;
  }

  /**
   **/
  public AgentRunSummary profileId(String profileId) {
    this.profileId = profileId;
    return this;
  }

  
  @ApiModelProperty(example = "MEDIUM", value = "")
  @JsonProperty("profileId")
  public String getProfileId() {
    return profileId;
  }

  @JsonProperty("profileId")
  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  /**
   **/
  public AgentRunSummary cpuUsagePercent(Double cpuUsagePercent) {
    this.cpuUsagePercent = cpuUsagePercent;
    return this;
  }

  
  @ApiModelProperty(example = "42.0", value = "")
  @JsonProperty("cpuUsagePercent")
  public Double getCpuUsagePercent() {
    return cpuUsagePercent;
  }

  @JsonProperty("cpuUsagePercent")
  public void setCpuUsagePercent(Double cpuUsagePercent) {
    this.cpuUsagePercent = cpuUsagePercent;
  }

  /**
   **/
  public AgentRunSummary memoryUsagePercent(Double memoryUsagePercent) {
    this.memoryUsagePercent = memoryUsagePercent;
    return this;
  }

  
  @ApiModelProperty(example = "68.0", value = "")
  @JsonProperty("memoryUsagePercent")
  public Double getMemoryUsagePercent() {
    return memoryUsagePercent;
  }

  @JsonProperty("memoryUsagePercent")
  public void setMemoryUsagePercent(Double memoryUsagePercent) {
    this.memoryUsagePercent = memoryUsagePercent;
  }

  /**
   **/
  public AgentRunSummary generatedOutputs(Long generatedOutputs) {
    this.generatedOutputs = generatedOutputs;
    return this;
  }

  
  @ApiModelProperty(example = "38", value = "")
  @JsonProperty("generatedOutputs")
  public Long getGeneratedOutputs() {
    return generatedOutputs;
  }

  @JsonProperty("generatedOutputs")
  public void setGeneratedOutputs(Long generatedOutputs) {
    this.generatedOutputs = generatedOutputs;
  }

  /**
   **/
  public AgentRunSummary createdSuggestions(Long createdSuggestions) {
    this.createdSuggestions = createdSuggestions;
    return this;
  }

  
  @ApiModelProperty(example = "24", value = "")
  @JsonProperty("createdSuggestions")
  public Long getCreatedSuggestions() {
    return createdSuggestions;
  }

  @JsonProperty("createdSuggestions")
  public void setCreatedSuggestions(Long createdSuggestions) {
    this.createdSuggestions = createdSuggestions;
  }

  /**
   **/
  public AgentRunSummary lastHeartbeatAt(OffsetDateTime lastHeartbeatAt) {
    this.lastHeartbeatAt = lastHeartbeatAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastHeartbeatAt")
  public OffsetDateTime getLastHeartbeatAt() {
    return lastHeartbeatAt;
  }

  @JsonProperty("lastHeartbeatAt")
  public void setLastHeartbeatAt(OffsetDateTime lastHeartbeatAt) {
    this.lastHeartbeatAt = lastHeartbeatAt;
  }

  /**
   **/
  public AgentRunSummary startedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "startedAt")
  @NotNull public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  @JsonProperty(required = true, value = "startedAt")
  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  /**
   **/
  public AgentRunSummary stoppedAt(OffsetDateTime stoppedAt) {
    this.stoppedAt = stoppedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stoppedAt")
  public OffsetDateTime getStoppedAt() {
    return stoppedAt;
  }

  @JsonProperty("stoppedAt")
  public void setStoppedAt(OffsetDateTime stoppedAt) {
    this.stoppedAt = stoppedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRunSummary agentRunSummary = (AgentRunSummary) o;
    return Objects.equals(this.id, agentRunSummary.id) &&
        Objects.equals(this.agentDefinitionId, agentRunSummary.agentDefinitionId) &&
        Objects.equals(this.agentName, agentRunSummary.agentName) &&
        Objects.equals(this.alert, agentRunSummary.alert) &&
        Objects.equals(this.status, agentRunSummary.status) &&
        Objects.equals(this.healthStatus, agentRunSummary.healthStatus) &&
        Objects.equals(this.healthScore, agentRunSummary.healthScore) &&
        Objects.equals(this.qualityStatus, agentRunSummary.qualityStatus) &&
        Objects.equals(this.qualityScore, agentRunSummary.qualityScore) &&
        Objects.equals(this.profileId, agentRunSummary.profileId) &&
        Objects.equals(this.cpuUsagePercent, agentRunSummary.cpuUsagePercent) &&
        Objects.equals(this.memoryUsagePercent, agentRunSummary.memoryUsagePercent) &&
        Objects.equals(this.generatedOutputs, agentRunSummary.generatedOutputs) &&
        Objects.equals(this.createdSuggestions, agentRunSummary.createdSuggestions) &&
        Objects.equals(this.lastHeartbeatAt, agentRunSummary.lastHeartbeatAt) &&
        Objects.equals(this.startedAt, agentRunSummary.startedAt) &&
        Objects.equals(this.stoppedAt, agentRunSummary.stoppedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, agentDefinitionId, agentName, alert, status, healthStatus, healthScore, qualityStatus, qualityScore, profileId, cpuUsagePercent, memoryUsagePercent, generatedOutputs, createdSuggestions, lastHeartbeatAt, startedAt, stoppedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRunSummary {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    agentName: ").append(toIndentedString(agentName)).append("\n");
    sb.append("    alert: ").append(toIndentedString(alert)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    healthStatus: ").append(toIndentedString(healthStatus)).append("\n");
    sb.append("    healthScore: ").append(toIndentedString(healthScore)).append("\n");
    sb.append("    qualityStatus: ").append(toIndentedString(qualityStatus)).append("\n");
    sb.append("    qualityScore: ").append(toIndentedString(qualityScore)).append("\n");
    sb.append("    profileId: ").append(toIndentedString(profileId)).append("\n");
    sb.append("    cpuUsagePercent: ").append(toIndentedString(cpuUsagePercent)).append("\n");
    sb.append("    memoryUsagePercent: ").append(toIndentedString(memoryUsagePercent)).append("\n");
    sb.append("    generatedOutputs: ").append(toIndentedString(generatedOutputs)).append("\n");
    sb.append("    createdSuggestions: ").append(toIndentedString(createdSuggestions)).append("\n");
    sb.append("    lastHeartbeatAt: ").append(toIndentedString(lastHeartbeatAt)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    stoppedAt: ").append(toIndentedString(stoppedAt)).append("\n");
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
