package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("ClarificationOption")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class ClarificationOption   {
  private String label;
  private String value;
  private @Valid Map<String, Object> metadata = new HashMap<>();

  public ClarificationOption() {
  }

  @JsonCreator
  public ClarificationOption(
    @JsonProperty(required = true, value = "label") String label,
    @JsonProperty(required = true, value = "value") String value
  ) {
    this.label = label;
    this.value = value;
  }

  /**
   **/
  public ClarificationOption label(String label) {
    this.label = label;
    return this;
  }

  
  @ApiModelProperty(example = "Torino Porta Nuova", required = true, value = "")
  @JsonProperty(required = true, value = "label")
  @NotNull public String getLabel() {
    return label;
  }

  @JsonProperty(required = true, value = "label")
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   **/
  public ClarificationOption value(String value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(example = "SP-TORINO-PN", required = true, value = "")
  @JsonProperty(required = true, value = "value")
  @NotNull public String getValue() {
    return value;
  }

  @JsonProperty(required = true, value = "value")
  public void setValue(String value) {
    this.value = value;
  }

  /**
   **/
  public ClarificationOption metadata(Map<String, Object> metadata) {
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

  public ClarificationOption putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }

    this.metadata.put(key, metadataItem);
    return this;
  }

  public ClarificationOption removeMetadataItem(String key) {
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
    ClarificationOption clarificationOption = (ClarificationOption) o;
    return Objects.equals(this.label, clarificationOption.label) &&
        Objects.equals(this.value, clarificationOption.value) &&
        Objects.equals(this.metadata, clarificationOption.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, value, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClarificationOption {\n");
    
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
