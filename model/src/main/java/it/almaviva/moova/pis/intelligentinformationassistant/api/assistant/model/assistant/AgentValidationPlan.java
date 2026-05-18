package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationExample;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AgentValidationPlan")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentValidationPlan   {
  private @Valid List<@Valid AgentValidationExample> positiveExamples = new ArrayList<>();
  private @Valid List<@Valid AgentValidationExample> negativeExamples = new ArrayList<>();
  private @Valid List<String> edgeCases = new ArrayList<>();

  public AgentValidationPlan() {
  }

  /**
   **/
  public AgentValidationPlan positiveExamples(List<@Valid AgentValidationExample> positiveExamples) {
    this.positiveExamples = positiveExamples;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("positiveExamples")
  @Valid public List<@Valid AgentValidationExample> getPositiveExamples() {
    return positiveExamples;
  }

  @JsonProperty("positiveExamples")
  public void setPositiveExamples(List<@Valid AgentValidationExample> positiveExamples) {
    this.positiveExamples = positiveExamples;
  }

  public AgentValidationPlan addPositiveExamplesItem(AgentValidationExample positiveExamplesItem) {
    if (this.positiveExamples == null) {
      this.positiveExamples = new ArrayList<>();
    }

    this.positiveExamples.add(positiveExamplesItem);
    return this;
  }

  public AgentValidationPlan removePositiveExamplesItem(AgentValidationExample positiveExamplesItem) {
    if (positiveExamplesItem != null && this.positiveExamples != null) {
      this.positiveExamples.remove(positiveExamplesItem);
    }

    return this;
  }
  /**
   **/
  public AgentValidationPlan negativeExamples(List<@Valid AgentValidationExample> negativeExamples) {
    this.negativeExamples = negativeExamples;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("negativeExamples")
  @Valid public List<@Valid AgentValidationExample> getNegativeExamples() {
    return negativeExamples;
  }

  @JsonProperty("negativeExamples")
  public void setNegativeExamples(List<@Valid AgentValidationExample> negativeExamples) {
    this.negativeExamples = negativeExamples;
  }

  public AgentValidationPlan addNegativeExamplesItem(AgentValidationExample negativeExamplesItem) {
    if (this.negativeExamples == null) {
      this.negativeExamples = new ArrayList<>();
    }

    this.negativeExamples.add(negativeExamplesItem);
    return this;
  }

  public AgentValidationPlan removeNegativeExamplesItem(AgentValidationExample negativeExamplesItem) {
    if (negativeExamplesItem != null && this.negativeExamples != null) {
      this.negativeExamples.remove(negativeExamplesItem);
    }

    return this;
  }
  /**
   **/
  public AgentValidationPlan edgeCases(List<String> edgeCases) {
    this.edgeCases = edgeCases;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("edgeCases")
  public List<String> getEdgeCases() {
    return edgeCases;
  }

  @JsonProperty("edgeCases")
  public void setEdgeCases(List<String> edgeCases) {
    this.edgeCases = edgeCases;
  }

  public AgentValidationPlan addEdgeCasesItem(String edgeCasesItem) {
    if (this.edgeCases == null) {
      this.edgeCases = new ArrayList<>();
    }

    this.edgeCases.add(edgeCasesItem);
    return this;
  }

  public AgentValidationPlan removeEdgeCasesItem(String edgeCasesItem) {
    if (edgeCasesItem != null && this.edgeCases != null) {
      this.edgeCases.remove(edgeCasesItem);
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
    AgentValidationPlan agentValidationPlan = (AgentValidationPlan) o;
    return Objects.equals(this.positiveExamples, agentValidationPlan.positiveExamples) &&
        Objects.equals(this.negativeExamples, agentValidationPlan.negativeExamples) &&
        Objects.equals(this.edgeCases, agentValidationPlan.edgeCases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(positiveExamples, negativeExamples, edgeCases);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentValidationPlan {\n");
    
    sb.append("    positiveExamples: ").append(toIndentedString(positiveExamples)).append("\n");
    sb.append("    negativeExamples: ").append(toIndentedString(negativeExamples)).append("\n");
    sb.append("    edgeCases: ").append(toIndentedString(edgeCases)).append("\n");
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
