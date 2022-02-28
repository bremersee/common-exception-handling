package org.bremersee.exception.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.RestApiExceptionMapperProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class RestApiExceptionMapperBootPropertiesTest {

  @Test
  void toRestApiExceptionMapperProperties(SoftAssertions softly) {
    RestApiExceptionMapperBootProperties source = new RestApiExceptionMapperBootProperties();
    RestApiExceptionMapperProperties actual = source.toRestApiExceptionMapperProperties();
    /*
    softly.assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(RestApiExceptionMapperProperties.builder().build());

     */
  }
}