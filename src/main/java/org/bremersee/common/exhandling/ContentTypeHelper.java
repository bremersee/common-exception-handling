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

/**
 * The content type helper.
 *
 * @author Christian Bremer
 */
public abstract class ContentTypeHelper {

  /**
   * Determine whether the given content type is json.
   *
   * @param contentType the content type
   * @return the boolean
   */
  public static boolean isJson(String contentType) {
    return contentType != null
        && (contentType.toLowerCase().contains("/json")
        || contentType.toLowerCase().contains("+json"));
  }

  /**
   * Determine whether the given content type is xml.
   *
   * @param contentType the content type
   * @return the boolean
   */
  public static boolean isXml(String contentType) {
    return contentType != null
        && (contentType.toLowerCase().contains("/xml")
        || contentType.toLowerCase().contains("+xml"));
  }

}
