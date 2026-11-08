package com.citas.models.errors;

public class CancelacionNoPermitidaException extends AplicacionCitaException {
  public CancelacionNoPermitidaException() {
    super("Solo se puede cancelar una cita PROGRAMADA y con al menos 2 horas de anticipación");
  }
}