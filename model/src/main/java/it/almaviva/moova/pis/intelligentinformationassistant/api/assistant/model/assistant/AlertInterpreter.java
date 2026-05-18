package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Metadata about the controlled interpreter produced by alert verification.
 **/
@ApiModel(description = "Metadata about the controlled interpreter produced by alert verification.")
@JsonTypeName("AlertInterpreter")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertInterpreter   {
  private AlertInterpreterType interpreterType;
  private String className;
  private String contractVersion;
  private String codeRef;
  private String implementationSummary;
  private String inputModel;
  private String outputModel;

  public AlertInterpreter() {
  }

  /**
   **/
  public AlertInterpreter interpreterType(AlertInterpreterType interpreterType) {
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
   **/
  public AlertInterpreter className(String className) {
    this.className = className;
    return this;
  }

  
  @ApiModelProperty(example = "GeneratedCancelledJourneyWithoutAnnouncementInterpreter", value = "")
  @JsonProperty("className")
  public String getClassName() {
    return className;
  }

  @JsonProperty("className")
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   **/
  public AlertInterpreter contractVersion(String contractVersion) {
    this.contractVersion = contractVersion;
    return this;
  }

  
  @ApiModelProperty(example = "0.0.1", value = "")
  @JsonProperty("contractVersion")
  public String getContractVersion() {
    return contractVersion;
  }

  @JsonProperty("contractVersion")
  public void setContractVersion(String contractVersion) {
    this.contractVersion = contractVersion;
  }

  /**
   * Internal read-only reference to the stored interpreter artifact. The API does not expose arbitrary executable code.
   **/
  public AlertInterpreter codeRef(String codeRef) {
    this.codeRef = codeRef;
    return this;
  }

  
  @ApiModelProperty(example = "iia-alert-artifact://f053d8f8", value = "Internal read-only reference to the stored interpreter artifact. The API does not expose arbitrary executable code.")
  @JsonProperty("codeRef")
  public String getCodeRef() {
    return codeRef;
  }

  @JsonProperty("codeRef")
  public void setCodeRef(String codeRef) {
    this.codeRef = codeRef;
  }

  /**
   **/
  public AlertInterpreter implementationSummary(String implementationSummary) {
    this.implementationSummary = implementationSummary;
    return this;
  }

  
  @ApiModelProperty(example = "Evaluates cancellation events and checks whether related audio announcements have been broadcast.", value = "")
  @JsonProperty("implementationSummary")
  public String getImplementationSummary() {
    return implementationSummary;
  }

  @JsonProperty("implementationSummary")
  public void setImplementationSummary(String implementationSummary) {
    this.implementationSummary = implementationSummary;
  }

  /**
   **/
  public AlertInterpreter inputModel(String inputModel) {
    this.inputModel = inputModel;
    return this;
  }

  
  @ApiModelProperty(example = "EventInterpreterInput", value = "")
  @JsonProperty("inputModel")
  public String getInputModel() {
    return inputModel;
  }

  @JsonProperty("inputModel")
  public void setInputModel(String inputModel) {
    this.inputModel = inputModel;
  }

  /**
   **/
  public AlertInterpreter outputModel(String outputModel) {
    this.outputModel = outputModel;
    return this;
  }

  
  @ApiModelProperty(example = "InterpreterSuggestionCandidate", value = "")
  @JsonProperty("outputModel")
  public String getOutputModel() {
    return outputModel;
  }

  @JsonProperty("outputModel")
  public void setOutputModel(String outputModel) {
    this.outputModel = outputModel;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertInterpreter alertInterpreter = (AlertInterpreter) o;
    return Objects.equals(this.interpreterType, alertInterpreter.interpreterType) &&
        Objects.equals(this.className, alertInterpreter.className) &&
        Objects.equals(this.contractVersion, alertInterpreter.contractVersion) &&
        Objects.equals(this.codeRef, alertInterpreter.codeRef) &&
        Objects.equals(this.implementationSummary, alertInterpreter.implementationSummary) &&
        Objects.equals(this.inputModel, alertInterpreter.inputModel) &&
        Objects.equals(this.outputModel, alertInterpreter.outputModel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interpreterType, className, contractVersion, codeRef, implementationSummary, inputModel, outputModel);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertInterpreter {\n");
    
    sb.append("    interpreterType: ").append(toIndentedString(interpreterType)).append("\n");
    sb.append("    className: ").append(toIndentedString(className)).append("\n");
    sb.append("    contractVersion: ").append(toIndentedString(contractVersion)).append("\n");
    sb.append("    codeRef: ").append(toIndentedString(codeRef)).append("\n");
    sb.append("    implementationSummary: ").append(toIndentedString(implementationSummary)).append("\n");
    sb.append("    inputModel: ").append(toIndentedString(inputModel)).append("\n");
    sb.append("    outputModel: ").append(toIndentedString(outputModel)).append("\n");
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
