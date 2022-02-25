/*
 * Copyright 2019 the original author or authors.
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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.util.ClassUtils.getUserClass;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.bremersee.exception.annotation.ErrorCode;
import org.bremersee.exception.model.Handler;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.exception.model.StackTraceItem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

/**
 * The default implementation of a rest api exception mapper.
 *
 * @author Christian Bremer
 */
@Slf4j
public class RestApiExceptionMapperForWeb implements RestApiExceptionMapper {

  @Getter(AccessLevel.PROTECTED)
  private final RestApiExceptionMapperProperties properties;

  @Getter(AccessLevel.PROTECTED)
  private final String applicationName;

  /**
   * Instantiates a new rest api exception mapper.
   *
   * @param properties the properties
   * @param applicationName the application name
   */
  public RestApiExceptionMapperForWeb(
      RestApiExceptionMapperProperties properties,
      String applicationName) {
    this.properties = properties;
    this.applicationName = applicationName;
  }

  @Override
  public List<String> getApiPaths() {
    return properties.getApiPaths();
  }

  @Override
  public HttpStatus detectHttpStatus(Throwable exception, Object handler) {

    return Optional.of(exception)
        .flatMap(exc -> {
          if (exc instanceof HttpStatusAware) {
            return fromStatus(((HttpStatusAware) exc).status());
          }
          if (exc instanceof ResponseStatusException) {
            return Optional.of(((ResponseStatusException) exc).getStatus());
          }
          return Optional.empty();
        })
        .or(() -> Optional
            .ofNullable(findMergedAnnotation(exception.getClass(), ResponseStatus.class))
            .map(ResponseStatus::code))
        .or(() -> findHandlerMethod(handler)
            .map(method -> findMergedAnnotation(method, ResponseStatus.class))
            .map(ResponseStatus::code))
        .or(() -> fromStatus(properties.findExceptionMapping(exception).getStatus()))
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  protected Optional<HttpStatus> fromStatus(Integer status) {
    return Optional.ofNullable(status)
        .map(HttpStatus::resolve);
  }

  @Override
  public RestApiException build(
      Throwable exception,
      String requestPath,
      Object handler) {

    ExceptionMappingConfig config = getProperties().findExceptionMappingConfig(exception);
    HttpStatus httpStatus = detectHttpStatus(exception, handler);

    RestApiException restApiException = new RestApiException();
    if (httpStatus.is5xxServerError()) {
      restApiException.setId(UUID.randomUUID().toString());
    }

    restApiException.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

    restApiException.setStatus(httpStatus.value());

    restApiException.setError(httpStatus.getReasonPhrase());

    restApiException = setErrorCode(restApiException, exception, handler, config);

    restApiException = setMessage(restApiException, exception, handler, config);

    restApiException = setClassName(restApiException, exception, config);

    restApiException = setApplication(restApiException, config);

    restApiException = setPath(restApiException, requestPath, config);

    restApiException = setHandler(restApiException, handler, config);

    restApiException = setStackTrace(restApiException, exception.getStackTrace(), config);

    return setCause(restApiException, exception, config);
  }

  /**
   * Find the handler class.
   *
   * @param handler the handler
   * @return the class
   */
  protected Optional<Class<?>> findHandlerClass(Object handler) {
    return Optional.ofNullable(handler)
        .map(h -> {
          if (h instanceof HandlerMethod) {
            return ((HandlerMethod) h).getBean().getClass();
          }
          return h.getClass();
        });
  }

  /**
   * Find the handler method.
   *
   * @param handler the handler
   * @return the method
   */
  protected Optional<Method> findHandlerMethod(Object handler) {
    return Optional.ofNullable(handler)
        .filter(h -> h instanceof HandlerMethod)
        .map(h -> ((HandlerMethod) h).getMethod());
  }

  protected RestApiException setErrorCode(
      @NotNull RestApiException restApiException,
      @NotNull Throwable exception,
      Object handler,
      @NotNull ExceptionMappingConfig config) {

    return Optional.of(exception)
        .filter(exc -> (exc instanceof ErrorCodeAware) && !config.isEvaluateAnnotationFirst())
        .map(exc -> ((ErrorCodeAware) exc).getErrorCode())
        .or(() -> Optional
            .ofNullable(findMergedAnnotation(getUserClass(exception), ErrorCode.class))
            .map(ErrorCode::value))
        .or(() -> findHandlerMethod(handler)
            .map(method -> findMergedAnnotation(method, ErrorCode.class))
            .map(ErrorCode::value))
        .or(() -> Optional.ofNullable(getProperties().findExceptionMapping(exception).getCode()))
        .filter(errorCode -> !errorCode.isBlank())
        .map(errorCode -> restApiException.toBuilder()
            .errorCode(errorCode)
            .errorCodeInherited(false)
            .build())
        .orElse(restApiException);
  }

  protected RestApiException setMessage(
      @NotNull RestApiException restApiException,
      @NotNull Throwable exception,
      Object handler,
      @NotNull ExceptionMappingConfig config) {

    if (!config.isIncludeMessage()) {
      return restApiException;
    }
    return Optional.ofNullable(exception.getMessage())
        .filter(msg -> !msg.isBlank() && !config.isEvaluateAnnotationFirst())
        .or(() -> Optional
            .ofNullable(findMergedAnnotation(getUserClass(exception), ResponseStatus.class))
            .map(ResponseStatus::reason))
        .or(() -> findHandlerMethod(handler)
            .map(method -> findMergedAnnotation(method, ResponseStatus.class))
            .map(ResponseStatus::reason))
        .or(() -> Optional.ofNullable(getProperties().findExceptionMapping(exception).getMessage()))
        .map(msg -> restApiException.toBuilder().message(msg).build())
        .orElse(restApiException);
  }

  protected RestApiException setClassName(
      @NotNull RestApiException restApiException,
      @NotNull Throwable exception,
      @NotNull ExceptionMappingConfig config) {

    if (!config.isIncludeExceptionClassName()) {
      return restApiException;
    }
    return restApiException.toBuilder()
        .className(getUserClass(exception).getName())
        .build();
  }

  protected RestApiException setApplication(
      @NotNull RestApiException restApiException,
      @NotNull ExceptionMappingConfig config) {

    if (!config.isIncludeApplicationName()) {
      return restApiException;
    }
    return restApiException.toBuilder().application(getApplicationName()).build();
  }

  protected RestApiException setPath(
      @NotNull RestApiException restApiException,
      String path,
      @NotNull ExceptionMappingConfig config) {

    if (!config.isIncludePath() || isNull(path)) {
      return restApiException;
    }
    return restApiException.toBuilder().path(path).build();
  }

  protected RestApiException setHandler(
      @NotNull RestApiException restApiException,
      Object handler,
      @NotNull ExceptionMappingConfig config) {

    if (!config.isIncludeHandler() || isNull(handler)) {
      return restApiException;
    }
    return findHandlerMethod(handler)
        .map(method -> Handler.builder()
            .methodName(method.getName())
            .methodParameterTypes(Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.toList()))
            .build())
        .flatMap(h -> findHandlerClass(handler)
            .map(cls -> h.toBuilder().className(cls.getName()).build()))
        .map(h -> restApiException.toBuilder().handler(h).build())
        .orElse(restApiException);
  }

  protected RestApiException setStackTrace(
      @NotNull RestApiException restApiException,
      StackTraceElement[] stackTrace,
      @NotNull ExceptionMappingConfig config) {

    if (!config.isIncludeStackTrace() || isNull(stackTrace) || stackTrace.length == 0) {
      return restApiException;
    }
    return restApiException.toBuilder()
        .stackTrace(Arrays.stream(stackTrace)
            .map(st -> StackTraceItem
                .builder()
                .declaringClass(st.getClassName())
                .fileName(st.getFileName())
                .lineNumber(st.getLineNumber())
                .methodName(st.getMethodName())
                .build())
            .collect(Collectors.toList()))
        .build();
  }

  protected RestApiException setCause(
      @NotNull RestApiException restApiException,
      Throwable exception,
      @NotNull ExceptionMappingConfig config) {

    if (!config.isIncludeCause() || isNull(exception)) {
      return restApiException;
    }

    return Optional.of(exception)
        .filter(exc -> exc instanceof RestApiExceptionAware)
        .map(exc -> ((RestApiExceptionAware) exc).getRestApiException())
        .map(cause -> reconfigureRestApiException(cause, config))
        .or(() -> Optional.ofNullable(exception.getCause())
            .map(cause -> {
              RestApiException rae = new RestApiException();
              rae = setErrorCode(rae, cause, null, config);
              rae = setMessage(rae, cause, null, config);
              rae = setClassName(rae, cause, config);
              rae = setStackTrace(rae, cause.getStackTrace(), config);
              rae = setCause(rae, cause.getCause(), config);
              return rae;
            }))
        .map(cause -> {
          String causeErrorCode = cause.getErrorCode();
          if (nonNull(causeErrorCode) && !causeErrorCode.isBlank()
              && !RestApiExceptionConstants.NO_ERROR_CODE_VALUE.equals(causeErrorCode)) {
            return restApiException.toBuilder()
                .errorCode(causeErrorCode)
                .errorCodeInherited(true)
                .cause(cause)
                .build();
          }
          return restApiException.toBuilder().cause(cause).build();
        })
        .orElse(restApiException);
  }

  protected RestApiException reconfigureRestApiException(
      @NotNull RestApiException source,
      @NotNull ExceptionMappingConfig config) {

    RestApiException target = new RestApiException();
    target.setId(source.getId());
    target.setTimestamp(source.getTimestamp());
    target.setStatus(source.getStatus());
    target.setError(source.getError());
    if (nonNull(source.getErrorCode()) && !source.getErrorCode().isBlank()
        && !RestApiExceptionConstants.NO_ERROR_CODE_VALUE.equals(source.getErrorCode())) {
      target.setErrorCode(source.getErrorCode());
      target.setErrorCodeInherited(source.getErrorCodeInherited());
    }
    if (config.isIncludeMessage()) {
      target.setMessage(source.getMessage());
    }
    if (config.isIncludeExceptionClassName()) {
      target.setClassName(source.getClassName());
    }
    if (config.isIncludeApplicationName()) {
      target.setApplication(source.getApplication());
    }
    if (config.isIncludePath()) {
      target.setPath(source.getPath());
    }
    if (config.isIncludeHandler()) {
      target.setHandler(source.getHandler());
    }
    if (config.isIncludeStackTrace()) {
      target.setStackTrace(source.getStackTrace());
    }
    if (config.isIncludeCause() && nonNull(source.getCause())) {
      target.setCause(reconfigureRestApiException(source.getCause(), config));
    }
    source.furtherDetails().forEach(target::furtherDetails);
    return target;
  }

}
