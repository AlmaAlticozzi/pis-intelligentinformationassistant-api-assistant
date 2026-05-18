package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolReference;
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



@JsonTypeName("AgentRuntimeContract")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentRuntimeContract   {
  private String runtimeImage;
  private String sdkVersion;
  private String inputModel;
  private String outputModel;
  private @Valid List<@Valid ToolReference> allowedTools = new ArrayList<>();
  private String networkPolicy;

  public AgentRuntimeContract() {
  }

  /**
   **/
  public AgentRuntimeContract runtimeImage(String runtimeImage) {
    this.runtimeImage = runtimeImage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("runtimeImage")
  public String getRuntimeImage() {
    return runtimeImage;
  }

  @JsonProperty("runtimeImage")
  public void setRuntimeImage(String runtimeImage) {
    this.runtimeImage = runtimeImage;
  }

  /**
   **/
  public AgentRuntimeContract sdkVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sdkVersion")
  public String getSdkVersion() {
    return sdkVersion;
  }

  @JsonProperty("sdkVersion")
  public void setSdkVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
  }

  /**
   **/
  public AgentRuntimeContract inputModel(String inputModel) {
    this.inputModel = inputModel;
    return this;
  }

  
  @ApiModelProperty(example = "AgentContext", value = "")
  @JsonProperty("inputModel")
  public String getInputModel() {
    return inputModel;
  }

  @JsonProperty("inputModel")
  public void setInputModel(String inputModel) {
    this.inputModel = inputModel;
  }

  /**
   **/
  public AgentRuntimeContract outputModel(String outputModel) {
    this.outputModel = outputModel;
    return this;
  }

  
  @ApiModelProperty(example = "AgentOutput", value = "")
  @JsonProperty("outputModel")
  public String getOutputModel() {
    return outputModel;
  }

  @JsonProperty("outputModel")
  public void setOutputModel(String outputModel) {
    this.outputModel = outputModel;
  }

  /**
   **/
  public AgentRuntimeContract allowedTools(List<@Valid ToolReference> allowedTools) {
    this.allowedTools = allowedTools;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("allowedTools")
  @Valid public List<@Valid ToolReference> getAllowedTools() {
    return allowedTools;
  }

  @JsonProperty("allowedTools")
  public void setAllowedTools(List<@Valid ToolReference> allowedTools) {
    this.allowedTools = allowedTools;
  }

  public AgentRuntimeContract addAllowedToolsItem(ToolReference allowedToolsItem) {
    if (this.allowedTools == null) {
      this.allowedTools = new ArrayList<>();
    }

    this.allowedTools.add(allowedToolsItem);
    return this;
  }

  public AgentRuntimeContract removeAllowedToolsItem(ToolReference allowedToolsItem) {
    if (allowedToolsItem != null && this.allowedTools != null) {
      this.allowedTools.remove(allowedToolsItem);
    }

    return this;
  }
  /**
   **/
  public AgentRuntimeContract networkPolicy(String networkPolicy) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRuntimeContract agentRuntimeContract = (AgentRuntimeContract) o;
    return Objects.equals(this.runtimeImage, agentRuntimeContract.runtimeImage) &&
        Objects.equals(this.sdkVersion, agentRuntimeContract.sdkVersion) &&
        Objects.equals(this.inputModel, agentRuntimeContract.inputModel) &&
        Objects.equals(this.outputModel, agentRuntimeContract.outputModel) &&
        Objects.equals(this.allowedTools, agentRuntimeContract.allowedTools) &&
        Objects.equals(this.networkPolicy, agentRuntimeContract.networkPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runtimeImage, sdkVersion, inputModel, outputModel, allowedTools, networkPolicy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRuntimeContract {\n");
    
    sb.append("    runtimeImage: ").append(toIndentedString(runtimeImage)).append("\n");
    sb.append("    sdkVersion: ").append(toIndentedString(sdkVersion)).append("\n");
    sb.append("    inputModel: ").append(toIndentedString(inputModel)).append("\n");
    sb.append("    outputModel: ").append(toIndentedString(outputModel)).append("\n");
    sb.append("    allowedTools: ").append(toIndentedString(allowedTools)).append("\n");
    sb.append("    networkPolicy: ").append(toIndentedString(networkPolicy)).append("\n");
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
