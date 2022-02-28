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

package org.bremersee.exception;

import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_XML;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;

/**
 * The rest api response type.
 *
 * @author Christian Bremer
 */
public enum RestApiResponseType {

  /**
   * Json rest api response type.
   */
  JSON(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_VALUE),

  /**
   * Xml rest api response type.
   */
  XML(MediaType.APPLICATION_XML, MediaType.APPLICATION_XML_VALUE),

  /**
   * Header rest api response type.
   */
  HEADER(MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN_VALUE);

  private static final MediaType APPLICATION_PLUS_JSON = new MediaType("application", "*+json");

  private static final MediaType APPLICATION_PLUS_XML = new MediaType("application", "*+xml");

  private final MediaType contentType;

  private final String contentTypeValue;

  RestApiResponseType(MediaType contentType, String contentTypeValue) {
    this.contentType = contentType;
    this.contentTypeValue = contentTypeValue;
  }

  /**
   * Gets content type.
   *
   * @return the content type
   */
  public MediaType getContentType() {
    return contentType;
  }

  /**
   * Gets content type value.
   *
   * @return the content type value
   */
  public String getContentTypeValue() {
    return contentTypeValue;
  }

  /**
   * Detect rest api response type by accept header.
   *
   * @param accepted the accepted
   * @return the rest api response content type
   */
  public static RestApiResponseType detectByAccepted(List<MediaType> accepted) {
    RestApiResponseType contentType;
    if (isJsonAccepted(accepted)) {
      contentType = JSON;
    } else if (isXmlAccepted(accepted)) {
      contentType = XML;
    } else {
      contentType = HEADER;
    }
    return contentType;
  }

  /**
   * Detect rest api response type by content type.
   *
   * @param contentType the content type
   * @return the rest api response type
   */
  public static RestApiResponseType detectByContentType(MediaType contentType) {
    if (isJson(contentType)) {
      return JSON;
    }
    if (isXml(contentType)) {
      return XML;
    }
    return HEADER;
  }

  private static boolean isJson(MediaType contentType) {
    return nonNull(contentType)
        && (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
        || contentType.isCompatibleWith(APPLICATION_PLUS_JSON));
  }

  private static boolean isJsonAccepted(List<MediaType> accepted) {
    return Optional.ofNullable(accepted)
        .stream()
        .flatMap(Collection::stream)
        .anyMatch(mediaType -> mediaType.includes(APPLICATION_JSON)
            || APPLICATION_PLUS_JSON.includes(mediaType)
            || mediaType.includes(TEXT_PLAIN));
  }

  private static boolean isXml(MediaType contentType) {
    return nonNull(contentType)
        && (contentType.isCompatibleWith(APPLICATION_XML)
        || contentType.isCompatibleWith(APPLICATION_PLUS_XML));
  }

  private static boolean isXmlAccepted(List<MediaType> accepted) {
    return Optional.ofNullable(accepted)
        .stream()
        .flatMap(Collection::stream)
        .anyMatch(mediaType -> mediaType.includes(APPLICATION_XML)
            || APPLICATION_PLUS_XML.includes(mediaType)
            || mediaType.includes(TEXT_XML));
  }

}
