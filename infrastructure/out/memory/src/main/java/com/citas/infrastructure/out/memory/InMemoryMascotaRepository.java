package com.citas.infrastructure.out.memory;

import com.citas.models.Mascota;
import com.citas.ports.out.persistence.MascotaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryMascotaRepository implements MascotaRepository {
  private final Map<String, Mascota> tabla = new HashMap<>();

  @Override
  public void save(Mascota mascota) {
    tabla.put(mascota.getId().toString(), mascota);
  }

  @Override
  public Optional<Mascota> findById(String id) {
    return Optional.ofNullable(tabla.get(id));
  }

  @Override
  public Integer count() {
    return tabla.size();
  }

  @Override
  public List<Mascota> findAll() {
    return List.copyOf(tabla.values());
  }
}
