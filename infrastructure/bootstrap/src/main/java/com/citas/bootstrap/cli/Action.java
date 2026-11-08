package com.citas.bootstrap.cli;

import lombok.Getter;

@Getter
public enum Action {
  AGENDAR_CITA("Agendar Cita"),
  CANCELAR_CITA("Cancelar Cita"),
  ATENDER_CITA("Atender Cita"),
  MARCAR_NO_ASISTIO("Marcar No Asistió"),
  REGISTRAR_MASCOTA("Registrar Mascota"),
  REGISTRAR_VETERINARIO("Registrar Veterinario"),
  SALIR("Salir");

  private final String description;

  Action(String description) {
    this.description = description;
  }
}
