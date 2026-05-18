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



@JsonTypeName("AgentCompilationRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentCompilationRequest   {
  private Boolean force = false;
  private AgentGenerationMode generationMode;
  private Boolean runSimulation = true;
  private String note;

  public AgentCompilationRequest() {
  }

  /**
   **/
  public AgentCompilationRequest force(Boolean force) {
    this.force = force;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("force")
  public Boolean getForce() {
    return force;
  }

  @JsonProperty("force")
  public void setForce(Boolean force) {
    this.force = force;
  }

  /**
   **/
  public AgentCompilationRequest generationMode(AgentGenerationMode generationMode) {
    this.generationMode = generationMode;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generationMode")
  public AgentGenerationMode getGenerationMode() {
    return generationMode;
  }

  @JsonProperty("generationMode")
  public void setGenerationMode(AgentGenerationMode generationMode) {
    this.generationMode = generationMode;
  }

  /**
   **/
  public AgentCompilationRequest runSimulation(Boolean runSimulation) {
    this.runSimulation = runSimulation;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("runSimulation")
  public Boolean getRunSimulation() {
    return runSimulation;
  }

  @JsonProperty("runSimulation")
  public void setRunSimulation(Boolean runSimulation) {
    this.runSimulation = runSimulation;
  }

  /**
   **/
  public AgentCompilationRequest note(String note) {
    this.note = note;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("note")
   @Size(max=1000)public String getNote() {
    return note;
  }

  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentCompilationRequest agentCompilationRequest = (AgentCompilationRequest) o;
    return Objects.equals(this.force, agentCompilationRequest.force) &&
        Objects.equals(this.generationMode, agentCompilationRequest.generationMode) &&
        Objects.equals(this.runSimulation, agentCompilationRequest.runSimulation) &&
        Objects.equals(this.note, agentCompilationRequest.note);
  }

  @Override
  public int hashCode() {
    return Objects.hash(force, generationMode, runSimulation, note);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentCompilationRequest {\n");
    
    sb.append("    force: ").append(toIndentedString(force)).append("\n");
    sb.append("    generationMode: ").append(toIndentedString(generationMode)).append("\n");
    sb.append("    runSimulation: ").append(toIndentedString(runSimulation)).append("\n");
    sb.append("    note: ").append(toIndentedString(note)).append("\n");
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
