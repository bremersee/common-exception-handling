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

package org.bremersee.exception.spring.boot.autoconfigure.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.client.RestTemplate;

/**
 * The rest template error handler autoconfiguration test.
 */
class RestTemplateErrorHandlerAutoConfigurationTest {

  private RestTemplateErrorHandlerAutoConfiguration target;

  /**
   * Setup.
   */
  @BeforeEach
  void setup() {
    //noinspection unchecked
    ObjectProvider<RestApiExceptionParser> parser = mock(ObjectProvider.class);
    when(parser.getIfAvailable()).thenReturn(new RestApiExceptionParserImpl());
    target = new RestTemplateErrorHandlerAutoConfiguration(parser);
  }

  /**
   * Customize.
   */
  @Test
  void customize() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    target.customize(restTemplate);
    verify(restTemplate).setErrorHandler(any());
  }
}