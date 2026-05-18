package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("AssistantSessionCreateRequest")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantSessionCreateRequest   {
  private String title;
  private @Valid Map<String, Object> uiContext = new HashMap<>();

  public AssistantSessionCreateRequest() {
  }

  /**
   **/
  public AssistantSessionCreateRequest title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(example = "Operational questions for Genova Brignole", value = "")
  @JsonProperty("title")
   @Size(max=200)public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Optional UI context useful for follow-up questions.
   **/
  public AssistantSessionCreateRequest uiContext(Map<String, Object> uiContext) {
    this.uiContext = uiContext;
    return this;
  }

  
  @ApiModelProperty(value = "Optional UI context useful for follow-up questions.")
  @JsonProperty("uiContext")
  public Map<String, Object> getUiContext() {
    return uiContext;
  }

  @JsonProperty("uiContext")
  public void setUiContext(Map<String, Object> uiContext) {
    this.uiContext = uiContext;
  }

  public AssistantSessionCreateRequest putUiContextItem(String key, Object uiContextItem) {
    if (this.uiContext == null) {
      this.uiContext = new HashMap<>();
    }

    this.uiContext.put(key, uiContextItem);
    return this;
  }

  public AssistantSessionCreateRequest removeUiContextItem(String key) {
    if (this.uiContext != null) {
      this.uiContext.remove(key);
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
    AssistantSessionCreateRequest assistantSessionCreateRequest = (AssistantSessionCreateRequest) o;
    return Objects.equals(this.title, assistantSessionCreateRequest.title) &&
        Objects.equals(this.uiContext, assistantSessionCreateRequest.uiContext);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, uiContext);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantSessionCreateRequest {\n");
    
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    uiContext: ").append(toIndentedString(uiContext)).append("\n");
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
