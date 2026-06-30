package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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

/**
 * Complete desired ACTIVE runtime package to create, update, reload or reactivate in the Agent Orchestrator.
 **/
@ApiModel(description = "Complete desired ACTIVE runtime package to create, update, reload or reactivate in the Agent Orchestrator.")
@JsonTypeName("DesiredRuntimeCatalogUpsertItem")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeCatalogUpsertItem   {
  public enum ActionEnum {

    UPSERT(String.valueOf("UPSERT"));


    private String value;

    ActionEnum (String v) {
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
    public static ActionEnum fromString(String s) {
        for (ActionEnum b : ActionEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static ActionEnum fromValue(String value) {
        for (ActionEnum b : ActionEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private ActionEnum action;
  private String agentDefinitionId;
  public enum SourceStatusEnum {

    ACTIVE(String.valueOf("ACTIVE"));


    private String value;

    SourceStatusEnum (String v) {
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
    public static SourceStatusEnum fromString(String s) {
        for (SourceStatusEnum b : SourceStatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static SourceStatusEnum fromValue(String value) {
        for (SourceStatusEnum b : SourceStatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private SourceStatusEnum sourceStatus;
  private OffsetDateTime sourceUpdatedAt;
  private String packageFingerprint;
  private DesiredRuntimeAgentSubmission runtimePackage;

  public DesiredRuntimeCatalogUpsertItem() {
  }

  @JsonCreator
  public DesiredRuntimeCatalogUpsertItem(
    @JsonProperty(required = true, value = "action") ActionEnum action,
    @JsonProperty(required = true, value = "agentDefinitionId") String agentDefinitionId,
    @JsonProperty(required = true, value = "sourceStatus") SourceStatusEnum sourceStatus,
    @JsonProperty(required = true, value = "sourceUpdatedAt") OffsetDateTime sourceUpdatedAt,
    @JsonProperty(required = true, value = "packageFingerprint") String packageFingerprint,
    @JsonProperty(required = true, value = "runtimePackage") DesiredRuntimeAgentSubmission runtimePackage
  ) {
    this.action = action;
    this.agentDefinitionId = agentDefinitionId;
    this.sourceStatus = sourceStatus;
    this.sourceUpdatedAt = sourceUpdatedAt;
    this.packageFingerprint = packageFingerprint;
    this.runtimePackage = runtimePackage;
  }

  /**
   **/
  public DesiredRuntimeCatalogUpsertItem action(ActionEnum action) {
    this.action = action;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "action")
  @NotNull public ActionEnum getAction() {
    return action;
  }

  @JsonProperty(required = true, value = "action")
  public void setAction(ActionEnum action) {
    this.action = action;
  }

  /**
   **/
  public DesiredRuntimeCatalogUpsertItem agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentDefinitionId")
  @NotNull  @Size(min=1,max=50)public String getAgentDefinitionId() {
    return agentDefinitionId;
  }

  @JsonProperty(required = true, value = "agentDefinitionId")
  public void setAgentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
  }

  /**
   **/
  public DesiredRuntimeCatalogUpsertItem sourceStatus(SourceStatusEnum sourceStatus) {
    this.sourceStatus = sourceStatus;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "sourceStatus")
  @NotNull public SourceStatusEnum getSourceStatus() {
    return sourceStatus;
  }

  @JsonProperty(required = true, value = "sourceStatus")
  public void setSourceStatus(SourceStatusEnum sourceStatus) {
    this.sourceStatus = sourceStatus;
  }

  /**
   **/
  public DesiredRuntimeCatalogUpsertItem sourceUpdatedAt(OffsetDateTime sourceUpdatedAt) {
    this.sourceUpdatedAt = sourceUpdatedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "sourceUpdatedAt")
  @NotNull public OffsetDateTime getSourceUpdatedAt() {
    return sourceUpdatedAt;
  }

  @JsonProperty(required = true, value = "sourceUpdatedAt")
  public void setSourceUpdatedAt(OffsetDateTime sourceUpdatedAt) {
    this.sourceUpdatedAt = sourceUpdatedAt;
  }

  /**
   * SHA-256 hexadecimal fingerprint of the canonical complete runtimePackage. It covers runtime-significant package fields and is not only the artifact hash.
   **/
  public DesiredRuntimeCatalogUpsertItem packageFingerprint(String packageFingerprint) {
    this.packageFingerprint = packageFingerprint;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "SHA-256 hexadecimal fingerprint of the canonical complete runtimePackage. It covers runtime-significant package fields and is not only the artifact hash.")
  @JsonProperty(required = true, value = "packageFingerprint")
  @NotNull  @Pattern(regexp="^[a-fA-F0-9]{64}$")public String getPackageFingerprint() {
    return packageFingerprint;
  }

  @JsonProperty(required = true, value = "packageFingerprint")
  public void setPackageFingerprint(String packageFingerprint) {
    this.packageFingerprint = packageFingerprint;
  }

  /**
   **/
  public DesiredRuntimeCatalogUpsertItem runtimePackage(DesiredRuntimeAgentSubmission runtimePackage) {
    this.runtimePackage = runtimePackage;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "runtimePackage")
  @NotNull @Valid public DesiredRuntimeAgentSubmission getRuntimePackage() {
    return runtimePackage;
  }

  @JsonProperty(required = true, value = "runtimePackage")
  public void setRuntimePackage(DesiredRuntimeAgentSubmission runtimePackage) {
    this.runtimePackage = runtimePackage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeCatalogUpsertItem desiredRuntimeCatalogUpsertItem = (DesiredRuntimeCatalogUpsertItem) o;
    return Objects.equals(this.action, desiredRuntimeCatalogUpsertItem.action) &&
        Objects.equals(this.agentDefinitionId, desiredRuntimeCatalogUpsertItem.agentDefinitionId) &&
        Objects.equals(this.sourceStatus, desiredRuntimeCatalogUpsertItem.sourceStatus) &&
        Objects.equals(this.sourceUpdatedAt, desiredRuntimeCatalogUpsertItem.sourceUpdatedAt) &&
        Objects.equals(this.packageFingerprint, desiredRuntimeCatalogUpsertItem.packageFingerprint) &&
        Objects.equals(this.runtimePackage, desiredRuntimeCatalogUpsertItem.runtimePackage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(action, agentDefinitionId, sourceStatus, sourceUpdatedAt, packageFingerprint, runtimePackage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeCatalogUpsertItem {\n");
    
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    sourceStatus: ").append(toIndentedString(sourceStatus)).append("\n");
    sb.append("    sourceUpdatedAt: ").append(toIndentedString(sourceUpdatedAt)).append("\n");
    sb.append("    packageFingerprint: ").append(toIndentedString(packageFingerprint)).append("\n");
    sb.append("    runtimePackage: ").append(toIndentedString(runtimePackage)).append("\n");
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
