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
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMapping;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
class RestApiExceptionMapperImplTest {

  private static RestApiExceptionMapper mapper;

  /**
   * Setup test.
   */
  @BeforeAll
  static void setup() {
    RestApiExceptionMapperProperties properties = RestApiExceptionMapperProperties.builder()
        .apiPaths(List.of("/api/**"))
        .build();
    mapper = new RestApiExceptionMapperImpl(properties, "test");
  }

  /**
   * Test get api paths.
   */
  @Test
  void testGetApiPaths() {
    assertTrue(mapper.getApiPaths().contains("/api/**"));
  }

  /**
   * Test build 409.
   */
  @Test
  void testBuild409() {
    final ServiceException exception = new ServiceException(409, "Either a or b", "TEST:4711");
    final RestApiException model = mapper.build(exception, "/api/something", null);
    assertNotNull(model);
    assertEquals(exception.getErrorCode(), model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/something", model.getPath());
    assertNull(model.getId());
  }

  /**
   * Test build 500.
   */
  @Test
  void testBuild500() {
    final ServiceException exception = new ServiceException(500, "Something failed.", "TEST:4711");
    final RestApiException model = mapper.build(exception, "/api/something", null);
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
    final RuntimeException exception = new RuntimeException("Something went wrong");
    final RestApiException model = mapper.build(
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
    final IllegalArgumentException exception = new IllegalArgumentException();
    final RestApiException model = mapper.build(exception, "/api/illegal", null);
    assertNotNull(model);
    assertNull(model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), model.getMessage());
    assertEquals("/api/illegal", model.getPath());
    assertNull(model.getId());
    assertEquals(IllegalArgumentException.class.getName(), model.getClassName());
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
            .isIncludeExceptionClassName(false)
            .isIncludeApplicationName(true)
            .isIncludePath(true)
            .isIncludeHandler(true)
            .isIncludeStackTrace(true)
            .isIncludeCause(true)
            .isEvaluateAnnotationFirst(true)
            .build())
        .build();
    final RestApiExceptionMapper configuredMapper = new RestApiExceptionMapperImpl(
        properties, "configured");

    final NullPointerException exception = new NullPointerException();
    final RestApiException model = configuredMapper.build(
        exception, "/null-api/something", null);
    assertNotNull(model);
    assertEquals("NULLPOINTER", model.getErrorCode());
    assertFalse(model.getErrorCodeInherited());
    assertEquals("A variable is null.", model.getMessage());
    assertEquals("/null-api/something", model.getPath());
    assertNotNull(model.getId());
    assertNull(model.getClassName());
  }

  /**
   * Test build with cause.
   */
  @Test
  void testBuildWithCause() {
    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    final RestApiException cause = new RestApiException();
    cause.setApplication("cause");
    cause.setClassName(ServiceException.class.getName());
    cause.setErrorCode("CBR:0123");
    cause.setErrorCodeInherited(false);
    cause.setId("1");
    cause.setMessage("Something failed in service 'cause'");
    cause.setPath("/api/cause");
    cause.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

    final ExampleException exception = new ExampleException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        Collections.unmodifiableMap(headers),
        cause);

    final RestApiException model = mapper.build(exception, "/api/this", null);
    assertNotNull(model);
    assertEquals(cause.getErrorCode(), model.getErrorCode());
    assertTrue(model.getErrorCodeInherited());
    assertEquals(exception.getMessage(), model.getMessage());
    assertEquals("/api/this", model.getPath());
    assertNotNull(model.getId());
    assertEquals(cause, model.getCause());
  }

  private static class ExampleException extends ResponseStatusException
      implements RestApiExceptionAware {

    private final Map<String, ? extends Collection<String>> headers;

    private final RestApiException restApiException;

    private ExampleException(
        final HttpStatus status,
        final Map<String, ? extends Collection<String>> headers,
        final RestApiException restApiException) {

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
