package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDslPreview;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
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



@JsonTypeName("AgentGenerationPreviewResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentGenerationPreviewResponse   {
  private AlertReference alert;
  private Boolean canGenerate;
  private AgentGenerationMode recommendedGenerationMode;
  private AgentComplexity estimatedComplexity;
  private @Valid List<AgentDataSource> requiredSources = new ArrayList<>();
  private @Valid List<String> requiredPermissions = new ArrayList<>();
  private AgentBlueprint blueprint;
  private AgentDslPreview dslPreview;
  private AgentValidationPlan validationPlan;
  private @Valid List<String> warnings = new ArrayList<>();
  private String rejectedReason;

  public AgentGenerationPreviewResponse() {
  }

  @JsonCreator
  public AgentGenerationPreviewResponse(
    @JsonProperty(required = true, value = "alert") AlertReference alert,
    @JsonProperty(required = true, value = "canGenerate") Boolean canGenerate,
    @JsonProperty(required = true, value = "recommendedGenerationMode") AgentGenerationMode recommendedGenerationMode,
    @JsonProperty(required = true, value = "blueprint") AgentBlueprint blueprint
  ) {
    this.alert = alert;
    this.canGenerate = canGenerate;
    this.recommendedGenerationMode = recommendedGenerationMode;
    this.blueprint = blueprint;
  }

  /**
   **/
  public AgentGenerationPreviewResponse alert(AlertReference alert) {
    this.alert = alert;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "alert")
  @NotNull @Valid public AlertReference getAlert() {
    return alert;
  }

  @JsonProperty(required = true, value = "alert")
  public void setAlert(AlertReference alert) {
    this.alert = alert;
  }

  /**
   **/
  public AgentGenerationPreviewResponse canGenerate(Boolean canGenerate) {
    this.canGenerate = canGenerate;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "")
  @JsonProperty(required = true, value = "canGenerate")
  @NotNull public Boolean getCanGenerate() {
    return canGenerate;
  }

  @JsonProperty(required = true, value = "canGenerate")
  public void setCanGenerate(Boolean canGenerate) {
    this.canGenerate = canGenerate;
  }

  /**
   **/
  public AgentGenerationPreviewResponse recommendedGenerationMode(AgentGenerationMode recommendedGenerationMode) {
    this.recommendedGenerationMode = recommendedGenerationMode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "recommendedGenerationMode")
  @NotNull public AgentGenerationMode getRecommendedGenerationMode() {
    return recommendedGenerationMode;
  }

  @JsonProperty(required = true, value = "recommendedGenerationMode")
  public void setRecommendedGenerationMode(AgentGenerationMode recommendedGenerationMode) {
    this.recommendedGenerationMode = recommendedGenerationMode;
  }

  /**
   **/
  public AgentGenerationPreviewResponse estimatedComplexity(AgentComplexity estimatedComplexity) {
    this.estimatedComplexity = estimatedComplexity;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("estimatedComplexity")
  public AgentComplexity getEstimatedComplexity() {
    return estimatedComplexity;
  }

  @JsonProperty("estimatedComplexity")
  public void setEstimatedComplexity(AgentComplexity estimatedComplexity) {
    this.estimatedComplexity = estimatedComplexity;
  }

  /**
   **/
  public AgentGenerationPreviewResponse requiredSources(List<AgentDataSource> requiredSources) {
    this.requiredSources = requiredSources;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requiredSources")
  public List<AgentDataSource> getRequiredSources() {
    return requiredSources;
  }

  @JsonProperty("requiredSources")
  public void setRequiredSources(List<AgentDataSource> requiredSources) {
    this.requiredSources = requiredSources;
  }

  public AgentGenerationPreviewResponse addRequiredSourcesItem(AgentDataSource requiredSourcesItem) {
    if (this.requiredSources == null) {
      this.requiredSources = new ArrayList<>();
    }

    this.requiredSources.add(requiredSourcesItem);
    return this;
  }

  public AgentGenerationPreviewResponse removeRequiredSourcesItem(AgentDataSource requiredSourcesItem) {
    if (requiredSourcesItem != null && this.requiredSources != null) {
      this.requiredSources.remove(requiredSourcesItem);
    }

    return this;
  }
  /**
   **/
  public AgentGenerationPreviewResponse requiredPermissions(List<String> requiredPermissions) {
    this.requiredPermissions = requiredPermissions;
    return this;
  }

  
  @ApiModelProperty(example = "[\"READ_SERVICE_DATA\",\"READ_AUDIO_MONITORING\"]", value = "")
  @JsonProperty("requiredPermissions")
  public List<String> getRequiredPermissions() {
    return requiredPermissions;
  }

  @JsonProperty("requiredPermissions")
  public void setRequiredPermissions(List<String> requiredPermissions) {
    this.requiredPermissions = requiredPermissions;
  }

  public AgentGenerationPreviewResponse addRequiredPermissionsItem(String requiredPermissionsItem) {
    if (this.requiredPermissions == null) {
      this.requiredPermissions = new ArrayList<>();
    }

    this.requiredPermissions.add(requiredPermissionsItem);
    return this;
  }

  public AgentGenerationPreviewResponse removeRequiredPermissionsItem(String requiredPermissionsItem) {
    if (requiredPermissionsItem != null && this.requiredPermissions != null) {
      this.requiredPermissions.remove(requiredPermissionsItem);
    }

    return this;
  }
  /**
   **/
  public AgentGenerationPreviewResponse blueprint(AgentBlueprint blueprint) {
    this.blueprint = blueprint;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "blueprint")
  @NotNull @Valid public AgentBlueprint getBlueprint() {
    return blueprint;
  }

  @JsonProperty(required = true, value = "blueprint")
  public void setBlueprint(AgentBlueprint blueprint) {
    this.blueprint = blueprint;
  }

  /**
   **/
  public AgentGenerationPreviewResponse dslPreview(AgentDslPreview dslPreview) {
    this.dslPreview = dslPreview;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("dslPreview")
  @Valid public AgentDslPreview getDslPreview() {
    return dslPreview;
  }

  @JsonProperty("dslPreview")
  public void setDslPreview(AgentDslPreview dslPreview) {
    this.dslPreview = dslPreview;
  }

  /**
   **/
  public AgentGenerationPreviewResponse validationPlan(AgentValidationPlan validationPlan) {
    this.validationPlan = validationPlan;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validationPlan")
  @Valid public AgentValidationPlan getValidationPlan() {
    return validationPlan;
  }

  @JsonProperty("validationPlan")
  public void setValidationPlan(AgentValidationPlan validationPlan) {
    this.validationPlan = validationPlan;
  }

  /**
   **/
  public AgentGenerationPreviewResponse warnings(List<String> warnings) {
    this.warnings = warnings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("warnings")
  public List<String> getWarnings() {
    return warnings;
  }

  @JsonProperty("warnings")
  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public AgentGenerationPreviewResponse addWarningsItem(String warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }

    this.warnings.add(warningsItem);
    return this;
  }

  public AgentGenerationPreviewResponse removeWarningsItem(String warningsItem) {
    if (warningsItem != null && this.warnings != null) {
      this.warnings.remove(warningsItem);
    }

    return this;
  }
  /**
   **/
  public AgentGenerationPreviewResponse rejectedReason(String rejectedReason) {
    this.rejectedReason = rejectedReason;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rejectedReason")
  public String getRejectedReason() {
    return rejectedReason;
  }

  @JsonProperty("rejectedReason")
  public void setRejectedReason(String rejectedReason) {
    this.rejectedReason = rejectedReason;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentGenerationPreviewResponse agentGenerationPreviewResponse = (AgentGenerationPreviewResponse) o;
    return Objects.equals(this.alert, agentGenerationPreviewResponse.alert) &&
        Objects.equals(this.canGenerate, agentGenerationPreviewResponse.canGenerate) &&
        Objects.equals(this.recommendedGenerationMode, agentGenerationPreviewResponse.recommendedGenerationMode) &&
        Objects.equals(this.estimatedComplexity, agentGenerationPreviewResponse.estimatedComplexity) &&
        Objects.equals(this.requiredSources, agentGenerationPreviewResponse.requiredSources) &&
        Objects.equals(this.requiredPermissions, agentGenerationPreviewResponse.requiredPermissions) &&
        Objects.equals(this.blueprint, agentGenerationPreviewResponse.blueprint) &&
        Objects.equals(this.dslPreview, agentGenerationPreviewResponse.dslPreview) &&
        Objects.equals(this.validationPlan, agentGenerationPreviewResponse.validationPlan) &&
        Objects.equals(this.warnings, agentGenerationPreviewResponse.warnings) &&
        Objects.equals(this.rejectedReason, agentGenerationPreviewResponse.rejectedReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alert, canGenerate, recommendedGenerationMode, estimatedComplexity, requiredSources, requiredPermissions, blueprint, dslPreview, validationPlan, warnings, rejectedReason);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentGenerationPreviewResponse {\n");
    
    sb.append("    alert: ").append(toIndentedString(alert)).append("\n");
    sb.append("    canGenerate: ").append(toIndentedString(canGenerate)).append("\n");
    sb.append("    recommendedGenerationMode: ").append(toIndentedString(recommendedGenerationMode)).append("\n");
    sb.append("    estimatedComplexity: ").append(toIndentedString(estimatedComplexity)).append("\n");
    sb.append("    requiredSources: ").append(toIndentedString(requiredSources)).append("\n");
    sb.append("    requiredPermissions: ").append(toIndentedString(requiredPermissions)).append("\n");
    sb.append("    blueprint: ").append(toIndentedString(blueprint)).append("\n");
    sb.append("    dslPreview: ").append(toIndentedString(dslPreview)).append("\n");
    sb.append("    validationPlan: ").append(toIndentedString(validationPlan)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
    sb.append("    rejectedReason: ").append(toIndentedString(rejectedReason)).append("\n");
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
