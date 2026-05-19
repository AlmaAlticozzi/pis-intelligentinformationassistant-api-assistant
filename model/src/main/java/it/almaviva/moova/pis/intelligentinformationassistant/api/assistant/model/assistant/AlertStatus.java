package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Alert lifecycle status. &#x60;DRAFT&#x60; means that the alert has only been persisted. No AI-assisted verification has been started yet, no controlled interpreter is available and the alert cannot be enabled. &#x60;VERIFYING&#x60; means that the alert prompt is being verified or re-verified. &#x60;VERIFIED&#x60; means that the alert has a valid controlled interpreter and can be enabled. &#x60;REJECTED&#x60; means that the prompt cannot be safely or deterministically implemented. &#x60;DISABLED&#x60; means that runtime execution has been disabled for an otherwise persisted alert. &#x60;ERROR&#x60; means that verification or runtime preparation failed. &#x60;DELETED&#x60; means that the alert is soft-deleted and must not be returned by ordinary search operations.
 */
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-19T15:56:44.348406306Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public enum AlertStatus {

  DRAFT("DRAFT"),

  VERIFYING("VERIFYING"),

  VERIFIED("VERIFIED"),

  REJECTED("REJECTED"),

  DISABLED("DISABLED"),

  ERROR("ERROR"),

  DELETED("DELETED");

  private String value;

  AlertStatus(String value) {
    this.value = value;
  }

  /**
   * Convert a String into String, as specified in the
   * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
   */
  public static AlertStatus fromString(String s) {
    for (AlertStatus b : AlertStatus.values()) {
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
  public static AlertStatus fromValue(String value) {
    for (AlertStatus b : AlertStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


