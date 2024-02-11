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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/**
 * The rest api exception mapper for web flux test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionMapperForWebFluxTest {

  private RestApiExceptionMapperForWebFlux target;

  /**
   * Init.
   */
  @BeforeEach
  void init() {
    target = new RestApiExceptionMapperForWebFlux(
        RestApiExceptionMapperProperties.builder().build(),
        "test");
  }

  /**
   * Detect http status.
   *
   * @param softly the softly
   */
  @Test
  void detectHttpStatus(SoftAssertions softly) {
    WebClientResponseException exception = mock(WebClientResponseException.class);
    when(exception.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
    softly.assertThat(target.detectHttpStatus(exception, null))
        .isEqualTo(HttpStatus.CONFLICT);

    RestApiResponseException otherException = new RestApiResponseException(
        RestApiException.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .build());
    softly.assertThat(target.detectHttpStatus(otherException, null))
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  /**
   * Gets error.
   *
   * @param softly the softly
   */
  @Test
  void getError(SoftAssertions softly) {
    String expected = "Missing value xyz";
    ResponseStatusException rse = new ResponseStatusException(HttpStatus.BAD_REQUEST, expected);
    String actual = target.getError(rse, HttpStatus.BAD_REQUEST);
    softly
        .assertThat(actual)
        .isEqualTo(expected);

    rse = new ResponseStatusException(HttpStatus.BAD_REQUEST);
    actual = target.getError(rse, HttpStatus.BAD_REQUEST);
    softly
        .assertThat(actual)
        .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());

    actual = target
        .getError(new Exception("Test exception"), HttpStatusCode.valueOf(599));
    softly
        .assertThat(actual)
        .isNull();
  }

}