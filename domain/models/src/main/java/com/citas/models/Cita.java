package com.citas.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter 
@Builder
@ToString 
@NoArgsConstructor
@AllArgsConstructor
public class Cita {
  private String codigo;
  private Mascota mascota;
  private Veterinario veterinario;
  private LocalDateTime fechaHora;
  private double valor;
  private EstadoCita estado;
  private TipoConsulta tipoConsulta;
  private String observaciones;

  public void cancelar() {
    this.estado = EstadoCita.CANCELADA;
  }

  public void atender() {
    this.estado = EstadoCita.ATENDIDA;
  }

  public void atender(String observaciones) {
    atender();
    this.observaciones = observaciones;
  }

  public void marcarNoAsistida() {
    this.estado = EstadoCita.NO_ASISTIDA;
  }

  public boolean esProgramada() {
    return this.estado == EstadoCita.PROGRAMADA;
  }
}