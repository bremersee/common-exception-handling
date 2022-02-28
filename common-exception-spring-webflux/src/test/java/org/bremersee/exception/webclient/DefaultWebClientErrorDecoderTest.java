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

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiResponseException;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientResponse.Headers;

/**
 * The default web client error decoder test.
 *
 * @author Christian Bremer
 */
@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class DefaultWebClientErrorDecoderTest {

  @Mock
  private RestApiExceptionParser restApiExceptionParser;

  @InjectMocks
  private DefaultWebClientErrorDecoder target;

  /**
   * Default constructor.
   */
  @Test
  void defaultConstructor() {
    assertThatNoException()
        .isThrownBy(DefaultWebClientErrorDecoder::new);
  }

  /**
   * Build exception.
   *
   * @param softy the softy
   */
  @Test
  void buildException(SoftAssertions softy) {

    RestApiException restApiException = RestApiException.builder().build();
    when(restApiExceptionParser.parseException(anyString(), any(), any()))
        .thenReturn(restApiException);

    ClientResponse clientResponse = mock(ClientResponse.class);
    when(clientResponse.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    Headers headers = mock(Headers.class);
    when(headers.asHttpHeaders()).thenReturn(new HttpHeaders());
    when(clientResponse.headers()).thenReturn(headers);
    RestApiResponseException actual = target.buildException(clientResponse, "Server response");

    softy.assertThat(actual)
        .isNotNull()
        .extracting(RestApiResponseException::getStatus)
        .isEqualTo(HttpStatus.BAD_REQUEST);
    softy.assertThat(actual)
        .isNotNull()
        .extracting(RestApiResponseException::getRestApiException)
        .isEqualTo(restApiException);
  }

}