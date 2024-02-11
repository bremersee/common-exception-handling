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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A stack trace element of an exception.
 *
 * @author Christian Bremer
 */
@Schema(description = "A stack trace element of an exception.")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
public class StackTraceItem implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The declaring class.
   */
  @Schema(description = "The declaring class.")
  private String declaringClass;

  /**
   * The method name.
   */
  @Schema(description = "The method name.")
  private String methodName;

  /**
   * The file name.
   */
  @Schema(description = "The file name.")
  private String fileName;

  /**
   * The line number.
   */
  @Schema(description = "The line number.")
  private Integer lineNumber;

  /**
   * Instantiates a new stack trace item.
   *
   * @param declaringClass the declaring class
   * @param methodName the method name
   * @param fileName the file name
   * @param lineNumber the line number
   */
  @Builder(toBuilder = true)
  protected StackTraceItem(
      String declaringClass,
      String methodName,
      String fileName,
      Integer lineNumber) {
    this.declaringClass = declaringClass;
    this.methodName = methodName;
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

}