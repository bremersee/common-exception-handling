/*
 * Copyright 2019 the original author or authors.
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

import java.util.function.Function;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * An error decoder for the {@link org.springframework.web.reactive.function.client.WebClient}.
 *
 * @param <E> the exception type
 * @author Christian Bremer
 */
public interface WebClientErrorDecoder<E extends Throwable>
    extends Function<ClientResponse, Mono<? extends Throwable>> {

  /**
   * Build exception.
   *
   * @param clientResponse the client response
   * @param response the response
   * @return the exception
   */
  E buildException(ClientResponse clientResponse, String response);

}
