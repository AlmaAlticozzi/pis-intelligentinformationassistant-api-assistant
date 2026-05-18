package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AssistantSessionMessage;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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



@JsonTypeName("AssistantSession")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AssistantSession   {
  private String id;
  private String title;
  public enum StatusEnum {

    ACTIVE(String.valueOf("ACTIVE")), CLOSED(String.valueOf("CLOSED")), ERROR(String.valueOf("ERROR"));


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
  private String lastIntent;
  private @Valid Map<String, Object> lastEntities = new HashMap<>();
  private @Valid List<@Valid AssistantSessionMessage> messages = new ArrayList<>();
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public AssistantSession() {
  }

  @JsonCreator
  public AssistantSession(
    @JsonProperty(required = true, value = "id") String id,
    @JsonProperty(required = true, value = "status") StatusEnum status,
    @JsonProperty(required = true, value = "createdAt") OffsetDateTime createdAt
  ) {
    this.id = id;
    this.status = status;
    this.createdAt = createdAt;
  }

  /**
   **/
  public AssistantSession id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "ASSS2026251400000001", required = true, value = "")
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
  public AssistantSession title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(example = "Operational questions", value = "")
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   **/
  public AssistantSession status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "ACTIVE", required = true, value = "")
  @JsonProperty(required = true, value = "status")
  @NotNull public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   **/
  public AssistantSession lastIntent(String lastIntent) {
    this.lastIntent = lastIntent;
    return this;
  }

  
  @ApiModelProperty(example = "GET_JOURNEY_STATUS", value = "")
  @JsonProperty("lastIntent")
  public String getLastIntent() {
    return lastIntent;
  }

  @JsonProperty("lastIntent")
  public void setLastIntent(String lastIntent) {
    this.lastIntent = lastIntent;
  }

  /**
   **/
  public AssistantSession lastEntities(Map<String, Object> lastEntities) {
    this.lastEntities = lastEntities;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastEntities")
  public Map<String, Object> getLastEntities() {
    return lastEntities;
  }

  @JsonProperty("lastEntities")
  public void setLastEntities(Map<String, Object> lastEntities) {
    this.lastEntities = lastEntities;
  }

  public AssistantSession putLastEntitiesItem(String key, Object lastEntitiesItem) {
    if (this.lastEntities == null) {
      this.lastEntities = new HashMap<>();
    }

    this.lastEntities.put(key, lastEntitiesItem);
    return this;
  }

  public AssistantSession removeLastEntitiesItem(String key) {
    if (this.lastEntities != null) {
      this.lastEntities.remove(key);
    }

    return this;
  }
  /**
   **/
  public AssistantSession messages(List<@Valid AssistantSessionMessage> messages) {
    this.messages = messages;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("messages")
  @Valid public List<@Valid AssistantSessionMessage> getMessages() {
    return messages;
  }

  @JsonProperty("messages")
  public void setMessages(List<@Valid AssistantSessionMessage> messages) {
    this.messages = messages;
  }

  public AssistantSession addMessagesItem(AssistantSessionMessage messagesItem) {
    if (this.messages == null) {
      this.messages = new ArrayList<>();
    }

    this.messages.add(messagesItem);
    return this;
  }

  public AssistantSession removeMessagesItem(AssistantSessionMessage messagesItem) {
    if (messagesItem != null && this.messages != null) {
      this.messages.remove(messagesItem);
    }

    return this;
  }
  /**
   **/
  public AssistantSession createdAt(OffsetDateTime createdAt) {
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

  /**
   **/
  public AssistantSession updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssistantSession assistantSession = (AssistantSession) o;
    return Objects.equals(this.id, assistantSession.id) &&
        Objects.equals(this.title, assistantSession.title) &&
        Objects.equals(this.status, assistantSession.status) &&
        Objects.equals(this.lastIntent, assistantSession.lastIntent) &&
        Objects.equals(this.lastEntities, assistantSession.lastEntities) &&
        Objects.equals(this.messages, assistantSession.messages) &&
        Objects.equals(this.createdAt, assistantSession.createdAt) &&
        Objects.equals(this.updatedAt, assistantSession.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, status, lastIntent, lastEntities, messages, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssistantSession {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    lastIntent: ").append(toIndentedString(lastIntent)).append("\n");
    sb.append("    lastEntities: ").append(toIndentedString(lastEntities)).append("\n");
    sb.append("    messages: ").append(toIndentedString(messages)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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
