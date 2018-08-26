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

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Christian Bremer
 */
public class ServiceException extends RuntimeException {

  @Getter
  private final Integer httpStatusCode;

  @Getter
  private final String errorCode;

  public ServiceException() {
    super();
    this.httpStatusCode = null;
    this.errorCode = null;
  }

  public ServiceException(final HttpStatus httpStatus) {
    super(detectReason(httpStatus));
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = null;
  }

  public ServiceException(final HttpStatus httpStatus, final String errorCode) {
    super(detectReason(httpStatus));
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = errorCode;
  }

  public ServiceException(final HttpStatus httpStatus, Throwable cause) {
    super(detectReason(httpStatus), cause);
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = null;
  }

  public ServiceException(final HttpStatus httpStatus, final String errorCode, Throwable cause) {
    super(detectReason(httpStatus), cause);
    this.httpStatusCode = detectHttpStatusCode(httpStatus);
    this.errorCode = errorCode;
  }


  public ServiceException(final int httpStatusCode, final String reason) {
    super(reason);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = null;
  }

  public ServiceException(final int httpStatusCode, final String reason, final String errorCode) {
    super(reason);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = errorCode;
  }

  public ServiceException(final int httpStatusCode, final String reason, final Throwable cause) {
    super(reason, cause);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = null;
  }

  public ServiceException(final int httpStatusCode, final String reason, final String errorCode,
      final Throwable cause) {
    super(reason, cause);
    this.httpStatusCode = resolveHttpStatusCode(httpStatusCode);
    this.errorCode = errorCode;
  }


  private static Integer detectHttpStatusCode(final HttpStatus httpStatus) {
    return httpStatus != null ? httpStatus.value() : null;
  }

  private static String detectReason(final HttpStatus httpStatus) {
    return httpStatus != null ? httpStatus.getReasonPhrase() : null;
  }

  private static Integer resolveHttpStatusCode(final int httpStatusCode) {
    final HttpStatus httpStatus = HttpStatus.resolve(httpStatusCode);
    return httpStatus != null ? httpStatus.value() : null;
  }

}
