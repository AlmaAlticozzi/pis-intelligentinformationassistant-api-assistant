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
 * Optional command body used when activating a &#x60;READY&#x60; or &#x60;DISABLED&#x60; Agent Definition. When the body is omitted, &#x60;startImmediatelyIfAllowed&#x60; defaults to &#x60;true&#x60;. The command does not execute code inside the Assistant API and the local status becomes &#x60;ACTIVE&#x60; only after successful Agent Orchestrator acceptance. 
 **/
@ApiModel(description = "Optional command body used when activating a `READY` or `DISABLED` Agent Definition. When the body is omitted, `startImmediatelyIfAllowed` defaults to `true`. The command does not execute code inside the Assistant API and the local status becomes `ACTIVE` only after successful Agent Orchestrator acceptance. ")
@JsonTypeName("AgentActivationRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-17T11:36:57.274521600+02:00[Europe/Rome]", comments = "Generator version: 7.23.0")
public class AgentActivationRequest   {
  private String note;
  private Boolean startImmediatelyIfAllowed = true;

  public AgentActivationRequest() {
  }

  /**
   * Optional operator or control-plane note propagated as non-authoritative submission metadata.
   **/
  public AgentActivationRequest note(String note) {
    this.note = note;
    return this;
  }

  
  @ApiModelProperty(value = "Optional operator or control-plane note propagated as non-authoritative submission metadata.")
  @JsonProperty("note")
   @Size(max=1000)public String getNote() {
    return note;
  }

  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }

  /**
   * Requests immediate runtime eligibility when allowed by the activation policy. It never bypasses validFrom, validTo, daily windows or other runtime governance checks.
   **/
  public AgentActivationRequest startImmediatelyIfAllowed(Boolean startImmediatelyIfAllowed) {
    this.startImmediatelyIfAllowed = startImmediatelyIfAllowed;
    return this;
  }

  
  @ApiModelProperty(value = "Requests immediate runtime eligibility when allowed by the activation policy. It never bypasses validFrom, validTo, daily windows or other runtime governance checks.")
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
