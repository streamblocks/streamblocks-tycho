package ch.epfl.vlsc.tycho.lsp4j;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;
import se.lth.cs.tycho.parsing.cal.Token;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CalTextDocumentService implements TextDocumentService {


    private HashMap<String, TextDocumentItem> documents = new HashMap<>();

    private CalLanguageServer calLanguageServer;


    public CalTextDocumentService(CalLanguageServer calLanguageServer) {
        this.calLanguageServer = calLanguageServer;
        Set<se.lth.cs.tycho.reporting.Diagnostic.Kind> kinds = new HashSet<>();
        kinds.add(se.lth.cs.tycho.reporting.Diagnostic.Kind.ERROR);
        kinds.add(se.lth.cs.tycho.reporting.Diagnostic.Kind.WARNING);
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {



        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        documents.put(params.getTextDocument().getUri(), params.getTextDocument());

        CompletableFuture.runAsync(() ->
                calLanguageServer.client.publishDiagnostics(
                        new PublishDiagnosticsParams(params.getTextDocument().getUri(), validate(params.getTextDocument().getText()))
                )
        );

    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {

        String uri = params.getTextDocument().getUri();
        for (TextDocumentContentChangeEvent changeEvent : params.getContentChanges()) {
            // Will be full update because we specified that is all we support
            if (changeEvent.getRange() != null) {
                throw new UnsupportedOperationException("Range should be null for full document update.");
            }
            if (changeEvent.getRangeLength() != null) {
                throw new UnsupportedOperationException("RangeLength should be null for full document update.");
            }

            documents.get(uri).setText(changeEvent.getText());

            CompletableFuture.runAsync(() ->
                    calLanguageServer.client.publishDiagnostics(
                            new PublishDiagnosticsParams(params.getTextDocument().getUri(), validate(changeEvent.getText()))
                    )
            );
        }

    }


    private List<Diagnostic> validate(String text) {
        List<Diagnostic> res = new ArrayList<>();

        try {
            StringReader sr = new StringReader(text);
            Reader r = new BufferedReader(sr);

            CalParser parser = new CalParser(r);
            NamespaceDecl ns = parser.CompilationUnit();
            System.out.println(ns.getQID());

        } catch (ParseException e) {
            Diagnostic d = new Diagnostic(); // = reporter.report(toDiagnostic(p, e));d.setSeverity(DiagnosticSeverity.Error);
            d.setMessage(e.getMessage());

            Token t = e.currentToken;

            // -- If kind is a Cal constant get next
            CalTokenId token = CalTokenId.getById(t.kind);
            if (token != CalTokenId.ID) {
                t = t.next;
            }

            final Range range = new Range();
            range.setStart(new Position(t.beginLine - 1, t.beginColumn - 1));
            range.setEnd(new Position(t.endLine - 1, t.beginColumn - 1 + t.image.length()));
            d.setRange(range);

            res.add(d);

        }

        return res;
    }


    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documents.remove(uri);

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }
}
