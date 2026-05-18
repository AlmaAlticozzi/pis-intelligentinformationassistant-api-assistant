package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSchedule;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AlertCreateRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertCreateRequest   {
  private String name;
  private String description;
  private String prompt;
  private AlertInterpreterType preferredInterpreterType;
  private AlertSchedule schedule;
  private Boolean enableAfterVerification = false;

  public AlertCreateRequest() {
  }

  @JsonCreator
  public AlertCreateRequest(
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "prompt") String prompt
  ) {
    this.name = name;
    this.prompt = prompt;
  }

  /**
   **/
  public AlertCreateRequest name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Cancelled journeys without announcements", required = true, value = "")
  @JsonProperty(required = true, value = "name")
  @NotNull  @Size(min=1,max=120)public String getName() {
    return name;
  }

  @JsonProperty(required = true, value = "name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AlertCreateRequest description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
   @Size(max=1000)public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Free-text alert rule written by the operator.
   **/
  public AlertCreateRequest prompt(String prompt) {
    this.prompt = prompt;
    return this;
  }

  
  @ApiModelProperty(example = "Create a suggestion when a journey is cancelled and no audio message has been broadcast within five minutes.", required = true, value = "Free-text alert rule written by the operator.")
  @JsonProperty(required = true, value = "prompt")
  @NotNull  @Size(min=10,max=8000)public String getPrompt() {
    return prompt;
  }

  @JsonProperty(required = true, value = "prompt")
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   **/
  public AlertCreateRequest preferredInterpreterType(AlertInterpreterType preferredInterpreterType) {
    this.preferredInterpreterType = preferredInterpreterType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("preferredInterpreterType")
  public AlertInterpreterType getPreferredInterpreterType() {
    return preferredInterpreterType;
  }

  @JsonProperty("preferredInterpreterType")
  public void setPreferredInterpreterType(AlertInterpreterType preferredInterpreterType) {
    this.preferredInterpreterType = preferredInterpreterType;
  }

  /**
   **/
  public AlertCreateRequest schedule(AlertSchedule schedule) {
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
   * If true, the alert is enabled automatically only when verification succeeds.
   **/
  public AlertCreateRequest enableAfterVerification(Boolean enableAfterVerification) {
    this.enableAfterVerification = enableAfterVerification;
    return this;
  }

  
  @ApiModelProperty(value = "If true, the alert is enabled automatically only when verification succeeds.")
  @JsonProperty("enableAfterVerification")
  public Boolean getEnableAfterVerification() {
    return enableAfterVerification;
  }

  @JsonProperty("enableAfterVerification")
  public void setEnableAfterVerification(Boolean enableAfterVerification) {
    this.enableAfterVerification = enableAfterVerification;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertCreateRequest alertCreateRequest = (AlertCreateRequest) o;
    return Objects.equals(this.name, alertCreateRequest.name) &&
        Objects.equals(this.description, alertCreateRequest.description) &&
        Objects.equals(this.prompt, alertCreateRequest.prompt) &&
        Objects.equals(this.preferredInterpreterType, alertCreateRequest.preferredInterpreterType) &&
        Objects.equals(this.schedule, alertCreateRequest.schedule) &&
        Objects.equals(this.enableAfterVerification, alertCreateRequest.enableAfterVerification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, prompt, preferredInterpreterType, schedule, enableAfterVerification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertCreateRequest {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
    sb.append("    preferredInterpreterType: ").append(toIndentedString(preferredInterpreterType)).append("\n");
    sb.append("    schedule: ").append(toIndentedString(schedule)).append("\n");
    sb.append("    enableAfterVerification: ").append(toIndentedString(enableAfterVerification)).append("\n");
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
