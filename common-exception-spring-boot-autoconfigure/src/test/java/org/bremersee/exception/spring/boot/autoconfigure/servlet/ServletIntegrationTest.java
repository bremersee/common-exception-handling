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

package org.bremersee.exception.spring.boot.autoconfigure.servlet;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.okhttp.OkHttpClient;
import java.time.OffsetDateTime;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiResponseException;
import org.bremersee.exception.feign.FeignClientException;
import org.bremersee.exception.feign.FeignClientExceptionErrorDecoder;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.exception.spring.boot.autoconfigure.servlet.app.TestConfiguration;
import org.bremersee.exception.spring.boot.autoconfigure.servlet.app.TestConfiguration.TestRestController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.http.HttpStatus;

/**
 * The servlet integration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = {TestConfiguration.class, FeignClientsConfiguration.class},
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=servlet",
        "spring.application.name=test",
        "bremersee.exception-mapping.api-paths=/api/**"
    }
)
@ExtendWith(SoftAssertionsExtension.class)
class ServletIntegrationTest {

  /**
   * The rest template builder.
   */
  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  /**
   * The Contract.
   */
  @Autowired
  Contract contract;

  /**
   * The Encoder.
   */
  @Autowired
  Encoder encoder;

  /**
   * The Decoder.
   */
  @Autowired
  Decoder decoder;

  /**
   * The Feign client exception error decoder.
   */
  @Autowired
  FeignClientExceptionErrorDecoder feignClientExceptionErrorDecoder;

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
   * Fetch rest api exception.
   */
  @Test
  void fetchRestApiException() {
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
    //noinspection ResultOfMethodCallIgnored
    assertThatThrownBy(() -> restTemplateBuilder.build()
        .getForEntity(baseUrl() + "/api/exception", String.class)
        .getBody())
        .isInstanceOf(RestApiResponseException.class)
        .extracting(exc -> ((RestApiResponseException) exc).getRestApiException())
        .usingRecursiveComparison()
        .ignoringFieldsOfTypes(OffsetDateTime.class)
        .isEqualTo(expected);
  }

  /**
   * Fetch rest api exception with feign.
   */
  @Test
  void fetchRestApiExceptionWithFeign() {
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

    TestRestController proxy = Feign.builder()
        .client(new OkHttpClient())
        .contract(contract)
        .encoder(encoder)
        .decoder(decoder)
        .errorDecoder(feignClientExceptionErrorDecoder)
        .target(TestRestController.class, baseUrl());

    assertThatThrownBy(proxy::throwServiceException)
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).getRestApiException())
        .usingRecursiveComparison()
        .ignoringFieldsOfTypes(OffsetDateTime.class)
        .isEqualTo(expected);
  }

}
