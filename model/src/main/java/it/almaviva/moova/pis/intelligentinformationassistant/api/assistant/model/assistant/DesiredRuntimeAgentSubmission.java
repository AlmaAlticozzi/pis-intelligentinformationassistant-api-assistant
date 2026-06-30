package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import org.openapitools.jackson.nullable.JsonNullable;
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
 * Governed runtime submission JSON-compatible with the Agent Orchestrator RuntimeAgentDefinitionSubmissionRequest.
 **/
@ApiModel(description = "Governed runtime submission JSON-compatible with the Agent Orchestrator RuntimeAgentDefinitionSubmissionRequest.")
@JsonTypeName("DesiredRuntimeAgentSubmission")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeAgentSubmission   {
  private String submissionId;
  public enum DesiredStatusEnum {

    ACTIVE(String.valueOf("ACTIVE"));


    private String value;

    DesiredStatusEnum (String v) {
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
    public static DesiredStatusEnum fromString(String s) {
        for (DesiredStatusEnum b : DesiredStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static DesiredStatusEnum fromValue(String value) {
        for (DesiredStatusEnum b : DesiredStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private DesiredStatusEnum desiredStatus;
  private Long packageVersion;
  private OffsetDateTime submittedAt;
  private String submittedBy;
  private Boolean startImmediatelyIfAllowed = true;
  private String note;
  private DesiredRuntimeAgentDefinitionPackage agentDefinition;

  public DesiredRuntimeAgentSubmission() {
  }

  @JsonCreator
  public DesiredRuntimeAgentSubmission(
    @JsonProperty(required = true, value = "submissionId") String submissionId,
    @JsonProperty(required = true, value = "desiredStatus") DesiredStatusEnum desiredStatus,
    @JsonProperty(required = true, value = "packageVersion") Long packageVersion,
    @JsonProperty(required = true, value = "submittedAt") OffsetDateTime submittedAt,
    @JsonProperty(required = true, value = "agentDefinition") DesiredRuntimeAgentDefinitionPackage agentDefinition
  ) {
    this.submissionId = submissionId;
    this.desiredStatus = desiredStatus;
    this.packageVersion = packageVersion;
    this.submittedAt = submittedAt;
    this.agentDefinition = agentDefinition;
  }

  /**
   * Stable idempotency identifier for the canonical package. It must be reused for equivalent activation retries and reconciliation replays and must not be regenerated on every catalog read.
   **/
  public DesiredRuntimeAgentSubmission submissionId(String submissionId) {
    this.submissionId = submissionId;
    return this;
  }

  
  @ApiModelProperty(example = "ACTIVATE:AGDF2026251400000001:1:2ef7bde608ce5404", required = true, value = "Stable idempotency identifier for the canonical package. It must be reused for equivalent activation retries and reconciliation replays and must not be regenerated on every catalog read.")
  @JsonProperty(required = true, value = "submissionId")
  @NotNull  @Size(min=1,max=100)public String getSubmissionId() {
    return submissionId;
  }

  @JsonProperty(required = true, value = "submissionId")
  public void setSubmissionId(String submissionId) {
    this.submissionId = submissionId;
  }

  /**
   **/
  public DesiredRuntimeAgentSubmission desiredStatus(DesiredStatusEnum desiredStatus) {
    this.desiredStatus = desiredStatus;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "desiredStatus")
  @NotNull public DesiredStatusEnum getDesiredStatus() {
    return desiredStatus;
  }

  @JsonProperty(required = true, value = "desiredStatus")
  public void setDesiredStatus(DesiredStatusEnum desiredStatus) {
    this.desiredStatus = desiredStatus;
  }

  /**
   * Monotonic runtime-package version. It changes whenever any runtime-significant package field changes.
   * minimum: 1
   **/
  public DesiredRuntimeAgentSubmission packageVersion(Long packageVersion) {
    this.packageVersion = packageVersion;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Monotonic runtime-package version. It changes whenever any runtime-significant package field changes.")
  @JsonProperty(required = true, value = "packageVersion")
  @NotNull  @Min(1L)public Long getPackageVersion() {
    return packageVersion;
  }

  @JsonProperty(required = true, value = "packageVersion")
  public void setPackageVersion(Long packageVersion) {
    this.packageVersion = packageVersion;
  }

  /**
   * Stable package submission timestamp persisted by the Assistant, not the catalog response generation time.
   **/
  public DesiredRuntimeAgentSubmission submittedAt(OffsetDateTime submittedAt) {
    this.submittedAt = submittedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Stable package submission timestamp persisted by the Assistant, not the catalog response generation time.")
  @JsonProperty(required = true, value = "submittedAt")
  @NotNull public OffsetDateTime getSubmittedAt() {
    return submittedAt;
  }

  @JsonProperty(required = true, value = "submittedAt")
  public void setSubmittedAt(OffsetDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  /**
   **/
  public DesiredRuntimeAgentSubmission submittedBy(String submittedBy) {
    this.submittedBy = submittedBy;
    return this;
  }

  
  @ApiModelProperty(example = "pis-intelligentinformationassistant-api-assistant", value = "")
  @JsonProperty("submittedBy")
   @Size(max=100)public String getSubmittedBy() {
    return submittedBy;
  }

  @JsonProperty("submittedBy")
  public void setSubmittedBy(String submittedBy) {
    this.submittedBy = submittedBy;
  }

  /**
   **/
  public DesiredRuntimeAgentSubmission startImmediatelyIfAllowed(Boolean startImmediatelyIfAllowed) {
    this.startImmediatelyIfAllowed = startImmediatelyIfAllowed;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("startImmediatelyIfAllowed")
  public Boolean getStartImmediatelyIfAllowed() {
    return startImmediatelyIfAllowed;
  }

  @JsonProperty("startImmediatelyIfAllowed")
  public void setStartImmediatelyIfAllowed(Boolean startImmediatelyIfAllowed) {
    this.startImmediatelyIfAllowed = startImmediatelyIfAllowed;
  }

  /**
   **/
  public DesiredRuntimeAgentSubmission note(String note) {
    this.note = note;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("note")
   @Size(max=1000)public String getNote() {
    return note;
  }

  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }

  /**
   **/
  public DesiredRuntimeAgentSubmission agentDefinition(DesiredRuntimeAgentDefinitionPackage agentDefinition) {
    this.agentDefinition = agentDefinition;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentDefinition")
  @NotNull @Valid public DesiredRuntimeAgentDefinitionPackage getAgentDefinition() {
    return agentDefinition;
  }

  @JsonProperty(required = true, value = "agentDefinition")
  public void setAgentDefinition(DesiredRuntimeAgentDefinitionPackage agentDefinition) {
    this.agentDefinition = agentDefinition;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeAgentSubmission desiredRuntimeAgentSubmission = (DesiredRuntimeAgentSubmission) o;
    return Objects.equals(this.submissionId, desiredRuntimeAgentSubmission.submissionId) &&
        Objects.equals(this.desiredStatus, desiredRuntimeAgentSubmission.desiredStatus) &&
        Objects.equals(this.packageVersion, desiredRuntimeAgentSubmission.packageVersion) &&
        Objects.equals(this.submittedAt, desiredRuntimeAgentSubmission.submittedAt) &&
        Objects.equals(this.submittedBy, desiredRuntimeAgentSubmission.submittedBy) &&
        Objects.equals(this.startImmediatelyIfAllowed, desiredRuntimeAgentSubmission.startImmediatelyIfAllowed) &&
        Objects.equals(this.note, desiredRuntimeAgentSubmission.note) &&
        Objects.equals(this.agentDefinition, desiredRuntimeAgentSubmission.agentDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(submissionId, desiredStatus, packageVersion, submittedAt, submittedBy, startImmediatelyIfAllowed, note, agentDefinition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeAgentSubmission {\n");
    
    sb.append("    submissionId: ").append(toIndentedString(submissionId)).append("\n");
    sb.append("    desiredStatus: ").append(toIndentedString(desiredStatus)).append("\n");
    sb.append("    packageVersion: ").append(toIndentedString(packageVersion)).append("\n");
    sb.append("    submittedAt: ").append(toIndentedString(submittedAt)).append("\n");
    sb.append("    submittedBy: ").append(toIndentedString(submittedBy)).append("\n");
    sb.append("    startImmediatelyIfAllowed: ").append(toIndentedString(startImmediatelyIfAllowed)).append("\n");
    sb.append("    note: ").append(toIndentedString(note)).append("\n");
    sb.append("    agentDefinition: ").append(toIndentedString(agentDefinition)).append("\n");
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
