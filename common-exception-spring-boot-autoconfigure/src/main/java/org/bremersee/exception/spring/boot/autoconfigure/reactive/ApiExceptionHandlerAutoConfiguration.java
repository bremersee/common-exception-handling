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

import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionMapperBootProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * The api exception handler autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(name = {
    "com.fasterxml.jackson.databind.ObjectMapper",
    "org.bremersee.exception.RestApiExceptionMapper"
})
@ConditionalOnBean({
    ErrorAttributes.class,
    WebProperties.class,
    ServerCodecConfigurer.class
})
@AutoConfigureAfter({
    RestApiExceptionMapperForWebFluxAutoConfiguration.class
})
@AutoConfiguration
@EnableConfigurationProperties({RestApiExceptionMapperBootProperties.class})
@Slf4j
public class ApiExceptionHandlerAutoConfiguration {

  private final RestApiExceptionMapperBootProperties properties;

  /**
   * Instantiates a new api exception handler autoconfiguration.
   *
   * @param properties the properties
   */
  public ApiExceptionHandlerAutoConfiguration(
      RestApiExceptionMapperBootProperties properties) {
    this.properties = properties;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("""

            *********************************************************************************
            * {}
            *********************************************************************************
            * apiPaths = {}
            *********************************************************************************""",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        properties.getApiPaths());
  }

  /**
   * Builds api exception handler bean.
   *
   * @param errorAttributes the error attributes
   * @param webProperties the web properties
   * @param applicationContext the application context
   * @param serverCodecConfigurer the server codec configurer
   * @param restApiExceptionMapper the rest api exception mapper
   * @return the api exception handler bean
   */
  @Bean
  @Order(-2) // to have a higher priority than DefaultErrorWebExceptionHandler
  public ApiExceptionHandler apiExceptionHandler(
      ObjectProvider<ErrorAttributes> errorAttributes,
      ObjectProvider<WebProperties> webProperties,
      ApplicationContext applicationContext,
      ObjectProvider<ServerCodecConfigurer> serverCodecConfigurer,
      ObjectProvider<RestApiExceptionMapper> restApiExceptionMapper) {

    Assert.notNull(
        errorAttributes.getIfAvailable(),
        "Error attributes must be present.");
    Assert.notNull(
        serverCodecConfigurer.getIfAvailable(),
        "Server codec configurer must be present.");
    Assert.notNull(
        restApiExceptionMapper.getIfAvailable(),
        "Rest api exception mapper must be present.");

    return new ApiExceptionHandler(
        properties.getApiPaths(),
        errorAttributes.getIfAvailable(),
        webProperties.getIfAvailable(WebProperties::new).getResources(),
        applicationContext,
        serverCodecConfigurer.getIfAvailable(),
        restApiExceptionMapper.getIfAvailable());
  }

}
