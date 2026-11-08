package com.citas.application.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.citas.ports.out.persistence.CitaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerarCodigoCitasTest {
  private static final LocalDateTime FECHA = LocalDateTime.of(2030, 1, 7, 9, 0);

  @Mock
  private CitaRepository repositorio;

  private GenerarCodigoCitas generador;

  @BeforeEach
  void setUp() {
    generador = new GenerarCodigoCitas(repositorio);
  }

  @Test
  void primeraCitaDelDiaEmpiezaEnUno() {
    when(repositorio.countPorFecha(FECHA.toLocalDate())).thenReturn(0L);
    assertEquals("VET-20300107-001", generador.generarCodigoCita(FECHA));
  }

  @ParameterizedTest
  @CsvSource({"1,002", "9,010", "41,042", "99,100", "997,998", "998,999"})
  void elConsecutivoSiempreTieneTresDigitos(long existentes, String esperado) {
    when(repositorio.countPorFecha(FECHA.toLocalDate())).thenReturn(existentes);
    assertEquals("VET-20300107-" + esperado, generador.generarCodigoCita(FECHA));
  }

  @Test
  void despuesDel999VuelveACero() {
    when(repositorio.countPorFecha(FECHA.toLocalDate())).thenReturn(999L);
    assertEquals("VET-20300107-000", generador.generarCodigoCita(FECHA));
  }

  @Test
  void despuesDelCeroContinuaEnUno() {
    when(repositorio.countPorFecha(FECHA.toLocalDate())).thenReturn(1000L);
    assertEquals("VET-20300107-001", generador.generarCodigoCita(FECHA));
  }

  @Test
  void consultaLasCitasDelDiaDeLaCitaSinImportarLaHora() {
    generador.generarCodigoCita(LocalDateTime.of(2030, 12, 31, 23, 59));
    verify(repositorio).countPorFecha(LocalDate.of(2030, 12, 31));
  }

  @Test
  void usaLaFechaDeLaCitaEnElCodigoConMesYDiaConCeros() {
    var fecha = LocalDateTime.of(2031, 3, 5, 16, 30);
    when(repositorio.countPorFecha(fecha.toLocalDate())).thenReturn(4L);
    assertEquals("VET-20310305-005", generador.generarCodigoCita(fecha));
  }
}
