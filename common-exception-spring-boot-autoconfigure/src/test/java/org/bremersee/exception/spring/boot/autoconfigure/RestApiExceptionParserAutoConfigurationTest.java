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

package org.bremersee.exception.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.RestApiExceptionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.env.MockEnvironment;

/**
 * The rest api exception parser auto configuration test.
 *
 * @author Christian Bremer
 */
class RestApiExceptionParserAutoConfigurationTest {

  private RestApiExceptionParserAutoConfiguration target;

  /**
   * Sets .
   */
  @BeforeEach
  void setup() {
    target = new RestApiExceptionParserAutoConfiguration();
  }

  /**
   * Init.
   */
  @Test
  void init() {
    assertThatNoException().isThrownBy(() -> target.init());
  }

  /**
   * Rest api exception parser for servlet.
   */
  @Test
  void restApiExceptionParserForServlet() {
    Environment environment = new MockEnvironment();
    RestApiExceptionParser actual = target
        .restApiExceptionParser(environment, objectMapperBuilderProvider());
    assertThat(actual)
        .isNotNull();
  }

  /**
   * Rest api exception parser for reactive.
   */
  @Test
  void restApiExceptionParserForReactive() {
    RestApiExceptionParser actual = target
        .restApiExceptionParser(objectMapperBuilderProvider());
    assertThat(actual)
        .isNotNull();
  }

  private ObjectProvider<Jackson2ObjectMapperBuilder> objectMapperBuilderProvider() {
    //noinspection unchecked
    ObjectProvider<Jackson2ObjectMapperBuilder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(new Jackson2ObjectMapperBuilder());
    return provider;
  }
}