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
 * Preview of the controlled DSL generated for the Agent. It is intended for diagnostics and governance, not for direct editing by the UI.
 **/
@ApiModel(description = "Preview of the controlled DSL generated for the Agent. It is intended for diagnostics and governance, not for direct editing by the UI.")
@JsonTypeName("AgentDslPreview")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentDslPreview   {
  private String schemaVersion;
  private String summary;
  private String dsl;
  private Boolean supportedByRuntime;

  public AgentDslPreview() {
  }

  /**
   **/
  public AgentDslPreview schemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
    return this;
  }

  
  @ApiModelProperty(example = "iia.agent.dsl/v1", value = "")
  @JsonProperty("schemaVersion")
  public String getSchemaVersion() {
    return schemaVersion;
  }

  @JsonProperty("schemaVersion")
  public void setSchemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  /**
   **/
  public AgentDslPreview summary(String summary) {
    this.summary = summary;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("summary")
  public String getSummary() {
    return summary;
  }

  @JsonProperty("summary")
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * Sanitized DSL text preview.
   **/
  public AgentDslPreview dsl(String dsl) {
    this.dsl = dsl;
    return this;
  }

  
  @ApiModelProperty(value = "Sanitized DSL text preview.")
  @JsonProperty("dsl")
  public String getDsl() {
    return dsl;
  }

  @JsonProperty("dsl")
  public void setDsl(String dsl) {
    this.dsl = dsl;
  }

  /**
   **/
  public AgentDslPreview supportedByRuntime(Boolean supportedByRuntime) {
    this.supportedByRuntime = supportedByRuntime;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("supportedByRuntime")
  public Boolean getSupportedByRuntime() {
    return supportedByRuntime;
  }

  @JsonProperty("supportedByRuntime")
  public void setSupportedByRuntime(Boolean supportedByRuntime) {
    this.supportedByRuntime = supportedByRuntime;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentDslPreview agentDslPreview = (AgentDslPreview) o;
    return Objects.equals(this.schemaVersion, agentDslPreview.schemaVersion) &&
        Objects.equals(this.summary, agentDslPreview.summary) &&
        Objects.equals(this.dsl, agentDslPreview.dsl) &&
        Objects.equals(this.supportedByRuntime, agentDslPreview.supportedByRuntime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaVersion, summary, dsl, supportedByRuntime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentDslPreview {\n");
    
    sb.append("    schemaVersion: ").append(toIndentedString(schemaVersion)).append("\n");
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    dsl: ").append(toIndentedString(dsl)).append("\n");
    sb.append("    supportedByRuntime: ").append(toIndentedString(supportedByRuntime)).append("\n");
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
