package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import java.time.OffsetDateTime;
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
 * Compact suggestion representation for dashboard rows.
 **/
@ApiModel(description = "Compact suggestion representation for dashboard rows.")
@JsonTypeName("SuggestionHeader")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionHeader   {
  private String id;
  private SuggestionStatus status;
  private AlertReference alert;
  private SuggestionTargetType targetType;
  private String targetTitle;
  private String targetSubtitle;
  private String eventName;
  private String reason;
  private Double confidence;
  private String operatorAdvicePreview;
  private String passengerMessagePreview;
  private Boolean hasPassengerMessage;
  private Boolean operatorAdviceEdited;
  private Boolean passengerMessageEdited;
  private OffsetDateTime generatedAt;
  private OffsetDateTime operatorActionTime;
  private String operatorUserId;
  private AgentReference generatedByAgent;
  private String agentOutputId;

  public SuggestionHeader() {
  }

  @JsonCreator
  public SuggestionHeader(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "status") SuggestionStatus status,
    @JsonProperty(required = true, value = "alert") AlertReference alert,
    @JsonProperty(required = true, value = "targetType") SuggestionTargetType targetType,
    @JsonProperty(required = true, value = "targetTitle") String targetTitle,
    @JsonProperty(required = true, value = "reason") String reason,
    @JsonProperty(required = true, value = "generatedAt") OffsetDateTime generatedAt
  ) {
    this.id = id;
    this.status = status;
    this.alert = alert;
    this.targetType = targetType;
    this.targetTitle = targetTitle;
    this.reason = reason;
    this.generatedAt = generatedAt;
  }

  /**
   **/
  public SuggestionHeader id(String id) {
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
  public SuggestionHeader status(SuggestionStatus status) {
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
  public SuggestionHeader alert(AlertReference alert) {
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
   **/
  public SuggestionHeader targetType(SuggestionTargetType targetType) {
    this.targetType = targetType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "targetType")
  @NotNull public SuggestionTargetType getTargetType() {
    return targetType;
  }

  @JsonProperty(required = true, value = "targetType")
  public void setTargetType(SuggestionTargetType targetType) {
    this.targetType = targetType;
  }

  /**
   * Main target label shown in the row.
   **/
  public SuggestionHeader targetTitle(String targetTitle) {
    this.targetTitle = targetTitle;
    return this;
  }

  
  @ApiModelProperty(example = "RV 1234", required = true, value = "Main target label shown in the row.")
  @JsonProperty(required = true, value = "targetTitle")
  @NotNull public String getTargetTitle() {
    return targetTitle;
  }

  @JsonProperty(required = true, value = "targetTitle")
  public void setTargetTitle(String targetTitle) {
    this.targetTitle = targetTitle;
  }

  /**
   * Optional target subtitle shown in the row.
   **/
  public SuggestionHeader targetSubtitle(String targetSubtitle) {
    this.targetSubtitle = targetSubtitle;
    return this;
  }

  
  @ApiModelProperty(example = "Genova Brignole - Savona", value = "Optional target subtitle shown in the row.")
  @JsonProperty("targetSubtitle")
  public String getTargetSubtitle() {
    return targetSubtitle;
  }

  @JsonProperty("targetSubtitle")
  public void setTargetSubtitle(String targetSubtitle) {
    this.targetSubtitle = targetSubtitle;
  }

  /**
   * Source event name or &#x60;AUTO&#x60; for scheduled checks.
   **/
  public SuggestionHeader eventName(String eventName) {
    this.eventName = eventName;
    return this;
  }

  
  @ApiModelProperty(example = "CANCELLATION", value = "Source event name or `AUTO` for scheduled checks.")
  @JsonProperty("eventName")
  public String getEventName() {
    return eventName;
  }

  @JsonProperty("eventName")
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  /**
   * Short reason, generated from verified context and suitable for list display.
   **/
  public SuggestionHeader reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(example = "Cancellation detected for journey RV 1234 at Genova Brignole.", required = true, value = "Short reason, generated from verified context and suitable for list display.")
  @JsonProperty(required = true, value = "reason")
  @NotNull public String getReason() {
    return reason;
  }

  @JsonProperty(required = true, value = "reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * minimum: 0
   * maximum: 1
   **/
  public SuggestionHeader confidence(Double confidence) {
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
  public SuggestionHeader operatorAdvicePreview(String operatorAdvicePreview) {
    this.operatorAdvicePreview = operatorAdvicePreview;
    return this;
  }

  
  @ApiModelProperty(example = "Inform passengers that the journey is cancelled and avoid proposing unverified alternatives.", value = "")
  @JsonProperty("operatorAdvicePreview")
  public String getOperatorAdvicePreview() {
    return operatorAdvicePreview;
  }

  @JsonProperty("operatorAdvicePreview")
  public void setOperatorAdvicePreview(String operatorAdvicePreview) {
    this.operatorAdvicePreview = operatorAdvicePreview;
  }

  /**
   **/
  public SuggestionHeader passengerMessagePreview(String passengerMessagePreview) {
    this.passengerMessagePreview = passengerMessagePreview;
    return this;
  }

  
  @ApiModelProperty(example = "Si informa la gentile clientela che la corsa RV 1234 diretta a Savona è soppressa.", value = "")
  @JsonProperty("passengerMessagePreview")
  public String getPassengerMessagePreview() {
    return passengerMessagePreview;
  }

  @JsonProperty("passengerMessagePreview")
  public void setPassengerMessagePreview(String passengerMessagePreview) {
    this.passengerMessagePreview = passengerMessagePreview;
  }

  /**
   **/
  public SuggestionHeader hasPassengerMessage(Boolean hasPassengerMessage) {
    this.hasPassengerMessage = hasPassengerMessage;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("hasPassengerMessage")
  public Boolean getHasPassengerMessage() {
    return hasPassengerMessage;
  }

  @JsonProperty("hasPassengerMessage")
  public void setHasPassengerMessage(Boolean hasPassengerMessage) {
    this.hasPassengerMessage = hasPassengerMessage;
  }

  /**
   **/
  public SuggestionHeader operatorAdviceEdited(Boolean operatorAdviceEdited) {
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
   **/
  public SuggestionHeader passengerMessageEdited(Boolean passengerMessageEdited) {
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
   **/
  public SuggestionHeader generatedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
    return this;
  }

  
  @ApiModelProperty(example = "2026-05-14T15:22:35Z", required = true, value = "")
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
  public SuggestionHeader operatorActionTime(OffsetDateTime operatorActionTime) {
    this.operatorActionTime = operatorActionTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("operatorActionTime")
  public OffsetDateTime getOperatorActionTime() {
    return operatorActionTime;
  }

  @JsonProperty("operatorActionTime")
  public void setOperatorActionTime(OffsetDateTime operatorActionTime) {
    this.operatorActionTime = operatorActionTime;
  }

  /**
   **/
  public SuggestionHeader operatorUserId(String operatorUserId) {
    this.operatorUserId = operatorUserId;
    return this;
  }

  
  @ApiModelProperty(example = "m.user", value = "")
  @JsonProperty("operatorUserId")
  public String getOperatorUserId() {
    return operatorUserId;
  }

  @JsonProperty("operatorUserId")
  public void setOperatorUserId(String operatorUserId) {
    this.operatorUserId = operatorUserId;
  }

  /**
   **/
  public SuggestionHeader generatedByAgent(AgentReference generatedByAgent) {
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
  public SuggestionHeader agentOutputId(String agentOutputId) {
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
    SuggestionHeader suggestionHeader = (SuggestionHeader) o;
    return Objects.equals(this.id, suggestionHeader.id) &&
        Objects.equals(this.status, suggestionHeader.status) &&
        Objects.equals(this.alert, suggestionHeader.alert) &&
        Objects.equals(this.targetType, suggestionHeader.targetType) &&
        Objects.equals(this.targetTitle, suggestionHeader.targetTitle) &&
        Objects.equals(this.targetSubtitle, suggestionHeader.targetSubtitle) &&
        Objects.equals(this.eventName, suggestionHeader.eventName) &&
        Objects.equals(this.reason, suggestionHeader.reason) &&
        Objects.equals(this.confidence, suggestionHeader.confidence) &&
        Objects.equals(this.operatorAdvicePreview, suggestionHeader.operatorAdvicePreview) &&
        Objects.equals(this.passengerMessagePreview, suggestionHeader.passengerMessagePreview) &&
        Objects.equals(this.hasPassengerMessage, suggestionHeader.hasPassengerMessage) &&
        Objects.equals(this.operatorAdviceEdited, suggestionHeader.operatorAdviceEdited) &&
        Objects.equals(this.passengerMessageEdited, suggestionHeader.passengerMessageEdited) &&
        Objects.equals(this.generatedAt, suggestionHeader.generatedAt) &&
        Objects.equals(this.operatorActionTime, suggestionHeader.operatorActionTime) &&
        Objects.equals(this.operatorUserId, suggestionHeader.operatorUserId) &&
        Objects.equals(this.generatedByAgent, suggestionHeader.generatedByAgent) &&
        Objects.equals(this.agentOutputId, suggestionHeader.agentOutputId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, alert, targetType, targetTitle, targetSubtitle, eventName, reason, confidence, operatorAdvicePreview, passengerMessagePreview, hasPassengerMessage, operatorAdviceEdited, passengerMessageEdited, generatedAt, operatorActionTime, operatorUserId, generatedByAgent, agentOutputId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionHeader {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    alert: ").append(toIndentedString(alert)).append("\n");
    sb.append("    targetType: ").append(toIndentedString(targetType)).append("\n");
    sb.append("    targetTitle: ").append(toIndentedString(targetTitle)).append("\n");
    sb.append("    targetSubtitle: ").append(toIndentedString(targetSubtitle)).append("\n");
    sb.append("    eventName: ").append(toIndentedString(eventName)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
    sb.append("    operatorAdvicePreview: ").append(toIndentedString(operatorAdvicePreview)).append("\n");
    sb.append("    passengerMessagePreview: ").append(toIndentedString(passengerMessagePreview)).append("\n");
    sb.append("    hasPassengerMessage: ").append(toIndentedString(hasPassengerMessage)).append("\n");
    sb.append("    operatorAdviceEdited: ").append(toIndentedString(operatorAdviceEdited)).append("\n");
    sb.append("    passengerMessageEdited: ").append(toIndentedString(passengerMessageEdited)).append("\n");
    sb.append("    generatedAt: ").append(toIndentedString(generatedAt)).append("\n");
    sb.append("    operatorActionTime: ").append(toIndentedString(operatorActionTime)).append("\n");
    sb.append("    operatorUserId: ").append(toIndentedString(operatorUserId)).append("\n");
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
