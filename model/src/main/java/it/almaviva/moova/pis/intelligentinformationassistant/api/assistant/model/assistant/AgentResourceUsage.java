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



@JsonTypeName("AgentResourceUsage")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentResourceUsage   {
  private Double cpuMillicores;
  private Integer cpuRequestMillicores;
  private Integer cpuLimitMillicores;
  private Double cpuUsagePercent;
  private Long memoryBytes;
  private Long memoryLimitBytes;
  private Double memoryUsagePercent;
  private Long networkRxBytes;
  private Long networkTxBytes;

  public AgentResourceUsage() {
  }

  /**
   **/
  public AgentResourceUsage cpuMillicores(Double cpuMillicores) {
    this.cpuMillicores = cpuMillicores;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("cpuMillicores")
  public Double getCpuMillicores() {
    return cpuMillicores;
  }

  @JsonProperty("cpuMillicores")
  public void setCpuMillicores(Double cpuMillicores) {
    this.cpuMillicores = cpuMillicores;
  }

  /**
   **/
  public AgentResourceUsage cpuRequestMillicores(Integer cpuRequestMillicores) {
    this.cpuRequestMillicores = cpuRequestMillicores;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("cpuRequestMillicores")
  public Integer getCpuRequestMillicores() {
    return cpuRequestMillicores;
  }

  @JsonProperty("cpuRequestMillicores")
  public void setCpuRequestMillicores(Integer cpuRequestMillicores) {
    this.cpuRequestMillicores = cpuRequestMillicores;
  }

  /**
   **/
  public AgentResourceUsage cpuLimitMillicores(Integer cpuLimitMillicores) {
    this.cpuLimitMillicores = cpuLimitMillicores;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("cpuLimitMillicores")
  public Integer getCpuLimitMillicores() {
    return cpuLimitMillicores;
  }

  @JsonProperty("cpuLimitMillicores")
  public void setCpuLimitMillicores(Integer cpuLimitMillicores) {
    this.cpuLimitMillicores = cpuLimitMillicores;
  }

  /**
   **/
  public AgentResourceUsage cpuUsagePercent(Double cpuUsagePercent) {
    this.cpuUsagePercent = cpuUsagePercent;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public AgentResourceUsage memoryBytes(Long memoryBytes) {
    this.memoryBytes = memoryBytes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("memoryBytes")
  public Long getMemoryBytes() {
    return memoryBytes;
  }

  @JsonProperty("memoryBytes")
  public void setMemoryBytes(Long memoryBytes) {
    this.memoryBytes = memoryBytes;
  }

  /**
   **/
  public AgentResourceUsage memoryLimitBytes(Long memoryLimitBytes) {
    this.memoryLimitBytes = memoryLimitBytes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("memoryLimitBytes")
  public Long getMemoryLimitBytes() {
    return memoryLimitBytes;
  }

  @JsonProperty("memoryLimitBytes")
  public void setMemoryLimitBytes(Long memoryLimitBytes) {
    this.memoryLimitBytes = memoryLimitBytes;
  }

  /**
   **/
  public AgentResourceUsage memoryUsagePercent(Double memoryUsagePercent) {
    this.memoryUsagePercent = memoryUsagePercent;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public AgentResourceUsage networkRxBytes(Long networkRxBytes) {
    this.networkRxBytes = networkRxBytes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("networkRxBytes")
  public Long getNetworkRxBytes() {
    return networkRxBytes;
  }

  @JsonProperty("networkRxBytes")
  public void setNetworkRxBytes(Long networkRxBytes) {
    this.networkRxBytes = networkRxBytes;
  }

  /**
   **/
  public AgentResourceUsage networkTxBytes(Long networkTxBytes) {
    this.networkTxBytes = networkTxBytes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("networkTxBytes")
  public Long getNetworkTxBytes() {
    return networkTxBytes;
  }

  @JsonProperty("networkTxBytes")
  public void setNetworkTxBytes(Long networkTxBytes) {
    this.networkTxBytes = networkTxBytes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentResourceUsage agentResourceUsage = (AgentResourceUsage) o;
    return Objects.equals(this.cpuMillicores, agentResourceUsage.cpuMillicores) &&
        Objects.equals(this.cpuRequestMillicores, agentResourceUsage.cpuRequestMillicores) &&
        Objects.equals(this.cpuLimitMillicores, agentResourceUsage.cpuLimitMillicores) &&
        Objects.equals(this.cpuUsagePercent, agentResourceUsage.cpuUsagePercent) &&
        Objects.equals(this.memoryBytes, agentResourceUsage.memoryBytes) &&
        Objects.equals(this.memoryLimitBytes, agentResourceUsage.memoryLimitBytes) &&
        Objects.equals(this.memoryUsagePercent, agentResourceUsage.memoryUsagePercent) &&
        Objects.equals(this.networkRxBytes, agentResourceUsage.networkRxBytes) &&
        Objects.equals(this.networkTxBytes, agentResourceUsage.networkTxBytes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpuMillicores, cpuRequestMillicores, cpuLimitMillicores, cpuUsagePercent, memoryBytes, memoryLimitBytes, memoryUsagePercent, networkRxBytes, networkTxBytes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentResourceUsage {\n");
    
    sb.append("    cpuMillicores: ").append(toIndentedString(cpuMillicores)).append("\n");
    sb.append("    cpuRequestMillicores: ").append(toIndentedString(cpuRequestMillicores)).append("\n");
    sb.append("    cpuLimitMillicores: ").append(toIndentedString(cpuLimitMillicores)).append("\n");
    sb.append("    cpuUsagePercent: ").append(toIndentedString(cpuUsagePercent)).append("\n");
    sb.append("    memoryBytes: ").append(toIndentedString(memoryBytes)).append("\n");
    sb.append("    memoryLimitBytes: ").append(toIndentedString(memoryLimitBytes)).append("\n");
    sb.append("    memoryUsagePercent: ").append(toIndentedString(memoryUsagePercent)).append("\n");
    sb.append("    networkRxBytes: ").append(toIndentedString(networkRxBytes)).append("\n");
    sb.append("    networkTxBytes: ").append(toIndentedString(networkTxBytes)).append("\n");
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
