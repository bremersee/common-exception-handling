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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The handler where the exception occurred.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
@Schema(description = "The handler where the exception occurred.")
@Valid
@JacksonXmlRootElement(localName = "Handler")
@XmlRootElement(name = "Handler")
@XmlType(name = "handlerType")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Handler implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("className")
  @JacksonXmlProperty(localName = "className")
  private String className = null;

  @JsonProperty("methodName")
  @JacksonXmlProperty(localName = "methodName")
  private String methodName = null;

  @JsonProperty("methodParameterTypes")
  @JacksonXmlElementWrapper(localName = "methodParameterTypes")
  @JacksonXmlProperty(localName = "methodParameterType")
  @XmlElementWrapper(name = "methodParameterTypes")
  @XmlElement(name = "methodParameterType")
  private List<String> methodParameterTypes = null;

  /**
   * Instantiates a new handler.
   *
   * @param className the class name
   * @param methodName the method name
   * @param methodParameterTypes the method parameter types
   */
  @Builder(toBuilder = true)
  @SuppressWarnings("unused")
  public Handler(
      String className,
      String methodName,
      List<String> methodParameterTypes) {
    this.className = className;
    this.methodName = methodName;
    this.methodParameterTypes = methodParameterTypes;
  }

  /**
   * The class name of the handler.
   *
   * @return className class name
   */
  @Schema(description = "The class name of the handler.")
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
   * The method name of the handler.
   *
   * @return methodName method name
   */
  @Schema(description = "The method name of the handler.")
  public String getMethodName() {
    return methodName;
  }

  /**
   * Sets method name.
   *
   * @param methodName the method name
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * The method parameters.
   *
   * @return methodParameterTypes method parameter types
   */
  @Schema(description = "The method parameters.")
  public List<String> getMethodParameterTypes() {
    return methodParameterTypes;
  }

  /**
   * Sets method parameter types.
   *
   * @param methodParameterTypes the method parameter types
   */
  public void setMethodParameterTypes(List<String> methodParameterTypes) {
    this.methodParameterTypes = methodParameterTypes;
  }

}

