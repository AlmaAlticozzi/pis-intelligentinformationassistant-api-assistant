package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("AlertTechnicalSpecificationResponse")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-08T11:10:16.490174964Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertTechnicalSpecificationResponse   {
  private AlertReference alert;
  private AlertStatus status;
  private AlertVerificationStatus verificationStatus;
  private AlertInterpreterType interpreterType;
  private String inputModel;
  private String outputModel;
  private Boolean technicalSpecificationEdited;
  private @Valid Map<String, Object> technicalSpecification = new HashMap<>();
  private Boolean agentBlueprintPreviewRegenerated;
  private @Valid List<String> warnings = new ArrayList<>();

  public AlertTechnicalSpecificationResponse() {
  }

  @JsonCreator
  public AlertTechnicalSpecificationResponse(
    @JsonProperty(required = true, value = "alert") AlertReference alert,
    @JsonProperty(required = true, value = "status") AlertStatus status,
    @JsonProperty(required = true, value = "verificationStatus") AlertVerificationStatus verificationStatus,
    @JsonProperty(required = true, value = "technicalSpecificationEdited") Boolean technicalSpecificationEdited,
    @JsonProperty(required = true, value = "technicalSpecification") Map<String, Object> technicalSpecification
  ) {
    this.alert = alert;
    this.status = status;
    this.verificationStatus = verificationStatus;
    this.technicalSpecificationEdited = technicalSpecificationEdited;
    this.technicalSpecification = technicalSpecification;
  }

  /**
   **/
  public AlertTechnicalSpecificationResponse alert(AlertReference alert) {
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
  public AlertTechnicalSpecificationResponse status(AlertStatus status) {
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
  public AlertTechnicalSpecificationResponse verificationStatus(AlertVerificationStatus verificationStatus) {
    this.verificationStatus = verificationStatus;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "verificationStatus")
  @NotNull public AlertVerificationStatus getVerificationStatus() {
    return verificationStatus;
  }

  @JsonProperty(required = true, value = "verificationStatus")
  public void setVerificationStatus(AlertVerificationStatus verificationStatus) {
    this.verificationStatus = verificationStatus;
  }

  /**
   **/
  public AlertTechnicalSpecificationResponse interpreterType(AlertInterpreterType interpreterType) {
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
   * Input model declared by the validated technical specification.
   **/
  public AlertTechnicalSpecificationResponse inputModel(String inputModel) {
    this.inputModel = inputModel;
    return this;
  }

  
  @ApiModelProperty(example = "ServiceDataStopPointJourneysV2", value = "Input model declared by the validated technical specification.")
  @JsonProperty("inputModel")
  public String getInputModel() {
    return inputModel;
  }

  @JsonProperty("inputModel")
  public void setInputModel(String inputModel) {
    this.inputModel = inputModel;
  }

  /**
   * Output model declared by the validated technical specification.
   **/
  public AlertTechnicalSpecificationResponse outputModel(String outputModel) {
    this.outputModel = outputModel;
    return this;
  }

  
  @ApiModelProperty(example = "AgentOutput.CANDIDATE_SUGGESTION", value = "Output model declared by the validated technical specification.")
  @JsonProperty("outputModel")
  public String getOutputModel() {
    return outputModel;
  }

  @JsonProperty("outputModel")
  public void setOutputModel(String outputModel) {
    this.outputModel = outputModel;
  }

  /**
   * Indicates whether the current technical specification was manually replaced after the last successful AI-assisted verification.
   **/
  public AlertTechnicalSpecificationResponse technicalSpecificationEdited(Boolean technicalSpecificationEdited) {
    this.technicalSpecificationEdited = technicalSpecificationEdited;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "Indicates whether the current technical specification was manually replaced after the last successful AI-assisted verification.")
  @JsonProperty(required = true, value = "technicalSpecificationEdited")
  @NotNull public Boolean getTechnicalSpecificationEdited() {
    return technicalSpecificationEdited;
  }

  @JsonProperty(required = true, value = "technicalSpecificationEdited")
  public void setTechnicalSpecificationEdited(Boolean technicalSpecificationEdited) {
    this.technicalSpecificationEdited = technicalSpecificationEdited;
  }

  /**
   * Backend-validated, non-executable technical specification of an Alert.  The object represents the controlled interpretation of the Alert prompt and is the source of truth for future Agent generation flows. Its concrete structure depends on the interpreter type and evaluation mode.  For &#x60;EVENT_INTERPRETER&#x60;, the specification describes a stateless condition over realtime &#x60;ServiceDataV2&#x60; event messages.  For &#x60;SCHEDULED_INTERPRETER&#x60;, the specification describes a scheduled snapshot evaluation over Service Data API results, including schedule, service data query, output policy and snapshot evaluation rules.  The API intentionally models this object as an extensible JSON artifact because its exact shape is governed by backend validators and capability catalogs rather than by arbitrary client-side editing. Clients may display and submit the JSON, but the backend remains the authority that validates whether the specification is acceptable.  This object must never contain executable code, scripts, SQL statements or ungoverned instructions. 
   **/
  public AlertTechnicalSpecificationResponse technicalSpecification(Map<String, Object> technicalSpecification) {
    this.technicalSpecification = technicalSpecification;
    return this;
  }

  
  @ApiModelProperty(example = "{\"source\":\"SERVICE_DATA\",\"schemaVersion\":\"iia.alert.technical-specification/v2\",\"interpreterType\":\"SCHEDULED_INTERPRETER\",\"triggerType\":\"SCHEDULE\",\"accessMode\":\"SERVICE_DATA_API_SNAPSHOT\",\"inputModel\":\"ServiceDataStopPointJourneysV2\",\"outputModel\":\"AgentOutput.CANDIDATE_SUGGESTION\",\"evaluationMode\":\"SCHEDULED_SNAPSHOT_MATCH\",\"schedule\":{\"frequencySeconds\":600,\"defaulted\":true},\"serviceDataQuery\":{\"operation\":\"POST /v2/stoppointjourneys\",\"stopPoints\":[\"TNPNTS00000000000009\"],\"monitoringScope\":\"EXPLICIT_STOP_POINTS\",\"requiresAllKnownStopPoints\":false,\"timeWindow\":{\"startMode\":\"NOW_TRUNCATED_TO_MINUTE\",\"endMode\":\"NOW_PLUS_DEFAULT_LOOKAHEAD\",\"lookaheadMinutes\":480,\"defaulted\":true}},\"outputPolicy\":{\"emit\":\"EVERY_RUN\",\"includeCount\":true,\"includeMatchingJourneys\":false},\"snapshotEvaluation\":{\"mode\":\"REPORT_COUNT\",\"journeyPath\":\"stopPointsJourneyDetails[]\",\"condition\":{\"type\":\"SERVICE_DATA_SCHEDULED_FIELD_MATCH\",\"anyElement\":{\"path\":\"stopPointsJourneyDetails[]\",\"conditions\":{\"all\":[{\"field\":\"departureDelay.delay\",\"operator\":\"GREATER_THAN\",\"value\":0}]}}}}}", required = true, value = "Backend-validated, non-executable technical specification of an Alert.  The object represents the controlled interpretation of the Alert prompt and is the source of truth for future Agent generation flows. Its concrete structure depends on the interpreter type and evaluation mode.  For `EVENT_INTERPRETER`, the specification describes a stateless condition over realtime `ServiceDataV2` event messages.  For `SCHEDULED_INTERPRETER`, the specification describes a scheduled snapshot evaluation over Service Data API results, including schedule, service data query, output policy and snapshot evaluation rules.  The API intentionally models this object as an extensible JSON artifact because its exact shape is governed by backend validators and capability catalogs rather than by arbitrary client-side editing. Clients may display and submit the JSON, but the backend remains the authority that validates whether the specification is acceptable.  This object must never contain executable code, scripts, SQL statements or ungoverned instructions. ")
  @JsonProperty(required = true, value = "technicalSpecification")
  @NotNull public Map<String, Object> getTechnicalSpecification() {
    return technicalSpecification;
  }

  @JsonProperty(required = true, value = "technicalSpecification")
  public void setTechnicalSpecification(Map<String, Object> technicalSpecification) {
    this.technicalSpecification = technicalSpecification;
  }

  public AlertTechnicalSpecificationResponse putTechnicalSpecificationItem(String key, Object technicalSpecificationItem) {
    if (this.technicalSpecification == null) {
      this.technicalSpecification = new HashMap<>();
    }

    this.technicalSpecification.put(key, technicalSpecificationItem);
    return this;
  }

  public AlertTechnicalSpecificationResponse removeTechnicalSpecificationItem(String key) {
    if (this.technicalSpecification != null) {
      this.technicalSpecification.remove(key);
    }

    return this;
  }
  /**
   * Indicates whether the backend regenerated, refreshed or realigned the Agent Blueprint preview from the current technical specification during the last replacement operation.  For read-only GET operations this field can be omitted or set according to the latest known persisted state. 
   **/
  public AlertTechnicalSpecificationResponse agentBlueprintPreviewRegenerated(Boolean agentBlueprintPreviewRegenerated) {
    this.agentBlueprintPreviewRegenerated = agentBlueprintPreviewRegenerated;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Indicates whether the backend regenerated, refreshed or realigned the Agent Blueprint preview from the current technical specification during the last replacement operation.  For read-only GET operations this field can be omitted or set according to the latest known persisted state. ")
  @JsonProperty("agentBlueprintPreviewRegenerated")
  public Boolean getAgentBlueprintPreviewRegenerated() {
    return agentBlueprintPreviewRegenerated;
  }

  @JsonProperty("agentBlueprintPreviewRegenerated")
  public void setAgentBlueprintPreviewRegenerated(Boolean agentBlueprintPreviewRegenerated) {
    this.agentBlueprintPreviewRegenerated = agentBlueprintPreviewRegenerated;
  }

  /**
   * Non-blocking warnings produced while reading or replacing the technical specification.
   **/
  public AlertTechnicalSpecificationResponse warnings(List<String> warnings) {
    this.warnings = warnings;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Alert was disabled after manual technical specification replacement and must be explicitly enabled again.\"]", value = "Non-blocking warnings produced while reading or replacing the technical specification.")
  @JsonProperty("warnings")
  public List<String> getWarnings() {
    return warnings;
  }

  @JsonProperty("warnings")
  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public AlertTechnicalSpecificationResponse addWarningsItem(String warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }

    this.warnings.add(warningsItem);
    return this;
  }

  public AlertTechnicalSpecificationResponse removeWarningsItem(String warningsItem) {
    if (warningsItem != null && this.warnings != null) {
      this.warnings.remove(warningsItem);
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
    AlertTechnicalSpecificationResponse alertTechnicalSpecificationResponse = (AlertTechnicalSpecificationResponse) o;
    return Objects.equals(this.alert, alertTechnicalSpecificationResponse.alert) &&
        Objects.equals(this.status, alertTechnicalSpecificationResponse.status) &&
        Objects.equals(this.verificationStatus, alertTechnicalSpecificationResponse.verificationStatus) &&
        Objects.equals(this.interpreterType, alertTechnicalSpecificationResponse.interpreterType) &&
        Objects.equals(this.inputModel, alertTechnicalSpecificationResponse.inputModel) &&
        Objects.equals(this.outputModel, alertTechnicalSpecificationResponse.outputModel) &&
        Objects.equals(this.technicalSpecificationEdited, alertTechnicalSpecificationResponse.technicalSpecificationEdited) &&
        Objects.equals(this.technicalSpecification, alertTechnicalSpecificationResponse.technicalSpecification) &&
        Objects.equals(this.agentBlueprintPreviewRegenerated, alertTechnicalSpecificationResponse.agentBlueprintPreviewRegenerated) &&
        Objects.equals(this.warnings, alertTechnicalSpecificationResponse.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alert, status, verificationStatus, interpreterType, inputModel, outputModel, technicalSpecificationEdited, technicalSpecification, agentBlueprintPreviewRegenerated, warnings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertTechnicalSpecificationResponse {\n");
    
    sb.append("    alert: ").append(toIndentedString(alert)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    verificationStatus: ").append(toIndentedString(verificationStatus)).append("\n");
    sb.append("    interpreterType: ").append(toIndentedString(interpreterType)).append("\n");
    sb.append("    inputModel: ").append(toIndentedString(inputModel)).append("\n");
    sb.append("    outputModel: ").append(toIndentedString(outputModel)).append("\n");
    sb.append("    technicalSpecificationEdited: ").append(toIndentedString(technicalSpecificationEdited)).append("\n");
    sb.append("    technicalSpecification: ").append(toIndentedString(technicalSpecification)).append("\n");
    sb.append("    agentBlueprintPreviewRegenerated: ").append(toIndentedString(agentBlueprintPreviewRegenerated)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
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
