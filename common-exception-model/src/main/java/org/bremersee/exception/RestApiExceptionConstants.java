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

import java.time.format.DateTimeFormatter;

/**
 * The rest api exception constants.
 *
 * @author Christian Bremer
 */
public abstract class RestApiExceptionConstants {

  /**
   * The header name for the 'id' attribute.
   */
  public static final String ID_HEADER_NAME = "X-ERROR-ID";

  /**
   * The header name for the 'timestamp' attribute.
   */
  public static final String TIMESTAMP_HEADER_NAME = "X-ERROR-TIMESTAMP";

  /**
   * The formatter of the timestamp value.
   */
  public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

  /**
   * The header name for the 'errorCode' attribute.
   */
  public static final String CODE_HEADER_NAME = "X-ERROR-CODE";

  /**
   * The header name for the 'errorCodeInherited' attribute.
   */
  public static final String CODE_INHERITED_HEADER_NAME = "X-ERROR-CODE-INHERITED";

  /**
   * The header name for the 'message' attribute.
   */
  public static final String MESSAGE_HEADER_NAME = "X-ERROR-MESSAGE";

  /**
   * The header name for the 'class name' attribute.
   */
  public static final String EXCEPTION_HEADER_NAME = "X-ERROR-EXCEPTION";

  /**
   * The header name for the 'application' attribute.
   */
  public static final String APPLICATION_HEADER_NAME = "X-ERROR-APPLICATION";

  /**
   * The header name for the 'path' attribute.
   */
  public static final String PATH_HEADER_NAME = "X-ERROR-PATH";

  private RestApiExceptionConstants() {
  }

}
