package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("DesiredRuntimeAgentDefinitionPackage")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeAgentDefinitionPackage   {
  private String id;
  private String name;
  private String description;
  private DesiredRuntimeAgentSourceReference source;
  private DesiredRuntimeAgentProfileSnapshot profile;
  private AgentActivationPolicy activationPolicy;
  private AlertInterpreterType interpreterType;
  private DesiredRuntimeTriggerType triggerType;
  private String inputModel;
  private String outputModel;
  private DesiredRuntimeAgentContract runtimeContract;
  private DesiredRuntimeArtifact artifact;
  private @Valid Map<String, Object> metadata = new HashMap<>();
  private OffsetDateTime sourceUpdatedAt;
  private String dataDomain;
  private @Valid List<@Valid DesiredRuntimeDataSourceBinding> dataSourceBindings = new ArrayList<>();

  public DesiredRuntimeAgentDefinitionPackage() {
  }

  @JsonCreator
  public DesiredRuntimeAgentDefinitionPackage(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "source") DesiredRuntimeAgentSourceReference source,
    @JsonProperty(required = true, value = "profile") DesiredRuntimeAgentProfileSnapshot profile,
    @JsonProperty(required = true, value = "activationPolicy") AgentActivationPolicy activationPolicy,
    @JsonProperty(required = true, value = "interpreterType") AlertInterpreterType interpreterType,
    @JsonProperty(required = true, value = "triggerType") DesiredRuntimeTriggerType triggerType,
    @JsonProperty(required = true, value = "inputModel") String inputModel,
    @JsonProperty(required = true, value = "outputModel") String outputModel,
    @JsonProperty(required = true, value = "runtimeContract") DesiredRuntimeAgentContract runtimeContract,
    @JsonProperty(required = true, value = "artifact") DesiredRuntimeArtifact artifact,
    @JsonProperty(required = true, value = "dataSourceBindings") List<@Valid DesiredRuntimeDataSourceBinding> dataSourceBindings
  ) {
    this.id = id;
    this.name = name;
    this.source = source;
    this.profile = profile;
    this.activationPolicy = activationPolicy;
    this.interpreterType = interpreterType;
    this.triggerType = triggerType;
    this.inputModel = inputModel;
    this.outputModel = outputModel;
    this.runtimeContract = runtimeContract;
    this.artifact = artifact;
    this.dataSourceBindings = dataSourceBindings;
  }

  /**
   **/
  public DesiredRuntimeAgentDefinitionPackage id(String id) {
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
  public DesiredRuntimeAgentDefinitionPackage name(String name) {
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
  public DesiredRuntimeAgentDefinitionPackage description(String description) {
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
  public DesiredRuntimeAgentDefinitionPackage source(DesiredRuntimeAgentSourceReference source) {
    this.source = source;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "source")
  @NotNull @Valid public DesiredRuntimeAgentSourceReference getSource() {
    return source;
  }

  @JsonProperty(required = true, value = "source")
  public void setSource(DesiredRuntimeAgentSourceReference source) {
    this.source = source;
  }

  /**
   **/
  public DesiredRuntimeAgentDefinitionPackage profile(DesiredRuntimeAgentProfileSnapshot profile) {
    this.profile = profile;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "profile")
  @NotNull @Valid public DesiredRuntimeAgentProfileSnapshot getProfile() {
    return profile;
  }

  @JsonProperty(required = true, value = "profile")
  public void setProfile(DesiredRuntimeAgentProfileSnapshot profile) {
    this.profile = profile;
  }

  /**
   **/
  public DesiredRuntimeAgentDefinitionPackage activationPolicy(AgentActivationPolicy activationPolicy) {
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
  public DesiredRuntimeAgentDefinitionPackage interpreterType(AlertInterpreterType interpreterType) {
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
  public DesiredRuntimeAgentDefinitionPackage triggerType(DesiredRuntimeTriggerType triggerType) {
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
  public DesiredRuntimeAgentDefinitionPackage inputModel(String inputModel) {
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
  public DesiredRuntimeAgentDefinitionPackage outputModel(String outputModel) {
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
  public DesiredRuntimeAgentDefinitionPackage runtimeContract(DesiredRuntimeAgentContract runtimeContract) {
    this.runtimeContract = runtimeContract;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "runtimeContract")
  @NotNull @Valid public DesiredRuntimeAgentContract getRuntimeContract() {
    return runtimeContract;
  }

  @JsonProperty(required = true, value = "runtimeContract")
  public void setRuntimeContract(DesiredRuntimeAgentContract runtimeContract) {
    this.runtimeContract = runtimeContract;
  }

  /**
   **/
  public DesiredRuntimeAgentDefinitionPackage artifact(DesiredRuntimeArtifact artifact) {
    this.artifact = artifact;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "artifact")
  @NotNull @Valid public DesiredRuntimeArtifact getArtifact() {
    return artifact;
  }

  @JsonProperty(required = true, value = "artifact")
  public void setArtifact(DesiredRuntimeArtifact artifact) {
    this.artifact = artifact;
  }

  /**
   * Optional sanitized non-authoritative metadata. Credentials, arbitrary endpoints, broker addresses and unrestricted connector details are forbidden.
   **/
  public DesiredRuntimeAgentDefinitionPackage metadata(Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }

  
  @ApiModelProperty(value = "Optional sanitized non-authoritative metadata. Credentials, arbitrary endpoints, broker addresses and unrestricted connector details are forbidden.")
  @JsonProperty("metadata")
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public DesiredRuntimeAgentDefinitionPackage putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }

    this.metadata.put(key, metadataItem);
    return this;
  }

  public DesiredRuntimeAgentDefinitionPackage removeMetadataItem(String key) {
    if (this.metadata != null) {
      this.metadata.remove(key);
    }

    return this;
  }
  /**
   * Last runtime-significant update timestamp in the Assistant.
   **/
  public DesiredRuntimeAgentDefinitionPackage sourceUpdatedAt(OffsetDateTime sourceUpdatedAt) {
    this.sourceUpdatedAt = sourceUpdatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "Last runtime-significant update timestamp in the Assistant.")
  @JsonProperty("sourceUpdatedAt")
  public OffsetDateTime getSourceUpdatedAt() {
    return sourceUpdatedAt;
  }

  @JsonProperty("sourceUpdatedAt")
  public void setSourceUpdatedAt(OffsetDateTime sourceUpdatedAt) {
    this.sourceUpdatedAt = sourceUpdatedAt;
  }

  /**
   **/
  public DesiredRuntimeAgentDefinitionPackage dataDomain(String dataDomain) {
    this.dataDomain = dataDomain;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("dataDomain")
   @Size(max=100)public String getDataDomain() {
    return dataDomain;
  }

  @JsonProperty("dataDomain")
  public void setDataDomain(String dataDomain) {
    this.dataDomain = dataDomain;
  }

  /**
   **/
  public DesiredRuntimeAgentDefinitionPackage dataSourceBindings(List<@Valid DesiredRuntimeDataSourceBinding> dataSourceBindings) {
    this.dataSourceBindings = dataSourceBindings;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "dataSourceBindings")
  @NotNull @Valid  @Size(min=1,max=10)public List<@Valid DesiredRuntimeDataSourceBinding> getDataSourceBindings() {
    return dataSourceBindings;
  }

  @JsonProperty(required = true, value = "dataSourceBindings")
  public void setDataSourceBindings(List<@Valid DesiredRuntimeDataSourceBinding> dataSourceBindings) {
    this.dataSourceBindings = dataSourceBindings;
  }

  public DesiredRuntimeAgentDefinitionPackage addDataSourceBindingsItem(DesiredRuntimeDataSourceBinding dataSourceBindingsItem) {
    if (this.dataSourceBindings == null) {
      this.dataSourceBindings = new ArrayList<>();
    }

    this.dataSourceBindings.add(dataSourceBindingsItem);
    return this;
  }

  public DesiredRuntimeAgentDefinitionPackage removeDataSourceBindingsItem(DesiredRuntimeDataSourceBinding dataSourceBindingsItem) {
    if (dataSourceBindingsItem != null && this.dataSourceBindings != null) {
      this.dataSourceBindings.remove(dataSourceBindingsItem);
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
    DesiredRuntimeAgentDefinitionPackage desiredRuntimeAgentDefinitionPackage = (DesiredRuntimeAgentDefinitionPackage) o;
    return Objects.equals(this.id, desiredRuntimeAgentDefinitionPackage.id) &&
        Objects.equals(this.name, desiredRuntimeAgentDefinitionPackage.name) &&
        Objects.equals(this.description, desiredRuntimeAgentDefinitionPackage.description) &&
        Objects.equals(this.source, desiredRuntimeAgentDefinitionPackage.source) &&
        Objects.equals(this.profile, desiredRuntimeAgentDefinitionPackage.profile) &&
        Objects.equals(this.activationPolicy, desiredRuntimeAgentDefinitionPackage.activationPolicy) &&
        Objects.equals(this.interpreterType, desiredRuntimeAgentDefinitionPackage.interpreterType) &&
        Objects.equals(this.triggerType, desiredRuntimeAgentDefinitionPackage.triggerType) &&
        Objects.equals(this.inputModel, desiredRuntimeAgentDefinitionPackage.inputModel) &&
        Objects.equals(this.outputModel, desiredRuntimeAgentDefinitionPackage.outputModel) &&
        Objects.equals(this.runtimeContract, desiredRuntimeAgentDefinitionPackage.runtimeContract) &&
        Objects.equals(this.artifact, desiredRuntimeAgentDefinitionPackage.artifact) &&
        Objects.equals(this.metadata, desiredRuntimeAgentDefinitionPackage.metadata) &&
        Objects.equals(this.sourceUpdatedAt, desiredRuntimeAgentDefinitionPackage.sourceUpdatedAt) &&
        Objects.equals(this.dataDomain, desiredRuntimeAgentDefinitionPackage.dataDomain) &&
        Objects.equals(this.dataSourceBindings, desiredRuntimeAgentDefinitionPackage.dataSourceBindings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, source, profile, activationPolicy, interpreterType, triggerType, inputModel, outputModel, runtimeContract, artifact, metadata, sourceUpdatedAt, dataDomain, dataSourceBindings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeAgentDefinitionPackage {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
    sb.append("    activationPolicy: ").append(toIndentedString(activationPolicy)).append("\n");
    sb.append("    interpreterType: ").append(toIndentedString(interpreterType)).append("\n");
    sb.append("    triggerType: ").append(toIndentedString(triggerType)).append("\n");
    sb.append("    inputModel: ").append(toIndentedString(inputModel)).append("\n");
    sb.append("    outputModel: ").append(toIndentedString(outputModel)).append("\n");
    sb.append("    runtimeContract: ").append(toIndentedString(runtimeContract)).append("\n");
    sb.append("    artifact: ").append(toIndentedString(artifact)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    sourceUpdatedAt: ").append(toIndentedString(sourceUpdatedAt)).append("\n");
    sb.append("    dataDomain: ").append(toIndentedString(dataDomain)).append("\n");
    sb.append("    dataSourceBindings: ").append(toIndentedString(dataSourceBindings)).append("\n");
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
