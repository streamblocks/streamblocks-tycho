package ch.epfl.vlsc.tycho.lsp4j;

import ch.epfl.vlsc.tycho.lsp4j.analysis.CalWorkspace;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class CalLanguageServer implements LanguageServer, LanguageClientAware {

    LanguageClient client = null;
    private final CalWorkspace workspace;
    private final CalTextDocumentService textService;
    private final CalWorkspaceService workspaceService;

    public CalLanguageServer() {
        this.workspace = new CalWorkspace();
        this.textService = new CalTextDocumentService(this, workspace);
        this.workspaceService = new CalWorkspaceService(workspace);
    }


    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

        workspace.configure(params.getRootUri() == null ? null : java.net.URI.create(params.getRootUri()), params.getWorkspaceFolders());

        final ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCompletionProvider(new CompletionOptions(false, new ArrayList<>()));
        capabilities.setDefinitionProvider(Boolean.TRUE);
        capabilities.setDocumentSymbolProvider(Boolean.TRUE);
    capabilities.setHoverProvider(Boolean.TRUE);
    capabilities.setReferencesProvider(Boolean.TRUE);
        capabilities.setWorkspaceSymbolProvider(Boolean.TRUE);

        final InitializeResult res = new InitializeResult(capabilities);

        return CompletableFuture.supplyAsync(() -> res);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.supplyAsync(Object::new);
    }

    @Override
    public void exit() {

    }


    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textService;
    }


    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;

    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    public CalWorkspace getWorkspace() {
        return workspace;
    }

    public LanguageClient getClient() {
        return client;
    }
}
