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



@JsonTypeName("AgentKubernetesRuntimeRef")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentKubernetesRuntimeRef   {
  private String namespace;
  public enum ResourceKindEnum {

    POD(String.valueOf("POD")), JOB(String.valueOf("JOB")), DEPLOYMENT(String.valueOf("DEPLOYMENT")), CRONJOB(String.valueOf("CRONJOB"));


    private String value;

    ResourceKindEnum (String v) {
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
    public static ResourceKindEnum fromString(String s) {
        for (ResourceKindEnum b : ResourceKindEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static ResourceKindEnum fromValue(String value) {
        for (ResourceKindEnum b : ResourceKindEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private ResourceKindEnum resourceKind;
  private String resourceName;
  private String podName;
  private String containerImage;
  private String podPhase;
  private String containerState;
  private Integer restartCount;
  private String nodeName;

  public AgentKubernetesRuntimeRef() {
  }

  /**
   **/
  public AgentKubernetesRuntimeRef namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  
  @ApiModelProperty(example = "pis-iia-agents", value = "")
  @JsonProperty("namespace")
  public String getNamespace() {
    return namespace;
  }

  @JsonProperty("namespace")
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef resourceKind(ResourceKindEnum resourceKind) {
    this.resourceKind = resourceKind;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("resourceKind")
  public ResourceKindEnum getResourceKind() {
    return resourceKind;
  }

  @JsonProperty("resourceKind")
  public void setResourceKind(ResourceKindEnum resourceKind) {
    this.resourceKind = resourceKind;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef resourceName(String resourceName) {
    this.resourceName = resourceName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("resourceName")
  public String getResourceName() {
    return resourceName;
  }

  @JsonProperty("resourceName")
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef podName(String podName) {
    this.podName = podName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("podName")
  public String getPodName() {
    return podName;
  }

  @JsonProperty("podName")
  public void setPodName(String podName) {
    this.podName = podName;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef containerImage(String containerImage) {
    this.containerImage = containerImage;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("containerImage")
  public String getContainerImage() {
    return containerImage;
  }

  @JsonProperty("containerImage")
  public void setContainerImage(String containerImage) {
    this.containerImage = containerImage;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef podPhase(String podPhase) {
    this.podPhase = podPhase;
    return this;
  }

  
  @ApiModelProperty(example = "Running", value = "")
  @JsonProperty("podPhase")
  public String getPodPhase() {
    return podPhase;
  }

  @JsonProperty("podPhase")
  public void setPodPhase(String podPhase) {
    this.podPhase = podPhase;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef containerState(String containerState) {
    this.containerState = containerState;
    return this;
  }

  
  @ApiModelProperty(example = "running", value = "")
  @JsonProperty("containerState")
  public String getContainerState() {
    return containerState;
  }

  @JsonProperty("containerState")
  public void setContainerState(String containerState) {
    this.containerState = containerState;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef restartCount(Integer restartCount) {
    this.restartCount = restartCount;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("restartCount")
  public Integer getRestartCount() {
    return restartCount;
  }

  @JsonProperty("restartCount")
  public void setRestartCount(Integer restartCount) {
    this.restartCount = restartCount;
  }

  /**
   **/
  public AgentKubernetesRuntimeRef nodeName(String nodeName) {
    this.nodeName = nodeName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("nodeName")
  public String getNodeName() {
    return nodeName;
  }

  @JsonProperty("nodeName")
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentKubernetesRuntimeRef agentKubernetesRuntimeRef = (AgentKubernetesRuntimeRef) o;
    return Objects.equals(this.namespace, agentKubernetesRuntimeRef.namespace) &&
        Objects.equals(this.resourceKind, agentKubernetesRuntimeRef.resourceKind) &&
        Objects.equals(this.resourceName, agentKubernetesRuntimeRef.resourceName) &&
        Objects.equals(this.podName, agentKubernetesRuntimeRef.podName) &&
        Objects.equals(this.containerImage, agentKubernetesRuntimeRef.containerImage) &&
        Objects.equals(this.podPhase, agentKubernetesRuntimeRef.podPhase) &&
        Objects.equals(this.containerState, agentKubernetesRuntimeRef.containerState) &&
        Objects.equals(this.restartCount, agentKubernetesRuntimeRef.restartCount) &&
        Objects.equals(this.nodeName, agentKubernetesRuntimeRef.nodeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, resourceKind, resourceName, podName, containerImage, podPhase, containerState, restartCount, nodeName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentKubernetesRuntimeRef {\n");
    
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    resourceKind: ").append(toIndentedString(resourceKind)).append("\n");
    sb.append("    resourceName: ").append(toIndentedString(resourceName)).append("\n");
    sb.append("    podName: ").append(toIndentedString(podName)).append("\n");
    sb.append("    containerImage: ").append(toIndentedString(containerImage)).append("\n");
    sb.append("    podPhase: ").append(toIndentedString(podPhase)).append("\n");
    sb.append("    containerState: ").append(toIndentedString(containerState)).append("\n");
    sb.append("    restartCount: ").append(toIndentedString(restartCount)).append("\n");
    sb.append("    nodeName: ").append(toIndentedString(nodeName)).append("\n");
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
