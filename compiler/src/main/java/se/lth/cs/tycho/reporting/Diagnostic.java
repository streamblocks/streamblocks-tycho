package se.lth.cs.tycho.reporting;

import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Diagnostic {
    private final Kind kind;
    private final String message;
    private final String location;
    private final InputStreamSupplier inputStream;
    private final int fromLine, fromCol, toLine, toCol;

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_COLOR_RESET = "\u001b[0m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public interface InputStreamSupplier {
        InputStream get() throws IOException;
    }

    public Diagnostic(Kind kind, String message, String location, InputStreamSupplier inputStream, int fromLine, int fromCol, int toLine, int toCol) {
        this.kind = kind;
        this.message = message;
        this.location = location;
        this.inputStream = inputStream;
        this.fromLine = fromLine;
        this.fromCol = fromCol;
        this.toLine = toLine;
        this.toCol = toCol;
    }

    public Diagnostic(Kind kind, String message, SourceUnit sourceUnit, IRNode node) {
        this.kind = kind;
        this.message = message;
        if (sourceUnit != null) {
            location = sourceUnit.getLocation();
            inputStream = sourceUnit::getInputStream;
        } else {
            location = null;
            inputStream = null;
        }
        if (node != null) {
            fromLine = node.getFromLineNumber();
            fromCol = node.getFromColumnNumber();
            toLine = node.getToLineNumber();
            toCol = node.getToColumnNumber();
        } else {
            fromLine = 0;
            fromCol = 0;
            toLine = 0;
            toCol = 0;
        }
    }

    public Diagnostic(Kind kind, String message, SourceUnit sourceUnit) {
        this(kind, message, sourceUnit, null);
    }

    public Diagnostic(Kind kind, String message) {
        this(kind, message, null, null);
    }

    public Kind getKind() {
        return kind;
    }

    private boolean hasPosition() {
        return fromLine > 0 && fromCol > 0 && toLine > 0 && toCol > 0;
    }

    public String generateMessage() {
        StringBuilder builder = new StringBuilder();
        if (location != null) {

            builder.append(location);
            if (hasPosition()) {
                builder.append(":");
                builder.append(fromLine);
                builder.append(":");
                builder.append(fromCol);
                builder.append(": ");
            } else {
                builder.append(": ");
            }
        }
        builder.append(kind.getText());
        builder.append(": ");
        builder.append(message);
        builder.append("\n");

        if (hasPosition() && inputStream != null) {
            try (InputStream stream = inputStream.get()) {
                List<String> lines = new BufferedReader(new InputStreamReader(stream)).lines()
                        .skip(fromLine - 1)
                        .limit(toLine - fromLine + 1)
                        .collect(Collectors.toList());
                int l = 0;
                for (String line : lines) {
                    String converted = tabsToSpaces(line);
                    builder.append(converted);
                    builder.append("\n");
                    int w = Math.max(converted.length(), toCol);
                    int start = l == 0 ? fromCol : 1;
                    int end = l == lines.size() - 1 ? toCol : w;
                    char[] marker = createMarker(w, start, end);
                    builder.append(ANSI_CYAN).append(marker).append(ANSI_COLOR_RESET);
                    if (lines.size() > 1)
                        builder.append("\n");
                    l = l + 1;
                }
            } catch (IOException e) {
            }
        }

        return builder.toString();
    }

    private char[] createMarker(int width, int start, int end) {
        char[] marker = new char[width];
        Arrays.fill(marker, 0, start - 1, ' ');
        Arrays.fill(marker, start - 1, end, '^');
        Arrays.fill(marker, end, width, ' ');
        return marker;
    }

    private String tabsToSpaces(String input) {
        StringBuilder output = new StringBuilder();
        int column = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\t') {
                int nextTab = (column & ~0b111) + 2;
                int spaces = nextTab - column;
                while (spaces > 0) {
                    output.append(' ');
                    column++;
                    spaces--;
                }
            } else {
                output.append(c);
                column++;
            }
        }
        return output.toString();
    }

    public enum Kind {
        ERROR(ANSI_RED + "error" + ANSI_COLOR_RESET), WARNING(ANSI_YELLOW + "warning" + ANSI_COLOR_RESET), INFO(ANSI_BLUE + "info" + ANSI_COLOR_RESET);
        String text;

        Kind(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
