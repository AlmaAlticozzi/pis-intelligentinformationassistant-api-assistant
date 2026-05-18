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
 * Request used to reject a suggestion.
 **/
@ApiModel(description = "Request used to reject a suggestion.")
@JsonTypeName("SuggestionRejectionRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionRejectionRequest   {
  private String operatorNote;

  public SuggestionRejectionRequest() {
  }

  /**
   * Optional rejection note.
   **/
  public SuggestionRejectionRequest operatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
    return this;
  }

  
  @ApiModelProperty(example = "The event has already been handled by the control room.", value = "Optional rejection note.")
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
    SuggestionRejectionRequest suggestionRejectionRequest = (SuggestionRejectionRequest) o;
    return Objects.equals(this.operatorNote, suggestionRejectionRequest.operatorNote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operatorNote);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionRejectionRequest {\n");
    
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
