package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentGenerationPreviewRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentGenerationPreviewRequest   {
  private AgentGenerationMode preferredGenerationMode;
  private Boolean includeDslPreview = true;
  private Boolean includeValidationPlan = true;

  public AgentGenerationPreviewRequest() {
  }

  /**
   **/
  public AgentGenerationPreviewRequest preferredGenerationMode(AgentGenerationMode preferredGenerationMode) {
    this.preferredGenerationMode = preferredGenerationMode;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("preferredGenerationMode")
  public AgentGenerationMode getPreferredGenerationMode() {
    return preferredGenerationMode;
  }

  @JsonProperty("preferredGenerationMode")
  public void setPreferredGenerationMode(AgentGenerationMode preferredGenerationMode) {
    this.preferredGenerationMode = preferredGenerationMode;
  }

  /**
   **/
  public AgentGenerationPreviewRequest includeDslPreview(Boolean includeDslPreview) {
    this.includeDslPreview = includeDslPreview;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("includeDslPreview")
  public Boolean getIncludeDslPreview() {
    return includeDslPreview;
  }

  @JsonProperty("includeDslPreview")
  public void setIncludeDslPreview(Boolean includeDslPreview) {
    this.includeDslPreview = includeDslPreview;
  }

  /**
   **/
  public AgentGenerationPreviewRequest includeValidationPlan(Boolean includeValidationPlan) {
    this.includeValidationPlan = includeValidationPlan;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("includeValidationPlan")
  public Boolean getIncludeValidationPlan() {
    return includeValidationPlan;
  }

  @JsonProperty("includeValidationPlan")
  public void setIncludeValidationPlan(Boolean includeValidationPlan) {
    this.includeValidationPlan = includeValidationPlan;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentGenerationPreviewRequest agentGenerationPreviewRequest = (AgentGenerationPreviewRequest) o;
    return Objects.equals(this.preferredGenerationMode, agentGenerationPreviewRequest.preferredGenerationMode) &&
        Objects.equals(this.includeDslPreview, agentGenerationPreviewRequest.includeDslPreview) &&
        Objects.equals(this.includeValidationPlan, agentGenerationPreviewRequest.includeValidationPlan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(preferredGenerationMode, includeDslPreview, includeValidationPlan);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentGenerationPreviewRequest {\n");
    
    sb.append("    preferredGenerationMode: ").append(toIndentedString(preferredGenerationMode)).append("\n");
    sb.append("    includeDslPreview: ").append(toIndentedString(includeDslPreview)).append("\n");
    sb.append("    includeValidationPlan: ").append(toIndentedString(includeValidationPlan)).append("\n");
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
