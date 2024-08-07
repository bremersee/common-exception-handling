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

package org.bremersee.exception.webclient;

import java.util.Optional;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.RestApiResponseException;
import org.bremersee.exception.model.RestApiException;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * This web client error decoder generates a {@link RestApiResponseException} from the error
 * response.
 *
 * @author Christian Bremer
 */
public class DefaultWebClientErrorDecoder
    implements WebClientErrorDecoder<RestApiResponseException> {

  private final RestApiExceptionParser parser;

  /**
   * Instantiates a new web client error decoder.
   */
  public DefaultWebClientErrorDecoder() {
    this(null);
  }

  /**
   * Instantiates a new web client error decoder.
   *
   * @param parser the parser
   */
  public DefaultWebClientErrorDecoder(RestApiExceptionParser parser) {
    this.parser = Optional.ofNullable(parser).orElseGet(RestApiExceptionParserImpl::new);
  }

  @Override
  public RestApiResponseException buildException(
      ClientResponse clientResponse,
      String response) {

    RestApiException restApiException = parser.parseException(
        response,
        clientResponse.statusCode(),
        clientResponse.headers().asHttpHeaders());
    return new RestApiResponseException(clientResponse.statusCode(), restApiException);
  }

}
