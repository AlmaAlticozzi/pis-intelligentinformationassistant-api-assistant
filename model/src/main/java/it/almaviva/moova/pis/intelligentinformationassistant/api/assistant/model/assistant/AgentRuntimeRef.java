package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Logical runtime reference assigned by the Agent Orchestrator. This is the canonical runtime identity in the shared-runtime model. Kubernetes metadata, when present, is optional diagnostic information.
 **/
@ApiModel(description = "Logical runtime reference assigned by the Agent Orchestrator. This is the canonical runtime identity in the shared-runtime model. Kubernetes metadata, when present, is optional diagnostic information.")
@JsonTypeName("AgentRuntimeRef")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-09T00:00:00Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentRuntimeRef   {
  private String orchestratorInstanceId;
  private String runtimeClass;
  public enum RuntimeModeEnum {

    SHARED_RUNTIME(String.valueOf("SHARED_RUNTIME")), DEDICATED_WORKLOAD(String.valueOf("DEDICATED_WORKLOAD")), EXTERNAL_RUNTIME(String.valueOf("EXTERNAL_RUNTIME"));


    private String value;

    RuntimeModeEnum (String v) {
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

    public static RuntimeModeEnum fromString(String s) {
        for (RuntimeModeEnum b : RuntimeModeEnum.values()) {
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static RuntimeModeEnum fromValue(String value) {
        for (RuntimeModeEnum b : RuntimeModeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private RuntimeModeEnum runtimeMode;
  private String assignmentId;
  private String loadedArtifactHash;
  private OffsetDateTime loadedAt;
  private OffsetDateTime lastSyncAt;

  public AgentRuntimeRef() {
  }

  public AgentRuntimeRef orchestratorInstanceId(String orchestratorInstanceId) {
    this.orchestratorInstanceId = orchestratorInstanceId;
    return this;
  }

  @ApiModelProperty(example = "iia-agentorchestrator-0", value = "")
  @JsonProperty("orchestratorInstanceId")
  public String getOrchestratorInstanceId() {
    return orchestratorInstanceId;
  }

  @JsonProperty("orchestratorInstanceId")
  public void setOrchestratorInstanceId(String orchestratorInstanceId) {
    this.orchestratorInstanceId = orchestratorInstanceId;
  }

  public AgentRuntimeRef runtimeClass(String runtimeClass) {
    this.runtimeClass = runtimeClass;
    return this;
  }

  @ApiModelProperty(example = "STANDARD_DSL_RUNTIME", value = "")
  @JsonProperty("runtimeClass")
  public String getRuntimeClass() {
    return runtimeClass;
  }

  @JsonProperty("runtimeClass")
  public void setRuntimeClass(String runtimeClass) {
    this.runtimeClass = runtimeClass;
  }

  public AgentRuntimeRef runtimeMode(RuntimeModeEnum runtimeMode) {
    this.runtimeMode = runtimeMode;
    return this;
  }

  @ApiModelProperty(example = "SHARED_RUNTIME", value = "")
  @JsonProperty("runtimeMode")
  public RuntimeModeEnum getRuntimeMode() {
    return runtimeMode;
  }

  @JsonProperty("runtimeMode")
  public void setRuntimeMode(RuntimeModeEnum runtimeMode) {
    this.runtimeMode = runtimeMode;
  }

  public AgentRuntimeRef assignmentId(String assignmentId) {
    this.assignmentId = assignmentId;
    return this;
  }

  @ApiModelProperty(example = "ASG-AGDF2026251400000001-001", value = "")
  @JsonProperty("assignmentId")
  public String getAssignmentId() {
    return assignmentId;
  }

  @JsonProperty("assignmentId")
  public void setAssignmentId(String assignmentId) {
    this.assignmentId = assignmentId;
  }

  public AgentRuntimeRef loadedArtifactHash(String loadedArtifactHash) {
    this.loadedArtifactHash = loadedArtifactHash;
    return this;
  }

  @ApiModelProperty(example = "sha256:5f2b6c...", value = "")
  @JsonProperty("loadedArtifactHash")
  public String getLoadedArtifactHash() {
    return loadedArtifactHash;
  }

  @JsonProperty("loadedArtifactHash")
  public void setLoadedArtifactHash(String loadedArtifactHash) {
    this.loadedArtifactHash = loadedArtifactHash;
  }

  public AgentRuntimeRef loadedAt(OffsetDateTime loadedAt) {
    this.loadedAt = loadedAt;
    return this;
  }

  @ApiModelProperty(value = "")
  @JsonProperty("loadedAt")
  public OffsetDateTime getLoadedAt() {
    return loadedAt;
  }

  @JsonProperty("loadedAt")
  public void setLoadedAt(OffsetDateTime loadedAt) {
    this.loadedAt = loadedAt;
  }

  public AgentRuntimeRef lastSyncAt(OffsetDateTime lastSyncAt) {
    this.lastSyncAt = lastSyncAt;
    return this;
  }

  @ApiModelProperty(value = "")
  @JsonProperty("lastSyncAt")
  public OffsetDateTime getLastSyncAt() {
    return lastSyncAt;
  }

  @JsonProperty("lastSyncAt")
  public void setLastSyncAt(OffsetDateTime lastSyncAt) {
    this.lastSyncAt = lastSyncAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentRuntimeRef agentRuntimeRef = (AgentRuntimeRef) o;
    return Objects.equals(this.orchestratorInstanceId, agentRuntimeRef.orchestratorInstanceId) &&
        Objects.equals(this.runtimeClass, agentRuntimeRef.runtimeClass) &&
        Objects.equals(this.runtimeMode, agentRuntimeRef.runtimeMode) &&
        Objects.equals(this.assignmentId, agentRuntimeRef.assignmentId) &&
        Objects.equals(this.loadedArtifactHash, agentRuntimeRef.loadedArtifactHash) &&
        Objects.equals(this.loadedAt, agentRuntimeRef.loadedAt) &&
        Objects.equals(this.lastSyncAt, agentRuntimeRef.lastSyncAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orchestratorInstanceId, runtimeClass, runtimeMode, assignmentId, loadedArtifactHash, loadedAt, lastSyncAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentRuntimeRef {\n");
    sb.append("    orchestratorInstanceId: ").append(toIndentedString(orchestratorInstanceId)).append("\n");
    sb.append("    runtimeClass: ").append(toIndentedString(runtimeClass)).append("\n");
    sb.append("    runtimeMode: ").append(toIndentedString(runtimeMode)).append("\n");
    sb.append("    assignmentId: ").append(toIndentedString(assignmentId)).append("\n");
    sb.append("    loadedArtifactHash: ").append(toIndentedString(loadedArtifactHash)).append("\n");
    sb.append("    loadedAt: ").append(toIndentedString(loadedAt)).append("\n");
    sb.append("    lastSyncAt: ").append(toIndentedString(lastSyncAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}
