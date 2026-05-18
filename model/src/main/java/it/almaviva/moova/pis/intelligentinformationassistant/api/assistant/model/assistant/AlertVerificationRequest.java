package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AlertVerificationRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertVerificationRequest   {
  private Boolean force = false;
  private AlertInterpreterType preferredInterpreterType;

  public AlertVerificationRequest() {
  }

  /**
   * Force re-verification even if the alert is already verified.
   **/
  public AlertVerificationRequest force(Boolean force) {
    this.force = force;
    return this;
  }

  
  @ApiModelProperty(value = "Force re-verification even if the alert is already verified.")
  @JsonProperty("force")
  public Boolean getForce() {
    return force;
  }

  @JsonProperty("force")
  public void setForce(Boolean force) {
    this.force = force;
  }

  /**
   **/
  public AlertVerificationRequest preferredInterpreterType(AlertInterpreterType preferredInterpreterType) {
    this.preferredInterpreterType = preferredInterpreterType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("preferredInterpreterType")
  public AlertInterpreterType getPreferredInterpreterType() {
    return preferredInterpreterType;
  }

  @JsonProperty("preferredInterpreterType")
  public void setPreferredInterpreterType(AlertInterpreterType preferredInterpreterType) {
    this.preferredInterpreterType = preferredInterpreterType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertVerificationRequest alertVerificationRequest = (AlertVerificationRequest) o;
    return Objects.equals(this.force, alertVerificationRequest.force) &&
        Objects.equals(this.preferredInterpreterType, alertVerificationRequest.preferredInterpreterType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(force, preferredInterpreterType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertVerificationRequest {\n");
    
    sb.append("    force: ").append(toIndentedString(force)).append("\n");
    sb.append("    preferredInterpreterType: ").append(toIndentedString(preferredInterpreterType)).append("\n");
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
