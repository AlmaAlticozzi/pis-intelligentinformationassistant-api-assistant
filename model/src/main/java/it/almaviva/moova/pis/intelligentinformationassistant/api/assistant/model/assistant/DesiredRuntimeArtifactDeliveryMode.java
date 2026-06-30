package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets DesiredRuntimeArtifactDeliveryMode
 */
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public enum DesiredRuntimeArtifactDeliveryMode {
  
  INLINE("INLINE"),
  
  URI("URI");

  private String value;

  DesiredRuntimeArtifactDeliveryMode(String value) {
    this.value = value;
  }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static DesiredRuntimeArtifactDeliveryMode fromString(String s) {
      for (DesiredRuntimeArtifactDeliveryMode b : DesiredRuntimeArtifactDeliveryMode.values()) {
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
  public static DesiredRuntimeArtifactDeliveryMode fromValue(String value) {
    for (DesiredRuntimeArtifactDeliveryMode b : DesiredRuntimeArtifactDeliveryMode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


