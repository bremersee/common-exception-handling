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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperForWeb;
import org.bremersee.exception.RestApiExceptionMapperProperties;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionMapperBootProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The rest api exception mapper autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass({
    ObjectMapper.class,
    RestApiExceptionMapperProperties.class
})
@Configuration
@EnableConfigurationProperties({RestApiExceptionMapperBootProperties.class})
@Slf4j
public class RestApiExceptionMapperForWebFluxAutoConfiguration {

  private final String applicationName;

  private final RestApiExceptionMapperBootProperties properties;

  /**
   * Instantiates a new rest api exception mapper autoconfiguration.
   *
   * @param applicationName the application name
   * @param properties the properties
   */
  public RestApiExceptionMapperForWebFluxAutoConfiguration(
      @Value("${spring.application.name:application}") String applicationName,
      RestApiExceptionMapperBootProperties properties) {
    this.applicationName = applicationName;
    this.properties = properties;
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* applicationName = {}\n"
            + "* apiPaths = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        applicationName,
        properties.getApiPaths());
  }

  /**
   * Builds the rest api exception mapper bean.
   *
   * @return the rest api exception mapper bean
   */
  @ConditionalOnMissingBean
  @Bean
  public RestApiExceptionMapper restApiExceptionMapper() {
    return new RestApiExceptionMapperForWeb(
        properties.toRestApiExceptionMapperProperties(),
        applicationName);
  }

}
