package com.citas.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoConsulta {
  GENERAL(50000),
  VACUNACION(35000),
  CONTROL(25000);

  private final int tarifaBase;
}