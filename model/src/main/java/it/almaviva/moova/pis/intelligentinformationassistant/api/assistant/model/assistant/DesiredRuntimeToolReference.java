package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("DesiredRuntimeToolReference")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeToolReference   {
  private String name;
  private String version;
  private Boolean required = true;

  public DesiredRuntimeToolReference() {
  }

  @JsonCreator
  public DesiredRuntimeToolReference(
    @JsonProperty(required = true, value = "name") String name
  ) {
    this.name = name;
  }

  /**
   **/
  public DesiredRuntimeToolReference name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "name")
  @NotNull  @Size(min=1,max=150)public String getName() {
    return name;
  }

  @JsonProperty(required = true, value = "name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public DesiredRuntimeToolReference version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("version")
   @Size(max=50)public String getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public DesiredRuntimeToolReference required(Boolean required) {
    this.required = required;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("required")
  public Boolean getRequired() {
    return required;
  }

  @JsonProperty("required")
  public void setRequired(Boolean required) {
    this.required = required;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeToolReference desiredRuntimeToolReference = (DesiredRuntimeToolReference) o;
    return Objects.equals(this.name, desiredRuntimeToolReference.name) &&
        Objects.equals(this.version, desiredRuntimeToolReference.version) &&
        Objects.equals(this.required, desiredRuntimeToolReference.required);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version, required);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeToolReference {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
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
