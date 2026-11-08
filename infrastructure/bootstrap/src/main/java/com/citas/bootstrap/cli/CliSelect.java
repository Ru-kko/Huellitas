package com.citas.bootstrap.cli;

import lombok.Builder;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Builder
public final class CliSelect<T> {
  private final List<T> options;

  @Builder.Default
  private final boolean scrollable = false;

  @Builder.Default
  private final int maxRows = 5;

  private final Consumer<T> onSelect;

  private final Function<T, String> parse;

  /** Línea opcional sobre las opciones. Se borra junto con ellas al terminar. */
  private final String title;

  public void render(Terminal terminal) {
    Objects.requireNonNull(terminal, "terminal");
    if (options.isEmpty()) {
      return;
    }

    PrintWriter writer = terminal.writer();
    BindingReader reader = new BindingReader(terminal.reader());

    KeyMap<String> keyMap = new KeyMap<>();
    keyMap.bind("UP", KeyMap.key(terminal, InfoCmp.Capability.key_up));
    keyMap.bind("DOWN", KeyMap.key(terminal, InfoCmp.Capability.key_down));
    keyMap.bind("ENTER", "\r");
    keyMap.bind("ENTER", "\n");
    keyMap.bind("CANCEL", KeyMap.esc());

    int total = options.size();
    int rows = scrollable ? Math.min(Math.max(1, maxRows), total) : total;
    boolean showPosition = rows < total;
    int selectedIndex = 0;
    int offset = 0;
    int printedLines = 0;

    T chosen = null;
    Attributes previous = terminal.enterRawMode();
    try {
      while (true) {
        if (printedLines > 0) {
          writer.print("\u001B[" + printedLines + "A\r\u001B[J");
        }

        printedLines = 0;
        if (title != null) {
          writer.println(Colors.ORANGE + title + Colors.RESET);
          printedLines++;
        }
        for (int i = offset; i < offset + rows; i++) {
          String label = describe(options.get(i));
          writer.println(i == selectedIndex
              ? Colors.GREEN + " > " + label + Colors.RESET
              : Colors.YELLOW + " - " + label + Colors.RESET);
        }
        printedLines += rows;
        if (showPosition) {
          writer.println(Colors.RESET + "   (" + (selectedIndex + 1) + "/" + total + ")");
          printedLines++;
        }
        writer.flush();

        String key = reader.readBinding(keyMap);
        if (key == null || key.equals("CANCEL")) {
          break;
        }
        if (key.equals("ENTER")) {
          chosen = options.get(selectedIndex);
          break;
        }

        if (key.equals("UP")) {
          selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (key.equals("DOWN")) {
          selectedIndex = Math.min(total - 1, selectedIndex + 1);
        }

        if (selectedIndex < offset) {
          offset = selectedIndex;
        } else if (selectedIndex >= offset + rows) {
          offset = selectedIndex - rows + 1;
        }
      }
    } finally {
      // Borrar el select de la pantalla para que el llamador deje solo el resultado
      if (printedLines > 0) {
        writer.print("\u001B[" + printedLines + "A\r\u001B[J");
        writer.flush();
      }
      terminal.setAttributes(previous);
    }

    if (chosen != null && onSelect != null) {
      onSelect.accept(chosen);
    }
  }

  private String describe(T option) {
    return parse != null ? parse.apply(option) : String.valueOf(option);
  }
}
