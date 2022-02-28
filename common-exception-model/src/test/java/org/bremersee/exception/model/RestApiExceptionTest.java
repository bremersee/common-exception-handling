/*
 * Copyright 2018-2022 the original author or authors.
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

package org.bremersee.exception.model;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The rest api exception test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionTest {

  /**
   * Gets id.
   *
   * @param softly the soft assertions
   */
  @Test
  void getId(SoftAssertions softly) {
    RestApiException model = new RestApiException();
    model.setId("value");
    softly.assertThat(model.getId()).isEqualTo("value");

    model = RestApiException.builder().id("value").build();
    softly.assertThat(model.getId()).isEqualTo("value");

    softly.assertThat(model).isNotEqualTo(null);
    softly.assertThat(model).isNotEqualTo(new Object());
    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().id("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets timestamp.
   *
   * @param softly the soft assertions
   */
  @Test
  void getTimestamp(SoftAssertions softly) {
    OffsetDateTime value = OffsetDateTime.now();
    RestApiException model = new RestApiException();
    model.setTimestamp(value);
    softly.assertThat(model.getTimestamp()).isEqualTo(value);

    model = RestApiException.builder().timestamp(value).build();
    softly.assertThat(model.getTimestamp()).isEqualTo(value);

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().timestamp(value).build());

    softly.assertThat(model.toString()).contains(value.toString());
  }

  /**
   * Gets status.
   *
   * @param softly the soft assertions
   */
  @Test
  void getStatus(SoftAssertions softly) {
    Integer value = 404;
    RestApiException model = new RestApiException();
    model.setStatus(value);
    softly.assertThat(model.getStatus()).isEqualTo(value);

    model = RestApiException.builder().status(value).build();
    softly.assertThat(model.getStatus()).isEqualTo(value);

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().status(value).build());

    softly.assertThat(model.toString()).contains(value.toString());
  }

  /**
   * Gets error.
   *
   * @param softly the soft assertions
   */
  @Test
  void getError(SoftAssertions softly) {
    String value = "not found";
    RestApiException model = new RestApiException();
    model.setError(value);
    softly.assertThat(model.getError()).isEqualTo(value);

    model = RestApiException.builder().error(value).build();
    softly.assertThat(model.getError()).isEqualTo(value);

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().error(value).build());

    softly.assertThat(model.toString()).contains(value);
  }

  /**
   * Gets error code.
   *
   * @param softly the soft assertions
   */
  @Test
  void getErrorCode(SoftAssertions softly) {
    RestApiException model = new RestApiException();
    model.setErrorCode("value");
    softly.assertThat(model.getErrorCode()).isEqualTo("value");

    model = RestApiException.builder().errorCode("value").build();
    softly.assertThat(model.getErrorCode()).isEqualTo("value");

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().errorCode("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets error code inherited.
   *
   * @param softly the soft assertions
   */
  @Test
  void getErrorCodeInherited(SoftAssertions softly) {
    RestApiException model = new RestApiException();

    model.setErrorCodeInherited(null);
    softly.assertThat(model.getErrorCodeInherited()).isFalse();

    model.setErrorCodeInherited(Boolean.TRUE);
    softly.assertThat(model.getErrorCodeInherited()).isTrue();

    model = RestApiException.builder().errorCodeInherited(Boolean.TRUE).build();
    softly.assertThat(model.getErrorCodeInherited()).isTrue();

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().errorCodeInherited(Boolean.TRUE).build());

    softly.assertThat(model.toString()).contains(Boolean.TRUE.toString());
  }

  /**
   * Gets message.
   *
   * @param softly the soft assertions
   */
  @Test
  void getMessage(SoftAssertions softly) {
    RestApiException model = new RestApiException();
    model.setMessage("value");
    softly.assertThat(model.getMessage()).isEqualTo("value");

    model = RestApiException.builder().message("value").build();
    softly.assertThat(model.getMessage()).isEqualTo("value");

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().message("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets class name.
   *
   * @param softly the soft assertions
   */
  @Test
  void getClassName(SoftAssertions softly) {
    RestApiException model = new RestApiException();
    model.setException("value");
    softly.assertThat(model.getException()).isEqualTo("value");

    model = RestApiException.builder().exception("value").build();
    softly.assertThat(model.getException()).isEqualTo("value");

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().exception("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets application.
   *
   * @param softly the soft assertions
   */
  @Test
  void getApplication(SoftAssertions softly) {
    RestApiException model = new RestApiException();
    model.setApplication("value");
    softly.assertThat(model.getApplication()).isEqualTo("value");

    model = RestApiException.builder().application("value").build();
    softly.assertThat(model.getApplication()).isEqualTo("value");

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().application("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets path.
   *
   * @param softly the soft assertions
   */
  @Test
  void getPath(SoftAssertions softly) {
    RestApiException model = new RestApiException();
    model.setPath("value");
    softly.assertThat(model.getPath()).isEqualTo("value");

    model = RestApiException.builder().path("value").build();
    softly.assertThat(model.getPath()).isEqualTo("value");

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().path("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets handler.
   *
   * @param softly the soft assertions
   */
  @Test
  void getHandler(SoftAssertions softly) {
    Handler value = Handler.builder()
        .className("org.example.FooBar")
        .methodName("getFooBar")
        .build();
    RestApiException model = new RestApiException();
    model.setHandler(value);
    softly.assertThat(model.getHandler()).isEqualTo(value);

    model = RestApiException.builder().handler(value).build();
    softly.assertThat(model.getHandler()).isEqualTo(value);

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().handler(value).build());

    softly.assertThat(model.toString()).contains(value.toString());
  }

  /**
   * Gets stack trace.
   *
   * @param softly the soft assertions
   */
  @Test
  void getStackTrace(SoftAssertions softly) {
    List<StackTraceItem> value = Collections.singletonList(StackTraceItem.builder()
        .fileName("filename")
        .lineNumber(123)
        .methodName("someMethod")
        .build());
    RestApiException model = new RestApiException();
    model.setStackTrace(value);
    softly.assertThat(model.getStackTrace()).isEqualTo(value);

    model = RestApiException.builder().stackTrace(value).build();
    softly.assertThat(model.getStackTrace()).isEqualTo(value);

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().stackTrace(value).build());

    softly.assertThat(model.toString()).contains(value.toString());
  }

  /**
   * Gets cause.
   *
   * @param softly the soft assertions
   */
  @Test
  void getCause(SoftAssertions softly) {
    RestApiException value = RestApiException.builder()
        .id(UUID.randomUUID().toString())
        .application("test")
        .path("/api/somewhere")
        .message("value")
        .errorCode("1234")
        .errorCodeInherited(Boolean.FALSE)
        .build();
    RestApiException model = new RestApiException();
    model.setCause(value);
    softly.assertThat(model.getCause()).isEqualTo(value);

    model = RestApiException.builder().cause(value).build();
    softly.assertThat(model.getCause()).isEqualTo(value);

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().cause(value).build());

    softly.assertThat(model.toString()).contains(value.toString());
  }

  /**
   * Builder.
   */
  @Test
  void builder() {
    assertThat(RestApiException.builder().build())
        .isEqualTo(new RestApiException(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null));
  }

  /**
   * Json.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void json(SoftAssertions softly) throws Exception {
    RestApiException cause = RestApiException.builder()
        .id(UUID.randomUUID().toString())
        .application("test")
        .path("/api/somewhere")
        .message("value")
        .errorCode("1234")
        .errorCodeInherited(Boolean.FALSE)
        .timestamp(OffsetDateTime.parse("2007-12-24T18:20Z", ISO_OFFSET_DATE_TIME))
        .build();
    Handler handler = Handler.builder()
        .methodName("getSomething")
        .methodParameterTypes(Arrays.asList("java.lang.String", "java.lang.Boolean"))
        .className("org.bremersee.SomethingController")
        .build();
    StackTraceItem i0 = StackTraceItem.builder()
        .methodName("getSomething")
        .lineNumber(123)
        .declaringClass("org.bremersee.SomethingController")
        .build();
    StackTraceItem i1 = StackTraceItem.builder()
        .methodName("findSomething")
        .lineNumber(456)
        .declaringClass("org.bremersee.SomethingRepository")
        .build();
    RestApiException model = RestApiException.builder()
        .cause(cause)
        .handler(handler)
        .stackTrace(Arrays.asList(i0, i1))
        .errorCodeInherited(true)
        .errorCode(cause.getErrorCode())
        .path("(api/something")
        .id("5678")
        .message("Something went wrong.")
        .application("junit")
        .timestamp(OffsetDateTime.parse("2007-12-24T18:21Z", ISO_OFFSET_DATE_TIME))
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
    // System.out.println(json);

    RestApiException actualModel = objectMapper.readValue(json, RestApiException.class);
    softly.assertThat(actualModel).isEqualTo(model);
  }

  /**
   * Xml.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void xml(SoftAssertions softly) throws Exception {
    RestApiException cause = RestApiException.builder()
        .id(UUID.randomUUID().toString())
        .application("test")
        .path("/api/somewhere")
        .message("value")
        .errorCode("1234")
        .errorCodeInherited(Boolean.FALSE)
        .timestamp(OffsetDateTime.parse("2007-12-24T18:20Z", ISO_OFFSET_DATE_TIME))
        .build();
    Handler handler = Handler.builder()
        .methodName("getSomething")
        .methodParameterTypes(Arrays.asList("java.lang.String", "java.lang.Boolean"))
        .className("org.bremersee.SomethingController")
        .build();
    StackTraceItem i0 = StackTraceItem.builder()
        .methodName("getSomething")
        .lineNumber(123)
        .declaringClass("org.bremersee.SomethingController")
        .build();
    StackTraceItem i1 = StackTraceItem.builder()
        .methodName("findSomething")
        .lineNumber(456)
        .declaringClass("org.bremersee.SomethingRepository")
        .build();
    RestApiException model = RestApiException.builder()
        .cause(cause)
        .handler(handler)
        .stackTrace(Arrays.asList(i0, i1))
        .errorCodeInherited(true)
        .errorCode(cause.getErrorCode())
        .path("(api/something")
        .id("5678")
        .message("Something went wrong.")
        .application("junit")
        .timestamp(OffsetDateTime.parse("2007-12-24T18:21Z", ISO_OFFSET_DATE_TIME))
        .build();
    model.furtherDetails("custom", "A custom information");

    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.registerModule(new Jdk8Module());
    xmlMapper.registerModule(new JavaTimeModule());
    xmlMapper.enable(Feature.WRITE_XML_DECLARATION);
    xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    xmlMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
    String xml = xmlMapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(model);
    System.out.println("Plain jackson without jaxb module:");
    System.out.println(xml);

    RestApiException actualModel = xmlMapper
        .readValue(new StringReader(xml), RestApiException.class);
    softly.assertThat(actualModel).isEqualTo(model);
  }

}