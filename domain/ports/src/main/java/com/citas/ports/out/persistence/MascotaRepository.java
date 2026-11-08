package com.citas.ports.out.persistence;

import com.citas.models.Mascota;

import java.util.List;
import java.util.Optional;

public interface MascotaRepository {
  void save(Mascota mascota);

  Optional<Mascota> findById(String id);

  Integer count();

  List<Mascota> findAll();
}