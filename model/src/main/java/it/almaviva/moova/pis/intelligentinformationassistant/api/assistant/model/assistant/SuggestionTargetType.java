package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Discriminator of the suggestion target.
 */
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public enum SuggestionTargetType {
  
  SERVICE_DATA_JOURNEY("SERVICE_DATA_JOURNEY"),
  
  SERVICE_DATA_JOURNEY_AGGREGATE("SERVICE_DATA_JOURNEY_AGGREGATE"),
  
  MONITORED_AUDIO_MESSAGE("MONITORED_AUDIO_MESSAGE"),
  
  MONITORED_AUDIO_MESSAGE_AGGREGATE("MONITORED_AUDIO_MESSAGE_AGGREGATE"),
  
  GENERIC("GENERIC");

  private String value;

  SuggestionTargetType(String value) {
    this.value = value;
  }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static SuggestionTargetType fromString(String s) {
      for (SuggestionTargetType b : SuggestionTargetType.values()) {
        // using Objects.toString() to be safe if value type non-object type
        // because types like 'int' etc. will be auto-boxed
        if (java.util.Objects.toString(b.value).equals(s)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static SuggestionTargetType fromValue(String value) {
    for (SuggestionTargetType b : SuggestionTargetType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


