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

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionParser;
import org.bremersee.exception.RestApiExceptionParserImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ClassUtils;

/**
 * The rest api exception parser autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(name = {
    "com.fasterxml.jackson.databind.ObjectMapper",
    "org.springframework.http.converter.json.Jackson2ObjectMapperBuilder",
    "org.bremersee.exception.RestApiExceptionParserImpl"
})
@AutoConfiguration
@Slf4j
public class RestApiExceptionParserForWebFluxAutoConfiguration {

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("""

            *********************************************************************************
            * {}
            *********************************************************************************""",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  /**
   * Creates rest api exception parser for reactive web application.
   *
   * @param objectMapperBuilderProvider the object mapper builder provider
   * @return the rest api exception parser
   */
  @ConditionalOnWebApplication(type = Type.REACTIVE)
  @ConditionalOnMissingBean
  @Bean
  public RestApiExceptionParser restApiExceptionParser(
      ObjectProvider<Jackson2ObjectMapperBuilder> objectMapperBuilderProvider) {

    Jackson2ObjectMapperBuilder objectMapperBuilder = objectMapperBuilderProvider.getIfAvailable();
    return Optional.ofNullable(objectMapperBuilder)
        .map(RestApiExceptionParserImpl::new)
        .orElseGet(RestApiExceptionParserImpl::new);
  }

}
