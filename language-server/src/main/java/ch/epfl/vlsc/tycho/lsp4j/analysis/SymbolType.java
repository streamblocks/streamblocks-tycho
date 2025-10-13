package ch.epfl.vlsc.tycho.lsp4j.analysis;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.SymbolKind;

public enum SymbolType {
    NAMESPACE(SymbolKind.Namespace, CompletionItemKind.Module, true),
    ACTOR(SymbolKind.Class, CompletionItemKind.Class, true),
    NETWORK(SymbolKind.Class, CompletionItemKind.Class, true),
    ACTION(SymbolKind.Function, CompletionItemKind.Function, false),
    VARIABLE(SymbolKind.Variable, CompletionItemKind.Variable, false),
    PORT(SymbolKind.Field, CompletionItemKind.Field, false);

    private final SymbolKind symbolKind;
    private final CompletionItemKind completionKind;
    private final boolean workspaceVisible;

    SymbolType(SymbolKind symbolKind, CompletionItemKind completionKind, boolean workspaceVisible) {
        this.symbolKind = symbolKind;
        this.completionKind = completionKind;
        this.workspaceVisible = workspaceVisible;
    }

    public SymbolKind getSymbolKind() {
        return symbolKind;
    }

    public CompletionItemKind getCompletionKind() {
        return completionKind;
    }

    public boolean isWorkspaceVisible() {
        return workspaceVisible;
    }
}
