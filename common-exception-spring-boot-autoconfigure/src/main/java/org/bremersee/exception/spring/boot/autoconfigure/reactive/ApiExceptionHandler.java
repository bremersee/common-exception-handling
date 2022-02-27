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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.StringUtils.hasText;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionConstants;
import org.bremersee.exception.RestApiExceptionMapper;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
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
@Valid
@Slf4j
public class ApiExceptionHandler extends AbstractErrorWebExceptionHandler {

  @Getter(AccessLevel.PROTECTED)
  private final List<String> apiPaths;

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
   * @param apiPaths the api paths
   * @param errorAttributes the error attributes
   * @param resources the resources
   * @param applicationContext the application context
   * @param serverCodecConfigurer the server codec configurer
   * @param restApiExceptionMapper the rest api exception mapper
   */
  public ApiExceptionHandler(
      List<String> apiPaths,
      @NotNull ErrorAttributes errorAttributes,
      @NotNull WebProperties.Resources resources,
      @NotNull ApplicationContext applicationContext,
      ServerCodecConfigurer serverCodecConfigurer,
      @NotNull RestApiExceptionMapper restApiExceptionMapper) {

    super(errorAttributes, resources, applicationContext);
    if (serverCodecConfigurer != null) {
      setMessageReaders(serverCodecConfigurer.getReaders());
      setMessageWriters(serverCodecConfigurer.getWriters());
    }
    this.apiPaths = nonNull(apiPaths) ? apiPaths : List.of();
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
    return apiPaths.stream().anyMatch(
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
      return emptyWithHeaders(response, restApiResponseType.getContentType());
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
      RestApiException response,
      MediaType contentType) {

    ServerResponse.BodyBuilder builder = ServerResponse
        .status(requireNonNullElse(
            response.getStatus(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()));

    if (hasText(response.getId())) {
      builder = builder.header(RestApiExceptionConstants.ID_HEADER_NAME, response.getId());
    }

    String timestamp;
    if (nonNull(response.getTimestamp())) {
      timestamp = response.getTimestamp()
          .format(RestApiExceptionConstants.TIMESTAMP_FORMATTER);
    } else {
      timestamp = OffsetDateTime.now(ZoneOffset.UTC)
          .format(RestApiExceptionConstants.TIMESTAMP_FORMATTER);
    }
    builder = builder.header(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME, timestamp);

    if (hasText(response.getErrorCode())) {
      builder = builder.header(
          RestApiExceptionConstants.CODE_HEADER_NAME,
          response.getErrorCode());
      builder = builder.header(
          RestApiExceptionConstants.CODE_INHERITED_HEADER_NAME,
          String.valueOf(response.getErrorCodeInherited()));
    }

    if (hasText(response.getMessage())) {
      builder = builder.header(
          RestApiExceptionConstants.MESSAGE_HEADER_NAME,
          response.getMessage());
    }

    if (hasText(response.getException())) {
      builder = builder.header(
          RestApiExceptionConstants.EXCEPTION_HEADER_NAME,
          response.getException());
    }

    if (hasText(response.getApplication())) {
      builder = builder.header(
          RestApiExceptionConstants.APPLICATION_HEADER_NAME,
          response.getApplication());
    }

    if (hasText(response.getPath())) {
      builder = builder.header(
          RestApiExceptionConstants.PATH_HEADER_NAME,
          response.getPath());
    }

    return builder
        .contentType(contentType)
        .body(BodyInserters.empty());
  }

}
