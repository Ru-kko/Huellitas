package com.citas.application.citas.tarifas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.citas.models.Mascota;
import com.citas.models.TipoConsulta;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TarifaEstandardTest {
  private static final LocalDateTime FECHA = LocalDateTime.of(2030, 1, 7, 9, 0);

  private final TarifaEstandard tarifa = new TarifaEstandard();

  private static Mascota conEdad(int edad) {
    return Mascota.builder().nombre("Firulais").edad(edad).build();
  }

  @ParameterizedTest
  @CsvSource({"GENERAL,50000", "VACUNACION,35000", "CONTROL,25000"})
  void mascotasMenoresDe10AnosPaganLaTarifaBase(TipoConsulta tipo, double esperado) {
    assertEquals(esperado, tarifa.calcular(tipo, conEdad(9), FECHA));
  }

  @ParameterizedTest
  @CsvSource({"GENERAL,45000", "VACUNACION,31500", "CONTROL,22500"})
  void mascotasDe10AnosOMasTienen10PorCientoDeDescuento(TipoConsulta tipo, double esperado) {
    assertEquals(esperado, tarifa.calcular(tipo, conEdad(10), FECHA));
    assertEquals(esperado, tarifa.calcular(tipo, conEdad(15), FECHA));
  }

  @Test
  void cachorroPagaTarifaBase() {
    assertEquals(50000, tarifa.calcular(TipoConsulta.GENERAL, conEdad(0), FECHA));
  }

  @Test
  void laFechaNoAfectaLaTarifa() {
    var otraFecha = LocalDateTime.of(2031, 6, 15, 16, 30);
    assertEquals(
        tarifa.calcular(TipoConsulta.CONTROL, conEdad(3), FECHA),
        tarifa.calcular(TipoConsulta.CONTROL, conEdad(3), otraFecha));
  }
}
