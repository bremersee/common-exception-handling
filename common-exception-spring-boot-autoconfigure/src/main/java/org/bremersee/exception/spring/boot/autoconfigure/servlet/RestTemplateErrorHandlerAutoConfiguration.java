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
import org.bremersee.exception.RestApiResponseErrorHandler;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionParserAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

/**
 * The rest template error handler autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({
    RestApiResponseErrorHandler.class
})
@ConditionalOnBean({
    RestApiExceptionParser.class
})
@AutoConfigureAfter({
    RestApiExceptionParserAutoConfiguration.class
})
@Configuration
@Order(100)
@Slf4j
public class RestTemplateErrorHandlerAutoConfiguration implements RestTemplateCustomizer {

  private final RestApiExceptionParser restApiExceptionParser;

  /**
   * Instantiates a new Rest template error handler autoconfiguration.
   *
   * @param restApiExceptionParser the rest api exception parser
   */
  public RestTemplateErrorHandlerAutoConfiguration(
      ObjectProvider<RestApiExceptionParser> restApiExceptionParser) {
    this.restApiExceptionParser = restApiExceptionParser
        .getIfAvailable(RestApiExceptionParserImpl::new);
  }

  @Override
  public void customize(RestTemplate restTemplate) {
    restTemplate.setErrorHandler(new RestApiResponseErrorHandler(restApiExceptionParser));
  }
}
