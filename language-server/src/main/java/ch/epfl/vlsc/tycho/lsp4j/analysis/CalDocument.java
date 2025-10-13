package ch.epfl.vlsc.tycho.lsp4j.analysis;

import ch.epfl.vlsc.tycho.lsp4j.symbols.SymbolFinding;
import ch.epfl.vlsc.tycho.lsp4j.util.TextPositionUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;
import se.lth.cs.tycho.parsing.cal.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CalDocument {
    private final String uri;
    private final Path path;
    private volatile boolean isOpen;
    private volatile String text;
    private volatile NamespaceDecl namespaceDecl;
    private volatile List<Diagnostic> diagnostics = Collections.emptyList();
    private volatile List<Either<SymbolInformation, DocumentSymbol>> documentSymbols = Collections.emptyList();
    private volatile Map<String, List<SymbolDefinition>> definitionsByName = Collections.emptyMap();
    private volatile List<SymbolDefinition> workspaceVisibleDefinitions = Collections.emptyList();

    public CalDocument(String uri, Path path) {
        this.uri = uri;
        this.path = path;
        this.isOpen = false;
    }

    public String getUri() {
        return uri;
    }

    public Path getPath() {
        return path;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public Optional<NamespaceDecl> getNamespaceDecl() {
        return Optional.ofNullable(namespaceDecl);
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public List<Either<SymbolInformation, DocumentSymbol>> getDocumentSymbols() {
        return documentSymbols;
    }

    public Map<String, List<SymbolDefinition>> getDefinitionsByName() {
        return definitionsByName;
    }

    public List<SymbolDefinition> getWorkspaceVisibleDefinitions() {
        return workspaceVisibleDefinitions;
    }

    public Optional<String> getText() {
        return Optional.ofNullable(text);
    }

    public List<Location> findOccurrences(String identifier) {
        if (identifier == null || identifier.isEmpty() || text == null) {
            return Collections.emptyList();
        }
        List<Location> occurrences = new ArrayList<>();
        int index = 0;
        while (index <= text.length() - identifier.length()) {
            int match = text.indexOf(identifier, index);
            if (match < 0) {
                break;
            }
            if (isIdentifierBoundary(match, identifier.length())) {
                Range range = new Range(
                        TextPositionUtils.positionAt(text, match),
                        TextPositionUtils.positionAt(text, match + identifier.length()));
                occurrences.add(new Location(uri, range));
            }
            index = match + Math.max(1, identifier.length());
        }
        return occurrences;
    }

    public void open(String content) {
        isOpen = true;
        setText(content);
    }

    public void setText(String content) {
        text = content;
        reparse();
    }

    public void close() {
        isOpen = false;
        if (Files.isReadable(path)) {
            try {
                reloadFromDisk();
            } catch (IOException e) {
                // keep last known state
            }
        }
    }

    public void reloadFromDisk() throws IOException {
        if (path == null) {
            return;
        }
        byte[] bytes = Files.readAllBytes(path);
        text = new String(bytes, StandardCharsets.UTF_8);
        reparse();
    }

    private void reparse() {
        if (text == null) {
            diagnostics = Collections.emptyList();
            namespaceDecl = null;
            documentSymbols = Collections.emptyList();
            definitionsByName = Collections.emptyMap();
            workspaceVisibleDefinitions = Collections.emptyList();
            return;
        }
        List<Diagnostic> newDiagnostics = new ArrayList<>();
        NamespaceDecl parsedNamespace = null;
        try {
            Reader reader = new BufferedReader(new StringReader(text));
            CalParser parser = new CalParser(reader);
            parsedNamespace = parser.CompilationUnit();
        } catch (ParseException e) {
            newDiagnostics.add(toDiagnostic(e));
        }
        namespaceDecl = parsedNamespace;
        diagnostics = Collections.unmodifiableList(newDiagnostics);
        if (parsedNamespace != null) {
            SymbolFinding symbolFinding = new SymbolFinding(uri);
            documentSymbols = Collections.unmodifiableList(symbolFinding.visit(parsedNamespace));
            DefinitionCollector collector = new DefinitionCollector(uri, text);
            collector.collect(parsedNamespace);
            definitionsByName = Collections.unmodifiableMap(new HashMap<>(collector.getDefinitionsByName()));
            workspaceVisibleDefinitions = Collections.unmodifiableList(new ArrayList<>(collector.getWorkspaceVisibleDefinitions()));
        } else {
            documentSymbols = Collections.emptyList();
            definitionsByName = Collections.emptyMap();
            workspaceVisibleDefinitions = Collections.emptyList();
        }
    }

    private Diagnostic toDiagnostic(ParseException exception) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setMessage(exception.getMessage());
        Token token = exception.currentToken;
        if (token != null) {
            Token reportToken = token;
            ch.epfl.vlsc.tycho.lsp4j.CalTokenId tokenId = ch.epfl.vlsc.tycho.lsp4j.CalTokenId.getById(token.kind);
            if (tokenId != ch.epfl.vlsc.tycho.lsp4j.CalTokenId.ID && token.next != null) {
                reportToken = token.next;
            }
            Range range = new Range();
            range.setStart(new org.eclipse.lsp4j.Position(Math.max(0, reportToken.beginLine - 1), Math.max(0, reportToken.beginColumn - 1)));
            range.setEnd(new org.eclipse.lsp4j.Position(Math.max(0, reportToken.endLine - 1), Math.max(0, reportToken.endColumn - 1)));
            diagnostic.setRange(range);
        }
        return diagnostic;
    }

    private boolean isIdentifierBoundary(int start, int length) {
        if (text == null) {
            return false;
        }
        if (start > 0) {
            char previous = text.charAt(start - 1);
            if (Character.isJavaIdentifierPart(previous)) {
                return false;
            }
        }
        int end = start + length;
        if (end < text.length()) {
            char next = text.charAt(end);
            if (Character.isJavaIdentifierPart(next)) {
                return false;
            }
        }
        return true;
    }

    public static CalDocument fromUri(String uriString) {
        try {
            URI uri = URI.create(uriString);
            Path path = uri.getScheme() == null ? null : Path.of(uri);
            CalDocument document = new CalDocument(uriString, path);
            if (path != null && Files.isReadable(path)) {
                document.reloadFromDisk();
            }
            return document;
        } catch (Exception e) {
            return new CalDocument(uriString, null);
        }
    }
}
