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

import static org.springframework.http.MediaType.parseMediaType;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;

/**
 * The rest api response type test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class RestApiResponseTypeTest {

  /**
   * Gets content type.
   *
   * @param softly the softly
   */
  @Test
  void getContentType(SoftAssertions softly) {
    softly.assertThat(RestApiResponseType.JSON.getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON);
    softly.assertThat(RestApiResponseType.XML.getContentType())
        .isEqualTo(MediaType.APPLICATION_XML);
    softly.assertThat(RestApiResponseType.HEADER.getContentType())
        .isEqualTo(MediaType.TEXT_PLAIN);
  }

  /**
   * Gets content type value.
   *
   * @param softly the softly
   */
  @Test
  void getContentTypeValue(SoftAssertions softly) {
    softly.assertThat(RestApiResponseType.JSON.getContentTypeValue())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    softly.assertThat(RestApiResponseType.XML.getContentTypeValue())
        .isEqualTo(MediaType.APPLICATION_XML_VALUE);
    softly.assertThat(RestApiResponseType.HEADER.getContentTypeValue())
        .isEqualTo(MediaType.TEXT_PLAIN_VALUE);
  }

  /**
   * Detect json by accepted.
   *
   * @param softly the softly
   */
  @Test
  void detectJsonByAccepted(SoftAssertions softly) {
    RestApiResponseType actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.ALL));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.APPLICATION_JSON));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.APPLICATION_PROBLEM_JSON));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByAccepted(List.of(parseMediaType("application/*")));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.APPLICATION_XML));
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByAccepted(List.of(parseMediaType("text/*")));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.TEXT_XML));
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByAccepted(List.of(
            MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON
        ));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType.detectByAccepted(null);
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType.detectByAccepted(List.of());
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.JSON);
  }

  /**
   * Detect xml by accepted.
   *
   * @param softly the softly
   */
  @Test
  void detectXmlByAccepted(SoftAssertions softly) {
    RestApiResponseType actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.ALL));
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.APPLICATION_JSON));
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.APPLICATION_XML));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.APPLICATION_PROBLEM_XML));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.TEXT_XML));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByAccepted(List.of(parseMediaType("application/*")));
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByAccepted(List.of(parseMediaType("text/*")));
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByAccepted(List.of(
            MediaType.TEXT_HTML,
            MediaType.APPLICATION_XML
        ));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType.detectByAccepted(null);
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType.detectByAccepted(List.of());
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.XML);
  }

  /**
   * Detect header by accepted.
   *
   * @param softly the softly
   */
  @Test
  void detectHeaderByAccepted(SoftAssertions softly) {
    RestApiResponseType actual = RestApiResponseType
        .detectByAccepted(List.of(MediaType.TEXT_HTML));
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.HEADER);

    actual = RestApiResponseType.detectByAccepted(null);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.HEADER);

    actual = RestApiResponseType.detectByAccepted(List.of());
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.HEADER);
  }

  /**
   * Detect json by content type.
   *
   * @param softly the softly
   */
  @Test
  void detectJsonByContentType(SoftAssertions softly) {
    RestApiResponseType actual = RestApiResponseType
        .detectByContentType(MediaType.APPLICATION_JSON);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByContentType(MediaType.APPLICATION_PROBLEM_JSON);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByContentType(MediaType.parseMediaType("text/*"));
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.JSON);

    actual = RestApiResponseType
        .detectByContentType(null);
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.JSON);
  }

  /**
   * Detect xml by content type.
   *
   * @param softly the softly
   */
  @Test
  void detectXmlByContentType(SoftAssertions softly) {
    RestApiResponseType actual = RestApiResponseType
        .detectByContentType(MediaType.APPLICATION_XML);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByContentType(MediaType.APPLICATION_PROBLEM_XML);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByContentType(MediaType.TEXT_XML);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.XML);

    actual = RestApiResponseType
        .detectByContentType(null);
    softly.assertThat(actual)
        .isNotEqualTo(RestApiResponseType.XML);
  }

  /**
   * Detect header by content type.
   *
   * @param softly the softly
   */
  @Test
  void detectHeaderByContentType(SoftAssertions softly) {
    RestApiResponseType actual = RestApiResponseType
        .detectByContentType(MediaType.TEXT_PLAIN);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.HEADER);

    actual = RestApiResponseType
        .detectByContentType(null);
    softly.assertThat(actual)
        .isEqualTo(RestApiResponseType.HEADER);
  }

}