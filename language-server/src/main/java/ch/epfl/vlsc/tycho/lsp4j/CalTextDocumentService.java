package ch.epfl.vlsc.tycho.lsp4j;

import ch.epfl.vlsc.tycho.lsp4j.analysis.CalWorkspace;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CalTextDocumentService implements TextDocumentService {

    private final CalLanguageServer languageServer;
    private final CalWorkspace workspace;

    public CalTextDocumentService(CalLanguageServer languageServer, CalWorkspace workspace) {
        this.languageServer = languageServer;
        this.workspace = workspace;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> items = workspace.completions(asTextDocumentPosition(params));
            return Either.forLeft(items);
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends org.eclipse.lsp4j.LocationLink>>> definition(TextDocumentPositionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<Location> definitions = workspace.definitionLocations(params);
            return Either.forLeft(definitions);
        });
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> workspace.documentSymbols(params.getTextDocument().getUri()));
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams params) {
        return CompletableFuture.supplyAsync(() -> workspace.hover(params).orElse(null));
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        boolean includeDeclaration = params.getContext() != null && params.getContext().isIncludeDeclaration();
        return CompletableFuture.supplyAsync(() -> workspace.references(asTextDocumentPosition(params), includeDeclaration));
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        workspace.openDocument(params.getTextDocument().getUri(), params.getTextDocument().getText());
        publishDiagnostics(params.getTextDocument().getUri());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        if (params.getContentChanges().isEmpty()) {
            return;
        }
        TextDocumentContentChangeEvent changeEvent = params.getContentChanges().get(params.getContentChanges().size() - 1);
        if (changeEvent.getRange() != null || changeEvent.getRangeLength() != null) {
            throw new UnsupportedOperationException("Incremental document updates are not supported yet.");
        }
        workspace.updateDocument(params.getTextDocument().getUri(), params.getContentChanges());
        publishDiagnostics(params.getTextDocument().getUri());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        workspace.closeDocument(params.getTextDocument().getUri());
        publishDiagnostics(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // Rely on didChange to keep the in-memory representation up to date.
    }

    private void publishDiagnostics(String uri) {
        List<Diagnostic> diagnostics = workspace.diagnostics(uri);
        if (languageServer.getClient() != null) {
            languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
        }
    }

    private TextDocumentPositionParams asTextDocumentPosition(CompletionParams params) {
        TextDocumentIdentifier identifier = params.getTextDocument();
        TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
        positionParams.setTextDocument(identifier);
        positionParams.setPosition(params.getPosition());
        return positionParams;
    }

    private TextDocumentPositionParams asTextDocumentPosition(ReferenceParams params) {
        TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
        positionParams.setTextDocument(params.getTextDocument());
        positionParams.setPosition(params.getPosition());
        return positionParams;
    }
}
