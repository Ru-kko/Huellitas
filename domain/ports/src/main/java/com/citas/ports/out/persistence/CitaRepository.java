package com.citas.ports.out.persistence;

import com.citas.models.Cita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository {
  void save(Cita cita);

  Optional<Cita> findByCode(String codigo);

  List<Cita> findByVeterinarioAndDate(String veterinarioId, LocalDateTime fecha);

  List<Cita> findByPetAndDate(String mascotaId, LocalDateTime fecha);

  List<Cita> findAll();

  int count();

  String getLastCode();

  long countPorFecha(LocalDate fecha);
}