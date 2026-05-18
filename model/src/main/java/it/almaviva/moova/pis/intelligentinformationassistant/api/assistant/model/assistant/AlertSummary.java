package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSchedule;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AlertSummary")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertSummary   {
  private String id;
  private String name;
  private AlertStatus status;
  private Boolean enabled;
  private AlertInterpreterType interpreterType;
  private @Valid List<SuggestionTargetType> targetTypes = new ArrayList<>();
  private Double confidence;
  private AlertVerificationSummary lastVerification;
  private AlertSchedule schedule;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private Integer version;

  public AlertSummary() {
  }

  @JsonCreator
  public AlertSummary(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "status") AlertStatus status,
    @JsonProperty(required = true, value = "enabled") Boolean enabled,
    @JsonProperty(required = true, value = "createdAt") OffsetDateTime createdAt
  ) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.enabled = enabled;
    this.createdAt = createdAt;
  }

  /**
   **/
  public AlertSummary id(String id) {
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
  public AlertSummary name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Cancelled journeys without announcements", required = true, value = "")
  @JsonProperty(required = true, value = "name")
  @NotNull public String getName() {
    return name;
  }

  @JsonProperty(required = true, value = "name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AlertSummary status(AlertStatus status) {
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
  public AlertSummary enabled(Boolean enabled) {
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
  public AlertSummary interpreterType(AlertInterpreterType interpreterType) {
    this.interpreterType = interpreterType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("interpreterType")
  public AlertInterpreterType getInterpreterType() {
    return interpreterType;
  }

  @JsonProperty("interpreterType")
  public void setInterpreterType(AlertInterpreterType interpreterType) {
    this.interpreterType = interpreterType;
  }

  /**
   **/
  public AlertSummary targetTypes(List<SuggestionTargetType> targetTypes) {
    this.targetTypes = targetTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("targetTypes")
  public List<SuggestionTargetType> getTargetTypes() {
    return targetTypes;
  }

  @JsonProperty("targetTypes")
  public void setTargetTypes(List<SuggestionTargetType> targetTypes) {
    this.targetTypes = targetTypes;
  }

  public AlertSummary addTargetTypesItem(SuggestionTargetType targetTypesItem) {
    if (this.targetTypes == null) {
      this.targetTypes = new ArrayList<>();
    }

    this.targetTypes.add(targetTypesItem);
    return this;
  }

  public AlertSummary removeTargetTypesItem(SuggestionTargetType targetTypesItem) {
    if (targetTypesItem != null && this.targetTypes != null) {
      this.targetTypes.remove(targetTypesItem);
    }

    return this;
  }
  /**
   * Estimated detection success/confidence shown by the UI.
   * minimum: 0
   * maximum: 1
   **/
  public AlertSummary confidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }

  
  @ApiModelProperty(example = "0.86", value = "Estimated detection success/confidence shown by the UI.")
  @JsonProperty("confidence")
   @DecimalMin("0") @DecimalMax("1")public Double getConfidence() {
    return confidence;
  }

  @JsonProperty("confidence")
  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  /**
   **/
  public AlertSummary lastVerification(AlertVerificationSummary lastVerification) {
    this.lastVerification = lastVerification;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastVerification")
  @Valid public AlertVerificationSummary getLastVerification() {
    return lastVerification;
  }

  @JsonProperty("lastVerification")
  public void setLastVerification(AlertVerificationSummary lastVerification) {
    this.lastVerification = lastVerification;
  }

  /**
   **/
  public AlertSummary schedule(AlertSchedule schedule) {
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
  public AlertSummary createdAt(OffsetDateTime createdAt) {
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
  public AlertSummary updatedAt(OffsetDateTime updatedAt) {
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
  public AlertSummary version(Integer version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("version")
  public Integer getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(Integer version) {
    this.version = version;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertSummary alertSummary = (AlertSummary) o;
    return Objects.equals(this.id, alertSummary.id) &&
        Objects.equals(this.name, alertSummary.name) &&
        Objects.equals(this.status, alertSummary.status) &&
        Objects.equals(this.enabled, alertSummary.enabled) &&
        Objects.equals(this.interpreterType, alertSummary.interpreterType) &&
        Objects.equals(this.targetTypes, alertSummary.targetTypes) &&
        Objects.equals(this.confidence, alertSummary.confidence) &&
        Objects.equals(this.lastVerification, alertSummary.lastVerification) &&
        Objects.equals(this.schedule, alertSummary.schedule) &&
        Objects.equals(this.createdAt, alertSummary.createdAt) &&
        Objects.equals(this.updatedAt, alertSummary.updatedAt) &&
        Objects.equals(this.version, alertSummary.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, status, enabled, interpreterType, targetTypes, confidence, lastVerification, schedule, createdAt, updatedAt, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertSummary {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    interpreterType: ").append(toIndentedString(interpreterType)).append("\n");
    sb.append("    targetTypes: ").append(toIndentedString(targetTypes)).append("\n");
    sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
    sb.append("    lastVerification: ").append(toIndentedString(lastVerification)).append("\n");
    sb.append("    schedule: ").append(toIndentedString(schedule)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
