package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DayOfWeek;
import java.time.LocalDate;
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



@JsonTypeName("AgentDailyWindowActivationPolicy")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentDailyWindowActivationPolicy   {
  public enum TypeEnum {

    DAILY_WINDOW(String.valueOf("DAILY_WINDOW"));


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
  private LocalDate validFromDate;
  private LocalDate validToDate;
  private String dailyStartTime;
  private String dailyEndTime;
  private @Valid List<DayOfWeek> daysOfWeek = new ArrayList<>();

  public AgentDailyWindowActivationPolicy() {
  }

  @JsonCreator
  public AgentDailyWindowActivationPolicy(
    @JsonProperty(required = true, value = "type") TypeEnum type,
    @JsonProperty(required = true, value = "timezone") String timezone,
    @JsonProperty(required = true, value = "validFromDate") LocalDate validFromDate,
    @JsonProperty(required = true, value = "validToDate") LocalDate validToDate,
    @JsonProperty(required = true, value = "dailyStartTime") String dailyStartTime,
    @JsonProperty(required = true, value = "dailyEndTime") String dailyEndTime
  ) {
    this.type = type;
    this.timezone = timezone;
    this.validFromDate = validFromDate;
    this.validToDate = validToDate;
    this.dailyStartTime = dailyStartTime;
    this.dailyEndTime = dailyEndTime;
  }

  /**
   **/
  public AgentDailyWindowActivationPolicy type(TypeEnum type) {
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
  public AgentDailyWindowActivationPolicy timezone(String timezone) {
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
   **/
  public AgentDailyWindowActivationPolicy validFromDate(LocalDate validFromDate) {
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
  public AgentDailyWindowActivationPolicy validToDate(LocalDate validToDate) {
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
  public AgentDailyWindowActivationPolicy dailyStartTime(String dailyStartTime) {
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
  public AgentDailyWindowActivationPolicy dailyEndTime(String dailyEndTime) {
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
  public AgentDailyWindowActivationPolicy daysOfWeek(List<DayOfWeek> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
    return this;
  }

  
  @ApiModelProperty(example = "[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\"]", value = "")
  @JsonProperty("daysOfWeek")
  public List<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  @JsonProperty("daysOfWeek")
  public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
  }

  public AgentDailyWindowActivationPolicy addDaysOfWeekItem(DayOfWeek daysOfWeekItem) {
    if (this.daysOfWeek == null) {
      this.daysOfWeek = new ArrayList<>();
    }

    this.daysOfWeek.add(daysOfWeekItem);
    return this;
  }

  public AgentDailyWindowActivationPolicy removeDaysOfWeekItem(DayOfWeek daysOfWeekItem) {
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
    AgentDailyWindowActivationPolicy agentDailyWindowActivationPolicy = (AgentDailyWindowActivationPolicy) o;
    return Objects.equals(this.type, agentDailyWindowActivationPolicy.type) &&
        Objects.equals(this.timezone, agentDailyWindowActivationPolicy.timezone) &&
        Objects.equals(this.validFromDate, agentDailyWindowActivationPolicy.validFromDate) &&
        Objects.equals(this.validToDate, agentDailyWindowActivationPolicy.validToDate) &&
        Objects.equals(this.dailyStartTime, agentDailyWindowActivationPolicy.dailyStartTime) &&
        Objects.equals(this.dailyEndTime, agentDailyWindowActivationPolicy.dailyEndTime) &&
        Objects.equals(this.daysOfWeek, agentDailyWindowActivationPolicy.daysOfWeek);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, timezone, validFromDate, validToDate, dailyStartTime, dailyEndTime, daysOfWeek);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentDailyWindowActivationPolicy {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    timezone: ").append(toIndentedString(timezone)).append("\n");
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
