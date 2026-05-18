package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.OperatorAction;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionGenerationMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTarget;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
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
 * Complete suggestion detail.
 **/
@ApiModel(description = "Complete suggestion detail.")
@JsonTypeName("SuggestionDetail")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionDetail   {
  private String id;
  private SuggestionStatus status;
  private AlertReference alert;
  private String eventName;
  private SuggestionSource source;
  private SuggestionTarget target;
  private String reason;
  private String operatorAdvice;
  private String generatedOperatorAdvice;
  private Boolean operatorAdviceEdited;
  private String passengerMessage;
  private String generatedPassengerMessage;
  private Boolean passengerMessageEdited;
  private Double confidence;
  private SuggestionGenerationMetadata generation;
  private OperatorAction operatorAction;
  private @Valid Map<String, Object> context = new HashMap<>();
  private OffsetDateTime generatedAt;
  private OffsetDateTime updatedAt;
  private AgentReference generatedByAgent;
  private String agentOutputId;

  public SuggestionDetail() {
  }

  @JsonCreator
  public SuggestionDetail(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "status") SuggestionStatus status,
    @JsonProperty(required = true, value = "alert") AlertReference alert,
    @JsonProperty(required = true, value = "target") SuggestionTarget target,
    @JsonProperty(required = true, value = "reason") String reason,
    @JsonProperty(required = true, value = "operatorAdvice") String operatorAdvice,
    @JsonProperty(required = true, value = "generatedAt") OffsetDateTime generatedAt
  ) {
    this.id = id;
    this.status = status;
    this.alert = alert;
    this.target = target;
    this.reason = reason;
    this.operatorAdvice = operatorAdvice;
    this.generatedAt = generatedAt;
  }

  /**
   **/
  public SuggestionDetail id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "SGGS2026251400000001", required = true, value = "")
  @JsonProperty(required = true, value = "id")
  @NotNull  @Size(max=50)public String getId() {
    return id;
  }

  @JsonProperty(required = true, value = "id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public SuggestionDetail status(SuggestionStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public SuggestionStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(SuggestionStatus status) {
    this.status = status;
  }

  /**
   **/
  public SuggestionDetail alert(AlertReference alert) {
    this.alert = alert;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "alert")
  @NotNull @Valid public AlertReference getAlert() {
    return alert;
  }

  @JsonProperty(required = true, value = "alert")
  public void setAlert(AlertReference alert) {
    this.alert = alert;
  }

  /**
   * Source event name or &#x60;AUTO&#x60; for scheduled interpreters.
   **/
  public SuggestionDetail eventName(String eventName) {
    this.eventName = eventName;
    return this;
  }

  
  @ApiModelProperty(example = "CANCELLATION", value = "Source event name or `AUTO` for scheduled interpreters.")
  @JsonProperty("eventName")
  public String getEventName() {
    return eventName;
  }

  @JsonProperty("eventName")
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  /**
   **/
  public SuggestionDetail source(SuggestionSource source) {
    this.source = source;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("source")
  @Valid public SuggestionSource getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(SuggestionSource source) {
    this.source = source;
  }

  /**
   **/
  public SuggestionDetail target(SuggestionTarget target) {
    this.target = target;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "target")
  @NotNull @Valid public SuggestionTarget getTarget() {
    return target;
  }

  @JsonProperty(required = true, value = "target")
  public void setTarget(SuggestionTarget target) {
    this.target = target;
  }

  /**
   * Short, operator-readable reason generated by the suggestion normalization step.
   **/
  public SuggestionDetail reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(example = "A cancellation event was detected for journey RV 1234 at Genova Brignole.", required = true, value = "Short, operator-readable reason generated by the suggestion normalization step.")
  @JsonProperty(required = true, value = "reason")
  @NotNull  @Size(max=1000)public String getReason() {
    return reason;
  }

  @JsonProperty(required = true, value = "reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Suggested operational advice for the operator.
   **/
  public SuggestionDetail operatorAdvice(String operatorAdvice) {
    this.operatorAdvice = operatorAdvice;
    return this;
  }

  
  @ApiModelProperty(example = "Inform passengers that the journey is cancelled. Avoid proposing alternatives unless they are confirmed by operational data.", required = true, value = "Suggested operational advice for the operator.")
  @JsonProperty(required = true, value = "operatorAdvice")
  @NotNull  @Size(max=4000)public String getOperatorAdvice() {
    return operatorAdvice;
  }

  @JsonProperty(required = true, value = "operatorAdvice")
  public void setOperatorAdvice(String operatorAdvice) {
    this.operatorAdvice = operatorAdvice;
  }

  /**
   * Original generated advice before operator edits.
   **/
  public SuggestionDetail generatedOperatorAdvice(String generatedOperatorAdvice) {
    this.generatedOperatorAdvice = generatedOperatorAdvice;
    return this;
  }

  
  @ApiModelProperty(value = "Original generated advice before operator edits.")
  @JsonProperty("generatedOperatorAdvice")
  public String getGeneratedOperatorAdvice() {
    return generatedOperatorAdvice;
  }

  @JsonProperty("generatedOperatorAdvice")
  public void setGeneratedOperatorAdvice(String generatedOperatorAdvice) {
    this.generatedOperatorAdvice = generatedOperatorAdvice;
  }

  /**
   **/
  public SuggestionDetail operatorAdviceEdited(Boolean operatorAdviceEdited) {
    this.operatorAdviceEdited = operatorAdviceEdited;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("operatorAdviceEdited")
  public Boolean getOperatorAdviceEdited() {
    return operatorAdviceEdited;
  }

  @JsonProperty("operatorAdviceEdited")
  public void setOperatorAdviceEdited(Boolean operatorAdviceEdited) {
    this.operatorAdviceEdited = operatorAdviceEdited;
  }

  /**
   * Suggested passenger-facing message. It can be absent when no public communication is needed.
   **/
  public SuggestionDetail passengerMessage(String passengerMessage) {
    this.passengerMessage = passengerMessage;
    return this;
  }

  
  @ApiModelProperty(example = "Si informa la gentile clientela che la corsa RV 1234 diretta a Savona è soppressa.", value = "Suggested passenger-facing message. It can be absent when no public communication is needed.")
  @JsonProperty("passengerMessage")
   @Size(max=4000)public String getPassengerMessage() {
    return passengerMessage;
  }

  @JsonProperty("passengerMessage")
  public void setPassengerMessage(String passengerMessage) {
    this.passengerMessage = passengerMessage;
  }

  /**
   * Original generated passenger message before operator edits.
   **/
  public SuggestionDetail generatedPassengerMessage(String generatedPassengerMessage) {
    this.generatedPassengerMessage = generatedPassengerMessage;
    return this;
  }

  
  @ApiModelProperty(value = "Original generated passenger message before operator edits.")
  @JsonProperty("generatedPassengerMessage")
  public String getGeneratedPassengerMessage() {
    return generatedPassengerMessage;
  }

  @JsonProperty("generatedPassengerMessage")
  public void setGeneratedPassengerMessage(String generatedPassengerMessage) {
    this.generatedPassengerMessage = generatedPassengerMessage;
  }

  /**
   **/
  public SuggestionDetail passengerMessageEdited(Boolean passengerMessageEdited) {
    this.passengerMessageEdited = passengerMessageEdited;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("passengerMessageEdited")
  public Boolean getPassengerMessageEdited() {
    return passengerMessageEdited;
  }

  @JsonProperty("passengerMessageEdited")
  public void setPassengerMessageEdited(Boolean passengerMessageEdited) {
    this.passengerMessageEdited = passengerMessageEdited;
  }

  /**
   * minimum: 0
   * maximum: 1
   **/
  public SuggestionDetail confidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }

  
  @ApiModelProperty(example = "0.91", value = "")
  @JsonProperty("confidence")
   @DecimalMin("0") @DecimalMax("1")public Double getConfidence() {
    return confidence;
  }

  @JsonProperty("confidence")
  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  /**
   **/
  public SuggestionDetail generation(SuggestionGenerationMetadata generation) {
    this.generation = generation;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generation")
  @Valid public SuggestionGenerationMetadata getGeneration() {
    return generation;
  }

  @JsonProperty("generation")
  public void setGeneration(SuggestionGenerationMetadata generation) {
    this.generation = generation;
  }

  /**
   **/
  public SuggestionDetail operatorAction(OperatorAction operatorAction) {
    this.operatorAction = operatorAction;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("operatorAction")
  @Valid public OperatorAction getOperatorAction() {
    return operatorAction;
  }

  @JsonProperty("operatorAction")
  public void setOperatorAction(OperatorAction operatorAction) {
    this.operatorAction = operatorAction;
  }

  /**
   * Compact verified context used to generate the suggestion. Intended for diagnostics, not for primary UI layout.
   **/
  public SuggestionDetail context(Map<String, Object> context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(value = "Compact verified context used to generate the suggestion. Intended for diagnostics, not for primary UI layout.")
  @JsonProperty("context")
  public Map<String, Object> getContext() {
    return context;
  }

  @JsonProperty("context")
  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  public SuggestionDetail putContextItem(String key, Object contextItem) {
    if (this.context == null) {
      this.context = new HashMap<>();
    }

    this.context.put(key, contextItem);
    return this;
  }

  public SuggestionDetail removeContextItem(String key) {
    if (this.context != null) {
      this.context.remove(key);
    }

    return this;
  }
  /**
   **/
  public SuggestionDetail generatedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "generatedAt")
  @NotNull public OffsetDateTime getGeneratedAt() {
    return generatedAt;
  }

  @JsonProperty(required = true, value = "generatedAt")
  public void setGeneratedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  /**
   **/
  public SuggestionDetail updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   **/
  public SuggestionDetail generatedByAgent(AgentReference generatedByAgent) {
    this.generatedByAgent = generatedByAgent;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generatedByAgent")
  @Valid public AgentReference getGeneratedByAgent() {
    return generatedByAgent;
  }

  @JsonProperty("generatedByAgent")
  public void setGeneratedByAgent(AgentReference generatedByAgent) {
    this.generatedByAgent = generatedByAgent;
  }

  /**
   **/
  public SuggestionDetail agentOutputId(String agentOutputId) {
    this.agentOutputId = agentOutputId;
    return this;
  }

  
  @ApiModelProperty(example = "AGOU2026251400000001", value = "")
  @JsonProperty("agentOutputId")
   @Size(max=50)public String getAgentOutputId() {
    return agentOutputId;
  }

  @JsonProperty("agentOutputId")
  public void setAgentOutputId(String agentOutputId) {
    this.agentOutputId = agentOutputId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuggestionDetail suggestionDetail = (SuggestionDetail) o;
    return Objects.equals(this.id, suggestionDetail.id) &&
        Objects.equals(this.status, suggestionDetail.status) &&
        Objects.equals(this.alert, suggestionDetail.alert) &&
        Objects.equals(this.eventName, suggestionDetail.eventName) &&
        Objects.equals(this.source, suggestionDetail.source) &&
        Objects.equals(this.target, suggestionDetail.target) &&
        Objects.equals(this.reason, suggestionDetail.reason) &&
        Objects.equals(this.operatorAdvice, suggestionDetail.operatorAdvice) &&
        Objects.equals(this.generatedOperatorAdvice, suggestionDetail.generatedOperatorAdvice) &&
        Objects.equals(this.operatorAdviceEdited, suggestionDetail.operatorAdviceEdited) &&
        Objects.equals(this.passengerMessage, suggestionDetail.passengerMessage) &&
        Objects.equals(this.generatedPassengerMessage, suggestionDetail.generatedPassengerMessage) &&
        Objects.equals(this.passengerMessageEdited, suggestionDetail.passengerMessageEdited) &&
        Objects.equals(this.confidence, suggestionDetail.confidence) &&
        Objects.equals(this.generation, suggestionDetail.generation) &&
        Objects.equals(this.operatorAction, suggestionDetail.operatorAction) &&
        Objects.equals(this.context, suggestionDetail.context) &&
        Objects.equals(this.generatedAt, suggestionDetail.generatedAt) &&
        Objects.equals(this.updatedAt, suggestionDetail.updatedAt) &&
        Objects.equals(this.generatedByAgent, suggestionDetail.generatedByAgent) &&
        Objects.equals(this.agentOutputId, suggestionDetail.agentOutputId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, alert, eventName, source, target, reason, operatorAdvice, generatedOperatorAdvice, operatorAdviceEdited, passengerMessage, generatedPassengerMessage, passengerMessageEdited, confidence, generation, operatorAction, context, generatedAt, updatedAt, generatedByAgent, agentOutputId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionDetail {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    alert: ").append(toIndentedString(alert)).append("\n");
    sb.append("    eventName: ").append(toIndentedString(eventName)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    operatorAdvice: ").append(toIndentedString(operatorAdvice)).append("\n");
    sb.append("    generatedOperatorAdvice: ").append(toIndentedString(generatedOperatorAdvice)).append("\n");
    sb.append("    operatorAdviceEdited: ").append(toIndentedString(operatorAdviceEdited)).append("\n");
    sb.append("    passengerMessage: ").append(toIndentedString(passengerMessage)).append("\n");
    sb.append("    generatedPassengerMessage: ").append(toIndentedString(generatedPassengerMessage)).append("\n");
    sb.append("    passengerMessageEdited: ").append(toIndentedString(passengerMessageEdited)).append("\n");
    sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
    sb.append("    generation: ").append(toIndentedString(generation)).append("\n");
    sb.append("    operatorAction: ").append(toIndentedString(operatorAction)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    generatedAt: ").append(toIndentedString(generatedAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    generatedByAgent: ").append(toIndentedString(generatedByAgent)).append("\n");
    sb.append("    agentOutputId: ").append(toIndentedString(agentOutputId)).append("\n");
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
