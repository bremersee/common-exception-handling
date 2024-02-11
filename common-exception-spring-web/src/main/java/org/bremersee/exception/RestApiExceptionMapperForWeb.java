/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.bremersee.exception.annotation.ErrorCode;
import org.bremersee.exception.model.Handler;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.exception.model.StackTraceItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

/**
 * The implementation of a rest api exception mapper for spring web.
 *
 * @author Christian Bremer
 */
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

  /**
   * Detect http status http status.
   *
   * @param exception the exception
   * @param handler the handler
   * @return the http status
   */
  protected HttpStatusCode detectHttpStatus(Throwable exception, Object handler) {
    return Optional.of(exception)
        .flatMap(exc -> {
          if (exc instanceof HttpStatusAware hsa) {
            return Optional.of(HttpStatusCode.valueOf(hsa.status()));
          }
          if (exc instanceof ResponseStatusException rse) {
            return Optional.of(rse.getStatusCode());
          }
          return Optional.empty();
        })
        .or(() -> findHandlerMethod(handler)
            .map(method -> findMergedAnnotation(method, ResponseStatus.class))
            .map(ResponseStatus::code))
        .or(() -> findHandlerClass(handler)
            .map(handlerClass -> findMergedAnnotation(handlerClass, ResponseStatus.class))
            .map(ResponseStatus::code))
        .or(() -> Optional
            .ofNullable(findMergedAnnotation(exception.getClass(), ResponseStatus.class))
            .map(ResponseStatus::code))
        .or(() -> fromStatus(properties.findExceptionMapping(exception).getStatus()))
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * From status optional.
   *
   * @param status the status
   * @return the optional
   */
  protected Optional<HttpStatusCode> fromStatus(Integer status) {
    return Optional.ofNullable(status)
        .map(HttpStatus::valueOf);
  }

  @Nullable
  protected String getError(Throwable throwable, HttpStatusCode httpStatusCode) {
    if ((throwable instanceof ResponseStatusException rse)
        && !(ObjectUtils.isEmpty(rse.getReason()))) {
      return rse.getReason();
    }
    if (httpStatusCode instanceof HttpStatus hs) {
      return hs.getReasonPhrase();
    }
    return null;
  }

  @Override
  public RestApiException build(
      Throwable exception,
      String requestPath,
      Object handler) {

    HttpStatusCode httpStatus = detectHttpStatus(exception, handler);

    RestApiException restApiException = new RestApiException();

    restApiException.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));

    restApiException.setStatus(httpStatus.value());

    restApiException.setError(getError(exception, httpStatus));

    ExceptionMappingConfig config = getProperties().findExceptionMappingConfig(exception);

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
          return (h instanceof Class) ? (Class<?>) h : h.getClass();
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

  /**
   * Sets error code.
   *
   * @param restApiException the rest api exception
   * @param exception the exception
   * @param handler the handler
   * @param config the config
   * @return the error code
   */
  protected RestApiException setErrorCode(
      RestApiException restApiException,
      Throwable exception,
      Object handler,
      ExceptionMappingConfig config) {

    return Optional.of(exception)
        .filter(exc -> (exc instanceof ErrorCodeAware) && !config.getEvaluateAnnotationFirst())
        .map(exc -> ((ErrorCodeAware) exc).getErrorCode())
        .or(() -> findHandlerMethod(handler)
            .map(method -> findMergedAnnotation(method, ErrorCode.class))
            .map(ErrorCode::value))
        .or(() -> findHandlerClass(handler)
            .map(handlerClass -> findMergedAnnotation(handlerClass, ErrorCode.class))
            .map(ErrorCode::value))
        .or(() -> Optional
            .ofNullable(findMergedAnnotation(getUserClass(exception), ErrorCode.class))
            .map(ErrorCode::value))
        .or(() -> Optional.ofNullable(getProperties().findExceptionMapping(exception).getCode()))
        .filter(errorCode -> !errorCode.isBlank())
        .map(errorCode -> restApiException.toBuilder()
            .errorCode(errorCode)
            .errorCodeInherited(false)
            .build())
        .orElse(restApiException);
  }

  /**
   * Sets message.
   *
   * @param restApiException the rest api exception
   * @param exception the exception
   * @param handler the handler
   * @param config the config
   * @return the message
   */
  protected RestApiException setMessage(
      RestApiException restApiException,
      Throwable exception,
      Object handler,
      ExceptionMappingConfig config) {

    if (!config.getIncludeMessage()) {
      return restApiException;
    }
    return Optional.ofNullable(exception.getMessage())
        .filter(msg -> !msg.isBlank() && !config.getEvaluateAnnotationFirst())
        .or(() -> findHandlerMethod(handler)
            .map(method -> findMergedAnnotation(method, ResponseStatus.class))
            .map(ResponseStatus::reason))
        .or(() -> findHandlerClass(handler)
            .map(handlerClass -> findMergedAnnotation(handlerClass, ResponseStatus.class))
            .map(ResponseStatus::reason))
        .or(() -> Optional
            .ofNullable(findMergedAnnotation(getUserClass(exception), ResponseStatus.class))
            .map(ResponseStatus::reason))
        .or(() -> Optional.ofNullable(getProperties().findExceptionMapping(exception).getMessage()))
        .map(msg -> restApiException.toBuilder().message(msg).build())
        .orElse(restApiException);
  }

  /**
   * Sets class name.
   *
   * @param restApiException the rest api exception
   * @param exception the exception
   * @param config the config
   * @return the class name
   */
  protected RestApiException setClassName(
      RestApiException restApiException,
      Throwable exception,
      ExceptionMappingConfig config) {

    if (!config.getIncludeException()) {
      return restApiException;
    }
    return restApiException.toBuilder()
        .exception(getUserClass(exception).getName())
        .build();
  }

  /**
   * Sets application.
   *
   * @param restApiException the rest api exception
   * @param config the config
   * @return the application
   */
  protected RestApiException setApplication(
      RestApiException restApiException,
      ExceptionMappingConfig config) {

    if (!config.getIncludeApplicationName()) {
      return restApiException;
    }
    return restApiException.toBuilder().application(getApplicationName()).build();
  }

  /**
   * Sets path.
   *
   * @param restApiException the rest api exception
   * @param path the path
   * @param config the config
   * @return the path
   */
  protected RestApiException setPath(
      RestApiException restApiException,
      String path,
      ExceptionMappingConfig config) {

    if (!config.getIncludePath() || isNull(path)) {
      return restApiException;
    }
    return restApiException.toBuilder().path(path).build();
  }

  /**
   * Sets handler.
   *
   * @param restApiException the rest api exception
   * @param handler the handler
   * @param config the config
   * @return the handler
   */
  protected RestApiException setHandler(
      RestApiException restApiException,
      Object handler,
      ExceptionMappingConfig config) {

    if (!config.getIncludeHandler() || isNull(handler)) {
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

  /**
   * Sets stack trace.
   *
   * @param restApiException the rest api exception
   * @param stackTrace the stack trace
   * @param config the config
   * @return the stack trace
   */
  protected RestApiException setStackTrace(
      RestApiException restApiException,
      StackTraceElement[] stackTrace,
      ExceptionMappingConfig config) {

    if (!config.getIncludeStackTrace() || isNull(stackTrace) || stackTrace.length == 0) {
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

  /**
   * Sets cause.
   *
   * @param restApiException the rest api exception
   * @param exception the exception
   * @param config the config
   * @return the cause
   */
  protected RestApiException setCause(
      RestApiException restApiException,
      Throwable exception,
      ExceptionMappingConfig config) {

    return Optional.ofNullable(exception)
        .filter(exc -> exc instanceof RestApiExceptionAware)
        .map(exc -> ((RestApiExceptionAware) exc).getRestApiException())
        .map(cause -> reconfigureRestApiException(cause, config))
        .or(() -> Optional.ofNullable(exception)
            .map(Throwable::getCause)
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
          RestApiException.RestApiExceptionBuilder builder = restApiException.toBuilder();
          String causeErrorCode = cause.getErrorCode();
          if (nonNull(causeErrorCode) && !causeErrorCode.isBlank()) {
            builder = builder
                .errorCode(causeErrorCode)
                .errorCodeInherited(true);
          }
          if (config.getIncludeCause()) {
            builder = builder.cause(cause);
          }
          return builder.build();
        })
        .orElse(restApiException);
  }

  /**
   * Reconfigure rest api exception rest api exception.
   *
   * @param source the source
   * @param config the config
   * @return the rest api exception
   */
  protected RestApiException reconfigureRestApiException(
      RestApiException source,
      ExceptionMappingConfig config) {

    RestApiException target = new RestApiException();
    target.setId(source.getId());
    target.setTimestamp(source.getTimestamp());
    target.setStatus(source.getStatus());
    target.setError(source.getError());
    if (nonNull(source.getErrorCode()) && !source.getErrorCode().isBlank()) {
      target.setErrorCode(source.getErrorCode());
      target.setErrorCodeInherited(source.getErrorCodeInherited());
    }
    if (config.getIncludeMessage()) {
      target.setMessage(source.getMessage());
    }
    if (config.getIncludeException()) {
      target.setException(source.getException());
    }
    if (config.getIncludeApplicationName()) {
      target.setApplication(source.getApplication());
    }
    if (config.getIncludePath()) {
      target.setPath(source.getPath());
    }
    if (config.getIncludeHandler()) {
      target.setHandler(source.getHandler());
    }
    if (config.getIncludeStackTrace()) {
      target.setStackTrace(source.getStackTrace());
    }
    if (config.getIncludeCause() && nonNull(source.getCause())) {
      target.setCause(reconfigureRestApiException(source.getCause(), config));
    }
    source.furtherDetails().forEach(target::furtherDetails);
    return target;
  }

}
