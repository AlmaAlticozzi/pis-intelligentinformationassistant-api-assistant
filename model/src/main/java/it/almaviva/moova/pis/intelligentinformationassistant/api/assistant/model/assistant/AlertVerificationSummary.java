package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
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



@JsonTypeName("AlertVerificationSummary")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertVerificationSummary   {
  private AlertVerificationStatus status;
  private OffsetDateTime verifiedAt;
  private String rejectedReason;
  private Double confidence;

  public AlertVerificationSummary() {
  }

  /**
   **/
  public AlertVerificationSummary status(AlertVerificationStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public AlertVerificationStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(AlertVerificationStatus status) {
    this.status = status;
  }

  /**
   **/
  public AlertVerificationSummary verifiedAt(OffsetDateTime verifiedAt) {
    this.verifiedAt = verifiedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("verifiedAt")
  public OffsetDateTime getVerifiedAt() {
    return verifiedAt;
  }

  @JsonProperty("verifiedAt")
  public void setVerifiedAt(OffsetDateTime verifiedAt) {
    this.verifiedAt = verifiedAt;
  }

  /**
   **/
  public AlertVerificationSummary rejectedReason(String rejectedReason) {
    this.rejectedReason = rejectedReason;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rejectedReason")
  public String getRejectedReason() {
    return rejectedReason;
  }

  @JsonProperty("rejectedReason")
  public void setRejectedReason(String rejectedReason) {
    this.rejectedReason = rejectedReason;
  }

  /**
   * minimum: 0
   * maximum: 1
   **/
  public AlertVerificationSummary confidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }

  
  @ApiModelProperty(example = "0.86", value = "")
  @JsonProperty("confidence")
   @DecimalMin("0") @DecimalMax("1")public Double getConfidence() {
    return confidence;
  }

  @JsonProperty("confidence")
  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertVerificationSummary alertVerificationSummary = (AlertVerificationSummary) o;
    return Objects.equals(this.status, alertVerificationSummary.status) &&
        Objects.equals(this.verifiedAt, alertVerificationSummary.verifiedAt) &&
        Objects.equals(this.rejectedReason, alertVerificationSummary.rejectedReason) &&
        Objects.equals(this.confidence, alertVerificationSummary.confidence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, verifiedAt, rejectedReason, confidence);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertVerificationSummary {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    verifiedAt: ").append(toIndentedString(verifiedAt)).append("\n");
    sb.append("    rejectedReason: ").append(toIndentedString(rejectedReason)).append("\n");
    sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
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
