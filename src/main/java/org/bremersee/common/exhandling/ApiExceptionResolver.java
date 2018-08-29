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
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.common.exhandling.feign.FeignClientException; // TODO without feign on classpath
import org.bremersee.common.exhandling.model.RestApiException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;
import org.springframework.web.util.WebUtils;

/**
 * @author Christian Bremer
 */
@Validated
@Slf4j
public class ApiExceptionResolver extends AbstractExceptionHandler implements
    HandlerExceptionResolver {

  @SuppressWarnings("WeakerAccess")
  public static final String MESSAGE_HEADER_NAME = "X-ERROR-MESSAGE";

  @SuppressWarnings("WeakerAccess")
  public static final String CODE_HEADER_NAME = "X-ERROR-CODE";

  @SuppressWarnings("WeakerAccess")
  public static final String CLASS_HEADER_NAME = "X-ERROR-CLASS-NAME";

  private static final String FEIGN_EXCEPTION_CLASS_NAME = "feign.FeignException";

  private static final String FEIGN_EXCEPTION_STATUS_METHOD_NAME = "status";

  private static final String MODEL_KEY = "error";

  private final ApiExceptionMapper exceptionMapper;

  @Setter
  private Jackson2ObjectMapperBuilder objectMapperBuilder;

  public ApiExceptionResolver(
      final ApiExceptionResolverProperties properties,
      final ApiExceptionMapper exceptionMapper) {
    super(properties);
    this.exceptionMapper = exceptionMapper;
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public ModelAndView resolveException(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler,
      final Exception ex) {

    if (!isRestController(handler)) {
      return null;
    }

    final RestApiException payload = exceptionMapper
        .buildRestApiException(ex, handler);

    ModelAndView modelAndView;
    final ResponseFormatAndContentType chooser = new ResponseFormatAndContentType(request);
    switch (chooser.getResponseFormat()) {
      case JSON:
        final MappingJackson2JsonView mjv = objectMapperBuilder == null
            ? new MappingJackson2JsonView()
            : new MappingJackson2JsonView(objectMapperBuilder.build());
        mjv.setContentType(chooser.getContentType());
        mjv.setPrettyPrint(true);
        mjv.setModelKey(MODEL_KEY);
        modelAndView = new ModelAndView(mjv, MODEL_KEY, payload);
        break;

      case XML:
        final MappingJackson2XmlView mxv = objectMapperBuilder == null
            ? new MappingJackson2XmlView()
            : new MappingJackson2XmlView(objectMapperBuilder.createXmlMapper(true).build());
        mxv.setContentType(chooser.getContentType());
        mxv.setPrettyPrint(true);
        mxv.setModelKey(MODEL_KEY);
        modelAndView = new ModelAndView(mxv, MODEL_KEY, payload);
        break;

      default:
        modelAndView = new ModelAndView(new EmptyView(payload, chooser.getContentType()));
    }

    response.setContentType(chooser.getContentType());
    final int statusCode = detectHttpStatus(ex, handler);
    applyStatusCodeIfPossible(request, response, statusCode);
    return modelAndView;
  }

  @SuppressWarnings("WeakerAccess")
  protected boolean isRestController(final Object handler) {
    if (handler == null) {
      return false;
    }
    final Class<?> cls = getHandlerClass(handler);
    final boolean result = AnnotationUtils.findAnnotation(cls, RestController.class) != null;
    if (log.isDebugEnabled()) {
      log.debug("Is handler [" + handler + "] a rest controller? " + result);
    }
    return result;
  }

  @SuppressWarnings("WeakerAccess")
  protected int detectHttpStatus(final @NotNull Exception ex, final Object handler) {

    if (ex instanceof ServiceException && ((ServiceException) ex).getHttpStatusCode() != null) {
      return ((ServiceException) ex).getHttpStatusCode();
    }

    if (ex instanceof FeignClientException
        && ((FeignClientException) ex).getHttpStatusCode() != null) {
      return ((FeignClientException) ex).getHttpStatusCode();
    }

    if (isInstanceOf(ex.getClass(), FEIGN_EXCEPTION_CLASS_NAME)) {
      try {
        final Method method = ReflectionUtils.findMethod(
            Class.forName(FEIGN_EXCEPTION_CLASS_NAME), FEIGN_EXCEPTION_STATUS_METHOD_NAME);
        if (method != null) {
          return (int) ReflectionUtils.invokeMethod(method, ex);
        }
      } catch (Exception ignored) {
        // ignored
      }
    }

    ResponseStatus responseStatus = AnnotationUtils
        .findAnnotation(ex.getClass(), ResponseStatus.class);
    if (responseStatus == null) {
      Optional<Method> method = getHandlerMethod(handler);
      if (method.isPresent()) {
        responseStatus = AnnotationUtils.findAnnotation(method.get(), ResponseStatus.class);
      }
    }
    if (responseStatus != null) {
      return responseStatus.code().value();
    }

    return getProperties().findExceptionMapping(ex).getStatus();
  }

  private boolean isInstanceOf(Class<?> cls, String clsName) {
    if (cls == null || clsName == null) {
      return false;
    }
    if (cls.getName().equals(clsName)) {
      return true;
    }
    return isInstanceOf(cls.getSuperclass(), clsName);
  }

  @SuppressWarnings("WeakerAccess")
  protected final void applyStatusCodeIfPossible(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final int statusCode) {

    if (!WebUtils.isIncludeRequest(request)) {
      if (log.isDebugEnabled()) {
        log.debug("Applying HTTP status code " + statusCode);
      }
      response.setStatus(statusCode);
      request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
    }
  }

  enum ResponseFormat {
    JSON, XML, EMPTY
  }

  static class ResponseFormatAndContentType {

    @Getter
    private ResponseFormat responseFormat;

    @Getter
    private String contentType;

    ResponseFormatAndContentType(final @NotNull HttpServletRequest request) {
      String acceptHeader = request
          .getHeader("Accept") == null ? "" : request.getHeader("Accept").toLowerCase();
      if ("".equals(acceptHeader) || acceptHeader.contains("*/*") || acceptHeader
          .contains("/json")) {
        responseFormat = ResponseFormat.JSON;
        contentType = MediaType.APPLICATION_JSON_UTF8_VALUE;
      } else if (acceptHeader.contains("/xml")) {
        responseFormat = ResponseFormat.XML;
        contentType = MediaType.APPLICATION_XML_VALUE;
      } else if (acceptHeader.contains("text/plain")) {
        responseFormat = ResponseFormat.JSON;
        contentType = MediaType.TEXT_PLAIN_VALUE;
      } else {
        responseFormat = ResponseFormat.EMPTY;
        int i = acceptHeader.charAt(',');
        if (i > 0) {
          acceptHeader = acceptHeader.substring(0, i);
        }
        i = acceptHeader.charAt(';');
        if (i > 0) {
          acceptHeader = acceptHeader.substring(0, i);
        }
        contentType = acceptHeader;
      }
    }
  }

  static class EmptyView extends AbstractView {

    final String errorMessage;

    final String errorCode;

    final String errorClassName;

    EmptyView(final @NotNull RestApiException payload, String contentType) {
      this.errorMessage = payload.getMessage();
      this.errorCode = payload.getErrorCode();
      this.errorClassName = payload.getExceptionClassName();
      if (contentType != null) {
        setContentType(contentType);
      }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void renderMergedOutputModel(
        final Map<String, Object> map,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse httpServletResponse) {

      if (StringUtils.hasText(errorMessage)) {
        httpServletResponse.addHeader(MESSAGE_HEADER_NAME, errorMessage);
      }
      if (StringUtils.hasText(errorCode)) {
        httpServletResponse.addHeader(CODE_HEADER_NAME, errorCode);
      }
      if (StringUtils.hasText(errorClassName)) {
        httpServletResponse.addHeader(CLASS_HEADER_NAME, errorClassName);
      }
    }

  }

}
