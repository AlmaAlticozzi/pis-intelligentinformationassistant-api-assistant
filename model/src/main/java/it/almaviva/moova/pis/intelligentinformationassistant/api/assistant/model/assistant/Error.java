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



@JsonTypeName("Error")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-05-18T06:40:20.070283797Z[Etc/UTC]", comments = "Generator version: 7.23.0-SNAPSHOT")
public class Error   {
  private String code;
  private String title;
  private String detail;
  private String source;
  private String traceId;

  public Error() {
  }

  @JsonCreator
  public Error(
    @JsonProperty(required = true, value = "code") String code,
    @JsonProperty(required = true, value = "title") String title
  ) {
    this.code = code;
    this.title = title;
  }

  /**
   **/
  public Error code(String code) {
    this.code = code;
    return this;
  }

  
  @ApiModelProperty(example = "IIA-400-001", required = true, value = "")
  @JsonProperty(required = true, value = "code")
  @NotNull public String getCode() {
    return code;
  }

  @JsonProperty(required = true, value = "code")
  public void setCode(String code) {
    this.code = code;
  }

  /**
   **/
  public Error title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(example = "Invalid request", required = true, value = "")
  @JsonProperty(required = true, value = "title")
  @NotNull public String getTitle() {
    return title;
  }

  @JsonProperty(required = true, value = "title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   **/
  public Error detail(String detail) {
    this.detail = detail;
    return this;
  }

  
  @ApiModelProperty(example = "The lookbackHours parameter is mandatory.", value = "")
  @JsonProperty("detail")
  public String getDetail() {
    return detail;
  }

  @JsonProperty("detail")
  public void setDetail(String detail) {
    this.detail = detail;
  }

  /**
   **/
  public Error source(String source) {
    this.source = source;
    return this;
  }

  
  @ApiModelProperty(example = "lookbackHours", value = "")
  @JsonProperty("source")
  public String getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(String source) {
    this.source = source;
  }

  /**
   **/
  public Error traceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  
  @ApiModelProperty(example = "9c39746e-d77a-481f-9db1-37cd4784c854", value = "")
  @JsonProperty("traceId")
  public String getTraceId() {
    return traceId;
  }

  @JsonProperty("traceId")
  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Error error = (Error) o;
    return Objects.equals(this.code, error.code) &&
        Objects.equals(this.title, error.title) &&
        Objects.equals(this.detail, error.detail) &&
        Objects.equals(this.source, error.source) &&
        Objects.equals(this.traceId, error.traceId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, title, detail, source, traceId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
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
