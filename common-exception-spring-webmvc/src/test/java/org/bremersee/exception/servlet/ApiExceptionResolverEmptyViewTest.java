/*
 * Copyright 2020-2022 the original author or authors.
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bremersee.exception.RestApiExceptionConstants;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.exception.servlet.ApiExceptionResolver.EmptyView;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * The api exception resolver empty view test.
 *
 * @author Christian Bremer
 */
class ApiExceptionResolverEmptyViewTest {

  /**
   * Render merged output model with no timestamp.
   */
  @Test
  void renderMergedOutputModelWithNoTimestamp() {
    RestApiException restApiException = RestApiException.builder()
        .id("1234")
        .status(404)
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .errorCode("4711")
        .errorCodeInherited(true)
        .message("Fatal")
        .exception("NotFoundException")
        .application("test")
        .path("/api/resource")
        .build();
    EmptyView emptyView = new EmptyView(restApiException, MediaType.TEXT_PLAIN_VALUE);
    Map<String, Object> map = new LinkedHashMap<>();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    emptyView.renderMergedOutputModel(map, request, response);
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.ID_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.CODE_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.CODE_INHERITED_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.MESSAGE_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.EXCEPTION_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.APPLICATION_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.PATH_HEADER_NAME), anyString());
  }

  /**
   * Render merged output model with timestamp.
   */
  @Test
  void renderMergedOutputModelWithTimestamp() {
    RestApiException restApiException = RestApiException.builder()
        .id("1234")
        .timestamp(OffsetDateTime.now())
        .status(404)
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .errorCode("4711")
        .errorCodeInherited(true)
        .message("Fatal")
        .exception("NotFoundException")
        .application("test")
        .path("/api/resource")
        .build();
    EmptyView emptyView = new EmptyView(restApiException, MediaType.TEXT_PLAIN_VALUE);
    Map<String, Object> map = new LinkedHashMap<>();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    emptyView.renderMergedOutputModel(map, request, response);
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.ID_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.CODE_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.CODE_INHERITED_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.MESSAGE_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.EXCEPTION_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.APPLICATION_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.PATH_HEADER_NAME), anyString());
  }

  /**
   * Render merged output model with no values.
   */
  @Test
  void renderMergedOutputModelWithNoValues() {
    RestApiException restApiException = RestApiException.builder()
        .build();
    EmptyView emptyView = new EmptyView(restApiException, MediaType.TEXT_PLAIN_VALUE);
    Map<String, Object> map = new LinkedHashMap<>();
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    emptyView.renderMergedOutputModel(map, request, response);
    verify(response, never())
        .addHeader(eq(RestApiExceptionConstants.ID_HEADER_NAME), anyString());
    verify(response)
        .addHeader(eq(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME), anyString());
    verify(response, never())
        .addHeader(eq(RestApiExceptionConstants.CODE_HEADER_NAME), anyString());
    verify(response, never())
        .addHeader(eq(RestApiExceptionConstants.CODE_INHERITED_HEADER_NAME), anyString());
    verify(response, never())
        .addHeader(eq(RestApiExceptionConstants.MESSAGE_HEADER_NAME), anyString());
    verify(response, never())
        .addHeader(eq(RestApiExceptionConstants.EXCEPTION_HEADER_NAME), anyString());
    verify(response, never())
        .addHeader(eq(RestApiExceptionConstants.APPLICATION_HEADER_NAME), anyString());
    verify(response, never())
        .addHeader(eq(RestApiExceptionConstants.PATH_HEADER_NAME), anyString());
  }

}
