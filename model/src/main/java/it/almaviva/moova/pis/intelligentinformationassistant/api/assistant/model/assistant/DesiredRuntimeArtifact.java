package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
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
 * Exactly one of content or uri is populated according to deliveryMode. The Assistant never returns arbitrary executable code.
 **/
@ApiModel(description = "Exactly one of content or uri is populated according to deliveryMode. The Assistant never returns arbitrary executable code.")
@JsonTypeName("DesiredRuntimeArtifact")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeArtifact   {
  private DesiredRuntimeArtifactType artifactType;
  private String schemaVersion;
  private String mediaType;
  private DesiredRuntimeArtifactDeliveryMode deliveryMode;
  private @Valid Map<String, Object> content;
  private String uri;
  public enum HashAlgorithmEnum {

    SHA_256(String.valueOf("SHA-256"));


    private String value;

    HashAlgorithmEnum (String v) {
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
    public static HashAlgorithmEnum fromString(String s) {
        for (HashAlgorithmEnum b : HashAlgorithmEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static HashAlgorithmEnum fromValue(String value) {
        for (HashAlgorithmEnum b : HashAlgorithmEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private HashAlgorithmEnum hashAlgorithm;
  private String hash;
  private String canonicalization;
  private DesiredRuntimeSignatureStatus signatureStatus;
  private DesiredRuntimeArtifactSignature signature;
  private OffsetDateTime createdAt;
  private Long sizeBytes;

  public DesiredRuntimeArtifact() {
  }

  @JsonCreator
  public DesiredRuntimeArtifact(
    @JsonProperty(required = true, value = "artifactType") DesiredRuntimeArtifactType artifactType,
    @JsonProperty(required = true, value = "schemaVersion") String schemaVersion,
    @JsonProperty(required = true, value = "mediaType") String mediaType,
    @JsonProperty(required = true, value = "deliveryMode") DesiredRuntimeArtifactDeliveryMode deliveryMode,
    @JsonProperty(required = true, value = "hashAlgorithm") HashAlgorithmEnum hashAlgorithm,
    @JsonProperty(required = true, value = "hash") String hash,
    @JsonProperty(required = true, value = "signatureStatus") DesiredRuntimeSignatureStatus signatureStatus,
    @JsonProperty(required = true, value = "createdAt") OffsetDateTime createdAt
  ) {
    this.artifactType = artifactType;
    this.schemaVersion = schemaVersion;
    this.mediaType = mediaType;
    this.deliveryMode = deliveryMode;
    this.hashAlgorithm = hashAlgorithm;
    this.hash = hash;
    this.signatureStatus = signatureStatus;
    this.createdAt = createdAt;
  }

  /**
   **/
  public DesiredRuntimeArtifact artifactType(DesiredRuntimeArtifactType artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "artifactType")
  @NotNull public DesiredRuntimeArtifactType getArtifactType() {
    return artifactType;
  }

  @JsonProperty(required = true, value = "artifactType")
  public void setArtifactType(DesiredRuntimeArtifactType artifactType) {
    this.artifactType = artifactType;
  }

  /**
   **/
  public DesiredRuntimeArtifact schemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "schemaVersion")
  @NotNull  @Size(min=1,max=100)public String getSchemaVersion() {
    return schemaVersion;
  }

  @JsonProperty(required = true, value = "schemaVersion")
  public void setSchemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  /**
   **/
  public DesiredRuntimeArtifact mediaType(String mediaType) {
    this.mediaType = mediaType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "mediaType")
  @NotNull  @Size(min=1,max=100)public String getMediaType() {
    return mediaType;
  }

  @JsonProperty(required = true, value = "mediaType")
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  /**
   **/
  public DesiredRuntimeArtifact deliveryMode(DesiredRuntimeArtifactDeliveryMode deliveryMode) {
    this.deliveryMode = deliveryMode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "deliveryMode")
  @NotNull public DesiredRuntimeArtifactDeliveryMode getDeliveryMode() {
    return deliveryMode;
  }

  @JsonProperty(required = true, value = "deliveryMode")
  public void setDeliveryMode(DesiredRuntimeArtifactDeliveryMode deliveryMode) {
    this.deliveryMode = deliveryMode;
  }

  /**
   * Controlled canonicalizable artifact content. Required for INLINE delivery.
   **/
  public DesiredRuntimeArtifact content(Map<String, Object> content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(value = "Controlled canonicalizable artifact content. Required for INLINE delivery.")
  @JsonProperty("content")
  public Map<String, Object> getContent() {
    return content;
  }

  @JsonProperty("content")
  public void setContent(Map<String, Object> content) {
    this.content = content;
  }

  public DesiredRuntimeArtifact putContentItem(String key, Object contentItem) {
    if (this.content == null) {
      this.content = new HashMap<>();
    }

    this.content.put(key, contentItem);
    return this;
  }

  public DesiredRuntimeArtifact removeContentItem(String key) {
    if (this.content != null) {
      this.content.remove(key);
    }

    return this;
  }
  /**
   * Allowed immutable artifact-repository URI. Required for URI delivery.
   **/
  public DesiredRuntimeArtifact uri(String uri) {
    this.uri = uri;
    return this;
  }

  
  @ApiModelProperty(value = "Allowed immutable artifact-repository URI. Required for URI delivery.")
  @JsonProperty("uri")
   @Size(max=2000)public String getUri() {
    return uri;
  }

  @JsonProperty("uri")
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   **/
  public DesiredRuntimeArtifact hashAlgorithm(HashAlgorithmEnum hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "hashAlgorithm")
  @NotNull public HashAlgorithmEnum getHashAlgorithm() {
    return hashAlgorithm;
  }

  @JsonProperty(required = true, value = "hashAlgorithm")
  public void setHashAlgorithm(HashAlgorithmEnum hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  /**
   **/
  public DesiredRuntimeArtifact hash(String hash) {
    this.hash = hash;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "hash")
  @NotNull  @Size(min=64,max=128)public String getHash() {
    return hash;
  }

  @JsonProperty(required = true, value = "hash")
  public void setHash(String hash) {
    this.hash = hash;
  }

  /**
   **/
  public DesiredRuntimeArtifact canonicalization(String canonicalization) {
    this.canonicalization = canonicalization;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("canonicalization")
   @Size(max=100)public String getCanonicalization() {
    return canonicalization;
  }

  @JsonProperty("canonicalization")
  public void setCanonicalization(String canonicalization) {
    this.canonicalization = canonicalization;
  }

  /**
   **/
  public DesiredRuntimeArtifact signatureStatus(DesiredRuntimeSignatureStatus signatureStatus) {
    this.signatureStatus = signatureStatus;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "signatureStatus")
  @NotNull public DesiredRuntimeSignatureStatus getSignatureStatus() {
    return signatureStatus;
  }

  @JsonProperty(required = true, value = "signatureStatus")
  public void setSignatureStatus(DesiredRuntimeSignatureStatus signatureStatus) {
    this.signatureStatus = signatureStatus;
  }

  /**
   **/
  public DesiredRuntimeArtifact signature(DesiredRuntimeArtifactSignature signature) {
    this.signature = signature;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("signature")
  @Valid public DesiredRuntimeArtifactSignature getSignature() {
    return signature;
  }

  @JsonProperty("signature")
  public void setSignature(DesiredRuntimeArtifactSignature signature) {
    this.signature = signature;
  }

  /**
   **/
  public DesiredRuntimeArtifact createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "createdAt")
  @NotNull public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty(required = true, value = "createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * minimum: 0
   **/
  public DesiredRuntimeArtifact sizeBytes(Long sizeBytes) {
    this.sizeBytes = sizeBytes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sizeBytes")
   @Min(0L)public Long getSizeBytes() {
    return sizeBytes;
  }

  @JsonProperty("sizeBytes")
  public void setSizeBytes(Long sizeBytes) {
    this.sizeBytes = sizeBytes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeArtifact desiredRuntimeArtifact = (DesiredRuntimeArtifact) o;
    return Objects.equals(this.artifactType, desiredRuntimeArtifact.artifactType) &&
        Objects.equals(this.schemaVersion, desiredRuntimeArtifact.schemaVersion) &&
        Objects.equals(this.mediaType, desiredRuntimeArtifact.mediaType) &&
        Objects.equals(this.deliveryMode, desiredRuntimeArtifact.deliveryMode) &&
        Objects.equals(this.content, desiredRuntimeArtifact.content) &&
        Objects.equals(this.uri, desiredRuntimeArtifact.uri) &&
        Objects.equals(this.hashAlgorithm, desiredRuntimeArtifact.hashAlgorithm) &&
        Objects.equals(this.hash, desiredRuntimeArtifact.hash) &&
        Objects.equals(this.canonicalization, desiredRuntimeArtifact.canonicalization) &&
        Objects.equals(this.signatureStatus, desiredRuntimeArtifact.signatureStatus) &&
        Objects.equals(this.signature, desiredRuntimeArtifact.signature) &&
        Objects.equals(this.createdAt, desiredRuntimeArtifact.createdAt) &&
        Objects.equals(this.sizeBytes, desiredRuntimeArtifact.sizeBytes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactType, schemaVersion, mediaType, deliveryMode, content, uri, hashAlgorithm, hash, canonicalization, signatureStatus, signature, createdAt, sizeBytes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeArtifact {\n");
    
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    schemaVersion: ").append(toIndentedString(schemaVersion)).append("\n");
    sb.append("    mediaType: ").append(toIndentedString(mediaType)).append("\n");
    sb.append("    deliveryMode: ").append(toIndentedString(deliveryMode)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
    sb.append("    hashAlgorithm: ").append(toIndentedString(hashAlgorithm)).append("\n");
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
    sb.append("    canonicalization: ").append(toIndentedString(canonicalization)).append("\n");
    sb.append("    signatureStatus: ").append(toIndentedString(signatureStatus)).append("\n");
    sb.append("    signature: ").append(toIndentedString(signature)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    sizeBytes: ").append(toIndentedString(sizeBytes)).append("\n");
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
