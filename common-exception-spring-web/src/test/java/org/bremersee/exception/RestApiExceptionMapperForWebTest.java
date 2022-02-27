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

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ReflectionUtils.findMethod;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.bremersee.exception.model.Handler;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.exception.model.StackTraceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.method.HandlerMethod;

/**
 * The rest api exception mapper for spring web test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionMapperForWebTest {

  private static final String APPLICATION_NAME = "test";

  private RestApiExceptionMapperForWeb targetWithIncludeAll;

  private RestApiExceptionMapperForWeb targetWithIncludeNothing;

  private RestApiExceptionMapperForWeb targetWithIncludeNothingBesidesCause;

  @BeforeEach
  void init() {
    var includeAllProperties = RestApiExceptionMapperProperties
        .builder()
        .defaultExceptionMappingConfig(ExceptionMappingConfig.builder()
            .isIncludeException(true)
            .isIncludeMessage(true)
            .isIncludeApplicationName(true)
            .isIncludePath(true)
            .isIncludeHandler(true)
            .isIncludeStackTrace(true)
            .isIncludeCause(true)
            .isEvaluateAnnotationFirst(false)
            .build())
        .build();
    targetWithIncludeAll = new RestApiExceptionMapperForWeb(
        includeAllProperties, APPLICATION_NAME);

    var includeNothingProperties = RestApiExceptionMapperProperties
        .builder()
        .defaultExceptionMappingConfig(ExceptionMappingConfig.builder()
            .isIncludeException(false)
            .isIncludeMessage(false)
            .isIncludeApplicationName(false)
            .isIncludePath(false)
            .isIncludeHandler(false)
            .isIncludeStackTrace(false)
            .isIncludeCause(false)
            .isEvaluateAnnotationFirst(false)
            .build())
        .build();
    targetWithIncludeNothing = new RestApiExceptionMapperForWeb(
        includeNothingProperties, APPLICATION_NAME);

    var includeNothingBesidesCauseProperties = RestApiExceptionMapperProperties
        .builder()
        .defaultExceptionMappingConfig(ExceptionMappingConfig.builder()
            .isIncludeException(false)
            .isIncludeMessage(false)
            .isIncludeApplicationName(false)
            .isIncludePath(false)
            .isIncludeHandler(false)
            .isIncludeStackTrace(false)
            .isIncludeCause(true)
            .isEvaluateAnnotationFirst(false)
            .build())
        .build();
    targetWithIncludeNothingBesidesCause = new RestApiExceptionMapperForWeb(
        includeNothingBesidesCauseProperties, APPLICATION_NAME);
  }

  @Test
  void buildWithServiceExceptionAndCause(SoftAssertions softly) {
    try {
      new TestHandler().throwServiceExceptionWithCause();

    } catch (ServiceException serviceException) {

      RestApiException actual = targetWithIncludeAll.build(
          serviceException,
          "/api/something",
          handlerMethodOfServiceExceptionWithCause());

      RestApiException expected = getRestApiExceptionOfServiceExceptionWithCause();

      softly.assertThat(actual)
          .usingRecursiveComparison()
          .ignoringFields("timestamp", "stackTrace", "cause.stackTrace")
          .isEqualTo(expected);
      softly.assertThat(actual.getTimestamp())
          .isNotNull();
      softly.assertThat(actual.getStackTrace())
          .isNotEmpty();
      softly.assertThat(actual.getCause().getStackTrace())
          .isNotEmpty();

      actual = targetWithIncludeNothing.build(
          serviceException,
          "/api/something",
          handlerMethodOfServiceExceptionWithCause());

      expected = RestApiException.builder()
          .timestamp(OffsetDateTime.now())
          .status(400)
          .error("Bad Request")
          .errorCode("4711")
          .errorCodeInherited(false)
          .build();

      softly.assertThat(actual)
          .usingRecursiveComparison()
          .ignoringFieldsOfTypes(OffsetDateTime.class)
          .isEqualTo(expected);
    }
  }

  @Test
  void buildWithRestApiResponseException(SoftAssertions softly) {
    try {
      new TestHandler().throwRestApiResponseException();

    } catch (RestApiResponseException restApiResponseException) {

      RestApiException actual = targetWithIncludeAll.build(
          restApiResponseException,
          "/api/something",
          null);

      System.out.println(actual);

      RestApiException expected = RestApiException.builder()
          .timestamp(OffsetDateTime.now())
          .status(400)
          .error("Bad Request")
          .errorCode("4711")
          .errorCodeInherited(true)
          .message("400 BAD_REQUEST")
          .exception("org.bremersee.exception.RestApiResponseException")
          .application("test")
          .path("/api/something")
          .cause(getRestApiExceptionOfServiceExceptionWithCause())
          .build();

      softly.assertThat(actual)
          .usingRecursiveComparison()
          .ignoringFields("timestamp", "stackTrace")
          .isEqualTo(expected);
      softly.assertThat(actual.getTimestamp())
          .isNotNull();
      softly.assertThat(actual.getStackTrace())
          .isNotEmpty();

      actual = targetWithIncludeNothingBesidesCause.build(
          restApiResponseException,
          "/api/something",
          handlerMethodOfServiceExceptionWithCause());

      expected = RestApiException.builder()
          .timestamp(OffsetDateTime.now())
          .status(400)
          .error("Bad Request")
          .errorCode("4711")
          .errorCodeInherited(true)
          .cause(RestApiException.builder()
              .timestamp(OffsetDateTime.parse("2021-12-24T18:21Z"))
              .status(400)
              .error("Bad Request")
              .errorCode("4711")
              .errorCodeInherited(false)
              .cause(RestApiException.builder()
                  .build())
              .build())
          .build();

      softly.assertThat(actual)
          .usingRecursiveComparison()
          .ignoringFieldsOfTypes(OffsetDateTime.class)
          .isEqualTo(expected);
    }
  }

  private static RestApiException getRestApiExceptionOfServiceExceptionWithCause() {
    return RestApiException.builder()
        .timestamp(OffsetDateTime.parse("2021-12-24T18:21Z"))
        .status(400)
        .error("Bad Request")
        .errorCode("4711")
        .errorCodeInherited(false)
        .message("Bad bad request")
        .exception("org.bremersee.exception.ServiceException")
        .application("test")
        .path("/api/something")
        .handler(Handler.builder()
            .className("org.bremersee.exception.RestApiExceptionMapperForWebTest$TestHandler")
            .methodName("throwServiceExceptionWithCause")
            .methodParameterTypes(List.of())
            .build())
        .stackTrace(List.of(
                StackTraceItem.builder()
                    .declaringClass("org.bremersee.exception.ServiceException$1")
                    .methodName("buildWith")
                    .fileName("ServiceException.java")
                    .lineNumber(465)
                    .build()
                // And so on
            )
        )
        .cause(RestApiException.builder()
            .message("Something illegal")
            .exception("java.lang.IllegalArgumentException")
            .stackTrace(List.of(
                    StackTraceItem.builder()
                        .declaringClass(
                            "org.bremersee.exception.RestApiExceptionMapperForWebTest$TestHandler")
                        .methodName("throwServiceExceptionWithCause")
                        .fileName("RestApiExceptionMapperForWebTest.java")
                        .lineNumber(308)
                        .build()
                    // And so on
                )
            )
            .build())
        .build();
  }

  private HandlerMethod handlerMethodOfServiceExceptionWithCause() {
    return new HandlerMethod(
        new TestHandler(),
        requireNonNull(findMethod(TestHandler.class, "throwServiceExceptionWithCause")));
  }

  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  protected static class TestHandler {

    protected void throwServiceExceptionWithCause() {
      IllegalArgumentException cause = new IllegalArgumentException("Something illegal");
      throw ServiceException.badRequest("Bad bad request", "4711", cause);
    }

    protected void throwRestApiResponseException() {
      throw new RestApiResponseException(getRestApiExceptionOfServiceExceptionWithCause());
    }

  }

}
