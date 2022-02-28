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

import java.util.Optional;
import lombok.Getter;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The rest api response exception.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
public class RestApiResponseException
    extends ResponseStatusException
    implements RestApiExceptionAware {

  @Getter
  private final RestApiException restApiException;

  /**
   * Instantiates a new rest api response exception.
   *
   * @param restApiException the rest api exception
   */
  public RestApiResponseException(
      RestApiException restApiException) {

    super(detectHttpStatus(restApiException));
    this.restApiException = restApiException;
  }

  /**
   * Instantiates a new rest api response exception.
   *
   * @param status the status
   * @param restApiException the rest api exception
   */
  public RestApiResponseException(
      HttpStatus status,
      RestApiException restApiException) {

    super(status);
    this.restApiException = restApiException;
  }

  private static HttpStatus detectHttpStatus(RestApiException restApiException) {
    return Optional.ofNullable(restApiException)
        .map(RestApiException::getStatus)
        .map(HttpStatus::resolve)
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
