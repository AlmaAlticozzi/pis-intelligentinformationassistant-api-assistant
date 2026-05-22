package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("AlertUpdateRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-22T15:04:49.137501121Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertUpdateRequest   {
  private String name;
  private String description;
  private String prompt;
  private Boolean verifyImmediately = false;
  private Boolean enableAfterVerification = false;

  public AlertUpdateRequest() {
  }

  @JsonCreator
  public AlertUpdateRequest(
    @JsonProperty(required = true, value = "name") String name,
    @JsonProperty(required = true, value = "prompt") String prompt
  ) {
    this.name = name;
    this.prompt = prompt;
  }

  /**
   **/
  public AlertUpdateRequest name(String name) {
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
  public AlertUpdateRequest description(String description) {
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
   * Free-text alert rule written by the operator.  If this value is different from the currently persisted prompt, every previously verified technical interpretation must be considered obsolete. The backend must either reset the Alert to &#x60;DRAFT&#x60; or start a new verification according to &#x60;verifyImmediately&#x60;. 
   **/
  public AlertUpdateRequest prompt(String prompt) {
    this.prompt = prompt;
    return this;
  }

  
  @ApiModelProperty(example = "Create a suggestion when a journey is cancelled and no audio message has been broadcast within five minutes.", required = true, value = "Free-text alert rule written by the operator.  If this value is different from the currently persisted prompt, every previously verified technical interpretation must be considered obsolete. The backend must either reset the Alert to `DRAFT` or start a new verification according to `verifyImmediately`. ")
  @JsonProperty(required = true, value = "prompt")
  @NotNull  @Size(min=10,max=8000)public String getPrompt() {
    return prompt;
  }

  @JsonProperty(required = true, value = "prompt")
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   * If true and the prompt has changed, the backend starts AI-assisted re-verification after updating the Alert.  If the prompt has not changed, this flag is ignored.  If false and the prompt has changed, the Alert is reset to &#x60;DRAFT&#x60; with verification status &#x60;PENDING&#x60;, without starting AI-assisted verification. 
   **/
  public AlertUpdateRequest verifyImmediately(Boolean verifyImmediately) {
    this.verifyImmediately = verifyImmediately;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "If true and the prompt has changed, the backend starts AI-assisted re-verification after updating the Alert.  If the prompt has not changed, this flag is ignored.  If false and the prompt has changed, the Alert is reset to `DRAFT` with verification status `PENDING`, without starting AI-assisted verification. ")
  @JsonProperty("verifyImmediately")
  public Boolean getVerifyImmediately() {
    return verifyImmediately;
  }

  @JsonProperty("verifyImmediately")
  public void setVerifyImmediately(Boolean verifyImmediately) {
    this.verifyImmediately = verifyImmediately;
  }

  /**
   * Accepted for symmetry with &#x60;AlertCreateRequest&#x60;.  During update, this flag must not be used to enable an Alert that was disabled before the prompt change. If the Alert was enabled before the update, the backend may restore &#x60;enabled &#x3D; true&#x60; only after the new verification completes successfully with status &#x60;VERIFIED&#x60;. 
   **/
  public AlertUpdateRequest enableAfterVerification(Boolean enableAfterVerification) {
    this.enableAfterVerification = enableAfterVerification;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Accepted for symmetry with `AlertCreateRequest`.  During update, this flag must not be used to enable an Alert that was disabled before the prompt change. If the Alert was enabled before the update, the backend may restore `enabled = true` only after the new verification completes successfully with status `VERIFIED`. ")
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
    AlertUpdateRequest alertUpdateRequest = (AlertUpdateRequest) o;
    return Objects.equals(this.name, alertUpdateRequest.name) &&
        Objects.equals(this.description, alertUpdateRequest.description) &&
        Objects.equals(this.prompt, alertUpdateRequest.prompt) &&
        Objects.equals(this.verifyImmediately, alertUpdateRequest.verifyImmediately) &&
        Objects.equals(this.enableAfterVerification, alertUpdateRequest.enableAfterVerification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, prompt, verifyImmediately, enableAfterVerification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertUpdateRequest {\n");
    
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
