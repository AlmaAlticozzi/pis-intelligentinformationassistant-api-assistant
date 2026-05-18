package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
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

/**
 * Compact process metadata for audit and troubleshooting.
 **/
@ApiModel(description = "Compact process metadata for audit and troubleshooting.")
@JsonTypeName("SuggestionGenerationMetadata")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionGenerationMetadata   {
  private AlertInterpreterType interpreterType;
  private String interpreterClassName;
  private Boolean generatedByLlm;
  private String llmProvider;
  private String llmModel;
  private String promptCode;
  private String promptVersion;
  private @Valid List<String> warnings = new ArrayList<>();
  private String agentDefinitionId;
  private String agentRunId;
  private String agentOutputId;
  private AgentArtifactType artifactType;
  private String artifactHash;

  public SuggestionGenerationMetadata() {
  }

  /**
   **/
  public SuggestionGenerationMetadata interpreterType(AlertInterpreterType interpreterType) {
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
   **/
  public SuggestionGenerationMetadata interpreterClassName(String interpreterClassName) {
    this.interpreterClassName = interpreterClassName;
    return this;
  }

  
  @ApiModelProperty(example = "GeneratedCancellationWithoutAnnouncementInterpreter", value = "")
  @JsonProperty("interpreterClassName")
  public String getInterpreterClassName() {
    return interpreterClassName;
  }

  @JsonProperty("interpreterClassName")
  public void setInterpreterClassName(String interpreterClassName) {
    this.interpreterClassName = interpreterClassName;
  }

  /**
   **/
  public SuggestionGenerationMetadata generatedByLlm(Boolean generatedByLlm) {
    this.generatedByLlm = generatedByLlm;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("generatedByLlm")
  public Boolean getGeneratedByLlm() {
    return generatedByLlm;
  }

  @JsonProperty("generatedByLlm")
  public void setGeneratedByLlm(Boolean generatedByLlm) {
    this.generatedByLlm = generatedByLlm;
  }

  /**
   **/
  public SuggestionGenerationMetadata llmProvider(String llmProvider) {
    this.llmProvider = llmProvider;
    return this;
  }

  
  @ApiModelProperty(example = "azure-openai", value = "")
  @JsonProperty("llmProvider")
  public String getLlmProvider() {
    return llmProvider;
  }

  @JsonProperty("llmProvider")
  public void setLlmProvider(String llmProvider) {
    this.llmProvider = llmProvider;
  }

  /**
   **/
  public SuggestionGenerationMetadata llmModel(String llmModel) {
    this.llmModel = llmModel;
    return this;
  }

  
  @ApiModelProperty(example = "gpt-4o-mini", value = "")
  @JsonProperty("llmModel")
  public String getLlmModel() {
    return llmModel;
  }

  @JsonProperty("llmModel")
  public void setLlmModel(String llmModel) {
    this.llmModel = llmModel;
  }

  /**
   **/
  public SuggestionGenerationMetadata promptCode(String promptCode) {
    this.promptCode = promptCode;
    return this;
  }

  
  @ApiModelProperty(example = "GENERATE_SUGGESTION_FROM_INTERPRETER_OUTPUT", value = "")
  @JsonProperty("promptCode")
  public String getPromptCode() {
    return promptCode;
  }

  @JsonProperty("promptCode")
  public void setPromptCode(String promptCode) {
    this.promptCode = promptCode;
  }

  /**
   **/
  public SuggestionGenerationMetadata promptVersion(String promptVersion) {
    this.promptVersion = promptVersion;
    return this;
  }

  
  @ApiModelProperty(example = "0.0.1", value = "")
  @JsonProperty("promptVersion")
  public String getPromptVersion() {
    return promptVersion;
  }

  @JsonProperty("promptVersion")
  public void setPromptVersion(String promptVersion) {
    this.promptVersion = promptVersion;
  }

  /**
   **/
  public SuggestionGenerationMetadata warnings(List<String> warnings) {
    this.warnings = warnings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("warnings")
  public List<String> getWarnings() {
    return warnings;
  }

  @JsonProperty("warnings")
  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public SuggestionGenerationMetadata addWarningsItem(String warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }

    this.warnings.add(warningsItem);
    return this;
  }

  public SuggestionGenerationMetadata removeWarningsItem(String warningsItem) {
    if (warningsItem != null && this.warnings != null) {
      this.warnings.remove(warningsItem);
    }

    return this;
  }
  /**
   **/
  public SuggestionGenerationMetadata agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(example = "AGDF2026251400000001", value = "")
  @JsonProperty("agentDefinitionId")
   @Size(max=50)public String getAgentDefinitionId() {
    return agentDefinitionId;
  }

  @JsonProperty("agentDefinitionId")
  public void setAgentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
  }

  /**
   **/
  public SuggestionGenerationMetadata agentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
    return this;
  }

  
  @ApiModelProperty(example = "AGRN2026251400000001", value = "")
  @JsonProperty("agentRunId")
   @Size(max=50)public String getAgentRunId() {
    return agentRunId;
  }

  @JsonProperty("agentRunId")
  public void setAgentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
  }

  /**
   **/
  public SuggestionGenerationMetadata agentOutputId(String agentOutputId) {
    this.agentOutputId = agentOutputId;
    return this;
  }

  
  @ApiModelProperty(example = "AGOU2026251400000001", value = "")
  @JsonProperty("agentOutputId")
   @Size(max=50)public String getAgentOutputId() {
    return agentOutputId;
  }

  @JsonProperty("agentOutputId")
  public void setAgentOutputId(String agentOutputId) {
    this.agentOutputId = agentOutputId;
  }

  /**
   **/
  public SuggestionGenerationMetadata artifactType(AgentArtifactType artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("artifactType")
  public AgentArtifactType getArtifactType() {
    return artifactType;
  }

  @JsonProperty("artifactType")
  public void setArtifactType(AgentArtifactType artifactType) {
    this.artifactType = artifactType;
  }

  /**
   **/
  public SuggestionGenerationMetadata artifactHash(String artifactHash) {
    this.artifactHash = artifactHash;
    return this;
  }

  
  @ApiModelProperty(example = "sha256:5f2b6c...", value = "")
  @JsonProperty("artifactHash")
  public String getArtifactHash() {
    return artifactHash;
  }

  @JsonProperty("artifactHash")
  public void setArtifactHash(String artifactHash) {
    this.artifactHash = artifactHash;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuggestionGenerationMetadata suggestionGenerationMetadata = (SuggestionGenerationMetadata) o;
    return Objects.equals(this.interpreterType, suggestionGenerationMetadata.interpreterType) &&
        Objects.equals(this.interpreterClassName, suggestionGenerationMetadata.interpreterClassName) &&
        Objects.equals(this.generatedByLlm, suggestionGenerationMetadata.generatedByLlm) &&
        Objects.equals(this.llmProvider, suggestionGenerationMetadata.llmProvider) &&
        Objects.equals(this.llmModel, suggestionGenerationMetadata.llmModel) &&
        Objects.equals(this.promptCode, suggestionGenerationMetadata.promptCode) &&
        Objects.equals(this.promptVersion, suggestionGenerationMetadata.promptVersion) &&
        Objects.equals(this.warnings, suggestionGenerationMetadata.warnings) &&
        Objects.equals(this.agentDefinitionId, suggestionGenerationMetadata.agentDefinitionId) &&
        Objects.equals(this.agentRunId, suggestionGenerationMetadata.agentRunId) &&
        Objects.equals(this.agentOutputId, suggestionGenerationMetadata.agentOutputId) &&
        Objects.equals(this.artifactType, suggestionGenerationMetadata.artifactType) &&
        Objects.equals(this.artifactHash, suggestionGenerationMetadata.artifactHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interpreterType, interpreterClassName, generatedByLlm, llmProvider, llmModel, promptCode, promptVersion, warnings, agentDefinitionId, agentRunId, agentOutputId, artifactType, artifactHash);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionGenerationMetadata {\n");
    
    sb.append("    interpreterType: ").append(toIndentedString(interpreterType)).append("\n");
    sb.append("    interpreterClassName: ").append(toIndentedString(interpreterClassName)).append("\n");
    sb.append("    generatedByLlm: ").append(toIndentedString(generatedByLlm)).append("\n");
    sb.append("    llmProvider: ").append(toIndentedString(llmProvider)).append("\n");
    sb.append("    llmModel: ").append(toIndentedString(llmModel)).append("\n");
    sb.append("    promptCode: ").append(toIndentedString(promptCode)).append("\n");
    sb.append("    promptVersion: ").append(toIndentedString(promptVersion)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    agentRunId: ").append(toIndentedString(agentRunId)).append("\n");
    sb.append("    agentOutputId: ").append(toIndentedString(agentOutputId)).append("\n");
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    artifactHash: ").append(toIndentedString(artifactHash)).append("\n");
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
