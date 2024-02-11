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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.RestApiExceptionMapperProperties;
import org.bremersee.exception.servlet.ApiExceptionResolver;
import org.bremersee.exception.servlet.HttpServletRequestIdProvider;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionMapperBootProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The api exception resolver autoconfiguration.
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({
    ObjectMapper.class,
    RestApiExceptionMapperProperties.class,
    ApiExceptionResolver.class
})
@ConditionalOnBean({
    Jackson2ObjectMapperBuilder.class
})
@AutoConfigureAfter({
    RestApiExceptionMapperForWebAutoConfiguration.class
})
@AutoConfiguration
@EnableConfigurationProperties({RestApiExceptionMapperBootProperties.class})
@Slf4j
public class ApiExceptionResolverAutoConfiguration implements WebMvcConfigurer {

  private final RestApiExceptionMapperBootProperties properties;

  private final ApiExceptionResolver apiExceptionResolver;

  /**
   * Instantiates a new api exception resolver autoconfiguration.
   *
   * @param properties the properties
   * @param apiExceptionMapper the api exception mapper
   * @param objectMapperBuilder the object mapper builder
   * @param restApiIdProvider the rest api id provider
   */
  public ApiExceptionResolverAutoConfiguration(
      RestApiExceptionMapperBootProperties properties,
      ObjectProvider<RestApiExceptionMapper> apiExceptionMapper,
      ObjectProvider<Jackson2ObjectMapperBuilder> objectMapperBuilder,
      ObjectProvider<HttpServletRequestIdProvider> restApiIdProvider) {

    RestApiExceptionMapper mapper = apiExceptionMapper.getIfAvailable();
    Jackson2ObjectMapperBuilder omBuilder = objectMapperBuilder.getIfAvailable();
    Assert.notNull(mapper, "Api exception resolver must be present.");
    Assert.notNull(omBuilder, "Object mapper builder must be present.");
    this.properties = properties;
    this.apiExceptionResolver = new ApiExceptionResolver(
        properties.getApiPaths(),
        mapper,
        omBuilder);
    this.apiExceptionResolver.setRestApiExceptionIdProvider(restApiIdProvider.getIfAvailable());
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

  @Override
  public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    log.info("Adding exception resolver [{}] to registry.",
        ClassUtils.getUserClass(apiExceptionResolver).getSimpleName());
    exceptionResolvers.add(0, apiExceptionResolver);
  }

}
