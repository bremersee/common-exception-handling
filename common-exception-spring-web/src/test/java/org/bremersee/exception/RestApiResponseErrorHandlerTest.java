/*
 * Copyright 2022 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * The rest api response error handler test.
 *
 * @author Christian Bremer
 */
@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class RestApiResponseErrorHandlerTest {

  @Mock
  private RestApiExceptionParser restApiExceptionParser;

  @InjectMocks
  private RestApiResponseErrorHandler target;

  /**
   * Default constructor.
   */
  @Test
  void defaultConstructor() {
    assertThatNoException()
        .isThrownBy(RestApiResponseErrorHandler::new);
  }

  /**
   * Has error.
   *
   * @param softly the softly
   * @throws IOException the io exception
   */
  @Test
  void hasError(SoftAssertions softly) throws IOException {
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    when(response.getStatusCode())
        .thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    softly.assertThat(target.hasError(response))
        .isTrue();

    response = mock(ClientHttpResponse.class);
    when(response.getStatusCode())
        .thenReturn(HttpStatus.BAD_REQUEST);
    softly.assertThat(target.hasError(response))
        .isTrue();

    response = mock(ClientHttpResponse.class);
    when(response.getStatusCode())
        .thenReturn(HttpStatus.TEMPORARY_REDIRECT);
    softly.assertThat(target.hasError(response))
        .isFalse();

    response = mock(ClientHttpResponse.class);
    when(response.getStatusCode())
        .thenReturn(HttpStatus.OK);
    softly.assertThat(target.hasError(response))
        .isFalse();
  }

  /**
   * Handle error.
   *
   * @throws IOException the io exception
   */
  @Test
  void handleError() throws IOException {
    RestApiException restApiException = RestApiException.builder()
        .status(HttpStatus.FORBIDDEN.value())
        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
        .build();
    when(restApiExceptionParser.parseException(any(InputStream.class), any(), any()))
        .thenReturn(restApiException);
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream(new byte[0]));
    when(response.getStatusCode())
        .thenReturn(HttpStatus.FORBIDDEN);
    when(response.getHeaders())
        .thenReturn(new HttpHeaders());
    assertThatThrownBy(() -> target.handleError(response))
        .isInstanceOf(RestApiResponseException.class)
        .asInstanceOf(InstanceOfAssertFactories.type(RestApiResponseException.class))
        .extracting(RestApiResponseException::getRestApiException)
        .isEqualTo(restApiException);
  }
}