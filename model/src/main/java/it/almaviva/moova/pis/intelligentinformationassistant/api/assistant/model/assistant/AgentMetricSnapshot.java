package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentResourceUsage;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentMetricSnapshot")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentMetricSnapshot   {
  private String agentRunId;
  private OffsetDateTime sampledAt;
  private AgentResourceUsage resources;
  private Long processedEvents;
  private Long generatedOutputs;
  private Long invalidOutputs;
  private Long errorsCount;

  public AgentMetricSnapshot() {
  }

  /**
   **/
  public AgentMetricSnapshot agentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("agentRunId")
  public String getAgentRunId() {
    return agentRunId;
  }

  @JsonProperty("agentRunId")
  public void setAgentRunId(String agentRunId) {
    this.agentRunId = agentRunId;
  }

  /**
   **/
  public AgentMetricSnapshot sampledAt(OffsetDateTime sampledAt) {
    this.sampledAt = sampledAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sampledAt")
  public OffsetDateTime getSampledAt() {
    return sampledAt;
  }

  @JsonProperty("sampledAt")
  public void setSampledAt(OffsetDateTime sampledAt) {
    this.sampledAt = sampledAt;
  }

  /**
   **/
  public AgentMetricSnapshot resources(AgentResourceUsage resources) {
    this.resources = resources;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("resources")
  @Valid public AgentResourceUsage getResources() {
    return resources;
  }

  @JsonProperty("resources")
  public void setResources(AgentResourceUsage resources) {
    this.resources = resources;
  }

  /**
   **/
  public AgentMetricSnapshot processedEvents(Long processedEvents) {
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
  public AgentMetricSnapshot generatedOutputs(Long generatedOutputs) {
    this.generatedOutputs = generatedOutputs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generatedOutputs")
  public Long getGeneratedOutputs() {
    return generatedOutputs;
  }

  @JsonProperty("generatedOutputs")
  public void setGeneratedOutputs(Long generatedOutputs) {
    this.generatedOutputs = generatedOutputs;
  }

  /**
   **/
  public AgentMetricSnapshot invalidOutputs(Long invalidOutputs) {
    this.invalidOutputs = invalidOutputs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("invalidOutputs")
  public Long getInvalidOutputs() {
    return invalidOutputs;
  }

  @JsonProperty("invalidOutputs")
  public void setInvalidOutputs(Long invalidOutputs) {
    this.invalidOutputs = invalidOutputs;
  }

  /**
   **/
  public AgentMetricSnapshot errorsCount(Long errorsCount) {
    this.errorsCount = errorsCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("errorsCount")
  public Long getErrorsCount() {
    return errorsCount;
  }

  @JsonProperty("errorsCount")
  public void setErrorsCount(Long errorsCount) {
    this.errorsCount = errorsCount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentMetricSnapshot agentMetricSnapshot = (AgentMetricSnapshot) o;
    return Objects.equals(this.agentRunId, agentMetricSnapshot.agentRunId) &&
        Objects.equals(this.sampledAt, agentMetricSnapshot.sampledAt) &&
        Objects.equals(this.resources, agentMetricSnapshot.resources) &&
        Objects.equals(this.processedEvents, agentMetricSnapshot.processedEvents) &&
        Objects.equals(this.generatedOutputs, agentMetricSnapshot.generatedOutputs) &&
        Objects.equals(this.invalidOutputs, agentMetricSnapshot.invalidOutputs) &&
        Objects.equals(this.errorsCount, agentMetricSnapshot.errorsCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(agentRunId, sampledAt, resources, processedEvents, generatedOutputs, invalidOutputs, errorsCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentMetricSnapshot {\n");
    
    sb.append("    agentRunId: ").append(toIndentedString(agentRunId)).append("\n");
    sb.append("    sampledAt: ").append(toIndentedString(sampledAt)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
    sb.append("    processedEvents: ").append(toIndentedString(processedEvents)).append("\n");
    sb.append("    generatedOutputs: ").append(toIndentedString(generatedOutputs)).append("\n");
    sb.append("    invalidOutputs: ").append(toIndentedString(invalidOutputs)).append("\n");
    sb.append("    errorsCount: ").append(toIndentedString(errorsCount)).append("\n");
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
