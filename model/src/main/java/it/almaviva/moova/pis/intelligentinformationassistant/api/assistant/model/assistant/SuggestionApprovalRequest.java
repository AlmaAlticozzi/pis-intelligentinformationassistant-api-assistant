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

/**
 * Request used to approve a suggestion.
 **/
@ApiModel(description = "Request used to approve a suggestion.")
@JsonTypeName("SuggestionApprovalRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionApprovalRequest   {
  private String operatorAdvice;
  private String passengerMessage;
  private String operatorNote;

  public SuggestionApprovalRequest() {
  }

  /**
   * Optional final operator advice override.
   **/
  public SuggestionApprovalRequest operatorAdvice(String operatorAdvice) {
    this.operatorAdvice = operatorAdvice;
    return this;
  }

  
  @ApiModelProperty(value = "Optional final operator advice override.")
  @JsonProperty("operatorAdvice")
   @Size(max=4000)public String getOperatorAdvice() {
    return operatorAdvice;
  }

  @JsonProperty("operatorAdvice")
  public void setOperatorAdvice(String operatorAdvice) {
    this.operatorAdvice = operatorAdvice;
  }

  /**
   * Optional final passenger-facing message override.
   **/
  public SuggestionApprovalRequest passengerMessage(String passengerMessage) {
    this.passengerMessage = passengerMessage;
    return this;
  }

  
  @ApiModelProperty(value = "Optional final passenger-facing message override.")
  @JsonProperty("passengerMessage")
   @Size(max=4000)public String getPassengerMessage() {
    return passengerMessage;
  }

  @JsonProperty("passengerMessage")
  public void setPassengerMessage(String passengerMessage) {
    this.passengerMessage = passengerMessage;
  }

  /**
   * Optional operator note.
   **/
  public SuggestionApprovalRequest operatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
    return this;
  }

  
  @ApiModelProperty(example = "Approved after checking the operational context.", value = "Optional operator note.")
  @JsonProperty("operatorNote")
   @Size(max=1000)public String getOperatorNote() {
    return operatorNote;
  }

  @JsonProperty("operatorNote")
  public void setOperatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuggestionApprovalRequest suggestionApprovalRequest = (SuggestionApprovalRequest) o;
    return Objects.equals(this.operatorAdvice, suggestionApprovalRequest.operatorAdvice) &&
        Objects.equals(this.passengerMessage, suggestionApprovalRequest.passengerMessage) &&
        Objects.equals(this.operatorNote, suggestionApprovalRequest.operatorNote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operatorAdvice, passengerMessage, operatorNote);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionApprovalRequest {\n");
    
    sb.append("    operatorAdvice: ").append(toIndentedString(operatorAdvice)).append("\n");
    sb.append("    passengerMessage: ").append(toIndentedString(passengerMessage)).append("\n");
    sb.append("    operatorNote: ").append(toIndentedString(operatorNote)).append("\n");
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
