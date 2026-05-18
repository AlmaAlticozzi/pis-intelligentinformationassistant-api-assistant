package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.StopPointRef;
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
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Target representing one Service Data journey.
 **/
@ApiModel(description = "Target representing one Service Data journey.")
@JsonTypeName("ServiceDataJourneyTarget")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class ServiceDataJourneyTarget   {
  private String serviceDataRef;
  private String journeyName;
  private String journeyRef;
  private String lineName;
  private String originName;
  private String destinationName;
  private StopPointRef stopPoint;
  private OffsetDateTime plannedDepartureTime;
  private OffsetDateTime estimatedDepartureTime;
  private Integer delayMinutes;
  private Boolean cancelled;
  private String platform;
  private @Valid Map<String, Object> metadata = new HashMap<>();

  public ServiceDataJourneyTarget() {
  }

  @JsonCreator
  public ServiceDataJourneyTarget(
    @JsonProperty(required = true, value = "journeyName") String journeyName
  ) {
    this.journeyName = journeyName;
  }

  /**
   * Technical Service Data reference if available.
   **/
  public ServiceDataJourneyTarget serviceDataRef(String serviceDataRef) {
    this.serviceDataRef = serviceDataRef;
    return this;
  }

  
  @ApiModelProperty(example = "SD-VJ-1234", value = "Technical Service Data reference if available.")
  @JsonProperty("serviceDataRef")
  public String getServiceDataRef() {
    return serviceDataRef;
  }

  @JsonProperty("serviceDataRef")
  public void setServiceDataRef(String serviceDataRef) {
    this.serviceDataRef = serviceDataRef;
  }

  /**
   **/
  public ServiceDataJourneyTarget journeyName(String journeyName) {
    this.journeyName = journeyName;
    return this;
  }

  
  @ApiModelProperty(example = "RV 1234", required = true, value = "")
  @JsonProperty(required = true, value = "journeyName")
  @NotNull public String getJourneyName() {
    return journeyName;
  }

  @JsonProperty(required = true, value = "journeyName")
  public void setJourneyName(String journeyName) {
    this.journeyName = journeyName;
  }

  /**
   **/
  public ServiceDataJourneyTarget journeyRef(String journeyRef) {
    this.journeyRef = journeyRef;
    return this;
  }

  
  @ApiModelProperty(example = "VJ-1234", value = "")
  @JsonProperty("journeyRef")
  public String getJourneyRef() {
    return journeyRef;
  }

  @JsonProperty("journeyRef")
  public void setJourneyRef(String journeyRef) {
    this.journeyRef = journeyRef;
  }

  /**
   **/
  public ServiceDataJourneyTarget lineName(String lineName) {
    this.lineName = lineName;
    return this;
  }

  
  @ApiModelProperty(example = "Regionale Veloce", value = "")
  @JsonProperty("lineName")
  public String getLineName() {
    return lineName;
  }

  @JsonProperty("lineName")
  public void setLineName(String lineName) {
    this.lineName = lineName;
  }

  /**
   **/
  public ServiceDataJourneyTarget originName(String originName) {
    this.originName = originName;
    return this;
  }

  
  @ApiModelProperty(example = "Genova Brignole", value = "")
  @JsonProperty("originName")
  public String getOriginName() {
    return originName;
  }

  @JsonProperty("originName")
  public void setOriginName(String originName) {
    this.originName = originName;
  }

  /**
   **/
  public ServiceDataJourneyTarget destinationName(String destinationName) {
    this.destinationName = destinationName;
    return this;
  }

  
  @ApiModelProperty(example = "Savona", value = "")
  @JsonProperty("destinationName")
  public String getDestinationName() {
    return destinationName;
  }

  @JsonProperty("destinationName")
  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

  /**
   **/
  public ServiceDataJourneyTarget stopPoint(StopPointRef stopPoint) {
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
  public ServiceDataJourneyTarget plannedDepartureTime(OffsetDateTime plannedDepartureTime) {
    this.plannedDepartureTime = plannedDepartureTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("plannedDepartureTime")
  public OffsetDateTime getPlannedDepartureTime() {
    return plannedDepartureTime;
  }

  @JsonProperty("plannedDepartureTime")
  public void setPlannedDepartureTime(OffsetDateTime plannedDepartureTime) {
    this.plannedDepartureTime = plannedDepartureTime;
  }

  /**
   **/
  public ServiceDataJourneyTarget estimatedDepartureTime(OffsetDateTime estimatedDepartureTime) {
    this.estimatedDepartureTime = estimatedDepartureTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public ServiceDataJourneyTarget delayMinutes(Integer delayMinutes) {
    this.delayMinutes = delayMinutes;
    return this;
  }

  
  @ApiModelProperty(example = "15", value = "")
  @JsonProperty("delayMinutes")
  public Integer getDelayMinutes() {
    return delayMinutes;
  }

  @JsonProperty("delayMinutes")
  public void setDelayMinutes(Integer delayMinutes) {
    this.delayMinutes = delayMinutes;
  }

  /**
   **/
  public ServiceDataJourneyTarget cancelled(Boolean cancelled) {
    this.cancelled = cancelled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
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
  public ServiceDataJourneyTarget platform(String platform) {
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
  public ServiceDataJourneyTarget metadata(Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("metadata")
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public ServiceDataJourneyTarget putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }

    this.metadata.put(key, metadataItem);
    return this;
  }

  public ServiceDataJourneyTarget removeMetadataItem(String key) {
    if (this.metadata != null) {
      this.metadata.remove(key);
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
    ServiceDataJourneyTarget serviceDataJourneyTarget = (ServiceDataJourneyTarget) o;
    return Objects.equals(this.serviceDataRef, serviceDataJourneyTarget.serviceDataRef) &&
        Objects.equals(this.journeyName, serviceDataJourneyTarget.journeyName) &&
        Objects.equals(this.journeyRef, serviceDataJourneyTarget.journeyRef) &&
        Objects.equals(this.lineName, serviceDataJourneyTarget.lineName) &&
        Objects.equals(this.originName, serviceDataJourneyTarget.originName) &&
        Objects.equals(this.destinationName, serviceDataJourneyTarget.destinationName) &&
        Objects.equals(this.stopPoint, serviceDataJourneyTarget.stopPoint) &&
        Objects.equals(this.plannedDepartureTime, serviceDataJourneyTarget.plannedDepartureTime) &&
        Objects.equals(this.estimatedDepartureTime, serviceDataJourneyTarget.estimatedDepartureTime) &&
        Objects.equals(this.delayMinutes, serviceDataJourneyTarget.delayMinutes) &&
        Objects.equals(this.cancelled, serviceDataJourneyTarget.cancelled) &&
        Objects.equals(this.platform, serviceDataJourneyTarget.platform) &&
        Objects.equals(this.metadata, serviceDataJourneyTarget.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceDataRef, journeyName, journeyRef, lineName, originName, destinationName, stopPoint, plannedDepartureTime, estimatedDepartureTime, delayMinutes, cancelled, platform, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceDataJourneyTarget {\n");
    
    sb.append("    serviceDataRef: ").append(toIndentedString(serviceDataRef)).append("\n");
    sb.append("    journeyName: ").append(toIndentedString(journeyName)).append("\n");
    sb.append("    journeyRef: ").append(toIndentedString(journeyRef)).append("\n");
    sb.append("    lineName: ").append(toIndentedString(lineName)).append("\n");
    sb.append("    originName: ").append(toIndentedString(originName)).append("\n");
    sb.append("    destinationName: ").append(toIndentedString(destinationName)).append("\n");
    sb.append("    stopPoint: ").append(toIndentedString(stopPoint)).append("\n");
    sb.append("    plannedDepartureTime: ").append(toIndentedString(plannedDepartureTime)).append("\n");
    sb.append("    estimatedDepartureTime: ").append(toIndentedString(estimatedDepartureTime)).append("\n");
    sb.append("    delayMinutes: ").append(toIndentedString(delayMinutes)).append("\n");
    sb.append("    cancelled: ").append(toIndentedString(cancelled)).append("\n");
    sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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
