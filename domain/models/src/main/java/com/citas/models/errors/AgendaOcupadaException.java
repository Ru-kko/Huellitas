package com.citas.models.errors;

import java.time.LocalDateTime;

public class AgendaOcupadaException extends AplicacionCitaException {
  public enum Tipo {
    VETERINARIO,
    MASCOTA
  }

  public AgendaOcupadaException(String nombre, Tipo tipo, LocalDateTime fecha) {
    super(
        tipo == Tipo.VETERINARIO
            ? "El veterinario " + nombre + " no tiene espacio en la hora " + fecha
                + ": ya tiene una cita PROGRAMADA en ese bloque"
            : "La mascota " + nombre + " ya tiene una cita PROGRAMADA el día " + fecha);
  }
}