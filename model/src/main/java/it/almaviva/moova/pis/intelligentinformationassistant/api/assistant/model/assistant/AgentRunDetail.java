package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentFunctionalMetrics;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentHealthStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentKubernetesRuntimeRef;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentQualityStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentResourceUsage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRunStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimeRef;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimeEvent;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentRunDetail")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentRunDetail   {
  private String id;
  private AgentDefinitionSummary agentDefinition;
  private AgentRunStatus status;
  private AgentHealthStatus healthStatus;
  private Integer healthScore;
  private AgentQualityStatus qualityStatus;
  private Integer qualityScore;
  private @Valid List<String> mainIssues = new ArrayList<>();
  private AgentKubernetesRuntimeRef kubernetes;
  private AgentResourceUsage resources;
  private AgentFunctionalMetrics functionalMetrics;
  private @Valid List<@Valid AgentRuntimeEvent> latestEvents = new ArrayList<>();
  private OffsetDateTime startedAt;
  private OffsetDateTime stoppedAt;
  private OffsetDateTime lastHeartbeatAt;
  private OffsetDateTime lastErrorAt;
  private String lastErrorCode;
  private String lastErrorMessage;
  private AgentRuntimeRef runtime;

  public AgentRunDetail() {
  }

  @JsonCreator
  public AgentRunDetail(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "agentDefinition") AgentDefinitionSummary agentDefinition,
    @JsonProperty(required = true, value = "status") AgentRunStatus status
  ) {
    this.id = id;
    this.agentDefinition = agentDefinition;
    this.status = status;
  }

  /**
   **/
  public AgentRunDetail id(String id) {
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
  public AgentRunDetail agentDefinition(AgentDefinitionSummary agentDefinition) {
    this.agentDefinition = agentDefinition;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentDefinition")
  @NotNull @Valid public AgentDefinitionSummary getAgentDefinition() {
    return agentDefinition;
  }

  @JsonProperty(required = true, value = "agentDefinition")
  public void setAgentDefinition(AgentDefinitionSummary agentDefinition) {
    this.agentDefinition = agentDefinition;
  }

  /**
   **/
  public AgentRunDetail status(AgentRunStatus status) {
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
  public AgentRunDetail healthStatus(AgentHealthStatus healthStatus) {
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
  public AgentRunDetail healthScore(Integer healthScore) {
    this.healthScore = healthScore;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public AgentRunDetail qualityStatus(AgentQualityStatus qualityStatus) {
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
  public AgentRunDetail qualityScore(Integer qualityScore) {
    this.qualityScore = qualityScore;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public AgentRunDetail mainIssues(List<String> mainIssues) {
    this.mainIssues = mainIssues;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("mainIssues")
  public List<String> getMainIssues() {
    return mainIssues;
  }

  @JsonProperty("mainIssues")
  public void setMainIssues(List<String> mainIssues) {
    this.mainIssues = mainIssues;
  }

  public AgentRunDetail addMainIssuesItem(String mainIssuesItem) {
    if (this.mainIssues == null) {
      this.mainIssues = new ArrayList<>();
    }

    this.mainIssues.add(mainIssuesItem);
    return this;
  }

  public AgentRunDetail removeMainIssuesItem(String mainIssuesItem) {
    if (mainIssuesItem != null && this.mainIssues != null) {
      this.mainIssues.remove(mainIssuesItem);
    }

    return this;
  }
  /**
   **/
  public AgentRunDetail kubernetes(AgentKubernetesRuntimeRef kubernetes) {
    this.kubernetes = kubernetes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("kubernetes")
  @Valid public AgentKubernetesRuntimeRef getKubernetes() {
    return kubernetes;
  }

  @JsonProperty("kubernetes")
  public void setKubernetes(AgentKubernetesRuntimeRef kubernetes) {
    this.kubernetes = kubernetes;
  }

  /**
   **/
  public AgentRunDetail resources(AgentResourceUsage resources) {
    this.resources = resources;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("resources")
  @Valid public AgentResourceUsage getResources() {
    return resources;
  }

  @JsonProperty("resources")
  public void setResources(AgentResourceUsage resources) {
    this.resources = resources;
  }

  /**
   **/
  public AgentRunDetail functionalMetrics(AgentFunctionalMetrics functionalMetrics) {
    this.functionalMetrics = functionalMetrics;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("functionalMetrics")
  @Valid public AgentFunctionalMetrics getFunctionalMetrics() {
    return functionalMetrics;
  }

  @JsonProperty("functionalMetrics")
  public void setFunctionalMetrics(AgentFunctionalMetrics functionalMetrics) {
    this.functionalMetrics = functionalMetrics;
  }

  /**
   **/
  public AgentRunDetail latestEvents(List<@Valid AgentRuntimeEvent> latestEvents) {
    this.latestEvents = latestEvents;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("latestEvents")
  @Valid public List<@Valid AgentRuntimeEvent> getLatestEvents() {
    return latestEvents;
  }

  @JsonProperty("latestEvents")
  public void setLatestEvents(List<@Valid AgentRuntimeEvent> latestEvents) {
    this.latestEvents = latestEvents;
  }

  public AgentRunDetail addLatestEventsItem(AgentRuntimeEvent latestEventsItem) {
    if (this.latestEvents == null) {
      this.latestEvents = new ArrayList<>();
    }

    this.latestEvents.add(latestEventsItem);
    return this;
  }

  public AgentRunDetail removeLatestEventsItem(AgentRuntimeEvent latestEventsItem) {
    if (latestEventsItem != null && this.latestEvents != null) {
      this.latestEvents.remove(latestEventsItem);
    }

    return this;
  }
  /**
   **/
  public AgentRunDetail startedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("startedAt")
  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  @JsonProperty("startedAt")
  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  /**
   **/
  public AgentRunDetail stoppedAt(OffsetDateTime stoppedAt) {
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

  /**
   **/
  public AgentRunDetail lastHeartbeatAt(OffsetDateTime lastHeartbeatAt) {
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
  public AgentRunDetail lastErrorAt(OffsetDateTime lastErrorAt) {
    this.lastErrorAt = lastErrorAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastErrorAt")
  public OffsetDateTime getLastErrorAt() {
    return lastErrorAt;
  }

  @JsonProperty("lastErrorAt")
  public void setLastErrorAt(OffsetDateTime lastErrorAt) {
    this.lastErrorAt = lastErrorAt;
  }

  /**
   **/
  public AgentRunDetail lastErrorCode(String lastErrorCode) {
    this.lastErrorCode = lastErrorCode;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastErrorCode")
  public String getLastErrorCode() {
    return lastErrorCode;
  }

  @JsonProperty("lastErrorCode")
  public void setLastErrorCode(String lastErrorCode) {
    this.lastErrorCode = lastErrorCode;
  }

  /**
   **/
  public AgentRunDetail lastErrorMessage(String lastErrorMessage) {
    this.lastErrorMessage = lastErrorMessage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastErrorMessage")
  public String getLastErrorMessage() {
    return lastErrorMessage;
  }

  @JsonProperty("lastErrorMessage")
  public void setLastErrorMessage(String lastErrorMessage) {
    this.lastErrorMessage = lastErrorMessage;
  }

  /**
   **/
  public AgentRunDetail runtime(AgentRuntimeRef runtime) {
    this.runtime = runtime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("runtime")
  @Valid public AgentRuntimeRef getRuntime() {
    return runtime;
  }

  @JsonProperty("runtime")
  public void setRuntime(AgentRuntimeRef runtime) {
    this.runtime = runtime;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRunDetail agentRunDetail = (AgentRunDetail) o;
    return Objects.equals(this.id, agentRunDetail.id) &&
        Objects.equals(this.agentDefinition, agentRunDetail.agentDefinition) &&
        Objects.equals(this.status, agentRunDetail.status) &&
        Objects.equals(this.healthStatus, agentRunDetail.healthStatus) &&
        Objects.equals(this.healthScore, agentRunDetail.healthScore) &&
        Objects.equals(this.qualityStatus, agentRunDetail.qualityStatus) &&
        Objects.equals(this.qualityScore, agentRunDetail.qualityScore) &&
        Objects.equals(this.mainIssues, agentRunDetail.mainIssues) &&
        Objects.equals(this.kubernetes, agentRunDetail.kubernetes) &&
        Objects.equals(this.resources, agentRunDetail.resources) &&
        Objects.equals(this.functionalMetrics, agentRunDetail.functionalMetrics) &&
        Objects.equals(this.latestEvents, agentRunDetail.latestEvents) &&
        Objects.equals(this.startedAt, agentRunDetail.startedAt) &&
        Objects.equals(this.stoppedAt, agentRunDetail.stoppedAt) &&
        Objects.equals(this.lastHeartbeatAt, agentRunDetail.lastHeartbeatAt) &&
        Objects.equals(this.lastErrorAt, agentRunDetail.lastErrorAt) &&
        Objects.equals(this.lastErrorCode, agentRunDetail.lastErrorCode) &&
        Objects.equals(this.lastErrorMessage, agentRunDetail.lastErrorMessage) &&
        Objects.equals(this.runtime, agentRunDetail.runtime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, agentDefinition, status, healthStatus, healthScore, qualityStatus, qualityScore, mainIssues, kubernetes, resources, functionalMetrics, latestEvents, startedAt, stoppedAt, lastHeartbeatAt, lastErrorAt, lastErrorCode, lastErrorMessage, runtime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRunDetail {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    agentDefinition: ").append(toIndentedString(agentDefinition)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    healthStatus: ").append(toIndentedString(healthStatus)).append("\n");
    sb.append("    healthScore: ").append(toIndentedString(healthScore)).append("\n");
    sb.append("    qualityStatus: ").append(toIndentedString(qualityStatus)).append("\n");
    sb.append("    qualityScore: ").append(toIndentedString(qualityScore)).append("\n");
    sb.append("    mainIssues: ").append(toIndentedString(mainIssues)).append("\n");
    sb.append("    kubernetes: ").append(toIndentedString(kubernetes)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
    sb.append("    functionalMetrics: ").append(toIndentedString(functionalMetrics)).append("\n");
    sb.append("    latestEvents: ").append(toIndentedString(latestEvents)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    stoppedAt: ").append(toIndentedString(stoppedAt)).append("\n");
    sb.append("    lastHeartbeatAt: ").append(toIndentedString(lastHeartbeatAt)).append("\n");
    sb.append("    lastErrorAt: ").append(toIndentedString(lastErrorAt)).append("\n");
    sb.append("    lastErrorCode: ").append(toIndentedString(lastErrorCode)).append("\n");
    sb.append("    lastErrorMessage: ").append(toIndentedString(lastErrorMessage)).append("\n");
    sb.append("    runtime: ").append(toIndentedString(runtime)).append("\n");
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
