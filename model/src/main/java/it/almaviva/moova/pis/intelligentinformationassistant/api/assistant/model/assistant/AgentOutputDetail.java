package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentOutputStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionSeverity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTarget;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
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



@JsonTypeName("AgentOutputDetail")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentOutputDetail   {
  private String id;
  private String agentRunId;
  private String agentDefinitionId;
  private AlertReference alert;
  private Integer alertVersion;
  private AgentOutputStatus status;
  public enum OutputTypeEnum {

    CANDIDATE_SUGGESTION(String.valueOf("CANDIDATE_SUGGESTION")), RUNTIME_EVENT(String.valueOf("RUNTIME_EVENT")), NO_OUTPUT(String.valueOf("NO_OUTPUT")), ERROR(String.valueOf("ERROR"));


    private String value;

    OutputTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static OutputTypeEnum fromString(String s) {
        for (OutputTypeEnum b : OutputTypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static OutputTypeEnum fromValue(String value) {
        for (OutputTypeEnum b : OutputTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private OutputTypeEnum outputType;
  private SuggestionTarget target;
  private SuggestionSeverity severity;
  private Double confidence;
  private String reason;
  private String operatorAdvice;
  private String passengerMessageProposal;
  private String deduplicationKey;
  private String validationErrorCode;
  private String validationErrorMessage;
  private String suggestionId;
  private @Valid Map<String, Object> rawOutput = new HashMap<>();
  private OffsetDateTime generatedAt;
  private OffsetDateTime processedAt;

  public AgentOutputDetail() {
  }

  @JsonCreator
  public AgentOutputDetail(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "agentRunId") String agentRunId,
    @JsonProperty(required = true, value = "agentDefinitionId") String agentDefinitionId,
    @JsonProperty(required = true, value = "status") AgentOutputStatus status,
    @JsonProperty(required = true, value = "outputType") OutputTypeEnum outputType,
    @JsonProperty(required = true, value = "generatedAt") OffsetDateTime generatedAt
  ) {
    this.id = id;
    this.agentRunId = agentRunId;
    this.agentDefinitionId = agentDefinitionId;
    this.status = status;
    this.outputType = outputType;
    this.generatedAt = generatedAt;
  }

  /**
   **/
  public AgentOutputDetail id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "AGOU2026251400000001", required = true, value = "")
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
  public AgentOutputDetail agentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentRunId")
  @NotNull  @Size(max=50)public String getAgentRunId() {
    return agentRunId;
  }

  @JsonProperty(required = true, value = "agentRunId")
  public void setAgentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
  }

  /**
   **/
  public AgentOutputDetail agentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "agentDefinitionId")
  @NotNull  @Size(max=50)public String getAgentDefinitionId() {
    return agentDefinitionId;
  }

  @JsonProperty(required = true, value = "agentDefinitionId")
  public void setAgentDefinitionId(String agentDefinitionId) {
    this.agentDefinitionId = agentDefinitionId;
  }

  /**
   **/
  public AgentOutputDetail alert(AlertReference alert) {
    this.alert = alert;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("alert")
  @Valid public AlertReference getAlert() {
    return alert;
  }

  @JsonProperty("alert")
  public void setAlert(AlertReference alert) {
    this.alert = alert;
  }

  /**
   **/
  public AgentOutputDetail alertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("alertVersion")
  public Integer getAlertVersion() {
    return alertVersion;
  }

  @JsonProperty("alertVersion")
  public void setAlertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
  }

  /**
   **/
  public AgentOutputDetail status(AgentOutputStatus status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public AgentOutputStatus getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(AgentOutputStatus status) {
    this.status = status;
  }

  /**
   **/
  public AgentOutputDetail outputType(OutputTypeEnum outputType) {
    this.outputType = outputType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "outputType")
  @NotNull public OutputTypeEnum getOutputType() {
    return outputType;
  }

  @JsonProperty(required = true, value = "outputType")
  public void setOutputType(OutputTypeEnum outputType) {
    this.outputType = outputType;
  }

  /**
   **/
  public AgentOutputDetail target(SuggestionTarget target) {
    this.target = target;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("target")
  @Valid public SuggestionTarget getTarget() {
    return target;
  }

  @JsonProperty("target")
  public void setTarget(SuggestionTarget target) {
    this.target = target;
  }

  /**
   **/
  public AgentOutputDetail severity(SuggestionSeverity severity) {
    this.severity = severity;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("severity")
  public SuggestionSeverity getSeverity() {
    return severity;
  }

  @JsonProperty("severity")
  public void setSeverity(SuggestionSeverity severity) {
    this.severity = severity;
  }

  /**
   * minimum: 0
   * maximum: 1
   **/
  public AgentOutputDetail confidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public AgentOutputDetail reason(String reason) {
    this.reason = reason;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("reason")
  public String getReason() {
    return reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   **/
  public AgentOutputDetail operatorAdvice(String operatorAdvice) {
    this.operatorAdvice = operatorAdvice;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("operatorAdvice")
  public String getOperatorAdvice() {
    return operatorAdvice;
  }

  @JsonProperty("operatorAdvice")
  public void setOperatorAdvice(String operatorAdvice) {
    this.operatorAdvice = operatorAdvice;
  }

  /**
   **/
  public AgentOutputDetail passengerMessageProposal(String passengerMessageProposal) {
    this.passengerMessageProposal = passengerMessageProposal;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("passengerMessageProposal")
  public String getPassengerMessageProposal() {
    return passengerMessageProposal;
  }

  @JsonProperty("passengerMessageProposal")
  public void setPassengerMessageProposal(String passengerMessageProposal) {
    this.passengerMessageProposal = passengerMessageProposal;
  }

  /**
   **/
  public AgentOutputDetail deduplicationKey(String deduplicationKey) {
    this.deduplicationKey = deduplicationKey;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deduplicationKey")
  public String getDeduplicationKey() {
    return deduplicationKey;
  }

  @JsonProperty("deduplicationKey")
  public void setDeduplicationKey(String deduplicationKey) {
    this.deduplicationKey = deduplicationKey;
  }

  /**
   **/
  public AgentOutputDetail validationErrorCode(String validationErrorCode) {
    this.validationErrorCode = validationErrorCode;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validationErrorCode")
  public String getValidationErrorCode() {
    return validationErrorCode;
  }

  @JsonProperty("validationErrorCode")
  public void setValidationErrorCode(String validationErrorCode) {
    this.validationErrorCode = validationErrorCode;
  }

  /**
   **/
  public AgentOutputDetail validationErrorMessage(String validationErrorMessage) {
    this.validationErrorMessage = validationErrorMessage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validationErrorMessage")
  public String getValidationErrorMessage() {
    return validationErrorMessage;
  }

  @JsonProperty("validationErrorMessage")
  public void setValidationErrorMessage(String validationErrorMessage) {
    this.validationErrorMessage = validationErrorMessage;
  }

  /**
   **/
  public AgentOutputDetail suggestionId(String suggestionId) {
    this.suggestionId = suggestionId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("suggestionId")
   @Size(max=50)public String getSuggestionId() {
    return suggestionId;
  }

  @JsonProperty("suggestionId")
  public void setSuggestionId(String suggestionId) {
    this.suggestionId = suggestionId;
  }

  /**
   **/
  public AgentOutputDetail rawOutput(Map<String, Object> rawOutput) {
    this.rawOutput = rawOutput;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rawOutput")
  public Map<String, Object> getRawOutput() {
    return rawOutput;
  }

  @JsonProperty("rawOutput")
  public void setRawOutput(Map<String, Object> rawOutput) {
    this.rawOutput = rawOutput;
  }

  public AgentOutputDetail putRawOutputItem(String key, Object rawOutputItem) {
    if (this.rawOutput == null) {
      this.rawOutput = new HashMap<>();
    }

    this.rawOutput.put(key, rawOutputItem);
    return this;
  }

  public AgentOutputDetail removeRawOutputItem(String key) {
    if (this.rawOutput != null) {
      this.rawOutput.remove(key);
    }

    return this;
  }
  /**
   **/
  public AgentOutputDetail generatedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "generatedAt")
  @NotNull public OffsetDateTime getGeneratedAt() {
    return generatedAt;
  }

  @JsonProperty(required = true, value = "generatedAt")
  public void setGeneratedAt(OffsetDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  /**
   **/
  public AgentOutputDetail processedAt(OffsetDateTime processedAt) {
    this.processedAt = processedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("processedAt")
  public OffsetDateTime getProcessedAt() {
    return processedAt;
  }

  @JsonProperty("processedAt")
  public void setProcessedAt(OffsetDateTime processedAt) {
    this.processedAt = processedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentOutputDetail agentOutputDetail = (AgentOutputDetail) o;
    return Objects.equals(this.id, agentOutputDetail.id) &&
        Objects.equals(this.agentRunId, agentOutputDetail.agentRunId) &&
        Objects.equals(this.agentDefinitionId, agentOutputDetail.agentDefinitionId) &&
        Objects.equals(this.alert, agentOutputDetail.alert) &&
        Objects.equals(this.alertVersion, agentOutputDetail.alertVersion) &&
        Objects.equals(this.status, agentOutputDetail.status) &&
        Objects.equals(this.outputType, agentOutputDetail.outputType) &&
        Objects.equals(this.target, agentOutputDetail.target) &&
        Objects.equals(this.severity, agentOutputDetail.severity) &&
        Objects.equals(this.confidence, agentOutputDetail.confidence) &&
        Objects.equals(this.reason, agentOutputDetail.reason) &&
        Objects.equals(this.operatorAdvice, agentOutputDetail.operatorAdvice) &&
        Objects.equals(this.passengerMessageProposal, agentOutputDetail.passengerMessageProposal) &&
        Objects.equals(this.deduplicationKey, agentOutputDetail.deduplicationKey) &&
        Objects.equals(this.validationErrorCode, agentOutputDetail.validationErrorCode) &&
        Objects.equals(this.validationErrorMessage, agentOutputDetail.validationErrorMessage) &&
        Objects.equals(this.suggestionId, agentOutputDetail.suggestionId) &&
        Objects.equals(this.rawOutput, agentOutputDetail.rawOutput) &&
        Objects.equals(this.generatedAt, agentOutputDetail.generatedAt) &&
        Objects.equals(this.processedAt, agentOutputDetail.processedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, agentRunId, agentDefinitionId, alert, alertVersion, status, outputType, target, severity, confidence, reason, operatorAdvice, passengerMessageProposal, deduplicationKey, validationErrorCode, validationErrorMessage, suggestionId, rawOutput, generatedAt, processedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentOutputDetail {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    agentRunId: ").append(toIndentedString(agentRunId)).append("\n");
    sb.append("    agentDefinitionId: ").append(toIndentedString(agentDefinitionId)).append("\n");
    sb.append("    alert: ").append(toIndentedString(alert)).append("\n");
    sb.append("    alertVersion: ").append(toIndentedString(alertVersion)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    outputType: ").append(toIndentedString(outputType)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    operatorAdvice: ").append(toIndentedString(operatorAdvice)).append("\n");
    sb.append("    passengerMessageProposal: ").append(toIndentedString(passengerMessageProposal)).append("\n");
    sb.append("    deduplicationKey: ").append(toIndentedString(deduplicationKey)).append("\n");
    sb.append("    validationErrorCode: ").append(toIndentedString(validationErrorCode)).append("\n");
    sb.append("    validationErrorMessage: ").append(toIndentedString(validationErrorMessage)).append("\n");
    sb.append("    suggestionId: ").append(toIndentedString(suggestionId)).append("\n");
    sb.append("    rawOutput: ").append(toIndentedString(rawOutput)).append("\n");
    sb.append("    generatedAt: ").append(toIndentedString(generatedAt)).append("\n");
    sb.append("    processedAt: ").append(toIndentedString(processedAt)).append("\n");
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
