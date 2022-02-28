/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.exception.spring.boot.autoconfigure.reactive;

import java.time.OffsetDateTime;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiResponseException;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.exception.spring.boot.autoconfigure.reactive.app.TestConfiguration;
import org.bremersee.exception.webclient.DefaultWebClientErrorDecoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * The reactive integration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.application.name=test",
        "bremersee.exception-mapping.api-paths=/api/**"
    }
)
@ExtendWith(SoftAssertionsExtension.class)
class ReactiveIntegrationTest {

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * The error decoder.
   */
  @Autowired(required = false)
  DefaultWebClientErrorDecoder errorDecoder;

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Creates a new web client, that uses the real security configuration.
   *
   * @return the web client
   */
  WebClient newWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl())
        .build();
  }

  /**
   * Fetch rest api exception.
   *
   * @param softly the softly
   */
  @Test
  void fetchRestApiException(SoftAssertions softly) {
    softly.assertThat(errorDecoder)
        .isNotNull();

    StepVerifier.create(newWebClient()
            .get()
            .uri("/api/exception")
            .retrieve()
            .onStatus(HttpStatus::isError, errorDecoder)
            .bodyToMono(String.class))
        .expectErrorMatches(throwable -> {
          if (throwable instanceof RestApiResponseException) {
            RestApiResponseException exception = (RestApiResponseException) throwable;
            RestApiException expected = RestApiException.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .errorCode("1234")
                .errorCodeInherited(false)
                .message("Entity with identifier [MyEntity] already exists.")
                .exception("org.bremersee.exception.ServiceException")
                .application("test")
                .path("/api/exception")
                .build();
            softly.assertThat(exception.getRestApiException())
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(OffsetDateTime.class)
                .isEqualTo(expected);
            return true;
          }
          return false;
        })
        .verify();
  }

}
