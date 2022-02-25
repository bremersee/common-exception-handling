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

package org.bremersee.exception.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.model.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * The rest api exception integration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=junit",
        "server.error.include-exception=true",
        "server.error.include-message=always"
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
@ExtendWith(SoftAssertionsExtension.class)
public class RestApiExceptionIntegrationTest {

  /**
   * The rest template builder.
   */
  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Rest template.
   *
   * @return the rest template
   */
  RestTemplate restTemplate() {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .errorHandler(new IgnoreErrorsHandler())
        .build();
  }

  /**
   * Spring error as json.
   *
   * @param softly the softly
   */
  @Test
  void springErrorAsJson(SoftAssertions softly) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    HttpEntity<?> httpEntity = new HttpEntity<>(null, headers);
    ResponseEntity<RestApiException> response = restTemplate()
        .exchange("/spring-error", HttpMethod.GET, httpEntity, RestApiException.class);
    RestApiException expected = RestApiException.builder()
        .status(500)
        .error("Internal Server Error")
        .message("Something must be valid")
        .exception("java.lang.IllegalStateException")
        .path("/spring-error")
        .build();
    expected.furtherDetails("locale", "de-DE");
    expected.furtherDetails("custom", Map.of("key", "value"));
    softly.assertThat(response.getBody())
        .usingRecursiveComparison()
        .ignoringFieldsOfTypes(OffsetDateTime.class)
        .isEqualTo(expected);
    softly.assertThat(response.getBody())
        .extracting(RestApiException::furtherDetails,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .containsAllEntriesOf(expected.furtherDetails());
  }

  /**
   * Spring error as xml.
   *
   * @param softly the softly
   */
  @Test
  void springErrorAsXml(SoftAssertions softly) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_XML));
    HttpEntity<?> httpEntity = new HttpEntity<>(null, headers);
    ResponseEntity<RestApiException> response = restTemplate()
        .exchange("/spring-error", HttpMethod.GET, httpEntity, RestApiException.class);
    RestApiException expected = RestApiException.builder()
        .status(500)
        .error("Internal Server Error")
        .message("Something must be valid")
        .exception("java.lang.IllegalStateException")
        .path("/spring-error")
        .build();
    expected.furtherDetails("locale", "de-DE");
    expected.furtherDetails("custom", Map.of("key", "value"));
    softly.assertThat(response.getBody())
        .usingRecursiveComparison()
        .ignoringFieldsOfTypes(OffsetDateTime.class)
        .isEqualTo(expected);
    softly.assertThat(response.getBody())
        .extracting(RestApiException::furtherDetails,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .containsAllEntriesOf(expected.furtherDetails());
  }

  private static class IgnoreErrorsHandler implements ResponseErrorHandler {

    private IgnoreErrorsHandler() {
    }

    @Override
    public boolean hasError(ClientHttpResponse response) {
      return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) {

    }
  }

}
