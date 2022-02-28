/*
 * Copyright 2022 the original author or authors.
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

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMapping;
import org.bremersee.exception.RestApiExceptionMapperProperties.ExceptionMappingConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

/**
 * The rest api exception mapper properties test.
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionMapperPropertiesTest {

  /**
   * Gets default exception mapping.
   *
   * @param softly the softly
   */
  @Test
  void getDefaultExceptionMapping(SoftAssertions softly) {
    ExceptionMapping target = RestApiExceptionMapperProperties.builder().build()
        .getDefaultExceptionMapping();
    softly.assertThat(target.getExceptionClassName())
        .isEqualTo("*");
    softly.assertThat(target.getStatus())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    softly.assertThat(target.getMessage())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    softly.assertThat(target.getCode())
        .isNull();
  }

  /**
   * Gets exception mappings.
   */
  @Test
  void getExceptionMappings() {
    RestApiExceptionMapperProperties target = RestApiExceptionMapperProperties.builder().build();
    assertThat(target.getExceptionMappings())
        .isNotEmpty();
  }

  /**
   * Gets default exception mapping config.
   *
   * @param softly the softly
   */
  @Test
  void getDefaultExceptionMappingConfig(SoftAssertions softly) {
    ExceptionMappingConfig target = RestApiExceptionMapperProperties.builder().build()
        .getDefaultExceptionMappingConfig();
    softly.assertThat(target.getExceptionClassName())
        .isEqualTo("*");
    softly.assertThat(target.isIncludeMessage())
        .isTrue();
    softly.assertThat(target.isIncludeException())
        .isTrue();
    softly.assertThat(target.isIncludeApplicationName())
        .isTrue();
    softly.assertThat(target.isIncludePath())
        .isTrue();
    softly.assertThat(target.isIncludeHandler())
        .isFalse();
    softly.assertThat(target.isIncludeStackTrace())
        .isFalse();
    softly.assertThat(target.isIncludeCause())
        .isFalse();
    softly.assertThat(target.isEvaluateAnnotationFirst())
        .isFalse();
  }

  /**
   * Gets exception mapping configs.
   */
  @Test
  void getExceptionMappingConfigs() {
    RestApiExceptionMapperProperties target = RestApiExceptionMapperProperties.builder().build();
    assertThat(target.getExceptionMappingConfigs())
        .isEmpty();
  }

  /**
   * Find exception mapping.
   *
   * @param softly the softly
   */
  @Test
  void findExceptionMapping(SoftAssertions softly) {
    RestApiExceptionMapperProperties target = RestApiExceptionMapperProperties.builder().build();

    ExceptionMapping actual = target.findExceptionMapping(new Exception());
    softly.assertThat(actual)
        .isEqualTo(target.getDefaultExceptionMapping());

    actual = target.findExceptionMapping(new IllegalArgumentException());
    softly.assertThat(actual)
        .isNotEqualTo(target.getDefaultExceptionMapping());
  }

  /**
   * Find exception mapping config.
   *
   * @param softly the softly
   */
  @Test
  void findExceptionMappingConfig(SoftAssertions softly) {
    RestApiExceptionMapperProperties target = RestApiExceptionMapperProperties.builder().build();

    ExceptionMappingConfig actual = target.findExceptionMappingConfig(new Exception());
    softly.assertThat(actual)
        .isEqualTo(target.getDefaultExceptionMappingConfig());

    boolean switchedValue = !target.getDefaultExceptionMappingConfig()
        .isEvaluateAnnotationFirst();
    target = RestApiExceptionMapperProperties.builder()
        .from(target)
        .addExceptionMappingConfigs(ExceptionMappingConfig.builder()
            .exceptionClassName("java.lang.*")
            .isEvaluateAnnotationFirst(switchedValue)
            .build())
        .build();

    actual = target.findExceptionMappingConfig(ServiceException.alreadyExists());
    softly.assertThat(actual)
        .isNotEqualTo(target.getDefaultExceptionMappingConfig())
        .extracting(
            ExceptionMappingConfig::isEvaluateAnnotationFirst,
            InstanceOfAssertFactories.BOOLEAN)
        .isEqualTo(switchedValue);

  }

}