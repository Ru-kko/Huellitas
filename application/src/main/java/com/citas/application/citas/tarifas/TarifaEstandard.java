package com.citas.application.citas.tarifas;

import java.time.LocalDateTime;

import com.citas.models.Mascota;
import com.citas.models.TipoConsulta;
import com.citas.ports.in.PoliticaTarifa;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TarifaEstandard implements PoliticaTarifa {
  private static final int EDAD_DESCUENTO = 10;
  private static final double DESCUENTO_MAYORES = 0.10;

  @Override
  public double calcular(TipoConsulta tipo, Mascota mascota, LocalDateTime fecha) {
    double valor = tipo.getTarifaBase();

    if (mascota.getEdad() >= EDAD_DESCUENTO) {
      valor -= valor * DESCUENTO_MAYORES;
    }
    return valor;
  }
}
