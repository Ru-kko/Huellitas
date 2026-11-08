package com.citas.ports.in;

import java.util.List;

import com.citas.models.Mascota;
import com.citas.models.Tutor;
import com.citas.models.Veterinario;

public interface RegisterUseCase {
  Veterinario registerVeterinario(String nombre);

  Mascota registerMascota(String nombre, String especie, int edad, Tutor tutor);

  List<Veterinario> listVeterinarios();
  List<Mascota> listMascotas();
}
