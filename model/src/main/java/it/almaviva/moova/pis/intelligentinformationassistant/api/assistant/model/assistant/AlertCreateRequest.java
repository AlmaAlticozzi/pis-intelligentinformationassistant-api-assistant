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



@JsonTypeName("AlertCreateRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-19T15:56:44.348406306Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertCreateRequest   {
  private String name;
  private String description;
  private String prompt;
  private Boolean verifyImmediately = false;
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
   * If true, the backend immediately starts AI-assisted verification for the newly created alert. The alert enters &#x60;VERIFYING&#x60; first and can then become &#x60;VERIFIED&#x60;, &#x60;REJECTED&#x60; or &#x60;ERROR&#x60; according to the verification result. If false, the alert is only persisted as &#x60;DRAFT&#x60;; no verification is started and no interpreter metadata is produced until &#x60;POST /v1/alerts/{alertId}/verify&#x60; is called.
   **/
  public AlertCreateRequest verifyImmediately(Boolean verifyImmediately) {
    this.verifyImmediately = verifyImmediately;
    return this;
  }


  @ApiModelProperty(example = "true", value = "If true, the backend immediately starts AI-assisted verification for the newly created alert. The alert enters `VERIFYING` first and can then become `VERIFIED`, `REJECTED` or `ERROR` according to the verification result. If false, the alert is only persisted as `DRAFT`; no verification is started and no interpreter metadata is produced until `POST /v1/alerts/{alertId}/verify` is called.")
  @JsonProperty("verifyImmediately")
  public Boolean getVerifyImmediately() {
    return verifyImmediately;
  }

  @JsonProperty("verifyImmediately")
  public void setVerifyImmediately(Boolean verifyImmediately) {
    this.verifyImmediately = verifyImmediately;
  }

  /**
   * If true, the alert is enabled automatically only when verification succeeds. This flag is meaningful only when &#x60;verifyImmediately&#x60; is true or when a later explicit verification succeeds.
   **/
  public AlertCreateRequest enableAfterVerification(Boolean enableAfterVerification) {
    this.enableAfterVerification = enableAfterVerification;
    return this;
  }


  @ApiModelProperty(value = "If true, the alert is enabled automatically only when verification succeeds. This flag is meaningful only when `verifyImmediately` is true or when a later explicit verification succeeds.")
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
            Objects.equals(this.verifyImmediately, alertCreateRequest.verifyImmediately) &&
            Objects.equals(this.enableAfterVerification, alertCreateRequest.enableAfterVerification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, prompt, verifyImmediately, enableAfterVerification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertCreateRequest {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
    sb.append("    verifyImmediately: ").append(toIndentedString(verifyImmediately)).append("\n");
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
