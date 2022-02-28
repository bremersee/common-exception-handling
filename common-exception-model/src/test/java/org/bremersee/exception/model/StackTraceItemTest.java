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

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The stack trace item test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class StackTraceItemTest {

  /**
   * Gets declaring class.
   *
   * @param softly the soft assertions
   */
  @Test
  void getDeclaringClass(SoftAssertions softly) {
    String value = "value";
    StackTraceItem model = new StackTraceItem();
    model.setDeclaringClass(value);
    softly.assertThat(model.getDeclaringClass()).isEqualTo(value);

    model = StackTraceItem.builder().declaringClass(value).build();
    softly.assertThat(model.getDeclaringClass()).isEqualTo(value);

    softly.assertThat(model).isNotEqualTo(null);
    softly.assertThat(model).isNotEqualTo(new Object());
    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().declaringClass(value).build());

    softly.assertThat(model.toString()).contains(value);
  }

  /**
   * Gets method name.
   *
   * @param softly the soft assertions
   */
  @Test
  void getMethodName(SoftAssertions softly) {
    String value = "value";
    StackTraceItem model = new StackTraceItem();
    model.setMethodName(value);
    softly.assertThat(model.getMethodName()).isEqualTo(value);

    model = StackTraceItem.builder().methodName(value).build();
    softly.assertThat(model.getMethodName()).isEqualTo(value);

    softly.assertThat(model).isNotEqualTo(null);
    softly.assertThat(model).isNotEqualTo(new Object());
    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().methodName(value).build());

    softly.assertThat(model.toString()).contains(value);
  }

  /**
   * Gets file name.
   *
   * @param softly the soft assertions
   */
  @Test
  void getFileName(SoftAssertions softly) {
    String value = "value";
    StackTraceItem model = new StackTraceItem();
    model.setFileName(value);
    softly.assertThat(model.getFileName()).isEqualTo(value);

    model = StackTraceItem.builder().fileName(value).build();
    softly.assertThat(model.getFileName()).isEqualTo(value);

    softly.assertThat(model).isNotEqualTo(null);
    softly.assertThat(model).isNotEqualTo(new Object());
    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().fileName(value).build());

    softly.assertThat(model.toString()).contains(value);
  }

  /**
   * Gets line number.
   *
   * @param softly the soft assertions
   */
  @Test
  void getLineNumber(SoftAssertions softly) {
    Integer value = 1234;
    StackTraceItem model = new StackTraceItem();
    model.setLineNumber(value);
    softly.assertThat(model.getLineNumber()).isEqualTo(value);

    model = StackTraceItem.builder().lineNumber(value).build();
    softly.assertThat(model.getLineNumber()).isEqualTo(value);

    softly.assertThat(model).isNotEqualTo(null);
    softly.assertThat(model).isNotEqualTo(new Object());
    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().lineNumber(value).build());

    softly.assertThat(model.toString()).contains(value.toString());
  }

  /**
   * Builder.
   */
  @Test
  void builder() {
    String className = "org.bremersee.Example";
    String methodName = "doSomething";
    String fileName = "unknown";
    Integer lineNumber = 234;
    assertThat(StackTraceItem.builder()
        .declaringClass(className)
        .methodName(methodName)
        .fileName(fileName)
        .lineNumber(lineNumber)
        .build())
        .isEqualTo(new StackTraceItem(className, methodName, fileName, lineNumber));
  }
}