package com.citas.application.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.citas.models.Cita;
import com.citas.models.EstadoCita;
import com.citas.models.Mascota;
import com.citas.models.TipoConsulta;
import com.citas.models.Veterinario;
import com.citas.models.errors.AgendaOcupadaException;
import com.citas.models.errors.CancelacionNoPermitidaException;
import com.citas.models.errors.HorarioNoValidoException;
import com.citas.models.errors.NoEncontradoException;
import com.citas.models.errors.TransicionNoPermitidaException;
import com.citas.ports.in.PoliticaTarifa;
import com.citas.ports.out.Notifier;
import com.citas.ports.out.persistence.CitaRepository;
import com.citas.ports.out.persistence.MascotaRepository;
import com.citas.ports.out.persistence.VeterinarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneradorCitasServicioTest {
  private static final LocalDateTime FECHA = LocalDateTime.of(2030, 1, 7, 9, 0);

  @Mock private MascotaRepository mascotaRepository;
  @Mock private VeterinarioRepository veterinarioRepository;
  @Mock private CitaRepository citaRepository;
  @Mock private ValidadorAgenda validadorAgenda;
  @Mock private PoliticaTarifa politicaTarifa;
  @Mock private GenerarCodigoCitas generarCodigoCitas;
  @Mock private Notifier notifier;

  private GeneradorCitasServicio servicio;

  private final Mascota mascota =
      Mascota.builder().id("M-001").nombre("Firulais").edad(4).build();
  private final Veterinario veterinario =
      Veterinario.builder().id("VET-001").nombre("Ana").build();

  @BeforeEach
  void setUp() {
    servicio = new GeneradorCitasServicio(
        mascotaRepository,
        veterinarioRepository,
        citaRepository,
        validadorAgenda,
        politicaTarifa,
        generarCodigoCitas,
        notifier);
  }

  private static List<Cita> pagina(Cita... citas) {
    return List.of(citas);
  }

  private static Cita citaEn(EstadoCita estado) {
    return Cita.builder().estado(estado).build();
  }

  private void mascotaYVeterinarioExisten() {
    when(mascotaRepository.findById(mascota.getId().toString())).thenReturn(Optional.of(mascota));
    when(veterinarioRepository.findById(veterinario.getId()))
        .thenReturn(Optional.of(veterinario));
  }

  private void agendaLibre() {
    when(citaRepository.findByVeterinarioAndDate(veterinario.getId(), FECHA))
        .thenReturn(pagina());
    when(citaRepository.findByPetAndDate(mascota.getId().toString(), FECHA)).thenReturn(pagina());
  }

  private Cita agendar() {
    return servicio.agendar(
        mascota.getId().toString(), veterinario.getId(), FECHA, TipoConsulta.GENERAL);
  }

  @Test
  void agendaLaCitaConTarifaCodigoYEstadoProgramada() {
    mascotaYVeterinarioExisten();
    agendaLibre();
    when(politicaTarifa.calcular(TipoConsulta.GENERAL, mascota, FECHA)).thenReturn(50000.0);
    when(generarCodigoCitas.generarCodigoCita(FECHA)).thenReturn("VET-20300107-001");

    Cita cita = agendar();

    assertEquals("VET-20300107-001", cita.getCodigo());
    assertSame(mascota, cita.getMascota());
    assertSame(veterinario, cita.getVeterinario());
    assertEquals(FECHA, cita.getFechaHora());
    assertEquals(50000.0, cita.getValor());
    assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
    assertEquals(TipoConsulta.GENERAL, cita.getTipoConsulta());
  }

  @Test
  void persisteLaCitaYNotificaDespuesDeValidar() {
    mascotaYVeterinarioExisten();
    agendaLibre();
    when(generarCodigoCitas.generarCodigoCita(FECHA)).thenReturn("VET-20300107-001");

    Cita cita = agendar();

    InOrder orden = inOrder(
        validadorAgenda, politicaTarifa, generarCodigoCitas, citaRepository, notifier);
    orden.verify(validadorAgenda).validarHorarioDisponible(FECHA);
    orden.verify(politicaTarifa).calcular(TipoConsulta.GENERAL, mascota, FECHA);
    orden.verify(generarCodigoCitas).generarCodigoCita(FECHA);
    orden.verify(citaRepository).save(cita);
    orden.verify(notifier).notify(anyString());
  }

  @Test
  void elMensajeDeNotificacionIncluyeCodigoMascotaYVeterinario() {
    mascotaYVeterinarioExisten();
    agendaLibre();
    when(generarCodigoCitas.generarCodigoCita(FECHA)).thenReturn("VET-20300107-001");

    agendar();

    var mensaje = ArgumentCaptor.forClass(String.class);
    verify(notifier).notify(mensaje.capture());
    assertTrue(mensaje.getValue().contains("VET-20300107-001"));
    assertTrue(mensaje.getValue().contains("Firulais"));
    assertTrue(mensaje.getValue().contains("Ana"));
  }

  @Test
  void fallaSiLaMascotaNoExiste() {
    when(mascotaRepository.findById(mascota.getId().toString())).thenReturn(Optional.empty());

    var ex = assertThrows(NoEncontradoException.class, this::agendar);

    assertEquals("Mascota no encontrada", ex.getMessage());
    verifyNoInteractions(citaRepository, notifier);
  }

  @Test
  void fallaSiElVeterinarioNoExiste() {
    when(mascotaRepository.findById(mascota.getId().toString())).thenReturn(Optional.of(mascota));
    when(veterinarioRepository.findById(veterinario.getId().toString()))
        .thenReturn(Optional.empty());

    var ex = assertThrows(NoEncontradoException.class, this::agendar);

    assertEquals("Veterinario no encontrada", ex.getMessage());
    verifyNoInteractions(citaRepository, notifier);
  }

  @Test
  void fallaSiElHorarioNoEsValidoSinConsultarLaAgenda() {
    mascotaYVeterinarioExisten();
    doThrow(new HorarioNoValidoException(FECHA)).when(validadorAgenda).validarHorarioDisponible(FECHA);

    assertThrows(HorarioNoValidoException.class, this::agendar);

    verifyNoInteractions(citaRepository, notifier);
  }

  @Test
  void fallaSiElVeterinarioTieneUnaCitaProgramadaEnElBloque() {
    mascotaYVeterinarioExisten();
    when(citaRepository.findByVeterinarioAndDate(veterinario.getId().toString(), FECHA))
        .thenReturn(pagina(citaEn(EstadoCita.PROGRAMADA)));

    var ex = assertThrows(AgendaOcupadaException.class, this::agendar);

    assertTrue(ex.getMessage().startsWith("El veterinario Ana"));
    verify(citaRepository, never()).save(any());
    verifyNoInteractions(notifier);
  }

  @Test
  void fallaSiLaMascotaTieneUnaCitaProgramadaEseDia() {
    mascotaYVeterinarioExisten();
    when(citaRepository.findByVeterinarioAndDate(veterinario.getId().toString(), FECHA))
        .thenReturn(pagina());
    when(citaRepository.findByPetAndDate(mascota.getId().toString(), FECHA))
        .thenReturn(pagina(citaEn(EstadoCita.PROGRAMADA)));

    var ex = assertThrows(AgendaOcupadaException.class, this::agendar);

    assertTrue(ex.getMessage().startsWith("La mascota Firulais"));
    verify(citaRepository, never()).save(any());
    verifyNoInteractions(notifier);
  }

  @Test
  void citasEnOtrosEstadosNoOcupanLaAgenda() {
    mascotaYVeterinarioExisten();
    when(citaRepository.findByVeterinarioAndDate(veterinario.getId().toString(), FECHA))
        .thenReturn(pagina(citaEn(EstadoCita.CANCELADA), citaEn(EstadoCita.ATENDIDA)));
    when(citaRepository.findByPetAndDate(mascota.getId().toString(), FECHA))
        .thenReturn(pagina(citaEn(EstadoCita.NO_ASISTIDA)));
    when(generarCodigoCitas.generarCodigoCita(FECHA)).thenReturn("VET-20300107-001");

    Cita cita = agendar();

    verify(citaRepository).save(cita);
  }

  private static Cita citaProgramadaEn(LocalDateTime fechaHora) {
    return Cita.builder()
        .codigo("VET-20300107-001")
        .estado(EstadoCita.PROGRAMADA)
        .fechaHora(fechaHora)
        .build();
  }

  @Test
  void cancelaUnaCitaProgramadaConMasDe2HorasDeAnticipacion() {
    var cita = citaProgramadaEn(LocalDateTime.now().plusHours(3));
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    servicio.cancelar("VET-20300107-001");

    assertEquals(EstadoCita.CANCELADA, cita.getEstado());
    InOrder orden = inOrder(citaRepository, notifier);
    orden.verify(citaRepository).save(cita);
    orden.verify(notifier).notify("Cita VET-20300107-001 cancelada");
  }

  @Test
  void noCancelaConMenosDe2HorasDeAnticipacion() {
    var cita = citaProgramadaEn(LocalDateTime.now().plusHours(1));
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    assertThrows(CancelacionNoPermitidaException.class, () -> servicio.cancelar("VET-20300107-001"));

    assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
    verify(citaRepository, never()).save(any());
  }

  @Test
  void noCancelaUnaCitaYaPasada() {
    var cita = citaProgramadaEn(LocalDateTime.now().minusDays(1));
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    assertThrows(CancelacionNoPermitidaException.class, () -> servicio.cancelar("VET-20300107-001"));

    verify(citaRepository, never()).save(any());
  }

  @ParameterizedTest
  @EnumSource(value = EstadoCita.class, names = "PROGRAMADA", mode = EnumSource.Mode.EXCLUDE)
  void soloSePuedeCancelarUnaCitaProgramada(EstadoCita estado) {
    var cita = Cita.builder()
        .codigo("VET-20300107-001")
        .estado(estado)
        .fechaHora(LocalDateTime.now().plusDays(1))
        .build();
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    assertThrows(CancelacionNoPermitidaException.class, () -> servicio.cancelar("VET-20300107-001"));

    assertEquals(estado, cita.getEstado());
    verify(citaRepository, never()).save(any());
  }

  @Test
  void noCancelaUnaCitaQueNoExiste() {
    when(citaRepository.findByCode("VET-20300107-999")).thenReturn(Optional.empty());

    var ex = assertThrows(NoEncontradoException.class, () -> servicio.cancelar("VET-20300107-999"));

    assertEquals("Cita no encontrada", ex.getMessage());
    verify(citaRepository, never()).save(any());
  }

  @Test
  void atiendeUnaCitaProgramadaGuardandoLasObservaciones() {
    var cita = citaProgramadaEn(LocalDateTime.now().plusDays(1));
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    servicio.atender("VET-20300107-001", "Vacunas al día");

    assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
    assertEquals("Vacunas al día", cita.getObservaciones());
    InOrder orden = inOrder(citaRepository, notifier);
    orden.verify(citaRepository).save(cita);
    orden.verify(notifier).notify("Cita VET-20300107-001 atendida");
  }

  @ParameterizedTest
  @EnumSource(value = EstadoCita.class, names = "PROGRAMADA", mode = EnumSource.Mode.EXCLUDE)
  void soloSePuedeAtenderUnaCitaProgramada(EstadoCita estado) {
    var cita = Cita.builder().codigo("VET-20300107-001").estado(estado).build();
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    assertThrows(
        TransicionNoPermitidaException.class,
        () -> servicio.atender("VET-20300107-001", "obs"));

    assertEquals(estado, cita.getEstado());
    verify(citaRepository, never()).save(any());
    verifyNoInteractions(notifier);
  }

  @Test
  void noAtiendeUnaCitaQueNoExiste() {
    when(citaRepository.findByCode("VET-20300107-999")).thenReturn(Optional.empty());

    var ex = assertThrows(
        NoEncontradoException.class, () -> servicio.atender("VET-20300107-999", "obs"));

    assertEquals("Cita no encontrada", ex.getMessage());
    verify(citaRepository, never()).save(any());
  }

  @Test
  void marcaNoAsistidaUnaCitaProgramadaQueYaPaso() {
    var cita = citaProgramadaEn(LocalDateTime.now().minusMinutes(1));
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    servicio.marcarNoAsistio("VET-20300107-001");

    assertEquals(EstadoCita.NO_ASISTIDA, cita.getEstado());
    InOrder orden = inOrder(citaRepository, notifier);
    orden.verify(citaRepository).save(cita);
    orden.verify(notifier).notify("Cita VET-20300107-001 marcada como no asistida");
  }

  @Test
  void noMarcaNoAsistidaUnaCitaQueTodaviaNoOcurre() {
    var cita = citaProgramadaEn(LocalDateTime.now().plusHours(1));
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    assertThrows(
        TransicionNoPermitidaException.class, () -> servicio.marcarNoAsistio("VET-20300107-001"));

    assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
    verify(citaRepository, never()).save(any());
    verifyNoInteractions(notifier);
  }

  @ParameterizedTest
  @EnumSource(value = EstadoCita.class, names = "PROGRAMADA", mode = EnumSource.Mode.EXCLUDE)
  void soloSePuedeMarcarNoAsistidaUnaCitaProgramada(EstadoCita estado) {
    var cita = Cita.builder()
        .codigo("VET-20300107-001")
        .estado(estado)
        .fechaHora(LocalDateTime.now().minusDays(1))
        .build();
    when(citaRepository.findByCode("VET-20300107-001")).thenReturn(Optional.of(cita));

    assertThrows(
        TransicionNoPermitidaException.class, () -> servicio.marcarNoAsistio("VET-20300107-001"));

    assertEquals(estado, cita.getEstado());
    verify(citaRepository, never()).save(any());
    verifyNoInteractions(notifier);
  }

  @Test
  void noMarcaNoAsistidaUnaCitaQueNoExiste() {
    when(citaRepository.findByCode("VET-20300107-999")).thenReturn(Optional.empty());

    assertThrows(NoEncontradoException.class, () -> servicio.marcarNoAsistio("VET-20300107-999"));

    verify(citaRepository, never()).save(any());
  }

  @Test
  void noNotificaSiFallaLaPersistencia() {
    mascotaYVeterinarioExisten();
    agendaLibre();
    when(generarCodigoCitas.generarCodigoCita(FECHA)).thenReturn("VET-20300107-001");
    doThrow(new IllegalStateException("bd caída")).when(citaRepository).save(any());

    assertThrows(IllegalStateException.class, this::agendar);

    verifyNoInteractions(notifier);
  }
}
