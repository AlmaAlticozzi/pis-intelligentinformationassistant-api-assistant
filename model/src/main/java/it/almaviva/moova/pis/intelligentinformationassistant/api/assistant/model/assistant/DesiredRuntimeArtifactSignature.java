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



@JsonTypeName("DesiredRuntimeArtifactSignature")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeArtifactSignature   {
  public enum TypeEnum {

    LOGICAL_MVP(String.valueOf("LOGICAL_MVP")), JWS(String.valueOf("JWS")), DETACHED_SIGNATURE(String.valueOf("DETACHED_SIGNATURE"));


    private String value;

    TypeEnum (String v) {
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
    public static TypeEnum fromString(String s) {
        for (TypeEnum b : TypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private TypeEnum type;
  private String algorithm;
  private String keyId;
  private String value;
  private OffsetDateTime signedAt;
  private String signedBy;

  public DesiredRuntimeArtifactSignature() {
  }

  /**
   **/
  public DesiredRuntimeArtifactSignature type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  public DesiredRuntimeArtifactSignature algorithm(String algorithm) {
    this.algorithm = algorithm;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("algorithm")
   @Size(max=100)public String getAlgorithm() {
    return algorithm;
  }

  @JsonProperty("algorithm")
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   **/
  public DesiredRuntimeArtifactSignature keyId(String keyId) {
    this.keyId = keyId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyId")
   @Size(max=500)public String getKeyId() {
    return keyId;
  }

  @JsonProperty("keyId")
  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  /**
   **/
  public DesiredRuntimeArtifactSignature value(String value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("value")
   @Size(max=10000)public String getValue() {
    return value;
  }

  @JsonProperty("value")
  public void setValue(String value) {
    this.value = value;
  }

  /**
   **/
  public DesiredRuntimeArtifactSignature signedAt(OffsetDateTime signedAt) {
    this.signedAt = signedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("signedAt")
  public OffsetDateTime getSignedAt() {
    return signedAt;
  }

  @JsonProperty("signedAt")
  public void setSignedAt(OffsetDateTime signedAt) {
    this.signedAt = signedAt;
  }

  /**
   **/
  public DesiredRuntimeArtifactSignature signedBy(String signedBy) {
    this.signedBy = signedBy;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("signedBy")
   @Size(max=100)public String getSignedBy() {
    return signedBy;
  }

  @JsonProperty("signedBy")
  public void setSignedBy(String signedBy) {
    this.signedBy = signedBy;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeArtifactSignature desiredRuntimeArtifactSignature = (DesiredRuntimeArtifactSignature) o;
    return Objects.equals(this.type, desiredRuntimeArtifactSignature.type) &&
        Objects.equals(this.algorithm, desiredRuntimeArtifactSignature.algorithm) &&
        Objects.equals(this.keyId, desiredRuntimeArtifactSignature.keyId) &&
        Objects.equals(this.value, desiredRuntimeArtifactSignature.value) &&
        Objects.equals(this.signedAt, desiredRuntimeArtifactSignature.signedAt) &&
        Objects.equals(this.signedBy, desiredRuntimeArtifactSignature.signedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, algorithm, keyId, value, signedAt, signedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeArtifactSignature {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    algorithm: ").append(toIndentedString(algorithm)).append("\n");
    sb.append("    keyId: ").append(toIndentedString(keyId)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    signedAt: ").append(toIndentedString(signedAt)).append("\n");
    sb.append("    signedBy: ").append(toIndentedString(signedBy)).append("\n");
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
