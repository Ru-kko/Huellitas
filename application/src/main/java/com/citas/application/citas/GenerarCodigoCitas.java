package com.citas.application.citas;

import com.citas.ports.out.persistence.CitaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenerarCodigoCitas {
  private static final String PREFIJO = "VET";
  private static final int LIMITE_CONSECUTIVO = 1000;

  private final CitaRepository citasRepository;

  public String generarCodigoCita(LocalDateTime fechaHora) {
    LocalDate fecha = fechaHora.toLocalDate();
    long consecutivo = (citasRepository.countPorFecha(fecha) + 1) % LIMITE_CONSECUTIVO;

    return "%s-%s-%03d".formatted(
        PREFIJO, fecha.format(DateTimeFormatter.BASIC_ISO_DATE), consecutivo);
  }
}
