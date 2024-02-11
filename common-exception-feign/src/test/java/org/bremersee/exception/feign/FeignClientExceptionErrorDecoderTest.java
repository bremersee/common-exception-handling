/*
 * Copyright 2019-2022 the original author or authors.
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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * The feign client exception error decoder test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class, MockitoExtension.class})
class FeignClientExceptionErrorDecoderTest {

  @Mock
  private RestApiExceptionParser restApiExceptionParser;

  @InjectMocks
  private FeignClientExceptionErrorDecoder target;

  /**
   * Test default constructor.
   */
  @Test
  void testDefaultConstructor() {
    assertThatNoException()
        .isThrownBy(FeignClientExceptionErrorDecoder::new);
  }

  /**
   * Test decode.
   *
   * @param softly the softly
   */
  @Test
  void testDecode(SoftAssertions softly) {
    HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    RestApiException expected = createRestApiException(httpStatus);
    when(restApiExceptionParser.parseException(any(byte[].class), eq(httpStatus), any()))
        .thenReturn(expected);

    Response response = createResponse(httpStatus);

    Exception actual = target.decode("getSomethingThatNotExists", response);
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(FeignClientException.class)
        .extracting(exc -> ((FeignClientException) exc).status())
        .isEqualTo(httpStatus.value());
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
    Response response = createResponse(HttpStatus.CONFLICT);
    softly.assertThat(target.getHttpMethod(response))
        .isEqualTo(HttpMethod.GET);

    softly.assertThat(target.getHttpMethod(null))
        .isNull();
  }

  /**
   * Test decode retryable exception.
   *
   * @param softly the softly
   */
  @Test
  void testDecodeRetryableException(SoftAssertions softly) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    RestApiException restException = createRestApiException(httpStatus);
    when(restApiExceptionParser.parseException(any(byte[].class), any(), any()))
        .thenReturn(restException);

    Response response = createResponse(httpStatus, "30");

    Exception actual = target.decode("saveSomething", response);
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
        .isEqualTo(httpStatus.value());
    softly.assertThat(actual)
        .isNotNull()
        .isInstanceOf(RetryableException.class)
        .extracting(Throwable::getMessage)
        .isEqualTo("Status 500 reading saveSomething");
  }

  /**
   * Determine retry after.
   *
   * @param softly the softly
   */
  @Test
  void testDetermineRetryAfter(SoftAssertions softly) {
    Optional<Long> actual = target.determineRetryAfter(null);
    softly.assertThat(actual)
        .isEmpty();

    FeignClientExceptionErrorDecoder spyTarget = spy(target);
    doReturn(0L)
        .when(spyTarget)
        .currentTimeMillis();
    actual = spyTarget.determineRetryAfter("30");
    softly.assertThat(actual)
        .isPresent()
        .get(InstanceOfAssertFactories.LONG)
        .isEqualTo(30000);

    actual = target.determineRetryAfter("30");
    softly.assertThat(actual)
        .isPresent()
        .get(InstanceOfAssertFactories.LONG)
        .isLessThan(System.currentTimeMillis() + 33000);

    OffsetDateTime expected = OffsetDateTime.parse("2007-12-24T18:21Z");
    String value = expected.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    actual = target.determineRetryAfter(value);
    softly.assertThat(actual)
        .isPresent()
        .hasValue(expected.toInstant().toEpochMilli());
  }

  /**
   * Determine retry after failed.
   */
  @Test
  void testDetermineRetryAfterFailed() {
    OffsetDateTime expected = OffsetDateTime.parse("2007-12-24T18:21Z");
    String value = expected.format(DateTimeFormatter.ISO_DATE_TIME);
    Optional<Long> actual = target.determineRetryAfter(value);
    assertThat(actual)
        .isEmpty();
  }

  private static Response createResponse(HttpStatus status) {
    return createResponse(status, null);
  }

  private static Response createResponse(HttpStatus status, String retry) {
    Map<String, Collection<String>> headers = new LinkedHashMap<>();
    headers.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
    if (Objects.nonNull(retry)) {
      headers.put(Util.RETRY_AFTER, List.of(retry));
    }
    return Response
        .builder()
        .request(Request
            .create(
                HttpMethod.GET,
                "https://example.org",
                new HashMap<>(),
                null,
                StandardCharsets.UTF_8,
                null))
        .body("Response from server".getBytes(StandardCharsets.UTF_8)) // parser is mocked
        .headers(headers)
        .reason(status.getReasonPhrase())
        .status(status.value())
        .build();

  }

  private static RestApiException createRestApiException(HttpStatus status) {
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
