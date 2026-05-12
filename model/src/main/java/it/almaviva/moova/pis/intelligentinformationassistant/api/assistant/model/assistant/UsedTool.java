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

/**
 * Backend tool/domain API used to answer the question.
 **/
@ApiModel(description = "Backend tool/domain API used to answer the question.")
@JsonTypeName("UsedTool")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class UsedTool   {
  private String name;
  private String status;
  private String description;

  public UsedTool() {
  }

  @JsonCreator
  public UsedTool(
    @JsonProperty(required = true, value = "name") String name
  ) {
    this.name = name;
  }

  /**
   **/
  public UsedTool name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "TimetableTool", required = true, value = "")
  @JsonProperty(required = true, value = "name")
  @NotNull public String getName() {
    return name;
  }

  @JsonProperty(required = true, value = "name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public UsedTool status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "SUCCESS", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public UsedTool description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Resolved departures from Torino around 13:00.", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UsedTool usedTool = (UsedTool) o;
    return Objects.equals(this.name, usedTool.name) &&
        Objects.equals(this.status, usedTool.status) &&
        Objects.equals(this.description, usedTool.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, status, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UsedTool {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
