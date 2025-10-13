package ch.epfl.vlsc.tycho.lsp4j.util;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import se.lth.cs.tycho.ir.IRNode;

public final class AstRangeUtils {
    private AstRangeUtils() {
    }

    public static Range toRange(IRNode node) {
        Range range = new Range();
        if (node == null) {
            range.setStart(new Position(0, 0));
            range.setEnd(new Position(0, 0));
            return range;
        }
        int startLine = Math.max(0, node.getFromLineNumber() - 1);
        int startChar = Math.max(0, node.getFromColumnNumber() - 1);
        int endLine = node.getToLineNumber() <= 0 ? startLine : Math.max(0, node.getToLineNumber() - 1);
        int endChar = node.getToColumnNumber() <= 0 ? startChar + 1 : Math.max(0, node.getToColumnNumber() - 1);
        range.setStart(new Position(startLine, startChar));
        range.setEnd(new Position(endLine, endChar));
        return range;
    }
}
