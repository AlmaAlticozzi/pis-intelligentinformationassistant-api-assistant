package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
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
 * Natural language question submitted by the operator.
 **/
@ApiModel(description = "Natural language question submitted by the operator.")
@JsonTypeName("AssistantQuestionRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantQuestionRequest   {
  private String question;
  private String sessionId;
  private String preferredLanguage;
  private Boolean includeItems = true;
  private Boolean includeToolExecutions = false;
  private @Valid Map<String, Object> uiContext = new HashMap<>();

  public AssistantQuestionRequest() {
  }

  @JsonCreator
  public AssistantQuestionRequest(
    @JsonProperty(required = true, value = "question") String question
  ) {
    this.question = question;
  }

  /**
   **/
  public AssistantQuestionRequest question(String question) {
    this.question = question;
    return this;
  }

  
  @ApiModelProperty(example = "Quali corse partono da Torino alle 13?", required = true, value = "")
  @JsonProperty(required = true, value = "question")
  @NotNull  @Size(min=1,max=2000)public String getQuestion() {
    return question;
  }

  @JsonProperty(required = true, value = "question")
  public void setQuestion(String question) {
    this.question = question;
  }

  /**
   **/
  public AssistantQuestionRequest sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  
  @ApiModelProperty(example = "SSSN2026251400000001", value = "")
  @JsonProperty("sessionId")
   @Size(max=50)public String getSessionId() {
    return sessionId;
  }

  @JsonProperty("sessionId")
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   **/
  public AssistantQuestionRequest preferredLanguage(String preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
    return this;
  }

  
  @ApiModelProperty(example = "it-IT", value = "")
  @JsonProperty("preferredLanguage")
  public String getPreferredLanguage() {
    return preferredLanguage;
  }

  @JsonProperty("preferredLanguage")
  public void setPreferredLanguage(String preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
  }

  /**
   **/
  public AssistantQuestionRequest includeItems(Boolean includeItems) {
    this.includeItems = includeItems;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("includeItems")
  public Boolean getIncludeItems() {
    return includeItems;
  }

  @JsonProperty("includeItems")
  public void setIncludeItems(Boolean includeItems) {
    this.includeItems = includeItems;
  }

  /**
   * When true, the response includes compact tool execution summaries. Full traces remain available through diagnostics.
   **/
  public AssistantQuestionRequest includeToolExecutions(Boolean includeToolExecutions) {
    this.includeToolExecutions = includeToolExecutions;
    return this;
  }

  
  @ApiModelProperty(value = "When true, the response includes compact tool execution summaries. Full traces remain available through diagnostics.")
  @JsonProperty("includeToolExecutions")
  public Boolean getIncludeToolExecutions() {
    return includeToolExecutions;
  }

  @JsonProperty("includeToolExecutions")
  public void setIncludeToolExecutions(Boolean includeToolExecutions) {
    this.includeToolExecutions = includeToolExecutions;
  }

  /**
   * Optional UI context. It must never be mandatory for answering a question.
   **/
  public AssistantQuestionRequest uiContext(Map<String, Object> uiContext) {
    this.uiContext = uiContext;
    return this;
  }

  
  @ApiModelProperty(value = "Optional UI context. It must never be mandatory for answering a question.")
  @JsonProperty("uiContext")
  public Map<String, Object> getUiContext() {
    return uiContext;
  }

  @JsonProperty("uiContext")
  public void setUiContext(Map<String, Object> uiContext) {
    this.uiContext = uiContext;
  }

  public AssistantQuestionRequest putUiContextItem(String key, Object uiContextItem) {
    if (this.uiContext == null) {
      this.uiContext = new HashMap<>();
    }

    this.uiContext.put(key, uiContextItem);
    return this;
  }

  public AssistantQuestionRequest removeUiContextItem(String key) {
    if (this.uiContext != null) {
      this.uiContext.remove(key);
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
    AssistantQuestionRequest assistantQuestionRequest = (AssistantQuestionRequest) o;
    return Objects.equals(this.question, assistantQuestionRequest.question) &&
        Objects.equals(this.sessionId, assistantQuestionRequest.sessionId) &&
        Objects.equals(this.preferredLanguage, assistantQuestionRequest.preferredLanguage) &&
        Objects.equals(this.includeItems, assistantQuestionRequest.includeItems) &&
        Objects.equals(this.includeToolExecutions, assistantQuestionRequest.includeToolExecutions) &&
        Objects.equals(this.uiContext, assistantQuestionRequest.uiContext);
  }

  @Override
  public int hashCode() {
    return Objects.hash(question, sessionId, preferredLanguage, includeItems, includeToolExecutions, uiContext);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantQuestionRequest {\n");
    
    sb.append("    question: ").append(toIndentedString(question)).append("\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    preferredLanguage: ").append(toIndentedString(preferredLanguage)).append("\n");
    sb.append("    includeItems: ").append(toIndentedString(includeItems)).append("\n");
    sb.append("    includeToolExecutions: ").append(toIndentedString(includeToolExecutions)).append("\n");
    sb.append("    uiContext: ").append(toIndentedString(uiContext)).append("\n");
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
