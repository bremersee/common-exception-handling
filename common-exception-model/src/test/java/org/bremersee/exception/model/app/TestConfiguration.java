/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.exception.model.app;

import java.util.Map;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

/**
 * The test configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class})
@ComponentScan(basePackageClasses = {TestConfiguration.class})
public class TestConfiguration {

  /**
   * The custom test error attributes.
   */
  @Component
  public static class CustomTestErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(
        WebRequest webRequest, ErrorAttributeOptions options) {

      Map<String, Object> errorAttributes =
          super.getErrorAttributes(webRequest, options);
      errorAttributes.put("locale", "de-DE");
      errorAttributes.put("custom", Map.of("key", "value"));
      return errorAttributes;
    }
  }

  /**
   * The error rest controller.
   */
  @RestController
  public static class ErrorRestController {

    /**
     * Produce spring error response entity.
     *
     * @return the response entity
     */
    @GetMapping(path = "/spring-error", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE
    })
    public ResponseEntity<String> produceSpringError() {
      IllegalArgumentException cause = new IllegalArgumentException("Something illegal");
      throw new IllegalStateException("Something must be valid", cause);
    }

  }

}
