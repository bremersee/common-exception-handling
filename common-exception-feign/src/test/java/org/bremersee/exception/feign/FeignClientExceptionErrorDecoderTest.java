/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.exception.feign;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The feign client exception error decoder test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class FeignClientExceptionErrorDecoderTest {

  /**
   * Test decode json.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void testDecodeJson(SoftAssertions softly) throws Exception {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder();
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    RestApiException expected = restApiException(HttpStatus.INTERNAL_SERVER_ERROR);
    @SuppressWarnings({"unchecked", "rawtypes"}) Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(getJsonMapper().writeValueAsBytes(expected))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    Exception actual = decoder.decode("getSomething", response);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).status())
        .isEqualTo(500);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).getRestApiException())
        .isEqualTo(expected);
  }

  /**
   * Test decode xml.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void testDecodeXml(SoftAssertions softly) throws Exception {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder(
        new RestApiExceptionParserImpl());
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    RestApiException expected = restApiException(HttpStatus.NOT_FOUND);
    @SuppressWarnings({"unchecked", "rawtypes"}) Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(getXmlMapper().writeValueAsBytes(expected))
        .headers((Map) headers)
        .reason("Nothing found")
        .status(404)
        .build();
    Exception actual = decoder.decode("getSomethingThatNotExists", response);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).status())
        .isEqualTo(404);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).getRestApiException())
        .isEqualTo(expected);
  }

  /**
   * Test decode something else.
   *
   * @param softly the softly
   */
  @Test
  void testDecodeSomethingElse(SoftAssertions softly) {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder(null);
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    String body = "Something failed";
    @SuppressWarnings({"unchecked", "rawtypes"}) Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(body.getBytes(StandardCharsets.UTF_8))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();

    Exception actual = decoder.decode("getSomething", response);
    RestApiException expected = RestApiException.builder()
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("Something failed")
        .build();
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).status())
        .isEqualTo(500);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).getRestApiException())
        .isEqualTo(expected);
  }

  /**
   * Test decode empty response.
   *
   * @param softly the softly
   */
  @Test
  void testDecodeEmptyResponse(SoftAssertions softly) {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder(null);
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
    String body = "";
    @SuppressWarnings({"unchecked", "rawtypes"}) Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(body.getBytes(StandardCharsets.UTF_8))
        .headers((Map) headers)
        .reason("Something bad")
        .status(500)
        .build();
    Exception actual = decoder.decode("getNothing", response);
    RestApiException expected = RestApiException.builder()
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .build();
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).status())
        .isEqualTo(500);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).getRestApiException())
        .isEqualTo(expected);
  }

  /**
   * Get http method.
   *
   * @param softly the softly
   */
  @Test
  void testGetHttpMethod(SoftAssertions softly) {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder();
    softly.assertThat(decoder.getHttpMethod(null))
        .isNull();

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
    @SuppressWarnings({"unchecked", "rawtypes"}) Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body("Not found.".getBytes())
        .headers((Map) headers)
        .reason("Nothing found")
        .status(404)
        .build();
    softly.assertThat(decoder.getHttpMethod(response))
        .isEqualTo(HttpMethod.GET);
  }

  /**
   * Test decode retryable exception.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void testDecodeRetryableException(SoftAssertions softly) throws Exception {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder();
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.add(Util.RETRY_AFTER, "30");
    RestApiException restException = restApiException(HttpStatus.INTERNAL_SERVER_ERROR);
    @SuppressWarnings({"unchecked", "rawtypes"}) Response response = Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body(getJsonMapper().writeValueAsBytes(restException))
        .headers((Map) headers)
        .reason("Something went wrong.")
        .status(500)
        .build();
    Exception actual = decoder.decode("theMethodKey", response);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(RetryableException.class)
        .extracting(exc -> ((RetryableException) exc).method())
        .isEqualTo(HttpMethod.GET);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(RetryableException.class)
        .extracting(exc -> ((RetryableException) exc).retryAfter(), InstanceOfAssertFactories.DATE)
        .isBefore(Instant.now().plusMillis(30001));
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(RetryableException.class)
        .extracting(exc -> ((RetryableException) exc).status())
        .isEqualTo(500);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(RetryableException.class)
        .extracting(Throwable::getMessage)
        .isEqualTo("Status 500 reading theMethodKey");
  }

  /**
   * Determine retry after.
   *
   * @param softly the softly
   */
  @Test
  void tesDetermineRetryAfter(SoftAssertions softly) {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder();

    Optional<Instant> actual = decoder.determineRetryAfter(null);
    softly.assertThat(actual)
        .isEmpty();

    actual = decoder.determineRetryAfter("30");
    softly.assertThat(actual)
        .isPresent()
        .get(InstanceOfAssertFactories.INSTANT)
        .isBefore(Instant.now().plus(Duration.ofMillis(30001)));

    OffsetDateTime expected = OffsetDateTime.parse("2007-12-24T18:21Z");
    String value = expected.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    actual = decoder.determineRetryAfter(value);
    softly.assertThat(actual)
        .isPresent()
        .hasValue(expected.toInstant());
  }

  /**
   * Determine retry after failed.
   */
  @Test
  void testDetermineRetryAfterFailed() {
    FeignClientExceptionErrorDecoder decoder = new FeignClientExceptionErrorDecoder();
    OffsetDateTime expected = OffsetDateTime.parse("2007-12-24T18:21Z");
    String value = expected.format(DateTimeFormatter.ISO_DATE_TIME);
    Optional<Instant> actual = decoder.determineRetryAfter(value);
    assertThat(actual)
        .isEmpty();
  }

  /**
   * Returns json mapper.
   *
   * @return the json mapper
   */
  private static ObjectMapper getJsonMapper() {
    return Jackson2ObjectMapperBuilder.json().build();
  }

  /**
   * Returns xml mapper.
   *
   * @return the xml mapper
   */
  private static XmlMapper getXmlMapper() {
    return Jackson2ObjectMapperBuilder.xml().createXmlMapper(true).build();
  }

  /**
   * Returns a rest api exception.
   *
   * @return the rest api exception
   */
  private static RestApiException restApiException(HttpStatus status) {
    RestApiException restApiException = new RestApiException();
    restApiException.setStatus(status.value());
    restApiException.setError(status.getReasonPhrase());
    restApiException.setApplication("test");
    restApiException.setException(ServiceException.class.getName());
    restApiException.setErrorCode("TEST:4711");
    restApiException.setErrorCodeInherited(false);
    restApiException.setId(UUID.randomUUID().toString());
    restApiException.setMessage("Something failed.");
    restApiException.setPath("/api/something");
    restApiException.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));
    return restApiException;
  }

}
