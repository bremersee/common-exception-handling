/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMapping;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

/**
 * The rest api exception mapper impl test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionMapperForWebTest {

  private static final String APPLICATION_NAME = "test";

  private static RestApiExceptionMapper mapper;

  /**
   * Setup test.
   */
  @BeforeAll
  static void setup() {
    RestApiExceptionMapperProperties properties = RestApiExceptionMapperProperties.builder()
        .apiPaths(List.of("/api/**"))
        .build();
    mapper = new RestApiExceptionMapperForWeb(properties, "test");
  }

  private RestApiExceptionMapperForWeb newInstance(RestApiExceptionMapperProperties properties) {
    return new RestApiExceptionMapperForWeb(properties, APPLICATION_NAME);
  }

  /**
   * Test get api paths.
   */
  @Test
  void testGetApiPaths() {
    RestApiExceptionMapperProperties properties = RestApiExceptionMapperProperties.builder()
        .apiPaths(List.of("/api/**"))
        .build();
    RestApiExceptionMapperForWeb target = newInstance(properties);
    assertThat(target.getApiPaths())
        .containsExactly("/api/**");
  }

  /**
   * Test build 409.
   */
  @Test
  void testBuild409() {
    HttpStatus httpStatus = HttpStatus.CONFLICT;
    String errorCode = "TEST:4711";
    String message = "Either a or b";
    String path = "/api/something";

    ServiceException exception = new ServiceException(httpStatus.value(), errorCode, message);

    RestApiExceptionMapperProperties properties = RestApiExceptionMapperProperties.builder()
        .apiPaths(List.of("/api/**"))
        .build();
    RestApiExceptionMapperForWeb target = newInstance(properties);

    RestApiException actual = target.build(exception, path, null);

    RestApiException expected = RestApiException.builder()
        .status(httpStatus.value())
        .error(httpStatus.getReasonPhrase())
        .errorCode(errorCode)
        .errorCodeInherited(false)
        .message(message)
        .exception(ServiceException.class.getName())
        .application(APPLICATION_NAME)
        .path(path)
        .build();

    assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFieldsOfTypes(OffsetDateTime.class)
        .isEqualTo(expected);
  }

  /**
   * Test build 500.
   */
  @Test
  void testBuild500() {
    ServiceException exception = new ServiceException(500, "Something failed.", "TEST:4711");
    RestApiException model = mapper.build(exception, "/api/something", null);
    assertNotNull(model);
    assertEquals(exception.getErrorCode(), model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/something", model.getPath());
    assertNotNull(model.getId());
  }

  /**
   * Test build with default exception mapping.
   */
  @Test
  void testBuildWithDefaultExceptionMapping() {
    RuntimeException exception = new RuntimeException("Something went wrong");
    RestApiException model = mapper.build(
        exception, "/api/something", null);
    assertNotNull(model);
    assertNull(model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/something", model.getPath());
    assertNotNull(model.getId());
  }

  /**
   * Test build with default exception mapping and illegal argument exception.
   */
  @Test
  void testBuildWithDefaultExceptionMappingAndIllegalArgumentException() {
    IllegalArgumentException exception = new IllegalArgumentException();
    RestApiException model = mapper.build(exception, "/api/illegal", null);
    assertNotNull(model);
    assertNull(model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), model.getMessage());
    assertEquals("/api/illegal", model.getPath());
    assertNull(model.getId());
    assertEquals(IllegalArgumentException.class.getName(), model.getException());
  }

  /**
   * Test build with configured exception mapping.
   */
  @Test
  void testBuildWithConfiguredExceptionMapping() {
    RestApiExceptionMapperProperties properties = RestApiExceptionMapperProperties.builder()
        .apiPaths(List.of("/null-api/**"))
        .addExceptionMappings(ExceptionMapping.builder()
            .exceptionClassName(NullPointerException.class.getName())
            .status(503)
            .message("A variable is null.")
            .code("NULLPOINTER")
            .build())
        .addExceptionMappingConfigs(ExceptionMappingConfig.builder()
            .exceptionClassName(NullPointerException.class.getName())
            .isIncludeException(false)
            .isIncludeApplicationName(true)
            .isIncludePath(true)
            .isIncludeHandler(true)
            .isIncludeStackTrace(true)
            .isIncludeCause(true)
            .isEvaluateAnnotationFirst(true)
            .build())
        .build();
    RestApiExceptionMapper configuredMapper = new RestApiExceptionMapperForWeb(
        properties, "configured");

    NullPointerException exception = new NullPointerException();
    RestApiException model = configuredMapper.build(
        exception, "/null-api/something", null);
    assertNotNull(model);
    assertEquals("NULLPOINTER", model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals("A variable is null.", model.getMessage());
    assertEquals("/null-api/something", model.getPath());
    assertNotNull(model.getId());
    assertNull(model.getException());
  }

  /**
   * Test build with cause.
   */
  @Test
  void testBuildWithCause() {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    RestApiException cause = new RestApiException();
    cause.setApplication("cause");
    cause.setException(ServiceException.class.getName());
    cause.setError("Something");
    cause.setErrorCode("CBR:0123");
    cause.setErrorCodeInherited(false);
    cause.setId("1");
    cause.setMessage("Something failed in service 'cause'");
    cause.setPath("/api/cause");
    cause.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

    ExampleException exception = new ExampleException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        Collections.unmodifiableMap(headers),
        cause);

    /*
    RestApiException model = mapper.build(exception, "/api/this", null);
    assertNotNull(model);
    assertEquals(cause.getErrorCode(), model.getErrorCode());
    assertTrue(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/this", model.getPath());
    assertNotNull(model.getId());
    assertEquals(cause, model.getCause());
    */
  }

  private static class ExampleException extends ResponseStatusException
      implements RestApiExceptionAware {

    private final Map<String, ? extends Collection<String>> headers;

    private final RestApiException restApiException;

    private ExampleException(
        HttpStatus status,
        Map<String, ? extends Collection<String>> headers,
        RestApiException restApiException) {

      super(status);
      this.headers = headers != null ? headers : Collections.emptyMap();
      this.restApiException = restApiException;
    }

    @Override
    public RestApiException getRestApiException() {
      return restApiException;
    }

    @NonNull
    @Override
    public HttpHeaders getResponseHeaders() {
      HttpHeaders httpHeaders = new HttpHeaders();
      for (Map.Entry<String, ? extends Collection<String>> entry : headers.entrySet()) {
        httpHeaders.put(entry.getKey(), List.copyOf(entry.getValue()));
      }
      return httpHeaders;
    }

  }

}
