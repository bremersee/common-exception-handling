/*
 * Copyright 2018-2020 the original author or authors.
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

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The handler test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class HandlerTest {

  /**
   * Gets class name.
   *
   * @param softly the soft assertions
   */
  @Test
  void getClassName(SoftAssertions softly) {
    Handler model = new Handler();
    model.setClassName("value");
    softly.assertThat(model.getClassName()).isEqualTo("value");

    model = Handler.builder().className("value").build();
    softly.assertThat(model.getClassName()).isEqualTo("value");

    softly.assertThat(model).isNotEqualTo(null);
    softly.assertThat(model).isNotEqualTo(new Object());
    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().className("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets method name.
   *
   * @param softly the soft assertions
   */
  @Test
  void getMethodName(SoftAssertions softly) {
    Handler model = new Handler();
    model.setMethodName("value");
    softly.assertThat(model.getMethodName()).isEqualTo("value");

    model = Handler.builder().methodName("value").build();
    softly.assertThat(model.getMethodName()).isEqualTo("value");

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().methodName("value").build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Gets method parameter types.
   *
   * @param softly the soft assertions
   */
  @Test
  void getMethodParameterTypes(SoftAssertions softly) {
    List<String> value = List.of("value");
    Handler model = new Handler();
    model.setMethodParameterTypes(value);
    softly.assertThat(model.getMethodParameterTypes()).isEqualTo(value);

    model = Handler.builder().methodParameterTypes(value).build();
    softly.assertThat(model.getMethodParameterTypes()).isEqualTo(value);

    softly.assertThat(model).isEqualTo(model);
    softly.assertThat(model).isEqualTo(model.toBuilder().methodParameterTypes(value).build());

    softly.assertThat(model.toString()).contains("value");
  }

  /**
   * Builder.
   */
  @Test
  void builder() {
    String className = "org.bremersee.Example";
    String methodName = "doSomething";
    List<String> methodParameterNames = List.of("a", "b", "c");
    assertThat(Handler.builder()
        .className(className)
        .methodName(methodName)
        .methodParameterTypes(methodParameterNames)
        .build())
        .isEqualTo(new Handler(className, methodName, methodParameterNames));
  }
}