package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Catalog read strategy.  &#x60;FULL&#x60; enumerates the complete ACTIVE desired catalog. &#x60;INCREMENTAL&#x60; emits current UPSERT or REMOVE changes after a source checkpoint. &#x60;TARGETED&#x60; evaluates an explicit identifier set and returns an outcome for every requested identifier. 
 */
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public enum DesiredRuntimeCatalogMode {
  
  FULL("FULL"),
  
  INCREMENTAL("INCREMENTAL"),
  
  TARGETED("TARGETED");

  private String value;

  DesiredRuntimeCatalogMode(String value) {
    this.value = value;
  }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static DesiredRuntimeCatalogMode fromString(String s) {
      for (DesiredRuntimeCatalogMode b : DesiredRuntimeCatalogMode.values()) {
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
  public static DesiredRuntimeCatalogMode fromValue(String value) {
    for (DesiredRuntimeCatalogMode b : DesiredRuntimeCatalogMode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


