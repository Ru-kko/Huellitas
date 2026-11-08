package com.citas.models.errors;

import java.time.LocalDateTime;

public class HorarioNoValidoException extends AplicacionCitaException {
  public HorarioNoValidoException(LocalDateTime fecha) {
    super(
        "La fecha " + fecha + " está fuera de horario: las citas se agendan de lunes a sábado, "
            + "entre las 8:00 y las 17:00, en punto o a la media hora (8:00, 8:30 … 16:30), "
            + "en una fecha y hora futuras y con máximo 30 días de anticipación");
  }
}