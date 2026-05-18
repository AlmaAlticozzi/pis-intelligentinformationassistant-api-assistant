package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.MonitoredAudioMessageTarget;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ServiceDataJourneyTarget;
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
import java.util.Map;
import java.util.HashMap;
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
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantAnswerItem extends HashMap<String, Object>  {
  private String type;
  private String title;
  private String subtitle;
  private ServiceDataJourneyTarget journey;
  private MonitoredAudioMessageTarget audioMessage;
  private @Valid Map<String, Object> metadata = new HashMap<>();

  public AssistantAnswerItem() {
  }

  /**
   **/
  public AssistantAnswerItem type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "JOURNEY", value = "")
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
  public AssistantAnswerItem title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(example = "AV 304", value = "")
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   **/
  public AssistantAnswerItem subtitle(String subtitle) {
    this.subtitle = subtitle;
    return this;
  }

  
  @ApiModelProperty(example = "Torino Porta Nuova - Roma Termini", value = "")
  @JsonProperty("subtitle")
  public String getSubtitle() {
    return subtitle;
  }

  @JsonProperty("subtitle")
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  /**
   **/
  public AssistantAnswerItem journey(ServiceDataJourneyTarget journey) {
    this.journey = journey;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("journey")
  @Valid public ServiceDataJourneyTarget getJourney() {
    return journey;
  }

  @JsonProperty("journey")
  public void setJourney(ServiceDataJourneyTarget journey) {
    this.journey = journey;
  }

  /**
   **/
  public AssistantAnswerItem audioMessage(MonitoredAudioMessageTarget audioMessage) {
    this.audioMessage = audioMessage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("audioMessage")
  @Valid public MonitoredAudioMessageTarget getAudioMessage() {
    return audioMessage;
  }

  @JsonProperty("audioMessage")
  public void setAudioMessage(MonitoredAudioMessageTarget audioMessage) {
    this.audioMessage = audioMessage;
  }

  /**
   **/
  public AssistantAnswerItem metadata(Map<String, Object> metadata) {
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

  public AssistantAnswerItem putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }

    this.metadata.put(key, metadataItem);
    return this;
  }

  public AssistantAnswerItem removeMetadataItem(String key) {
    if (this.metadata != null) {
      this.metadata.remove(key);
    }

    return this;
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
        Objects.equals(this.title, assistantAnswerItem.title) &&
        Objects.equals(this.subtitle, assistantAnswerItem.subtitle) &&
        Objects.equals(this.journey, assistantAnswerItem.journey) &&
        Objects.equals(this.audioMessage, assistantAnswerItem.audioMessage) &&
        Objects.equals(this.metadata, assistantAnswerItem.metadata) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, subtitle, journey, audioMessage, metadata, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantAnswerItem {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    subtitle: ").append(toIndentedString(subtitle)).append("\n");
    sb.append("    journey: ").append(toIndentedString(journey)).append("\n");
    sb.append("    audioMessage: ").append(toIndentedString(audioMessage)).append("\n");
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
