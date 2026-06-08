package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("AlertTechnicalSpecificationUpdateRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-08T11:10:16.490174964Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertTechnicalSpecificationUpdateRequest   {
  private @Valid Map<String, Object> technicalSpecification = new HashMap<>();

  public AlertTechnicalSpecificationUpdateRequest() {
  }

  @JsonCreator
  public AlertTechnicalSpecificationUpdateRequest(
    @JsonProperty(required = true, value = "technicalSpecification") Map<String, Object> technicalSpecification
  ) {
    this.technicalSpecification = technicalSpecification;
  }

  /**
   * Backend-validated, non-executable technical specification of an Alert.  The object represents the controlled interpretation of the Alert prompt and is the source of truth for future Agent generation flows. Its concrete structure depends on the interpreter type and evaluation mode.  For &#x60;EVENT_INTERPRETER&#x60;, the specification describes a stateless condition over realtime &#x60;ServiceDataV2&#x60; event messages.  For &#x60;SCHEDULED_INTERPRETER&#x60;, the specification describes a scheduled snapshot evaluation over Service Data API results, including schedule, service data query, output policy and snapshot evaluation rules.  The API intentionally models this object as an extensible JSON artifact because its exact shape is governed by backend validators and capability catalogs rather than by arbitrary client-side editing. Clients may display and submit the JSON, but the backend remains the authority that validates whether the specification is acceptable.  This object must never contain executable code, scripts, SQL statements or ungoverned instructions. 
   **/
  public AlertTechnicalSpecificationUpdateRequest technicalSpecification(Map<String, Object> technicalSpecification) {
    this.technicalSpecification = technicalSpecification;
    return this;
  }

  
  @ApiModelProperty(example = "{\"source\":\"SERVICE_DATA\",\"schemaVersion\":\"iia.alert.technical-specification/v2\",\"interpreterType\":\"SCHEDULED_INTERPRETER\",\"triggerType\":\"SCHEDULE\",\"accessMode\":\"SERVICE_DATA_API_SNAPSHOT\",\"inputModel\":\"ServiceDataStopPointJourneysV2\",\"outputModel\":\"AgentOutput.CANDIDATE_SUGGESTION\",\"evaluationMode\":\"SCHEDULED_SNAPSHOT_MATCH\",\"schedule\":{\"frequencySeconds\":600,\"defaulted\":true},\"serviceDataQuery\":{\"operation\":\"POST /v2/stoppointjourneys\",\"stopPoints\":[\"TNPNTS00000000000009\"],\"monitoringScope\":\"EXPLICIT_STOP_POINTS\",\"requiresAllKnownStopPoints\":false,\"timeWindow\":{\"startMode\":\"NOW_TRUNCATED_TO_MINUTE\",\"endMode\":\"NOW_PLUS_DEFAULT_LOOKAHEAD\",\"lookaheadMinutes\":480,\"defaulted\":true}},\"outputPolicy\":{\"emit\":\"EVERY_RUN\",\"includeCount\":true,\"includeMatchingJourneys\":false},\"snapshotEvaluation\":{\"mode\":\"REPORT_COUNT\",\"journeyPath\":\"stopPointsJourneyDetails[]\",\"condition\":{\"type\":\"SERVICE_DATA_SCHEDULED_FIELD_MATCH\",\"anyElement\":{\"path\":\"stopPointsJourneyDetails[]\",\"conditions\":{\"all\":[{\"field\":\"departureDelay.delay\",\"operator\":\"GREATER_THAN\",\"value\":0}]}}}}}", required = true, value = "Backend-validated, non-executable technical specification of an Alert.  The object represents the controlled interpretation of the Alert prompt and is the source of truth for future Agent generation flows. Its concrete structure depends on the interpreter type and evaluation mode.  For `EVENT_INTERPRETER`, the specification describes a stateless condition over realtime `ServiceDataV2` event messages.  For `SCHEDULED_INTERPRETER`, the specification describes a scheduled snapshot evaluation over Service Data API results, including schedule, service data query, output policy and snapshot evaluation rules.  The API intentionally models this object as an extensible JSON artifact because its exact shape is governed by backend validators and capability catalogs rather than by arbitrary client-side editing. Clients may display and submit the JSON, but the backend remains the authority that validates whether the specification is acceptable.  This object must never contain executable code, scripts, SQL statements or ungoverned instructions. ")
  @JsonProperty(required = true, value = "technicalSpecification")
  @NotNull public Map<String, Object> getTechnicalSpecification() {
    return technicalSpecification;
  }

  @JsonProperty(required = true, value = "technicalSpecification")
  public void setTechnicalSpecification(Map<String, Object> technicalSpecification) {
    this.technicalSpecification = technicalSpecification;
  }

  public AlertTechnicalSpecificationUpdateRequest putTechnicalSpecificationItem(String key, Object technicalSpecificationItem) {
    if (this.technicalSpecification == null) {
      this.technicalSpecification = new HashMap<>();
    }

    this.technicalSpecification.put(key, technicalSpecificationItem);
    return this;
  }

  public AlertTechnicalSpecificationUpdateRequest removeTechnicalSpecificationItem(String key) {
    if (this.technicalSpecification != null) {
      this.technicalSpecification.remove(key);
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
    AlertTechnicalSpecificationUpdateRequest alertTechnicalSpecificationUpdateRequest = (AlertTechnicalSpecificationUpdateRequest) o;
    return Objects.equals(this.technicalSpecification, alertTechnicalSpecificationUpdateRequest.technicalSpecification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(technicalSpecification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertTechnicalSpecificationUpdateRequest {\n");
    
    sb.append("    technicalSpecification: ").append(toIndentedString(technicalSpecification)).append("\n");
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
