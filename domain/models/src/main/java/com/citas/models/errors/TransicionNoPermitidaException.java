package com.citas.models.errors;

public class TransicionNoPermitidaException extends AplicacionCitaException {
  public TransicionNoPermitidaException() {
    super(
        "Solo una cita PROGRAMADA puede pasar a ATENDIDA o a NO_ASISTIO; "
            + "los demás estados son finales");
  }
}