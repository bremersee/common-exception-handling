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

package org.bremersee.exception.spring.boot.autoconfigure.reactive;

import static java.util.Objects.requireNonNullElse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionConstants;
import org.bremersee.exception.RestApiResponseType;
import org.bremersee.exception.model.RestApiException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * The reactive api exception handler.
 *
 * @author Christian Bremer
 */
@Validated
@Slf4j
public class ApiExceptionHandler extends AbstractErrorWebExceptionHandler {

  @Getter(AccessLevel.PROTECTED)
  @Setter
  @NotNull
  private PathMatcher pathMatcher = new AntPathMatcher();

  @Getter(AccessLevel.PROTECTED)
  @NotNull
  private final RestApiExceptionMapper restApiExceptionMapper;

  /**
   * Instantiates a new api exception handler.
   *
   * @param errorAttributes the error attributes
   * @param resources the resources
   * @param applicationContext the application context
   * @param serverCodecConfigurer the server codec configurer
   * @param restApiExceptionMapper the rest api exception mapper
   */
  public ApiExceptionHandler(
      @NotNull ErrorAttributes errorAttributes,
      @NotNull WebProperties.Resources resources,
      @NotNull ApplicationContext applicationContext,
      @Nullable ServerCodecConfigurer serverCodecConfigurer,
      @NotNull RestApiExceptionMapper restApiExceptionMapper) {

    super(errorAttributes, resources, applicationContext);
    if (serverCodecConfigurer != null) {
      setMessageReaders(serverCodecConfigurer.getReaders());
      setMessageWriters(serverCodecConfigurer.getWriters());
    }
    this.restApiExceptionMapper = restApiExceptionMapper;
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(
      ErrorAttributes errorAttributes) {

    return RouterFunctions.route(this::isResponsibleExceptionHandler, this::renderErrorResponse);
  }

  /**
   * Is this exception handler responsible.
   *
   * @param request the request
   * @return {@code true} if it is responsible, otherwise {@code false}
   */
  protected boolean isResponsibleExceptionHandler(ServerRequest request) {
    return getRestApiExceptionMapper().getApiPaths().stream().anyMatch(
        path -> getPathMatcher().match(path, request.path()));
  }

  /**
   * Render error response.
   *
   * @param request the request
   * @return the server response
   */
  @NonNull
  protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

    RestApiException response = getRestApiExceptionMapper()
        .build(getError(request), request.path(), null);

    RestApiResponseType restApiResponseType = RestApiResponseType
        .detectByAccepted(request.headers().accept());
    if (RestApiResponseType.HEADER == restApiResponseType) {
      return emptyWithHeaders(request, response, restApiResponseType.getContentType());
    } else {
      return ServerResponse
          .status(requireNonNullElse(
              response.getStatus(),
              HttpStatus.INTERNAL_SERVER_ERROR.value()))
          .contentType(restApiResponseType.getContentType())
          .body(BodyInserters.fromValue(response));
    }
  }

  private Mono<ServerResponse> emptyWithHeaders(
      ServerRequest request,
      RestApiException response,
      MediaType contentType) {

    String id = StringUtils.hasText(response.getId())
        ? response.getId()
        : RestApiExceptionConstants.NO_ID_VALUE;
    String timestamp = response.getTimestamp() != null
        ? response.getTimestamp().format(RestApiExceptionConstants.TIMESTAMP_FORMATTER)
        : OffsetDateTime.now(ZoneOffset.UTC).format(RestApiExceptionConstants.TIMESTAMP_FORMATTER);
    String msg = StringUtils.hasText(response.getMessage())
        ? response.getMessage()
        : RestApiExceptionConstants.NO_MESSAGE_VALUE;
    String code = StringUtils.hasText(response.getErrorCode())
        ? response.getErrorCode()
        : RestApiExceptionConstants.NO_ERROR_CODE_VALUE;
    String exceptionName = StringUtils.hasText(response.getException())
        ? response.getException()
        : RestApiExceptionConstants.NO_EXCEPTION_VALUE;
    return ServerResponse
        .status(getRestApiExceptionMapper().detectHttpStatus(getError(request), null))
        .header(RestApiExceptionConstants.ID_HEADER_NAME, id)
        .header(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME, timestamp)
        .header(RestApiExceptionConstants.MESSAGE_HEADER_NAME, msg)
        .header(RestApiExceptionConstants.CODE_HEADER_NAME, code)
        .header(RestApiExceptionConstants.EXCEPTION_HEADER_NAME, exceptionName)
        .contentType(contentType)
        .body(BodyInserters.empty());
  }

}
