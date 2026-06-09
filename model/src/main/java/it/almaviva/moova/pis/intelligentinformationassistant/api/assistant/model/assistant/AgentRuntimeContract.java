package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public enum RuntimeExecutionModelEnum {

    STANDARD_DSL_EVALUATOR(String.valueOf("STANDARD_DSL_EVALUATOR")), APPROVED_TEMPLATE_RUNTIME(String.valueOf("APPROVED_TEMPLATE_RUNTIME"));


    private String value;

    RuntimeExecutionModelEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    public static RuntimeExecutionModelEnum fromString(String s) {
        for (RuntimeExecutionModelEnum b : RuntimeExecutionModelEnum.values()) {
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static RuntimeExecutionModelEnum fromValue(String value) {
        for (RuntimeExecutionModelEnum b : RuntimeExecutionModelEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private RuntimeExecutionModelEnum runtimeExecutionModel;
  private AlertInterpreterType interpreterType;
  private String triggerType;
  private String evaluationMode;
  private @Valid List<String> requiredOperators = new ArrayList<>();
  private @Valid List<String> forbiddenCapabilities = new ArrayList<>();
  private @Valid Map<String, Object> orchestratorCompatibility = new HashMap<>();

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

  /**
   * Execution model expected by the Agent Orchestrator.
   **/
  public AgentRuntimeContract runtimeExecutionModel(RuntimeExecutionModelEnum runtimeExecutionModel) {
    this.runtimeExecutionModel = runtimeExecutionModel;
    return this;
  }

  
  @ApiModelProperty(example = "STANDARD_DSL_EVALUATOR", value = "Execution model expected by the Agent Orchestrator.")
  @JsonProperty("runtimeExecutionModel")
  public RuntimeExecutionModelEnum getRuntimeExecutionModel() {
    return runtimeExecutionModel;
  }

  @JsonProperty("runtimeExecutionModel")
  public void setRuntimeExecutionModel(RuntimeExecutionModelEnum runtimeExecutionModel) {
    this.runtimeExecutionModel = runtimeExecutionModel;
  }

  /**
   **/
  public AgentRuntimeContract interpreterType(AlertInterpreterType interpreterType) {
    this.interpreterType = interpreterType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("interpreterType")
  public AlertInterpreterType getInterpreterType() {
    return interpreterType;
  }

  @JsonProperty("interpreterType")
  public void setInterpreterType(AlertInterpreterType interpreterType) {
    this.interpreterType = interpreterType;
  }

  /**
   * Runtime trigger type supported by the artifact.
   **/
  public AgentRuntimeContract triggerType(String triggerType) {
    this.triggerType = triggerType;
    return this;
  }

  
  @ApiModelProperty(value = "Runtime trigger type supported by the artifact.")
  @JsonProperty("triggerType")
  public String getTriggerType() {
    return triggerType;
  }

  @JsonProperty("triggerType")
  public void setTriggerType(String triggerType) {
    this.triggerType = triggerType;
  }

  /**
   * Evaluation mode required by the runtime artifact.
   **/
  public AgentRuntimeContract evaluationMode(String evaluationMode) {
    this.evaluationMode = evaluationMode;
    return this;
  }

  
  @ApiModelProperty(example = "STATELESS_EVENT_MATCH", value = "Evaluation mode required by the runtime artifact.")
  @JsonProperty("evaluationMode")
  public String getEvaluationMode() {
    return evaluationMode;
  }

  @JsonProperty("evaluationMode")
  public void setEvaluationMode(String evaluationMode) {
    this.evaluationMode = evaluationMode;
  }

  /**
   * Operators that must be implemented by the runtime DSL evaluator in order to load this artifact.
   **/
  public AgentRuntimeContract requiredOperators(List<String> requiredOperators) {
    this.requiredOperators = requiredOperators;
    return this;
  }

  
  @ApiModelProperty(value = "Operators that must be implemented by the runtime DSL evaluator in order to load this artifact.")
  @JsonProperty("requiredOperators")
  public List<String> getRequiredOperators() {
    return requiredOperators;
  }

  @JsonProperty("requiredOperators")
  public void setRequiredOperators(List<String> requiredOperators) {
    this.requiredOperators = requiredOperators;
  }

  /**
   * Capabilities explicitly forbidden for this runtime contract.
   **/
  public AgentRuntimeContract forbiddenCapabilities(List<String> forbiddenCapabilities) {
    this.forbiddenCapabilities = forbiddenCapabilities;
    return this;
  }

  
  @ApiModelProperty(value = "Capabilities explicitly forbidden for this runtime contract.")
  @JsonProperty("forbiddenCapabilities")
  public List<String> getForbiddenCapabilities() {
    return forbiddenCapabilities;
  }

  @JsonProperty("forbiddenCapabilities")
  public void setForbiddenCapabilities(List<String> forbiddenCapabilities) {
    this.forbiddenCapabilities = forbiddenCapabilities;
  }

  /**
   * Optional runtime compatibility metadata used by the Agent Orchestrator.
   **/
  public AgentRuntimeContract orchestratorCompatibility(Map<String, Object> orchestratorCompatibility) {
    this.orchestratorCompatibility = orchestratorCompatibility;
    return this;
  }

  
  @ApiModelProperty(value = "Optional runtime compatibility metadata used by the Agent Orchestrator.")
  @JsonProperty("orchestratorCompatibility")
  public Map<String, Object> getOrchestratorCompatibility() {
    return orchestratorCompatibility;
  }

  @JsonProperty("orchestratorCompatibility")
  public void setOrchestratorCompatibility(Map<String, Object> orchestratorCompatibility) {
    this.orchestratorCompatibility = orchestratorCompatibility;
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
        Objects.equals(this.networkPolicy, agentRuntimeContract.networkPolicy) &&
        Objects.equals(this.runtimeExecutionModel, agentRuntimeContract.runtimeExecutionModel) &&
        Objects.equals(this.interpreterType, agentRuntimeContract.interpreterType) &&
        Objects.equals(this.triggerType, agentRuntimeContract.triggerType) &&
        Objects.equals(this.evaluationMode, agentRuntimeContract.evaluationMode) &&
        Objects.equals(this.requiredOperators, agentRuntimeContract.requiredOperators) &&
        Objects.equals(this.forbiddenCapabilities, agentRuntimeContract.forbiddenCapabilities) &&
        Objects.equals(this.orchestratorCompatibility, agentRuntimeContract.orchestratorCompatibility);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runtimeImage, sdkVersion, inputModel, outputModel, allowedTools, networkPolicy, runtimeExecutionModel, interpreterType, triggerType, evaluationMode, requiredOperators, forbiddenCapabilities, orchestratorCompatibility);
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
    sb.append("    runtimeExecutionModel: ").append(toIndentedString(runtimeExecutionModel)).append("\n");
    sb.append("    interpreterType: ").append(toIndentedString(interpreterType)).append("\n");
    sb.append("    triggerType: ").append(toIndentedString(triggerType)).append("\n");
    sb.append("    evaluationMode: ").append(toIndentedString(evaluationMode)).append("\n");
    sb.append("    requiredOperators: ").append(toIndentedString(requiredOperators)).append("\n");
    sb.append("    forbiddenCapabilities: ").append(toIndentedString(forbiddenCapabilities)).append("\n");
    sb.append("    orchestratorCompatibility: ").append(toIndentedString(orchestratorCompatibility)).append("\n");
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
