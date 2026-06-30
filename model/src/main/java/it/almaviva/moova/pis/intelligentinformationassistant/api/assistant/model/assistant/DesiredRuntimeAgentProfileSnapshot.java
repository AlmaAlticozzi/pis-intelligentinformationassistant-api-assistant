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
 * Immutable Agent Profile runtime snapshot used by activation and reconciliation.
 **/
@ApiModel(description = "Immutable Agent Profile runtime snapshot used by activation and reconciliation.")
@JsonTypeName("DesiredRuntimeAgentProfileSnapshot")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeAgentProfileSnapshot   {
  private String id;
  private String name;
  private Boolean enabled;
  private Integer cpuRequestMillicores;
  private Integer cpuLimitMillicores;
  private Integer memoryRequestMiB;
  private Integer memoryLimitMiB;
  private String networkPolicy;
  private String runtimeClass;
  private Integer maxRuntimeConcurrency;
  private Integer minScheduleIntervalSeconds;
  private Integer maxConditionNodes;
  private Boolean supportsStatefulExecution;
  private Integer maxScheduledQueryItems;
  private Integer maxDataSourceBindings;

  public DesiredRuntimeAgentProfileSnapshot() {
  }

  @JsonCreator
  public DesiredRuntimeAgentProfileSnapshot(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "enabled") Boolean enabled,
    @JsonProperty(required = true, value = "cpuRequestMillicores") Integer cpuRequestMillicores,
    @JsonProperty(required = true, value = "cpuLimitMillicores") Integer cpuLimitMillicores,
    @JsonProperty(required = true, value = "memoryRequestMiB") Integer memoryRequestMiB,
    @JsonProperty(required = true, value = "memoryLimitMiB") Integer memoryLimitMiB,
    @JsonProperty(required = true, value = "networkPolicy") String networkPolicy,
    @JsonProperty(required = true, value = "runtimeClass") String runtimeClass
  ) {
    this.id = id;
    this.name = name;
    this.enabled = enabled;
    this.cpuRequestMillicores = cpuRequestMillicores;
    this.cpuLimitMillicores = cpuLimitMillicores;
    this.memoryRequestMiB = memoryRequestMiB;
    this.memoryLimitMiB = memoryLimitMiB;
    this.networkPolicy = networkPolicy;
    this.runtimeClass = runtimeClass;
  }

  /**
   **/
  public DesiredRuntimeAgentProfileSnapshot id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "id")
  @NotNull  @Size(min=1,max=50)public String getId() {
    return id;
  }

  @JsonProperty(required = true, value = "id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public DesiredRuntimeAgentProfileSnapshot name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "name")
  @NotNull  @Size(min=1,max=120)public String getName() {
    return name;
  }

  @JsonProperty(required = true, value = "name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public DesiredRuntimeAgentProfileSnapshot enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "enabled")
  @NotNull public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty(required = true, value = "enabled")
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * minimum: 0
   **/
  public DesiredRuntimeAgentProfileSnapshot cpuRequestMillicores(Integer cpuRequestMillicores) {
    this.cpuRequestMillicores = cpuRequestMillicores;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "cpuRequestMillicores")
  @NotNull  @Min(0)public Integer getCpuRequestMillicores() {
    return cpuRequestMillicores;
  }

  @JsonProperty(required = true, value = "cpuRequestMillicores")
  public void setCpuRequestMillicores(Integer cpuRequestMillicores) {
    this.cpuRequestMillicores = cpuRequestMillicores;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentProfileSnapshot cpuLimitMillicores(Integer cpuLimitMillicores) {
    this.cpuLimitMillicores = cpuLimitMillicores;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "cpuLimitMillicores")
  @NotNull  @Min(1)public Integer getCpuLimitMillicores() {
    return cpuLimitMillicores;
  }

  @JsonProperty(required = true, value = "cpuLimitMillicores")
  public void setCpuLimitMillicores(Integer cpuLimitMillicores) {
    this.cpuLimitMillicores = cpuLimitMillicores;
  }

  /**
   * minimum: 0
   **/
  public DesiredRuntimeAgentProfileSnapshot memoryRequestMiB(Integer memoryRequestMiB) {
    this.memoryRequestMiB = memoryRequestMiB;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "memoryRequestMiB")
  @NotNull  @Min(0)public Integer getMemoryRequestMiB() {
    return memoryRequestMiB;
  }

  @JsonProperty(required = true, value = "memoryRequestMiB")
  public void setMemoryRequestMiB(Integer memoryRequestMiB) {
    this.memoryRequestMiB = memoryRequestMiB;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentProfileSnapshot memoryLimitMiB(Integer memoryLimitMiB) {
    this.memoryLimitMiB = memoryLimitMiB;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "memoryLimitMiB")
  @NotNull  @Min(1)public Integer getMemoryLimitMiB() {
    return memoryLimitMiB;
  }

  @JsonProperty(required = true, value = "memoryLimitMiB")
  public void setMemoryLimitMiB(Integer memoryLimitMiB) {
    this.memoryLimitMiB = memoryLimitMiB;
  }

  /**
   **/
  public DesiredRuntimeAgentProfileSnapshot networkPolicy(String networkPolicy) {
    this.networkPolicy = networkPolicy;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "networkPolicy")
  @NotNull  @Size(min=1,max=100)public String getNetworkPolicy() {
    return networkPolicy;
  }

  @JsonProperty(required = true, value = "networkPolicy")
  public void setNetworkPolicy(String networkPolicy) {
    this.networkPolicy = networkPolicy;
  }

  /**
   **/
  public DesiredRuntimeAgentProfileSnapshot runtimeClass(String runtimeClass) {
    this.runtimeClass = runtimeClass;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "runtimeClass")
  @NotNull  @Size(min=1,max=100)public String getRuntimeClass() {
    return runtimeClass;
  }

  @JsonProperty(required = true, value = "runtimeClass")
  public void setRuntimeClass(String runtimeClass) {
    this.runtimeClass = runtimeClass;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentProfileSnapshot maxRuntimeConcurrency(Integer maxRuntimeConcurrency) {
    this.maxRuntimeConcurrency = maxRuntimeConcurrency;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("maxRuntimeConcurrency")
   @Min(1)public Integer getMaxRuntimeConcurrency() {
    return maxRuntimeConcurrency;
  }

  @JsonProperty("maxRuntimeConcurrency")
  public void setMaxRuntimeConcurrency(Integer maxRuntimeConcurrency) {
    this.maxRuntimeConcurrency = maxRuntimeConcurrency;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentProfileSnapshot minScheduleIntervalSeconds(Integer minScheduleIntervalSeconds) {
    this.minScheduleIntervalSeconds = minScheduleIntervalSeconds;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("minScheduleIntervalSeconds")
   @Min(1)public Integer getMinScheduleIntervalSeconds() {
    return minScheduleIntervalSeconds;
  }

  @JsonProperty("minScheduleIntervalSeconds")
  public void setMinScheduleIntervalSeconds(Integer minScheduleIntervalSeconds) {
    this.minScheduleIntervalSeconds = minScheduleIntervalSeconds;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentProfileSnapshot maxConditionNodes(Integer maxConditionNodes) {
    this.maxConditionNodes = maxConditionNodes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("maxConditionNodes")
   @Min(1)public Integer getMaxConditionNodes() {
    return maxConditionNodes;
  }

  @JsonProperty("maxConditionNodes")
  public void setMaxConditionNodes(Integer maxConditionNodes) {
    this.maxConditionNodes = maxConditionNodes;
  }

  /**
   **/
  public DesiredRuntimeAgentProfileSnapshot supportsStatefulExecution(Boolean supportsStatefulExecution) {
    this.supportsStatefulExecution = supportsStatefulExecution;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("supportsStatefulExecution")
  public Boolean getSupportsStatefulExecution() {
    return supportsStatefulExecution;
  }

  @JsonProperty("supportsStatefulExecution")
  public void setSupportsStatefulExecution(Boolean supportsStatefulExecution) {
    this.supportsStatefulExecution = supportsStatefulExecution;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentProfileSnapshot maxScheduledQueryItems(Integer maxScheduledQueryItems) {
    this.maxScheduledQueryItems = maxScheduledQueryItems;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("maxScheduledQueryItems")
   @Min(1)public Integer getMaxScheduledQueryItems() {
    return maxScheduledQueryItems;
  }

  @JsonProperty("maxScheduledQueryItems")
  public void setMaxScheduledQueryItems(Integer maxScheduledQueryItems) {
    this.maxScheduledQueryItems = maxScheduledQueryItems;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentProfileSnapshot maxDataSourceBindings(Integer maxDataSourceBindings) {
    this.maxDataSourceBindings = maxDataSourceBindings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("maxDataSourceBindings")
   @Min(1)public Integer getMaxDataSourceBindings() {
    return maxDataSourceBindings;
  }

  @JsonProperty("maxDataSourceBindings")
  public void setMaxDataSourceBindings(Integer maxDataSourceBindings) {
    this.maxDataSourceBindings = maxDataSourceBindings;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeAgentProfileSnapshot desiredRuntimeAgentProfileSnapshot = (DesiredRuntimeAgentProfileSnapshot) o;
    return Objects.equals(this.id, desiredRuntimeAgentProfileSnapshot.id) &&
        Objects.equals(this.name, desiredRuntimeAgentProfileSnapshot.name) &&
        Objects.equals(this.enabled, desiredRuntimeAgentProfileSnapshot.enabled) &&
        Objects.equals(this.cpuRequestMillicores, desiredRuntimeAgentProfileSnapshot.cpuRequestMillicores) &&
        Objects.equals(this.cpuLimitMillicores, desiredRuntimeAgentProfileSnapshot.cpuLimitMillicores) &&
        Objects.equals(this.memoryRequestMiB, desiredRuntimeAgentProfileSnapshot.memoryRequestMiB) &&
        Objects.equals(this.memoryLimitMiB, desiredRuntimeAgentProfileSnapshot.memoryLimitMiB) &&
        Objects.equals(this.networkPolicy, desiredRuntimeAgentProfileSnapshot.networkPolicy) &&
        Objects.equals(this.runtimeClass, desiredRuntimeAgentProfileSnapshot.runtimeClass) &&
        Objects.equals(this.maxRuntimeConcurrency, desiredRuntimeAgentProfileSnapshot.maxRuntimeConcurrency) &&
        Objects.equals(this.minScheduleIntervalSeconds, desiredRuntimeAgentProfileSnapshot.minScheduleIntervalSeconds) &&
        Objects.equals(this.maxConditionNodes, desiredRuntimeAgentProfileSnapshot.maxConditionNodes) &&
        Objects.equals(this.supportsStatefulExecution, desiredRuntimeAgentProfileSnapshot.supportsStatefulExecution) &&
        Objects.equals(this.maxScheduledQueryItems, desiredRuntimeAgentProfileSnapshot.maxScheduledQueryItems) &&
        Objects.equals(this.maxDataSourceBindings, desiredRuntimeAgentProfileSnapshot.maxDataSourceBindings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, enabled, cpuRequestMillicores, cpuLimitMillicores, memoryRequestMiB, memoryLimitMiB, networkPolicy, runtimeClass, maxRuntimeConcurrency, minScheduleIntervalSeconds, maxConditionNodes, supportsStatefulExecution, maxScheduledQueryItems, maxDataSourceBindings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeAgentProfileSnapshot {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    cpuRequestMillicores: ").append(toIndentedString(cpuRequestMillicores)).append("\n");
    sb.append("    cpuLimitMillicores: ").append(toIndentedString(cpuLimitMillicores)).append("\n");
    sb.append("    memoryRequestMiB: ").append(toIndentedString(memoryRequestMiB)).append("\n");
    sb.append("    memoryLimitMiB: ").append(toIndentedString(memoryLimitMiB)).append("\n");
    sb.append("    networkPolicy: ").append(toIndentedString(networkPolicy)).append("\n");
    sb.append("    runtimeClass: ").append(toIndentedString(runtimeClass)).append("\n");
    sb.append("    maxRuntimeConcurrency: ").append(toIndentedString(maxRuntimeConcurrency)).append("\n");
    sb.append("    minScheduleIntervalSeconds: ").append(toIndentedString(minScheduleIntervalSeconds)).append("\n");
    sb.append("    maxConditionNodes: ").append(toIndentedString(maxConditionNodes)).append("\n");
    sb.append("    supportsStatefulExecution: ").append(toIndentedString(supportsStatefulExecution)).append("\n");
    sb.append("    maxScheduledQueryItems: ").append(toIndentedString(maxScheduledQueryItems)).append("\n");
    sb.append("    maxDataSourceBindings: ").append(toIndentedString(maxDataSourceBindings)).append("\n");
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
