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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
   * Gets api paths.
   *
   * @return the api paths
   */
  @Value.Default
  @NotNull
  default List<String> getApiPaths() {
    return List.of();
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
    return ExceptionMappingConfig.builder().build();
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
   * Find exception mapping exception mapping.
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
   * Find exception mapping config exception mapping config.
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

  private boolean matches(final Throwable throwable, final String exceptionClassName) {
    if (throwable == null || exceptionClassName == null) {
      return false;
    }
    if (throwable.getClass().getName().equals(exceptionClassName)) {
      return true;
    }
    if (exceptionClassName.endsWith(".*")) {
      final String packagePrefix = exceptionClassName.substring(0, exceptionClassName.length() - 1);
      if (throwable.getClass().getName().startsWith(packagePrefix)) {
        return true;
      }
    }
    if (matches(throwable.getCause(), exceptionClassName)) {
      return true;
    }
    return matches(throwable.getClass().getSuperclass(), exceptionClassName);
  }

  private boolean matches(final Class<?> exceptionClass, final String exceptionClassName) {
    if (exceptionClass == null || exceptionClassName == null) {
      return false;
    }
    if (exceptionClass.getName().equals(exceptionClassName)) {
      return true;
    }
    if (exceptionClassName.endsWith(".*")) {
      final String packagePrefix = exceptionClassName.substring(0, exceptionClassName.length() - 1);
      if (exceptionClass.getName().startsWith(packagePrefix)) {
        return true;
      }
    }
    return matches(exceptionClass.getSuperclass(), exceptionClassName);
  }

  /**
   * The interface Exception mapping.
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
   * The interface Exception mapping config.
   */
  @Value.Immutable
  interface ExceptionMappingConfig {

    /**
     * Builder immutable exception mapping config . builder.
     *
     * @return the immutable exception mapping config . builder
     */
    static ImmutableExceptionMappingConfig.Builder builder() {
      return ImmutableExceptionMappingConfig.builder();
    }

    /**
     * Gets exception class name.
     *
     * @return the exception class name
     */
    @Nullable
    String getExceptionClassName();

    /**
     * Is include exception class name boolean.
     *
     * @return the boolean
     */
    @Value.Default
    default boolean isIncludeExceptionClassName() {
      return true;
    }

    /**
     * Is include application name boolean.
     *
     * @return the boolean
     */
    @Value.Default
    default boolean isIncludeApplicationName() {
      return true;
    }

    /**
     * Is include path boolean.
     *
     * @return the boolean
     */
    @Value.Default
    default boolean isIncludePath() {
      return true;
    }

    /**
     * Is include handler boolean.
     *
     * @return the boolean
     */
    @Value.Default
    default boolean isIncludeHandler() {
      return false;
    }

    /**
     * Is include stack trace boolean.
     *
     * @return the boolean
     */
    @Value.Default
    default boolean isIncludeStackTrace() {
      return false;
    }

    /**
     * Is include cause boolean.
     *
     * @return the boolean
     */
    @Value.Default
    default boolean isIncludeCause() {
      return true;
    }

    /**
     * Is evaluate annotation first boolean.
     *
     * @return the boolean
     */
    @Value.Default
    default boolean isEvaluateAnnotationFirst() {
      return false;
    }

  }

}
