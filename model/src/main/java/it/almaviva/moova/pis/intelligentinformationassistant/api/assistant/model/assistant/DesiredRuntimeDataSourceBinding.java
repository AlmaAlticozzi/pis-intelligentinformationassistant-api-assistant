package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;



@JsonTypeName("DesiredRuntimeDataSourceBinding")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-06-30T10:13:20.788393631Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class DesiredRuntimeDataSourceBinding   {
  private String bindingId;
  private String dataDomain;
  private DesiredRuntimeDataSourceAccessMode accessMode;
  private DesiredRuntimeDataSourceConnectorType connectorType;
  private String connectorRef;
  private String inputModel;
  private String inputSchemaVersion;
  private String bindingSchemaVersion;
  private String operationRef;
  private @Valid Map<String, Object> _configuration = new HashMap<>();
  private Boolean required = true;
  private @Valid Set<@Size(max = 150)String> failoverConnectorRefs = new LinkedHashSet<>();

  public DesiredRuntimeDataSourceBinding() {
  }

  @JsonCreator
  public DesiredRuntimeDataSourceBinding(
    @JsonProperty(required = true, value = "bindingId") String bindingId,
    @JsonProperty(required = true, value = "dataDomain") String dataDomain,
    @JsonProperty(required = true, value = "accessMode") DesiredRuntimeDataSourceAccessMode accessMode,
    @JsonProperty(required = true, value = "connectorType") DesiredRuntimeDataSourceConnectorType connectorType,
    @JsonProperty(required = true, value = "connectorRef") String connectorRef,
    @JsonProperty(required = true, value = "inputModel") String inputModel,
    @JsonProperty(required = true, value = "bindingSchemaVersion") String bindingSchemaVersion
  ) {
    this.bindingId = bindingId;
    this.dataDomain = dataDomain;
    this.accessMode = accessMode;
    this.connectorType = connectorType;
    this.connectorRef = connectorRef;
    this.inputModel = inputModel;
    this.bindingSchemaVersion = bindingSchemaVersion;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding bindingId(String bindingId) {
    this.bindingId = bindingId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "bindingId")
  @NotNull  @Size(min=1,max=100)public String getBindingId() {
    return bindingId;
  }

  @JsonProperty(required = true, value = "bindingId")
  public void setBindingId(String bindingId) {
    this.bindingId = bindingId;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding dataDomain(String dataDomain) {
    this.dataDomain = dataDomain;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "dataDomain")
  @NotNull  @Size(min=1,max=100)public String getDataDomain() {
    return dataDomain;
  }

  @JsonProperty(required = true, value = "dataDomain")
  public void setDataDomain(String dataDomain) {
    this.dataDomain = dataDomain;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding accessMode(DesiredRuntimeDataSourceAccessMode accessMode) {
    this.accessMode = accessMode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "accessMode")
  @NotNull public DesiredRuntimeDataSourceAccessMode getAccessMode() {
    return accessMode;
  }

  @JsonProperty(required = true, value = "accessMode")
  public void setAccessMode(DesiredRuntimeDataSourceAccessMode accessMode) {
    this.accessMode = accessMode;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding connectorType(DesiredRuntimeDataSourceConnectorType connectorType) {
    this.connectorType = connectorType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "connectorType")
  @NotNull public DesiredRuntimeDataSourceConnectorType getConnectorType() {
    return connectorType;
  }

  @JsonProperty(required = true, value = "connectorType")
  public void setConnectorType(DesiredRuntimeDataSourceConnectorType connectorType) {
    this.connectorType = connectorType;
  }

  /**
   * Logical registered connector reference, never a physical endpoint or credential.
   **/
  public DesiredRuntimeDataSourceBinding connectorRef(String connectorRef) {
    this.connectorRef = connectorRef;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Logical registered connector reference, never a physical endpoint or credential.")
  @JsonProperty(required = true, value = "connectorRef")
  @NotNull  @Size(min=1,max=150)public String getConnectorRef() {
    return connectorRef;
  }

  @JsonProperty(required = true, value = "connectorRef")
  public void setConnectorRef(String connectorRef) {
    this.connectorRef = connectorRef;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding inputModel(String inputModel) {
    this.inputModel = inputModel;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "inputModel")
  @NotNull  @Size(min=1,max=150)public String getInputModel() {
    return inputModel;
  }

  @JsonProperty(required = true, value = "inputModel")
  public void setInputModel(String inputModel) {
    this.inputModel = inputModel;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding inputSchemaVersion(String inputSchemaVersion) {
    this.inputSchemaVersion = inputSchemaVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("inputSchemaVersion")
   @Size(max=100)public String getInputSchemaVersion() {
    return inputSchemaVersion;
  }

  @JsonProperty("inputSchemaVersion")
  public void setInputSchemaVersion(String inputSchemaVersion) {
    this.inputSchemaVersion = inputSchemaVersion;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding bindingSchemaVersion(String bindingSchemaVersion) {
    this.bindingSchemaVersion = bindingSchemaVersion;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "bindingSchemaVersion")
  @NotNull  @Size(min=1,max=100)public String getBindingSchemaVersion() {
    return bindingSchemaVersion;
  }

  @JsonProperty(required = true, value = "bindingSchemaVersion")
  public void setBindingSchemaVersion(String bindingSchemaVersion) {
    this.bindingSchemaVersion = bindingSchemaVersion;
  }

  /**
   **/
  public DesiredRuntimeDataSourceBinding operationRef(String operationRef) {
    this.operationRef = operationRef;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("operationRef")
   @Size(max=150)public String getOperationRef() {
    return operationRef;
  }

  @JsonProperty("operationRef")
  public void setOperationRef(String operationRef) {
    this.operationRef = operationRef;
  }

  /**
   * Governed connector configuration. Credentials, arbitrary URLs, broker addresses and unrestricted topics are forbidden.
   **/
  public DesiredRuntimeDataSourceBinding _configuration(Map<String, Object> _configuration) {
    this._configuration = _configuration;
    return this;
  }

  
  @ApiModelProperty(value = "Governed connector configuration. Credentials, arbitrary URLs, broker addresses and unrestricted topics are forbidden.")
  @JsonProperty("configuration")
  public Map<String, Object> getConfiguration() {
    return _configuration;
  }

  @JsonProperty("configuration")
  public void setConfiguration(Map<String, Object> _configuration) {
    this._configuration = _configuration;
  }

  public DesiredRuntimeDataSourceBinding putConfigurationItem(String key, Object _configurationItem) {
    if (this._configuration == null) {
      this._configuration = new HashMap<>();
    }

    this._configuration.put(key, _configurationItem);
    return this;
  }

  public DesiredRuntimeDataSourceBinding removeConfigurationItem(String key) {
    if (this._configuration != null) {
      this._configuration.remove(key);
    }

    return this;
  }
  /**
   **/
  public DesiredRuntimeDataSourceBinding required(Boolean required) {
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
  public DesiredRuntimeDataSourceBinding failoverConnectorRefs(Set<@Size(max = 150)String> failoverConnectorRefs) {
    this.failoverConnectorRefs = failoverConnectorRefs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("failoverConnectorRefs")
   @Size(max=5)public Set< @Size(max=150)String> getFailoverConnectorRefs() {
    return failoverConnectorRefs;
  }

  @JsonProperty("failoverConnectorRefs")
  @JsonDeserialize(as = LinkedHashSet.class)
  public void setFailoverConnectorRefs(Set<@Size(max = 150)String> failoverConnectorRefs) {
    this.failoverConnectorRefs = failoverConnectorRefs;
  }

  public DesiredRuntimeDataSourceBinding addFailoverConnectorRefsItem(String failoverConnectorRefsItem) {
    if (this.failoverConnectorRefs == null) {
      this.failoverConnectorRefs = new LinkedHashSet<>();
    }

    this.failoverConnectorRefs.add(failoverConnectorRefsItem);
    return this;
  }

  public DesiredRuntimeDataSourceBinding removeFailoverConnectorRefsItem(String failoverConnectorRefsItem) {
    if (failoverConnectorRefsItem != null && this.failoverConnectorRefs != null) {
      this.failoverConnectorRefs.remove(failoverConnectorRefsItem);
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
    DesiredRuntimeDataSourceBinding desiredRuntimeDataSourceBinding = (DesiredRuntimeDataSourceBinding) o;
    return Objects.equals(this.bindingId, desiredRuntimeDataSourceBinding.bindingId) &&
        Objects.equals(this.dataDomain, desiredRuntimeDataSourceBinding.dataDomain) &&
        Objects.equals(this.accessMode, desiredRuntimeDataSourceBinding.accessMode) &&
        Objects.equals(this.connectorType, desiredRuntimeDataSourceBinding.connectorType) &&
        Objects.equals(this.connectorRef, desiredRuntimeDataSourceBinding.connectorRef) &&
        Objects.equals(this.inputModel, desiredRuntimeDataSourceBinding.inputModel) &&
        Objects.equals(this.inputSchemaVersion, desiredRuntimeDataSourceBinding.inputSchemaVersion) &&
        Objects.equals(this.bindingSchemaVersion, desiredRuntimeDataSourceBinding.bindingSchemaVersion) &&
        Objects.equals(this.operationRef, desiredRuntimeDataSourceBinding.operationRef) &&
        Objects.equals(this._configuration, desiredRuntimeDataSourceBinding._configuration) &&
        Objects.equals(this.required, desiredRuntimeDataSourceBinding.required) &&
        Objects.equals(this.failoverConnectorRefs, desiredRuntimeDataSourceBinding.failoverConnectorRefs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bindingId, dataDomain, accessMode, connectorType, connectorRef, inputModel, inputSchemaVersion, bindingSchemaVersion, operationRef, _configuration, required, failoverConnectorRefs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesiredRuntimeDataSourceBinding {\n");
    
    sb.append("    bindingId: ").append(toIndentedString(bindingId)).append("\n");
    sb.append("    dataDomain: ").append(toIndentedString(dataDomain)).append("\n");
    sb.append("    accessMode: ").append(toIndentedString(accessMode)).append("\n");
    sb.append("    connectorType: ").append(toIndentedString(connectorType)).append("\n");
    sb.append("    connectorRef: ").append(toIndentedString(connectorRef)).append("\n");
    sb.append("    inputModel: ").append(toIndentedString(inputModel)).append("\n");
    sb.append("    inputSchemaVersion: ").append(toIndentedString(inputSchemaVersion)).append("\n");
    sb.append("    bindingSchemaVersion: ").append(toIndentedString(bindingSchemaVersion)).append("\n");
    sb.append("    operationRef: ").append(toIndentedString(operationRef)).append("\n");
    sb.append("    _configuration: ").append(toIndentedString(_configuration)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    failoverConnectorRefs: ").append(toIndentedString(failoverConnectorRefs)).append("\n");
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
