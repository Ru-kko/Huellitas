package com.citas.infrastructure.out.memory;

import com.citas.models.Cita;
import com.citas.ports.out.persistence.CitaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryCitaRepository implements CitaRepository {
  private final LinkedHashMap<String, Cita> tabla = new LinkedHashMap<>();

  @Override
  public void save(Cita cita) {
    tabla.put(cita.getCodigo(), cita);
  }

  @Override
  public Optional<Cita> findByCode(String codigo) {
    return Optional.ofNullable(tabla.get(codigo));
  }

  @Override
  public List<Cita> findByVeterinarioAndDate(String veterinarioId, LocalDateTime fecha) {
    return tabla.values().stream()
        .filter(cita -> cita.getVeterinario().getId().toString().equals(veterinarioId))
        .filter(cita -> cita.getFechaHora().equals(fecha))
        .toList();
  }

  @Override
  public List<Cita> findByPetAndDate(String mascotaId, LocalDateTime fecha) {
    return tabla.values().stream()
        .filter(cita -> cita.getMascota().getId().toString().equals(mascotaId))
        .filter(cita -> cita.getFechaHora().toLocalDate().equals(fecha.toLocalDate()))
        .toList();
  }

  @Override
  public String getLastCode() {
    var ultima = tabla.lastEntry();
    return ultima == null ? null : ultima.getKey();
  }

  @Override
  public long countPorFecha(LocalDate fecha) {
    return tabla.values().stream()
        .filter(cita -> cita.getFechaHora().toLocalDate().equals(fecha))
        .count();
  }

  @Override
  public List<Cita> findAll() {
    return tabla.values().stream().toList();
  }

  @Override
  public int count() {
    return tabla.size();
  }
}
