package ch.epfl.vlsc.tycho.lsp4j;

import ch.epfl.vlsc.tycho.lsp4j.analysis.CalWorkspace;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CalWorkspaceService implements WorkspaceService {

    private final CalWorkspace workspace;

    public CalWorkspaceService(CalWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> workspace == null
                ? Collections.emptyList()
                : workspace.workspaceSymbols(params.getQuery()));
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // configuration updates are not handled yet
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        if (workspace == null || params == null || params.getChanges() == null) {
            return;
        }
        params.getChanges().forEach(event -> workspace.handleFileEvent(event.getUri(), event.getType()));
    }
}
