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

package org.bremersee.exception.servlet;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionConstants;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiResponseType;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;
import org.springframework.web.util.WebUtils;

/**
 * The api exception resolver.
 *
 * @author Christian Bremer
 */
@Slf4j
public class ApiExceptionResolver implements HandlerExceptionResolver {

  /**
   * The constant MODEL_KEY.
   */
  protected static final String MODEL_KEY = "error";

  @Getter(AccessLevel.PROTECTED)
  private final List<String> apiPaths;

  @Getter(AccessLevel.PROTECTED)
  @Setter
  private PathMatcher pathMatcher = new AntPathMatcher();

  @Getter(AccessLevel.PROTECTED)
  @Setter
  private Function<HttpServletRequest, String> restApiExceptionIdProvider;

  @Getter(AccessLevel.PROTECTED)
  private final RestApiExceptionMapper exceptionMapper;

  @Getter(AccessLevel.PROTECTED)
  private final ObjectMapper objectMapper;

  @Getter(AccessLevel.PROTECTED)
  private final XmlMapper xmlMapper;

  /**
   * Instantiates a new api exception resolver.
   *
   * @param exceptionMapper the exception mapper
   */
  public ApiExceptionResolver(
      List<String> apiPaths,
      RestApiExceptionMapper exceptionMapper) {
    this(apiPaths, exceptionMapper, new Jackson2ObjectMapperBuilder());
  }

  /**
   * Instantiates a new api exception resolver.
   *
   * @param exceptionMapper the exception mapper
   * @param objectMapperBuilder the object mapper builder
   */
  public ApiExceptionResolver(
      List<String> apiPaths,
      RestApiExceptionMapper exceptionMapper,
      Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this(
        apiPaths,
        exceptionMapper,
        objectMapperBuilder.build(),
        objectMapperBuilder.createXmlMapper(true).build());
  }

  /**
   * Instantiates a new api exception resolver.
   *
   * @param exceptionMapper the exception mapper
   * @param objectMapper the object mapper
   * @param xmlMapper the xml mapper
   */
  public ApiExceptionResolver(
      List<String> apiPaths,
      RestApiExceptionMapper exceptionMapper,
      ObjectMapper objectMapper,
      XmlMapper xmlMapper) {
    this.apiPaths = apiPaths;
    this.exceptionMapper = exceptionMapper;
    this.objectMapper = objectMapper;
    this.xmlMapper = xmlMapper;
  }

  @Override
  public ModelAndView resolveException(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      Object handler,
      @NonNull Exception ex) {

    if (!isExceptionHandlerResponsible(request, handler)) {
      return null;
    }

    RestApiException payload = exceptionMapper.build(ex, request.getRequestURI(), handler);
    if (!isEmpty(restApiExceptionIdProvider)) {
      payload.setId(restApiExceptionIdProvider.apply(request));
    }

    ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
    List<MediaType> accepted = httpRequest.getHeaders().getAccept();
    RestApiResponseType responseType = RestApiResponseType.detectByAccepted(accepted);

    ModelAndView modelAndView;
    switch (responseType) {
      case JSON:
        MappingJackson2JsonView mjv = new MappingJackson2JsonView(objectMapper);
        mjv.setContentType(responseType.getContentTypeValue());
        mjv.setPrettyPrint(true);
        mjv.setModelKey(MODEL_KEY);
        mjv.setExtractValueFromSingleKeyModel(true); // removes the MODEL_KEY from the output
        modelAndView = new ModelAndView(mjv, MODEL_KEY, payload);
        break;

      case XML:
        MappingJackson2XmlView mxv = new MappingJackson2XmlView(xmlMapper);
        mxv.setContentType(responseType.getContentTypeValue());
        mxv.setPrettyPrint(true);
        mxv.setModelKey(MODEL_KEY);
        modelAndView = new ModelAndView(mxv, MODEL_KEY, payload);
        break;

      default:
        modelAndView = new ModelAndView(new EmptyView(payload, responseType.getContentTypeValue()));
    }

    response.setContentType(responseType.getContentTypeValue());
    int statusCode = requireNonNullElse(
        payload.getStatus(),
        HttpStatus.INTERNAL_SERVER_ERROR.value());
    modelAndView.setStatus(HttpStatus.resolve(statusCode));
    applyStatusCodeIfPossible(request, response, statusCode);
    return modelAndView;
  }

  /**
   * Is this exception handler responsible.
   *
   * @param request the request
   * @param handler the handler
   * @return {@code true} if it is responsible, otherwise {@code false}
   */
  protected boolean isExceptionHandlerResponsible(
      HttpServletRequest request,
      Object handler) {

    if (!isEmpty(apiPaths)) {
      return apiPaths.stream().anyMatch(
          s -> pathMatcher.match(s, request.getServletPath()));
    }

    if (isEmpty(handler)) {
      return false;
    }
    Class<?> cls = handler instanceof HandlerMethod
        ? ((HandlerMethod) handler).getBean().getClass()
        : handler.getClass();
    boolean result = !isEmpty(findAnnotation(cls, RestController.class));
    if (log.isDebugEnabled()) {
      log.debug("Is handler [" + handler + "] a rest controller? " + result);
    }
    return result;
  }

  /**
   * Apply status code if possible.
   *
   * @param request the request
   * @param response the response
   * @param statusCode the status code
   */
  protected final void applyStatusCodeIfPossible(
      HttpServletRequest request,
      HttpServletResponse response,
      int statusCode) {

    if (!WebUtils.isIncludeRequest(request)) {
      if (log.isDebugEnabled()) {
        log.debug("Applying HTTP status code " + statusCode);
      }
      response.setStatus(statusCode);
      request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
    }
  }

  /**
   * The empty view.
   */
  protected static class EmptyView extends AbstractView {

    /**
     * The rest api exception.
     */
    protected final RestApiException restApiException;

    /**
     * Instantiates a new empty view.
     *
     * @param payload the payload
     * @param contentType the content type
     */
    protected EmptyView(RestApiException payload, String contentType) {
      this.restApiException = payload;
      setContentType(contentType);
    }

    @Override
    protected void renderMergedOutputModel(
        Map<String, Object> map,
        @NonNull HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse) {

      if (!isEmpty(restApiException.getId())) {
        httpServletResponse.addHeader(
            RestApiExceptionConstants.ID_HEADER_NAME,
            restApiException.getId());
      }

      String timestamp;
      if (!isEmpty(restApiException.getTimestamp())) {
        timestamp = restApiException.getTimestamp()
            .format(RestApiExceptionConstants.TIMESTAMP_FORMATTER);
      } else {
        timestamp = OffsetDateTime.now(ZoneOffset.UTC)
            .format(RestApiExceptionConstants.TIMESTAMP_FORMATTER);
      }
      httpServletResponse.addHeader(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME, timestamp);

      if (!isEmpty(restApiException.getErrorCode())) {
        httpServletResponse.addHeader(
            RestApiExceptionConstants.CODE_HEADER_NAME,
            restApiException.getErrorCode());
        httpServletResponse.addHeader(
            RestApiExceptionConstants.CODE_INHERITED_HEADER_NAME,
            String.valueOf(restApiException.getErrorCodeInherited()));
      }

      if (!isEmpty(restApiException.getMessage())) {
        httpServletResponse.addHeader(
            RestApiExceptionConstants.MESSAGE_HEADER_NAME,
            restApiException.getMessage());
      }

      if (!isEmpty(restApiException.getException())) {
        httpServletResponse.addHeader(
            RestApiExceptionConstants.EXCEPTION_HEADER_NAME,
            restApiException.getException());
      }

      if (!isEmpty(restApiException.getApplication())) {
        httpServletResponse.addHeader(
            RestApiExceptionConstants.APPLICATION_HEADER_NAME,
            restApiException.getApplication());
      }

      if (!isEmpty(restApiException.getPath())) {
        httpServletResponse.addHeader(
            RestApiExceptionConstants.PATH_HEADER_NAME,
            restApiException.getPath());
      }
    }

  }

}
