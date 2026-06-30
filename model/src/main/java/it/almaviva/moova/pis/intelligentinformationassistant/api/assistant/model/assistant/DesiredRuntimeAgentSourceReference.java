package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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



@JsonTypeName("DesiredRuntimeAgentSourceReference")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeAgentSourceReference   {
  private String controlPlaneComponent;
  private String alertId;
  private String alertName;
  private Integer alertVersion;
  private String agentCompilationId;
  private Integer agentCompilationVersion;

  public DesiredRuntimeAgentSourceReference() {
  }

  @JsonCreator
  public DesiredRuntimeAgentSourceReference(
    @JsonProperty(required = true, value = "controlPlaneComponent") String controlPlaneComponent,
    @JsonProperty(required = true, value = "alertId") String alertId,
    @JsonProperty(required = true, value = "alertVersion") Integer alertVersion
  ) {
    this.controlPlaneComponent = controlPlaneComponent;
    this.alertId = alertId;
    this.alertVersion = alertVersion;
  }

  /**
   **/
  public DesiredRuntimeAgentSourceReference controlPlaneComponent(String controlPlaneComponent) {
    this.controlPlaneComponent = controlPlaneComponent;
    return this;
  }

  
  @ApiModelProperty(example = "pis-intelligentinformationassistant-api-assistant", required = true, value = "")
  @JsonProperty(required = true, value = "controlPlaneComponent")
  @NotNull  @Size(max=100)public String getControlPlaneComponent() {
    return controlPlaneComponent;
  }

  @JsonProperty(required = true, value = "controlPlaneComponent")
  public void setControlPlaneComponent(String controlPlaneComponent) {
    this.controlPlaneComponent = controlPlaneComponent;
  }

  /**
   **/
  public DesiredRuntimeAgentSourceReference alertId(String alertId) {
    this.alertId = alertId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "alertId")
  @NotNull  @Size(min=1,max=50)public String getAlertId() {
    return alertId;
  }

  @JsonProperty(required = true, value = "alertId")
  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  /**
   **/
  public DesiredRuntimeAgentSourceReference alertName(String alertName) {
    this.alertName = alertName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("alertName")
   @Size(max=120)public String getAlertName() {
    return alertName;
  }

  @JsonProperty("alertName")
  public void setAlertName(String alertName) {
    this.alertName = alertName;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentSourceReference alertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "alertVersion")
  @NotNull  @Min(1)public Integer getAlertVersion() {
    return alertVersion;
  }

  @JsonProperty(required = true, value = "alertVersion")
  public void setAlertVersion(Integer alertVersion) {
    this.alertVersion = alertVersion;
  }

  /**
   **/
  public DesiredRuntimeAgentSourceReference agentCompilationId(String agentCompilationId) {
    this.agentCompilationId = agentCompilationId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("agentCompilationId")
   @Size(max=50)public String getAgentCompilationId() {
    return agentCompilationId;
  }

  @JsonProperty("agentCompilationId")
  public void setAgentCompilationId(String agentCompilationId) {
    this.agentCompilationId = agentCompilationId;
  }

  /**
   * minimum: 1
   **/
  public DesiredRuntimeAgentSourceReference agentCompilationVersion(Integer agentCompilationVersion) {
    this.agentCompilationVersion = agentCompilationVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("agentCompilationVersion")
   @Min(1)public Integer getAgentCompilationVersion() {
    return agentCompilationVersion;
  }

  @JsonProperty("agentCompilationVersion")
  public void setAgentCompilationVersion(Integer agentCompilationVersion) {
    this.agentCompilationVersion = agentCompilationVersion;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesiredRuntimeAgentSourceReference desiredRuntimeAgentSourceReference = (DesiredRuntimeAgentSourceReference) o;
    return Objects.equals(this.controlPlaneComponent, desiredRuntimeAgentSourceReference.controlPlaneComponent) &&
        Objects.equals(this.alertId, desiredRuntimeAgentSourceReference.alertId) &&
        Objects.equals(this.alertName, desiredRuntimeAgentSourceReference.alertName) &&
        Objects.equals(this.alertVersion, desiredRuntimeAgentSourceReference.alertVersion) &&
        Objects.equals(this.agentCompilationId, desiredRuntimeAgentSourceReference.agentCompilationId) &&
        Objects.equals(this.agentCompilationVersion, desiredRuntimeAgentSourceReference.agentCompilationVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(controlPlaneComponent, alertId, alertName, alertVersion, agentCompilationId, agentCompilationVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeAgentSourceReference {\n");
    
    sb.append("    controlPlaneComponent: ").append(toIndentedString(controlPlaneComponent)).append("\n");
    sb.append("    alertId: ").append(toIndentedString(alertId)).append("\n");
    sb.append("    alertName: ").append(toIndentedString(alertName)).append("\n");
    sb.append("    alertVersion: ").append(toIndentedString(alertVersion)).append("\n");
    sb.append("    agentCompilationId: ").append(toIndentedString(agentCompilationId)).append("\n");
    sb.append("    agentCompilationVersion: ").append(toIndentedString(agentCompilationVersion)).append("\n");
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
