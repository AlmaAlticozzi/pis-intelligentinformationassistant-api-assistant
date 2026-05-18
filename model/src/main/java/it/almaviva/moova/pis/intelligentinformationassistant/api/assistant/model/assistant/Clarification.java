package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ClarificationOption;
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
 * Clarification request returned when the assistant needs more information.
 **/
@ApiModel(description = "Clarification request returned when the assistant needs more information.")
@JsonTypeName("Clarification")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class Clarification   {
  private String reason;
  private @Valid List<@Valid ClarificationOption> options = new ArrayList<>();

  public Clarification() {
  }

  /**
   **/
  public Clarification reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(example = "Multiple stop points match the location name Torino.", value = "")
  @JsonProperty("reason")
  public String getReason() {
    return reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   **/
  public Clarification options(List<@Valid ClarificationOption> options) {
    this.options = options;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("options")
  @Valid public List<@Valid ClarificationOption> getOptions() {
    return options;
  }

  @JsonProperty("options")
  public void setOptions(List<@Valid ClarificationOption> options) {
    this.options = options;
  }

  public Clarification addOptionsItem(ClarificationOption optionsItem) {
    if (this.options == null) {
      this.options = new ArrayList<>();
    }

    this.options.add(optionsItem);
    return this;
  }

  public Clarification removeOptionsItem(ClarificationOption optionsItem) {
    if (optionsItem != null && this.options != null) {
      this.options.remove(optionsItem);
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
    Clarification clarification = (Clarification) o;
    return Objects.equals(this.reason, clarification.reason) &&
        Objects.equals(this.options, clarification.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reason, options);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Clarification {\n");
    
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    options: ").append(toIndentedString(options)).append("\n");
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
