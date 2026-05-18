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



@JsonTypeName("ToolExecutionSummary")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class ToolExecutionSummary   {
  private String id;
  private String toolName;
  private String operationName;
  public enum StatusEnum {

    SUCCESS(String.valueOf("SUCCESS")), ERROR(String.valueOf("ERROR")), SKIPPED(String.valueOf("SKIPPED"));


    private String value;

    StatusEnum (String v) {
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
    public static StatusEnum fromString(String s) {
        for (StatusEnum b : StatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private StatusEnum status;
  private Integer durationMs;

  public ToolExecutionSummary() {
  }

  @JsonCreator
  public ToolExecutionSummary(
    @JsonProperty(required = true, value = "toolName") String toolName,
    @JsonProperty(required = true, value = "operationName") String operationName,
    @JsonProperty(required = true, value = "status") StatusEnum status
  ) {
    this.toolName = toolName;
    this.operationName = operationName;
    this.status = status;
  }

  /**
   **/
  public ToolExecutionSummary id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "1c22eae5-dbfc-447a-9d4f-a0fd00d9a394", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public ToolExecutionSummary toolName(String toolName) {
    this.toolName = toolName;
    return this;
  }

  
  @ApiModelProperty(example = "JourneyTool", required = true, value = "")
  @JsonProperty(required = true, value = "toolName")
  @NotNull public String getToolName() {
    return toolName;
  }

  @JsonProperty(required = true, value = "toolName")
  public void setToolName(String toolName) {
    this.toolName = toolName;
  }

  /**
   **/
  public ToolExecutionSummary operationName(String operationName) {
    this.operationName = operationName;
    return this;
  }

  
  @ApiModelProperty(example = "searchDepartures", required = true, value = "")
  @JsonProperty(required = true, value = "operationName")
  @NotNull public String getOperationName() {
    return operationName;
  }

  @JsonProperty(required = true, value = "operationName")
  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  /**
   **/
  public ToolExecutionSummary status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "SUCCESS", required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   **/
  public ToolExecutionSummary durationMs(Integer durationMs) {
    this.durationMs = durationMs;
    return this;
  }

  
  @ApiModelProperty(example = "128", value = "")
  @JsonProperty("durationMs")
  public Integer getDurationMs() {
    return durationMs;
  }

  @JsonProperty("durationMs")
  public void setDurationMs(Integer durationMs) {
    this.durationMs = durationMs;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ToolExecutionSummary toolExecutionSummary = (ToolExecutionSummary) o;
    return Objects.equals(this.id, toolExecutionSummary.id) &&
        Objects.equals(this.toolName, toolExecutionSummary.toolName) &&
        Objects.equals(this.operationName, toolExecutionSummary.operationName) &&
        Objects.equals(this.status, toolExecutionSummary.status) &&
        Objects.equals(this.durationMs, toolExecutionSummary.durationMs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, toolName, operationName, status, durationMs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ToolExecutionSummary {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    toolName: ").append(toIndentedString(toolName)).append("\n");
    sb.append("    operationName: ").append(toIndentedString(operationName)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    durationMs: ").append(toIndentedString(durationMs)).append("\n");
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
