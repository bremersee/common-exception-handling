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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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

    RestApiResponseException otherException = mock(RestApiResponseException.class);
    when(otherException.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);
    softly.assertThat(target.detectHttpStatus(otherException, null))
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }
}