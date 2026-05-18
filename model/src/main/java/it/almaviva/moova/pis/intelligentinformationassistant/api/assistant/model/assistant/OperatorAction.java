package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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

/**
 * Operator action metadata.
 **/
@ApiModel(description = "Operator action metadata.")
@JsonTypeName("OperatorAction")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class OperatorAction   {
  private String operatorUserId;
  private OffsetDateTime actionTime;
  private String operatorNote;

  public OperatorAction() {
  }

  /**
   **/
  public OperatorAction operatorUserId(String operatorUserId) {
    this.operatorUserId = operatorUserId;
    return this;
  }

  
  @ApiModelProperty(example = "m.user", value = "")
  @JsonProperty("operatorUserId")
  public String getOperatorUserId() {
    return operatorUserId;
  }

  @JsonProperty("operatorUserId")
  public void setOperatorUserId(String operatorUserId) {
    this.operatorUserId = operatorUserId;
  }

  /**
   **/
  public OperatorAction actionTime(OffsetDateTime actionTime) {
    this.actionTime = actionTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("actionTime")
  public OffsetDateTime getActionTime() {
    return actionTime;
  }

  @JsonProperty("actionTime")
  public void setActionTime(OffsetDateTime actionTime) {
    this.actionTime = actionTime;
  }

  /**
   **/
  public OperatorAction operatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
    return this;
  }

  
  @ApiModelProperty(example = "Approved after operational check.", value = "")
  @JsonProperty("operatorNote")
  public String getOperatorNote() {
    return operatorNote;
  }

  @JsonProperty("operatorNote")
  public void setOperatorNote(String operatorNote) {
    this.operatorNote = operatorNote;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperatorAction operatorAction = (OperatorAction) o;
    return Objects.equals(this.operatorUserId, operatorAction.operatorUserId) &&
        Objects.equals(this.actionTime, operatorAction.actionTime) &&
        Objects.equals(this.operatorNote, operatorAction.operatorNote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operatorUserId, actionTime, operatorNote);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperatorAction {\n");
    
    sb.append("    operatorUserId: ").append(toIndentedString(operatorUserId)).append("\n");
    sb.append("    actionTime: ").append(toIndentedString(actionTime)).append("\n");
    sb.append("    operatorNote: ").append(toIndentedString(operatorNote)).append("\n");
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
