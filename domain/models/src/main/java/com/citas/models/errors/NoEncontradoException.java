package com.citas.models.errors;

public class NoEncontradoException extends AplicacionCitaException {
  public NoEncontradoException(String entidad) {
    super(entidad + " no encontrada");
  }
}