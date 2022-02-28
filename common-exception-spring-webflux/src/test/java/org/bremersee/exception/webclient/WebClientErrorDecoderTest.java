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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The web client error decoder test.
 *
 * @author Christian Bremer
 */
class WebClientErrorDecoderTest {

  /**
   * Apply with payload.
   */
  @Test
  void applyWithPayload() {
    WebClientErrorDecoder<Exception> target = (clientResponse, response) -> new Exception(response);

    String expectedMessage = "Fatal";
    ClientResponse clientResponse = mock(ClientResponse.class);
    when(clientResponse.bodyToMono(any(Class.class)))
        .thenReturn(Mono.just(expectedMessage));
    StepVerifier.create(target.apply(clientResponse))
        .assertNext(exception -> assertThat(exception.getMessage())
            .isEqualTo(expectedMessage))
        .verifyComplete();
  }

  /**
   * Apply without payload.
   */
  @Test
  void applyWithoutPayload() {
    WebClientErrorDecoder<Exception> target = (clientResponse, response) -> new Exception(response);

    String expectedMessage = "";
    ClientResponse clientResponse = mock(ClientResponse.class);
    when(clientResponse.bodyToMono(any(Class.class)))
        .thenReturn(Mono.empty());
    StepVerifier.create(target.apply(clientResponse))
        .assertNext(exception -> assertThat(exception.getMessage())
            .isEqualTo(expectedMessage))
        .verifyComplete();
  }
}