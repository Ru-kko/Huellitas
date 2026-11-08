package com.citas.ports.out.persistence;

import com.citas.models.Veterinario;
import java.util.List;
import java.util.Optional;

public interface VeterinarioRepository {
  void save(Veterinario veterinario);

  Optional<Veterinario> findById(String id);

  List<Veterinario> findAll();

  Integer count();
}