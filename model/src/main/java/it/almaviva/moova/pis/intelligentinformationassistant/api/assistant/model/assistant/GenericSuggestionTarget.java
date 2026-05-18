package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionSeverity;
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
 * Generic target used when the suggestion is an operational evaluation not directly linked to one journey or message.
 **/
@ApiModel(description = "Generic target used when the suggestion is an operational evaluation not directly linked to one journey or message.")
@JsonTypeName("GenericSuggestionTarget")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class GenericSuggestionTarget   {
  private String title;
  private String description;
  private SuggestionSeverity severity;
  private @Valid Map<String, Object> metadata = new HashMap<>();

  public GenericSuggestionTarget() {
  }

  @JsonCreator
  public GenericSuggestionTarget(
    @JsonProperty(required = true, value = "title") String title
  ) {
    this.title = title;
  }

  /**
   **/
  public GenericSuggestionTarget title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(example = "Passenger information consistency check", required = true, value = "")
  @JsonProperty(required = true, value = "title")
  @NotNull public String getTitle() {
    return title;
  }

  @JsonProperty(required = true, value = "title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   **/
  public GenericSuggestionTarget description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "The assistant detected a possible inconsistency between Service Data and broadcast history.", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public GenericSuggestionTarget severity(SuggestionSeverity severity) {
    this.severity = severity;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("severity")
  public SuggestionSeverity getSeverity() {
    return severity;
  }

  @JsonProperty("severity")
  public void setSeverity(SuggestionSeverity severity) {
    this.severity = severity;
  }

  /**
   **/
  public GenericSuggestionTarget metadata(Map<String, Object> metadata) {
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

  public GenericSuggestionTarget putMetadataItem(String key, Object metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }

    this.metadata.put(key, metadataItem);
    return this;
  }

  public GenericSuggestionTarget removeMetadataItem(String key) {
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
    GenericSuggestionTarget genericSuggestionTarget = (GenericSuggestionTarget) o;
    return Objects.equals(this.title, genericSuggestionTarget.title) &&
        Objects.equals(this.description, genericSuggestionTarget.description) &&
        Objects.equals(this.severity, genericSuggestionTarget.severity) &&
        Objects.equals(this.metadata, genericSuggestionTarget.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description, severity, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenericSuggestionTarget {\n");
    
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
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
