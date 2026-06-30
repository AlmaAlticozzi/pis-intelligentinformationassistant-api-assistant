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
 * Tombstone instructing reconciliation that the definition must not remain operational. It never contains a runtime package.
 **/
@ApiModel(description = "Tombstone instructing reconciliation that the definition must not remain operational. It never contains a runtime package.")
@JsonTypeName("DesiredRuntimeCatalogRemovalItem")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeCatalogRemovalItem   {
  public enum ActionEnum {

    REMOVE(String.valueOf("REMOVE"));


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
  private DesiredRuntimeCatalogRemovalReason removalReason;
  private AgentDefinitionStatus sourceStatus;
  private OffsetDateTime sourceUpdatedAt;
  private OffsetDateTime evaluatedAt;

  public DesiredRuntimeCatalogRemovalItem() {
  }

  @JsonCreator
  public DesiredRuntimeCatalogRemovalItem(
    @JsonProperty(required = true, value = "action") ActionEnum action,
    @JsonProperty(required = true, value = "agentDefinitionId") String agentDefinitionId,
    @JsonProperty(required = true, value = "removalReason") DesiredRuntimeCatalogRemovalReason removalReason,
    @JsonProperty(required = true, value = "evaluatedAt") OffsetDateTime evaluatedAt
  ) {
    this.action = action;
    this.agentDefinitionId = agentDefinitionId;
    this.removalReason = removalReason;
    this.evaluatedAt = evaluatedAt;
  }

  /**
   **/
  public DesiredRuntimeCatalogRemovalItem action(ActionEnum action) {
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
  public DesiredRuntimeCatalogRemovalItem agentDefinitionId(String agentDefinitionId) {
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
  public DesiredRuntimeCatalogRemovalItem removalReason(DesiredRuntimeCatalogRemovalReason removalReason) {
    this.removalReason = removalReason;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "removalReason")
  @NotNull public DesiredRuntimeCatalogRemovalReason getRemovalReason() {
    return removalReason;
  }

  @JsonProperty(required = true, value = "removalReason")
  public void setRemovalReason(DesiredRuntimeCatalogRemovalReason removalReason) {
    this.removalReason = removalReason;
  }

  /**
   * Current Assistant status when the definition exists. Omitted or null for NOT_FOUND.
   **/
  public DesiredRuntimeCatalogRemovalItem sourceStatus(AgentDefinitionStatus sourceStatus) {
    this.sourceStatus = sourceStatus;
    return this;
  }

  
  @ApiModelProperty(value = "Current Assistant status when the definition exists. Omitted or null for NOT_FOUND.")
  @JsonProperty("sourceStatus")
  public AgentDefinitionStatus getSourceStatus() {
    return sourceStatus;
  }

  @JsonProperty("sourceStatus")
  public void setSourceStatus(AgentDefinitionStatus sourceStatus) {
    this.sourceStatus = sourceStatus;
  }

  /**
   **/
  public DesiredRuntimeCatalogRemovalItem sourceUpdatedAt(OffsetDateTime sourceUpdatedAt) {
    this.sourceUpdatedAt = sourceUpdatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sourceUpdatedAt")
  public OffsetDateTime getSourceUpdatedAt() {
    return sourceUpdatedAt;
  }

  @JsonProperty("sourceUpdatedAt")
  public void setSourceUpdatedAt(OffsetDateTime sourceUpdatedAt) {
    this.sourceUpdatedAt = sourceUpdatedAt;
  }

  /**
   * Timestamp at which the targeted or incremental absence decision was evaluated.
   **/
  public DesiredRuntimeCatalogRemovalItem evaluatedAt(OffsetDateTime evaluatedAt) {
    this.evaluatedAt = evaluatedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Timestamp at which the targeted or incremental absence decision was evaluated.")
  @JsonProperty(required = true, value = "evaluatedAt")
  @NotNull public OffsetDateTime getEvaluatedAt() {
    return evaluatedAt;
  }

  @JsonProperty(required = true, value = "evaluatedAt")
  public void setEvaluatedAt(OffsetDateTime evaluatedAt) {
    this.evaluatedAt = evaluatedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeCatalogRemovalItem desiredRuntimeCatalogRemovalItem = (DesiredRuntimeCatalogRemovalItem) o;
    return Objects.equals(this.action, desiredRuntimeCatalogRemovalItem.action) &&
        Objects.equals(this.agentDefinitionId, desiredRuntimeCatalogRemovalItem.agentDefinitionId) &&
        Objects.equals(this.removalReason, desiredRuntimeCatalogRemovalItem.removalReason) &&
        Objects.equals(this.sourceStatus, desiredRuntimeCatalogRemovalItem.sourceStatus) &&
        Objects.equals(this.sourceUpdatedAt, desiredRuntimeCatalogRemovalItem.sourceUpdatedAt) &&
        Objects.equals(this.evaluatedAt, desiredRuntimeCatalogRemovalItem.evaluatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(action, agentDefinitionId, removalReason, sourceStatus, sourceUpdatedAt, evaluatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeCatalogRemovalItem {\n");
    
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    removalReason: ").append(toIndentedString(removalReason)).append("\n");
    sb.append("    sourceStatus: ").append(toIndentedString(sourceStatus)).append("\n");
    sb.append("    sourceUpdatedAt: ").append(toIndentedString(sourceUpdatedAt)).append("\n");
    sb.append("    evaluatedAt: ").append(toIndentedString(evaluatedAt)).append("\n");
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
