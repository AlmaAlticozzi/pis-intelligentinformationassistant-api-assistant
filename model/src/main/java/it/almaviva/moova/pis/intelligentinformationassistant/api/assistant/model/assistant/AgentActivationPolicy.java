package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentContinuousActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDailyWindowActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = AgentContinuousActivationPolicy.class, name = "CONTINUOUS"),
  @JsonSubTypes.Type(value = AgentDailyWindowActivationPolicy.class, name = "DAILY_WINDOW"),
})


@JsonTypeName("AgentActivationPolicy")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentActivationPolicy   {
  public enum TypeEnum {

    CONTINUOUS(String.valueOf("CONTINUOUS")), DAILY_WINDOW(String.valueOf("DAILY_WINDOW"));


    private String value;

    TypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static TypeEnum fromString(String s) {
        for (TypeEnum b : TypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private TypeEnum type;
  private String timezone;
  private OffsetDateTime validFrom;
  private OffsetDateTime validTo;
  private LocalDate validFromDate;
  private LocalDate validToDate;
  private String dailyStartTime;
  private String dailyEndTime;
  private @Valid List<DayOfWeek> daysOfWeek = new ArrayList<>();

  public AgentActivationPolicy() {
  }

  @JsonCreator
  public AgentActivationPolicy(
    @JsonProperty(required = true, value = "type") TypeEnum type,
    @JsonProperty(required = true, value = "timezone") String timezone,
    @JsonProperty(required = true, value = "validFrom") OffsetDateTime validFrom,
    @JsonProperty(required = true, value = "validTo") OffsetDateTime validTo,
    @JsonProperty(required = true, value = "validFromDate") LocalDate validFromDate,
    @JsonProperty(required = true, value = "validToDate") LocalDate validToDate,
    @JsonProperty(required = true, value = "dailyStartTime") String dailyStartTime,
    @JsonProperty(required = true, value = "dailyEndTime") String dailyEndTime
  ) {
    this.type = type;
    this.timezone = timezone;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.validFromDate = validFromDate;
    this.validToDate = validToDate;
    this.dailyStartTime = dailyStartTime;
    this.dailyEndTime = dailyEndTime;
  }

  /**
   **/
  public AgentActivationPolicy type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "type")
  @NotNull public TypeEnum getType() {
    return type;
  }

  @JsonProperty(required = true, value = "type")
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  public AgentActivationPolicy timezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  
  @ApiModelProperty(example = "Europe/Rome", required = true, value = "")
  @JsonProperty(required = true, value = "timezone")
  @NotNull public String getTimezone() {
    return timezone;
  }

  @JsonProperty(required = true, value = "timezone")
  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  /**
   * Local validity start expressed with timezone offset.
   **/
  public AgentActivationPolicy validFrom(OffsetDateTime validFrom) {
    this.validFrom = validFrom;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Local validity start expressed with timezone offset.")
  @JsonProperty(required = true, value = "validFrom")
  @NotNull public OffsetDateTime getValidFrom() {
    return validFrom;
  }

  @JsonProperty(required = true, value = "validFrom")
  public void setValidFrom(OffsetDateTime validFrom) {
    this.validFrom = validFrom;
  }

  /**
   * Local validity end expressed with timezone offset.
   **/
  public AgentActivationPolicy validTo(OffsetDateTime validTo) {
    this.validTo = validTo;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Local validity end expressed with timezone offset.")
  @JsonProperty(required = true, value = "validTo")
  @NotNull public OffsetDateTime getValidTo() {
    return validTo;
  }

  @JsonProperty(required = true, value = "validTo")
  public void setValidTo(OffsetDateTime validTo) {
    this.validTo = validTo;
  }

  /**
   **/
  public AgentActivationPolicy validFromDate(LocalDate validFromDate) {
    this.validFromDate = validFromDate;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "validFromDate")
  @NotNull public LocalDate getValidFromDate() {
    return validFromDate;
  }

  @JsonProperty(required = true, value = "validFromDate")
  public void setValidFromDate(LocalDate validFromDate) {
    this.validFromDate = validFromDate;
  }

  /**
   **/
  public AgentActivationPolicy validToDate(LocalDate validToDate) {
    this.validToDate = validToDate;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "validToDate")
  @NotNull public LocalDate getValidToDate() {
    return validToDate;
  }

  @JsonProperty(required = true, value = "validToDate")
  public void setValidToDate(LocalDate validToDate) {
    this.validToDate = validToDate;
  }

  /**
   **/
  public AgentActivationPolicy dailyStartTime(String dailyStartTime) {
    this.dailyStartTime = dailyStartTime;
    return this;
  }

  
  @ApiModelProperty(example = "07:00:00", required = true, value = "")
  @JsonProperty(required = true, value = "dailyStartTime")
  @NotNull  @Pattern(regexp="^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$")public String getDailyStartTime() {
    return dailyStartTime;
  }

  @JsonProperty(required = true, value = "dailyStartTime")
  public void setDailyStartTime(String dailyStartTime) {
    this.dailyStartTime = dailyStartTime;
  }

  /**
   **/
  public AgentActivationPolicy dailyEndTime(String dailyEndTime) {
    this.dailyEndTime = dailyEndTime;
    return this;
  }

  
  @ApiModelProperty(example = "10:30:00", required = true, value = "")
  @JsonProperty(required = true, value = "dailyEndTime")
  @NotNull  @Pattern(regexp="^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$")public String getDailyEndTime() {
    return dailyEndTime;
  }

  @JsonProperty(required = true, value = "dailyEndTime")
  public void setDailyEndTime(String dailyEndTime) {
    this.dailyEndTime = dailyEndTime;
  }

  /**
   **/
  public AgentActivationPolicy daysOfWeek(List<DayOfWeek> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
    return this;
  }

  
  @ApiModelProperty(example = "[MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]", value = "")
  @JsonProperty("daysOfWeek")
  public List<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  @JsonProperty("daysOfWeek")
  public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
  }

  public AgentActivationPolicy addDaysOfWeekItem(DayOfWeek daysOfWeekItem) {
    if (this.daysOfWeek == null) {
      this.daysOfWeek = new ArrayList<>();
    }

    this.daysOfWeek.add(daysOfWeekItem);
    return this;
  }

  public AgentActivationPolicy removeDaysOfWeekItem(DayOfWeek daysOfWeekItem) {
    if (daysOfWeekItem != null && this.daysOfWeek != null) {
      this.daysOfWeek.remove(daysOfWeekItem);
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
    AgentActivationPolicy agentActivationPolicy = (AgentActivationPolicy) o;
    return Objects.equals(this.type, agentActivationPolicy.type) &&
        Objects.equals(this.timezone, agentActivationPolicy.timezone) &&
        Objects.equals(this.validFrom, agentActivationPolicy.validFrom) &&
        Objects.equals(this.validTo, agentActivationPolicy.validTo) &&
        Objects.equals(this.validFromDate, agentActivationPolicy.validFromDate) &&
        Objects.equals(this.validToDate, agentActivationPolicy.validToDate) &&
        Objects.equals(this.dailyStartTime, agentActivationPolicy.dailyStartTime) &&
        Objects.equals(this.dailyEndTime, agentActivationPolicy.dailyEndTime) &&
        Objects.equals(this.daysOfWeek, agentActivationPolicy.daysOfWeek);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, timezone, validFrom, validTo, validFromDate, validToDate, dailyStartTime, dailyEndTime, daysOfWeek);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentActivationPolicy {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    timezone: ").append(toIndentedString(timezone)).append("\n");
    sb.append("    validFrom: ").append(toIndentedString(validFrom)).append("\n");
    sb.append("    validTo: ").append(toIndentedString(validTo)).append("\n");
    sb.append("    validFromDate: ").append(toIndentedString(validFromDate)).append("\n");
    sb.append("    validToDate: ").append(toIndentedString(validToDate)).append("\n");
    sb.append("    dailyStartTime: ").append(toIndentedString(dailyStartTime)).append("\n");
    sb.append("    dailyEndTime: ").append(toIndentedString(dailyEndTime)).append("\n");
    sb.append("    daysOfWeek: ").append(toIndentedString(daysOfWeek)).append("\n");
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
