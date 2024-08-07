/*
 * Copyright 2020-2022 the original author or authors.
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

package org.bremersee.exception.spring.boot.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.exception.RestApiExceptionMapperProperties;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMapping;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;

/**
 * Configuration properties for the rest api exception handler or resolver.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass(RestApiExceptionMapperProperties.class)
@ConfigurationProperties(prefix = "bremersee.exception-mapping")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RestApiExceptionMapperBootProperties {

  /**
   * The request paths the handler is responsible for. Default is empty.
   */
  private List<String> apiPaths = new ArrayList<>();

  /**
   * The default values of a rest api exception that will be set, if a value is not detected. The
   * default values are:
   * <table style="border: 1px solid">
   * <thead>
   * <tr>
   * <th style="border: 1px solid">attribute</th>
   * <th style="border: 1px solid">value</th>
   * </tr>
   * </thead>
   * <tbody>
   * <tr>
   * <td style="border: 1px solid">status</td>
   * <td style="border: 1px solid">500</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">message</td>
   * <td style="border: 1px solid">Internal Server Error</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">code</td>
   * <td style="border: 1px solid">UNSPECIFIED</td>
   * </tr>
   * </tbody>
   * </table>
   */
  private ExceptionMappingImpl defaultExceptionMapping;

  /**
   * Values ​​of errors whose values ​​can not be determined automatically. The name of the
   * exception can be a package, too (e. g. org.bremersee.foobar.*).
   *
   * <p>Examples application.yml:
   * <pre>
   * bremersee:
   *   exception-mapping:
   *     exception-mappings:
   *     - exception-class-name: org.springframework.security.access.AccessDeniedException
   *       status: 403
   *       message: Forbidden
   *       code: XYZ:0815
   *     - exception-class-name: javax.persistence.EntityNotFoundException
   *       status: 404
   *       message: Not Found
   *       code: GEN:404
   * </pre>
   */
  private List<ExceptionMappingImpl> exceptionMappings = new ArrayList<>();

  /**
   * The default configuration of the exception mapping.
   *
   * <table style="border: 1px solid">
   * <thead>
   * <tr>
   * <th style="border: 1px solid">attribute</th>
   * <th style="border: 1px solid">value</th>
   * </tr>
   * </thead>
   * <tbody>
   * <tr>
   * <td style="border: 1px solid">includeException</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeApplicationName</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includePath</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeHandler</td>
   * <td style="border: 1px solid">false</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeStackTrace</td>
   * <td style="border: 1px solid">false</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">includeCause</td>
   * <td style="border: 1px solid">true</td>
   * </tr>
   * <tr>
   * <td style="border: 1px solid">evaluateAnnotationFirst</td>
   * <td style="border: 1px solid">false</td>
   * </tr>
   * </tbody>
   * </table>
   *
   * <p>If two values of a key are available (e. g. per annotation (see {@link
   * org.springframework.web.bind.annotation.ResponseStatus}) and member attribute) it is possible
   * with {@code evaluateAnnotationFirst} to specify which one should be used.
   */
  private ExceptionMappingConfigImpl defaultExceptionMappingConfig;

  /**
   * Specifies mapping configuration per exception class.  The name of the exception can be a
   * package, too (e. g. org.bremersee.foobar.*).
   *
   * <p>Examples application.yml:
   * <pre>
   * bremersee:
   *   exception-mapping:
   *     exception-mapping-configs:
   *     - exception-class-name: org.springframework.security.access.AccessDeniedException
   *       include-exception-class-name: false
   *       include-handler: true
   *       include-cause: true
   *     - exception-class-name: org.springframework.*
   *       include-exception-class-name: true
   *       include-handler: true
   *       include-cause: false
   * </pre>
   */
  private List<ExceptionMappingConfigImpl> exceptionMappingConfigs = new ArrayList<>();

  /**
   * Instantiates rest api exception mapper properties.
   */
  public RestApiExceptionMapperBootProperties() {

    defaultExceptionMapping = new ExceptionMappingImpl();
    defaultExceptionMapping.setExceptionClassName("*");
    defaultExceptionMapping.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    defaultExceptionMapping.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

    defaultExceptionMappingConfig = new ExceptionMappingConfigImpl();
    defaultExceptionMappingConfig.setExceptionClassName("*");

    exceptionMappings.add(new ExceptionMappingImpl(
        IllegalArgumentException.class.getName(),
        HttpStatus.BAD_REQUEST,
        null));

    exceptionMappings.add(new ExceptionMappingImpl(
        "org.springframework.security.access.AccessDeniedException",
        HttpStatus.FORBIDDEN,
        null));

    exceptionMappings.add(new ExceptionMappingImpl(
        "javax.persistence.EntityNotFoundException",
        HttpStatus.NOT_FOUND,
        null));
  }

  /**
   * To rest api exception mapper properties rest api exception mapper properties.
   *
   * @return the rest api exception mapper properties
   */
  public RestApiExceptionMapperProperties toRestApiExceptionMapperProperties() {
    return RestApiExceptionMapperProperties.builder()
        .defaultExceptionMapping(getDefaultExceptionMapping())
        .exceptionMappings(getExceptionMappings())
        .defaultExceptionMappingConfig(getDefaultExceptionMappingConfig())
        .exceptionMappingConfigs(getExceptionMappingConfigs())
        .build();
  }

  /**
   * The exception mapping.
   */
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExceptionMappingImpl implements ExceptionMapping {

    /**
     * Instantiates a new exception mapping.
     *
     * @param exceptionClassName the exception class name
     * @param httpStatus the http status
     * @param code the code
     */
    public ExceptionMappingImpl(String exceptionClassName, HttpStatus httpStatus, String code) {
      this.exceptionClassName = exceptionClassName;
      if (httpStatus != null) {
        this.status = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
      }
      this.code = code;
    }

    /**
     * The exception class name.
     */
    @Getter
    private String exceptionClassName;

    private int status;

    /**
     * The exception message.
     */
    @Getter
    private String message;

    /**
     * The exception code.
     */
    @Getter
    private String code;

    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
      if (HttpStatus.resolve(status) == null) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
      }
      return status;
    }

  }

  /**
   * The exception mapping config.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExceptionMappingConfigImpl implements ExceptionMappingConfig {

    private String exceptionClassName;

    private Boolean includeMessage = true;

    private Boolean includeException = true;

    private Boolean includeApplicationName = true;

    private Boolean includePath = true;

    private Boolean includeHandler = false;

    private Boolean includeStackTrace = false;

    private Boolean includeCause = false;

    private Boolean evaluateAnnotationFirst = false;

  }

}
