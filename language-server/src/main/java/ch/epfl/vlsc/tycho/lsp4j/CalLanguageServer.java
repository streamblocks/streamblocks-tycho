package ch.epfl.vlsc.tycho.lsp4j;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CalLanguageServer implements LanguageServer, LanguageClientAware {

    LanguageClient client = null;
    private TextDocumentService textService;
    private WorkspaceService workspaceService;

    private CalTextDocumentService calTextDocumentService;
    private CalWorkspaceService calWorkspaceService;



    public CalLanguageServer() {

        this.textService = new CalTextDocumentService(this);
        this.workspaceService = new CalWorkspaceService();

    }


    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

        final InitializeResult res = new InitializeResult(new ServerCapabilities());
        //res.getCapabilities().setCodeActionProvider(Boolean.TRUE);
        //res.getCapabilities().setCompletionProvider(new CompletionOptions());
        //res.getCapabilities().setDefinitionProvider(Boolean.TRUE);
        //res.getCapabilities().setHoverProvider(Boolean.TRUE);
        //res.getCapabilities().setReferencesProvider(Boolean.TRUE);
        res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        res.getCapabilities().setDocumentSymbolProvider(Boolean.TRUE);

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
}
