package com.citas.bootstrap.cli;

import com.citas.models.Cita;
import com.citas.models.Mascota;
import com.citas.models.TipoConsulta;
import com.citas.models.Tutor;
import com.citas.models.Veterinario;
import com.citas.models.errors.AplicacionCitaException;
import com.citas.ports.in.CitasUseCase;
import com.citas.ports.in.RegisterUseCase;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.stereotype.Controller;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

@Controller
public class App {
  private static final int VISIBLE_ROWS = 5;
  private static final List<String> ESPECIES = List.of("Gato", "Perro", "Ave");
  private static final DateTimeFormatter FORMATO_FECHA =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final Terminal terminal;
  private final CitasUseCase citeCase;
  private final RegisterUseCase registerUseCase;

  public App(CitasUseCase citeCase, RegisterUseCase registerUseCase) throws Exception {
    this.terminal = TerminalBuilder.terminal();
    this.citeCase = citeCase;
    this.registerUseCase = registerUseCase;
  }

  public void run() {
    PrintWriter writer = this.terminal.writer();
    writer.println("Bienvenido a Huellitas!");

    Action action;
    do {
      action = startMenu();

      switch (action) {
        case AGENDAR_CITA -> agendarCita();
        case CANCELAR_CITA -> cambiarEstadoCita("Cancelar Cita", "Cita cancelada", citeCase::cancelar);
        case ATENDER_CITA -> cambiarEstadoCita(
            "Atender Cita", "Cita atendida", codigo -> citeCase.atender(codigo, ""));
        case MARCAR_NO_ASISTIO -> cambiarEstadoCita(
            "Marcar No Asistió", "Cita marcada como no asistida", citeCase::marcarNoAsistio);
        case REGISTRAR_VETERINARIO -> registrarVeterinario();
        case REGISTRAR_MASCOTA -> registrarMascota();
        case SALIR -> writer.println("Hasta pronto!");
        default -> {
          writer.println(Colors.RED + "Acción aún no implementada" + Colors.RESET);
          writer.flush();
        }
      }
    } while (action != Action.SALIR);

    writer.flush();
  }

  private void agendarCita() {
    PrintWriter writer = this.terminal.writer();
    LineReader lineReader = LineReaderBuilder.builder().terminal(this.terminal).build();

    writer.print(Colors.RESET);
    writer.println();
    writer.println("== Agendar Cita ==");
    writer.flush();

    List<Mascota> mascotas = registerUseCase.listMascotas();
    if (mascotas.isEmpty()) {
      writer.println(Colors.RED + "No hay mascotas registradas. Registre una primero." + Colors.RESET);
      writer.println();
      writer.flush();
      return;
    }
    List<Veterinario> veterinarios = registerUseCase.listVeterinarios();
    if (veterinarios.isEmpty()) {
      writer.println(Colors.RED + "No hay veterinarios registrados. Registre uno primero." + Colors.RESET);
      writer.println();
      writer.flush();
      return;
    }

    try {
      LocalDateTime fechaHora = leerFechaHora(lineReader, "Fecha y hora (yyyy-MM-dd HH:mm): ");

      TipoConsulta tipo = seleccionar("Tipo de consulta:", List.of(TipoConsulta.values()), false,
          t -> t.name() + " (" + formatearPrecio(t.getTarifaBase()) + ")");
      if (tipo == null) {
        throw new EndOfFileException();
      }
      writer.println("Tipo: " + tipo.name() + " (" + formatearPrecio(tipo.getTarifaBase()) + ")");

      Mascota mascota = seleccionar("Seleccione mascota:", mascotas, true,
          m -> m.getId() + " - " + m.getNombre() + " - de " + m.getTutor().getNombre());
      if (mascota == null) {
        throw new EndOfFileException();
      }
      writer.println("Mascota: " + mascota.getId() + " - " + mascota.getNombre()
          + " - de " + mascota.getTutor().getNombre());

      Veterinario veterinario = seleccionar("Seleccione veterinario:", veterinarios, true,
          v -> v.getId() + " - " + v.getNombre());
      if (veterinario == null) {
        throw new EndOfFileException();
      }
      writer.println("Veterinario: " + veterinario.getId() + " - " + veterinario.getNombre());

      Cita cita = citeCase.agendar(mascota.getId(), veterinario.getId(), fechaHora, tipo);
      writer.println(Colors.GREEN + "Cita agendada: " + cita.getCodigo()
          + " - " + cita.getFechaHora().format(FORMATO_FECHA)
          + " - " + formatearPrecio((long) cita.getValor()) + Colors.RESET);
    } catch (UserInterruptException | EndOfFileException e) {
      writer.println(Colors.YELLOW + "Agendamiento cancelado" + Colors.RESET);
    } catch (IllegalArgumentException | AplicacionCitaException e) {
      writer.println(Colors.RED + e.getMessage() + Colors.RESET);
    }

    writer.println();
    writer.flush();
  }

  private void cambiarEstadoCita(String titulo, String mensajeOk, Consumer<String> accion) {
    PrintWriter writer = this.terminal.writer();

    writer.print(Colors.RESET);
    writer.println();
    writer.println("== " + titulo + " ==");
    writer.flush();

    List<Cita> citas = citeCase.listarCitas();
    if (citas.isEmpty()) {
      writer.println(Colors.RED + "No hay citas registradas." + Colors.RESET);
      writer.println();
      writer.flush();
      return;
    }

    try {
      Cita cita = seleccionar("Seleccione cita:", citas, true, this::describirCita);
      if (cita == null) {
        throw new EndOfFileException();
      }
      writer.println("Cita: " + describirCita(cita));

      accion.accept(cita.getCodigo());
      writer.println(Colors.GREEN + mensajeOk + ": " + cita.getCodigo() + Colors.RESET);
    } catch (UserInterruptException | EndOfFileException e) {
      writer.println(Colors.YELLOW + "Operación cancelada" + Colors.RESET);
    } catch (IllegalArgumentException | AplicacionCitaException e) {
      writer.println(Colors.RED + e.getMessage() + Colors.RESET);
    }

    writer.println();
    writer.flush();
  }

  private String describirCita(Cita cita) {
    return cita.getCodigo()
        + " - " + cita.getFechaHora().format(FORMATO_FECHA)
        + " - " + cita.getMascota().getNombre()
        + " - " + cita.getVeterinario().getNombre()
        + " - " + cita.getEstado();
  }

  private LocalDateTime leerFechaHora(LineReader lineReader, String prompt) {
    while (true) {
      String valor = leerObligatorio(lineReader, prompt);
      try {
        return LocalDateTime.parse(valor, FORMATO_FECHA);
      } catch (DateTimeParseException e) {
        this.terminal.writer().println(
            Colors.RED + "Formato inválido, use yyyy-MM-dd HH:mm (ej. 2026-11-09 08:30)" + Colors.RESET);
        this.terminal.writer().flush();
      }
    }
  }

  private static String formatearPrecio(long valor) {
    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
    return formatter.format(valor);
  }

  private void registrarMascota() {
    PrintWriter writer = this.terminal.writer();
    LineReader lineReader = LineReaderBuilder.builder().terminal(this.terminal).build();

    writer.print(Colors.RESET);
    writer.println();
    writer.println("== Registrar Mascota ==");
    writer.flush();


    try {
      String nombre = leerObligatorio(lineReader, "Nombre de la mascota: ");

      String especie = seleccionar("Especie:", ESPECIES, false, e -> e);
      if (especie == null) {
        throw new EndOfFileException();
      }
      writer.println("Especie: " + especie);

      int edad = leerEntero(lineReader, "Edad: ");


      writer.println();
      writer.println("== Datos del Tutor ==");
      writer.flush();
      Tutor tutor = Tutor.builder()
          .nombre(leerObligatorio(lineReader, "Nombre: "))
          .documento(leerObligatorio(lineReader, "Documento: "))
          .telefono(leerObligatorio(lineReader, "Telefono: "))
          .correo(leerObligatorio(lineReader, "Email: "))
          .build();

      Mascota mascota = registerUseCase.registerMascota(nombre, especie, edad, tutor);
      writer.println(Colors.GREEN + "Mascota registrada: " + mascota.getNombre()
          + " (id: " + mascota.getId() + ")" + Colors.RESET);
    } catch (UserInterruptException | EndOfFileException e) {
      writer.println(Colors.YELLOW + "Registro cancelado" + Colors.RESET);
    } catch (IllegalArgumentException | AplicacionCitaException e) {
      writer.println(Colors.RED + e.getMessage() + Colors.RESET);
    }

    writer.println();
    writer.flush();
  }

  private <T> T seleccionar(
      String titulo, List<T> opciones, boolean scrollable, Function<T, String> descripcion) {
    AtomicReference<T> elegido = new AtomicReference<>();
    CliSelect.<T>builder()
        .title(titulo)
        .options(opciones)
        .scrollable(scrollable)
        .maxRows(VISIBLE_ROWS)
        .parse(descripcion)
        .onSelect(elegido::set)
        .build()
        .render(this.terminal);
    return elegido.get();
  }

  private String leerObligatorio(LineReader lineReader, String prompt) {
    while (true) {
      String valor = lineReader.readLine(prompt).trim();
      if (!valor.isEmpty()) {
        return valor;
      }
      this.terminal.writer().println(Colors.RED + "Este campo es obligatorio" + Colors.RESET);
      this.terminal.writer().flush();
    }
  }

  private int leerEntero(LineReader lineReader, String prompt) {
    while (true) {
      String valor = leerObligatorio(lineReader, prompt);
      try {
        int numero = Integer.parseInt(valor);
        if (numero >= 0) {
          return numero;
        }
      } catch (NumberFormatException ignored) {
      }
      this.terminal.writer().println(Colors.RED + "Ingrese un número entero mayor o igual a 0" + Colors.RESET);
      this.terminal.writer().flush();
    }
  }

  private void registrarVeterinario() {
    PrintWriter writer = this.terminal.writer();
    LineReader lineReader = LineReaderBuilder.builder().terminal(this.terminal).build();

    writer.print(Colors.RESET);
    writer.println();
    writer.println("== Registrar Veterinario ==");
    writer.flush();

    try {
      String nombre = lineReader.readLine("Nombre: ");
      Veterinario veterinario = registerUseCase.registerVeterinario(nombre);
      writer.println(Colors.GREEN + "Veterinario registrado: " + veterinario.getNombre()
          + " (id: " + veterinario.getId() + ")" + Colors.RESET);
    } catch (UserInterruptException | EndOfFileException e) {
      writer.println(Colors.YELLOW + "Registro cancelado" + Colors.RESET);
    } catch (IllegalArgumentException e) {
      writer.println(Colors.RED + e.getMessage() + Colors.RESET);
    }

    writer.println();
    writer.flush();
  }

  private Action startMenu() {
    PrintWriter writer = this.terminal.writer();
    AtomicReference<Action> seleccionada = new AtomicReference<>();

    writer.print(Colors.ORANGE);
    writer.println("Que desea hacer?");
    writer.println(Colors.RESET);
    writer.flush();

    CliSelect.<Action>builder()
        .options(List.of(Action.values()))
        .parse(Action::getDescription)
        .onSelect(seleccionada::set)
        .build()
        .render(this.terminal);

    // Esc o fin de entrada en el menú principal equivale a salir
    return seleccionada.get() != null ? seleccionada.get() : Action.SALIR;
  }
}
