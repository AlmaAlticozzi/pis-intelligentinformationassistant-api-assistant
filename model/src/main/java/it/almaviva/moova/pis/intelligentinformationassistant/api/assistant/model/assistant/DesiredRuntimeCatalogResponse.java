package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
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



@JsonTypeName("DesiredRuntimeCatalogResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeCatalogResponse   {
  private String catalogVersion;
  private DesiredRuntimeCatalogMode mode;
  private OffsetDateTime generatedAt;
  private OffsetDateTime catalogAsOf;
  private String sourceCheckpoint;
  private String nextCheckpoint;
  private @Valid List<DesiredRuntimeCatalogItem> items = new ArrayList<>();
  private DesiredRuntimeCatalogPage page;

  public DesiredRuntimeCatalogResponse() {
  }

  @JsonCreator
  public DesiredRuntimeCatalogResponse(
    @JsonProperty(required = true, value = "catalogVersion") String catalogVersion,
    @JsonProperty(required = true, value = "mode") DesiredRuntimeCatalogMode mode,
    @JsonProperty(required = true, value = "generatedAt") OffsetDateTime generatedAt,
    @JsonProperty(required = true, value = "catalogAsOf") OffsetDateTime catalogAsOf,
    @JsonProperty(required = true, value = "items") List<@Valid DesiredRuntimeCatalogItem> items,
    @JsonProperty(required = true, value = "page") DesiredRuntimeCatalogPage page
  ) {
    this.catalogVersion = catalogVersion;
    this.mode = mode;
    this.generatedAt = generatedAt;
    this.catalogAsOf = catalogAsOf;
    this.items = items;
    this.page = page;
  }

  /**
   * Version of the service-to-service desired catalog contract.
   **/
  public DesiredRuntimeCatalogResponse catalogVersion(String catalogVersion) {
    this.catalogVersion = catalogVersion;
    return this;
  }

  
  @ApiModelProperty(example = "iia.desired-runtime-catalog/v1", required = true, value = "Version of the service-to-service desired catalog contract.")
  @JsonProperty(required = true, value = "catalogVersion")
  @NotNull public String getCatalogVersion() {
    return catalogVersion;
  }

  @JsonProperty(required = true, value = "catalogVersion")
  public void setCatalogVersion(String catalogVersion) {
    this.catalogVersion = catalogVersion;
  }

  /**
   **/
  public DesiredRuntimeCatalogResponse mode(DesiredRuntimeCatalogMode mode) {
    this.mode = mode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "mode")
  @NotNull public DesiredRuntimeCatalogMode getMode() {
    return mode;
  }

  @JsonProperty(required = true, value = "mode")
  public void setMode(DesiredRuntimeCatalogMode mode) {
    this.mode = mode;
  }

  /**
   * Timestamp at which this response page was generated.
   **/
  public DesiredRuntimeCatalogResponse generatedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Timestamp at which this response page was generated.")
  @JsonProperty(required = true, value = "generatedAt")
  @NotNull public OffsetDateTime getGeneratedAt() {
    return generatedAt;
  }

  @JsonProperty(required = true, value = "generatedAt")
  public void setGeneratedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  /**
   * Stable source upper bound established for the paginated catalog read and encoded in subsequent cursors.
   **/
  public DesiredRuntimeCatalogResponse catalogAsOf(OffsetDateTime catalogAsOf) {
    this.catalogAsOf = catalogAsOf;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Stable source upper bound established for the paginated catalog read and encoded in subsequent cursors.")
  @JsonProperty(required = true, value = "catalogAsOf")
  @NotNull public OffsetDateTime getCatalogAsOf() {
    return catalogAsOf;
  }

  @JsonProperty(required = true, value = "catalogAsOf")
  public void setCatalogAsOf(OffsetDateTime catalogAsOf) {
    this.catalogAsOf = catalogAsOf;
  }

  /**
   * Opaque checkpoint effectively used as the incremental lower bound, when applicable.
   **/
  public DesiredRuntimeCatalogResponse sourceCheckpoint(String sourceCheckpoint) {
    this.sourceCheckpoint = sourceCheckpoint;
    return this;
  }

  
  @ApiModelProperty(value = "Opaque checkpoint effectively used as the incremental lower bound, when applicable.")
  @JsonProperty("sourceCheckpoint")
   @Size(max=2000)public String getSourceCheckpoint() {
    return sourceCheckpoint;
  }

  @JsonProperty("sourceCheckpoint")
  public void setSourceCheckpoint(String sourceCheckpoint) {
    this.sourceCheckpoint = sourceCheckpoint;
  }

  /**
   * Opaque checkpoint covering source changes through catalogAsOf. Returned only on the final page and persisted by the Orchestrator only after checkpoint-safe reconciliation completion.
   **/
  public DesiredRuntimeCatalogResponse nextCheckpoint(String nextCheckpoint) {
    this.nextCheckpoint = nextCheckpoint;
    return this;
  }

  
  @ApiModelProperty(value = "Opaque checkpoint covering source changes through catalogAsOf. Returned only on the final page and persisted by the Orchestrator only after checkpoint-safe reconciliation completion.")
  @JsonProperty("nextCheckpoint")
   @Size(max=2000)public String getNextCheckpoint() {
    return nextCheckpoint;
  }

  @JsonProperty("nextCheckpoint")
  public void setNextCheckpoint(String nextCheckpoint) {
    this.nextCheckpoint = nextCheckpoint;
  }

  /**
   **/
  public DesiredRuntimeCatalogResponse items(List<DesiredRuntimeCatalogItem> items) {
    this.items = items;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "items")
  @NotNull @Valid public List<@Valid DesiredRuntimeCatalogItem> getItems() {
    return items;
  }

  @JsonProperty(required = true, value = "items")
  public void setItems(List<DesiredRuntimeCatalogItem> items) {
    this.items = items;
  }

  public DesiredRuntimeCatalogResponse addItemsItem(DesiredRuntimeCatalogItem itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public DesiredRuntimeCatalogResponse removeItemsItem(DesiredRuntimeCatalogItem itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeCatalogResponse page(DesiredRuntimeCatalogPage page) {
    this.page = page;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "page")
  @NotNull @Valid public DesiredRuntimeCatalogPage getPage() {
    return page;
  }

  @JsonProperty(required = true, value = "page")
  public void setPage(DesiredRuntimeCatalogPage page) {
    this.page = page;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeCatalogResponse desiredRuntimeCatalogResponse = (DesiredRuntimeCatalogResponse) o;
    return Objects.equals(this.catalogVersion, desiredRuntimeCatalogResponse.catalogVersion) &&
        Objects.equals(this.mode, desiredRuntimeCatalogResponse.mode) &&
        Objects.equals(this.generatedAt, desiredRuntimeCatalogResponse.generatedAt) &&
        Objects.equals(this.catalogAsOf, desiredRuntimeCatalogResponse.catalogAsOf) &&
        Objects.equals(this.sourceCheckpoint, desiredRuntimeCatalogResponse.sourceCheckpoint) &&
        Objects.equals(this.nextCheckpoint, desiredRuntimeCatalogResponse.nextCheckpoint) &&
        Objects.equals(this.items, desiredRuntimeCatalogResponse.items) &&
        Objects.equals(this.page, desiredRuntimeCatalogResponse.page);
  }

  @Override
  public int hashCode() {
    return Objects.hash(catalogVersion, mode, generatedAt, catalogAsOf, sourceCheckpoint, nextCheckpoint, items, page);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeCatalogResponse {\n");
    
    sb.append("    catalogVersion: ").append(toIndentedString(catalogVersion)).append("\n");
    sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
    sb.append("    generatedAt: ").append(toIndentedString(generatedAt)).append("\n");
    sb.append("    catalogAsOf: ").append(toIndentedString(catalogAsOf)).append("\n");
    sb.append("    sourceCheckpoint: ").append(toIndentedString(sourceCheckpoint)).append("\n");
    sb.append("    nextCheckpoint: ").append(toIndentedString(nextCheckpoint)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
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
