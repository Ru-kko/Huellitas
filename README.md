# Huellitas

Una aplicación de terminal para gestionar una veterinaria llamada **Huellitas**: registro de
veterinarios y mascotas, y agendamiento y seguimiento de citas.

Se maneja con menús interactivos: flechas ↑/↓ para moverse, `Enter` para elegir y `Esc` para cancelar.

## Funcionalidades

| Acción | Qué hace |
|---|---|
| Agendar Cita | Pide fecha y hora, tipo de consulta, mascota y veterinario, y genera la cita con su código y valor. |
| Cancelar Cita | Cancela una cita programada (hasta 2 horas antes de su hora). |
| Atender Cita | Marca una cita programada como atendida. |
| Marcar No Asistió | Marca como no asistida una cita programada cuya hora ya pasó. |
| Registrar Mascota | Registra una mascota (nombre, especie, edad) con los datos de su tutor. |
| Registrar Veterinario | Registra un veterinario. |

### Reglas de negocio

- **Horario:** lunes a sábado, de 8:00 a 16:30, en bloques de 30 minutos (en punto o y media).
- **Anticipación:** la cita debe ser a futuro y a lo sumo 30 días adelante.
- **Disponibilidad:** un veterinario no puede tener dos citas programadas en el mismo bloque, y una
  mascota no puede tener dos citas programadas el mismo día.
- **Tarifas base:** General $50.000, Vacunación $35.000, Control $25.000. Las mascotas de 10 años o
  más tienen un 10% de descuento.
- **Códigos de cita:** `VET-AAAAMMDD-NNN`, por ejemplo `VET-20300107-001`.
- **Especies:** Gato, Perro y Ave.

> Los datos viven en memoria y se pierden al cerrar la aplicación. Al iniciar existen dos
> veterinarios de ejemplo (`VET-01` y `VET-02`).

## Requisitos

- **Java 21** (JDK)
- **Maven 3.9** o superior

Verifica las versiones con `java -version` y `mvn -version`.

## Compilar

Desde la raíz del proyecto:

```
mvn clean package
```

Esto compila todos los módulos, ejecuta los tests y genera un único jar ejecutable en
`infrastructure/bootstrap/target/huellitas.jar`.

Para compilar sin ejecutar los tests:

```
mvn clean package -DskipTests
```

## Probar

```
mvn test
```

## Ejecutar

```
java -jar infrastructure/bootstrap/target/huellitas.jar
```

Ejecútalo desde un terminal real (PowerShell, Windows Terminal, bash, etc.). Los menús usan las
flechas y secuencias ANSI, por lo que pueden no funcionar en la consola integrada de algunos IDEs.
En Windows, si los acentos se ven mal, ejecuta `chcp 65001` antes de iniciar.

## Estructura del proyecto

Arquitectura hexagonal con módulos Maven:

```
domain/models                 Entidades y excepciones del dominio
domain/ports                  Puertos de entrada (casos de uso) y de salida (repositorios, notificador)
application                   Servicios que implementan los casos de uso y las reglas de negocio
infrastructure/out/memory     Repositorios en memoria y notificador por log
infrastructure/bootstrap      Configuración de Spring, interfaz de terminal (JLine) y punto de entrada
```
## Matriz de correspondencia: requisito, diseño y código

Una fila por caso de uso del dossier de diseño. La columna "¿Cambió algo del diseño?" registra y justifica cada diferencia entre el dossier y el código.

Rutas abreviadas: `app` = `application/src/main/java/com/citas/application`, `models` = `domain/models/src/main/java/com/citas/models`, `ports` = `domain/ports/src/main/java/com/citas/ports`, `cli` = `infrastructure/bootstrap/src/main/java/com/citas/bootstrap/cli`.

| Caso de uso | Reglas | Clases y métodos que lo implementan | Pruebas que lo cubren | ¿Cambió algo del diseño? |
|---|---|---|---|---|
| **CU1** Registrar tutor y mascota | (ninguna) | `RegisterUseCase.registerMascota` (puerto), `RegisterService.registerMascota` y `requerido` (`app`), `Mascota`, `Tutor` (`models`), `MascotaRepository.save` y `count`, `InMemoryMascotaRepository`, `App.registrarMascota` (`cli`) | `RegisterServiceTest`: `registraMascotaConIdConsecutivoYSusDatos`, `noRegistraMascotaConDatosInvalidos`. `InMemoryMascotaYVeterinarioRepositoryTest` | **Sí.** `ServicioRegistro` pasó a llamarse `RegisterService` y recibe el `Tutor` ya construido, no sus campos sueltos. No se valida el tope de edad de 30 años, solo que sea 0 o mayor. Se agregó `registerVeterinario` y las listas `listMascotas` y `listVeterinarios` porque el menú tiene la opción "Registrar Veterinario". |
| **CU2** Agendar cita | RN1, RN2, RN3, RN4, RN6, RN7, RN9 | `CitasUseCase.agendar` (puerto), `GeneradorCitasServicio.agendar` (`app/citas`). RN1 y RN4: `ValidadorAgenda.validarHorarioDisponible`. RN2: `validarVeterinarioDisponible`. RN3: `validarMascotaDisponible`. RN6: `PoliticaTarifa.calcular` implementado por `TarifaEstandard` (`app/citas/tarifas`). RN7: `GenerarCodigoCitas.generarCodigoCita`. RN9: `Notifier.notify` con `LogNotifier`. Persistencia: `CitaRepository.save`, `findByVeterinarioAndDate`, `findByPetAndDate`, `countPorFecha`. Menú: `App.agendarCita` | `GeneradorCitasServicioTest`: `agendaLaCitaConTarifaCodigoYEstadoProgramada`, `persisteLaCitaYNotificaDespuesDeValidar`, `fallaSiLaMascotaNoExiste`, `fallaSiElVeterinarioNoExiste`, `fallaSiElHorarioNoEsValidoSinConsultarLaAgenda`, `fallaSiElVeterinarioTieneUnaCitaProgramadaEnElBloque`, `fallaSiLaMascotaTieneUnaCitaProgramadaEseDia`, `citasEnOtrosEstadosNoOcupanLaAgenda`. `ValidadorAgendaTest`. `TarifaEstandardTest`. `GenerarCodigoCitasTest` | **Sí.** (1) Nombres: `ServicioCitas` es `GeneradorCitasServicio`, `GeneradorCodigoCita` es `GenerarCodigoCitas`, `TarifaEstandar` es `TarifaEstandard`, y los repositorios y puertos tienen nombres en inglés (`CitaRepository`, `Notifier`). (2) No existe la interfaz `Reloj`: `ValidadorAgenda` y el servicio usan `LocalDateTime.now()`, así que las pruebas de fechas se calculan relativas a hoy y no con una fecha fija. `validarHorario(fechaHora, ahora)` quedó como `validarHorarioDisponible(fechaHora)`. (3) `Notificador.notificar(Tutor, String)` quedó como `Notifier.notify(String)`: el mensaje no se dirige a un tutor concreto. (4) `PoliticaTarifa` vive en `ports.in` y devuelve `double`, no `long`. (5) El consecutivo del código vuelve a 000 después del 999. (6) La `Cita` se arma con builder y no con el constructor del dossier. |
| **CU3** Consultar agenda del día de un veterinario | (ninguna) | **Pendiente.** Lo más cercano es `CitasUseCase.listarCitas`, que devuelve todas las citas sin filtrar por veterinario ni fecha y sin ordenar. | Ninguna (CA10 no está cubierto) | **Sí: no implementado.** Falta `agendaDelDia(idVeterinario, fecha)` en el puerto y en el servicio, la consulta del repositorio por veterinario y día completo, y la opción en el menú. |
| **CU4** Cancelar cita | RN5, RN9 | `CitasUseCase.cancelar` (puerto), `GeneradorCitasServicio.cancelar`, `Cita.cancelar` y `Cita.esProgramada` (`models`), `CancelacionNoPermitidaException`, `NoEncontradoException`, `Notifier.notify`. Menú: `App.cambiarEstadoCita` con `Action.CANCELAR_CITA` | `GeneradorCitasServicioTest`: `cancelaUnaCitaProgramadaConMasDe2HorasDeAnticipacion`, `noCancelaConMenosDe2HorasDeAnticipacion`, `noCancelaUnaCitaYaPasada`, `soloSePuedeCancelarUnaCitaProgramada`, `noCancelaUnaCitaQueNoExiste` | **Sí.** El estado y las 2 horas se validan en el servicio, no en `Cita`, así que `Cita.cancelar()` cambia el estado sin comprobar nada (menor encapsulamiento que el pedido). Usa `LocalDateTime.now()` en lugar de `Reloj`. El bloque queda libre porque las validaciones de agenda solo cuentan citas PROGRAMADA. |
| **CU5** Atender cita | RN8 | `CitasUseCase.atender` (puerto), `GeneradorCitasServicio.atender`, `Cita.atender(String)` (`models`), `TransicionNoPermitidaException`. Menú: `Action.ATENDER_CITA` | `GeneradorCitasServicioTest`: `atiendeUnaCitaProgramadaGuardandoLasObservaciones`, `soloSePuedeAtenderUnaCitaProgramada`, `noAtiendeUnaCitaQueNoExiste` | **Sí.** No se exige que las observaciones sean no vacías (el dossier pide `IllegalArgumentException`), y el menú siempre envía `""`, o sea que nunca se piden al usuario. La transición se valida en el servicio y no dentro de `Cita`. Además notifica la atención. |
| **CU6** Marcar cita como no asistida | RN8 | `CitasUseCase.marcarNoAsistio` (puerto), `GeneradorCitasServicio.marcarNoAsistio`, `Cita.marcarNoAsistida` (`models`), `EstadoCita.NO_ASISTIDA`, `TransicionNoPermitidaException`. Menú: `Action.MARCAR_NO_ASISTIO` | `GeneradorCitasServicioTest`: `marcaNoAsistidaUnaCitaProgramadaQueYaPaso`, `noMarcaNoAsistidaUnaCitaQueTodaviaNoOcurre`, `soloSePuedeMarcarNoAsistidaUnaCitaProgramada`, `noMarcaNoAsistidaUnaCitaQueNoExiste` | **Sí.** El estado se llama `NO_ASISTIDA` (el dossier dice `NO_ASISTIO`). Se agregó una regla que el dossier no tiene: solo se puede marcar si la hora de la cita ya pasó. La transición se valida en el servicio y no dentro de `Cita`. |

### Cambios que afectan a todo el proyecto

| Tema | Dossier | Código |
|---|---|---|
| Tecnología | Java 17+, sin frameworks, sin Maven | Java 21, Maven multimódulo, Spring (`AppConfig`), Lombok y JLine |
| Estructura | Paquetes `vet.*` y `Main` que ensambla todo | Arquitectura hexagonal en módulos (`domain/models`, `domain/ports`, `application`, `infrastructure/out/memory`, `infrastructure/bootstrap`). El ensamblado está en `AppConfig`. |
| Pruebas | `PruebasAceptacion` con CA1 a CA10 y un `RelojFijo` | Pruebas unitarias con JUnit y Mockito por clase; no existe una clase con los criterios CA1 a CA10 ni un reloj fijo |
| Escenario de cambio | `TarifaJornadaVacunacion` y `NotificadorWhatsApp`, sin tocar `ServicioCitas` | No aplicado. Además, `Notifier.notify(String)` no recibe al tutor, así que un canal de WhatsApp (que necesita el teléfono) obligaría a cambiar el puerto. |
| Menú | 6 opciones (incluye "Ver agenda del día") | 6 opciones, pero cambia una: "Registrar Veterinario" ocupa el lugar de "Ver agenda del día" |

### Correspondencia de los criterios de aceptación con las pruebas existentes

| Criterio | Prueba que lo cubre | Estado |
|---|---|---|
| CA1 | `agendaLaCitaConTarifaCodigoYEstadoProgramada` | Cubierto (con `LocalDateTime.now()`, no con la fecha fija del dossier) |
| CA2 | `TarifaEstandardTest` (`VACUNACION` con 10 años o más da 31500) | Cubierto |
| CA3 | `ValidadorAgendaTest`: `rechazaHorasQueNoSonEnPuntoNiALaMedia`, `rechazaHorasFueraDelHorarioDeAtencion`, `rechazaDomingo` | Cubierto |
| CA4 | `fallaSiElVeterinarioTieneUnaCitaProgramadaEnElBloque` | Cubierto |
| CA5 | `fallaSiLaMascotaTieneUnaCitaProgramadaEseDia` | Cubierto |
| CA6 | `ValidadorAgendaTest`: `rechazaFechasPasadas`, `rechazaFechaConMasDe30DiasDeAnticipacion` | Cubierto |
| CA7 | `cancelaUnaCitaProgramadaConMasDe2HorasDeAnticipacion` y `citasEnOtrosEstadosNoOcupanLaAgenda` | Cubierto |
| CA8 | `noCancelaConMenosDe2HorasDeAnticipacion` | Cubierto |
| CA9 | `soloSePuedeAtenderUnaCitaProgramada` (estado `CANCELADA`) | Cubierto |
| CA10 | (ninguna) | **No cubierto**: depende de CU3 |
