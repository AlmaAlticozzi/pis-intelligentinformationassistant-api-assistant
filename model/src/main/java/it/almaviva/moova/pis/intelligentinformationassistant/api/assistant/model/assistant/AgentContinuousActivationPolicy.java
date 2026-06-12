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



@JsonTypeName("CONTINUOUS")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentContinuousActivationPolicy extends AgentActivationPolicy  {
  public enum TypeEnum {

    CONTINUOUS(String.valueOf("CONTINUOUS"));


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

  private AgentActivationPolicy.TypeEnum type;
  private String timezone;
  private OffsetDateTime validFrom;
  private OffsetDateTime validTo;

  public AgentContinuousActivationPolicy() {
  }

  @JsonCreator
  public AgentContinuousActivationPolicy(
    @JsonProperty(required = true, value = "type") AgentActivationPolicy.TypeEnum type,
    @JsonProperty(required = true, value = "timezone") String timezone,
    @JsonProperty(required = true, value = "validFrom") OffsetDateTime validFrom,
    @JsonProperty(required = true, value = "validTo") OffsetDateTime validTo
  ) {
    super.setType(AgentActivationPolicy.TypeEnum.CONTINUOUS);
    super.setTimezone(timezone);
    super.setValidFrom(validFrom);
    super.setValidTo(validTo);
    this.type = type;
    this.timezone = timezone;
    this.validFrom = validFrom;
    this.validTo = validTo;
  }

  /**
   **/
  public AgentContinuousActivationPolicy type(AgentActivationPolicy.TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "type")
  @NotNull public AgentActivationPolicy.TypeEnum getType() {
    return type;
  }

  @JsonProperty(required = true, value = "type")
  public void setType(AgentActivationPolicy.TypeEnum type) {
    this.type = type;
    super.setType(type);
  }

  /**
   **/
  public AgentContinuousActivationPolicy timezone(String timezone) {
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
    super.setTimezone(timezone);
  }

  /**
   * Local validity start expressed with timezone offset.
   **/
  public AgentContinuousActivationPolicy validFrom(OffsetDateTime validFrom) {
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
    super.setValidFrom(validFrom);
  }

  /**
   * Local validity end expressed with timezone offset.
   **/
  public AgentContinuousActivationPolicy validTo(OffsetDateTime validTo) {
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
    super.setValidTo(validTo);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentContinuousActivationPolicy agentContinuousActivationPolicy = (AgentContinuousActivationPolicy) o;
    return Objects.equals(this.type, agentContinuousActivationPolicy.type) &&
        Objects.equals(this.timezone, agentContinuousActivationPolicy.timezone) &&
        Objects.equals(this.validFrom, agentContinuousActivationPolicy.validFrom) &&
        Objects.equals(this.validTo, agentContinuousActivationPolicy.validTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, timezone, validFrom, validTo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentContinuousActivationPolicy {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    timezone: ").append(toIndentedString(timezone)).append("\n");
    sb.append("    validFrom: ").append(toIndentedString(validFrom)).append("\n");
    sb.append("    validTo: ").append(toIndentedString(validTo)).append("\n");
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
