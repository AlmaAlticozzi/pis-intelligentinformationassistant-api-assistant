package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifact;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
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



@JsonTypeName("AgentCompilationStatusResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentCompilationStatusResponse   {
  private String agentDefinitionId;
  private AgentCompilationStatus status;
  private OffsetDateTime startedAt;
  private OffsetDateTime completedAt;
  private String currentStep;
  private @Valid List<@Valid AgentCompilationStep> steps = new ArrayList<>();
  private AgentArtifact artifact;
  private @Valid List<String> errors = new ArrayList<>();
  private @Valid List<String> warnings = new ArrayList<>();

  public AgentCompilationStatusResponse() {
  }

  @JsonCreator
  public AgentCompilationStatusResponse(
    @JsonProperty(required = true, value = "agentDefinitionId") String agentDefinitionId,
    @JsonProperty(required = true, value = "status") AgentCompilationStatus status
  ) {
    this.agentDefinitionId = agentDefinitionId;
    this.status = status;
  }

  /**
   **/
  public AgentCompilationStatusResponse agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentDefinitionId")
  @NotNull  @Size(max=50)public String getAgentDefinitionId() {
    return agentDefinitionId;
  }

  @JsonProperty(required = true, value = "agentDefinitionId")
  public void setAgentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
  }

  /**
   **/
  public AgentCompilationStatusResponse status(AgentCompilationStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public AgentCompilationStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(AgentCompilationStatus status) {
    this.status = status;
  }

  /**
   **/
  public AgentCompilationStatusResponse startedAt(OffsetDateTime startedAt) {
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
  public AgentCompilationStatusResponse completedAt(OffsetDateTime completedAt) {
    this.completedAt = completedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("completedAt")
  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  @JsonProperty("completedAt")
  public void setCompletedAt(OffsetDateTime completedAt) {
    this.completedAt = completedAt;
  }

  /**
   **/
  public AgentCompilationStatusResponse currentStep(String currentStep) {
    this.currentStep = currentStep;
    return this;
  }

  
  @ApiModelProperty(example = "STATIC_ANALYSIS", value = "")
  @JsonProperty("currentStep")
  public String getCurrentStep() {
    return currentStep;
  }

  @JsonProperty("currentStep")
  public void setCurrentStep(String currentStep) {
    this.currentStep = currentStep;
  }

  /**
   **/
  public AgentCompilationStatusResponse steps(List<@Valid AgentCompilationStep> steps) {
    this.steps = steps;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("steps")
  @Valid public List<@Valid AgentCompilationStep> getSteps() {
    return steps;
  }

  @JsonProperty("steps")
  public void setSteps(List<@Valid AgentCompilationStep> steps) {
    this.steps = steps;
  }

  public AgentCompilationStatusResponse addStepsItem(AgentCompilationStep stepsItem) {
    if (this.steps == null) {
      this.steps = new ArrayList<>();
    }

    this.steps.add(stepsItem);
    return this;
  }

  public AgentCompilationStatusResponse removeStepsItem(AgentCompilationStep stepsItem) {
    if (stepsItem != null && this.steps != null) {
      this.steps.remove(stepsItem);
    }

    return this;
  }
  /**
   **/
  public AgentCompilationStatusResponse artifact(AgentArtifact artifact) {
    this.artifact = artifact;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("artifact")
  @Valid public AgentArtifact getArtifact() {
    return artifact;
  }

  @JsonProperty("artifact")
  public void setArtifact(AgentArtifact artifact) {
    this.artifact = artifact;
  }

  /**
   **/
  public AgentCompilationStatusResponse errors(List<String> errors) {
    this.errors = errors;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("errors")
  public List<String> getErrors() {
    return errors;
  }

  @JsonProperty("errors")
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public AgentCompilationStatusResponse addErrorsItem(String errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<>();
    }

    this.errors.add(errorsItem);
    return this;
  }

  public AgentCompilationStatusResponse removeErrorsItem(String errorsItem) {
    if (errorsItem != null && this.errors != null) {
      this.errors.remove(errorsItem);
    }

    return this;
  }
  /**
   **/
  public AgentCompilationStatusResponse warnings(List<String> warnings) {
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

  public AgentCompilationStatusResponse addWarningsItem(String warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }

    this.warnings.add(warningsItem);
    return this;
  }

  public AgentCompilationStatusResponse removeWarningsItem(String warningsItem) {
    if (warningsItem != null && this.warnings != null) {
      this.warnings.remove(warningsItem);
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
    AgentCompilationStatusResponse agentCompilationStatusResponse = (AgentCompilationStatusResponse) o;
    return Objects.equals(this.agentDefinitionId, agentCompilationStatusResponse.agentDefinitionId) &&
        Objects.equals(this.status, agentCompilationStatusResponse.status) &&
        Objects.equals(this.startedAt, agentCompilationStatusResponse.startedAt) &&
        Objects.equals(this.completedAt, agentCompilationStatusResponse.completedAt) &&
        Objects.equals(this.currentStep, agentCompilationStatusResponse.currentStep) &&
        Objects.equals(this.steps, agentCompilationStatusResponse.steps) &&
        Objects.equals(this.artifact, agentCompilationStatusResponse.artifact) &&
        Objects.equals(this.errors, agentCompilationStatusResponse.errors) &&
        Objects.equals(this.warnings, agentCompilationStatusResponse.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(agentDefinitionId, status, startedAt, completedAt, currentStep, steps, artifact, errors, warnings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentCompilationStatusResponse {\n");
    
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    completedAt: ").append(toIndentedString(completedAt)).append("\n");
    sb.append("    currentStep: ").append(toIndentedString(currentStep)).append("\n");
    sb.append("    steps: ").append(toIndentedString(steps)).append("\n");
    sb.append("    artifact: ").append(toIndentedString(artifact)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
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
