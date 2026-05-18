package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.GenericSuggestionTarget;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.MonitoredAudioMessageAggregateTarget;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.MonitoredAudioMessageTarget;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ServiceDataJourneyAggregateTarget;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ServiceDataJourneyTarget;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
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
 * Typed suggestion target. Exactly one target type must be represented according to &#x60;targetType&#x60;.
 **/
@ApiModel(description = "Typed suggestion target. Exactly one target type must be represented according to `targetType`.")
@JsonTypeName("SuggestionTarget")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class SuggestionTarget   {
  private SuggestionTargetType targetType;
  private ServiceDataJourneyTarget serviceDataJourney;
  private ServiceDataJourneyAggregateTarget serviceDataJourneyAggregate;
  private MonitoredAudioMessageTarget monitoredAudioMessage;
  private MonitoredAudioMessageAggregateTarget monitoredAudioMessageAggregate;
  private GenericSuggestionTarget generic;

  public SuggestionTarget() {
  }

  @JsonCreator
  public SuggestionTarget(
    @JsonProperty(required = true, value = "targetType") SuggestionTargetType targetType
  ) {
    this.targetType = targetType;
  }

  /**
   **/
  public SuggestionTarget targetType(SuggestionTargetType targetType) {
    this.targetType = targetType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "targetType")
  @NotNull public SuggestionTargetType getTargetType() {
    return targetType;
  }

  @JsonProperty(required = true, value = "targetType")
  public void setTargetType(SuggestionTargetType targetType) {
    this.targetType = targetType;
  }

  /**
   **/
  public SuggestionTarget serviceDataJourney(ServiceDataJourneyTarget serviceDataJourney) {
    this.serviceDataJourney = serviceDataJourney;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serviceDataJourney")
  @Valid public ServiceDataJourneyTarget getServiceDataJourney() {
    return serviceDataJourney;
  }

  @JsonProperty("serviceDataJourney")
  public void setServiceDataJourney(ServiceDataJourneyTarget serviceDataJourney) {
    this.serviceDataJourney = serviceDataJourney;
  }

  /**
   **/
  public SuggestionTarget serviceDataJourneyAggregate(ServiceDataJourneyAggregateTarget serviceDataJourneyAggregate) {
    this.serviceDataJourneyAggregate = serviceDataJourneyAggregate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serviceDataJourneyAggregate")
  @Valid public ServiceDataJourneyAggregateTarget getServiceDataJourneyAggregate() {
    return serviceDataJourneyAggregate;
  }

  @JsonProperty("serviceDataJourneyAggregate")
  public void setServiceDataJourneyAggregate(ServiceDataJourneyAggregateTarget serviceDataJourneyAggregate) {
    this.serviceDataJourneyAggregate = serviceDataJourneyAggregate;
  }

  /**
   **/
  public SuggestionTarget monitoredAudioMessage(MonitoredAudioMessageTarget monitoredAudioMessage) {
    this.monitoredAudioMessage = monitoredAudioMessage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("monitoredAudioMessage")
  @Valid public MonitoredAudioMessageTarget getMonitoredAudioMessage() {
    return monitoredAudioMessage;
  }

  @JsonProperty("monitoredAudioMessage")
  public void setMonitoredAudioMessage(MonitoredAudioMessageTarget monitoredAudioMessage) {
    this.monitoredAudioMessage = monitoredAudioMessage;
  }

  /**
   **/
  public SuggestionTarget monitoredAudioMessageAggregate(MonitoredAudioMessageAggregateTarget monitoredAudioMessageAggregate) {
    this.monitoredAudioMessageAggregate = monitoredAudioMessageAggregate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("monitoredAudioMessageAggregate")
  @Valid public MonitoredAudioMessageAggregateTarget getMonitoredAudioMessageAggregate() {
    return monitoredAudioMessageAggregate;
  }

  @JsonProperty("monitoredAudioMessageAggregate")
  public void setMonitoredAudioMessageAggregate(MonitoredAudioMessageAggregateTarget monitoredAudioMessageAggregate) {
    this.monitoredAudioMessageAggregate = monitoredAudioMessageAggregate;
  }

  /**
   **/
  public SuggestionTarget generic(GenericSuggestionTarget generic) {
    this.generic = generic;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("generic")
  @Valid public GenericSuggestionTarget getGeneric() {
    return generic;
  }

  @JsonProperty("generic")
  public void setGeneric(GenericSuggestionTarget generic) {
    this.generic = generic;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuggestionTarget suggestionTarget = (SuggestionTarget) o;
    return Objects.equals(this.targetType, suggestionTarget.targetType) &&
        Objects.equals(this.serviceDataJourney, suggestionTarget.serviceDataJourney) &&
        Objects.equals(this.serviceDataJourneyAggregate, suggestionTarget.serviceDataJourneyAggregate) &&
        Objects.equals(this.monitoredAudioMessage, suggestionTarget.monitoredAudioMessage) &&
        Objects.equals(this.monitoredAudioMessageAggregate, suggestionTarget.monitoredAudioMessageAggregate) &&
        Objects.equals(this.generic, suggestionTarget.generic);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetType, serviceDataJourney, serviceDataJourneyAggregate, monitoredAudioMessage, monitoredAudioMessageAggregate, generic);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuggestionTarget {\n");
    
    sb.append("    targetType: ").append(toIndentedString(targetType)).append("\n");
    sb.append("    serviceDataJourney: ").append(toIndentedString(serviceDataJourney)).append("\n");
    sb.append("    serviceDataJourneyAggregate: ").append(toIndentedString(serviceDataJourneyAggregate)).append("\n");
    sb.append("    monitoredAudioMessage: ").append(toIndentedString(monitoredAudioMessage)).append("\n");
    sb.append("    monitoredAudioMessageAggregate: ").append(toIndentedString(monitoredAudioMessageAggregate)).append("\n");
    sb.append("    generic: ").append(toIndentedString(generic)).append("\n");
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
