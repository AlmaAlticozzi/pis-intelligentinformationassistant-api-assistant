package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
 * Assistant answer returned to the operator.
 **/
@ApiModel(description = "Assistant answer returned to the operator.")
@JsonTypeName("AssistantQuestionResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantQuestionResponse   {
  private UUID questionId;
  private OperatorQuestionStatus status;
  private String detectedIntent;
  private String answer;
  private @Valid Map<String, Object> resolvedEntities = new HashMap<>();
  private @Valid List<@Valid UsedTool> usedTools = new ArrayList<>();
  private @Valid List<AssistantAnswerItem> items = new ArrayList<>();
  private Boolean interpretedByLlm;
  private Boolean answeredByLlm;
  private String llmProvider;
  private String llmModel;
  private String promptCode;
  private String promptVersion;
  private OffsetDateTime createdAt;
  private OffsetDateTime answeredAt;

  public AssistantQuestionResponse() {
  }

  @JsonCreator
  public AssistantQuestionResponse(
    @JsonProperty(required = true, value = "questionId") UUID questionId,
    @JsonProperty(required = true, value = "status") OperatorQuestionStatus status,
    @JsonProperty(required = true, value = "answer") String answer
  ) {
    this.questionId = questionId;
    this.status = status;
    this.answer = answer;
  }

  /**
   * UUID identifier.
   **/
  public AssistantQuestionResponse questionId(UUID questionId) {
    this.questionId = questionId;
    return this;
  }

  
  @ApiModelProperty(example = "7e2ff31e-7768-4a23-b1bc-51f426d42bd2", required = true, value = "UUID identifier.")
  @JsonProperty(required = true, value = "questionId")
  @NotNull public UUID getQuestionId() {
    return questionId;
  }

  @JsonProperty(required = true, value = "questionId")
  public void setQuestionId(UUID questionId) {
    this.questionId = questionId;
  }

  /**
   **/
  public AssistantQuestionResponse status(OperatorQuestionStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public OperatorQuestionStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(OperatorQuestionStatus status) {
    this.status = status;
  }

  /**
   * Intent detected by the backend.
   **/
  public AssistantQuestionResponse detectedIntent(String detectedIntent) {
    this.detectedIntent = detectedIntent;
    return this;
  }

  
  @ApiModelProperty(example = "LIST_DEPARTING_JOURNEYS", value = "Intent detected by the backend.")
  @JsonProperty("detectedIntent")
  public String getDetectedIntent() {
    return detectedIntent;
  }

  @JsonProperty("detectedIntent")
  public void setDetectedIntent(String detectedIntent) {
    this.detectedIntent = detectedIntent;
  }

  /**
   * Natural language answer generated from retrieved tool results.
   **/
  public AssistantQuestionResponse answer(String answer) {
    this.answer = answer;
    return this;
  }

  
  @ApiModelProperty(example = "Da Torino alle 13:00 risultano 4 corse in partenza.", required = true, value = "Natural language answer generated from retrieved tool results.")
  @JsonProperty(required = true, value = "answer")
  @NotNull public String getAnswer() {
    return answer;
  }

  @JsonProperty(required = true, value = "answer")
  public void setAnswer(String answer) {
    this.answer = answer;
  }

  /**
   * Entities resolved from the question.
   **/
  public AssistantQuestionResponse resolvedEntities(Map<String, Object> resolvedEntities) {
    this.resolvedEntities = resolvedEntities;
    return this;
  }

  
  @ApiModelProperty(example = "{\"placeName\":\"Torino\",\"time\":\"13:00\",\"date\":\"2026-05-10\"}", value = "Entities resolved from the question.")
  @JsonProperty("resolvedEntities")
  public Map<String, Object> getResolvedEntities() {
    return resolvedEntities;
  }

  @JsonProperty("resolvedEntities")
  public void setResolvedEntities(Map<String, Object> resolvedEntities) {
    this.resolvedEntities = resolvedEntities;
  }

  public AssistantQuestionResponse putResolvedEntitiesItem(String key, Object resolvedEntitiesItem) {
    if (this.resolvedEntities == null) {
      this.resolvedEntities = new HashMap<>();
    }

    this.resolvedEntities.put(key, resolvedEntitiesItem);
    return this;
  }

  public AssistantQuestionResponse removeResolvedEntitiesItem(String key) {
    if (this.resolvedEntities != null) {
      this.resolvedEntities.remove(key);
    }

    return this;
  }
  /**
   * Tools/domain APIs used to answer the question.
   **/
  public AssistantQuestionResponse usedTools(List<@Valid UsedTool> usedTools) {
    this.usedTools = usedTools;
    return this;
  }

  
  @ApiModelProperty(value = "Tools/domain APIs used to answer the question.")
  @JsonProperty("usedTools")
  @Valid public List<@Valid UsedTool> getUsedTools() {
    return usedTools;
  }

  @JsonProperty("usedTools")
  public void setUsedTools(List<@Valid UsedTool> usedTools) {
    this.usedTools = usedTools;
  }

  public AssistantQuestionResponse addUsedToolsItem(UsedTool usedToolsItem) {
    if (this.usedTools == null) {
      this.usedTools = new ArrayList<>();
    }

    this.usedTools.add(usedToolsItem);
    return this;
  }

  public AssistantQuestionResponse removeUsedToolsItem(UsedTool usedToolsItem) {
    if (usedToolsItem != null && this.usedTools != null) {
      this.usedTools.remove(usedToolsItem);
    }

    return this;
  }
  /**
   * Structured result items supporting the natural language answer.
   **/
  public AssistantQuestionResponse items(List<AssistantAnswerItem> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(value = "Structured result items supporting the natural language answer.")
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
   * True when an LLM was used to interpret the question.
   **/
  public AssistantQuestionResponse interpretedByLlm(Boolean interpretedByLlm) {
    this.interpretedByLlm = interpretedByLlm;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "True when an LLM was used to interpret the question.")
  @JsonProperty("interpretedByLlm")
  public Boolean getInterpretedByLlm() {
    return interpretedByLlm;
  }

  @JsonProperty("interpretedByLlm")
  public void setInterpretedByLlm(Boolean interpretedByLlm) {
    this.interpretedByLlm = interpretedByLlm;
  }

  /**
   * True when an LLM was used to format the final answer.
   **/
  public AssistantQuestionResponse answeredByLlm(Boolean answeredByLlm) {
    this.answeredByLlm = answeredByLlm;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "True when an LLM was used to format the final answer.")
  @JsonProperty("answeredByLlm")
  public Boolean getAnsweredByLlm() {
    return answeredByLlm;
  }

  @JsonProperty("answeredByLlm")
  public void setAnsweredByLlm(Boolean answeredByLlm) {
    this.answeredByLlm = answeredByLlm;
  }

  /**
   **/
  public AssistantQuestionResponse llmProvider(String llmProvider) {
    this.llmProvider = llmProvider;
    return this;
  }

  
  @ApiModelProperty(example = "azure-openai", value = "")
  @JsonProperty("llmProvider")
  public String getLlmProvider() {
    return llmProvider;
  }

  @JsonProperty("llmProvider")
  public void setLlmProvider(String llmProvider) {
    this.llmProvider = llmProvider;
  }

  /**
   **/
  public AssistantQuestionResponse llmModel(String llmModel) {
    this.llmModel = llmModel;
    return this;
  }

  
  @ApiModelProperty(example = "gpt-4o-mini", value = "")
  @JsonProperty("llmModel")
  public String getLlmModel() {
    return llmModel;
  }

  @JsonProperty("llmModel")
  public void setLlmModel(String llmModel) {
    this.llmModel = llmModel;
  }

  /**
   **/
  public AssistantQuestionResponse promptCode(String promptCode) {
    this.promptCode = promptCode;
    return this;
  }

  
  @ApiModelProperty(example = "ASSISTANT_QUESTION_ORCHESTRATION", value = "")
  @JsonProperty("promptCode")
  public String getPromptCode() {
    return promptCode;
  }

  @JsonProperty("promptCode")
  public void setPromptCode(String promptCode) {
    this.promptCode = promptCode;
  }

  /**
   **/
  public AssistantQuestionResponse promptVersion(String promptVersion) {
    this.promptVersion = promptVersion;
    return this;
  }

  
  @ApiModelProperty(example = "0.0.1", value = "")
  @JsonProperty("promptVersion")
  public String getPromptVersion() {
    return promptVersion;
  }

  @JsonProperty("promptVersion")
  public void setPromptVersion(String promptVersion) {
    this.promptVersion = promptVersion;
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
        Objects.equals(this.status, assistantQuestionResponse.status) &&
        Objects.equals(this.detectedIntent, assistantQuestionResponse.detectedIntent) &&
        Objects.equals(this.answer, assistantQuestionResponse.answer) &&
        Objects.equals(this.resolvedEntities, assistantQuestionResponse.resolvedEntities) &&
        Objects.equals(this.usedTools, assistantQuestionResponse.usedTools) &&
        Objects.equals(this.items, assistantQuestionResponse.items) &&
        Objects.equals(this.interpretedByLlm, assistantQuestionResponse.interpretedByLlm) &&
        Objects.equals(this.answeredByLlm, assistantQuestionResponse.answeredByLlm) &&
        Objects.equals(this.llmProvider, assistantQuestionResponse.llmProvider) &&
        Objects.equals(this.llmModel, assistantQuestionResponse.llmModel) &&
        Objects.equals(this.promptCode, assistantQuestionResponse.promptCode) &&
        Objects.equals(this.promptVersion, assistantQuestionResponse.promptVersion) &&
        Objects.equals(this.createdAt, assistantQuestionResponse.createdAt) &&
        Objects.equals(this.answeredAt, assistantQuestionResponse.answeredAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(questionId, status, detectedIntent, answer, resolvedEntities, usedTools, items, interpretedByLlm, answeredByLlm, llmProvider, llmModel, promptCode, promptVersion, createdAt, answeredAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantQuestionResponse {\n");
    
    sb.append("    questionId: ").append(toIndentedString(questionId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    detectedIntent: ").append(toIndentedString(detectedIntent)).append("\n");
    sb.append("    answer: ").append(toIndentedString(answer)).append("\n");
    sb.append("    resolvedEntities: ").append(toIndentedString(resolvedEntities)).append("\n");
    sb.append("    usedTools: ").append(toIndentedString(usedTools)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    interpretedByLlm: ").append(toIndentedString(interpretedByLlm)).append("\n");
    sb.append("    answeredByLlm: ").append(toIndentedString(answeredByLlm)).append("\n");
    sb.append("    llmProvider: ").append(toIndentedString(llmProvider)).append("\n");
    sb.append("    llmModel: ").append(toIndentedString(llmModel)).append("\n");
    sb.append("    promptCode: ").append(toIndentedString(promptCode)).append("\n");
    sb.append("    promptVersion: ").append(toIndentedString(promptVersion)).append("\n");
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
