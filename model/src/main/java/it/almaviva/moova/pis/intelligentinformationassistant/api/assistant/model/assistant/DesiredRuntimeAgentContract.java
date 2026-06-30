package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Declarative and non-executable runtime governance contract compatible with the Agent Orchestrator admission contract.
 **/
@ApiModel(description = "Declarative and non-executable runtime governance contract compatible with the Agent Orchestrator admission contract.")
@JsonTypeName("DesiredRuntimeAgentContract")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeAgentContract   {
  private String runtimeImage;
  private String sdkVersion;
  private String minimumRuntimeVersion;
  private DesiredRuntimeExecutionModel runtimeExecutionModel;
  private AlertInterpreterType interpreterType;
  private DesiredRuntimeTriggerType triggerType;
  private String inputModel;
  private String outputModel;
  private String evaluationMode;
  private @Valid Set<String> requiredOperators = new LinkedHashSet<>();
  private @Valid List<@Valid DesiredRuntimeToolReference> allowedTools = new ArrayList<>();
  private String networkPolicy;
  private @Valid Set<String> forbiddenCapabilities = new LinkedHashSet<>();
  private @Valid Map<String, Object> compatibility = new HashMap<>();
  private @Valid Set<DesiredRuntimeDataSourceAccessMode> requiredDataSourceAccessModes = new LinkedHashSet<>();
  private @Valid Set<DesiredRuntimeDataSourceConnectorType> requiredConnectorTypes = new LinkedHashSet<>();
  private @Valid Set<@Size(max = 150)String> allowedConnectorRefs = new LinkedHashSet<>();

  public DesiredRuntimeAgentContract() {
  }

  @JsonCreator
  public DesiredRuntimeAgentContract(
    @JsonProperty(required = true, value = "runtimeExecutionModel") DesiredRuntimeExecutionModel runtimeExecutionModel,
    @JsonProperty(required = true, value = "interpreterType") AlertInterpreterType interpreterType,
    @JsonProperty(required = true, value = "triggerType") DesiredRuntimeTriggerType triggerType,
    @JsonProperty(required = true, value = "inputModel") String inputModel,
    @JsonProperty(required = true, value = "outputModel") String outputModel,
    @JsonProperty(required = true, value = "evaluationMode") String evaluationMode,
    @JsonProperty(required = true, value = "requiredOperators") Set<String> requiredOperators,
    @JsonProperty(required = true, value = "forbiddenCapabilities") Set<String> forbiddenCapabilities
  ) {
    this.runtimeExecutionModel = runtimeExecutionModel;
    this.interpreterType = interpreterType;
    this.triggerType = triggerType;
    this.inputModel = inputModel;
    this.outputModel = outputModel;
    this.evaluationMode = evaluationMode;
    this.requiredOperators = requiredOperators;
    this.forbiddenCapabilities = forbiddenCapabilities;
  }

  /**
   **/
  public DesiredRuntimeAgentContract runtimeImage(String runtimeImage) {
    this.runtimeImage = runtimeImage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("runtimeImage")
   @Size(max=500)public String getRuntimeImage() {
    return runtimeImage;
  }

  @JsonProperty("runtimeImage")
  public void setRuntimeImage(String runtimeImage) {
    this.runtimeImage = runtimeImage;
  }

  /**
   **/
  public DesiredRuntimeAgentContract sdkVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sdkVersion")
   @Size(max=50)public String getSdkVersion() {
    return sdkVersion;
  }

  @JsonProperty("sdkVersion")
  public void setSdkVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
  }

  /**
   **/
  public DesiredRuntimeAgentContract minimumRuntimeVersion(String minimumRuntimeVersion) {
    this.minimumRuntimeVersion = minimumRuntimeVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("minimumRuntimeVersion")
   @Size(max=50)public String getMinimumRuntimeVersion() {
    return minimumRuntimeVersion;
  }

  @JsonProperty("minimumRuntimeVersion")
  public void setMinimumRuntimeVersion(String minimumRuntimeVersion) {
    this.minimumRuntimeVersion = minimumRuntimeVersion;
  }

  /**
   **/
  public DesiredRuntimeAgentContract runtimeExecutionModel(DesiredRuntimeExecutionModel runtimeExecutionModel) {
    this.runtimeExecutionModel = runtimeExecutionModel;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "runtimeExecutionModel")
  @NotNull public DesiredRuntimeExecutionModel getRuntimeExecutionModel() {
    return runtimeExecutionModel;
  }

  @JsonProperty(required = true, value = "runtimeExecutionModel")
  public void setRuntimeExecutionModel(DesiredRuntimeExecutionModel runtimeExecutionModel) {
    this.runtimeExecutionModel = runtimeExecutionModel;
  }

  /**
   **/
  public DesiredRuntimeAgentContract interpreterType(AlertInterpreterType interpreterType) {
    this.interpreterType = interpreterType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "interpreterType")
  @NotNull public AlertInterpreterType getInterpreterType() {
    return interpreterType;
  }

  @JsonProperty(required = true, value = "interpreterType")
  public void setInterpreterType(AlertInterpreterType interpreterType) {
    this.interpreterType = interpreterType;
  }

  /**
   **/
  public DesiredRuntimeAgentContract triggerType(DesiredRuntimeTriggerType triggerType) {
    this.triggerType = triggerType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "triggerType")
  @NotNull public DesiredRuntimeTriggerType getTriggerType() {
    return triggerType;
  }

  @JsonProperty(required = true, value = "triggerType")
  public void setTriggerType(DesiredRuntimeTriggerType triggerType) {
    this.triggerType = triggerType;
  }

  /**
   **/
  public DesiredRuntimeAgentContract inputModel(String inputModel) {
    this.inputModel = inputModel;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "inputModel")
  @NotNull  @Size(min=1,max=100)public String getInputModel() {
    return inputModel;
  }

  @JsonProperty(required = true, value = "inputModel")
  public void setInputModel(String inputModel) {
    this.inputModel = inputModel;
  }

  /**
   **/
  public DesiredRuntimeAgentContract outputModel(String outputModel) {
    this.outputModel = outputModel;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "outputModel")
  @NotNull  @Size(min=1,max=100)public String getOutputModel() {
    return outputModel;
  }

  @JsonProperty(required = true, value = "outputModel")
  public void setOutputModel(String outputModel) {
    this.outputModel = outputModel;
  }

  /**
   **/
  public DesiredRuntimeAgentContract evaluationMode(String evaluationMode) {
    this.evaluationMode = evaluationMode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "evaluationMode")
  @NotNull  @Size(min=1,max=100)public String getEvaluationMode() {
    return evaluationMode;
  }

  @JsonProperty(required = true, value = "evaluationMode")
  public void setEvaluationMode(String evaluationMode) {
    this.evaluationMode = evaluationMode;
  }

  /**
   **/
  public DesiredRuntimeAgentContract requiredOperators(Set<String> requiredOperators) {
    this.requiredOperators = requiredOperators;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "requiredOperators")
  @NotNull public Set<String> getRequiredOperators() {
    return requiredOperators;
  }

  @JsonProperty(required = true, value = "requiredOperators")
  @JsonDeserialize(as = LinkedHashSet.class)
  public void setRequiredOperators(Set<String> requiredOperators) {
    this.requiredOperators = requiredOperators;
  }

  public DesiredRuntimeAgentContract addRequiredOperatorsItem(String requiredOperatorsItem) {
    if (this.requiredOperators == null) {
      this.requiredOperators = new LinkedHashSet<>();
    }

    this.requiredOperators.add(requiredOperatorsItem);
    return this;
  }

  public DesiredRuntimeAgentContract removeRequiredOperatorsItem(String requiredOperatorsItem) {
    if (requiredOperatorsItem != null && this.requiredOperators != null) {
      this.requiredOperators.remove(requiredOperatorsItem);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeAgentContract allowedTools(List<@Valid DesiredRuntimeToolReference> allowedTools) {
    this.allowedTools = allowedTools;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("allowedTools")
  @Valid public List<@Valid DesiredRuntimeToolReference> getAllowedTools() {
    return allowedTools;
  }

  @JsonProperty("allowedTools")
  public void setAllowedTools(List<@Valid DesiredRuntimeToolReference> allowedTools) {
    this.allowedTools = allowedTools;
  }

  public DesiredRuntimeAgentContract addAllowedToolsItem(DesiredRuntimeToolReference allowedToolsItem) {
    if (this.allowedTools == null) {
      this.allowedTools = new ArrayList<>();
    }

    this.allowedTools.add(allowedToolsItem);
    return this;
  }

  public DesiredRuntimeAgentContract removeAllowedToolsItem(DesiredRuntimeToolReference allowedToolsItem) {
    if (allowedToolsItem != null && this.allowedTools != null) {
      this.allowedTools.remove(allowedToolsItem);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeAgentContract networkPolicy(String networkPolicy) {
    this.networkPolicy = networkPolicy;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("networkPolicy")
   @Size(max=100)public String getNetworkPolicy() {
    return networkPolicy;
  }

  @JsonProperty("networkPolicy")
  public void setNetworkPolicy(String networkPolicy) {
    this.networkPolicy = networkPolicy;
  }

  /**
   **/
  public DesiredRuntimeAgentContract forbiddenCapabilities(Set<String> forbiddenCapabilities) {
    this.forbiddenCapabilities = forbiddenCapabilities;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "forbiddenCapabilities")
  @NotNull public Set<String> getForbiddenCapabilities() {
    return forbiddenCapabilities;
  }

  @JsonProperty(required = true, value = "forbiddenCapabilities")
  @JsonDeserialize(as = LinkedHashSet.class)
  public void setForbiddenCapabilities(Set<String> forbiddenCapabilities) {
    this.forbiddenCapabilities = forbiddenCapabilities;
  }

  public DesiredRuntimeAgentContract addForbiddenCapabilitiesItem(String forbiddenCapabilitiesItem) {
    if (this.forbiddenCapabilities == null) {
      this.forbiddenCapabilities = new LinkedHashSet<>();
    }

    this.forbiddenCapabilities.add(forbiddenCapabilitiesItem);
    return this;
  }

  public DesiredRuntimeAgentContract removeForbiddenCapabilitiesItem(String forbiddenCapabilitiesItem) {
    if (forbiddenCapabilitiesItem != null && this.forbiddenCapabilities != null) {
      this.forbiddenCapabilities.remove(forbiddenCapabilitiesItem);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeAgentContract compatibility(Map<String, Object> compatibility) {
    this.compatibility = compatibility;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("compatibility")
  public Map<String, Object> getCompatibility() {
    return compatibility;
  }

  @JsonProperty("compatibility")
  public void setCompatibility(Map<String, Object> compatibility) {
    this.compatibility = compatibility;
  }

  public DesiredRuntimeAgentContract putCompatibilityItem(String key, Object compatibilityItem) {
    if (this.compatibility == null) {
      this.compatibility = new HashMap<>();
    }

    this.compatibility.put(key, compatibilityItem);
    return this;
  }

  public DesiredRuntimeAgentContract removeCompatibilityItem(String key) {
    if (this.compatibility != null) {
      this.compatibility.remove(key);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeAgentContract requiredDataSourceAccessModes(Set<DesiredRuntimeDataSourceAccessMode> requiredDataSourceAccessModes) {
    this.requiredDataSourceAccessModes = requiredDataSourceAccessModes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requiredDataSourceAccessModes")
  public Set<DesiredRuntimeDataSourceAccessMode> getRequiredDataSourceAccessModes() {
    return requiredDataSourceAccessModes;
  }

  @JsonProperty("requiredDataSourceAccessModes")
  @JsonDeserialize(as = LinkedHashSet.class)
  public void setRequiredDataSourceAccessModes(Set<DesiredRuntimeDataSourceAccessMode> requiredDataSourceAccessModes) {
    this.requiredDataSourceAccessModes = requiredDataSourceAccessModes;
  }

  public DesiredRuntimeAgentContract addRequiredDataSourceAccessModesItem(DesiredRuntimeDataSourceAccessMode requiredDataSourceAccessModesItem) {
    if (this.requiredDataSourceAccessModes == null) {
      this.requiredDataSourceAccessModes = new LinkedHashSet<>();
    }

    this.requiredDataSourceAccessModes.add(requiredDataSourceAccessModesItem);
    return this;
  }

  public DesiredRuntimeAgentContract removeRequiredDataSourceAccessModesItem(DesiredRuntimeDataSourceAccessMode requiredDataSourceAccessModesItem) {
    if (requiredDataSourceAccessModesItem != null && this.requiredDataSourceAccessModes != null) {
      this.requiredDataSourceAccessModes.remove(requiredDataSourceAccessModesItem);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeAgentContract requiredConnectorTypes(Set<DesiredRuntimeDataSourceConnectorType> requiredConnectorTypes) {
    this.requiredConnectorTypes = requiredConnectorTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requiredConnectorTypes")
  public Set<DesiredRuntimeDataSourceConnectorType> getRequiredConnectorTypes() {
    return requiredConnectorTypes;
  }

  @JsonProperty("requiredConnectorTypes")
  @JsonDeserialize(as = LinkedHashSet.class)
  public void setRequiredConnectorTypes(Set<DesiredRuntimeDataSourceConnectorType> requiredConnectorTypes) {
    this.requiredConnectorTypes = requiredConnectorTypes;
  }

  public DesiredRuntimeAgentContract addRequiredConnectorTypesItem(DesiredRuntimeDataSourceConnectorType requiredConnectorTypesItem) {
    if (this.requiredConnectorTypes == null) {
      this.requiredConnectorTypes = new LinkedHashSet<>();
    }

    this.requiredConnectorTypes.add(requiredConnectorTypesItem);
    return this;
  }

  public DesiredRuntimeAgentContract removeRequiredConnectorTypesItem(DesiredRuntimeDataSourceConnectorType requiredConnectorTypesItem) {
    if (requiredConnectorTypesItem != null && this.requiredConnectorTypes != null) {
      this.requiredConnectorTypes.remove(requiredConnectorTypesItem);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeAgentContract allowedConnectorRefs(Set<@Size(max = 150)String> allowedConnectorRefs) {
    this.allowedConnectorRefs = allowedConnectorRefs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("allowedConnectorRefs")
  public Set< @Size(max=150)String> getAllowedConnectorRefs() {
    return allowedConnectorRefs;
  }

  @JsonProperty("allowedConnectorRefs")
  @JsonDeserialize(as = LinkedHashSet.class)
  public void setAllowedConnectorRefs(Set<@Size(max = 150)String> allowedConnectorRefs) {
    this.allowedConnectorRefs = allowedConnectorRefs;
  }

  public DesiredRuntimeAgentContract addAllowedConnectorRefsItem(String allowedConnectorRefsItem) {
    if (this.allowedConnectorRefs == null) {
      this.allowedConnectorRefs = new LinkedHashSet<>();
    }

    this.allowedConnectorRefs.add(allowedConnectorRefsItem);
    return this;
  }

  public DesiredRuntimeAgentContract removeAllowedConnectorRefsItem(String allowedConnectorRefsItem) {
    if (allowedConnectorRefsItem != null && this.allowedConnectorRefs != null) {
      this.allowedConnectorRefs.remove(allowedConnectorRefsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeAgentContract desiredRuntimeAgentContract = (DesiredRuntimeAgentContract) o;
    return Objects.equals(this.runtimeImage, desiredRuntimeAgentContract.runtimeImage) &&
        Objects.equals(this.sdkVersion, desiredRuntimeAgentContract.sdkVersion) &&
        Objects.equals(this.minimumRuntimeVersion, desiredRuntimeAgentContract.minimumRuntimeVersion) &&
        Objects.equals(this.runtimeExecutionModel, desiredRuntimeAgentContract.runtimeExecutionModel) &&
        Objects.equals(this.interpreterType, desiredRuntimeAgentContract.interpreterType) &&
        Objects.equals(this.triggerType, desiredRuntimeAgentContract.triggerType) &&
        Objects.equals(this.inputModel, desiredRuntimeAgentContract.inputModel) &&
        Objects.equals(this.outputModel, desiredRuntimeAgentContract.outputModel) &&
        Objects.equals(this.evaluationMode, desiredRuntimeAgentContract.evaluationMode) &&
        Objects.equals(this.requiredOperators, desiredRuntimeAgentContract.requiredOperators) &&
        Objects.equals(this.allowedTools, desiredRuntimeAgentContract.allowedTools) &&
        Objects.equals(this.networkPolicy, desiredRuntimeAgentContract.networkPolicy) &&
        Objects.equals(this.forbiddenCapabilities, desiredRuntimeAgentContract.forbiddenCapabilities) &&
        Objects.equals(this.compatibility, desiredRuntimeAgentContract.compatibility) &&
        Objects.equals(this.requiredDataSourceAccessModes, desiredRuntimeAgentContract.requiredDataSourceAccessModes) &&
        Objects.equals(this.requiredConnectorTypes, desiredRuntimeAgentContract.requiredConnectorTypes) &&
        Objects.equals(this.allowedConnectorRefs, desiredRuntimeAgentContract.allowedConnectorRefs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runtimeImage, sdkVersion, minimumRuntimeVersion, runtimeExecutionModel, interpreterType, triggerType, inputModel, outputModel, evaluationMode, requiredOperators, allowedTools, networkPolicy, forbiddenCapabilities, compatibility, requiredDataSourceAccessModes, requiredConnectorTypes, allowedConnectorRefs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeAgentContract {\n");
    
    sb.append("    runtimeImage: ").append(toIndentedString(runtimeImage)).append("\n");
    sb.append("    sdkVersion: ").append(toIndentedString(sdkVersion)).append("\n");
    sb.append("    minimumRuntimeVersion: ").append(toIndentedString(minimumRuntimeVersion)).append("\n");
    sb.append("    runtimeExecutionModel: ").append(toIndentedString(runtimeExecutionModel)).append("\n");
    sb.append("    interpreterType: ").append(toIndentedString(interpreterType)).append("\n");
    sb.append("    triggerType: ").append(toIndentedString(triggerType)).append("\n");
    sb.append("    inputModel: ").append(toIndentedString(inputModel)).append("\n");
    sb.append("    outputModel: ").append(toIndentedString(outputModel)).append("\n");
    sb.append("    evaluationMode: ").append(toIndentedString(evaluationMode)).append("\n");
    sb.append("    requiredOperators: ").append(toIndentedString(requiredOperators)).append("\n");
    sb.append("    allowedTools: ").append(toIndentedString(allowedTools)).append("\n");
    sb.append("    networkPolicy: ").append(toIndentedString(networkPolicy)).append("\n");
    sb.append("    forbiddenCapabilities: ").append(toIndentedString(forbiddenCapabilities)).append("\n");
    sb.append("    compatibility: ").append(toIndentedString(compatibility)).append("\n");
    sb.append("    requiredDataSourceAccessModes: ").append(toIndentedString(requiredDataSourceAccessModes)).append("\n");
    sb.append("    requiredConnectorTypes: ").append(toIndentedString(requiredConnectorTypes)).append("\n");
    sb.append("    allowedConnectorRefs: ").append(toIndentedString(allowedConnectorRefs)).append("\n");
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
