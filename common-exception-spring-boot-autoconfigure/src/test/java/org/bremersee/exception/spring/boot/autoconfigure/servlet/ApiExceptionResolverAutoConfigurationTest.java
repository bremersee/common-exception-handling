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

package org.bremersee.exception.spring.boot.autoconfigure.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperForWeb;
import org.bremersee.exception.servlet.HttpServletRequestIdProvider;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionMapperBootProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * The api exception resolver autoconfiguration test.
 *
 * @author Christian Bremer
 */
class ApiExceptionResolverAutoConfigurationTest {

  private ApiExceptionResolverAutoConfiguration target;

  /**
   * Sets .
   */
  @BeforeEach
  void setup() {
    RestApiExceptionMapperBootProperties properties = new RestApiExceptionMapperBootProperties();

    //noinspection unchecked
    ObjectProvider<RestApiExceptionMapper> apiExceptionMapper = mock(ObjectProvider.class);
    when(apiExceptionMapper.getIfAvailable()).thenReturn(new RestApiExceptionMapperForWeb(
        properties.toRestApiExceptionMapperProperties(),
        "test"
    ));

    //noinspection unchecked
    ObjectProvider<Jackson2ObjectMapperBuilder> objectMapperBuilder = mock(ObjectProvider.class);
    when(objectMapperBuilder.getIfAvailable()).thenReturn(new Jackson2ObjectMapperBuilder());

    //noinspection unchecked
    ObjectProvider<HttpServletRequestIdProvider> restApiIdProvider = mock(ObjectProvider.class);
    when(restApiIdProvider.getIfAvailable()).thenReturn(null);

    this.target = new ApiExceptionResolverAutoConfiguration(
        properties, apiExceptionMapper, objectMapperBuilder, restApiIdProvider);
  }

  /**
   * Init.
   */
  @Test
  void init() {
    assertThatNoException().isThrownBy(() -> target.init());
  }

  /**
   * Extend handler exception resolvers.
   */
  @Test
  void extendHandlerExceptionResolvers() {
    List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();
    target.extendHandlerExceptionResolvers(exceptionResolvers);
    assertThat(exceptionResolvers)
        .isNotEmpty();
  }
}