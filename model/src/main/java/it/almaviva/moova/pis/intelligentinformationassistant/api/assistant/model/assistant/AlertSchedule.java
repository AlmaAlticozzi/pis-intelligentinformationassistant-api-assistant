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

/**
 * Schedule for scheduled interpreters. Required only when &#x60;interpreterType&#x60; is &#x60;SCHEDULED_INTERPRETER&#x60;.
 **/
@ApiModel(description = "Schedule for scheduled interpreters. Required only when `interpreterType` is `SCHEDULED_INTERPRETER`.")
@JsonTypeName("AlertSchedule")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertSchedule   {
  private Integer frequencySeconds;
  private Integer timeWindowMinutes;
  private Boolean enabledOnlyInServiceHours = false;
  private String cronExpression;

  public AlertSchedule() {
  }

  /**
   * minimum: 30
   * maximum: 86400
   **/
  public AlertSchedule frequencySeconds(Integer frequencySeconds) {
    this.frequencySeconds = frequencySeconds;
    return this;
  }

  
  @ApiModelProperty(example = "300", value = "")
  @JsonProperty("frequencySeconds")
   @Min(30) @Max(86400)public Integer getFrequencySeconds() {
    return frequencySeconds;
  }

  @JsonProperty("frequencySeconds")
  public void setFrequencySeconds(Integer frequencySeconds) {
    this.frequencySeconds = frequencySeconds;
  }

  /**
   * minimum: 1
   * maximum: 1440
   **/
  public AlertSchedule timeWindowMinutes(Integer timeWindowMinutes) {
    this.timeWindowMinutes = timeWindowMinutes;
    return this;
  }

  
  @ApiModelProperty(example = "30", value = "")
  @JsonProperty("timeWindowMinutes")
   @Min(1) @Max(1440)public Integer getTimeWindowMinutes() {
    return timeWindowMinutes;
  }

  @JsonProperty("timeWindowMinutes")
  public void setTimeWindowMinutes(Integer timeWindowMinutes) {
    this.timeWindowMinutes = timeWindowMinutes;
  }

  /**
   **/
  public AlertSchedule enabledOnlyInServiceHours(Boolean enabledOnlyInServiceHours) {
    this.enabledOnlyInServiceHours = enabledOnlyInServiceHours;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("enabledOnlyInServiceHours")
  public Boolean getEnabledOnlyInServiceHours() {
    return enabledOnlyInServiceHours;
  }

  @JsonProperty("enabledOnlyInServiceHours")
  public void setEnabledOnlyInServiceHours(Boolean enabledOnlyInServiceHours) {
    this.enabledOnlyInServiceHours = enabledOnlyInServiceHours;
  }

  /**
   * Optional advanced cron expression if supported by the implementation.
   **/
  public AlertSchedule cronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
    return this;
  }

  
  @ApiModelProperty(example = "0 *_/5 * * * ?", value = "Optional advanced cron expression if supported by the implementation.")
  @JsonProperty("cronExpression")
  public String getCronExpression() {
    return cronExpression;
  }

  @JsonProperty("cronExpression")
  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertSchedule alertSchedule = (AlertSchedule) o;
    return Objects.equals(this.frequencySeconds, alertSchedule.frequencySeconds) &&
        Objects.equals(this.timeWindowMinutes, alertSchedule.timeWindowMinutes) &&
        Objects.equals(this.enabledOnlyInServiceHours, alertSchedule.enabledOnlyInServiceHours) &&
        Objects.equals(this.cronExpression, alertSchedule.cronExpression);
  }

  @Override
  public int hashCode() {
    return Objects.hash(frequencySeconds, timeWindowMinutes, enabledOnlyInServiceHours, cronExpression);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertSchedule {\n");
    
    sb.append("    frequencySeconds: ").append(toIndentedString(frequencySeconds)).append("\n");
    sb.append("    timeWindowMinutes: ").append(toIndentedString(timeWindowMinutes)).append("\n");
    sb.append("    enabledOnlyInServiceHours: ").append(toIndentedString(enabledOnlyInServiceHours)).append("\n");
    sb.append("    cronExpression: ").append(toIndentedString(cronExpression)).append("\n");
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
