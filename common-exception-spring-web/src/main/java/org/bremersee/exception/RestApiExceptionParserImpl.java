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

package org.bremersee.exception;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.hasText;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.model.RestApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ObjectUtils;

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

  /**
   * Instantiates a new rest api exception parser.
   */
  public RestApiExceptionParserImpl() {
    this(new Jackson2ObjectMapperBuilder());
  }

  /**
   * Instantiates a new rest api exception parser.
   *
   * @param defaultCharset the default charset
   */
  public RestApiExceptionParserImpl(Charset defaultCharset) {
    this(new Jackson2ObjectMapperBuilder(), defaultCharset);
  }

  /**
   * Instantiates a new rest api exception parser.
   *
   * @param objectMapperBuilder the object mapper builder
   */
  public RestApiExceptionParserImpl(Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this(objectMapperBuilder.build(), objectMapperBuilder.createXmlMapper(true).build());
  }

  /**
   * Instantiates a new rest api exception parser.
   *
   * @param objectMapperBuilder the object mapper builder
   * @param charset the charset
   */
  public RestApiExceptionParserImpl(
      Jackson2ObjectMapperBuilder objectMapperBuilder,
      Charset charset) {
    this(objectMapperBuilder.build(), objectMapperBuilder.createXmlMapper(true).build(), charset);
  }

  /**
   * Instantiates a new rest api exception parser.
   *
   * @param objectMapper the object mapper
   * @param xmlMapper the xml mapper
   */
  public RestApiExceptionParserImpl(ObjectMapper objectMapper, XmlMapper xmlMapper) {
    this(objectMapper, xmlMapper, null);
  }

  /**
   * Instantiates a new rest api exception parser.
   *
   * @param objectMapper the object mapper
   * @param xmlMapper the xml mapper
   * @param defaultCharset the default charset
   */
  public RestApiExceptionParserImpl(
      ObjectMapper objectMapper,
      XmlMapper xmlMapper,
      Charset defaultCharset) {
    this.objectMapper = objectMapper;
    this.xmlMapper = xmlMapper;
    this.defaultCharset = Optional.ofNullable(defaultCharset)
        .orElse(StandardCharsets.UTF_8);
  }

  private ObjectMapper getJsonMapper() {
    return objectMapper;
  }

  private XmlMapper getXmlMapper() {
    return xmlMapper;
  }

  /**
   * Gets object mapper.
   *
   * @param responseType the response type
   * @return the object mapper
   */
  Optional<ObjectMapper> getObjectMapper(RestApiResponseType responseType) {
    if (responseType == RestApiResponseType.JSON) {
      return Optional.of(getJsonMapper());
    }
    if (responseType == RestApiResponseType.XML) {
      return Optional.of(getXmlMapper());
    }
    return Optional.empty();
  }

  /**
   * Gets default charset.
   *
   * @return the default charset
   */
  protected Charset getDefaultCharset() {
    return defaultCharset;
  }

  @Override
  public RestApiException parseException(
      byte[] response,
      HttpStatusCode httpStatus,
      HttpHeaders headers) {

    String responseStr;
    if (isNull(response) || response.length == 0) {
      responseStr = null;
    } else {
      responseStr = new String(response, getContentTypeCharset(headers.getContentType()));
    }
    return parseException(responseStr, httpStatus, headers);
  }

  @Override
  public RestApiException parseException(
      String response,
      HttpStatusCode httpStatus,
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
        .map(restApiException -> {
          restApiException.setStatus(httpStatus.value());
          if ((httpStatus instanceof HttpStatus status)
              && ObjectUtils.isEmpty(restApiException.getError())) {
            restApiException.setError(status.getReasonPhrase());
          }
          return restApiException;
        })
        .orElseGet(() -> getRestApiExceptionFromHeaders(response, httpStatus, headers));
  }

  /**
   * Gets rest api exception from headers.
   *
   * @param response the response
   * @param httpStatus the http status
   * @param httpHeaders the http headers
   * @return the rest api exception from headers
   */
  protected RestApiException getRestApiExceptionFromHeaders(
      String response,
      HttpStatusCode httpStatus,
      HttpHeaders httpHeaders) {

    RestApiException restApiException = new RestApiException();

    String tmp = httpHeaders.getFirst(RestApiExceptionConstants.ID_HEADER_NAME);
    if (hasText(tmp)) {
      restApiException.setId(tmp);
    }

    tmp = httpHeaders.getFirst(RestApiExceptionConstants.TIMESTAMP_HEADER_NAME);
    restApiException.setTimestamp(parseErrorTimestamp(tmp));

    tmp = httpHeaders.getFirst(RestApiExceptionConstants.CODE_HEADER_NAME);
    if (hasText(tmp)) {
      restApiException.setErrorCode(tmp);

      tmp = httpHeaders.getFirst(RestApiExceptionConstants.CODE_INHERITED_HEADER_NAME);
      if (hasText(tmp)) {
        restApiException.setErrorCodeInherited(Boolean.valueOf(tmp));
      }
    }

    tmp = httpHeaders.getFirst(RestApiExceptionConstants.MESSAGE_HEADER_NAME);
    if (hasText(tmp)) {
      restApiException.setMessage(tmp);
    } else {
      restApiException.setMessage(response);
    }

    tmp = httpHeaders.getFirst(RestApiExceptionConstants.EXCEPTION_HEADER_NAME);
    if (hasText(tmp)) {
      restApiException.setException(tmp);
    }

    tmp = httpHeaders.getFirst(RestApiExceptionConstants.APPLICATION_HEADER_NAME);
    if (hasText(tmp)) {
      restApiException.setApplication(tmp);
    }

    tmp = httpHeaders.getFirst(RestApiExceptionConstants.PATH_HEADER_NAME);
    if (hasText(tmp)) {
      restApiException.setPath(tmp);
    }

    restApiException.setStatus(httpStatus.value());
    if (httpStatus instanceof HttpStatus status) {
      restApiException.setError(status.getReasonPhrase());
    }

    return restApiException;
  }

  /**
   * Gets content type charset.
   *
   * @param contentType the content type
   * @return the content type charset
   */
  protected Charset getContentTypeCharset(MediaType contentType) {
    return Optional.ofNullable(contentType)
        .map(ct -> {
          RestApiResponseType responseType = RestApiResponseType.detectByContentType(ct);
          if (RestApiResponseType.JSON == responseType) {
            return StandardCharsets.UTF_8;
          }
          Charset charset = ct.getCharset();
          if (isNull(charset) && RestApiResponseType.XML == responseType) {
            return StandardCharsets.UTF_8;
          }
          return charset;
        })
        .orElseGet(this::getDefaultCharset);
  }

  /**
   * Parse the 'timestamp' header value.
   *
   * @param value the 'timestamp' header value
   * @return the timestamp
   */
  protected OffsetDateTime parseErrorTimestamp(String value) {
    OffsetDateTime time = null;
    if (nonNull(value)) {
      try {
        time = OffsetDateTime.parse(value, RestApiExceptionConstants.TIMESTAMP_FORMATTER);
      } catch (Exception e) {
        log.debug("Parsing timestamp failed, timestamp = '{}'.", value);
      }
    }
    return time;
  }

}
