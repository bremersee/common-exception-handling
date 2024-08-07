/*
 * Copyright 2020-2022 the original author or authors.
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

import org.springframework.http.HttpStatusCode;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * The implementation of a rest api exception mapper for spring web flux.
 *
 * @author Christian Bremer
 */
public class RestApiExceptionMapperForWebFlux extends RestApiExceptionMapperForWeb {

  /**
   * Instantiates a new rest api exception mapper.
   *
   * @param properties the properties
   * @param applicationName the application name
   */
  public RestApiExceptionMapperForWebFlux(
      RestApiExceptionMapperProperties properties,
      String applicationName) {
    super(properties, applicationName);
  }

  @Override
  protected HttpStatusCode detectHttpStatus(Throwable exception, Object handler) {
    if (exception instanceof WebClientResponseException cre) {
      return cre.getStatusCode();
    }
    return super.detectHttpStatus(exception, handler);
  }

  @Override
  protected String getError(Throwable exception, HttpStatusCode httpStatusCode) {
    if ((exception instanceof WebClientResponseException cre)
        && !(ObjectUtils.isEmpty(cre.getStatusText()))) {
      return cre.getStatusText();
    }
    return super.getError(exception, httpStatusCode);
  }
}
