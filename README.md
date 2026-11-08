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
