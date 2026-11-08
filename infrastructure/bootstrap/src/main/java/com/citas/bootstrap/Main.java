package com.citas.bootstrap;

import com.citas.bootstrap.cli.App;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
  public static void main(String[] args) {
    try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
      context.getBean(App.class).run();
    }
  }
}
