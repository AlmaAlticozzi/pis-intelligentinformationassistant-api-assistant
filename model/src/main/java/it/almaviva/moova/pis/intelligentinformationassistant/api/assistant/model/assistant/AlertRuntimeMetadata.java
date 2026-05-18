package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("AlertRuntimeMetadata")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertRuntimeMetadata   {
  public enum DeploymentStatusEnum {

    NOT_DEPLOYED(String.valueOf("NOT_DEPLOYED")), DEPLOYING(String.valueOf("DEPLOYING")), DEPLOYED(String.valueOf("DEPLOYED")), FAILED(String.valueOf("FAILED")), DISABLED(String.valueOf("DISABLED"));


    private String value;

    DeploymentStatusEnum (String v) {
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

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static DeploymentStatusEnum fromString(String s) {
        for (DeploymentStatusEnum b : DeploymentStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static DeploymentStatusEnum fromValue(String value) {
        for (DeploymentStatusEnum b : DeploymentStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private DeploymentStatusEnum deploymentStatus;
  private OffsetDateTime lastExecutionAt;
  public enum LastExecutionStatusEnum {

    SUCCESS(String.valueOf("SUCCESS")), NO_MATCH(String.valueOf("NO_MATCH")), ERROR(String.valueOf("ERROR")), SKIPPED(String.valueOf("SKIPPED"));


    private String value;

    LastExecutionStatusEnum (String v) {
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

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static LastExecutionStatusEnum fromString(String s) {
        for (LastExecutionStatusEnum b : LastExecutionStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static LastExecutionStatusEnum fromValue(String value) {
        for (LastExecutionStatusEnum b : LastExecutionStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private LastExecutionStatusEnum lastExecutionStatus;
  private String lastGeneratedSuggestionId;
  private Long executionCount;
  private String errorMessage;

  public AlertRuntimeMetadata() {
  }

  /**
   **/
  public AlertRuntimeMetadata deploymentStatus(DeploymentStatusEnum deploymentStatus) {
    this.deploymentStatus = deploymentStatus;
    return this;
  }

  
  @ApiModelProperty(example = "DEPLOYED", value = "")
  @JsonProperty("deploymentStatus")
  public DeploymentStatusEnum getDeploymentStatus() {
    return deploymentStatus;
  }

  @JsonProperty("deploymentStatus")
  public void setDeploymentStatus(DeploymentStatusEnum deploymentStatus) {
    this.deploymentStatus = deploymentStatus;
  }

  /**
   **/
  public AlertRuntimeMetadata lastExecutionAt(OffsetDateTime lastExecutionAt) {
    this.lastExecutionAt = lastExecutionAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastExecutionAt")
  public OffsetDateTime getLastExecutionAt() {
    return lastExecutionAt;
  }

  @JsonProperty("lastExecutionAt")
  public void setLastExecutionAt(OffsetDateTime lastExecutionAt) {
    this.lastExecutionAt = lastExecutionAt;
  }

  /**
   **/
  public AlertRuntimeMetadata lastExecutionStatus(LastExecutionStatusEnum lastExecutionStatus) {
    this.lastExecutionStatus = lastExecutionStatus;
    return this;
  }

  
  @ApiModelProperty(example = "SUCCESS", value = "")
  @JsonProperty("lastExecutionStatus")
  public LastExecutionStatusEnum getLastExecutionStatus() {
    return lastExecutionStatus;
  }

  @JsonProperty("lastExecutionStatus")
  public void setLastExecutionStatus(LastExecutionStatusEnum lastExecutionStatus) {
    this.lastExecutionStatus = lastExecutionStatus;
  }

  /**
   **/
  public AlertRuntimeMetadata lastGeneratedSuggestionId(String lastGeneratedSuggestionId) {
    this.lastGeneratedSuggestionId = lastGeneratedSuggestionId;
    return this;
  }

  
  @ApiModelProperty(example = "SGGS2026251400000001", value = "")
  @JsonProperty("lastGeneratedSuggestionId")
   @Size(max=50)public String getLastGeneratedSuggestionId() {
    return lastGeneratedSuggestionId;
  }

  @JsonProperty("lastGeneratedSuggestionId")
  public void setLastGeneratedSuggestionId(String lastGeneratedSuggestionId) {
    this.lastGeneratedSuggestionId = lastGeneratedSuggestionId;
  }

  /**
   **/
  public AlertRuntimeMetadata executionCount(Long executionCount) {
    this.executionCount = executionCount;
    return this;
  }

  
  @ApiModelProperty(example = "42", value = "")
  @JsonProperty("executionCount")
  public Long getExecutionCount() {
    return executionCount;
  }

  @JsonProperty("executionCount")
  public void setExecutionCount(Long executionCount) {
    this.executionCount = executionCount;
  }

  /**
   **/
  public AlertRuntimeMetadata errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("errorMessage")
  public String getErrorMessage() {
    return errorMessage;
  }

  @JsonProperty("errorMessage")
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertRuntimeMetadata alertRuntimeMetadata = (AlertRuntimeMetadata) o;
    return Objects.equals(this.deploymentStatus, alertRuntimeMetadata.deploymentStatus) &&
        Objects.equals(this.lastExecutionAt, alertRuntimeMetadata.lastExecutionAt) &&
        Objects.equals(this.lastExecutionStatus, alertRuntimeMetadata.lastExecutionStatus) &&
        Objects.equals(this.lastGeneratedSuggestionId, alertRuntimeMetadata.lastGeneratedSuggestionId) &&
        Objects.equals(this.executionCount, alertRuntimeMetadata.executionCount) &&
        Objects.equals(this.errorMessage, alertRuntimeMetadata.errorMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentStatus, lastExecutionAt, lastExecutionStatus, lastGeneratedSuggestionId, executionCount, errorMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertRuntimeMetadata {\n");
    
    sb.append("    deploymentStatus: ").append(toIndentedString(deploymentStatus)).append("\n");
    sb.append("    lastExecutionAt: ").append(toIndentedString(lastExecutionAt)).append("\n");
    sb.append("    lastExecutionStatus: ").append(toIndentedString(lastExecutionStatus)).append("\n");
    sb.append("    lastGeneratedSuggestionId: ").append(toIndentedString(lastGeneratedSuggestionId)).append("\n");
    sb.append("    executionCount: ").append(toIndentedString(executionCount)).append("\n");
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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
