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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.util.FileCopyUtils;

/**
 * This error decoder produces either a {@link FeignClientException} or a {@link
 * feign.RetryableException}.
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
   * Instantiates a new Feign client exception error decoder.
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
    RestApiException restApiException = parser.parseException(body, httpHeaders);
    FeignClientException feignClientException = new FeignClientException(
        response.status(),
        String.format("Status %s reading %s", response.status(), methodKey),
        response.request(),
        headers,
        body,
        restApiException);
    Date retryAfter = determineRetryAfter(httpHeaders.getFirst(RETRY_AFTER));
    if (nonNull(retryAfter)) {
      return new RetryableException(
          response.status(),
          feignClientException.getMessage(),
          findHttpMethod(response),
          feignClientException,
          retryAfter,
          response.request());
    }
    return feignClientException;
  }

  byte[] getResponseBody(Response response) {
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

  Date determineRetryAfter(String retryAfter) {
    if (retryAfter == null) {
      return null;
    }
    try {
      if (retryAfter.matches("^[0-9]+\\.?0*$")) {
        String parsedRetryAfter = retryAfter.replaceAll("\\.0*$", "");
        long deltaMillis = SECONDS.toMillis(Long.parseLong(parsedRetryAfter));
        return new Date(System.currentTimeMillis() + deltaMillis);
      }
      return Date.from(OffsetDateTime.parse(retryAfter,
          DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
    } catch (Exception e) {
      log.warn("Parsing retry after date for feign's RetryableException failed.", e);
      return null;
    }
  }

  HttpMethod findHttpMethod(Response response) {
    if (response == null || response.request() == null) {
      return null;
    }
    return response.request().httpMethod();
  }

}
