package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolExecutionDetail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("ToolExecutionListResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class ToolExecutionListResponse   {
  private @Valid List<ToolExecutionDetail> items = new ArrayList<>();

  public ToolExecutionListResponse() {
  }

  @JsonCreator
  public ToolExecutionListResponse(
    @JsonProperty(required = true, value = "items") List<@Valid ToolExecutionDetail> items
  ) {
    this.items = items;
  }

  /**
   **/
  public ToolExecutionListResponse items(List<ToolExecutionDetail> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "items")
  @NotNull @Valid public List<@Valid ToolExecutionDetail> getItems() {
    return items;
  }

  @JsonProperty(required = true, value = "items")
  public void setItems(List<ToolExecutionDetail> items) {
    this.items = items;
  }

  public ToolExecutionListResponse addItemsItem(ToolExecutionDetail itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public ToolExecutionListResponse removeItemsItem(ToolExecutionDetail itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
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
    ToolExecutionListResponse toolExecutionListResponse = (ToolExecutionListResponse) o;
    return Objects.equals(this.items, toolExecutionListResponse.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ToolExecutionListResponse {\n");
    
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
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
