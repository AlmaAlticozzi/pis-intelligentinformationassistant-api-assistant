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



@JsonTypeName("AgentActivationRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentActivationRequest   {
  private String note;
  private Boolean startImmediatelyIfAllowed = true;

  public AgentActivationRequest() {
  }

  /**
   **/
  public AgentActivationRequest note(String note) {
    this.note = note;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("note")
   @Size(max=1000)public String getNote() {
    return note;
  }

  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }

  /**
   **/
  public AgentActivationRequest startImmediatelyIfAllowed(Boolean startImmediatelyIfAllowed) {
    this.startImmediatelyIfAllowed = startImmediatelyIfAllowed;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("startImmediatelyIfAllowed")
  public Boolean getStartImmediatelyIfAllowed() {
    return startImmediatelyIfAllowed;
  }

  @JsonProperty("startImmediatelyIfAllowed")
  public void setStartImmediatelyIfAllowed(Boolean startImmediatelyIfAllowed) {
    this.startImmediatelyIfAllowed = startImmediatelyIfAllowed;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentActivationRequest agentActivationRequest = (AgentActivationRequest) o;
    return Objects.equals(this.note, agentActivationRequest.note) &&
        Objects.equals(this.startImmediatelyIfAllowed, agentActivationRequest.startImmediatelyIfAllowed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(note, startImmediatelyIfAllowed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentActivationRequest {\n");
    
    sb.append("    note: ").append(toIndentedString(note)).append("\n");
    sb.append("    startImmediatelyIfAllowed: ").append(toIndentedString(startImmediatelyIfAllowed)).append("\n");
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
