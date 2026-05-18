package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentCompilationSummary")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentCompilationSummary   {
  private AgentCompilationStatus status;
  private String currentStep;
  private OffsetDateTime completedAt;

  public AgentCompilationSummary() {
  }

  /**
   **/
  public AgentCompilationSummary status(AgentCompilationStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public AgentCompilationStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(AgentCompilationStatus status) {
    this.status = status;
  }

  /**
   **/
  public AgentCompilationSummary currentStep(String currentStep) {
    this.currentStep = currentStep;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public AgentCompilationSummary completedAt(OffsetDateTime completedAt) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentCompilationSummary agentCompilationSummary = (AgentCompilationSummary) o;
    return Objects.equals(this.status, agentCompilationSummary.status) &&
        Objects.equals(this.currentStep, agentCompilationSummary.currentStep) &&
        Objects.equals(this.completedAt, agentCompilationSummary.completedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, currentStep, completedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentCompilationSummary {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    currentStep: ").append(toIndentedString(currentStep)).append("\n");
    sb.append("    completedAt: ").append(toIndentedString(completedAt)).append("\n");
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
