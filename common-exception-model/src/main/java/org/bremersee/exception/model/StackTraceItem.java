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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
@SuppressWarnings("SameNameButDifferent")
@Schema(description = "A stack trace element of an exception.")
@Valid
@JacksonXmlRootElement(localName = "StackTraceItem")
@XmlRootElement(name = "StackTraceItem")
@XmlType(name = "stackTraceItemType")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
public class StackTraceItem implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("declaringClass")
  @JacksonXmlProperty(localName = "declaringClass")
  @Schema(description = "The declaring class.")
  private String declaringClass;

  @JsonProperty("methodName")
  @JacksonXmlProperty(localName = "methodName")
  @Schema(description = "The method name.")
  private String methodName;

  @JsonProperty("fileName")
  @JacksonXmlProperty(localName = "fileName")
  @Schema(description = "The file name.")
  private String fileName;

  @JsonProperty("lineNumber")
  @JacksonXmlProperty(localName = "lineNumber")
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
  public StackTraceItem(
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