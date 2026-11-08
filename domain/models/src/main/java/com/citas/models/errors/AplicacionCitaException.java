package com.citas.models.errors;

public class AplicacionCitaException extends RuntimeException {
  public AplicacionCitaException(String message) {
    super(message);
  }

  public AplicacionCitaException(String message, Throwable cause) {
    super(message, cause);
  }
}