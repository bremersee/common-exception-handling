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

package org.bremersee.exception;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * The default implementation of a http response parser that creates a {@link RestApiException}.
 *
 * @author Christian Bremer
 */
@Slf4j
public class RestApiExceptionParserImpl implements RestApiExceptionParser {

  private final ObjectMapper objectMapper;

  private final XmlMapper xmlMapper;

  private final Charset defaultCharset;

  public RestApiExceptionParserImpl() {
    this(
        Jackson2ObjectMapperBuilder.json().build(),
        Jackson2ObjectMapperBuilder.xml().createXmlMapper(true).build());
  }

  public RestApiExceptionParserImpl(Charset defaultCharset) {
    this(
        Jackson2ObjectMapperBuilder.json().build(),
        Jackson2ObjectMapperBuilder.xml().createXmlMapper(true).build(),
        defaultCharset);
  }

  /**
   * Instantiates a new rest api exception parser.
   *
   * @param objectMapperBuilder the object mapper builder
   */
  public RestApiExceptionParserImpl(Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this(objectMapperBuilder.build(), objectMapperBuilder.createXmlMapper(true).build());
  }

  public RestApiExceptionParserImpl(
      Jackson2ObjectMapperBuilder objectMapperBuilder,
      Charset charset) {
    this(objectMapperBuilder.build(), objectMapperBuilder.createXmlMapper(true).build(), charset);
  }

  public RestApiExceptionParserImpl(
      ObjectMapper objectMapper,
      XmlMapper xmlMapper) {
    this(objectMapper, xmlMapper, null);
  }

  public RestApiExceptionParserImpl(
      ObjectMapper objectMapper,
      XmlMapper xmlMapper,
      Charset defaultCharset) {
    this.objectMapper = objectMapper;
    this.xmlMapper = xmlMapper;
    this.defaultCharset = nonNull(defaultCharset) ? defaultCharset : StandardCharsets.UTF_8;
  }

  private ObjectMapper getJsonMapper() {
    return objectMapper;
  }

  private XmlMapper getXmlMapper() {
    return xmlMapper;
  }

  private Optional<ObjectMapper> getObjectMapper(RestApiResponseType responseType) {
    if (responseType == RestApiResponseType.JSON) {
      return Optional.of(getJsonMapper());
    }
    if (responseType == RestApiResponseType.XML) {
      return Optional.of(getXmlMapper());
    }
    return Optional.empty();
  }

  public Charset getDefaultCharset() {
    return defaultCharset;
  }

  @Override
  public RestApiException parseException(
      @Nullable byte[] response,
      HttpHeaders headers) {

    String responseStr;
    if (isNull(response) || response.length == 0) {
      responseStr = null;
    } else {
      responseStr = new String(response, getContentTypeCharset(headers.getContentType()));
    }
    return parseException(responseStr, headers);
  }

  @Override
  public RestApiException parseException(
      @Nullable String response,
      HttpHeaders headers) {

    RestApiResponseType responseType = RestApiResponseType
        .detectByContentType(headers.getContentType());
    return Optional.ofNullable(response)
        .filter(res -> !res.isBlank())
        .flatMap(res -> getObjectMapper(responseType).flatMap(om -> {
          try {
            return Optional.of(om.readValue(res, RestApiException.class));
          } catch (Exception ignored) {
            log.debug("Response is not a 'RestApiException' as {}.", responseType.name());
            return Optional.empty();
          }
        }))
        .orElseGet(() -> getRestApiExceptionFromHeaders(response, headers));
  }

  private RestApiException getRestApiExceptionFromHeaders(
      String response,
      HttpHeaders httpHeaders) {

    RestApiException restApiException = new RestApiException();

    String id = httpHeaders.getFirst(RestApiExceptionConstants.ID_HEADER_NAME);
    if (StringUtils.hasText(id) && !RestApiExceptionConstants.NO_ID_VALUE.equals(id)) {
      restApiException.setId(id);
    }

    String timestamp = httpHeaders.getFirst(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME);
    restApiException.setTimestamp(parseErrorTimestamp(timestamp));

    if (StringUtils.hasText(response)) {
      restApiException.setMessage(response);
    } else {
      String message = httpHeaders.getFirst(RestApiExceptionConstants.MESSAGE_HEADER_NAME);
      restApiException.setMessage(
          StringUtils.hasText(message) ? message : RestApiExceptionConstants.NO_MESSAGE_VALUE);
    }

    String errorCode = httpHeaders.getFirst(RestApiExceptionConstants.CODE_HEADER_NAME);
    if (StringUtils.hasText(errorCode)
        && !RestApiExceptionConstants.NO_ERROR_CODE_VALUE.equals(errorCode)) {
      restApiException.setErrorCode(errorCode);
    }

    String cls = httpHeaders.getFirst(RestApiExceptionConstants.CLASS_HEADER_NAME);
    if (StringUtils.hasText(cls) && !RestApiExceptionConstants.NO_CLASS_VALUE.equals(cls)) {
      restApiException.setClassName(cls);
    }

    return restApiException;
  }

  Charset getContentTypeCharset(MediaType contentType) {
    return Optional.ofNullable(contentType)
        .flatMap(ct -> Optional.ofNullable(ct.getCharset()))
        .orElseGet(this::getDefaultCharset);
  }

  /**
   * Parse the 'timestamp' header value.
   *
   * @param value the 'timestamp' header value
   * @return the timestamp
   */
  OffsetDateTime parseErrorTimestamp(String value) {
    OffsetDateTime time = null;
    if (Objects.nonNull(value)) {
      try {
        time = OffsetDateTime.parse(value, RestApiExceptionConstants.TIMESTAMP_FORMATTER);
      } catch (final Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("msg=[Parsing timestamp failed.] timestamp=[{}]", value);
        }
      }
    }
    return time;
  }

}
