package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.OffsetDateTime;
import java.util.HashMap;
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
 * Assistant suggestion shown to the operator.
 **/
@ApiModel(description = "Assistant suggestion shown to the operator.")
@JsonTypeName("Suggestion")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class Suggestion   {
  private UUID id;
  private SuggestionType type;
  private SuggestionStatus status;
  private SuggestionSeverity severity;
  private SourceEventType sourceEventType;
  private OffsetDateTime sourceEventGenerationTime;
  private JourneyRef journey;
  private StopPointRef stopPoint;
  private PlaceRef origin;
  private PlaceRef destination;
  private OffsetDateTime timetabledDepartureTime;
  private OffsetDateTime estimatedDepartureTime;
  private Integer departureDelayMinutes;
  private String platform;
  private String title;
  private String description;
  private String suggestedMessage;
  private String editedMessage;
  private String finalMessage;
  private String reason;
  private Double confidence;
  private Boolean generatedByLlm;
  private @Valid Map<String, Object> context = new HashMap<>();
  private @Valid Map<String, Object> uiContext = new HashMap<>();
  private String operatorUserId;
  private OffsetDateTime operatorActionTime;
  private String operatorNote;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public Suggestion() {
  }

  @JsonCreator
  public Suggestion(
    @JsonProperty(required = true, value = "id") UUID id,
    @JsonProperty(required = true, value = "type") SuggestionType type,
    @JsonProperty(required = true, value = "status") SuggestionStatus status,
    @JsonProperty(required = true, value = "severity") SuggestionSeverity severity,
    @JsonProperty(required = true, value = "title") String title,
    @JsonProperty(required = true, value = "createdAt") OffsetDateTime createdAt,
    @JsonProperty(required = true, value = "updatedAt") OffsetDateTime updatedAt
  ) {
    this.id = id;
    this.type = type;
    this.status = status;
    this.severity = severity;
    this.title = title;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * UUID identifier.
   **/
  public Suggestion id(UUID id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "7e2ff31e-7768-4a23-b1bc-51f426d42bd2", required = true, value = "UUID identifier.")
  @JsonProperty(required = true, value = "id")
  @NotNull public UUID getId() {
    return id;
  }

  @JsonProperty(required = true, value = "id")
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   **/
  public Suggestion type(SuggestionType type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "type")
  @NotNull public SuggestionType getType() {
    return type;
  }

  @JsonProperty(required = true, value = "type")
  public void setType(SuggestionType type) {
    this.type = type;
  }

  /**
   **/
  public Suggestion status(SuggestionStatus status) {
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
  public Suggestion severity(SuggestionSeverity severity) {
    this.severity = severity;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "severity")
  @NotNull public SuggestionSeverity getSeverity() {
    return severity;
  }

  @JsonProperty(required = true, value = "severity")
  public void setSeverity(SuggestionSeverity severity) {
    this.severity = severity;
  }

  /**
   **/
  public Suggestion sourceEventType(SourceEventType sourceEventType) {
    this.sourceEventType = sourceEventType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sourceEventType")
  public SourceEventType getSourceEventType() {
    return sourceEventType;
  }

  @JsonProperty("sourceEventType")
  public void setSourceEventType(SourceEventType sourceEventType) {
    this.sourceEventType = sourceEventType;
  }

  /**
   **/
  public Suggestion sourceEventGenerationTime(OffsetDateTime sourceEventGenerationTime) {
    this.sourceEventGenerationTime = sourceEventGenerationTime;
    return this;
  }

  
  @ApiModelProperty(example = "2026-05-10T13:05Z", value = "")
  @JsonProperty("sourceEventGenerationTime")
  public OffsetDateTime getSourceEventGenerationTime() {
    return sourceEventGenerationTime;
  }

  @JsonProperty("sourceEventGenerationTime")
  public void setSourceEventGenerationTime(OffsetDateTime sourceEventGenerationTime) {
    this.sourceEventGenerationTime = sourceEventGenerationTime;
  }

  /**
   **/
  public Suggestion journey(JourneyRef journey) {
    this.journey = journey;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("journey")
  @Valid public JourneyRef getJourney() {
    return journey;
  }

  @JsonProperty("journey")
  public void setJourney(JourneyRef journey) {
    this.journey = journey;
  }

  /**
   **/
  public Suggestion stopPoint(StopPointRef stopPoint) {
    this.stopPoint = stopPoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stopPoint")
  @Valid public StopPointRef getStopPoint() {
    return stopPoint;
  }

  @JsonProperty("stopPoint")
  public void setStopPoint(StopPointRef stopPoint) {
    this.stopPoint = stopPoint;
  }

  /**
   **/
  public Suggestion origin(PlaceRef origin) {
    this.origin = origin;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("origin")
  @Valid public PlaceRef getOrigin() {
    return origin;
  }

  @JsonProperty("origin")
  public void setOrigin(PlaceRef origin) {
    this.origin = origin;
  }

  /**
   **/
  public Suggestion destination(PlaceRef destination) {
    this.destination = destination;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("destination")
  @Valid public PlaceRef getDestination() {
    return destination;
  }

  @JsonProperty("destination")
  public void setDestination(PlaceRef destination) {
    this.destination = destination;
  }

  /**
   **/
  public Suggestion timetabledDepartureTime(OffsetDateTime timetabledDepartureTime) {
    this.timetabledDepartureTime = timetabledDepartureTime;
    return this;
  }

  
  @ApiModelProperty(example = "2026-05-10T13:00Z", value = "")
  @JsonProperty("timetabledDepartureTime")
  public OffsetDateTime getTimetabledDepartureTime() {
    return timetabledDepartureTime;
  }

  @JsonProperty("timetabledDepartureTime")
  public void setTimetabledDepartureTime(OffsetDateTime timetabledDepartureTime) {
    this.timetabledDepartureTime = timetabledDepartureTime;
  }

  /**
   **/
  public Suggestion estimatedDepartureTime(OffsetDateTime estimatedDepartureTime) {
    this.estimatedDepartureTime = estimatedDepartureTime;
    return this;
  }

  
  @ApiModelProperty(example = "2026-05-10T13:15Z", value = "")
  @JsonProperty("estimatedDepartureTime")
  public OffsetDateTime getEstimatedDepartureTime() {
    return estimatedDepartureTime;
  }

  @JsonProperty("estimatedDepartureTime")
  public void setEstimatedDepartureTime(OffsetDateTime estimatedDepartureTime) {
    this.estimatedDepartureTime = estimatedDepartureTime;
  }

  /**
   **/
  public Suggestion departureDelayMinutes(Integer departureDelayMinutes) {
    this.departureDelayMinutes = departureDelayMinutes;
    return this;
  }

  
  @ApiModelProperty(example = "15", value = "")
  @JsonProperty("departureDelayMinutes")
  public Integer getDepartureDelayMinutes() {
    return departureDelayMinutes;
  }

  @JsonProperty("departureDelayMinutes")
  public void setDepartureDelayMinutes(Integer departureDelayMinutes) {
    this.departureDelayMinutes = departureDelayMinutes;
  }

  /**
   **/
  public Suggestion platform(String platform) {
    this.platform = platform;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "")
  @JsonProperty("platform")
  public String getPlatform() {
    return platform;
  }

  @JsonProperty("platform")
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  /**
   **/
  public Suggestion title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(example = "Delay detected for AV 304", required = true, value = "")
  @JsonProperty(required = true, value = "title")
  @NotNull public String getTitle() {
    return title;
  }

  @JsonProperty(required = true, value = "title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   **/
  public Suggestion description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "The journey AV 304 has a relevant departure delay.", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public Suggestion suggestedMessage(String suggestedMessage) {
    this.suggestedMessage = suggestedMessage;
    return this;
  }

  
  @ApiModelProperty(example = "Si informa la gentile clientela che la corsa AV 304 partirà con circa 15 minuti di ritardo.", value = "")
  @JsonProperty("suggestedMessage")
  public String getSuggestedMessage() {
    return suggestedMessage;
  }

  @JsonProperty("suggestedMessage")
  public void setSuggestedMessage(String suggestedMessage) {
    this.suggestedMessage = suggestedMessage;
  }

  /**
   **/
  public Suggestion editedMessage(String editedMessage) {
    this.editedMessage = editedMessage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("editedMessage")
  public String getEditedMessage() {
    return editedMessage;
  }

  @JsonProperty("editedMessage")
  public void setEditedMessage(String editedMessage) {
    this.editedMessage = editedMessage;
  }

  /**
   **/
  public Suggestion finalMessage(String finalMessage) {
    this.finalMessage = finalMessage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("finalMessage")
  public String getFinalMessage() {
    return finalMessage;
  }

  @JsonProperty("finalMessage")
  public void setFinalMessage(String finalMessage) {
    this.finalMessage = finalMessage;
  }

  /**
   **/
  public Suggestion reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(example = "Departure delay greater than configured threshold.", value = "")
  @JsonProperty("reason")
  public String getReason() {
    return reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * minimum: 0
   * maximum: 1
   **/
  public Suggestion confidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }

  
  @ApiModelProperty(example = "0.92", value = "")
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
  public Suggestion generatedByLlm(Boolean generatedByLlm) {
    this.generatedByLlm = generatedByLlm;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("generatedByLlm")
  public Boolean getGeneratedByLlm() {
    return generatedByLlm;
  }

  @JsonProperty("generatedByLlm")
  public void setGeneratedByLlm(Boolean generatedByLlm) {
    this.generatedByLlm = generatedByLlm;
  }

  /**
   * Generic context used to generate or explain the suggestion.
   **/
  public Suggestion context(Map<String, Object> context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(value = "Generic context used to generate or explain the suggestion.")
  @JsonProperty("context")
  public Map<String, Object> getContext() {
    return context;
  }

  @JsonProperty("context")
  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  public Suggestion putContextItem(String key, Object contextItem) {
    if (this.context == null) {
      this.context = new HashMap<>();
    }

    this.context.put(key, contextItem);
    return this;
  }

  public Suggestion removeContextItem(String key) {
    if (this.context != null) {
      this.context.remove(key);
    }

    return this;
  }
  /**
   * Optional UI-oriented context used by the console.
   **/
  public Suggestion uiContext(Map<String, Object> uiContext) {
    this.uiContext = uiContext;
    return this;
  }

  
  @ApiModelProperty(value = "Optional UI-oriented context used by the console.")
  @JsonProperty("uiContext")
  public Map<String, Object> getUiContext() {
    return uiContext;
  }

  @JsonProperty("uiContext")
  public void setUiContext(Map<String, Object> uiContext) {
    this.uiContext = uiContext;
  }

  public Suggestion putUiContextItem(String key, Object uiContextItem) {
    if (this.uiContext == null) {
      this.uiContext = new HashMap<>();
    }

    this.uiContext.put(key, uiContextItem);
    return this;
  }

  public Suggestion removeUiContextItem(String key) {
    if (this.uiContext != null) {
      this.uiContext.remove(key);
    }

    return this;
  }
  /**
   **/
  public Suggestion operatorUserId(String operatorUserId) {
    this.operatorUserId = operatorUserId;
    return this;
  }

  
  @ApiModelProperty(example = "m.alticozzi", value = "")
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
  public Suggestion operatorActionTime(OffsetDateTime operatorActionTime) {
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
  public Suggestion operatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("operatorNote")
  public String getOperatorNote() {
    return operatorNote;
  }

  @JsonProperty("operatorNote")
  public void setOperatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
  }

  /**
   **/
  public Suggestion createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "createdAt")
  @NotNull public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty(required = true, value = "createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public Suggestion updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "updatedAt")
  @NotNull public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty(required = true, value = "updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Suggestion suggestion = (Suggestion) o;
    return Objects.equals(this.id, suggestion.id) &&
        Objects.equals(this.type, suggestion.type) &&
        Objects.equals(this.status, suggestion.status) &&
        Objects.equals(this.severity, suggestion.severity) &&
        Objects.equals(this.sourceEventType, suggestion.sourceEventType) &&
        Objects.equals(this.sourceEventGenerationTime, suggestion.sourceEventGenerationTime) &&
        Objects.equals(this.journey, suggestion.journey) &&
        Objects.equals(this.stopPoint, suggestion.stopPoint) &&
        Objects.equals(this.origin, suggestion.origin) &&
        Objects.equals(this.destination, suggestion.destination) &&
        Objects.equals(this.timetabledDepartureTime, suggestion.timetabledDepartureTime) &&
        Objects.equals(this.estimatedDepartureTime, suggestion.estimatedDepartureTime) &&
        Objects.equals(this.departureDelayMinutes, suggestion.departureDelayMinutes) &&
        Objects.equals(this.platform, suggestion.platform) &&
        Objects.equals(this.title, suggestion.title) &&
        Objects.equals(this.description, suggestion.description) &&
        Objects.equals(this.suggestedMessage, suggestion.suggestedMessage) &&
        Objects.equals(this.editedMessage, suggestion.editedMessage) &&
        Objects.equals(this.finalMessage, suggestion.finalMessage) &&
        Objects.equals(this.reason, suggestion.reason) &&
        Objects.equals(this.confidence, suggestion.confidence) &&
        Objects.equals(this.generatedByLlm, suggestion.generatedByLlm) &&
        Objects.equals(this.context, suggestion.context) &&
        Objects.equals(this.uiContext, suggestion.uiContext) &&
        Objects.equals(this.operatorUserId, suggestion.operatorUserId) &&
        Objects.equals(this.operatorActionTime, suggestion.operatorActionTime) &&
        Objects.equals(this.operatorNote, suggestion.operatorNote) &&
        Objects.equals(this.createdAt, suggestion.createdAt) &&
        Objects.equals(this.updatedAt, suggestion.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, status, severity, sourceEventType, sourceEventGenerationTime, journey, stopPoint, origin, destination, timetabledDepartureTime, estimatedDepartureTime, departureDelayMinutes, platform, title, description, suggestedMessage, editedMessage, finalMessage, reason, confidence, generatedByLlm, context, uiContext, operatorUserId, operatorActionTime, operatorNote, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Suggestion {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    sourceEventType: ").append(toIndentedString(sourceEventType)).append("\n");
    sb.append("    sourceEventGenerationTime: ").append(toIndentedString(sourceEventGenerationTime)).append("\n");
    sb.append("    journey: ").append(toIndentedString(journey)).append("\n");
    sb.append("    stopPoint: ").append(toIndentedString(stopPoint)).append("\n");
    sb.append("    origin: ").append(toIndentedString(origin)).append("\n");
    sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
    sb.append("    timetabledDepartureTime: ").append(toIndentedString(timetabledDepartureTime)).append("\n");
    sb.append("    estimatedDepartureTime: ").append(toIndentedString(estimatedDepartureTime)).append("\n");
    sb.append("    departureDelayMinutes: ").append(toIndentedString(departureDelayMinutes)).append("\n");
    sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    suggestedMessage: ").append(toIndentedString(suggestedMessage)).append("\n");
    sb.append("    editedMessage: ").append(toIndentedString(editedMessage)).append("\n");
    sb.append("    finalMessage: ").append(toIndentedString(finalMessage)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
    sb.append("    generatedByLlm: ").append(toIndentedString(generatedByLlm)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    uiContext: ").append(toIndentedString(uiContext)).append("\n");
    sb.append("    operatorUserId: ").append(toIndentedString(operatorUserId)).append("\n");
    sb.append("    operatorActionTime: ").append(toIndentedString(operatorActionTime)).append("\n");
    sb.append("    operatorNote: ").append(toIndentedString(operatorNote)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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
