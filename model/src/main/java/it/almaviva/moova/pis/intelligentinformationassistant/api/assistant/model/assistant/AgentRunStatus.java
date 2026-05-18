package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets AgentRunStatus
 */
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public enum AgentRunStatus {
  
  PENDING("PENDING"),
  
  STARTING("STARTING"),
  
  RUNNING("RUNNING"),
  
  IDLE("IDLE"),
  
  DEGRADED("DEGRADED"),
  
  STOPPING("STOPPING"),
  
  STOPPED("STOPPED"),
  
  FAILED("FAILED"),
  
  KILLED("KILLED"),
  
  EXPIRED("EXPIRED");

  private String value;

  AgentRunStatus(String value) {
    this.value = value;
  }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static AgentRunStatus fromString(String s) {
      for (AgentRunStatus b : AgentRunStatus.values()) {
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
  public static AgentRunStatus fromValue(String value) {
    for (AgentRunStatus b : AgentRunStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


