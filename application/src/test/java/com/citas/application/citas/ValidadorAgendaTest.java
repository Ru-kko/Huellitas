package com.citas.application.citas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.citas.models.errors.HorarioNoValidoException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ValidadorAgendaTest {
  private final ValidadorAgenda validador = new ValidadorAgenda();

  /** Primer día futuro (dentro de los próximos 7 días) que cae en el día de la semana dado. */
  private static LocalDate proximo(DayOfWeek dia) {
    LocalDate fecha = LocalDate.now().plusDays(1);
    while (fecha.getDayOfWeek() != dia) {
      fecha = fecha.plusDays(1);
    }
    return fecha;
  }

  private static LocalDateTime manana(int hora, int minuto) {
    return proximo(DayOfWeek.WEDNESDAY).atTime(hora, minuto);
  }

  @ParameterizedTest
  @EnumSource(value = DayOfWeek.class, names = "SUNDAY", mode = EnumSource.Mode.EXCLUDE)
  void aceptaCualquierDiaDeLunesASabado(DayOfWeek dia) {
    assertDoesNotThrow(() -> validador.validarHorarioDisponible(proximo(dia).atTime(9, 0)));
  }

  @Test
  void rechazaDomingo() {
    var fecha = proximo(DayOfWeek.SUNDAY).atTime(9, 0);
    assertThrows(HorarioNoValidoException.class, () -> validador.validarHorarioDisponible(fecha));
  }

  @ParameterizedTest
  @CsvSource({"8,0", "8,30", "12,0", "16,0", "16,30"})
  void aceptaHorasEnPuntoOALaMediaDentroDelHorario(int hora, int minuto) {
    assertDoesNotThrow(() -> validador.validarHorarioDisponible(manana(hora, minuto)));
  }

  @ParameterizedTest
  @CsvSource({"7,30", "7,0", "17,0", "17,30", "18,0", "0,0"})
  void rechazaHorasFueraDelHorarioDeAtencion(int hora, int minuto) {
    var fecha = manana(hora, minuto);
    assertThrows(HorarioNoValidoException.class, () -> validador.validarHorarioDisponible(fecha));
  }

  @ParameterizedTest
  @CsvSource({"8,15", "9,45", "10,1", "16,45", "16,59"})
  void rechazaHorasQueNoSonEnPuntoNiALaMedia(int hora, int minuto) {
    var fecha = manana(hora, minuto);
    assertThrows(HorarioNoValidoException.class, () -> validador.validarHorarioDisponible(fecha));
  }

  @Test
  void rechazaHoraConSegundos() {
    var fecha = manana(9, 0).withSecond(1);
    assertThrows(HorarioNoValidoException.class, () -> validador.validarHorarioDisponible(fecha));
  }

  @Test
  void rechazaFechasPasadas() {
    var fecha = LocalDate.now().minusDays(1);
    if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
      fecha = fecha.minusDays(1);
    }
    var pasada = fecha.atTime(9, 0);
    assertThrows(HorarioNoValidoException.class, () -> validador.validarHorarioDisponible(pasada));
  }

  @Test
  void aceptaFechaDentroDeLos30Dias() {
    var fecha = LocalDate.now().plusDays(29);
    if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
      fecha = fecha.minusDays(1);
    }
    var dentro = fecha.atTime(9, 0);
    assertDoesNotThrow(() -> validador.validarHorarioDisponible(dentro));
  }

  @Test
  void rechazaFechaConMasDe30DiasDeAnticipacion() {
    var fecha = LocalDate.now().plusDays(31);
    if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
      fecha = fecha.plusDays(1);
    }
    var lejana = fecha.atTime(9, 0);
    assertThrows(HorarioNoValidoException.class, () -> validador.validarHorarioDisponible(lejana));
  }

  @Test
  void elMensajeIncluyeLaFechaRechazada() {
    var fecha = proximo(DayOfWeek.SUNDAY).atTime(9, 0);
    var ex = assertThrows(
        HorarioNoValidoException.class, () -> validador.validarHorarioDisponible(fecha));
    assertTrue(ex.getMessage().contains(fecha.toString()));
  }
}
