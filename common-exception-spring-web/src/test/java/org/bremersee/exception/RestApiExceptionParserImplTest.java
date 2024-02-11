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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * The rest api exception parser impl test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionParserImplTest {

  private final RestApiExceptionParserImpl target = new RestApiExceptionParserImpl(
      StandardCharsets.US_ASCII);

  private final RestApiExceptionParserImpl targetWithDefaults = new RestApiExceptionParserImpl();

  /**
   * Gets default charset.
   */
  @Test
  void getDefaultCharset() {
    assertThat(target.getDefaultCharset())
        .isEqualTo(StandardCharsets.US_ASCII);
  }

  /**
   * Gets object mapper.
   *
   * @param softly the softly
   */
  @Test
  void getObjectMapper(SoftAssertions softly) {
    softly.assertThat(targetWithDefaults.getObjectMapper(RestApiResponseType.JSON))
        .isPresent()
        .get()
        .isInstanceOf(ObjectMapper.class);
    softly.assertThat(targetWithDefaults.getObjectMapper(RestApiResponseType.XML))
        .isPresent()
        .get()
        .isInstanceOf(XmlMapper.class);
    softly.assertThat(targetWithDefaults.getObjectMapper(RestApiResponseType.HEADER))
        .isEmpty();
  }

  /**
   * Parse exception.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void parseException(SoftAssertions softly) throws Exception {
    HttpStatus httpStatus = HttpStatus.FORBIDDEN;
    HttpHeaders httpHeaders = new HttpHeaders();

    RestApiException actual = targetWithDefaults.parseException(
        (byte[]) null, httpStatus, httpHeaders);
    RestApiException expected = RestApiException.builder()
        .status(HttpStatus.FORBIDDEN.value())
        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
        .build();
    softly.assertThat(actual)
        .isEqualTo(expected);

    actual = targetWithDefaults.parseException(
        "Something failed".getBytes(StandardCharsets.US_ASCII), httpStatus, httpHeaders);
    expected = expected.toBuilder()
        .message("Something failed")
        .build();
    softly.assertThat(actual)
        .isEqualTo(expected);

    httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    actual = targetWithDefaults.parseException(
        "Something failed", httpStatus, httpHeaders);
    softly.assertThat(actual)
        .isEqualTo(expected);

    String response = Jackson2ObjectMapperBuilder.json().build().writeValueAsString(expected);
    actual = targetWithDefaults.parseException(
        response, httpStatus, httpHeaders);
    softly.assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets rest api exception from headers.
   *
   * @param softly the softly
   */
  @Test
  void getRestApiExceptionFromHeaders(SoftAssertions softly) {
    HttpStatus httpStatus = HttpStatus.FORBIDDEN;
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(RestApiExceptionConstants.ID_HEADER_NAME, "1234");
    httpHeaders.add(
        RestApiExceptionConstants.TIMESTAMP_HEADER_NAME,
        OffsetDateTime.parse("2022-02-28T02:31:22Z")
            .format(RestApiExceptionConstants.TIMESTAMP_FORMATTER));
    httpHeaders.add(RestApiExceptionConstants.CODE_HEADER_NAME, "5005");
    httpHeaders.add(RestApiExceptionConstants.CODE_INHERITED_HEADER_NAME, "true");
    httpHeaders.add(RestApiExceptionConstants.EXCEPTION_HEADER_NAME, "exc");
    httpHeaders.add(RestApiExceptionConstants.APPLICATION_HEADER_NAME, "app");
    httpHeaders.add(RestApiExceptionConstants.PATH_HEADER_NAME, "/path");

    RestApiException actual = target.getRestApiExceptionFromHeaders(
        "no message in headers",
        httpStatus,
        httpHeaders);

    RestApiException expected = RestApiException.builder()
        .status(HttpStatus.FORBIDDEN.value())
        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
        .id("1234")
        .timestamp(OffsetDateTime.parse("2022-02-28T02:31:22Z"))
        .errorCode("5005")
        .errorCodeInherited(true)
        .exception("exc")
        .application("app")
        .path("/path")
        .message("no message in headers")
        .build();

    softly.assertThat(actual)
        .isEqualTo(expected);

    httpHeaders.add(RestApiExceptionConstants.MESSAGE_HEADER_NAME, "given");
    actual = target.getRestApiExceptionFromHeaders(
        "no message in headers",
        httpStatus,
        httpHeaders);
    expected = expected.toBuilder()
        .message("given")
        .build();
    softly.assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets content type charset.
   *
   * @param softly the softly
   */
  @Test
  void getContentTypeCharset(SoftAssertions softly) {
    Charset actual = target.getContentTypeCharset(null);
    softly.assertThat(actual)
        .isEqualTo(target.getDefaultCharset());

    actual = target.getContentTypeCharset(MediaType.APPLICATION_JSON);
    softly.assertThat(actual)
        .isEqualTo(StandardCharsets.UTF_8);

    actual = target.getContentTypeCharset(MediaType.APPLICATION_XML);
    softly.assertThat(actual)
        .isEqualTo(StandardCharsets.UTF_8);

    MediaType mediaType = MediaType.parseMediaType(
        MediaType.APPLICATION_JSON_VALUE + ";charset=" + StandardCharsets.ISO_8859_1.name());
    actual = target.getContentTypeCharset(mediaType);
    softly.assertThat(actual)
        .isEqualTo(StandardCharsets.UTF_8);

    mediaType = MediaType.parseMediaType(
        MediaType.APPLICATION_XML_VALUE + ";charset=" + StandardCharsets.ISO_8859_1.name());
    actual = target.getContentTypeCharset(mediaType);
    softly.assertThat(actual)
        .isEqualTo(StandardCharsets.ISO_8859_1);

    mediaType = MediaType.parseMediaType(
        MediaType.TEXT_PLAIN_VALUE + ";charset=" + StandardCharsets.ISO_8859_1.name());
    actual = target.getContentTypeCharset(mediaType);
    softly.assertThat(actual)
        .isEqualTo(StandardCharsets.ISO_8859_1);
  }

  /**
   * Parse error timestamp.
   *
   * @param softly the softly
   */
  @Test
  void parseErrorTimestamp(SoftAssertions softly) {
    OffsetDateTime actual = target.parseErrorTimestamp(null);
    softly.assertThat(actual)
        .isNull();

    actual = target.parseErrorTimestamp("illegal");
    softly.assertThat(actual)
        .isNull();

    OffsetDateTime expected = OffsetDateTime.parse("2022-02-28T02:31:22Z");
    actual = target.parseErrorTimestamp(expected
        .format(RestApiExceptionConstants.TIMESTAMP_FORMATTER));
    softly.assertThat(actual)
        .isEqualTo(expected);
  }
}