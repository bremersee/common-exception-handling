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

package org.bremersee.exception.spring.boot.autoconfigure.reactive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.bremersee.exception.RestApiExceptionConstants;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperForWebFlux;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionMapperBootProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.test.StepVerifier;

/**
 * The api exception handler test.
 *
 * @author Christian Bremer
 */
class ApiExceptionHandlerTest {

  private static ServiceException exception;

  private static ApiExceptionHandler exceptionHandler;

  /**
   * Setup test.
   */
  @BeforeAll
  static void setup() {
    RestApiExceptionMapperBootProperties properties = new RestApiExceptionMapperBootProperties();
    properties.setApiPaths(Collections.singletonList("/api/**"));
    properties.getDefaultExceptionMappingConfig().setIncludeHandler(true);
    properties.getDefaultExceptionMappingConfig().setIncludeStackTrace(false);

    RestApiExceptionMapper mapper = new RestApiExceptionMapperForWebFlux(
        properties.toRestApiExceptionMapperProperties(), "testapp");

    exception = ServiceException.builder()
        .httpStatus(500)
        .reason("Oops, a conflict")
        .errorCode("TEST:4711")
        .build();
    ErrorAttributes errorAttributes = mock(ErrorAttributes.class);
    when(errorAttributes.getError(any(ServerRequest.class))).thenReturn(exception);

    final Resources resources = new Resources();

    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getClassLoader())
        .thenReturn(ApplicationContext.class.getClassLoader());

    final DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();

    exceptionHandler = new ApiExceptionHandler(
        properties.getApiPaths(),
        errorAttributes,
        resources,
        applicationContext,
        codecConfigurer,
        mapper);
    exceptionHandler.setPathMatcher(new AntPathMatcher());
  }

  /**
   * Test get router function.
   */
  @Test
  void testGetRouterFunction() {
    assertThat(exceptionHandler.getRoutingFunction(mock(ErrorAttributes.class)))
        .isNotNull();
  }

  /**
   * Test responsible exception handler.
   */
  @Test
  void testResponsibleExceptionHandler() {
    ServerRequest serverRequest = mock(ServerRequest.class);
    when(serverRequest.path()).thenReturn("/api/resource");
    assertThat(exceptionHandler.isResponsibleExceptionHandler(serverRequest))
        .isTrue();
  }

  /**
   * Test render error response as json.
   */
  @Test
  void testRenderErrorResponseAsJson() {
    doTestingRenderErrorResponse(MediaType.APPLICATION_JSON);
  }

  /**
   * Test render error response as xml.
   */
  @Test
  void testRenderErrorResponseAsXml() {
    doTestingRenderErrorResponse(MediaType.APPLICATION_XML);
  }

  /**
   * Test render error response as something else.
   */
  @Test
  void testRenderErrorResponseAsSomethingElse() {
    doTestingRenderErrorResponse(MediaType.IMAGE_JPEG);
  }

  private void doTestingRenderErrorResponse(MediaType mediaType) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.put(HttpHeaders.ACCEPT, List.of(String.valueOf(mediaType)));
    ServerRequest.Headers headers = mock(ServerRequest.Headers.class);
    when(headers.asHttpHeaders()).thenReturn(httpHeaders);
    when(headers.accept()).thenReturn(httpHeaders.getAccept());

    ServerRequest serverRequest = mock(ServerRequest.class);
    when(serverRequest.path()).thenReturn("/api/resource");
    when(serverRequest.headers()).thenReturn(headers);

    StepVerifier.create(exceptionHandler.renderErrorResponse(serverRequest))
        .assertNext(response -> {
          SoftAssertions softly = new SoftAssertions();
          softly.assertThat(response.statusCode())
              .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

          if (MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)) {
            String actual = response.headers()
                .getFirst(RestApiExceptionConstants.MESSAGE_HEADER_NAME);
            softly.assertThat(actual)
                .isEqualTo(exception.getMessage());
            actual = response.headers()
                .getFirst(RestApiExceptionConstants.CODE_HEADER_NAME);
            softly.assertThat(actual)
                .isEqualTo(exception.getErrorCode());
          }
          softly.assertAll();
        })
        .expectNextCount(0)
        .verifyComplete();
  }

}
