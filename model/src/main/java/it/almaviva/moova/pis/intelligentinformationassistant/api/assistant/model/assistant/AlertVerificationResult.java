package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolReference;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

/**
 * Result of the AI-assisted verification flow.
 **/
@ApiModel(description = "Result of the AI-assisted verification flow.")
@JsonTypeName("AlertVerificationResult")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertVerificationResult   {
  private AlertVerificationStatus status;
  private String summary;
  private String rejectedReason;
  private Double confidence;
  private @Valid List<SuggestionTargetType> interpretedTargetTypes = new ArrayList<>();
  private @Valid List<String> interpretedEventNames = new ArrayList<>();
  private @Valid List<@Valid ToolReference> requiredTools = new ArrayList<>();
  private @Valid List<String> safetyChecks = new ArrayList<>();
  private @Valid List<String> warnings = new ArrayList<>();
  private OffsetDateTime verifiedAt;
  private String promptCode;
  private String promptVersion;
  private String llmProvider;
  private String llmModel;

  public AlertVerificationResult() {
  }

  /**
   **/
  public AlertVerificationResult status(AlertVerificationStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public AlertVerificationStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(AlertVerificationStatus status) {
    this.status = status;
  }

  /**
   **/
  public AlertVerificationResult summary(String summary) {
    this.summary = summary;
    return this;
  }

  
  @ApiModelProperty(example = "The alert can be implemented as an event interpreter using Service Data cancellation events and Broadcast history checks.", value = "")
  @JsonProperty("summary")
  public String getSummary() {
    return summary;
  }

  @JsonProperty("summary")
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   **/
  public AlertVerificationResult rejectedReason(String rejectedReason) {
    this.rejectedReason = rejectedReason;
    return this;
  }

  
  @ApiModelProperty(example = "The requested condition requires data sources that are not available through controlled tools.", value = "")
  @JsonProperty("rejectedReason")
  public String getRejectedReason() {
    return rejectedReason;
  }

  @JsonProperty("rejectedReason")
  public void setRejectedReason(String rejectedReason) {
    this.rejectedReason = rejectedReason;
  }

  /**
   * minimum: 0
   * maximum: 1
   **/
  public AlertVerificationResult confidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }

  
  @ApiModelProperty(example = "0.86", value = "")
  @JsonProperty("confidence")
   @DecimalMin("0") @DecimalMax("1")public Double getConfidence() {
    return confidence;
  }

  @JsonProperty("confidence")
  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  /**
   **/
  public AlertVerificationResult interpretedTargetTypes(List<SuggestionTargetType> interpretedTargetTypes) {
    this.interpretedTargetTypes = interpretedTargetTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("interpretedTargetTypes")
  public List<SuggestionTargetType> getInterpretedTargetTypes() {
    return interpretedTargetTypes;
  }

  @JsonProperty("interpretedTargetTypes")
  public void setInterpretedTargetTypes(List<SuggestionTargetType> interpretedTargetTypes) {
    this.interpretedTargetTypes = interpretedTargetTypes;
  }

  public AlertVerificationResult addInterpretedTargetTypesItem(SuggestionTargetType interpretedTargetTypesItem) {
    if (this.interpretedTargetTypes == null) {
      this.interpretedTargetTypes = new ArrayList<>();
    }

    this.interpretedTargetTypes.add(interpretedTargetTypesItem);
    return this;
  }

  public AlertVerificationResult removeInterpretedTargetTypesItem(SuggestionTargetType interpretedTargetTypesItem) {
    if (interpretedTargetTypesItem != null && this.interpretedTargetTypes != null) {
      this.interpretedTargetTypes.remove(interpretedTargetTypesItem);
    }

    return this;
  }
  /**
   **/
  public AlertVerificationResult interpretedEventNames(List<String> interpretedEventNames) {
    this.interpretedEventNames = interpretedEventNames;
    return this;
  }

  
  @ApiModelProperty(example = "[\"CANCELLATION\"]", value = "")
  @JsonProperty("interpretedEventNames")
  public List<String> getInterpretedEventNames() {
    return interpretedEventNames;
  }

  @JsonProperty("interpretedEventNames")
  public void setInterpretedEventNames(List<String> interpretedEventNames) {
    this.interpretedEventNames = interpretedEventNames;
  }

  public AlertVerificationResult addInterpretedEventNamesItem(String interpretedEventNamesItem) {
    if (this.interpretedEventNames == null) {
      this.interpretedEventNames = new ArrayList<>();
    }

    this.interpretedEventNames.add(interpretedEventNamesItem);
    return this;
  }

  public AlertVerificationResult removeInterpretedEventNamesItem(String interpretedEventNamesItem) {
    if (interpretedEventNamesItem != null && this.interpretedEventNames != null) {
      this.interpretedEventNames.remove(interpretedEventNamesItem);
    }

    return this;
  }
  /**
   **/
  public AlertVerificationResult requiredTools(List<@Valid ToolReference> requiredTools) {
    this.requiredTools = requiredTools;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requiredTools")
  @Valid public List<@Valid ToolReference> getRequiredTools() {
    return requiredTools;
  }

  @JsonProperty("requiredTools")
  public void setRequiredTools(List<@Valid ToolReference> requiredTools) {
    this.requiredTools = requiredTools;
  }

  public AlertVerificationResult addRequiredToolsItem(ToolReference requiredToolsItem) {
    if (this.requiredTools == null) {
      this.requiredTools = new ArrayList<>();
    }

    this.requiredTools.add(requiredToolsItem);
    return this;
  }

  public AlertVerificationResult removeRequiredToolsItem(ToolReference requiredToolsItem) {
    if (requiredToolsItem != null && this.requiredTools != null) {
      this.requiredTools.remove(requiredToolsItem);
    }

    return this;
  }
  /**
   **/
  public AlertVerificationResult safetyChecks(List<String> safetyChecks) {
    this.safetyChecks = safetyChecks;
    return this;
  }

  
  @ApiModelProperty(example = "[\"No automatic publication\",\"No invented alternatives\",\"Backend validates generated code\"]", value = "")
  @JsonProperty("safetyChecks")
  public List<String> getSafetyChecks() {
    return safetyChecks;
  }

  @JsonProperty("safetyChecks")
  public void setSafetyChecks(List<String> safetyChecks) {
    this.safetyChecks = safetyChecks;
  }

  public AlertVerificationResult addSafetyChecksItem(String safetyChecksItem) {
    if (this.safetyChecks == null) {
      this.safetyChecks = new ArrayList<>();
    }

    this.safetyChecks.add(safetyChecksItem);
    return this;
  }

  public AlertVerificationResult removeSafetyChecksItem(String safetyChecksItem) {
    if (safetyChecksItem != null && this.safetyChecks != null) {
      this.safetyChecks.remove(safetyChecksItem);
    }

    return this;
  }
  /**
   **/
  public AlertVerificationResult warnings(List<String> warnings) {
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

  public AlertVerificationResult addWarningsItem(String warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }

    this.warnings.add(warningsItem);
    return this;
  }

  public AlertVerificationResult removeWarningsItem(String warningsItem) {
    if (warningsItem != null && this.warnings != null) {
      this.warnings.remove(warningsItem);
    }

    return this;
  }
  /**
   **/
  public AlertVerificationResult verifiedAt(OffsetDateTime verifiedAt) {
    this.verifiedAt = verifiedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("verifiedAt")
  public OffsetDateTime getVerifiedAt() {
    return verifiedAt;
  }

  @JsonProperty("verifiedAt")
  public void setVerifiedAt(OffsetDateTime verifiedAt) {
    this.verifiedAt = verifiedAt;
  }

  /**
   **/
  public AlertVerificationResult promptCode(String promptCode) {
    this.promptCode = promptCode;
    return this;
  }

  
  @ApiModelProperty(example = "VERIFY_ALERT_PROMPT", value = "")
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
  public AlertVerificationResult promptVersion(String promptVersion) {
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
  public AlertVerificationResult llmProvider(String llmProvider) {
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
  public AlertVerificationResult llmModel(String llmModel) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertVerificationResult alertVerificationResult = (AlertVerificationResult) o;
    return Objects.equals(this.status, alertVerificationResult.status) &&
        Objects.equals(this.summary, alertVerificationResult.summary) &&
        Objects.equals(this.rejectedReason, alertVerificationResult.rejectedReason) &&
        Objects.equals(this.confidence, alertVerificationResult.confidence) &&
        Objects.equals(this.interpretedTargetTypes, alertVerificationResult.interpretedTargetTypes) &&
        Objects.equals(this.interpretedEventNames, alertVerificationResult.interpretedEventNames) &&
        Objects.equals(this.requiredTools, alertVerificationResult.requiredTools) &&
        Objects.equals(this.safetyChecks, alertVerificationResult.safetyChecks) &&
        Objects.equals(this.warnings, alertVerificationResult.warnings) &&
        Objects.equals(this.verifiedAt, alertVerificationResult.verifiedAt) &&
        Objects.equals(this.promptCode, alertVerificationResult.promptCode) &&
        Objects.equals(this.promptVersion, alertVerificationResult.promptVersion) &&
        Objects.equals(this.llmProvider, alertVerificationResult.llmProvider) &&
        Objects.equals(this.llmModel, alertVerificationResult.llmModel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, summary, rejectedReason, confidence, interpretedTargetTypes, interpretedEventNames, requiredTools, safetyChecks, warnings, verifiedAt, promptCode, promptVersion, llmProvider, llmModel);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertVerificationResult {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    rejectedReason: ").append(toIndentedString(rejectedReason)).append("\n");
    sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
    sb.append("    interpretedTargetTypes: ").append(toIndentedString(interpretedTargetTypes)).append("\n");
    sb.append("    interpretedEventNames: ").append(toIndentedString(interpretedEventNames)).append("\n");
    sb.append("    requiredTools: ").append(toIndentedString(requiredTools)).append("\n");
    sb.append("    safetyChecks: ").append(toIndentedString(safetyChecks)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
    sb.append("    verifiedAt: ").append(toIndentedString(verifiedAt)).append("\n");
    sb.append("    promptCode: ").append(toIndentedString(promptCode)).append("\n");
    sb.append("    promptVersion: ").append(toIndentedString(promptVersion)).append("\n");
    sb.append("    llmProvider: ").append(toIndentedString(llmProvider)).append("\n");
    sb.append("    llmModel: ").append(toIndentedString(llmModel)).append("\n");
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
