package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("DesiredRuntimeCatalogPage")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeCatalogPage   {
  private Integer limit;
  private Integer returned;
  private String nextCursor;
  private Boolean hasMore;

  public DesiredRuntimeCatalogPage() {
  }

  @JsonCreator
  public DesiredRuntimeCatalogPage(
    @JsonProperty(required = true, value = "limit") Integer limit,
    @JsonProperty(required = true, value = "returned") Integer returned,
    @JsonProperty(required = true, value = "hasMore") Boolean hasMore
  ) {
    this.limit = limit;
    this.returned = returned;
    this.hasMore = hasMore;
  }

  /**
   * minimum: 1
   * maximum: 500
   **/
  public DesiredRuntimeCatalogPage limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "limit")
  @NotNull  @Min(1) @Max(500)public Integer getLimit() {
    return limit;
  }

  @JsonProperty(required = true, value = "limit")
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  /**
   * minimum: 0
   **/
  public DesiredRuntimeCatalogPage returned(Integer returned) {
    this.returned = returned;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "returned")
  @NotNull  @Min(0)public Integer getReturned() {
    return returned;
  }

  @JsonProperty(required = true, value = "returned")
  public void setReturned(Integer returned) {
    this.returned = returned;
  }

  /**
   **/
  public DesiredRuntimeCatalogPage nextCursor(String nextCursor) {
    this.nextCursor = nextCursor;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("nextCursor")
   @Size(max=1000)public String getNextCursor() {
    return nextCursor;
  }

  @JsonProperty("nextCursor")
  public void setNextCursor(String nextCursor) {
    this.nextCursor = nextCursor;
  }

  /**
   **/
  public DesiredRuntimeCatalogPage hasMore(Boolean hasMore) {
    this.hasMore = hasMore;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "hasMore")
  @NotNull public Boolean getHasMore() {
    return hasMore;
  }

  @JsonProperty(required = true, value = "hasMore")
  public void setHasMore(Boolean hasMore) {
    this.hasMore = hasMore;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeCatalogPage desiredRuntimeCatalogPage = (DesiredRuntimeCatalogPage) o;
    return Objects.equals(this.limit, desiredRuntimeCatalogPage.limit) &&
        Objects.equals(this.returned, desiredRuntimeCatalogPage.returned) &&
        Objects.equals(this.nextCursor, desiredRuntimeCatalogPage.nextCursor) &&
        Objects.equals(this.hasMore, desiredRuntimeCatalogPage.hasMore);
  }

  @Override
  public int hashCode() {
    return Objects.hash(limit, returned, nextCursor, hasMore);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeCatalogPage {\n");
    
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    returned: ").append(toIndentedString(returned)).append("\n");
    sb.append("    nextCursor: ").append(toIndentedString(nextCursor)).append("\n");
    sb.append("    hasMore: ").append(toIndentedString(hasMore)).append("\n");
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
