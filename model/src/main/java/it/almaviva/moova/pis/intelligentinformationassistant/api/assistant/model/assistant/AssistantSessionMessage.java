package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
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



@JsonTypeName("AssistantSessionMessage")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantSessionMessage   {
  private String id;
  public enum RoleEnum {

    USER(String.valueOf("USER")), ASSISTANT(String.valueOf("ASSISTANT")), SYSTEM(String.valueOf("SYSTEM")), TOOL(String.valueOf("TOOL"));


    private String value;

    RoleEnum (String v) {
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
    public static RoleEnum fromString(String s) {
        for (RoleEnum b : RoleEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static RoleEnum fromValue(String value) {
        for (RoleEnum b : RoleEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private RoleEnum role;
  private String content;
  private @Valid Map<String, Object> structuredContent = new HashMap<>();
  private OffsetDateTime createdAt;

  public AssistantSessionMessage() {
  }

  @JsonCreator
  public AssistantSessionMessage(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "role") RoleEnum role,
    @JsonProperty(required = true, value = "content") String content,
    @JsonProperty(required = true, value = "createdAt") OffsetDateTime createdAt
  ) {
    this.id = id;
    this.role = role;
    this.content = content;
    this.createdAt = createdAt;
  }

  /**
   **/
  public AssistantSessionMessage id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "856daf9c-1d60-42ee-9db6-fd7e45d6b5d4", required = true, value = "")
  @JsonProperty(required = true, value = "id")
  @NotNull  @Size(max=50)public String getId() {
    return id;
  }

  @JsonProperty(required = true, value = "id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public AssistantSessionMessage role(RoleEnum role) {
    this.role = role;
    return this;
  }

  
  @ApiModelProperty(example = "USER", required = true, value = "")
  @JsonProperty(required = true, value = "role")
  @NotNull public RoleEnum getRole() {
    return role;
  }

  @JsonProperty(required = true, value = "role")
  public void setRole(RoleEnum role) {
    this.role = role;
  }

  /**
   **/
  public AssistantSessionMessage content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(example = "La corsa AV 304 è in ritardo?", required = true, value = "")
  @JsonProperty(required = true, value = "content")
  @NotNull public String getContent() {
    return content;
  }

  @JsonProperty(required = true, value = "content")
  public void setContent(String content) {
    this.content = content;
  }

  /**
   **/
  public AssistantSessionMessage structuredContent(Map<String, Object> structuredContent) {
    this.structuredContent = structuredContent;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("structuredContent")
  public Map<String, Object> getStructuredContent() {
    return structuredContent;
  }

  @JsonProperty("structuredContent")
  public void setStructuredContent(Map<String, Object> structuredContent) {
    this.structuredContent = structuredContent;
  }

  public AssistantSessionMessage putStructuredContentItem(String key, Object structuredContentItem) {
    if (this.structuredContent == null) {
      this.structuredContent = new HashMap<>();
    }

    this.structuredContent.put(key, structuredContentItem);
    return this;
  }

  public AssistantSessionMessage removeStructuredContentItem(String key) {
    if (this.structuredContent != null) {
      this.structuredContent.remove(key);
    }

    return this;
  }
  /**
   **/
  public AssistantSessionMessage createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "createdAt")
  @NotNull public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty(required = true, value = "createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssistantSessionMessage assistantSessionMessage = (AssistantSessionMessage) o;
    return Objects.equals(this.id, assistantSessionMessage.id) &&
        Objects.equals(this.role, assistantSessionMessage.role) &&
        Objects.equals(this.content, assistantSessionMessage.content) &&
        Objects.equals(this.structuredContent, assistantSessionMessage.structuredContent) &&
        Objects.equals(this.createdAt, assistantSessionMessage.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, role, content, structuredContent, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantSessionMessage {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    structuredContent: ").append(toIndentedString(structuredContent)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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
