package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AssistantAnswerItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AssistantQuestionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.Clarification;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolExecutionSummary;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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



@JsonTypeName("AssistantQuestionResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantQuestionResponse   {
  private String questionId;
  private String sessionId;
  private AssistantQuestionStatus status;
  private String detectedIntent;
  private String answer;
  private @Valid Map<String, Object> entities = new HashMap<>();
  private @Valid List<AssistantAnswerItem> items = new ArrayList<>();
  private Clarification clarification;
  private @Valid List<@Valid ToolExecutionSummary> toolExecutions = new ArrayList<>();
  private @Valid List<String> warnings = new ArrayList<>();
  private OffsetDateTime createdAt;
  private OffsetDateTime answeredAt;

  public AssistantQuestionResponse() {
  }

  @JsonCreator
  public AssistantQuestionResponse(
    @JsonProperty(required = true, value = "questionId") String questionId,
    @JsonProperty(required = true, value = "sessionId") String sessionId,
    @JsonProperty(required = true, value = "status") AssistantQuestionStatus status,
    @JsonProperty(required = true, value = "answer") String answer
  ) {
    this.questionId = questionId;
    this.sessionId = sessionId;
    this.status = status;
    this.answer = answer;
  }

  /**
   **/
  public AssistantQuestionResponse questionId(String questionId) {
    this.questionId = questionId;
    return this;
  }

  
  @ApiModelProperty(example = "QSTN2026251400000001", required = true, value = "")
  @JsonProperty(required = true, value = "questionId")
  @NotNull  @Size(max=50)public String getQuestionId() {
    return questionId;
  }

  @JsonProperty(required = true, value = "questionId")
  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }

  /**
   **/
  public AssistantQuestionResponse sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  
  @ApiModelProperty(example = "SSSN2026251400000001", required = true, value = "")
  @JsonProperty(required = true, value = "sessionId")
  @NotNull  @Size(max=50)public String getSessionId() {
    return sessionId;
  }

  @JsonProperty(required = true, value = "sessionId")
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   **/
  public AssistantQuestionResponse status(AssistantQuestionStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public AssistantQuestionStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(AssistantQuestionStatus status) {
    this.status = status;
  }

  /**
   **/
  public AssistantQuestionResponse detectedIntent(String detectedIntent) {
    this.detectedIntent = detectedIntent;
    return this;
  }

  
  @ApiModelProperty(example = "SEARCH_DEPARTURES", value = "")
  @JsonProperty("detectedIntent")
  public String getDetectedIntent() {
    return detectedIntent;
  }

  @JsonProperty("detectedIntent")
  public void setDetectedIntent(String detectedIntent) {
    this.detectedIntent = detectedIntent;
  }

  /**
   **/
  public AssistantQuestionResponse answer(String answer) {
    this.answer = answer;
    return this;
  }

  
  @ApiModelProperty(example = "Da Torino alle 13:00 risultano 4 corse in partenza.", required = true, value = "")
  @JsonProperty(required = true, value = "answer")
  @NotNull public String getAnswer() {
    return answer;
  }

  @JsonProperty(required = true, value = "answer")
  public void setAnswer(String answer) {
    this.answer = answer;
  }

  /**
   **/
  public AssistantQuestionResponse entities(Map<String, Object> entities) {
    this.entities = entities;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("entities")
  public Map<String, Object> getEntities() {
    return entities;
  }

  @JsonProperty("entities")
  public void setEntities(Map<String, Object> entities) {
    this.entities = entities;
  }

  public AssistantQuestionResponse putEntitiesItem(String key, Object entitiesItem) {
    if (this.entities == null) {
      this.entities = new HashMap<>();
    }

    this.entities.put(key, entitiesItem);
    return this;
  }

  public AssistantQuestionResponse removeEntitiesItem(String key) {
    if (this.entities != null) {
      this.entities.remove(key);
    }

    return this;
  }
  /**
   **/
  public AssistantQuestionResponse items(List<AssistantAnswerItem> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("items")
  @Valid public List<@Valid AssistantAnswerItem> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<AssistantAnswerItem> items) {
    this.items = items;
  }

  public AssistantQuestionResponse addItemsItem(AssistantAnswerItem itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public AssistantQuestionResponse removeItemsItem(AssistantAnswerItem itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
    }

    return this;
  }
  /**
   **/
  public AssistantQuestionResponse clarification(Clarification clarification) {
    this.clarification = clarification;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("clarification")
  @Valid public Clarification getClarification() {
    return clarification;
  }

  @JsonProperty("clarification")
  public void setClarification(Clarification clarification) {
    this.clarification = clarification;
  }

  /**
   **/
  public AssistantQuestionResponse toolExecutions(List<@Valid ToolExecutionSummary> toolExecutions) {
    this.toolExecutions = toolExecutions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("toolExecutions")
  @Valid public List<@Valid ToolExecutionSummary> getToolExecutions() {
    return toolExecutions;
  }

  @JsonProperty("toolExecutions")
  public void setToolExecutions(List<@Valid ToolExecutionSummary> toolExecutions) {
    this.toolExecutions = toolExecutions;
  }

  public AssistantQuestionResponse addToolExecutionsItem(ToolExecutionSummary toolExecutionsItem) {
    if (this.toolExecutions == null) {
      this.toolExecutions = new ArrayList<>();
    }

    this.toolExecutions.add(toolExecutionsItem);
    return this;
  }

  public AssistantQuestionResponse removeToolExecutionsItem(ToolExecutionSummary toolExecutionsItem) {
    if (toolExecutionsItem != null && this.toolExecutions != null) {
      this.toolExecutions.remove(toolExecutionsItem);
    }

    return this;
  }
  /**
   **/
  public AssistantQuestionResponse warnings(List<String> warnings) {
    this.warnings = warnings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("warnings")
  public List<String> getWarnings() {
    return warnings;
  }

  @JsonProperty("warnings")
  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public AssistantQuestionResponse addWarningsItem(String warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }

    this.warnings.add(warningsItem);
    return this;
  }

  public AssistantQuestionResponse removeWarningsItem(String warningsItem) {
    if (warningsItem != null && this.warnings != null) {
      this.warnings.remove(warningsItem);
    }

    return this;
  }
  /**
   **/
  public AssistantQuestionResponse createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public AssistantQuestionResponse answeredAt(OffsetDateTime answeredAt) {
    this.answeredAt = answeredAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("answeredAt")
  public OffsetDateTime getAnsweredAt() {
    return answeredAt;
  }

  @JsonProperty("answeredAt")
  public void setAnsweredAt(OffsetDateTime answeredAt) {
    this.answeredAt = answeredAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssistantQuestionResponse assistantQuestionResponse = (AssistantQuestionResponse) o;
    return Objects.equals(this.questionId, assistantQuestionResponse.questionId) &&
        Objects.equals(this.sessionId, assistantQuestionResponse.sessionId) &&
        Objects.equals(this.status, assistantQuestionResponse.status) &&
        Objects.equals(this.detectedIntent, assistantQuestionResponse.detectedIntent) &&
        Objects.equals(this.answer, assistantQuestionResponse.answer) &&
        Objects.equals(this.entities, assistantQuestionResponse.entities) &&
        Objects.equals(this.items, assistantQuestionResponse.items) &&
        Objects.equals(this.clarification, assistantQuestionResponse.clarification) &&
        Objects.equals(this.toolExecutions, assistantQuestionResponse.toolExecutions) &&
        Objects.equals(this.warnings, assistantQuestionResponse.warnings) &&
        Objects.equals(this.createdAt, assistantQuestionResponse.createdAt) &&
        Objects.equals(this.answeredAt, assistantQuestionResponse.answeredAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(questionId, sessionId, status, detectedIntent, answer, entities, items, clarification, toolExecutions, warnings, createdAt, answeredAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantQuestionResponse {\n");
    
    sb.append("    questionId: ").append(toIndentedString(questionId)).append("\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    detectedIntent: ").append(toIndentedString(detectedIntent)).append("\n");
    sb.append("    answer: ").append(toIndentedString(answer)).append("\n");
    sb.append("    entities: ").append(toIndentedString(entities)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    clarification: ").append(toIndentedString(clarification)).append("\n");
    sb.append("    toolExecutions: ").append(toIndentedString(toolExecutions)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    answeredAt: ").append(toIndentedString(answeredAt)).append("\n");
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
