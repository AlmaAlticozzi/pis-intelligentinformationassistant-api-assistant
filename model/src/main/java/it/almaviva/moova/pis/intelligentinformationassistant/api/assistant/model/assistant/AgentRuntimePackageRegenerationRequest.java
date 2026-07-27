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
 * Operational metadata for governed ACTIVE Runtime Agent Package regeneration. Raw DSL and package content are not accepted.
 **/
@ApiModel(description = "Operational metadata for governed ACTIVE Runtime Agent Package regeneration. Raw DSL and package content are not accepted.")
@JsonTypeName("AgentRuntimePackageRegenerationRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.23.0")
public class AgentRuntimePackageRegenerationRequest   {
  private String note;

  public AgentRuntimePackageRegenerationRequest() {
  }

  /**
   * Optional bounded operational note. It is audit metadata and does not alter canonical package identity.
   **/
  public AgentRuntimePackageRegenerationRequest note(String note) {
    this.note = note;
    return this;
  }

  
  @ApiModelProperty(value = "Optional bounded operational note. It is audit metadata and does not alter canonical package identity.")
  @JsonProperty("note")
   @Size(max=1000)public String getNote() {
    return note;
  }

  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRuntimePackageRegenerationRequest agentRuntimePackageRegenerationRequest = (AgentRuntimePackageRegenerationRequest) o;
    return Objects.equals(this.note, agentRuntimePackageRegenerationRequest.note);
  }

  @Override
  public int hashCode() {
    return Objects.hash(note);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRuntimePackageRegenerationRequest {\n");
    
    sb.append("    note: ").append(toIndentedString(note)).append("\n");
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
