/*
 * Copyright 2022 the original author or authors.
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

import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * The rest api response error handler.
 *
 * @author Christian Bremer
 */
@Slf4j
public class RestApiResponseErrorHandler implements ResponseErrorHandler {

  private final RestApiExceptionParser restApiExceptionParser;

  /**
   * Instantiates a new rest api response error handler.
   */
  public RestApiResponseErrorHandler() {
    this(null);
  }

  /**
   * Instantiates a new rest api response error handler.
   *
   * @param restApiExceptionParser the rest api exception parser
   */
  public RestApiResponseErrorHandler(RestApiExceptionParser restApiExceptionParser) {
    this.restApiExceptionParser = Optional.ofNullable(restApiExceptionParser)
        .orElseGet(RestApiExceptionParserImpl::new);
  }

  @Override
  public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
    return response.getStatusCode().isError();
  }

  @Override
  public void handleError(@NonNull ClientHttpResponse response) throws IOException {
    RestApiException restApiException = restApiExceptionParser.parseException(
        response.getBody(),
        response.getStatusCode(),
        response.getHeaders());
    RestApiResponseException exception = new RestApiResponseException(
        response.getStatusCode(), restApiException);
    log.error("A rest api exception occurred: {}", restApiException, exception);
    throw exception;
  }

}
