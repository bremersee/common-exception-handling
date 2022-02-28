/*
 * Copyright 2018-2022 the original author or authors.
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

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The serialized exception.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
@Schema(description = "The serialized exception.")
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode(exclude = {"furtherDetails"})
@ToString
@NoArgsConstructor
public class RestApiException implements Serializable {

  private static final long serialVersionUID = 1L;

  @Schema(description = "The id of the exception.")
  @Getter
  @Setter
  private String id;

  @Schema(description = "The timestamp.")
  @Getter
  @Setter
  private OffsetDateTime timestamp;

  @Schema(description = "The http status code.")
  @Getter
  @Setter
  private Integer status;

  @Schema(description = "The http error message.")
  @Getter
  @Setter
  private String error;

  @Schema(description = "A service specific error code.")
  @Getter
  @Setter
  private String errorCode;

  @Schema(description = "Determines whether the error code is inherited from the cause or not.")
  @Setter
  private Boolean errorCodeInherited;

  @Schema(description = "A human readable exception message.")
  @Getter
  @Setter
  private String message;

  @Schema(description = "The class name of the exception.")
  @Getter
  @Setter
  private String exception;

  @Schema(description = "The name of the application.")
  @Getter
  @Setter
  private String application;

  @Schema(description = "The request path.")
  @Getter
  @Setter
  private String path;

  @Schema(description = "The handler.")
  @Getter
  @Setter
  private Handler handler;

  @Schema(description = "The stack trace.")
  @Getter
  @Setter
  private List<StackTraceItem> stackTrace;

  @Schema(description = "The cause.")
  @Getter
  @Setter
  private RestApiException cause;

  @Schema(hidden = true)
  @JsonIgnore
  private final Map<String, Object> furtherDetails = new LinkedHashMap<>();

  /**
   * Instantiates a new rest api exception.
   *
   * @param id the id
   * @param timestamp the timestamp
   * @param status the status
   * @param error the error
   * @param errorCode the error code
   * @param errorCodeInherited the error code inherited
   * @param message the message
   * @param exception the class name
   * @param application the application
   * @param path the path
   * @param handler the handler
   * @param stackTrace the stack trace
   * @param cause the cause
   */
  @Builder(toBuilder = true)
  protected RestApiException(
      String id,
      OffsetDateTime timestamp,
      Integer status,
      String error,
      String errorCode,
      Boolean errorCodeInherited,
      String message,
      String exception,
      String application,
      String path,
      Handler handler,
      List<StackTraceItem> stackTrace,
      RestApiException cause,
      Map<String, Object> furtherDetails) {
    this.id = id;
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.errorCode = errorCode;
    this.errorCodeInherited = errorCodeInherited;
    this.message = message;
    this.exception = exception;
    this.application = application;
    this.path = path;
    this.handler = handler;
    this.stackTrace = stackTrace;
    this.cause = cause;
    if (nonNull(furtherDetails)) {
      this.furtherDetails.putAll(furtherDetails);
    }
  }

  /**
   * Is the error code inherited from the cause.
   *
   * @return errorCodeInherited error code inherited
   */
  public Boolean getErrorCodeInherited() {
    return Boolean.TRUE.equals(errorCodeInherited);
  }

  /**
   * Any json setter.
   *
   * @param name the name
   * @param value the value
   */
  @JsonAnySetter
  public void furtherDetails(String name, Object value) {
    if (nonNull(name) && !name.isBlank()) {
      furtherDetails.put(name, value);
    }
  }

  /**
   * Further details map.
   *
   * @return the map
   */
  @JsonAnySetter
  public Map<String, Object> furtherDetails() {
    return furtherDetails;
  }

}

