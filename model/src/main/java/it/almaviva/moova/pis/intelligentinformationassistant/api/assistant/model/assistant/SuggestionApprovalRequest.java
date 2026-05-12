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
 * Request used by the operator to approve a suggestion.
 **/
@ApiModel(description = "Request used by the operator to approve a suggestion.")
@JsonTypeName("SuggestionApprovalRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionApprovalRequest   {
  private String finalMessage;
  private String operatorNote;

  public SuggestionApprovalRequest() {
  }

  /**
   * Optional edited final message. When provided, it becomes the approved message.
   **/
  public SuggestionApprovalRequest finalMessage(String finalMessage) {
    this.finalMessage = finalMessage;
    return this;
  }

  
  @ApiModelProperty(example = "Si informa la gentile clientela che la corsa AV 304 partirà con circa 15 minuti di ritardo.", value = "Optional edited final message. When provided, it becomes the approved message.")
  @JsonProperty("finalMessage")
   @Size(max=4000)public String getFinalMessage() {
    return finalMessage;
  }

  @JsonProperty("finalMessage")
  public void setFinalMessage(String finalMessage) {
    this.finalMessage = finalMessage;
  }

  /**
   * Optional operator note.
   **/
  public SuggestionApprovalRequest operatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
    return this;
  }

  
  @ApiModelProperty(example = "Message approved after minor review.", value = "Optional operator note.")
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
    return Objects.equals(this.finalMessage, suggestionApprovalRequest.finalMessage) &&
        Objects.equals(this.operatorNote, suggestionApprovalRequest.operatorNote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(finalMessage, operatorNote);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionApprovalRequest {\n");
    
    sb.append("    finalMessage: ").append(toIndentedString(finalMessage)).append("\n");
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
