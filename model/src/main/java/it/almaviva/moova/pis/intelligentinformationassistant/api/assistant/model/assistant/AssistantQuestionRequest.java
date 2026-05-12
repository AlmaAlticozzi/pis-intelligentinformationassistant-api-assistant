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
 * Natural language question submitted by the operator to the Intelligent Information Assistant.  The request is intentionally generic and contains only the question text. Stop point, journey, date, time window and domain context must be extracted or resolved by the backend when possible. 
 **/
@ApiModel(description = "Natural language question submitted by the operator to the Intelligent Information Assistant.  The request is intentionally generic and contains only the question text. Stop point, journey, date, time window and domain context must be extracted or resolved by the backend when possible. ")
@JsonTypeName("AssistantQuestionRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantQuestionRequest   {
  private String question;

  public AssistantQuestionRequest() {
  }

  @JsonCreator
  public AssistantQuestionRequest(
    @JsonProperty(required = true, value = "question") String question
  ) {
    this.question = question;
  }

  /**
   * Natural language question asked by the operator.
   **/
  public AssistantQuestionRequest question(String question) {
    this.question = question;
    return this;
  }

  
  @ApiModelProperty(example = "Quali corse partono da Torino alle 13?", required = true, value = "Natural language question asked by the operator.")
  @JsonProperty(required = true, value = "question")
  @NotNull  @Size(min=1,max=2000)public String getQuestion() {
    return question;
  }

  @JsonProperty(required = true, value = "question")
  public void setQuestion(String question) {
    this.question = question;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssistantQuestionRequest assistantQuestionRequest = (AssistantQuestionRequest) o;
    return Objects.equals(this.question, assistantQuestionRequest.question);
  }

  @Override
  public int hashCode() {
    return Objects.hash(question);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantQuestionRequest {\n");
    
    sb.append("    question: ").append(toIndentedString(question)).append("\n");
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
