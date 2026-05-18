package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("AlertPatchRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertPatchRequest   {
  private String name;
  private String description;
  private String prompt;
  private AlertSchedule schedule;

  public AlertPatchRequest() {
  }

  /**
   **/
  public AlertPatchRequest name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
   @Size(min=1,max=120)public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AlertPatchRequest description(String description) {
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
   * Changing the prompt requires re-verification.
   **/
  public AlertPatchRequest prompt(String prompt) {
    this.prompt = prompt;
    return this;
  }

  
  @ApiModelProperty(value = "Changing the prompt requires re-verification.")
  @JsonProperty("prompt")
   @Size(min=10,max=8000)public String getPrompt() {
    return prompt;
  }

  @JsonProperty("prompt")
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   **/
  public AlertPatchRequest schedule(AlertSchedule schedule) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertPatchRequest alertPatchRequest = (AlertPatchRequest) o;
    return Objects.equals(this.name, alertPatchRequest.name) &&
        Objects.equals(this.description, alertPatchRequest.description) &&
        Objects.equals(this.prompt, alertPatchRequest.prompt) &&
        Objects.equals(this.schedule, alertPatchRequest.schedule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, prompt, schedule);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertPatchRequest {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
    sb.append("    schedule: ").append(toIndentedString(schedule)).append("\n");
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
