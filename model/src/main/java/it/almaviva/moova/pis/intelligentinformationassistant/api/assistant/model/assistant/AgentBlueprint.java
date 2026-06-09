package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
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
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Structured technical specification generated from the verified Alert. It is not executable code.
 **/
@ApiModel(description = "Structured technical specification generated from the verified Alert. It is not executable code.")
@JsonTypeName("AgentBlueprint")
@JsonFormat(shape=JsonFormat.Shape.OBJECT)
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class AgentBlueprint extends HashMap<String, Object>  {
  private String agentName;
  private String description;
  public enum TriggerTypeEnum {

    EVENT(String.valueOf("EVENT")), SCHEDULE(String.valueOf("SCHEDULE")), SCHEDULED(String.valueOf("SCHEDULED"));


    private String value;

    TriggerTypeEnum (String v) {
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
    public static TriggerTypeEnum fromString(String s) {
        for (TriggerTypeEnum b : TriggerTypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static TriggerTypeEnum fromValue(String value) {
        for (TriggerTypeEnum b : TriggerTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private TriggerTypeEnum triggerType;
  private @Valid List<AgentDataSource> requiredSources = new ArrayList<>();
  private @Valid List<SuggestionTargetType> targetTypes = new ArrayList<>();
  private @Valid Map<String, Object> parameters = new HashMap<>();
  private @Valid Map<String, Object> stateRequirements = new HashMap<>();
  private @Valid Map<String, Object> suggestionIntent = new HashMap<>();

  public AgentBlueprint() {
  }

  /**
   **/
  public AgentBlueprint agentName(String agentName) {
    this.agentName = agentName;
    return this;
  }

  
  @ApiModelProperty(example = "CancelledMetroTrainsAtIonioAgent", value = "")
  @JsonProperty("agentName")
  public String getAgentName() {
    return agentName;
  }

  @JsonProperty("agentName")
  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  /**
   **/
  public AgentBlueprint description(String description) {
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
  public AgentBlueprint triggerType(TriggerTypeEnum triggerType) {
    this.triggerType = triggerType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("triggerType")
  public TriggerTypeEnum getTriggerType() {
    return triggerType;
  }

  @JsonProperty("triggerType")
  public void setTriggerType(TriggerTypeEnum triggerType) {
    this.triggerType = triggerType;
  }

  /**
   **/
  public AgentBlueprint requiredSources(List<AgentDataSource> requiredSources) {
    this.requiredSources = requiredSources;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requiredSources")
  public List<AgentDataSource> getRequiredSources() {
    return requiredSources;
  }

  @JsonProperty("requiredSources")
  public void setRequiredSources(List<AgentDataSource> requiredSources) {
    this.requiredSources = requiredSources;
  }

  public AgentBlueprint addRequiredSourcesItem(AgentDataSource requiredSourcesItem) {
    if (this.requiredSources == null) {
      this.requiredSources = new ArrayList<>();
    }

    this.requiredSources.add(requiredSourcesItem);
    return this;
  }

  public AgentBlueprint removeRequiredSourcesItem(AgentDataSource requiredSourcesItem) {
    if (requiredSourcesItem != null && this.requiredSources != null) {
      this.requiredSources.remove(requiredSourcesItem);
    }

    return this;
  }
  /**
   **/
  public AgentBlueprint targetTypes(List<SuggestionTargetType> targetTypes) {
    this.targetTypes = targetTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("targetTypes")
  public List<SuggestionTargetType> getTargetTypes() {
    return targetTypes;
  }

  @JsonProperty("targetTypes")
  public void setTargetTypes(List<SuggestionTargetType> targetTypes) {
    this.targetTypes = targetTypes;
  }

  public AgentBlueprint addTargetTypesItem(SuggestionTargetType targetTypesItem) {
    if (this.targetTypes == null) {
      this.targetTypes = new ArrayList<>();
    }

    this.targetTypes.add(targetTypesItem);
    return this;
  }

  public AgentBlueprint removeTargetTypesItem(SuggestionTargetType targetTypesItem) {
    if (targetTypesItem != null && this.targetTypes != null) {
      this.targetTypes.remove(targetTypesItem);
    }

    return this;
  }
  /**
   **/
  public AgentBlueprint parameters(Map<String, Object> parameters) {
    this.parameters = parameters;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("parameters")
  public Map<String, Object> getParameters() {
    return parameters;
  }

  @JsonProperty("parameters")
  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public AgentBlueprint putParametersItem(String key, Object parametersItem) {
    if (this.parameters == null) {
      this.parameters = new HashMap<>();
    }

    this.parameters.put(key, parametersItem);
    return this;
  }

  public AgentBlueprint removeParametersItem(String key) {
    if (this.parameters != null) {
      this.parameters.remove(key);
    }

    return this;
  }
  /**
   **/
  public AgentBlueprint stateRequirements(Map<String, Object> stateRequirements) {
    this.stateRequirements = stateRequirements;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stateRequirements")
  public Map<String, Object> getStateRequirements() {
    return stateRequirements;
  }

  @JsonProperty("stateRequirements")
  public void setStateRequirements(Map<String, Object> stateRequirements) {
    this.stateRequirements = stateRequirements;
  }

  public AgentBlueprint putStateRequirementsItem(String key, Object stateRequirementsItem) {
    if (this.stateRequirements == null) {
      this.stateRequirements = new HashMap<>();
    }

    this.stateRequirements.put(key, stateRequirementsItem);
    return this;
  }

  public AgentBlueprint removeStateRequirementsItem(String key) {
    if (this.stateRequirements != null) {
      this.stateRequirements.remove(key);
    }

    return this;
  }
  /**
   **/
  public AgentBlueprint suggestionIntent(Map<String, Object> suggestionIntent) {
    this.suggestionIntent = suggestionIntent;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("suggestionIntent")
  public Map<String, Object> getSuggestionIntent() {
    return suggestionIntent;
  }

  @JsonProperty("suggestionIntent")
  public void setSuggestionIntent(Map<String, Object> suggestionIntent) {
    this.suggestionIntent = suggestionIntent;
  }

  public AgentBlueprint putSuggestionIntentItem(String key, Object suggestionIntentItem) {
    if (this.suggestionIntent == null) {
      this.suggestionIntent = new HashMap<>();
    }

    this.suggestionIntent.put(key, suggestionIntentItem);
    return this;
  }

  public AgentBlueprint removeSuggestionIntentItem(String key) {
    if (this.suggestionIntent != null) {
      this.suggestionIntent.remove(key);
    }

    return this;
  }
  /**
   * Set the additional (undeclared) property with the specified name and value.
   * Creates the property if it does not already exist, otherwise replaces it.
   * @param key the name of the property
   * @param value the value of the property
   * @return self reference
   */
  @JsonAnySetter
  public AgentBlueprint putAdditionalProperty(String key, Object value) {
    this.put(key, value);
    return this;
  }

  /**
   * Return the additional (undeclared) properties.
   * @return the additional (undeclared) properties
   */
  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this;
  }

  /**
   * Return the additional (undeclared) property with the specified name.
   * @param key the name of the property
   * @return the additional (undeclared) property with the specified name
   */
  public Object getAdditionalProperty(String key) {
    return this.get(key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentBlueprint agentBlueprint = (AgentBlueprint) o;
    return Objects.equals(this.agentName, agentBlueprint.agentName) &&
        Objects.equals(this.description, agentBlueprint.description) &&
        Objects.equals(this.triggerType, agentBlueprint.triggerType) &&
        Objects.equals(this.requiredSources, agentBlueprint.requiredSources) &&
        Objects.equals(this.targetTypes, agentBlueprint.targetTypes) &&
        Objects.equals(this.parameters, agentBlueprint.parameters) &&
        Objects.equals(this.stateRequirements, agentBlueprint.stateRequirements) &&
        Objects.equals(this.suggestionIntent, agentBlueprint.suggestionIntent) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(agentName, description, triggerType, requiredSources, targetTypes, parameters, stateRequirements, suggestionIntent, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentBlueprint {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    agentName: ").append(toIndentedString(agentName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    triggerType: ").append(toIndentedString(triggerType)).append("\n");
    sb.append("    requiredSources: ").append(toIndentedString(requiredSources)).append("\n");
    sb.append("    targetTypes: ").append(toIndentedString(targetTypes)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
    sb.append("    stateRequirements: ").append(toIndentedString(stateRequirements)).append("\n");
    sb.append("    suggestionIntent: ").append(toIndentedString(suggestionIntent)).append("\n");
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
