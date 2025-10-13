package ch.epfl.vlsc.tycho.lsp4j.util;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public final class TextPositionUtils {
    private TextPositionUtils() {
    }

    public static int offset(String text, Position position) {
        if (text == null || position == null) {
            return -1;
        }
        int line = Math.max(0, position.getLine());
        int character = Math.max(0, position.getCharacter());
        int currentLine = 0;
        int index = 0;
        int length = text.length();
        while (index < length && currentLine < line) {
            char c = text.charAt(index++);
            if (c == '\n') {
                currentLine++;
            }
        }
        if (currentLine != line) {
            return -1;
        }
        int lineStart = index;
        int remaining = length - lineStart;
        int offsetInLine = Math.min(character, remaining);
        return lineStart + offsetInLine;
    }

    public static Range identifierRange(String text, int offset) {
        if (text == null || offset < 0 || offset > text.length()) {
            return null;
        }
        int start = offset;
        int end = offset;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        while (end < text.length() && Character.isJavaIdentifierPart(text.charAt(end))) {
            end++;
        }
        return new Range(positionAt(text, start), positionAt(text, end));
    }

    public static String identifierAt(String text, int offset) {
        Range range = identifierRange(text, offset);
        if (range == null) {
            return "";
        }
        int start = offset(text, range.getStart());
        int end = offset(text, range.getEnd());
        if (start < 0 || end < 0 || end <= start) {
            return "";
        }
        return text.substring(start, end);
    }

    public static String identifierPrefix(String text, int offset) {
        if (text == null || offset < 0 || offset > text.length()) {
            return "";
        }
        int start = offset;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        return text.substring(start, offset);
    }

    public static Position positionAt(String text, int offset) {
        if (text == null) {
            return new Position(0, 0);
        }
        int boundedOffset = Math.max(0, Math.min(offset, text.length()));
        int line = 0;
        int character = 0;
        for (int i = 0; i < boundedOffset; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                line++;
                character = 0;
            } else {
                character++;
            }
        }
        return new Position(line, character);
    }

    public static int offset(String text, Range range) {
        return offset(text, range.getStart());
    }
}
