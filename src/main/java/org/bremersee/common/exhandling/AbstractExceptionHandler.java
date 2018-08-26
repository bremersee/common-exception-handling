/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.common.exhandling;

import java.lang.reflect.Method;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;

/**
 * @author Christian Bremer
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractExceptionHandler {

  @Getter
  private final ApiExceptionResolverProperties properties;

  public AbstractExceptionHandler(
      ApiExceptionResolverProperties properties) {
    this.properties = properties;
  }

  protected Optional<Method> getHandlerMethod(final Object handler) {
    if (handler instanceof HandlerMethod) {
      return Optional.of(((HandlerMethod) handler).getMethod());
    }
    return Optional.empty();
  }

  protected Class<?> getHandlerClass(final @NotNull Object handler) {
    Assert.notNull(handler, "Handler must not be null.");
    if (handler instanceof HandlerMethod) {
      return ((HandlerMethod) handler).getBean().getClass();
    } else {
      return handler.getClass();
    }
  }

}
