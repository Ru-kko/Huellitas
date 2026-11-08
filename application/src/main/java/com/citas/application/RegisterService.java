package com.citas.application;

import java.util.List;

import com.citas.models.Mascota;
import com.citas.models.Tutor;
import com.citas.models.Veterinario;
import com.citas.ports.in.RegisterUseCase;
import com.citas.ports.out.persistence.MascotaRepository;
import com.citas.ports.out.persistence.VeterinarioRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {
  private final MascotaRepository mascotaRepository;
  private final VeterinarioRepository veterinarioRepository;

  @Override 
  public Veterinario registerVeterinario(String nombre) {
    requerido(nombre, "El nombre del veterinario es obligatorio");

    Veterinario veterinario = Veterinario.builder()
        .id(String.format("VET-%03d", veterinarioRepository.count() + 1))
        .nombre(nombre)
        .build();
    veterinarioRepository.save(veterinario);
    return veterinario;
  }

  @Override 
  public Mascota registerMascota(String nombre, String especie, int edad, Tutor tutor) {
    requerido(nombre, "El nombre de la mascota es obligatorio");
    requerido(especie, "La especie de la mascota es obligatoria");

    if (edad < 0) {
      throw new IllegalArgumentException("La edad de la mascota no puede ser negativa");
    }
    if (tutor == null) {
      throw new IllegalArgumentException("Los datos del tutor son obligatorios");
    }
    requerido(tutor.getNombre(), "El nombre del tutor es obligatorio");
    requerido(tutor.getDocumento(), "El documento del tutor es obligatorio");
    requerido(tutor.getTelefono(), "El teléfono del tutor es obligatorio");
    requerido(tutor.getCorreo(), "El correo del tutor es obligatorio");

    Mascota mascota = Mascota.builder()
        .id(String.format("M-%03d", mascotaRepository.count() + 1))
        .nombre(nombre)
        .especie(especie)
        .edad(edad)
        .tutor(tutor)
        .build();
    mascotaRepository.save(mascota);
    return mascota;
  }


  @Override 
  public List<Veterinario> listVeterinarios() {
    return veterinarioRepository.findAll();
  }

  @Override 
  public List<Mascota> listMascotas() {
    return mascotaRepository.findAll();
  }

  private static void requerido(String valor, String mensaje) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException(mensaje);
    }
  }
}
