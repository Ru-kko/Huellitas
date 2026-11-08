package com.citas.bootstrap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class AppConfigTest {
  @Test
  void contextoArranca() {
    try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
      assertNotNull(context.getBean(AppConfig.class));
    }
  }
}
