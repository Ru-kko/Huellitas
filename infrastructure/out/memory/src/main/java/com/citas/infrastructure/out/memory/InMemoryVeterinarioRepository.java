package com.citas.infrastructure.out.memory;

import com.citas.models.Veterinario;
import com.citas.ports.out.persistence.VeterinarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryVeterinarioRepository implements VeterinarioRepository {
  private final Map<String, Veterinario> tabla;

  public InMemoryVeterinarioRepository() {
      this.tabla = new HashMap<>();
      String vet1Id = "VET-01";
      String vet2Id = "VET-02";

      this.tabla.put(vet1Id, Veterinario.builder().id(vet1Id).nombre("Laura Gomez").build());
      this.tabla.put(vet2Id, Veterinario.builder().id(vet2Id).nombre("Maria").build());
  }

  @Override
  public void save(Veterinario veterinario) {
    tabla.put(veterinario.getId(), veterinario);
  }

  @Override
  public Optional<Veterinario> findById(String id) {
    return Optional.ofNullable(tabla.get(id));
  }

  @Override
  public List<Veterinario> findAll() {
    return tabla.values().stream()
        .sorted(Comparator.comparing(Veterinario::getId))
        .toList();
  }

  @Override
  public Integer count() {
    return tabla.size();
  }
}
