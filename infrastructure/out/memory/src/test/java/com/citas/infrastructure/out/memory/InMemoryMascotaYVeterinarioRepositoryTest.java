package com.citas.infrastructure.out.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.citas.models.Mascota;
import com.citas.models.Veterinario;
import org.junit.jupiter.api.Test;

class InMemoryMascotaYVeterinarioRepositoryTest {
  @Test
  void mascotaSeGuardaYSeBuscaPorId() {
    var repositorio = new InMemoryMascotaRepository();
    var mascota = Mascota.builder().id("M-001").nombre("Firulais").build();

    repositorio.save(mascota);

    assertSame(mascota, repositorio.findById(mascota.getId()).orElseThrow());
    assertTrue(repositorio.findById("M-999").isEmpty());
  }

  @Test
  void mascotaGuardadaDeNuevoConElMismoIdSeReemplaza() {
    var repositorio = new InMemoryMascotaRepository();
    var id = "M-001";
    repositorio.save(Mascota.builder().id(id).nombre("Firulais").build());
    repositorio.save(Mascota.builder().id(id).nombre("Rex").build());

    assertEquals("Rex", repositorio.findById(id).orElseThrow().getNombre());
  }

  @Test
  void mascotasSeListanYSeCuentan() {
    var repositorio = new InMemoryMascotaRepository();
    assertEquals(0, repositorio.count());
    assertTrue(repositorio.findAll().isEmpty());

    repositorio.save(Mascota.builder().id("M-001").nombre("Firulais").build());
    repositorio.save(Mascota.builder().id("M-002").nombre("Misu").build());

    assertEquals(2, repositorio.count());
    assertEquals(2, repositorio.findAll().size());
  }

  @Test
  void veterinariosSeListanOrdenadosPorIdYSeCuentan() {
    var repositorio = new InMemoryVeterinarioRepository();
    int inicial = repositorio.count();
    repositorio.save(Veterinario.builder().id("VET-900").nombre("Zoe").build());
    repositorio.save(Veterinario.builder().id("VET-050").nombre("Ada").build());

    var todos = repositorio.findAll();

    assertEquals(inicial + 2, repositorio.count());
    assertEquals(inicial + 2, todos.size());
    assertEquals(todos.stream().map(Veterinario::getId).sorted().toList(),
        todos.stream().map(Veterinario::getId).toList());
  }

  @Test
  void veterinarioSeGuardaYSeBuscaPorId() {
    var repositorio = new InMemoryVeterinarioRepository();
    var veterinario = Veterinario.builder().id("VET-001").nombre("Ana").build();

    repositorio.save(veterinario);

    assertSame(veterinario, repositorio.findById(veterinario.getId()).orElseThrow());
    assertTrue(repositorio.findById("VET-999").isEmpty());
  }
}
