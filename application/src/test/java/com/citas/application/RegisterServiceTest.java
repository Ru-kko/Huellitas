package com.citas.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.citas.models.Mascota;
import com.citas.models.Tutor;
import com.citas.models.Veterinario;
import com.citas.ports.out.persistence.MascotaRepository;
import com.citas.ports.out.persistence.VeterinarioRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {
  @Mock private MascotaRepository mascotaRepository;
  @Mock private VeterinarioRepository veterinarioRepository;

  private RegisterService servicio;

  private final Tutor tutor = Tutor.builder()
      .nombre("Juan").documento("123").telefono("300").correo("juan@mail.com").build();

  @BeforeEach
  void setUp() {
    servicio = new RegisterService(mascotaRepository, veterinarioRepository);
  }

  @Test
  void registraVeterinarioConIdConsecutivoDeTresDigitos() {
    when(veterinarioRepository.count()).thenReturn(2);

    Veterinario vet = servicio.registerVeterinario("Ana");

    assertEquals("VET-003", vet.getId());
    assertEquals("Ana", vet.getNombre());
    verify(veterinarioRepository).save(vet);
  }

  @Test
  void noRegistraVeterinarioSinNombre() {
    assertThrows(IllegalArgumentException.class, () -> servicio.registerVeterinario(" "));
    assertThrows(IllegalArgumentException.class, () -> servicio.registerVeterinario(null));

    verify(veterinarioRepository, never()).save(any());
  }

  @Test
  void registraMascotaConIdConsecutivoYSusDatos() {
    when(mascotaRepository.count()).thenReturn(0);

    Mascota mascota = servicio.registerMascota("Firulais", "Perro", 4, tutor);

    assertEquals("M-001", mascota.getId());
    assertEquals("Firulais", mascota.getNombre());
    assertEquals("Perro", mascota.getEspecie());
    assertEquals(4, mascota.getEdad());
    assertSame(tutor, mascota.getTutor());
    var guardada = ArgumentCaptor.forClass(Mascota.class);
    verify(mascotaRepository).save(guardada.capture());
    assertSame(mascota, guardada.getValue());
  }

  @Test
  void noRegistraMascotaConDatosInvalidos() {
    var sinDocumento = Tutor.builder()
        .nombre("Juan").documento("").telefono("300").correo("juan@mail.com").build();

    assertThrows(IllegalArgumentException.class, () -> servicio.registerMascota("", "Perro", 4, tutor));
    assertThrows(IllegalArgumentException.class, () -> servicio.registerMascota("Rex", " ", 4, tutor));
    assertThrows(IllegalArgumentException.class, () -> servicio.registerMascota("Rex", "Perro", -1, tutor));
    assertThrows(IllegalArgumentException.class, () -> servicio.registerMascota("Rex", "Perro", 4, null));
    assertThrows(IllegalArgumentException.class, () -> servicio.registerMascota("Rex", "Perro", 4, sinDocumento));

    verify(mascotaRepository, never()).save(any());
  }

  @Test
  void listaVeterinariosYMascotasDesdeLosRepositorios() {
    var vets = List.of(Veterinario.builder().id("VET-001").nombre("Ana").build());
    var mascotas = List.of(Mascota.builder().id("M-001").nombre("Rex").build());
    when(veterinarioRepository.findAll()).thenReturn(vets);
    when(mascotaRepository.findAll()).thenReturn(mascotas);

    assertSame(vets, servicio.listVeterinarios());
    assertSame(mascotas, servicio.listMascotas());
  }
}
