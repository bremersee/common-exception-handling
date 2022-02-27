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

package org.bremersee.exception.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bremersee.exception.servlet.ApiExceptionResolver.MODEL_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * The api exception resolver test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class, MockitoExtension.class})
class ApiExceptionResolverTest {

  @Mock
  private RestApiExceptionMapper exceptionMapper;

  private ApiExceptionResolver target;

  /**
   * Init.
   */
  @BeforeEach
  void init() {
    target = new ApiExceptionResolver(List.of("/api/**"), exceptionMapper);
  }

  /**
   * Resolve exception with no responsibility.
   */
  @Test
  void resolveExceptionWithNoResponsibility() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("/not");

    HttpServletResponse response = mock(HttpServletResponse.class);

    ServiceException exception = mock(ServiceException.class);

    ModelAndView actual = target.resolveException(request, response, null, exception);

    assertThat(actual)
        .isNull();
  }

  /**
   * Resolve exception with accept json.
   *
   * @param softly the softly
   */
  @Test
  void resolveExceptionWithAcceptJson(SoftAssertions softly) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("/api/resource");
    when(request.getRequestURI()).thenReturn("/api/resource");
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(List.of(HttpHeaders.ACCEPT)));
    when(request.getHeaders(eq(HttpHeaders.ACCEPT)))
        .thenReturn(Collections.enumeration(List.of(MediaType.APPLICATION_JSON_VALUE)));

    when(request.getAttribute(eq(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE)))
        .thenReturn(true);

    RestApiException expected = RestApiException.builder()
        .status(404)
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .build();

    when(exceptionMapper.build(any(Throwable.class), anyString(), any()))
        .thenReturn(expected);

    HttpServletResponse response = mock(HttpServletResponse.class);

    ServiceException exception = mock(ServiceException.class);

    ModelAndView actual = target.resolveException(request, response, null, exception);

    softly.assertThat(actual)
        .isNotNull()
        .extracting(ModelAndView::getStatus)
        .isEqualTo(HttpStatus.NOT_FOUND);
    softly.assertThat(actual)
        .isNotNull()
        .extracting(ModelAndView::getModel)
        .extracting(model -> model.get(MODEL_KEY))
        .isEqualTo(expected);
  }

  /**
   * Resolve exception with accept xml.
   *
   * @param softly the softly
   */
  @Test
  void resolveExceptionWithAcceptXml(SoftAssertions softly) {
    target.setRestApiExceptionIdProvider(request -> "requestId");

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("/api/resource");
    when(request.getRequestURI()).thenReturn("/api/resource");
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(List.of(HttpHeaders.ACCEPT)));
    when(request.getHeaders(eq(HttpHeaders.ACCEPT)))
        .thenReturn(Collections.enumeration(List.of(MediaType.APPLICATION_XML_VALUE)));

    when(request.getAttribute(eq(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE)))
        .thenReturn(null);

    RestApiException expected = RestApiException.builder()
        .status(404)
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .build();

    when(exceptionMapper.build(any(Throwable.class), anyString(), any()))
        .thenReturn(expected);

    HttpServletResponse response = mock(HttpServletResponse.class);

    ServiceException exception = mock(ServiceException.class);

    ModelAndView actual = target.resolveException(request, response, null, exception);

    softly.assertThat(actual)
        .isNotNull()
        .extracting(ModelAndView::getStatus)
        .isEqualTo(HttpStatus.NOT_FOUND);
    softly.assertThat(actual)
        .isNotNull()
        .extracting(ModelAndView::getModel)
        .extracting(model -> model.get(MODEL_KEY))
        .isEqualTo(expected);
  }

  /**
   * Resolve exception.
   *
   * @param softly the softly
   */
  @Test
  void resolveException(SoftAssertions softly) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("/api/resource");
    when(request.getRequestURI()).thenReturn("/api/resource");
    when(request.getHeaderNames())
        .thenReturn(Collections.enumeration(List.of(HttpHeaders.ACCEPT)));
    when(request.getHeaders(eq(HttpHeaders.ACCEPT)))
        .thenReturn(Collections.enumeration(List.of(MediaType.APPLICATION_OCTET_STREAM_VALUE)));

    RestApiException expected = RestApiException.builder()
        .status(404)
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .build();

    when(exceptionMapper.build(any(Throwable.class), anyString(), any()))
        .thenReturn(expected);

    HttpServletResponse response = mock(HttpServletResponse.class);

    ServiceException exception = mock(ServiceException.class);

    ModelAndView actual = target.resolveException(request, response, null, exception);

    softly.assertThat(actual)
        .isNotNull();

  }

  /**
   * Is exception handler responsible with matching servlet path.
   */
  @Test
  void isExceptionHandlerResponsibleWithMatchingServletPath() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("/api/resource");

    boolean actual = target.isExceptionHandlerResponsible(request, new TestRestController());
    assertThat(actual)
        .isTrue();
  }

  /**
   * Is exception handler responsible with no matching servlet path.
   */
  @Test
  void isExceptionHandlerResponsibleWithNoMatchingServletPath() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("/not");

    boolean actual = target.isExceptionHandlerResponsible(request, new TestRestController());
    assertThat(actual)
        .isFalse();
  }

  /**
   * Is exception handler responsible with no matching servlet path and no handler.
   */
  @Test
  void isExceptionHandlerResponsibleWithNoMatchingServletPathAndNoHandler() {
    ApiExceptionResolver targetWithNoApiPaths = new ApiExceptionResolver(
        List.of(), exceptionMapper);
    HttpServletRequest request = mock(HttpServletRequest.class);
    boolean actual = targetWithNoApiPaths.isExceptionHandlerResponsible(request, null);
    assertThat(actual)
        .isFalse();
  }

  /**
   * Is exception handler responsible with no matching servlet path but handler.
   */
  @Test
  void isExceptionHandlerResponsibleWithNoMatchingServletPathButHandler() {
    ApiExceptionResolver targetWithNoApiPaths = new ApiExceptionResolver(
        List.of(), exceptionMapper);
    HttpServletRequest request = mock(HttpServletRequest.class);
    boolean actual = targetWithNoApiPaths
        .isExceptionHandlerResponsible(request, new TestRestController());
    assertThat(actual)
        .isTrue();
  }

  @RestController
  private static class TestRestController {

  }

}