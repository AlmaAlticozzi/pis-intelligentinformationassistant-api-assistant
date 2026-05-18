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



@JsonTypeName("AlertRequiredData")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AlertRequiredData   {
  public enum CategoryEnum {

    SERVICE_DATA(String.valueOf("SERVICE_DATA")), JOURNEY(String.valueOf("JOURNEY")), ANNOUNCEMENT(String.valueOf("ANNOUNCEMENT")), BROADCAST(String.valueOf("BROADCAST")), CONTENT(String.valueOf("CONTENT")), DEVICE(String.valueOf("DEVICE")), CONFIGURATION(String.valueOf("CONFIGURATION")), LOCAL_SUGGESTION_STORE(String.valueOf("LOCAL_SUGGESTION_STORE")), OTHER(String.valueOf("OTHER"));


    private String value;

    CategoryEnum (String v) {
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
    public static CategoryEnum fromString(String s) {
        for (CategoryEnum b : CategoryEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static CategoryEnum fromValue(String value) {
        for (CategoryEnum b : CategoryEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private CategoryEnum category;
  private Boolean required = true;
  private String description;

  public AlertRequiredData() {
  }

  @JsonCreator
  public AlertRequiredData(
    @JsonProperty(required = true, value = "category") CategoryEnum category
  ) {
    this.category = category;
  }

  /**
   **/
  public AlertRequiredData category(CategoryEnum category) {
    this.category = category;
    return this;
  }

  
  @ApiModelProperty(example = "BROADCAST", required = true, value = "")
  @JsonProperty(required = true, value = "category")
  @NotNull public CategoryEnum getCategory() {
    return category;
  }

  @JsonProperty(required = true, value = "category")
  public void setCategory(CategoryEnum category) {
    this.category = category;
  }

  /**
   **/
  public AlertRequiredData required(Boolean required) {
    this.required = required;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("required")
  public Boolean getRequired() {
    return required;
  }

  @JsonProperty("required")
  public void setRequired(Boolean required) {
    this.required = required;
  }

  /**
   **/
  public AlertRequiredData description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Broadcast history is required to determine whether the announcement was actually published.", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertRequiredData alertRequiredData = (AlertRequiredData) o;
    return Objects.equals(this.category, alertRequiredData.category) &&
        Objects.equals(this.required, alertRequiredData.required) &&
        Objects.equals(this.description, alertRequiredData.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, required, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertRequiredData {\n");
    
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
