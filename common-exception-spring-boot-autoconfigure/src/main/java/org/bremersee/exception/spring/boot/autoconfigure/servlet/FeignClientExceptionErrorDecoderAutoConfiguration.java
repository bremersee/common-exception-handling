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

import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.bremersee.exception.feign.FeignClientExceptionErrorDecoder;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionParserAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The feign client exception error decoder autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({
    FeignClientExceptionErrorDecoder.class
})
@ConditionalOnBean({
    RestApiExceptionParser.class
})
@AutoConfigureAfter({
    RestApiExceptionParserAutoConfiguration.class
})
@Configuration
@Slf4j
public class FeignClientExceptionErrorDecoderAutoConfiguration {

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  /**
   * Creates feign client exception error decoder bean.
   *
   * @param parserProvider the parser provider
   * @return the feign client exception error decoder
   */
  @ConditionalOnMissingBean
  @Bean
  public FeignClientExceptionErrorDecoder feignClientExceptionErrorDecoder(
      ObjectProvider<RestApiExceptionParser> parserProvider) {

    RestApiExceptionParser parser = parserProvider.getIfAvailable(RestApiExceptionParserImpl::new);
    return new FeignClientExceptionErrorDecoder(parser);
  }

}
