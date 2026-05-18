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



@JsonTypeName("AgentFunctionalMetrics")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentFunctionalMetrics   {
  private Long processedEvents;
  private Long candidateOutputs;
  private Long validOutputs;
  private Long discardedOutputs;
  private Long deduplicatedOutputs;
  private Long createdSuggestions;
  private Long approvedSuggestions;
  private Long rejectedSuggestions;
  private Long modifiedSuggestions;
  private Double averageConfidence;
  private Double approvalRate;
  private Double rejectionRate;

  public AgentFunctionalMetrics() {
  }

  /**
   **/
  public AgentFunctionalMetrics processedEvents(Long processedEvents) {
    this.processedEvents = processedEvents;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("processedEvents")
  public Long getProcessedEvents() {
    return processedEvents;
  }

  @JsonProperty("processedEvents")
  public void setProcessedEvents(Long processedEvents) {
    this.processedEvents = processedEvents;
  }

  /**
   **/
  public AgentFunctionalMetrics candidateOutputs(Long candidateOutputs) {
    this.candidateOutputs = candidateOutputs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("candidateOutputs")
  public Long getCandidateOutputs() {
    return candidateOutputs;
  }

  @JsonProperty("candidateOutputs")
  public void setCandidateOutputs(Long candidateOutputs) {
    this.candidateOutputs = candidateOutputs;
  }

  /**
   **/
  public AgentFunctionalMetrics validOutputs(Long validOutputs) {
    this.validOutputs = validOutputs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validOutputs")
  public Long getValidOutputs() {
    return validOutputs;
  }

  @JsonProperty("validOutputs")
  public void setValidOutputs(Long validOutputs) {
    this.validOutputs = validOutputs;
  }

  /**
   **/
  public AgentFunctionalMetrics discardedOutputs(Long discardedOutputs) {
    this.discardedOutputs = discardedOutputs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("discardedOutputs")
  public Long getDiscardedOutputs() {
    return discardedOutputs;
  }

  @JsonProperty("discardedOutputs")
  public void setDiscardedOutputs(Long discardedOutputs) {
    this.discardedOutputs = discardedOutputs;
  }

  /**
   **/
  public AgentFunctionalMetrics deduplicatedOutputs(Long deduplicatedOutputs) {
    this.deduplicatedOutputs = deduplicatedOutputs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deduplicatedOutputs")
  public Long getDeduplicatedOutputs() {
    return deduplicatedOutputs;
  }

  @JsonProperty("deduplicatedOutputs")
  public void setDeduplicatedOutputs(Long deduplicatedOutputs) {
    this.deduplicatedOutputs = deduplicatedOutputs;
  }

  /**
   **/
  public AgentFunctionalMetrics createdSuggestions(Long createdSuggestions) {
    this.createdSuggestions = createdSuggestions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdSuggestions")
  public Long getCreatedSuggestions() {
    return createdSuggestions;
  }

  @JsonProperty("createdSuggestions")
  public void setCreatedSuggestions(Long createdSuggestions) {
    this.createdSuggestions = createdSuggestions;
  }

  /**
   **/
  public AgentFunctionalMetrics approvedSuggestions(Long approvedSuggestions) {
    this.approvedSuggestions = approvedSuggestions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("approvedSuggestions")
  public Long getApprovedSuggestions() {
    return approvedSuggestions;
  }

  @JsonProperty("approvedSuggestions")
  public void setApprovedSuggestions(Long approvedSuggestions) {
    this.approvedSuggestions = approvedSuggestions;
  }

  /**
   **/
  public AgentFunctionalMetrics rejectedSuggestions(Long rejectedSuggestions) {
    this.rejectedSuggestions = rejectedSuggestions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rejectedSuggestions")
  public Long getRejectedSuggestions() {
    return rejectedSuggestions;
  }

  @JsonProperty("rejectedSuggestions")
  public void setRejectedSuggestions(Long rejectedSuggestions) {
    this.rejectedSuggestions = rejectedSuggestions;
  }

  /**
   **/
  public AgentFunctionalMetrics modifiedSuggestions(Long modifiedSuggestions) {
    this.modifiedSuggestions = modifiedSuggestions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("modifiedSuggestions")
  public Long getModifiedSuggestions() {
    return modifiedSuggestions;
  }

  @JsonProperty("modifiedSuggestions")
  public void setModifiedSuggestions(Long modifiedSuggestions) {
    this.modifiedSuggestions = modifiedSuggestions;
  }

  /**
   * minimum: 0
   * maximum: 1
   **/
  public AgentFunctionalMetrics averageConfidence(Double averageConfidence) {
    this.averageConfidence = averageConfidence;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("averageConfidence")
   @DecimalMin("0") @DecimalMax("1")public Double getAverageConfidence() {
    return averageConfidence;
  }

  @JsonProperty("averageConfidence")
  public void setAverageConfidence(Double averageConfidence) {
    this.averageConfidence = averageConfidence;
  }

  /**
   **/
  public AgentFunctionalMetrics approvalRate(Double approvalRate) {
    this.approvalRate = approvalRate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("approvalRate")
  public Double getApprovalRate() {
    return approvalRate;
  }

  @JsonProperty("approvalRate")
  public void setApprovalRate(Double approvalRate) {
    this.approvalRate = approvalRate;
  }

  /**
   **/
  public AgentFunctionalMetrics rejectionRate(Double rejectionRate) {
    this.rejectionRate = rejectionRate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rejectionRate")
  public Double getRejectionRate() {
    return rejectionRate;
  }

  @JsonProperty("rejectionRate")
  public void setRejectionRate(Double rejectionRate) {
    this.rejectionRate = rejectionRate;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentFunctionalMetrics agentFunctionalMetrics = (AgentFunctionalMetrics) o;
    return Objects.equals(this.processedEvents, agentFunctionalMetrics.processedEvents) &&
        Objects.equals(this.candidateOutputs, agentFunctionalMetrics.candidateOutputs) &&
        Objects.equals(this.validOutputs, agentFunctionalMetrics.validOutputs) &&
        Objects.equals(this.discardedOutputs, agentFunctionalMetrics.discardedOutputs) &&
        Objects.equals(this.deduplicatedOutputs, agentFunctionalMetrics.deduplicatedOutputs) &&
        Objects.equals(this.createdSuggestions, agentFunctionalMetrics.createdSuggestions) &&
        Objects.equals(this.approvedSuggestions, agentFunctionalMetrics.approvedSuggestions) &&
        Objects.equals(this.rejectedSuggestions, agentFunctionalMetrics.rejectedSuggestions) &&
        Objects.equals(this.modifiedSuggestions, agentFunctionalMetrics.modifiedSuggestions) &&
        Objects.equals(this.averageConfidence, agentFunctionalMetrics.averageConfidence) &&
        Objects.equals(this.approvalRate, agentFunctionalMetrics.approvalRate) &&
        Objects.equals(this.rejectionRate, agentFunctionalMetrics.rejectionRate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(processedEvents, candidateOutputs, validOutputs, discardedOutputs, deduplicatedOutputs, createdSuggestions, approvedSuggestions, rejectedSuggestions, modifiedSuggestions, averageConfidence, approvalRate, rejectionRate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentFunctionalMetrics {\n");
    
    sb.append("    processedEvents: ").append(toIndentedString(processedEvents)).append("\n");
    sb.append("    candidateOutputs: ").append(toIndentedString(candidateOutputs)).append("\n");
    sb.append("    validOutputs: ").append(toIndentedString(validOutputs)).append("\n");
    sb.append("    discardedOutputs: ").append(toIndentedString(discardedOutputs)).append("\n");
    sb.append("    deduplicatedOutputs: ").append(toIndentedString(deduplicatedOutputs)).append("\n");
    sb.append("    createdSuggestions: ").append(toIndentedString(createdSuggestions)).append("\n");
    sb.append("    approvedSuggestions: ").append(toIndentedString(approvedSuggestions)).append("\n");
    sb.append("    rejectedSuggestions: ").append(toIndentedString(rejectedSuggestions)).append("\n");
    sb.append("    modifiedSuggestions: ").append(toIndentedString(modifiedSuggestions)).append("\n");
    sb.append("    averageConfidence: ").append(toIndentedString(averageConfidence)).append("\n");
    sb.append("    approvalRate: ").append(toIndentedString(approvalRate)).append("\n");
    sb.append("    rejectionRate: ").append(toIndentedString(rejectionRate)).append("\n");
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
