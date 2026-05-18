package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifactType;
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
 * Metadata of the validated Agent artifact. Raw executable code is not exposed by the API.
 **/
@ApiModel(description = "Metadata of the validated Agent artifact. Raw executable code is not exposed by the API.")
@JsonTypeName("AgentArtifact")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentArtifact   {
  private AgentArtifactType artifactType;
  private String artifactUri;
  private String artifactHash;
  public enum SignatureStatusEnum {

    NOT_SIGNED(String.valueOf("NOT_SIGNED")), SIGNED(String.valueOf("SIGNED")), INVALID(String.valueOf("INVALID")), EXPIRED(String.valueOf("EXPIRED"));


    private String value;

    SignatureStatusEnum (String v) {
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
    public static SignatureStatusEnum fromString(String s) {
        for (SignatureStatusEnum b : SignatureStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static SignatureStatusEnum fromValue(String value) {
        for (SignatureStatusEnum b : SignatureStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private SignatureStatusEnum signatureStatus;
  private String runtimeImage;
  private String sdkVersion;
  private String implementationSummary;

  public AgentArtifact() {
  }

  /**
   **/
  public AgentArtifact artifactType(AgentArtifactType artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("artifactType")
  public AgentArtifactType getArtifactType() {
    return artifactType;
  }

  @JsonProperty("artifactType")
  public void setArtifactType(AgentArtifactType artifactType) {
    this.artifactType = artifactType;
  }

  /**
   **/
  public AgentArtifact artifactUri(String artifactUri) {
    this.artifactUri = artifactUri;
    return this;
  }

  
  @ApiModelProperty(example = "iia-agent-artifact://agent-def-123/rule.yaml", value = "")
  @JsonProperty("artifactUri")
  public String getArtifactUri() {
    return artifactUri;
  }

  @JsonProperty("artifactUri")
  public void setArtifactUri(String artifactUri) {
    this.artifactUri = artifactUri;
  }

  /**
   **/
  public AgentArtifact artifactHash(String artifactHash) {
    this.artifactHash = artifactHash;
    return this;
  }

  
  @ApiModelProperty(example = "sha256:5f2b6c...", value = "")
  @JsonProperty("artifactHash")
  public String getArtifactHash() {
    return artifactHash;
  }

  @JsonProperty("artifactHash")
  public void setArtifactHash(String artifactHash) {
    this.artifactHash = artifactHash;
  }

  /**
   **/
  public AgentArtifact signatureStatus(SignatureStatusEnum signatureStatus) {
    this.signatureStatus = signatureStatus;
    return this;
  }

  
  @ApiModelProperty(example = "SIGNED", value = "")
  @JsonProperty("signatureStatus")
  public SignatureStatusEnum getSignatureStatus() {
    return signatureStatus;
  }

  @JsonProperty("signatureStatus")
  public void setSignatureStatus(SignatureStatusEnum signatureStatus) {
    this.signatureStatus = signatureStatus;
  }

  /**
   **/
  public AgentArtifact runtimeImage(String runtimeImage) {
    this.runtimeImage = runtimeImage;
    return this;
  }

  
  @ApiModelProperty(example = "pis-intelligentinformationassistant-agent-runtime:1.0.0", value = "")
  @JsonProperty("runtimeImage")
  public String getRuntimeImage() {
    return runtimeImage;
  }

  @JsonProperty("runtimeImage")
  public void setRuntimeImage(String runtimeImage) {
    this.runtimeImage = runtimeImage;
  }

  /**
   **/
  public AgentArtifact sdkVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("sdkVersion")
  public String getSdkVersion() {
    return sdkVersion;
  }

  @JsonProperty("sdkVersion")
  public void setSdkVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
  }

  /**
   **/
  public AgentArtifact implementationSummary(String implementationSummary) {
    this.implementationSummary = implementationSummary;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("implementationSummary")
  public String getImplementationSummary() {
    return implementationSummary;
  }

  @JsonProperty("implementationSummary")
  public void setImplementationSummary(String implementationSummary) {
    this.implementationSummary = implementationSummary;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentArtifact agentArtifact = (AgentArtifact) o;
    return Objects.equals(this.artifactType, agentArtifact.artifactType) &&
        Objects.equals(this.artifactUri, agentArtifact.artifactUri) &&
        Objects.equals(this.artifactHash, agentArtifact.artifactHash) &&
        Objects.equals(this.signatureStatus, agentArtifact.signatureStatus) &&
        Objects.equals(this.runtimeImage, agentArtifact.runtimeImage) &&
        Objects.equals(this.sdkVersion, agentArtifact.sdkVersion) &&
        Objects.equals(this.implementationSummary, agentArtifact.implementationSummary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactType, artifactUri, artifactHash, signatureStatus, runtimeImage, sdkVersion, implementationSummary);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentArtifact {\n");
    
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    artifactUri: ").append(toIndentedString(artifactUri)).append("\n");
    sb.append("    artifactHash: ").append(toIndentedString(artifactHash)).append("\n");
    sb.append("    signatureStatus: ").append(toIndentedString(signatureStatus)).append("\n");
    sb.append("    runtimeImage: ").append(toIndentedString(runtimeImage)).append("\n");
    sb.append("    sdkVersion: ").append(toIndentedString(sdkVersion)).append("\n");
    sb.append("    implementationSummary: ").append(toIndentedString(implementationSummary)).append("\n");
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
