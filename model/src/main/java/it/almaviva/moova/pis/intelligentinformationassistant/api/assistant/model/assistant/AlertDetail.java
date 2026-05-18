package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreter;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRequiredData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRuntimeMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSchedule;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationResult;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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



@JsonTypeName("AlertDetail")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertDetail   {
  private String id;
  private String name;
  private String description;
  private String prompt;
  private AlertStatus status;
  private Boolean enabled;
  private AlertVerificationResult verification;
  private AlertInterpreter interpreter;
  private @Valid List<@Valid AlertRequiredData> requiredData = new ArrayList<>();
  private AlertSchedule schedule;
  private AlertRuntimeMetadata runtime;
  private String createdBy;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private OffsetDateTime deletedAt;
  private Integer version;
  private @Valid List<@Valid AgentDefinitionSummary> agentDefinitions = new ArrayList<>();

  public AlertDetail() {
  }

  @JsonCreator
  public AlertDetail(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "prompt") String prompt,
    @JsonProperty(required = true, value = "status") AlertStatus status,
    @JsonProperty(required = true, value = "enabled") Boolean enabled,
    @JsonProperty(required = true, value = "createdAt") OffsetDateTime createdAt
  ) {
    this.id = id;
    this.name = name;
    this.prompt = prompt;
    this.status = status;
    this.enabled = enabled;
    this.createdAt = createdAt;
  }

  /**
   **/
  public AlertDetail id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "ALRT2026251400000001", required = true, value = "")
  @JsonProperty(required = true, value = "id")
  @NotNull  @Size(max=50)public String getId() {
    return id;
  }

  @JsonProperty(required = true, value = "id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public AlertDetail name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Cancelled journeys without announcements", required = true, value = "")
  @JsonProperty(required = true, value = "name")
  @NotNull  @Size(max=120)public String getName() {
    return name;
  }

  @JsonProperty(required = true, value = "name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AlertDetail description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Detects cancelled journeys for which no passenger announcement has been generated or broadcast.", value = "")
  @JsonProperty("description")
   @Size(max=1000)public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Free-text prompt inserted by the operator.
   **/
  public AlertDetail prompt(String prompt) {
    this.prompt = prompt;
    return this;
  }

  
  @ApiModelProperty(example = "Create a suggestion when a journey is cancelled and no audio message has been broadcast within five minutes.", required = true, value = "Free-text prompt inserted by the operator.")
  @JsonProperty(required = true, value = "prompt")
  @NotNull  @Size(max=8000)public String getPrompt() {
    return prompt;
  }

  @JsonProperty(required = true, value = "prompt")
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   **/
  public AlertDetail status(AlertStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public AlertStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(AlertStatus status) {
    this.status = status;
  }

  /**
   **/
  public AlertDetail enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "")
  @JsonProperty(required = true, value = "enabled")
  @NotNull public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty(required = true, value = "enabled")
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public AlertDetail verification(AlertVerificationResult verification) {
    this.verification = verification;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("verification")
  @Valid public AlertVerificationResult getVerification() {
    return verification;
  }

  @JsonProperty("verification")
  public void setVerification(AlertVerificationResult verification) {
    this.verification = verification;
  }

  /**
   **/
  public AlertDetail interpreter(AlertInterpreter interpreter) {
    this.interpreter = interpreter;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("interpreter")
  @Valid public AlertInterpreter getInterpreter() {
    return interpreter;
  }

  @JsonProperty("interpreter")
  public void setInterpreter(AlertInterpreter interpreter) {
    this.interpreter = interpreter;
  }

  /**
   * Data categories required by the interpreter.
   **/
  public AlertDetail requiredData(List<@Valid AlertRequiredData> requiredData) {
    this.requiredData = requiredData;
    return this;
  }

  
  @ApiModelProperty(value = "Data categories required by the interpreter.")
  @JsonProperty("requiredData")
  @Valid public List<@Valid AlertRequiredData> getRequiredData() {
    return requiredData;
  }

  @JsonProperty("requiredData")
  public void setRequiredData(List<@Valid AlertRequiredData> requiredData) {
    this.requiredData = requiredData;
  }

  public AlertDetail addRequiredDataItem(AlertRequiredData requiredDataItem) {
    if (this.requiredData == null) {
      this.requiredData = new ArrayList<>();
    }

    this.requiredData.add(requiredDataItem);
    return this;
  }

  public AlertDetail removeRequiredDataItem(AlertRequiredData requiredDataItem) {
    if (requiredDataItem != null && this.requiredData != null) {
      this.requiredData.remove(requiredDataItem);
    }

    return this;
  }
  /**
   **/
  public AlertDetail schedule(AlertSchedule schedule) {
    this.schedule = schedule;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("schedule")
  @Valid public AlertSchedule getSchedule() {
    return schedule;
  }

  @JsonProperty("schedule")
  public void setSchedule(AlertSchedule schedule) {
    this.schedule = schedule;
  }

  /**
   **/
  public AlertDetail runtime(AlertRuntimeMetadata runtime) {
    this.runtime = runtime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("runtime")
  @Valid public AlertRuntimeMetadata getRuntime() {
    return runtime;
  }

  @JsonProperty("runtime")
  public void setRuntime(AlertRuntimeMetadata runtime) {
    this.runtime = runtime;
  }

  /**
   **/
  public AlertDetail createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
  @ApiModelProperty(example = "m.user", value = "")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }

  @JsonProperty("createdBy")
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   **/
  public AlertDetail createdAt(OffsetDateTime createdAt) {
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
   **/
  public AlertDetail updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   **/
  public AlertDetail deletedAt(OffsetDateTime deletedAt) {
    this.deletedAt = deletedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deletedAt")
  public OffsetDateTime getDeletedAt() {
    return deletedAt;
  }

  @JsonProperty("deletedAt")
  public void setDeletedAt(OffsetDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  /**
   * Monotonic Alert version. A new version is produced when prompt or relevant verification inputs change.
   **/
  public AlertDetail version(Integer version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Monotonic Alert version. A new version is produced when prompt or relevant verification inputs change.")
  @JsonProperty("version")
  public Integer getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(Integer version) {
    this.version = version;
  }

  /**
   * Agent definitions generated from this Alert.
   **/
  public AlertDetail agentDefinitions(List<@Valid AgentDefinitionSummary> agentDefinitions) {
    this.agentDefinitions = agentDefinitions;
    return this;
  }

  
  @ApiModelProperty(value = "Agent definitions generated from this Alert.")
  @JsonProperty("agentDefinitions")
  @Valid public List<@Valid AgentDefinitionSummary> getAgentDefinitions() {
    return agentDefinitions;
  }

  @JsonProperty("agentDefinitions")
  public void setAgentDefinitions(List<@Valid AgentDefinitionSummary> agentDefinitions) {
    this.agentDefinitions = agentDefinitions;
  }

  public AlertDetail addAgentDefinitionsItem(AgentDefinitionSummary agentDefinitionsItem) {
    if (this.agentDefinitions == null) {
      this.agentDefinitions = new ArrayList<>();
    }

    this.agentDefinitions.add(agentDefinitionsItem);
    return this;
  }

  public AlertDetail removeAgentDefinitionsItem(AgentDefinitionSummary agentDefinitionsItem) {
    if (agentDefinitionsItem != null && this.agentDefinitions != null) {
      this.agentDefinitions.remove(agentDefinitionsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertDetail alertDetail = (AlertDetail) o;
    return Objects.equals(this.id, alertDetail.id) &&
        Objects.equals(this.name, alertDetail.name) &&
        Objects.equals(this.description, alertDetail.description) &&
        Objects.equals(this.prompt, alertDetail.prompt) &&
        Objects.equals(this.status, alertDetail.status) &&
        Objects.equals(this.enabled, alertDetail.enabled) &&
        Objects.equals(this.verification, alertDetail.verification) &&
        Objects.equals(this.interpreter, alertDetail.interpreter) &&
        Objects.equals(this.requiredData, alertDetail.requiredData) &&
        Objects.equals(this.schedule, alertDetail.schedule) &&
        Objects.equals(this.runtime, alertDetail.runtime) &&
        Objects.equals(this.createdBy, alertDetail.createdBy) &&
        Objects.equals(this.createdAt, alertDetail.createdAt) &&
        Objects.equals(this.updatedAt, alertDetail.updatedAt) &&
        Objects.equals(this.deletedAt, alertDetail.deletedAt) &&
        Objects.equals(this.version, alertDetail.version) &&
        Objects.equals(this.agentDefinitions, alertDetail.agentDefinitions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, prompt, status, enabled, verification, interpreter, requiredData, schedule, runtime, createdBy, createdAt, updatedAt, deletedAt, version, agentDefinitions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertDetail {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    verification: ").append(toIndentedString(verification)).append("\n");
    sb.append("    interpreter: ").append(toIndentedString(interpreter)).append("\n");
    sb.append("    requiredData: ").append(toIndentedString(requiredData)).append("\n");
    sb.append("    schedule: ").append(toIndentedString(schedule)).append("\n");
    sb.append("    runtime: ").append(toIndentedString(runtime)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    deletedAt: ").append(toIndentedString(deletedAt)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    agentDefinitions: ").append(toIndentedString(agentDefinitions)).append("\n");
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
