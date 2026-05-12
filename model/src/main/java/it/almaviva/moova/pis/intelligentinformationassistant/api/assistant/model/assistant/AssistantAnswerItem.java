package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.OffsetDateTime;
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
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Generic structured item returned by the assistant question endpoint.
 **/
@ApiModel(description = "Generic structured item returned by the assistant question endpoint.")
@JsonTypeName("AssistantAnswerItem")
@JsonFormat(shape=JsonFormat.Shape.OBJECT)
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantAnswerItem extends HashMap<String, Object>  {
  private String type;
  private JourneyRef journey;
  private StopPointRef stopPoint;
  private PlaceRef destination;
  private OffsetDateTime timetabledDepartureTime;
  private OffsetDateTime estimatedDepartureTime;
  private Integer departureDelayMinutes;
  private Boolean cancelled;
  private String platform;
  private String announcementText;
  private String broadcastStatus;

  public AssistantAnswerItem() {
  }

  /**
   * Kind of returned item.
   **/
  public AssistantAnswerItem type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "JOURNEY", value = "Kind of returned item.")
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public AssistantAnswerItem journey(JourneyRef journey) {
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
  public AssistantAnswerItem stopPoint(StopPointRef stopPoint) {
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
  public AssistantAnswerItem destination(PlaceRef destination) {
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
  public AssistantAnswerItem timetabledDepartureTime(OffsetDateTime timetabledDepartureTime) {
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
  public AssistantAnswerItem estimatedDepartureTime(OffsetDateTime estimatedDepartureTime) {
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
  public AssistantAnswerItem departureDelayMinutes(Integer departureDelayMinutes) {
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
  public AssistantAnswerItem cancelled(Boolean cancelled) {
    this.cancelled = cancelled;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("cancelled")
  public Boolean getCancelled() {
    return cancelled;
  }

  @JsonProperty("cancelled")
  public void setCancelled(Boolean cancelled) {
    this.cancelled = cancelled;
  }

  /**
   **/
  public AssistantAnswerItem platform(String platform) {
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
  public AssistantAnswerItem announcementText(String announcementText) {
    this.announcementText = announcementText;
    return this;
  }

  
  @ApiModelProperty(example = "La corsa AV 304 partirà con circa 15 minuti di ritardo.", value = "")
  @JsonProperty("announcementText")
  public String getAnnouncementText() {
    return announcementText;
  }

  @JsonProperty("announcementText")
  public void setAnnouncementText(String announcementText) {
    this.announcementText = announcementText;
  }

  /**
   **/
  public AssistantAnswerItem broadcastStatus(String broadcastStatus) {
    this.broadcastStatus = broadcastStatus;
    return this;
  }

  
  @ApiModelProperty(example = "PUBLISHED", value = "")
  @JsonProperty("broadcastStatus")
  public String getBroadcastStatus() {
    return broadcastStatus;
  }

  @JsonProperty("broadcastStatus")
  public void setBroadcastStatus(String broadcastStatus) {
    this.broadcastStatus = broadcastStatus;
  }

  /**
   * Set the additional (undeclared) property with the specified name and value.
   * Creates the property if it does not already exist, otherwise replaces it.
   * @param key the name of the property
   * @param value the value of the property
   * @return self reference
   */
  @JsonAnySetter
  public AssistantAnswerItem putAdditionalProperty(String key, Object value) {
    this.put(key, value);
    return this;
  }

  /**
   * Return the additional (undeclared) properties.
   * @return the additional (undeclared) properties
   */
  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this;
  }

  /**
   * Return the additional (undeclared) property with the specified name.
   * @param key the name of the property
   * @return the additional (undeclared) property with the specified name
   */
  public Object getAdditionalProperty(String key) {
    return this.get(key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssistantAnswerItem assistantAnswerItem = (AssistantAnswerItem) o;
    return Objects.equals(this.type, assistantAnswerItem.type) &&
        Objects.equals(this.journey, assistantAnswerItem.journey) &&
        Objects.equals(this.stopPoint, assistantAnswerItem.stopPoint) &&
        Objects.equals(this.destination, assistantAnswerItem.destination) &&
        Objects.equals(this.timetabledDepartureTime, assistantAnswerItem.timetabledDepartureTime) &&
        Objects.equals(this.estimatedDepartureTime, assistantAnswerItem.estimatedDepartureTime) &&
        Objects.equals(this.departureDelayMinutes, assistantAnswerItem.departureDelayMinutes) &&
        Objects.equals(this.cancelled, assistantAnswerItem.cancelled) &&
        Objects.equals(this.platform, assistantAnswerItem.platform) &&
        Objects.equals(this.announcementText, assistantAnswerItem.announcementText) &&
        Objects.equals(this.broadcastStatus, assistantAnswerItem.broadcastStatus) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, journey, stopPoint, destination, timetabledDepartureTime, estimatedDepartureTime, departureDelayMinutes, cancelled, platform, announcementText, broadcastStatus, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantAnswerItem {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    journey: ").append(toIndentedString(journey)).append("\n");
    sb.append("    stopPoint: ").append(toIndentedString(stopPoint)).append("\n");
    sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
    sb.append("    timetabledDepartureTime: ").append(toIndentedString(timetabledDepartureTime)).append("\n");
    sb.append("    estimatedDepartureTime: ").append(toIndentedString(estimatedDepartureTime)).append("\n");
    sb.append("    departureDelayMinutes: ").append(toIndentedString(departureDelayMinutes)).append("\n");
    sb.append("    cancelled: ").append(toIndentedString(cancelled)).append("\n");
    sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
    sb.append("    announcementText: ").append(toIndentedString(announcementText)).append("\n");
    sb.append("    broadcastStatus: ").append(toIndentedString(broadcastStatus)).append("\n");
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
