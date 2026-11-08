package com.citas.bootstrap;

import com.citas.application.RegisterService;
import com.citas.application.citas.GeneradorCitasServicio;
import com.citas.application.citas.GenerarCodigoCitas;
import com.citas.application.citas.ValidadorAgenda;
import com.citas.application.citas.tarifas.TarifaEstandard;
import com.citas.ports.in.CitasUseCase;
import com.citas.ports.in.PoliticaTarifa;
import com.citas.ports.in.RegisterUseCase;
import com.citas.ports.out.Notifier;
import com.citas.ports.out.persistence.CitaRepository;
import com.citas.ports.out.persistence.MascotaRepository;
import com.citas.ports.out.persistence.VeterinarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.citas.bootstrap", "com.citas.infrastructure"})
public class AppConfig {

  @Bean
  public ValidadorAgenda validadorAgenda() {
    return new ValidadorAgenda();
  }

  @Bean
  public PoliticaTarifa politicaTarifa() {
    return new TarifaEstandard();
  }

  @Bean
  public GenerarCodigoCitas generarCodigoCitas(CitaRepository citaRepository) {
    return new GenerarCodigoCitas(citaRepository);
  }

  @Bean
  public CitasUseCase citasUseCase(
      MascotaRepository mascotaRepository,
      VeterinarioRepository veterinarioRepository,
      CitaRepository citaRepository,
      ValidadorAgenda validadorAgenda,
      PoliticaTarifa politicaTarifa,
      GenerarCodigoCitas generarCodigoCitas,
      Notifier notifier) {
    return new GeneradorCitasServicio(
        mascotaRepository,
        veterinarioRepository,
        citaRepository,
        validadorAgenda,
        politicaTarifa,
        generarCodigoCitas,
        notifier);
  }

  @Bean
  public RegisterUseCase registerUseCase(
      MascotaRepository mascotaRepository, VeterinarioRepository veterinarioRepository) {
    return new RegisterService(mascotaRepository, veterinarioRepository);
  }
}
