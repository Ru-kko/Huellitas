package com.citas.infrastructure.out.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.citas.models.Cita;
import com.citas.models.EstadoCita;
import com.citas.models.Mascota;
import com.citas.models.Veterinario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class InMemoryCitaRepositoryTest {
  private static final LocalDateTime FECHA = LocalDateTime.of(2030, 1, 7, 9, 0);

  private final InMemoryCitaRepository repositorio = new InMemoryCitaRepository();
  private final Mascota mascota = Mascota.builder().id("M-001").nombre("Firulais").build();
  private final Veterinario veterinario =
      Veterinario.builder().id("VET-001").nombre("Ana").build();

  private Cita cita(String codigo, Mascota m, Veterinario v, LocalDateTime fecha) {
    return Cita.builder()
        .codigo(codigo)
        .mascota(m)
        .veterinario(v)
        .fechaHora(fecha)
        .estado(EstadoCita.PROGRAMADA)
        .build();
  }

  @Test
  void guardaYBuscaPorCodigo() {
    var cita = cita("VET-20300107-001", mascota, veterinario, FECHA);
    repositorio.save(cita);

    assertSame(cita, repositorio.findByCode("VET-20300107-001").orElseThrow());
    assertTrue(repositorio.findByCode("VET-20300107-999").isEmpty());
  }

  @Test
  void guardarDeNuevoUnaCitaExistenteLaActualiza() {
    var cita = cita("VET-20300107-001", mascota, veterinario, FECHA);
    repositorio.save(cita);
    cita.cancelar();
    repositorio.save(cita);

    assertEquals(EstadoCita.CANCELADA, repositorio.findByCode("VET-20300107-001").orElseThrow().getEstado());
    assertEquals(1, repositorio.countPorFecha(FECHA.toLocalDate()));
  }

  @Test
  void buscaCitasDelVeterinarioSoloEnElBloqueExacto() {
    var otroVet = Veterinario.builder().id("VET-002").nombre("Luis").build();
    repositorio.save(cita("VET-20300107-001", mascota, veterinario, FECHA));
    repositorio.save(cita("VET-20300107-002", mascota, veterinario, FECHA.plusMinutes(30)));
    repositorio.save(cita("VET-20300107-003", mascota, otroVet, FECHA));

    var pagina = repositorio.findByVeterinarioAndDate(veterinario.getId().toString(), FECHA);

    assertEquals(1, pagina.size());
    assertEquals("VET-20300107-001", pagina.get(0).getCodigo());
  }

  @Test
  void buscaCitasDeLaMascotaDeTodoElDia() {
    var otraMascota = Mascota.builder().id("M-002").nombre("Misu").build();
    repositorio.save(cita("VET-20300107-001", mascota, veterinario, FECHA));
    repositorio.save(cita("VET-20300107-002", mascota, veterinario, FECHA.plusHours(5)));
    repositorio.save(cita("VET-20300108-001", mascota, veterinario, FECHA.plusDays(1)));
    repositorio.save(cita("VET-20300107-003", otraMascota, veterinario, FECHA));

    var pagina = repositorio.findByPetAndDate(mascota.getId().toString(), FECHA);

    assertEquals(2, pagina.size());
  }

  @Test
  void sinResultadosDevuelveUnaListaVacia() {
    var pagina = repositorio.findByPetAndDate(mascota.getId().toString(), FECHA);

    assertTrue(pagina.isEmpty());
  }

  @Test
  void cuentaLasCitasDeUnDiaEnCualquierEstado() {
    var cancelada = cita("VET-20300107-002", mascota, veterinario, FECHA.plusHours(1));
    cancelada.cancelar();
    repositorio.save(cita("VET-20300107-001", mascota, veterinario, FECHA));
    repositorio.save(cancelada);
    repositorio.save(cita("VET-20300108-001", mascota, veterinario, FECHA.plusDays(1)));

    assertEquals(2, repositorio.countPorFecha(LocalDate.of(2030, 1, 7)));
    assertEquals(1, repositorio.countPorFecha(LocalDate.of(2030, 1, 8)));
    assertEquals(0, repositorio.countPorFecha(LocalDate.of(2030, 1, 9)));
  }

  @Test
  void ultimoCodigoEsElDeLaUltimaCitaCreada() {
    assertNull(repositorio.getLastCode());

    repositorio.save(cita("VET-20300107-001", mascota, veterinario, FECHA));
    repositorio.save(cita("VET-20300107-002", mascota, veterinario, FECHA.plusHours(1)));
    assertEquals("VET-20300107-002", repositorio.getLastCode());

    // Actualizar una cita existente no cambia cuál fue la última creada.
    repositorio.save(cita("VET-20300107-001", mascota, veterinario, FECHA));
    assertEquals("VET-20300107-002", repositorio.getLastCode());
  }
}
