package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("AgentProfile")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentProfile   {
  private String id;
  private String name;
  private String description;
  private Boolean enabled;
  private @Valid List<String> recommendedFor = new ArrayList<>();
  private Integer cpuRequestMillicores;
  private Integer cpuLimitMillicores;
  private Integer memoryRequestMiB;
  private Integer memoryLimitMiB;
  private String networkPolicy;
  private Integer maxRuntimeConcurrency;
  private @Valid List<AlertInterpreterType> supportedInterpreterTypes = new ArrayList<>();
  private @Valid List<String> supportedTriggerTypes = new ArrayList<>();
  private Integer minScheduleIntervalSeconds;
  private Integer maxServiceDataStopPoints;
  private Integer maxConditionNodes;
  private Boolean supportsStatefulExecution;
  private String runtimeClass;

  public AgentProfile() {
  }

  @JsonCreator
  public AgentProfile(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "enabled") Boolean enabled,
    @JsonProperty(required = true, value = "cpuRequestMillicores") Integer cpuRequestMillicores,
    @JsonProperty(required = true, value = "cpuLimitMillicores") Integer cpuLimitMillicores,
    @JsonProperty(required = true, value = "memoryRequestMiB") Integer memoryRequestMiB,
    @JsonProperty(required = true, value = "memoryLimitMiB") Integer memoryLimitMiB
  ) {
    this.id = id;
    this.name = name;
    this.enabled = enabled;
    this.cpuRequestMillicores = cpuRequestMillicores;
    this.cpuLimitMillicores = cpuLimitMillicores;
    this.memoryRequestMiB = memoryRequestMiB;
    this.memoryLimitMiB = memoryLimitMiB;
  }

  /**
   **/
  public AgentProfile id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "MEDIUM", required = true, value = "")
  @JsonProperty(required = true, value = "id")
  @NotNull public String getId() {
    return id;
  }

  @JsonProperty(required = true, value = "id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public AgentProfile name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Medium Agent", required = true, value = "")
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
  public AgentProfile description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Default profile for ordinary operational monitoring agents.", value = "")
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
  public AgentProfile enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "")
  @JsonProperty(required = true, value = "enabled")
  @NotNull public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty(required = true, value = "enabled")
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public AgentProfile recommendedFor(List<String> recommendedFor) {
    this.recommendedFor = recommendedFor;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Scheduled checks\",\"Moderate event correlation\"]", value = "")
  @JsonProperty("recommendedFor")
  public List<String> getRecommendedFor() {
    return recommendedFor;
  }

  @JsonProperty("recommendedFor")
  public void setRecommendedFor(List<String> recommendedFor) {
    this.recommendedFor = recommendedFor;
  }

  public AgentProfile addRecommendedForItem(String recommendedForItem) {
    if (this.recommendedFor == null) {
      this.recommendedFor = new ArrayList<>();
    }

    this.recommendedFor.add(recommendedForItem);
    return this;
  }

  public AgentProfile removeRecommendedForItem(String recommendedForItem) {
    if (recommendedForItem != null && this.recommendedFor != null) {
      this.recommendedFor.remove(recommendedForItem);
    }

    return this;
  }
  /**
   **/
  public AgentProfile cpuRequestMillicores(Integer cpuRequestMillicores) {
    this.cpuRequestMillicores = cpuRequestMillicores;
    return this;
  }

  
  @ApiModelProperty(example = "250", required = true, value = "")
  @JsonProperty(required = true, value = "cpuRequestMillicores")
  @NotNull public Integer getCpuRequestMillicores() {
    return cpuRequestMillicores;
  }

  @JsonProperty(required = true, value = "cpuRequestMillicores")
  public void setCpuRequestMillicores(Integer cpuRequestMillicores) {
    this.cpuRequestMillicores = cpuRequestMillicores;
  }

  /**
   **/
  public AgentProfile cpuLimitMillicores(Integer cpuLimitMillicores) {
    this.cpuLimitMillicores = cpuLimitMillicores;
    return this;
  }

  
  @ApiModelProperty(example = "700", required = true, value = "")
  @JsonProperty(required = true, value = "cpuLimitMillicores")
  @NotNull public Integer getCpuLimitMillicores() {
    return cpuLimitMillicores;
  }

  @JsonProperty(required = true, value = "cpuLimitMillicores")
  public void setCpuLimitMillicores(Integer cpuLimitMillicores) {
    this.cpuLimitMillicores = cpuLimitMillicores;
  }

  /**
   **/
  public AgentProfile memoryRequestMiB(Integer memoryRequestMiB) {
    this.memoryRequestMiB = memoryRequestMiB;
    return this;
  }

  
  @ApiModelProperty(example = "256", required = true, value = "")
  @JsonProperty(required = true, value = "memoryRequestMiB")
  @NotNull public Integer getMemoryRequestMiB() {
    return memoryRequestMiB;
  }

  @JsonProperty(required = true, value = "memoryRequestMiB")
  public void setMemoryRequestMiB(Integer memoryRequestMiB) {
    this.memoryRequestMiB = memoryRequestMiB;
  }

  /**
   **/
  public AgentProfile memoryLimitMiB(Integer memoryLimitMiB) {
    this.memoryLimitMiB = memoryLimitMiB;
    return this;
  }

  
  @ApiModelProperty(example = "768", required = true, value = "")
  @JsonProperty(required = true, value = "memoryLimitMiB")
  @NotNull public Integer getMemoryLimitMiB() {
    return memoryLimitMiB;
  }

  @JsonProperty(required = true, value = "memoryLimitMiB")
  public void setMemoryLimitMiB(Integer memoryLimitMiB) {
    this.memoryLimitMiB = memoryLimitMiB;
  }

  /**
   **/
  public AgentProfile networkPolicy(String networkPolicy) {
    this.networkPolicy = networkPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "TOOL_GATEWAY_ONLY", value = "")
  @JsonProperty("networkPolicy")
  public String getNetworkPolicy() {
    return networkPolicy;
  }

  @JsonProperty("networkPolicy")
  public void setNetworkPolicy(String networkPolicy) {
    this.networkPolicy = networkPolicy;
  }

  /**
   **/
  public AgentProfile maxRuntimeConcurrency(Integer maxRuntimeConcurrency) {
    this.maxRuntimeConcurrency = maxRuntimeConcurrency;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("maxRuntimeConcurrency")
  public Integer getMaxRuntimeConcurrency() {
    return maxRuntimeConcurrency;
  }

  @JsonProperty("maxRuntimeConcurrency")
  public void setMaxRuntimeConcurrency(Integer maxRuntimeConcurrency) {
    this.maxRuntimeConcurrency = maxRuntimeConcurrency;
  }

  /**
   * Optional list of interpreter types supported by this profile. Empty or omitted means backend default policy applies.
   **/
  public AgentProfile supportedInterpreterTypes(List<AlertInterpreterType> supportedInterpreterTypes) {
    this.supportedInterpreterTypes = supportedInterpreterTypes;
    return this;
  }

  
  @ApiModelProperty(value = "Optional list of interpreter types supported by this profile. Empty or omitted means backend default policy applies.")
  @JsonProperty("supportedInterpreterTypes")
  public List<AlertInterpreterType> getSupportedInterpreterTypes() {
    return supportedInterpreterTypes;
  }

  @JsonProperty("supportedInterpreterTypes")
  public void setSupportedInterpreterTypes(List<AlertInterpreterType> supportedInterpreterTypes) {
    this.supportedInterpreterTypes = supportedInterpreterTypes;
  }

  /**
   * Optional list of trigger types supported by this profile.
   **/
  public AgentProfile supportedTriggerTypes(List<String> supportedTriggerTypes) {
    this.supportedTriggerTypes = supportedTriggerTypes;
    return this;
  }

  
  @ApiModelProperty(value = "Optional list of trigger types supported by this profile.")
  @JsonProperty("supportedTriggerTypes")
  public List<String> getSupportedTriggerTypes() {
    return supportedTriggerTypes;
  }

  @JsonProperty("supportedTriggerTypes")
  public void setSupportedTriggerTypes(List<String> supportedTriggerTypes) {
    this.supportedTriggerTypes = supportedTriggerTypes;
  }

  /**
   * Minimum allowed interval for SCHEDULED_INTERPRETER definitions using this profile.
   **/
  public AgentProfile minScheduleIntervalSeconds(Integer minScheduleIntervalSeconds) {
    this.minScheduleIntervalSeconds = minScheduleIntervalSeconds;
    return this;
  }

  
  @ApiModelProperty(example = "300", value = "Minimum allowed interval for SCHEDULED_INTERPRETER definitions using this profile.")
  @JsonProperty("minScheduleIntervalSeconds")
  public Integer getMinScheduleIntervalSeconds() {
    return minScheduleIntervalSeconds;
  }

  @JsonProperty("minScheduleIntervalSeconds")
  public void setMinScheduleIntervalSeconds(Integer minScheduleIntervalSeconds) {
    this.minScheduleIntervalSeconds = minScheduleIntervalSeconds;
  }

  /**
   * Maximum number of ServiceData stopPoint identifiers that a scheduled definition can query when this profile is selected.
   **/
  public AgentProfile maxServiceDataStopPoints(Integer maxServiceDataStopPoints) {
    this.maxServiceDataStopPoints = maxServiceDataStopPoints;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Maximum number of ServiceData stopPoint identifiers that a scheduled definition can query when this profile is selected.")
  @JsonProperty("maxServiceDataStopPoints")
  public Integer getMaxServiceDataStopPoints() {
    return maxServiceDataStopPoints;
  }

  @JsonProperty("maxServiceDataStopPoints")
  public void setMaxServiceDataStopPoints(Integer maxServiceDataStopPoints) {
    this.maxServiceDataStopPoints = maxServiceDataStopPoints;
  }

  /**
   * Maximum number of DSL condition/evaluation nodes allowed for this profile.
   **/
  public AgentProfile maxConditionNodes(Integer maxConditionNodes) {
    this.maxConditionNodes = maxConditionNodes;
    return this;
  }

  
  @ApiModelProperty(example = "50", value = "Maximum number of DSL condition/evaluation nodes allowed for this profile.")
  @JsonProperty("maxConditionNodes")
  public Integer getMaxConditionNodes() {
    return maxConditionNodes;
  }

  @JsonProperty("maxConditionNodes")
  public void setMaxConditionNodes(Integer maxConditionNodes) {
    this.maxConditionNodes = maxConditionNodes;
  }

  /**
   * Whether this profile allows future stateful Agent execution.
   **/
  public AgentProfile supportsStatefulExecution(Boolean supportsStatefulExecution) {
    this.supportsStatefulExecution = supportsStatefulExecution;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Whether this profile allows future stateful Agent execution.")
  @JsonProperty("supportsStatefulExecution")
  public Boolean getSupportsStatefulExecution() {
    return supportsStatefulExecution;
  }

  @JsonProperty("supportsStatefulExecution")
  public void setSupportsStatefulExecution(Boolean supportsStatefulExecution) {
    this.supportsStatefulExecution = supportsStatefulExecution;
  }

  /**
   * Logical runtime class used by the Agent Orchestrator when deciding where to load ACTIVE definitions.
   **/
  public AgentProfile runtimeClass(String runtimeClass) {
    this.runtimeClass = runtimeClass;
    return this;
  }

  
  @ApiModelProperty(example = "STANDARD_DSL_RUNTIME", value = "Logical runtime class used by the Agent Orchestrator when deciding where to load ACTIVE definitions.")
  @JsonProperty("runtimeClass")
  public String getRuntimeClass() {
    return runtimeClass;
  }

  @JsonProperty("runtimeClass")
  public void setRuntimeClass(String runtimeClass) {
    this.runtimeClass = runtimeClass;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentProfile agentProfile = (AgentProfile) o;
    return Objects.equals(this.id, agentProfile.id) &&
        Objects.equals(this.name, agentProfile.name) &&
        Objects.equals(this.description, agentProfile.description) &&
        Objects.equals(this.enabled, agentProfile.enabled) &&
        Objects.equals(this.recommendedFor, agentProfile.recommendedFor) &&
        Objects.equals(this.cpuRequestMillicores, agentProfile.cpuRequestMillicores) &&
        Objects.equals(this.cpuLimitMillicores, agentProfile.cpuLimitMillicores) &&
        Objects.equals(this.memoryRequestMiB, agentProfile.memoryRequestMiB) &&
        Objects.equals(this.memoryLimitMiB, agentProfile.memoryLimitMiB) &&
        Objects.equals(this.networkPolicy, agentProfile.networkPolicy) &&
        Objects.equals(this.maxRuntimeConcurrency, agentProfile.maxRuntimeConcurrency) &&
        Objects.equals(this.supportedInterpreterTypes, agentProfile.supportedInterpreterTypes) &&
        Objects.equals(this.supportedTriggerTypes, agentProfile.supportedTriggerTypes) &&
        Objects.equals(this.minScheduleIntervalSeconds, agentProfile.minScheduleIntervalSeconds) &&
        Objects.equals(this.maxServiceDataStopPoints, agentProfile.maxServiceDataStopPoints) &&
        Objects.equals(this.maxConditionNodes, agentProfile.maxConditionNodes) &&
        Objects.equals(this.supportsStatefulExecution, agentProfile.supportsStatefulExecution) &&
        Objects.equals(this.runtimeClass, agentProfile.runtimeClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, enabled, recommendedFor, cpuRequestMillicores, cpuLimitMillicores, memoryRequestMiB, memoryLimitMiB, networkPolicy, maxRuntimeConcurrency, supportedInterpreterTypes, supportedTriggerTypes, minScheduleIntervalSeconds, maxServiceDataStopPoints, maxConditionNodes, supportsStatefulExecution, runtimeClass);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentProfile {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    recommendedFor: ").append(toIndentedString(recommendedFor)).append("\n");
    sb.append("    cpuRequestMillicores: ").append(toIndentedString(cpuRequestMillicores)).append("\n");
    sb.append("    cpuLimitMillicores: ").append(toIndentedString(cpuLimitMillicores)).append("\n");
    sb.append("    memoryRequestMiB: ").append(toIndentedString(memoryRequestMiB)).append("\n");
    sb.append("    memoryLimitMiB: ").append(toIndentedString(memoryLimitMiB)).append("\n");
    sb.append("    networkPolicy: ").append(toIndentedString(networkPolicy)).append("\n");
    sb.append("    maxRuntimeConcurrency: ").append(toIndentedString(maxRuntimeConcurrency)).append("\n");
    sb.append("    supportedInterpreterTypes: ").append(toIndentedString(supportedInterpreterTypes)).append("\n");
    sb.append("    supportedTriggerTypes: ").append(toIndentedString(supportedTriggerTypes)).append("\n");
    sb.append("    minScheduleIntervalSeconds: ").append(toIndentedString(minScheduleIntervalSeconds)).append("\n");
    sb.append("    maxServiceDataStopPoints: ").append(toIndentedString(maxServiceDataStopPoints)).append("\n");
    sb.append("    maxConditionNodes: ").append(toIndentedString(maxConditionNodes)).append("\n");
    sb.append("    supportsStatefulExecution: ").append(toIndentedString(supportsStatefulExecution)).append("\n");
    sb.append("    runtimeClass: ").append(toIndentedString(runtimeClass)).append("\n");
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
