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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperForWebFlux;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionMapperBootProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.codec.ServerCodecConfigurer;

/**
 * The api exception handler autoconfiguration test.
 *
 * @author Christian Bremer
 */
class ApiExceptionHandlerAutoConfigurationTest {

  private ApiExceptionHandlerAutoConfiguration target;

  /**
   * Setup.
   */
  @BeforeEach
  void setup() {
    RestApiExceptionMapperBootProperties properties = new RestApiExceptionMapperBootProperties();
    target = new ApiExceptionHandlerAutoConfiguration(properties);
  }

  /**
   * Init.
   */
  @Test
  void init() {
    assertThatNoException().isThrownBy(() -> target.init());
  }

  /**
   * Api exception handler.
   */
  @Test
  void apiExceptionHandler() {
    //noinspection unchecked
    ObjectProvider<ErrorAttributes> errorAttributes = mock(ObjectProvider.class);
    when(errorAttributes.getIfAvailable()).thenReturn(mock(ErrorAttributes.class));

    //noinspection unchecked
    ObjectProvider<WebProperties> webProperties = mock(ObjectProvider.class);
    when(webProperties.getIfAvailable(any())).thenReturn(new WebProperties());

    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getClassLoader())
        .thenReturn(ApplicationContext.class.getClassLoader());

    //noinspection unchecked
    ObjectProvider<ServerCodecConfigurer> serverCodecConfigurer = mock(ObjectProvider.class);
    when(serverCodecConfigurer.getIfAvailable()).thenReturn(mock(ServerCodecConfigurer.class));

    //noinspection unchecked
    ObjectProvider<RestApiExceptionMapper> restApiExceptionMapper = mock(ObjectProvider.class);
    when(restApiExceptionMapper.getIfAvailable()).thenReturn(new RestApiExceptionMapperForWebFlux(
        new RestApiExceptionMapperBootProperties().toRestApiExceptionMapperProperties(),
        "test"
    ));

    ApiExceptionHandler actual = target.apiExceptionHandler(
        errorAttributes,
        webProperties,
        applicationContext,
        serverCodecConfigurer,
        restApiExceptionMapper);

    assertThat(actual)
        .isNotNull();
  }
}