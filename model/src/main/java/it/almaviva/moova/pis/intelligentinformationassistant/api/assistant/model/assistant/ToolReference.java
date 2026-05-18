package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("ToolReference")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class ToolReference   {
  private String toolName;
  private @Valid List<String> operations = new ArrayList<>();

  public ToolReference() {
  }

  @JsonCreator
  public ToolReference(
    @JsonProperty(required = true, value = "toolName") String toolName
  ) {
    this.toolName = toolName;
  }

  /**
   **/
  public ToolReference toolName(String toolName) {
    this.toolName = toolName;
    return this;
  }

  
  @ApiModelProperty(example = "BroadcastTool", required = true, value = "")
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
  public ToolReference operations(List<String> operations) {
    this.operations = operations;
    return this;
  }

  
  @ApiModelProperty(example = "[\"findBroadcastHistory\"]", value = "")
  @JsonProperty("operations")
  public List<String> getOperations() {
    return operations;
  }

  @JsonProperty("operations")
  public void setOperations(List<String> operations) {
    this.operations = operations;
  }

  public ToolReference addOperationsItem(String operationsItem) {
    if (this.operations == null) {
      this.operations = new ArrayList<>();
    }

    this.operations.add(operationsItem);
    return this;
  }

  public ToolReference removeOperationsItem(String operationsItem) {
    if (operationsItem != null && this.operations != null) {
      this.operations.remove(operationsItem);
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
    ToolReference toolReference = (ToolReference) o;
    return Objects.equals(this.toolName, toolReference.toolName) &&
        Objects.equals(this.operations, toolReference.operations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(toolName, operations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ToolReference {\n");
    
    sb.append("    toolName: ").append(toIndentedString(toolName)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
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
