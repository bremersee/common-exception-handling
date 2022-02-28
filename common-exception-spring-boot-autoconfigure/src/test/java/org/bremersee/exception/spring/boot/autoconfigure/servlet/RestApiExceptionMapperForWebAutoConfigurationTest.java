package org.bremersee.exception.spring.boot.autoconfigure.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.spring.boot.autoconfigure.RestApiExceptionMapperBootProperties;
import org.junit.jupiter.api.Test;

class RestApiExceptionMapperForWebAutoConfigurationTest {

  private final RestApiExceptionMapperForWebAutoConfiguration target
      = new RestApiExceptionMapperForWebAutoConfiguration(
      "test", new RestApiExceptionMapperBootProperties());

  @Test
  void init() {
    assertThatNoException().isThrownBy(target::init);
  }

  @Test
  void restApiExceptionMapper() {
    RestApiExceptionMapper actual = target.restApiExceptionMapper();
    assertThat(actual)
        .isNotNull();
  }
}