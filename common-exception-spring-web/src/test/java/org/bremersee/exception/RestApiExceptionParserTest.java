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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * The rest api exception parser test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionParserTest {

  private final RestApiExceptionParser target = new RestApiExceptionParserTestImpl();

  /**
   * Parse exception.
   *
   * @param softly the softly
   * @throws IOException the io exception
   */
  @Test
  void parseException(SoftAssertions softly) throws IOException {
    RestApiException actual = target.parseException(
        (InputStream) null, HttpStatus.BAD_REQUEST, new HttpHeaders());
    RestApiException expected = RestApiException.builder()
        .message("")
        .build();
    softly.assertThat(actual)
        .isEqualTo(expected);

    byte[] content = "Hello world".getBytes(StandardCharsets.UTF_8);
    actual = target.parseException(
        new ByteArrayInputStream(content), HttpStatus.BAD_REQUEST, new HttpHeaders());
    expected = RestApiException.builder()
        .message(new String(content, StandardCharsets.UTF_8))
        .build();
    softly.assertThat(actual)
        .isEqualTo(expected);
  }

  private static class RestApiExceptionParserTestImpl implements RestApiExceptionParser {

    @Override
    public RestApiException parseException(String response, HttpStatusCode httpStatus,
        HttpHeaders headers) {

      return RestApiException.builder()
          .message(response)
          .build();
    }

    @Override
    public RestApiException parseException(byte[] response, HttpStatusCode httpStatus,
        HttpHeaders headers) {

      return parseException(new String(response, StandardCharsets.UTF_8), httpStatus, headers);
    }
  }
}