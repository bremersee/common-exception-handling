/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.exception.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bremersee.exception.ErrorCodeAware;

/**
 * Marks an exception class or method with an error code {@link #value()} of the application.
 *
 * <p>The error code is applied to the HTTP response.
 *
 * @author Christian Bremer
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ErrorCode {

  /**
   * The error code of the exception in addition to the HTTP status code. The default value is
   * {@link ErrorCodeAware#NO_ERROR_CODE_VALUE}.
   *
   * @return the error code
   */
  String value() default ErrorCodeAware.NO_ERROR_CODE_VALUE;

}
