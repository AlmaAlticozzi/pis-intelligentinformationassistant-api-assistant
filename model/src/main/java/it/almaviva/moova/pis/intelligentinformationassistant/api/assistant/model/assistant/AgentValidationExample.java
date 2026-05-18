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



@JsonTypeName("AgentValidationExample")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentValidationExample   {
  private String description;
  public enum ExpectedOutputEnum {

    CANDIDATE_SUGGESTION(String.valueOf("CANDIDATE_SUGGESTION")), NO_OUTPUT(String.valueOf("NO_OUTPUT")), ERROR(String.valueOf("ERROR"));


    private String value;

    ExpectedOutputEnum (String v) {
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
    public static ExpectedOutputEnum fromString(String s) {
        for (ExpectedOutputEnum b : ExpectedOutputEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static ExpectedOutputEnum fromValue(String value) {
        for (ExpectedOutputEnum b : ExpectedOutputEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private ExpectedOutputEnum expectedOutput;

  public AgentValidationExample() {
  }

  /**
   **/
  public AgentValidationExample description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public AgentValidationExample expectedOutput(ExpectedOutputEnum expectedOutput) {
    this.expectedOutput = expectedOutput;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("expectedOutput")
  public ExpectedOutputEnum getExpectedOutput() {
    return expectedOutput;
  }

  @JsonProperty("expectedOutput")
  public void setExpectedOutput(ExpectedOutputEnum expectedOutput) {
    this.expectedOutput = expectedOutput;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentValidationExample agentValidationExample = (AgentValidationExample) o;
    return Objects.equals(this.description, agentValidationExample.description) &&
        Objects.equals(this.expectedOutput, agentValidationExample.expectedOutput);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, expectedOutput);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentValidationExample {\n");
    
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    expectedOutput: ").append(toIndentedString(expectedOutput)).append("\n");
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
