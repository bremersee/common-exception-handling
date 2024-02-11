/*
 * Copyright 2022 the original author or authors.
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

package org.bremersee.exception;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.immutables.value.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

/**
 * The rest api exception mapper properties.
 *
 * @author Christian Bremer
 */
@Value.Immutable
@Valid
public interface RestApiExceptionMapperProperties {

  /**
   * Creates rest api exception mapper properties builder.
   *
   * @return the builder
   */
  static ImmutableRestApiExceptionMapperProperties.Builder builder() {
    return ImmutableRestApiExceptionMapperProperties.builder();
  }

  /**
   * Gets default exception mapping.
   *
   * @return the default exception mapping
   */
  @Value.Default
  @NotNull
  default ExceptionMapping getDefaultExceptionMapping() {
    return ExceptionMapping.builder()
        .message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .exceptionClassName("*")
        .build();
  }

  /**
   * Gets exception mappings.
   *
   * @return the exception mappings
   */
  @Value.Default
  @NotNull
  default List<ExceptionMapping> getExceptionMappings() {
    return List.of(
        ExceptionMapping.builder()
            .exceptionClassName(IllegalArgumentException.class.getName())
            .status(HttpStatus.BAD_REQUEST.value())
            .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .build(),
        ExceptionMapping.builder()
            .exceptionClassName("org.springframework.security.access.AccessDeniedException")
            .status(HttpStatus.FORBIDDEN.value())
            .message(HttpStatus.FORBIDDEN.getReasonPhrase())
            .build(),
        ExceptionMapping.builder()
            .exceptionClassName("javax.persistence.EntityNotFoundException")
            .status(HttpStatus.NOT_FOUND.value())
            .message(HttpStatus.NOT_FOUND.getReasonPhrase())
            .build()
    );
  }

  /**
   * Gets default exception mapping config.
   *
   * @return the default exception mapping config
   */
  @Value.Default
  @NotNull
  default ExceptionMappingConfig getDefaultExceptionMappingConfig() {
    return ExceptionMappingConfig.builder()
        .exceptionClassName("*")
        .build();
  }

  /**
   * Gets exception mapping configs.
   *
   * @return the exception mapping configs
   */
  @Value.Default
  @NotNull
  default List<ExceptionMappingConfig> getExceptionMappingConfigs() {
    return List.of();
  }

  /**
   * Find exception mapping.
   *
   * @param throwable the throwable
   * @return the exception mapping
   */
  @NotNull
  default ExceptionMapping findExceptionMapping(Throwable throwable) {
    return getExceptionMappings()
        .stream()
        .filter(exceptionMapping -> matches(
            throwable, exceptionMapping.getExceptionClassName()))
        .findFirst()
        .orElseGet(this::getDefaultExceptionMapping);
  }

  /**
   * Find exception mapping config.
   *
   * @param throwable the throwable
   * @return the exception mapping config
   */
  @NotNull
  default ExceptionMappingConfig findExceptionMappingConfig(Throwable throwable) {
    return getExceptionMappingConfigs()
        .stream()
        .filter(exceptionMappingConfig -> matches(
            throwable, exceptionMappingConfig.getExceptionClassName()))
        .findFirst()
        .orElseGet(this::getDefaultExceptionMappingConfig);
  }

  private boolean matches(Throwable throwable, String exceptionClassName) {
    if (throwable == null || exceptionClassName == null) {
      return false;
    }
    if (throwable.getClass().getName().equals(exceptionClassName)) {
      return true;
    }
    if (exceptionClassName.endsWith(".*")) {
      String packagePrefix = exceptionClassName.substring(0, exceptionClassName.length() - 1);
      if (throwable.getClass().getName().startsWith(packagePrefix)) {
        return true;
      }
    }
    if (matches(throwable.getCause(), exceptionClassName)) {
      return true;
    }
    return matches(throwable.getClass().getSuperclass(), exceptionClassName);
  }

  private boolean matches(Class<?> exceptionClass, String exceptionClassName) {
    if (exceptionClass == null || exceptionClassName == null) {
      return false;
    }
    if (exceptionClass.getName().equals(exceptionClassName)) {
      return true;
    }
    if (exceptionClassName.endsWith(".*")) {
      String packagePrefix = exceptionClassName.substring(0, exceptionClassName.length() - 1);
      if (exceptionClass.getName().startsWith(packagePrefix)) {
        return true;
      }
    }
    return matches(exceptionClass.getSuperclass(), exceptionClassName);
  }

  /**
   * The exception mapping.
   */
  @Value.Immutable
  interface ExceptionMapping {

    /**
     * Builder immutable exception mapping . builder.
     *
     * @return the immutable exception mapping . builder
     */
    static ImmutableExceptionMapping.Builder builder() {
      return ImmutableExceptionMapping.builder();
    }

    /**
     * Gets exception class name.
     *
     * @return the exception class name
     */
    @NotNull
    String getExceptionClassName();

    /**
     * Gets status.
     *
     * @return the status
     */
    @Value.Default
    default int getStatus() {
      return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    @Nullable
    String getMessage();

    /**
     * Gets code.
     *
     * @return the code
     */
    @Nullable
    String getCode();

  }

  /**
   * The exception mapping config.
   */
  @Value.Immutable
  interface ExceptionMappingConfig {

    /**
     * Creates builder.
     *
     * @return the builder
     */
    static ImmutableExceptionMappingConfig.Builder builder() {
      return ImmutableExceptionMappingConfig.builder();
    }

    /**
     * Gets exception class name.
     *
     * @return the exception class name
     */
    @NotNull
    String getExceptionClassName();

    @Value.Default
    @NotNull
    default Boolean getIncludeMessage() {
      return true;
    }

    /**
     * Is include exception class name.
     *
     * @return the boolean
     */
    @Value.Default
    @NotNull
    default Boolean getIncludeException() {
      return true;
    }

    /**
     * Is include application name.
     *
     * @return the boolean
     */
    @Value.Default
    @NotNull
    default Boolean getIncludeApplicationName() {
      return true;
    }

    /**
     * Is include path.
     *
     * @return the boolean
     */
    @Value.Default
    @NotNull
    default Boolean getIncludePath() {
      return true;
    }

    /**
     * Is include handler.
     *
     * @return the boolean
     */
    @Value.Default
    @NotNull
    default Boolean getIncludeHandler() {
      return false;
    }

    /**
     * Is include stack trace.
     *
     * @return the boolean
     */
    @Value.Default
    @NotNull
    default Boolean getIncludeStackTrace() {
      return false;
    }

    /**
     * Is include cause.
     *
     * @return the boolean
     */
    @Value.Default
    @NotNull
    default Boolean getIncludeCause() {
      return false;
    }

    /**
     * Is evaluate annotation first.
     *
     * @return the boolean
     */
    @Value.Default
    @NotNull
    default Boolean getEvaluateAnnotationFirst() {
      return false;
    }

  }

}
