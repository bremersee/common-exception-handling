/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.common.exhandling;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.bremersee.common.exhandling.ApiExceptionResolverProperties.ExceptionMappingConfig;
import org.bremersee.common.exhandling.annotation.ErrorCode;
import org.bremersee.common.exhandling.feign.FeignClientException;
import org.bremersee.common.exhandling.model.RestApiException;
import org.bremersee.common.exhandling.model.StackTraceItem;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;

/**
 * @author Christian Bremer
 */
@Validated
public class ApiExceptionMapperImpl extends AbstractExceptionHandler implements ApiExceptionMapper {

  private final String applicationName;

  public ApiExceptionMapperImpl(
      final ApiExceptionResolverProperties properties,
      final String applicationName) {
    super(properties);
    this.applicationName = StringUtils.hasText(applicationName) ? applicationName : "UNSPECIFIED";
  }

  @Override
  public @NotNull RestApiException buildRestApiException(
      @NotNull Exception ex,
      @Nullable Object handler) {

    final ExceptionMappingConfig config = getProperties().findExceptionMappingConfig(ex);

    final RestApiException restApiException = new RestApiException();

    restApiException.setMessage(detectMessage(ex, handler, config));

    if (config.isIncludeApplication()) {
      restApiException.setApplication(applicationName);
    }

    if (config.isIncludeCode()) {
      restApiException.setErrorCode(detectErrorCode(ex, handler, config));
    }

    if (config.isIncludeExceptionClass()) {
      restApiException.setExceptionClassName(ex.getClass().getName());
    }

    if (handler != null && config.isIncludeHandlerClass()) {
      restApiException.setHandlerClassName(getHandlerClass(handler).getName());
    }

    if (config.isIncludeHandlerMethod()) {
      restApiException.setHandlerMethodName(detectHandlerMethodName(handler));
    }

    if (config.isIncludeHandlerMethodParameterTypes()) {
      restApiException.setHandlerMethodParameterTypes(detectHandlerMethodParameterTypes(handler));
    }

    if (config.isIncludeStackTrace()) {
      addStackTraceItems(restApiException, ex.getStackTrace());
    }

    if (config.isIncludeCause()) {
      if (ex instanceof FeignClientException) {
        restApiException.setCause(((FeignClientException) ex).getRestApiException());
      } else {
        restApiException.setCause(buildRestApiExceptionCause(ex.getCause(), config));
      }
    }

    return restApiException;
  }

  private String detectMessage(
      final Throwable throwable,
      final Object handler,
      final ExceptionMappingConfig config) {

    String message = throwable.getMessage();
    if (StringUtils.hasText(message) && !config.isEvaluateAnnotationFirst()) {
      return message;
    }

    ResponseStatus responseStatus = AnnotationUtils
        .findAnnotation(throwable.getClass(), ResponseStatus.class);
    if (responseStatus == null) {
      Optional<Method> method = getHandlerMethod(handler);
      if (method.isPresent()) {
        responseStatus = AnnotationUtils.findAnnotation(method.get(), ResponseStatus.class);
      }
    }
    if (responseStatus != null && StringUtils.hasText(responseStatus.reason())) {
      message = responseStatus.reason();
    }
    return StringUtils.hasText(message)
        ? message
        : getProperties().findExceptionMapping(throwable).getMessage();
  }

  private String detectErrorCode(
      final Throwable throwable,
      final Object handler,
      final ExceptionMappingConfig config) {

    String code = (throwable instanceof ServiceException)
        ? ((ServiceException) throwable).getErrorCode()
        : null;
    if (StringUtils.hasText(code) && !config.isEvaluateAnnotationFirst()) {
      return code;
    }

    ErrorCode errorCode = AnnotationUtils.findAnnotation(throwable.getClass(), ErrorCode.class);
    if (errorCode == null) {
      Optional<Method> method = getHandlerMethod(handler);
      if (method.isPresent()) {
        errorCode = AnnotationUtils.findAnnotation(method.get(), ErrorCode.class);
      }
    }
    if (errorCode != null && StringUtils.hasText(errorCode.value())) {
      code = errorCode.value();
    }
    return StringUtils.hasText(code)
        ? code
        : getProperties().findExceptionMapping(throwable).getCode();
  }

  private String detectHandlerMethodName(final Object handler) {
    if (handler instanceof HandlerMethod) {
      return ((HandlerMethod) handler).getMethod().getName();
    } else {
      return null;
    }
  }

  private List<String> detectHandlerMethodParameterTypes(final Object handler) {
    if (handler instanceof HandlerMethod) {
      final Class<?>[] types = ((HandlerMethod) handler).getMethod().getParameterTypes();
      return Arrays.stream(types).map(Class::getName).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private void addStackTraceItems(
      final RestApiException restApiException,
      final StackTraceElement[] stackTrace) {

    if (stackTrace != null) {
      for (StackTraceElement elem : stackTrace) {
        restApiException.addStackTraceItem(
            new StackTraceItem().declaringClass(elem.getClassName()).fileName(elem.getFileName())
                .lineNumber(elem.getLineNumber()).methodName(elem.getMethodName()));
      }
    }
  }

  private RestApiException buildRestApiExceptionCause(
      final Throwable cause,
      final ExceptionMappingConfig config) {

    if (cause == null) {
      return null;
    }
    if (cause instanceof FeignClientException) {
      return ((FeignClientException) cause).getRestApiException();
    }
    final RestApiException payload = new RestApiException();
    payload.setMessage(detectMessage(cause, null, config));
    if (config.isIncludeCode()) {
      payload.setErrorCode(detectErrorCode(cause, null, config));
    }
    if (config.isIncludeExceptionClass()) {
      payload.setExceptionClassName(cause.getClass().getName());
    }
    if (config.isIncludeStackTrace()) {
      addStackTraceItems(payload, cause.getStackTrace());
    }
    payload.setCause(buildRestApiExceptionCause(cause.getCause(), config));
    return payload;
  }

}
