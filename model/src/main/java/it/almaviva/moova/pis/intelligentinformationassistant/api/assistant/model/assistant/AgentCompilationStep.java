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



@JsonTypeName("AgentCompilationStep")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentCompilationStep   {
  private String name;
  public enum StatusEnum {

    PENDING(String.valueOf("PENDING")), RUNNING(String.valueOf("RUNNING")), SUCCESS(String.valueOf("SUCCESS")), FAILED(String.valueOf("FAILED")), SKIPPED(String.valueOf("SKIPPED"));


    private String value;

    StatusEnum (String v) {
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
    public static StatusEnum fromString(String s) {
        for (StatusEnum b : StatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private StatusEnum status;
  private OffsetDateTime startedAt;
  private OffsetDateTime completedAt;
  private String message;

  public AgentCompilationStep() {
  }

  /**
   **/
  public AgentCompilationStep name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "STATIC_ANALYSIS", value = "")
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
  public AgentCompilationStep status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   **/
  public AgentCompilationStep startedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("startedAt")
  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  @JsonProperty("startedAt")
  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  /**
   **/
  public AgentCompilationStep completedAt(OffsetDateTime completedAt) {
    this.completedAt = completedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("completedAt")
  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  @JsonProperty("completedAt")
  public void setCompletedAt(OffsetDateTime completedAt) {
    this.completedAt = completedAt;
  }

  /**
   **/
  public AgentCompilationStep message(String message) {
    this.message = message;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentCompilationStep agentCompilationStep = (AgentCompilationStep) o;
    return Objects.equals(this.name, agentCompilationStep.name) &&
        Objects.equals(this.status, agentCompilationStep.status) &&
        Objects.equals(this.startedAt, agentCompilationStep.startedAt) &&
        Objects.equals(this.completedAt, agentCompilationStep.completedAt) &&
        Objects.equals(this.message, agentCompilationStep.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, status, startedAt, completedAt, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentCompilationStep {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    completedAt: ").append(toIndentedString(completedAt)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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
