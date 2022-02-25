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

import java.io.IOException;
import java.util.List;
import org.bremersee.exception.model.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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
import org.springframework.lang.NonNull;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=junit",
        // {"timestamp":"2022-02-24T11:10:53.995+00:00","status":500,"error":"Internal Server Error","path":"/spring-error"}
        "server.error.include-exception=true", // "exception":"java.lang.IllegalStateException"
        "server.error.include-message=always", // "message":"Something must be valid"
        //"server.error.include-stacktrace=always" // "trace":"java.lang.IllegalStateException: Something must be valid\n\tat org.breme
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class RestApiExceptionIntegrationTest {

  /**
   * The Rest template builder.
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
   * Rest template rest template.
   *
   * @return the rest template
   */
  RestTemplate restTemplate() {
    return restTemplateBuilder
        .rootUri(baseUrl())
        .errorHandler(new ResponseErrorHandler() {
          @Override
          public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
            System.out.println("Is error? " + response.getStatusCode().isError());
            return false;
          }

          @Override
          public void handleError(@NonNull ClientHttpResponse response) {

          }
        })
        .build();
  }

  @Test
  void springError() {

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_XML));
    HttpEntity<?> httpEntity = new HttpEntity<>(null, headers);
    ResponseEntity<RestApiException> response = restTemplate()
        .exchange("/spring-error", HttpMethod.GET, httpEntity, RestApiException.class);
    System.out.println("Status   = " + response.getStatusCode());
    System.out.println("Response = " + response.getBody());
  }

}
