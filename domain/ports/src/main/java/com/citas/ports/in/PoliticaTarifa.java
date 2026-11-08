package com.citas.ports.in;

import com.citas.models.Mascota;
import com.citas.models.TipoConsulta;
import java.time.LocalDateTime;

public interface PoliticaTarifa {
  double calcular(TipoConsulta tipo, Mascota mascota, LocalDateTime fecha);
}