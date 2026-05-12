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

/**
 * Journey reference.
 **/
@ApiModel(description = "Journey reference.")
@JsonTypeName("JourneyRef")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-12T15:20:56.039425814Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class JourneyRef   {
  private String infomobilityVehicleJourneyId;
  private String infomobilityDataFrameId;
  private String name;
  private String lineId;
  private String lineName;

  public JourneyRef() {
  }

  /**
   **/
  public JourneyRef infomobilityVehicleJourneyId(String infomobilityVehicleJourneyId) {
    this.infomobilityVehicleJourneyId = infomobilityVehicleJourneyId;
    return this;
  }

  
  @ApiModelProperty(example = "VJ-1234", value = "")
  @JsonProperty("infomobilityVehicleJourneyId")
  public String getInfomobilityVehicleJourneyId() {
    return infomobilityVehicleJourneyId;
  }

  @JsonProperty("infomobilityVehicleJourneyId")
  public void setInfomobilityVehicleJourneyId(String infomobilityVehicleJourneyId) {
    this.infomobilityVehicleJourneyId = infomobilityVehicleJourneyId;
  }

  /**
   **/
  public JourneyRef infomobilityDataFrameId(String infomobilityDataFrameId) {
    this.infomobilityDataFrameId = infomobilityDataFrameId;
    return this;
  }

  
  @ApiModelProperty(example = "DF-20260510", value = "")
  @JsonProperty("infomobilityDataFrameId")
  public String getInfomobilityDataFrameId() {
    return infomobilityDataFrameId;
  }

  @JsonProperty("infomobilityDataFrameId")
  public void setInfomobilityDataFrameId(String infomobilityDataFrameId) {
    this.infomobilityDataFrameId = infomobilityDataFrameId;
  }

  /**
   **/
  public JourneyRef name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "AV 304", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public JourneyRef lineId(String lineId) {
    this.lineId = lineId;
    return this;
  }

  
  @ApiModelProperty(example = "LINE-AV", value = "")
  @JsonProperty("lineId")
  public String getLineId() {
    return lineId;
  }

  @JsonProperty("lineId")
  public void setLineId(String lineId) {
    this.lineId = lineId;
  }

  /**
   **/
  public JourneyRef lineName(String lineName) {
    this.lineName = lineName;
    return this;
  }

  
  @ApiModelProperty(example = "Alta Velocità", value = "")
  @JsonProperty("lineName")
  public String getLineName() {
    return lineName;
  }

  @JsonProperty("lineName")
  public void setLineName(String lineName) {
    this.lineName = lineName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JourneyRef journeyRef = (JourneyRef) o;
    return Objects.equals(this.infomobilityVehicleJourneyId, journeyRef.infomobilityVehicleJourneyId) &&
        Objects.equals(this.infomobilityDataFrameId, journeyRef.infomobilityDataFrameId) &&
        Objects.equals(this.name, journeyRef.name) &&
        Objects.equals(this.lineId, journeyRef.lineId) &&
        Objects.equals(this.lineName, journeyRef.lineName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(infomobilityVehicleJourneyId, infomobilityDataFrameId, name, lineId, lineName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JourneyRef {\n");
    
    sb.append("    infomobilityVehicleJourneyId: ").append(toIndentedString(infomobilityVehicleJourneyId)).append("\n");
    sb.append("    infomobilityDataFrameId: ").append(toIndentedString(infomobilityDataFrameId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    lineId: ").append(toIndentedString(lineId)).append("\n");
    sb.append("    lineName: ").append(toIndentedString(lineName)).append("\n");
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
