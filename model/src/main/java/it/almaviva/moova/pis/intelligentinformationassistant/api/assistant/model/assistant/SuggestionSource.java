package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
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
 * Information about the source that produced the interpreter input.
 **/
@ApiModel(description = "Information about the source that produced the interpreter input.")
@JsonTypeName("SuggestionSource")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionSource   {
  public enum SourceSystemEnum {

    SERVICE_DATA(String.valueOf("SERVICE_DATA")), ANNOUNCEMENT(String.valueOf("ANNOUNCEMENT")), BROADCAST(String.valueOf("BROADCAST")), DEVICE(String.valueOf("DEVICE")), CONTENT(String.valueOf("CONTENT")), CONFIGURATION(String.valueOf("CONFIGURATION")), ASSISTANT(String.valueOf("ASSISTANT")), SCHEDULED_CHECK(String.valueOf("SCHEDULED_CHECK")), OTHER(String.valueOf("OTHER"));


    private String value;

    SourceSystemEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static SourceSystemEnum fromString(String s) {
        for (SourceSystemEnum b : SourceSystemEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static SourceSystemEnum fromValue(String value) {
        for (SourceSystemEnum b : SourceSystemEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private SourceSystemEnum sourceSystem;
  private String sourceEventId;
  private String sourceEventName;
  private OffsetDateTime sourceEventTime;
  private String correlationKey;

  public SuggestionSource() {
  }

  /**
   **/
  public SuggestionSource sourceSystem(SourceSystemEnum sourceSystem) {
    this.sourceSystem = sourceSystem;
    return this;
  }

  
  @ApiModelProperty(example = "SERVICE_DATA", value = "")
  @JsonProperty("sourceSystem")
  public SourceSystemEnum getSourceSystem() {
    return sourceSystem;
  }

  @JsonProperty("sourceSystem")
  public void setSourceSystem(SourceSystemEnum sourceSystem) {
    this.sourceSystem = sourceSystem;
  }

  /**
   **/
  public SuggestionSource sourceEventId(String sourceEventId) {
    this.sourceEventId = sourceEventId;
    return this;
  }

  
  @ApiModelProperty(example = "evt-20260514-0001", value = "")
  @JsonProperty("sourceEventId")
  public String getSourceEventId() {
    return sourceEventId;
  }

  @JsonProperty("sourceEventId")
  public void setSourceEventId(String sourceEventId) {
    this.sourceEventId = sourceEventId;
  }

  /**
   **/
  public SuggestionSource sourceEventName(String sourceEventName) {
    this.sourceEventName = sourceEventName;
    return this;
  }

  
  @ApiModelProperty(example = "CANCELLATION", value = "")
  @JsonProperty("sourceEventName")
  public String getSourceEventName() {
    return sourceEventName;
  }

  @JsonProperty("sourceEventName")
  public void setSourceEventName(String sourceEventName) {
    this.sourceEventName = sourceEventName;
  }

  /**
   **/
  public SuggestionSource sourceEventTime(OffsetDateTime sourceEventTime) {
    this.sourceEventTime = sourceEventTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sourceEventTime")
  public OffsetDateTime getSourceEventTime() {
    return sourceEventTime;
  }

  @JsonProperty("sourceEventTime")
  public void setSourceEventTime(OffsetDateTime sourceEventTime) {
    this.sourceEventTime = sourceEventTime;
  }

  /**
   * Stable key used to avoid duplicate open suggestions.
   **/
  public SuggestionSource correlationKey(String correlationKey) {
    this.correlationKey = correlationKey;
    return this;
  }

  
  @ApiModelProperty(example = "SERVICE_DATA:CANCELLATION:RV1234:GENOVA-BRIGNOLE:20260514T152235", value = "Stable key used to avoid duplicate open suggestions.")
  @JsonProperty("correlationKey")
  public String getCorrelationKey() {
    return correlationKey;
  }

  @JsonProperty("correlationKey")
  public void setCorrelationKey(String correlationKey) {
    this.correlationKey = correlationKey;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuggestionSource suggestionSource = (SuggestionSource) o;
    return Objects.equals(this.sourceSystem, suggestionSource.sourceSystem) &&
        Objects.equals(this.sourceEventId, suggestionSource.sourceEventId) &&
        Objects.equals(this.sourceEventName, suggestionSource.sourceEventName) &&
        Objects.equals(this.sourceEventTime, suggestionSource.sourceEventTime) &&
        Objects.equals(this.correlationKey, suggestionSource.correlationKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceSystem, sourceEventId, sourceEventName, sourceEventTime, correlationKey);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionSource {\n");
    
    sb.append("    sourceSystem: ").append(toIndentedString(sourceSystem)).append("\n");
    sb.append("    sourceEventId: ").append(toIndentedString(sourceEventId)).append("\n");
    sb.append("    sourceEventName: ").append(toIndentedString(sourceEventName)).append("\n");
    sb.append("    sourceEventTime: ").append(toIndentedString(sourceEventTime)).append("\n");
    sb.append("    correlationKey: ").append(toIndentedString(correlationKey)).append("\n");
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
