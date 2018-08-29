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
import lombok.extern.slf4j.Slf4j;
import org.bremersee.common.exhandling.model.RestApiException;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christian Bremer
 */
@Slf4j
abstract class FeignReflectionUtils {

  private FeignReflectionUtils() {
  }

  static boolean isFeignException(final Throwable throwable) {
    if (throwable == null) {
      return false;
    }
    return isInstanceOf(throwable.getClass(), "feign.FeignException");
  }

  static boolean isFeignClientException(final Throwable throwable) {
    if (throwable == null) {
      return false;
    }
    return isInstanceOf(throwable.getClass(),
        "org.bremersee.common.exhandling.feign.FeignClientException");
  }

  static int getStatus(final Throwable throwable) {
    final Object result = get(throwable, "status");
    if (result instanceof Integer) {
      return (int) result;
    }
    return 500;
  }

  static RestApiException getRestApiException(final Throwable throwable) {
    return get(throwable, "getRestApiException");
  }

  private static boolean isInstanceOf(final Class<?> cls, final String clsName) {
    if (cls == null || clsName == null) {
      return false;
    }
    if (cls.getName().equals(clsName)) {
      return true;
    }
    return isInstanceOf(cls.getSuperclass(), clsName);
  }

  private static <T> T get(final Throwable throwable, final String methodName) {
    try {
      final Method method = ReflectionUtils.findMethod(
          throwable.getClass(), methodName);
      if (method != null) {
        //noinspection unchecked
        return (T) ReflectionUtils.invokeMethod(method, throwable);
      } else {
        throw new Exception("Method " + methodName + " not found in " + throwable.getClass()
            .getName());
      }
    } catch (Exception e) {
      log.warn("Calling " + methodName + " from " + throwable.getClass().getName() + " failed. "
          + "Returning null.", e);
    }
    return null;
  }

}
