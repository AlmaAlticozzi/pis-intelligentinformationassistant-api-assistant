package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRunLogEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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



@JsonTypeName("AgentRunLogListResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentRunLogListResponse   {
  private @Valid List<@Valid AgentRunLogEntry> items = new ArrayList<>();
  private String externalLogUrl;

  public AgentRunLogListResponse() {
  }

  /**
   **/
  public AgentRunLogListResponse items(List<@Valid AgentRunLogEntry> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("items")
  @Valid public List<@Valid AgentRunLogEntry> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<@Valid AgentRunLogEntry> items) {
    this.items = items;
  }

  public AgentRunLogListResponse addItemsItem(AgentRunLogEntry itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public AgentRunLogListResponse removeItemsItem(AgentRunLogEntry itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
    }

    return this;
  }
  /**
   **/
  public AgentRunLogListResponse externalLogUrl(String externalLogUrl) {
    this.externalLogUrl = externalLogUrl;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("externalLogUrl")
  public String getExternalLogUrl() {
    return externalLogUrl;
  }

  @JsonProperty("externalLogUrl")
  public void setExternalLogUrl(String externalLogUrl) {
    this.externalLogUrl = externalLogUrl;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRunLogListResponse agentRunLogListResponse = (AgentRunLogListResponse) o;
    return Objects.equals(this.items, agentRunLogListResponse.items) &&
        Objects.equals(this.externalLogUrl, agentRunLogListResponse.externalLogUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, externalLogUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRunLogListResponse {\n");
    
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    externalLogUrl: ").append(toIndentedString(externalLogUrl)).append("\n");
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
