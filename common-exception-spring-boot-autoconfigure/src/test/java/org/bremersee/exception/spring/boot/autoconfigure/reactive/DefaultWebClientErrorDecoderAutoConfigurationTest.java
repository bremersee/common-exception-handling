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

package org.bremersee.exception.spring.boot.autoconfigure.reactive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.webclient.DefaultWebClientErrorDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The default web client error decoder autoconfiguration test.
 *
 * @author Christian Bremer
 */
class DefaultWebClientErrorDecoderAutoConfigurationTest {

  private final DefaultWebClientErrorDecoderAutoConfiguration target
      = new DefaultWebClientErrorDecoderAutoConfiguration();

  /**
   * Init.
   */
  @Test
  void init() {
    assertThatNoException().isThrownBy(target::init);
  }

  /**
   * Default web client error decoder.
   */
  @Test
  void defaultWebClientErrorDecoder() {
    //noinspection unchecked
    ObjectProvider<RestApiExceptionParser> parser = mock(ObjectProvider.class);
    when(parser.getIfAvailable()).thenReturn(new RestApiExceptionParserImpl());
    DefaultWebClientErrorDecoder actual = target.defaultWebClientErrorDecoder(parser);
    assertThat(actual)
        .isNotNull();
  }
}