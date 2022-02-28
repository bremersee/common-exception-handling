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

import java.io.IOException;
import java.io.InputStream;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;

/**
 * A http response parser that creates a {@link RestApiException}.
 *
 * @author Christian Bremer
 */
@Valid
public interface RestApiExceptionParser {

  /**
   * Parse exception.
   *
   * @param response the response
   * @param httpStatus the http status
   * @param headers the headers
   * @return the parsed exception
   */
  RestApiException parseException(
      String response,
      @NotNull HttpStatus httpStatus,
      @NotNull HttpHeaders headers);

  /**
   * Parse exception.
   *
   * @param response the response
   * @param httpStatus the http status
   * @param headers the headers
   * @return the parsed exception
   */
  RestApiException parseException(
      byte[] response,
      @NotNull HttpStatus httpStatus,
      @NotNull HttpHeaders headers);

  /**
   * Parse exception.
   *
   * @param response the response
   * @param httpStatus the http status
   * @param headers the headers
   * @return the rest api exception
   * @throws IOException the io exception
   */
  default RestApiException parseException(
      InputStream response,
      @NotNull HttpStatus httpStatus,
      @NotNull HttpHeaders headers) throws IOException {

    return parseException(FileCopyUtils.copyToByteArray(response), httpStatus, headers);
  }

}
