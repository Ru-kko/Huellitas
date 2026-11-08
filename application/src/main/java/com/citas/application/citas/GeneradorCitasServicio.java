package com.citas.application.citas;

import java.time.LocalDateTime;

import com.citas.models.Cita;
import com.citas.models.EstadoCita;
import com.citas.models.Mascota;
import com.citas.models.TipoConsulta;
import com.citas.models.Veterinario;
import com.citas.models.errors.AgendaOcupadaException;
import com.citas.models.errors.CancelacionNoPermitidaException;
import com.citas.models.errors.NoEncontradoException;
import com.citas.models.errors.TransicionNoPermitidaException;
import com.citas.ports.in.CitasUseCase;
import com.citas.ports.in.PoliticaTarifa;
import com.citas.ports.out.Notifier;
import com.citas.ports.out.persistence.CitaRepository;
import com.citas.ports.out.persistence.MascotaRepository;
import com.citas.ports.out.persistence.VeterinarioRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GeneradorCitasServicio implements CitasUseCase {
  private static final int HORAS_MINIMAS_CANCELACION = 2;

  private final MascotaRepository mascotaRepository;
  private final VeterinarioRepository veterinarioRepository;
  private final CitaRepository citaRepository;
  private final ValidadorAgenda validadorAgenda;
  private final PoliticaTarifa politicaTarifa;
  private final GenerarCodigoCitas generarCodigoCitas;
  private final Notifier notifier;

  @Override
  public Cita agendar(String idMascota, String idVeterinario, LocalDateTime fechaHora, TipoConsulta tipo) {
    Mascota mascota = mascotaRepository.findById(idMascota)
        .orElseThrow(() -> new NoEncontradoException("Mascota"));
    Veterinario veterinario = veterinarioRepository.findById(idVeterinario)
        .orElseThrow(() -> new NoEncontradoException("Veterinario"));

    validadorAgenda.validarHorarioDisponible(fechaHora);
    validarVeterinarioDisponible(veterinario, fechaHora);
    validarMascotaDisponible(mascota, fechaHora);

    double valor = politicaTarifa.calcular(tipo, mascota, fechaHora);
    String codigo = generarCodigoCitas.generarCodigoCita(fechaHora);

    Cita cita = Cita.builder()
        .codigo(codigo)
        .mascota(mascota)
        .veterinario(veterinario)
        .fechaHora(fechaHora)
        .valor(valor)
        .estado(EstadoCita.PROGRAMADA)
        .tipoConsulta(tipo)
        .observaciones("")
        .build();

    citaRepository.save(cita);
    notifier.notify("Cita %s programada para %s con %s el %s".formatted(
        codigo, mascota.getNombre(), veterinario.getNombre(), fechaHora));

    return cita;
  }

  private void validarVeterinarioDisponible(Veterinario veterinario, LocalDateTime fechaHora) {
    boolean ocupado = citaRepository
        .findByVeterinarioAndDate(veterinario.getId().toString(), fechaHora)
        .stream()
        .anyMatch(cita -> cita.getEstado() == EstadoCita.PROGRAMADA);

    if (ocupado) {
      throw new AgendaOcupadaException(
          veterinario.getNombre(), AgendaOcupadaException.Tipo.VETERINARIO, fechaHora);
    }
  }

  private void validarMascotaDisponible(Mascota mascota, LocalDateTime fechaHora) {
    boolean ocupada = citaRepository
        .findByPetAndDate(mascota.getId().toString(), fechaHora)
        .stream()
        .anyMatch(cita -> cita.getEstado() == EstadoCita.PROGRAMADA);

    if (ocupada) {
      throw new AgendaOcupadaException(
          mascota.getNombre(), AgendaOcupadaException.Tipo.MASCOTA, fechaHora);
    }
  }

  @Override
  public java.util.List<Cita> listarCitas() {
    return citaRepository.findAll();
  }

  @Override
  public void cancelar(String codigoCita) {
    Cita cita = citaRepository.findByCode(codigoCita)
        .orElseThrow(() -> new NoEncontradoException("Cita"));

    boolean conAnticipacion = !cita.getFechaHora()
        .isBefore(LocalDateTime.now().plusHours(HORAS_MINIMAS_CANCELACION));

    if (!cita.esProgramada() || !conAnticipacion) {
      throw new CancelacionNoPermitidaException();
    }

    cita.cancelar();
    citaRepository.save(cita);
    notifier.notify("Cita %s cancelada".formatted(cita.getCodigo()));
  }

  @Override
  public void atender(String codigo, String observaciones) {
    Cita cita = citaRepository.findByCode(codigo)
        .orElseThrow(() -> new NoEncontradoException("Cita"));

    if (!cita.esProgramada()) {
      throw new TransicionNoPermitidaException();
    }

    cita.atender(observaciones);
    citaRepository.save(cita);
    notifier.notify("Cita %s atendida".formatted(cita.getCodigo()));
  }

  @Override
  public void marcarNoAsistio(String codigo) {
    Cita cita = citaRepository.findByCode(codigo)
        .orElseThrow(() -> new NoEncontradoException("Cita"));

    if (!cita.esProgramada() || !LocalDateTime.now().isAfter(cita.getFechaHora())) {
      throw new TransicionNoPermitidaException();
    }

    cita.marcarNoAsistida();
    citaRepository.save(cita);
    notifier.notify("Cita %s marcada como no asistida".formatted(cita.getCodigo()));
  }
}
