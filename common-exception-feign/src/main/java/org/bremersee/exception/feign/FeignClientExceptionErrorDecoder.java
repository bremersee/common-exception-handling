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

package org.bremersee.exception.feign;

import static feign.Util.RETRY_AFTER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import feign.Request.HttpMethod;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;

/**
 * This error decoder produces either a {@link FeignClientException} or a {@link
 * feign.RetryableException}**.
 *
 * @author Christian Bremer
 */
@Slf4j
public class FeignClientExceptionErrorDecoder implements ErrorDecoder {

  private final RestApiExceptionParser parser;

  /**
   * Instantiates a new feign client exception error decoder.
   */
  public FeignClientExceptionErrorDecoder() {
    this(null);
  }

  /**
   * Instantiates a new feign client exception error decoder.
   *
   * @param parser the parser
   */
  public FeignClientExceptionErrorDecoder(RestApiExceptionParser parser) {
    this.parser = nonNull(parser) ? parser : new RestApiExceptionParserImpl();
  }

  @Override
  public Exception decode(String methodKey, Response response) {

    if (log.isDebugEnabled()) {
      log.debug("Decoding feign exception at {}", methodKey);
    }
    Map<String, Collection<String>> headers = Objects
        .requireNonNullElseGet(response.headers(), Map::of);
    HttpHeaders httpHeaders = headers.entrySet()
        .stream()
        .collect(
            HttpHeaders::new,
            (a, b) -> a.addAll(b.getKey(), List.copyOf(b.getValue())),
            HttpHeaders::putAll);
    byte[] body = getResponseBody(response);
    RestApiException restApiException = parser.parseException(
        body,
        Optional.ofNullable(HttpStatus.resolve(response.status()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR),
        httpHeaders);
    FeignClientException feignClientException = new FeignClientException(
        response.status(),
        String.format("Status %s reading %s", response.status(), methodKey),
        response.request(),
        headers,
        body,
        restApiException);
    return determineRetryAfter(httpHeaders.getFirst(RETRY_AFTER))
        .map(retryAfter -> (Exception) new RetryableException(
            response.status(),
            feignClientException.getMessage(),
            getHttpMethod(response),
            feignClientException,
            Date.from(retryAfter),
            response.request()))
        .orElse(feignClientException);
  }

  /**
   * Get response body.
   *
   * @param response the response
   * @return the body as byte array
   */
  protected byte[] getResponseBody(Response response) {
    byte[] body;
    if (isNull(response.body())) {
      body = new byte[0];
    } else {
      try (InputStream in = response.body().asInputStream()) {
        body = FileCopyUtils.copyToByteArray(in);
      } catch (IOException e) {
        body = new byte[0];
      }
    }
    return body;
  }

  /**
   * Find http method.
   *
   * @param response the response
   * @return the http method
   */
  protected HttpMethod getHttpMethod(Response response) {
    if (isNull(response) || isNull(response.request())) {
      return null;
    }
    return response.request().httpMethod();
  }

  /**
   * Determine retry after.
   *
   * @param retryAfter the retry after
   * @return the optional
   */
  protected Optional<Instant> determineRetryAfter(String retryAfter) {
    try {
      return Optional.ofNullable(retryAfter)
          .filter(retryAfterValue -> retryAfterValue.matches("^[0-9]+\\.?0*$"))
          .map(retryAfterValue -> retryAfterValue.replaceAll("\\.0*$", ""))
          .map(retryAfterValue -> SECONDS.toMillis(Long.parseLong(retryAfterValue)))
          .map(deltaMillis -> Instant.now().plus(Duration.ofMillis(deltaMillis)))
          .or(() -> Optional.ofNullable(retryAfter)
              .map(retryAfterValue -> OffsetDateTime.parse(
                  retryAfterValue,
                  DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()));

    } catch (Exception e) {
      log.warn("Parsing retry after date for feigns RetryableException failed.", e);
      return Optional.empty();
    }
  }

}
