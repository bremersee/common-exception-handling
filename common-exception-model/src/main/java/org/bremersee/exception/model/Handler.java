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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The handler where the exception occurred.
 *
 * @author Christian Bremer
 */
@Schema(description = "The handler where the exception occurred.")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
public class Handler implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The class name of the handler.
   */
  @Schema(description = "The class name of the handler.")
  private String className;

  /**
   * The method name of the handler.
   */
  @Schema(description = "The method name of the handler.")
  private String methodName;

  /**
   * The method parameters.
   */
  @JsonProperty("methodParameterTypes")
  @JacksonXmlElementWrapper(localName = "methodParameterTypes")
  @JacksonXmlProperty(localName = "methodParameterType")
  @Schema(description = "The method parameters.")
  private List<String> methodParameterTypes;

  /**
   * Instantiates a new handler.
   *
   * @param className the class name
   * @param methodName the method name
   * @param methodParameterTypes the method parameter types
   */
  @Builder(toBuilder = true)
  protected Handler(
      String className,
      String methodName,
      List<String> methodParameterTypes) {
    this.className = className;
    this.methodName = methodName;
    this.methodParameterTypes = methodParameterTypes;
  }

}

