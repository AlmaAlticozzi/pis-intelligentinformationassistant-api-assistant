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
 * Target representing one monitored audio message.
 **/
@ApiModel(description = "Target representing one monitored audio message.")
@JsonTypeName("MonitoredAudioMessageTarget")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class MonitoredAudioMessageTarget   {
  private String audioMessageRef;
  private String audioMessageName;
  private String journeyName;
  private StopPointRef stopPoint;
  private String messageText;
  private String language;
  private String channel;
  private String status;
  private OffsetDateTime generatedAt;
  private OffsetDateTime broadcastAt;
  private @Valid Map<String, Object> metadata = new HashMap<>();

  public MonitoredAudioMessageTarget() {
  }

  @JsonCreator
  public MonitoredAudioMessageTarget(
    @JsonProperty(required = true, value = "audioMessageRef") String audioMessageRef
  ) {
    this.audioMessageRef = audioMessageRef;
  }

  /**
   **/
  public MonitoredAudioMessageTarget audioMessageRef(String audioMessageRef) {
    this.audioMessageRef = audioMessageRef;
    return this;
  }

  
  @ApiModelProperty(example = "MAM-20260514-0001", required = true, value = "")
  @JsonProperty(required = true, value = "audioMessageRef")
  @NotNull public String getAudioMessageRef() {
    return audioMessageRef;
  }

  @JsonProperty(required = true, value = "audioMessageRef")
  public void setAudioMessageRef(String audioMessageRef) {
    this.audioMessageRef = audioMessageRef;
  }

  /**
   **/
  public MonitoredAudioMessageTarget audioMessageName(String audioMessageName) {
    this.audioMessageName = audioMessageName;
    return this;
  }

  
  @ApiModelProperty(example = "Delay announcement for RV 1234", value = "")
  @JsonProperty("audioMessageName")
  public String getAudioMessageName() {
    return audioMessageName;
  }

  @JsonProperty("audioMessageName")
  public void setAudioMessageName(String audioMessageName) {
    this.audioMessageName = audioMessageName;
  }

  /**
   **/
  public MonitoredAudioMessageTarget journeyName(String journeyName) {
    this.journeyName = journeyName;
    return this;
  }

  
  @ApiModelProperty(example = "RV 1234", value = "")
  @JsonProperty("journeyName")
  public String getJourneyName() {
    return journeyName;
  }

  @JsonProperty("journeyName")
  public void setJourneyName(String journeyName) {
    this.journeyName = journeyName;
  }

  /**
   **/
  public MonitoredAudioMessageTarget stopPoint(StopPointRef stopPoint) {
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
  public MonitoredAudioMessageTarget messageText(String messageText) {
    this.messageText = messageText;
    return this;
  }

  
  @ApiModelProperty(example = "La corsa RV 1234 partirà con circa 15 minuti di ritardo.", value = "")
  @JsonProperty("messageText")
  public String getMessageText() {
    return messageText;
  }

  @JsonProperty("messageText")
  public void setMessageText(String messageText) {
    this.messageText = messageText;
  }

  /**
   **/
  public MonitoredAudioMessageTarget language(String language) {
    this.language = language;
    return this;
  }

  
  @ApiModelProperty(example = "it-IT", value = "")
  @JsonProperty("language")
  public String getLanguage() {
    return language;
  }

  @JsonProperty("language")
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   **/
  public MonitoredAudioMessageTarget channel(String channel) {
    this.channel = channel;
    return this;
  }

  
  @ApiModelProperty(example = "AUDIO", value = "")
  @JsonProperty("channel")
  public String getChannel() {
    return channel;
  }

  @JsonProperty("channel")
  public void setChannel(String channel) {
    this.channel = channel;
  }

  /**
   **/
  public MonitoredAudioMessageTarget status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "GENERATED", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public MonitoredAudioMessageTarget generatedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generatedAt")
  public OffsetDateTime getGeneratedAt() {
    return generatedAt;
  }

  @JsonProperty("generatedAt")
  public void setGeneratedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  /**
   **/
  public MonitoredAudioMessageTarget broadcastAt(OffsetDateTime broadcastAt) {
    this.broadcastAt = broadcastAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("broadcastAt")
  public OffsetDateTime getBroadcastAt() {
    return broadcastAt;
  }

  @JsonProperty("broadcastAt")
  public void setBroadcastAt(OffsetDateTime broadcastAt) {
    this.broadcastAt = broadcastAt;
  }

  /**
   **/
  public MonitoredAudioMessageTarget metadata(Map<String, Object> metadata) {
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

  public MonitoredAudioMessageTarget putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }

    this.metadata.put(key, metadataItem);
    return this;
  }

  public MonitoredAudioMessageTarget removeMetadataItem(String key) {
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
    MonitoredAudioMessageTarget monitoredAudioMessageTarget = (MonitoredAudioMessageTarget) o;
    return Objects.equals(this.audioMessageRef, monitoredAudioMessageTarget.audioMessageRef) &&
        Objects.equals(this.audioMessageName, monitoredAudioMessageTarget.audioMessageName) &&
        Objects.equals(this.journeyName, monitoredAudioMessageTarget.journeyName) &&
        Objects.equals(this.stopPoint, monitoredAudioMessageTarget.stopPoint) &&
        Objects.equals(this.messageText, monitoredAudioMessageTarget.messageText) &&
        Objects.equals(this.language, monitoredAudioMessageTarget.language) &&
        Objects.equals(this.channel, monitoredAudioMessageTarget.channel) &&
        Objects.equals(this.status, monitoredAudioMessageTarget.status) &&
        Objects.equals(this.generatedAt, monitoredAudioMessageTarget.generatedAt) &&
        Objects.equals(this.broadcastAt, monitoredAudioMessageTarget.broadcastAt) &&
        Objects.equals(this.metadata, monitoredAudioMessageTarget.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(audioMessageRef, audioMessageName, journeyName, stopPoint, messageText, language, channel, status, generatedAt, broadcastAt, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonitoredAudioMessageTarget {\n");
    
    sb.append("    audioMessageRef: ").append(toIndentedString(audioMessageRef)).append("\n");
    sb.append("    audioMessageName: ").append(toIndentedString(audioMessageName)).append("\n");
    sb.append("    journeyName: ").append(toIndentedString(journeyName)).append("\n");
    sb.append("    stopPoint: ").append(toIndentedString(stopPoint)).append("\n");
    sb.append("    messageText: ").append(toIndentedString(messageText)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
    sb.append("    channel: ").append(toIndentedString(channel)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    generatedAt: ").append(toIndentedString(generatedAt)).append("\n");
    sb.append("    broadcastAt: ").append(toIndentedString(broadcastAt)).append("\n");
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
