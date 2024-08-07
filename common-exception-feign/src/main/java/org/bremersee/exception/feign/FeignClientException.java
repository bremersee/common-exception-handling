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

import feign.FeignException;
import feign.Request;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.bremersee.exception.HttpStatusAware;
import org.bremersee.exception.RestApiExceptionAware;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * Feign exception that stores the error payload as a {@link RestApiException}. If the error payload
 * cannot be parsed as {@link RestApiException}, the whole body of the error payload will be stored
 * in the message field of the {@link RestApiException}.
 *
 * @author Christian Bremer
 */
@Getter
public class FeignClientException extends FeignException
    implements HttpStatusAware, RestApiExceptionAware {

  /**
   * The rest api exception.
   */
  private final RestApiException restApiException;

  /**
   * Instantiates a new feign client exception.
   *
   * @param status the response status code
   * @param message the message of this {@link FeignClientException}
   * @param request the original request
   * @param responseHeaders the response headers
   * @param responseBody the response body
   * @param restApiException the rest api exception
   */
  public FeignClientException(
      int status,
      String message,
      Request request,
      Map<String, Collection<String>> responseHeaders,
      byte[] responseBody,
      RestApiException restApiException) {

    super(
        resolveHttpStatusCode(status),
        message,
        request,
        responseBody,
        responseHeaders);
    this.restApiException = restApiException;
  }

  private static int resolveHttpStatusCode(int httpStatusCode) {
    HttpStatus httpStatus = HttpStatus.resolve(httpStatusCode);
    return httpStatus != null ? httpStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
  }

}
