package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.MonitoredAudioMessageTarget;
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

/**
 * Target representing an aggregate of monitored audio messages.
 **/
@ApiModel(description = "Target representing an aggregate of monitored audio messages.")
@JsonTypeName("MonitoredAudioMessageAggregateTarget")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class MonitoredAudioMessageAggregateTarget   {
  private String title;
  private String criteria;
  private @Valid List<@Valid MonitoredAudioMessageTarget> items = new ArrayList<>();
  private Integer totalItems;
  private @Valid Map<String, Object> metadata = new HashMap<>();

  public MonitoredAudioMessageAggregateTarget() {
  }

  @JsonCreator
  public MonitoredAudioMessageAggregateTarget(
    @JsonProperty(required = true, value = "items") List<@Valid MonitoredAudioMessageTarget> items
  ) {
    this.items = items;
  }

  /**
   **/
  public MonitoredAudioMessageAggregateTarget title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(example = "Audio messages generated but not broadcast", value = "")
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
  public MonitoredAudioMessageAggregateTarget criteria(String criteria) {
    this.criteria = criteria;
    return this;
  }

  
  @ApiModelProperty(example = "Audio messages for delayed journeys that have no successful broadcast trace.", value = "")
  @JsonProperty("criteria")
  public String getCriteria() {
    return criteria;
  }

  @JsonProperty("criteria")
  public void setCriteria(String criteria) {
    this.criteria = criteria;
  }

  /**
   **/
  public MonitoredAudioMessageAggregateTarget items(List<@Valid MonitoredAudioMessageTarget> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "items")
  @NotNull @Valid public List<@Valid MonitoredAudioMessageTarget> getItems() {
    return items;
  }

  @JsonProperty(required = true, value = "items")
  public void setItems(List<@Valid MonitoredAudioMessageTarget> items) {
    this.items = items;
  }

  public MonitoredAudioMessageAggregateTarget addItemsItem(MonitoredAudioMessageTarget itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public MonitoredAudioMessageAggregateTarget removeItemsItem(MonitoredAudioMessageTarget itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
    }

    return this;
  }
  /**
   **/
  public MonitoredAudioMessageAggregateTarget totalItems(Integer totalItems) {
    this.totalItems = totalItems;
    return this;
  }

  
  @ApiModelProperty(example = "2", value = "")
  @JsonProperty("totalItems")
  public Integer getTotalItems() {
    return totalItems;
  }

  @JsonProperty("totalItems")
  public void setTotalItems(Integer totalItems) {
    this.totalItems = totalItems;
  }

  /**
   **/
  public MonitoredAudioMessageAggregateTarget metadata(Map<String, Object> metadata) {
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

  public MonitoredAudioMessageAggregateTarget putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }

    this.metadata.put(key, metadataItem);
    return this;
  }

  public MonitoredAudioMessageAggregateTarget removeMetadataItem(String key) {
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
    MonitoredAudioMessageAggregateTarget monitoredAudioMessageAggregateTarget = (MonitoredAudioMessageAggregateTarget) o;
    return Objects.equals(this.title, monitoredAudioMessageAggregateTarget.title) &&
        Objects.equals(this.criteria, monitoredAudioMessageAggregateTarget.criteria) &&
        Objects.equals(this.items, monitoredAudioMessageAggregateTarget.items) &&
        Objects.equals(this.totalItems, monitoredAudioMessageAggregateTarget.totalItems) &&
        Objects.equals(this.metadata, monitoredAudioMessageAggregateTarget.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, criteria, items, totalItems, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonitoredAudioMessageAggregateTarget {\n");
    
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    criteria: ").append(toIndentedString(criteria)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    totalItems: ").append(toIndentedString(totalItems)).append("\n");
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
