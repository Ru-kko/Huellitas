package com.citas.application.citas;

import com.citas.models.errors.HorarioNoValidoException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidadorAgenda {
  private static final LocalTime PRIMERA_CITA = LocalTime.of(8, 0);
  private static final LocalTime ULTIMA_CITA = LocalTime.of(16, 30);
  private static final int DIAS_MAXIMOS_ANTICIPACION = 30;

  public void validarHorarioDisponible(LocalDateTime fechaHora) {
    LocalDateTime now = LocalDateTime.now();

    if (!esDiaDeAtencion(fechaHora)
        || !esHoraDeCita(fechaHora.toLocalTime())
        || !esFechaAgendable(fechaHora, now)) {
      throw new HorarioNoValidoException(fechaHora);
    }
  }

  private boolean esDiaDeAtencion(LocalDateTime fechaHora) {
    return fechaHora.getDayOfWeek() != DayOfWeek.SUNDAY;
  }

  private boolean esHoraDeCita(LocalTime hora) {
    boolean enPuntoOMedia = (hora.getMinute() == 0 || hora.getMinute() == 30)
        && hora.getSecond() == 0
        && hora.getNano() == 0;
    return enPuntoOMedia && !hora.isBefore(PRIMERA_CITA) && !hora.isAfter(ULTIMA_CITA);
  }

  private boolean esFechaAgendable(LocalDateTime fechaHora, LocalDateTime now) {
    return fechaHora.isAfter(now)
        && !fechaHora.isAfter(now.plusDays(DIAS_MAXIMOS_ANTICIPACION));
  }
}
