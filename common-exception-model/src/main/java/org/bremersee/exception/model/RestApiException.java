/*
 * Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.exception.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bremersee.xml.adapter.OffsetDateTimeXmlAdapter;

/**
 * The serialized exception.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
@Schema(description = "The serialized exception.")
@Valid
@JsonTypeInfo(include = As.EXISTING_PROPERTY, use = Id.CLASS, property = "_type", visible = true)
@JacksonXmlRootElement(localName = "RestApiException")
@XmlRootElement(name = "RestApiException")
@XmlType(name = "restApiExceptionType")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Handler.class, StackTraceItem.class})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class RestApiException implements Serializable {

  private static final long serialVersionUID = 1L;

  @Schema(
      name = "_type",
      description = "The type specifier, must always be "
          + "'org.bremersee.exception.model.RestApiException'.",
      example = "org.bremersee.exception.model.RestApiException")
  @JsonProperty("_type")
  @JacksonXmlProperty(localName = "_type", isAttribute = true)
  @XmlAttribute(name = "_type")
  private String type = RestApiException.class.getName();

  @JsonProperty("id")
  @JacksonXmlProperty(localName = "id")
  private String id = null;

  @JsonProperty("timestamp")
  @JacksonXmlProperty(localName = "timestamp")
  @XmlJavaTypeAdapter(OffsetDateTimeXmlAdapter.class)
  private OffsetDateTime timestamp;

  @JsonProperty("message")
  @JacksonXmlProperty(localName = "message")
  private String message = "No message present.";

  @JsonProperty("errorCode")
  @JacksonXmlProperty(localName = "errorCode")
  private String errorCode = null;

  @JsonProperty("errorCodeInherited")
  @JacksonXmlProperty(localName = "errorCodeInherited")
  private Boolean errorCodeInherited = Boolean.FALSE;

  @JsonProperty("className")
  @JacksonXmlProperty(localName = "className")
  private String className = null;

  @JsonProperty("application")
  @JacksonXmlProperty(localName = "application")
  private String application = null;

  @JsonProperty("path")
  @JacksonXmlProperty(localName = "path")
  private String path = null;

  @JsonProperty("handler")
  @JacksonXmlProperty(localName = "handler")
  private Handler handler = null;

  @JsonProperty("stackTrace")
  @JacksonXmlElementWrapper(localName = "stackTrace")
  @JacksonXmlProperty(localName = "stackTraceItem")
  @XmlElementWrapper(name = "stackTrace")
  @XmlElement(name = "stackTraceItem")
  private List<StackTraceItem> stackTrace = null;

  @JsonProperty("cause")
  @JacksonXmlProperty(localName = "cause")
  private RestApiException cause = null;

  /**
   * Instantiates a new rest api exception.
   *
   * @param id the id
   * @param timestamp the timestamp
   * @param message the message
   * @param errorCode the error code
   * @param errorCodeInherited the error code inherited
   * @param className the class name
   * @param application the application
   * @param path the path
   * @param handler the handler
   * @param stackTrace the stack trace
   * @param cause the cause
   */
  @Builder(toBuilder = true)
  @SuppressWarnings("unused")
  public RestApiException(
      String id,
      OffsetDateTime timestamp,
      String message,
      String errorCode,
      Boolean errorCodeInherited,
      String className,
      String application,
      String path,
      Handler handler,
      List<StackTraceItem> stackTrace,
      RestApiException cause) {
    this.id = id;
    this.timestamp = timestamp;
    this.message = message;
    this.errorCode = errorCode;
    this.errorCodeInherited = Boolean.TRUE.equals(errorCodeInherited);
    this.className = className;
    this.application = application;
    this.path = path;
    this.handler = handler;
    this.stackTrace = stackTrace;
    this.cause = cause;
  }

  /**
   * The id of the exception.
   *
   * @return id id
   */
  @Schema(description = "The id of the exception.")
  public String getId() {
    return id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The timestamp.
   *
   * @return timestamp timestamp
   */
  @Schema(description = "The timestamp.")
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  /**
   * Sets timestamp.
   *
   * @param timestamp the timestamp
   */
  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * A human readable exception message.
   *
   * @return message message
   */
  @Schema(description = "A human readable exception message.")
  public String getMessage() {
    return message;
  }

  /**
   * Sets message.
   *
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * A service specific error code.
   *
   * @return errorCode error code
   */
  @Schema(description = "A service specific error code.")
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Sets error code.
   *
   * @param errorCode the error code
   */
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Is the error code inherited from the cause.
   *
   * @return errorCodeInherited error code inherited
   */
  @Schema(description = "Is the error code inherited from the cause?")
  public Boolean getErrorCodeInherited() {
    return errorCodeInherited;
  }

  /**
   * Sets error code inherited.
   *
   * @param errorCodeInherited the error code inherited
   */
  public void setErrorCodeInherited(Boolean errorCodeInherited) {
    this.errorCodeInherited = Boolean.TRUE.equals(errorCodeInherited);
  }

  /**
   * The class name of the exception.
   *
   * @return className class name
   */
  @Schema(description = "The class name of the exception.")
  public String getClassName() {
    return className;
  }

  /**
   * Sets class name.
   *
   * @param className the class name
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * The name of the application.
   *
   * @return application application
   */
  @Schema(description = "The name of the application.")
  public String getApplication() {
    return application;
  }

  /**
   * Sets application.
   *
   * @param application the application
   */
  public void setApplication(String application) {
    this.application = application;
  }

  /**
   * The request path.
   *
   * @return path path
   */
  @Schema(description = "The request path.")
  public String getPath() {
    return path;
  }

  /**
   * Sets path.
   *
   * @param path the path
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Get handler.
   *
   * @return handler handler
   */
  @Schema(description = "The handler.")
  public Handler getHandler() {
    return handler;
  }

  /**
   * Sets handler.
   *
   * @param handler the handler
   */
  public void setHandler(Handler handler) {
    this.handler = handler;
  }

  /**
   * The stack trace.
   *
   * @return stackTrace stack trace
   */
  @Schema(description = "The stack trace.")
  public List<StackTraceItem> getStackTrace() {
    return stackTrace;
  }

  /**
   * Sets stack trace.
   *
   * @param stackTrace the stack trace
   */
  public void setStackTrace(List<StackTraceItem> stackTrace) {
    this.stackTrace = stackTrace;
  }

  /**
   * Get cause.
   *
   * @return cause cause
   */
  @Schema(description = "The cause.")
  public RestApiException getCause() {
    return cause;
  }

  /**
   * Sets cause.
   *
   * @param cause the cause
   */
  public void setCause(RestApiException cause) {
    this.cause = cause;
  }

}

