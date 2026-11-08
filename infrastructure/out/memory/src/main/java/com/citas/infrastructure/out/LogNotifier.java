package com.citas.infrastructure.out;

import com.citas.ports.out.Notifier;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Log
@Component
public class LogNotifier implements Notifier {
  @Override
  public void notify(String message) {
    log.info("Notification: " + message);
  }
}
