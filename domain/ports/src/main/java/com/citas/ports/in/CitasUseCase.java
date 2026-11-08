package com.citas.ports.in;

import com.citas.models.Cita;
import com.citas.models.TipoConsulta;
import java.time.LocalDateTime;
import java.util.List;

public interface CitasUseCase {
  Cita agendar(String idMascota, String idVeterinario, LocalDateTime fechaHora, TipoConsulta tipo);
  void cancelar(String codigoCita);
  void atender(String codigo, String observaciones);
  void marcarNoAsistio(String codigo);
  List<Cita> listarCitas();
}
