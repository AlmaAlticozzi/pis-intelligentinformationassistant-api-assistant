package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentRuntimePackageRegenerationResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.23.0")
public class AgentRuntimePackageRegenerationResponse   {
  private String agentDefinitionId;
  private Long oldPackageVersion;
  private Long currentPackageVersion;
  private String artifactHash;
  private String packageFingerprint;
  private String submissionId;
  public enum StatusEnum {

    ACTIVE(String.valueOf("ACTIVE"));


    private String value;

    StatusEnum (String v) {
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
    public static StatusEnum fromString(String s) {
        for (StatusEnum b : StatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private StatusEnum status;
  private Boolean packageCreated;
  private Boolean currentPackageReused;

  public AgentRuntimePackageRegenerationResponse() {
  }

  @JsonCreator
  public AgentRuntimePackageRegenerationResponse(
    @JsonProperty(required = true, value = "agentDefinitionId") String agentDefinitionId,
    @JsonProperty(required = true, value = "oldPackageVersion") Long oldPackageVersion,
    @JsonProperty(required = true, value = "currentPackageVersion") Long currentPackageVersion,
    @JsonProperty(required = true, value = "artifactHash") String artifactHash,
    @JsonProperty(required = true, value = "packageFingerprint") String packageFingerprint,
    @JsonProperty(required = true, value = "submissionId") String submissionId,
    @JsonProperty(required = true, value = "status") StatusEnum status,
    @JsonProperty(required = true, value = "packageCreated") Boolean packageCreated,
    @JsonProperty(required = true, value = "currentPackageReused") Boolean currentPackageReused
  ) {
    this.agentDefinitionId = agentDefinitionId;
    this.oldPackageVersion = oldPackageVersion;
    this.currentPackageVersion = currentPackageVersion;
    this.artifactHash = artifactHash;
    this.packageFingerprint = packageFingerprint;
    this.submissionId = submissionId;
    this.status = status;
    this.packageCreated = packageCreated;
    this.currentPackageReused = currentPackageReused;
  }

  /**
   **/
  public AgentRuntimePackageRegenerationResponse agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentDefinitionId")
  @NotNull  @Size(max=50)public String getAgentDefinitionId() {
    return agentDefinitionId;
  }

  @JsonProperty(required = true, value = "agentDefinitionId")
  public void setAgentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
  }

  /**
   **/
  public AgentRuntimePackageRegenerationResponse oldPackageVersion(Long oldPackageVersion) {
    this.oldPackageVersion = oldPackageVersion;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "oldPackageVersion")
  @NotNull public Long getOldPackageVersion() {
    return oldPackageVersion;
  }

  @JsonProperty(required = true, value = "oldPackageVersion")
  public void setOldPackageVersion(Long oldPackageVersion) {
    this.oldPackageVersion = oldPackageVersion;
  }

  /**
   **/
  public AgentRuntimePackageRegenerationResponse currentPackageVersion(Long currentPackageVersion) {
    this.currentPackageVersion = currentPackageVersion;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "currentPackageVersion")
  @NotNull public Long getCurrentPackageVersion() {
    return currentPackageVersion;
  }

  @JsonProperty(required = true, value = "currentPackageVersion")
  public void setCurrentPackageVersion(Long currentPackageVersion) {
    this.currentPackageVersion = currentPackageVersion;
  }

  /**
   **/
  public AgentRuntimePackageRegenerationResponse artifactHash(String artifactHash) {
    this.artifactHash = artifactHash;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "artifactHash")
  @NotNull  @Size(max=71)public String getArtifactHash() {
    return artifactHash;
  }

  @JsonProperty(required = true, value = "artifactHash")
  public void setArtifactHash(String artifactHash) {
    this.artifactHash = artifactHash;
  }

  /**
   **/
  public AgentRuntimePackageRegenerationResponse packageFingerprint(String packageFingerprint) {
    this.packageFingerprint = packageFingerprint;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "packageFingerprint")
  @NotNull  @Size(min=64,max=64)public String getPackageFingerprint() {
    return packageFingerprint;
  }

  @JsonProperty(required = true, value = "packageFingerprint")
  public void setPackageFingerprint(String packageFingerprint) {
    this.packageFingerprint = packageFingerprint;
  }

  /**
   **/
  public AgentRuntimePackageRegenerationResponse submissionId(String submissionId) {
    this.submissionId = submissionId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "submissionId")
  @NotNull  @Size(max=100)public String getSubmissionId() {
    return submissionId;
  }

  @JsonProperty(required = true, value = "submissionId")
  public void setSubmissionId(String submissionId) {
    this.submissionId = submissionId;
  }

  /**
   **/
  public AgentRuntimePackageRegenerationResponse status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * True when this invocation persisted a new immutable package version.
   **/
  public AgentRuntimePackageRegenerationResponse packageCreated(Boolean packageCreated) {
    this.packageCreated = packageCreated;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "True when this invocation persisted a new immutable package version.")
  @JsonProperty(required = true, value = "packageCreated")
  @NotNull public Boolean getPackageCreated() {
    return packageCreated;
  }

  @JsonProperty(required = true, value = "packageCreated")
  public void setPackageCreated(Boolean packageCreated) {
    this.packageCreated = packageCreated;
  }

  /**
   * True when the corrected canonical package was already the current desired package.
   **/
  public AgentRuntimePackageRegenerationResponse currentPackageReused(Boolean currentPackageReused) {
    this.currentPackageReused = currentPackageReused;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "True when the corrected canonical package was already the current desired package.")
  @JsonProperty(required = true, value = "currentPackageReused")
  @NotNull public Boolean getCurrentPackageReused() {
    return currentPackageReused;
  }

  @JsonProperty(required = true, value = "currentPackageReused")
  public void setCurrentPackageReused(Boolean currentPackageReused) {
    this.currentPackageReused = currentPackageReused;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRuntimePackageRegenerationResponse agentRuntimePackageRegenerationResponse = (AgentRuntimePackageRegenerationResponse) o;
    return Objects.equals(this.agentDefinitionId, agentRuntimePackageRegenerationResponse.agentDefinitionId) &&
        Objects.equals(this.oldPackageVersion, agentRuntimePackageRegenerationResponse.oldPackageVersion) &&
        Objects.equals(this.currentPackageVersion, agentRuntimePackageRegenerationResponse.currentPackageVersion) &&
        Objects.equals(this.artifactHash, agentRuntimePackageRegenerationResponse.artifactHash) &&
        Objects.equals(this.packageFingerprint, agentRuntimePackageRegenerationResponse.packageFingerprint) &&
        Objects.equals(this.submissionId, agentRuntimePackageRegenerationResponse.submissionId) &&
        Objects.equals(this.status, agentRuntimePackageRegenerationResponse.status) &&
        Objects.equals(this.packageCreated, agentRuntimePackageRegenerationResponse.packageCreated) &&
        Objects.equals(this.currentPackageReused, agentRuntimePackageRegenerationResponse.currentPackageReused);
  }

  @Override
  public int hashCode() {
    return Objects.hash(agentDefinitionId, oldPackageVersion, currentPackageVersion, artifactHash, packageFingerprint, submissionId, status, packageCreated, currentPackageReused);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRuntimePackageRegenerationResponse {\n");
    
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    oldPackageVersion: ").append(toIndentedString(oldPackageVersion)).append("\n");
    sb.append("    currentPackageVersion: ").append(toIndentedString(currentPackageVersion)).append("\n");
    sb.append("    artifactHash: ").append(toIndentedString(artifactHash)).append("\n");
    sb.append("    packageFingerprint: ").append(toIndentedString(packageFingerprint)).append("\n");
    sb.append("    submissionId: ").append(toIndentedString(submissionId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    packageCreated: ").append(toIndentedString(packageCreated)).append("\n");
    sb.append("    currentPackageReused: ").append(toIndentedString(currentPackageReused)).append("\n");
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
