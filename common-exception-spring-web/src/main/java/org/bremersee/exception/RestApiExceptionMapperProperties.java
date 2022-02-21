/*
 * Copyright 2020 the original author or authors.
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
 * @author Christian Bremer
 */
@Value.Immutable
@Valid
public interface RestApiExceptionMapperProperties {

  static ImmutableRestApiExceptionMapperProperties.Builder builder() {
    return ImmutableRestApiExceptionMapperProperties.builder();
  }

  @Value.Default
  @NotNull
  default List<String> getApiPaths() {
    return List.of();
  }

  @Value.Default
  @NotNull
  default ExceptionMapping getDefaultExceptionMapping() {
    return ExceptionMapping.builder()
        .message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .exceptionClassName("*")
        .build();
  }

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

  @Value.Default
  @NotNull
  default ExceptionMappingConfig getDefaultExceptionMappingConfig() {
    return ExceptionMappingConfig.builder().build();
  }

  @Value.Default
  @NotNull
  default List<ExceptionMappingConfig> getExceptionMappingConfigs() {
    return List.of();
  }

  @NotNull
  default ExceptionMapping findExceptionMapping(Throwable throwable) {
    return getExceptionMappings()
        .stream()
        .filter(exceptionMapping -> matches(
            throwable, exceptionMapping.getExceptionClassName()))
        .findFirst()
        .orElseGet(this::getDefaultExceptionMapping);
  }

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

  @Value.Immutable
  interface ExceptionMapping {

    static ImmutableExceptionMapping.Builder builder() {
      return ImmutableExceptionMapping.builder();
    }

    @NotNull
    String getExceptionClassName();

    @Value.Default
    default int getStatus() {
      return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    @Nullable
    String getMessage();

    @Nullable
    String getCode();

  }

  @Value.Immutable
  interface ExceptionMappingConfig {

    static ImmutableExceptionMappingConfig.Builder builder() {
      return ImmutableExceptionMappingConfig.builder();
    }

    @Nullable
    String getExceptionClassName();

    @Value.Default
    default boolean isIncludeExceptionClassName() {
      return true;
    }

    @Value.Default
    default boolean isIncludeApplicationName() {
      return true;
    }

    @Value.Default
    default boolean isIncludePath() {
      return true;
    }

    @Value.Default
    default boolean isIncludeHandler() {
      return false;
    }

    @Value.Default
    default boolean isIncludeStackTrace() {
      return false;
    }

    @Value.Default
    default boolean isIncludeCause() {
      return true;
    }

    @Value.Default
    default boolean isEvaluateAnnotationFirst() {
      return false;
    }

  }

}
