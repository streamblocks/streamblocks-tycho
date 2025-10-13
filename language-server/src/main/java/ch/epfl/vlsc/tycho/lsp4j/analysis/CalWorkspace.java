package ch.epfl.vlsc.tycho.lsp4j.analysis;

import ch.epfl.vlsc.tycho.lsp4j.util.TextPositionUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CalWorkspace {
    private static final List<String> KEYWORDS = List.of(
            "actor", "action", "begin", "const", "end", "import", "namespace", "network", "var", "procedure", "schedule"
    );

    private final Map<String, CalDocument> documentsByUri = new ConcurrentHashMap<>();
    private final Map<Path, CalDocument> documentsByPath = new ConcurrentHashMap<>();
    private final Map<CalDocument, List<SymbolDefinition>> globalSymbolsByDocument = new HashMap<>();
    private final Map<String, List<SymbolDefinition>> globalIndex = new HashMap<>();
    private final Set<Path> workspaceFolders = new HashSet<>();

    public CalWorkspace() {
    }

    public void configure(URI rootUri, List<WorkspaceFolder> folders) {
        workspaceFolders.clear();
        if (folders != null && !folders.isEmpty()) {
            for (WorkspaceFolder folder : folders) {
                Path path = uriToPath(folder.getUri());
                if (path != null) {
                    workspaceFolders.add(path);
                }
            }
        } else if (rootUri != null) {
            Path path = uriToPath(rootUri.toString());
            if (path != null) {
                workspaceFolders.add(path);
            }
        }
        if (workspaceFolders.isEmpty()) {
            workspaceFolders.add(Paths.get("."));
        }
        indexWorkspace();
    }

    public Optional<CalDocument> getDocument(String uri) {
        return Optional.ofNullable(documentsByUri.get(uri));
    }

    public CalDocument openDocument(String uri, String content) {
        CalDocument document = documentsByUri.computeIfAbsent(uri, this::createDocument);
        if (document.getPath() != null) {
            documentsByPath.put(document.getPath(), document);
        }
        synchronized (document) {
            document.open(content);
            refreshGlobalIndex(document);
        }
        return document;
    }

    public void updateDocument(String uri, List<TextDocumentContentChangeEvent> changes) {
        CalDocument document = documentsByUri.get(uri);
        if (document == null || changes.isEmpty()) {
            return;
        }
        TextDocumentContentChangeEvent changeEvent = changes.get(changes.size() - 1);
        synchronized (document) {
            document.setText(changeEvent.getText());
            refreshGlobalIndex(document);
        }
    }

    public void closeDocument(String uri) {
        CalDocument document = documentsByUri.get(uri);
        if (document == null) {
            return;
        }
        synchronized (document) {
            document.close();
            refreshGlobalIndex(document);
        }
    }

    public List<Diagnostic> diagnostics(String uri) {
        return getDocument(uri)
                .map(CalDocument::getDiagnostics)
                .orElse(Collections.emptyList());
    }

    public List<Either<SymbolInformation, DocumentSymbol>> documentSymbols(String uri) {
        return getDocument(uri)
                .map(CalDocument::getDocumentSymbols)
                .orElse(Collections.emptyList());
    }

    public List<Location> definitionLocations(TextDocumentPositionParams params) {
        CalDocument document = documentsByUri.get(params.getTextDocument().getUri());
        if (document == null) {
            return Collections.emptyList();
        }
        Optional<String> textOpt = document.getText();
        if (textOpt.isEmpty()) {
            return Collections.emptyList();
        }
        int offset = TextPositionUtils.offset(textOpt.get(), params.getPosition());
        if (offset < 0) {
            return Collections.emptyList();
        }
        String identifier = TextPositionUtils.identifierAt(textOpt.get(), offset);
        if (identifier.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<SymbolDefinition>> definitions = document.getDefinitionsByName();
        List<Location> locations = new ArrayList<>();
        if (definitions.containsKey(identifier)) {
            for (SymbolDefinition definition : definitions.get(identifier)) {
                locations.add(definition.getLocation());
            }
        }
        if (locations.isEmpty()) {
            List<SymbolDefinition> globalDefs = getGlobalDefinitions(identifier);
            for (SymbolDefinition definition : globalDefs) {
                locations.add(definition.getLocation());
            }
        }
        return locations;
    }

    public Optional<Hover> hover(TextDocumentPositionParams params) {
        CalDocument document = documentsByUri.get(params.getTextDocument().getUri());
        if (document == null) {
            return Optional.empty();
        }
        Optional<String> textOpt = document.getText();
        if (textOpt.isEmpty()) {
            return Optional.empty();
        }
        int offset = TextPositionUtils.offset(textOpt.get(), params.getPosition());
        if (offset < 0) {
            return Optional.empty();
        }
        String identifier = TextPositionUtils.identifierAt(textOpt.get(), offset);
        if (identifier.isEmpty()) {
            return Optional.empty();
        }
        List<SymbolDefinition> definitions = new ArrayList<>();
        Map<String, List<SymbolDefinition>> localDefs = document.getDefinitionsByName();
        if (localDefs.containsKey(identifier)) {
            definitions.addAll(localDefs.get(identifier));
        } else {
            definitions.addAll(getGlobalDefinitions(identifier));
        }
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        SymbolDefinition symbol = definitions.get(0);
        MarkupContent content = new MarkupContent();
        content.setKind("markdown");
        content.setValue(buildHoverMarkdown(symbol));
        Hover hover = new Hover();
        hover.setContents(content);
        hover.setRange(symbol.getSelectionRange());
        return Optional.of(hover);
    }

    public List<Location> references(TextDocumentPositionParams params, boolean includeDeclaration) {
        CalDocument document = documentsByUri.get(params.getTextDocument().getUri());
        if (document == null) {
            return Collections.emptyList();
        }
        Optional<String> textOpt = document.getText();
        if (textOpt.isEmpty()) {
            return Collections.emptyList();
        }
        int offset = TextPositionUtils.offset(textOpt.get(), params.getPosition());
        if (offset < 0) {
            return Collections.emptyList();
        }
        String identifier = TextPositionUtils.identifierAt(textOpt.get(), offset);
        if (identifier.isEmpty()) {
            return Collections.emptyList();
        }
        List<Location> results = new ArrayList<>();
        for (CalDocument doc : allDocuments()) {
            List<Location> occurrences = new ArrayList<>(doc.findOccurrences(identifier));
            if (!includeDeclaration) {
                List<Range> definitionRanges = getDefinitionRanges(doc, identifier);
                occurrences.removeIf(loc -> isDefinitionRange(loc.getRange(), definitionRanges));
            }
            results.addAll(occurrences);
        }
        return results;
    }

    public List<CompletionItem> completions(TextDocumentPositionParams params) {
        CalDocument document = documentsByUri.get(params.getTextDocument().getUri());
        if (document == null) {
            return Collections.emptyList();
        }
        Optional<String> textOpt = document.getText();
        if (textOpt.isEmpty()) {
            return Collections.emptyList();
        }
        String text = textOpt.get();
        int offset = TextPositionUtils.offset(text, params.getPosition());
        if (offset < 0) {
            return Collections.emptyList();
        }
        String prefix = TextPositionUtils.identifierPrefix(text, offset);
        Set<String> seen = new HashSet<>();
        List<CompletionItem> items = new ArrayList<>();
        for (String keyword : KEYWORDS) {
            if (keyword.startsWith(prefix) && seen.add(keyword)) {
                CompletionItem item = new CompletionItem(keyword);
                item.setKind(CompletionItemKind.Keyword);
                items.add(item);
            }
        }
        Map<String, List<SymbolDefinition>> definitions = document.getDefinitionsByName();
        addSymbolCompletions(prefix, seen, items, definitions);
        for (List<SymbolDefinition> defs : globalDefinitionsSnapshot()) {
            addSymbolCompletions(prefix, seen, items, defs);
        }
        return items;
    }

    public List<SymbolInformation> workspaceSymbols(String query) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT);
        List<SymbolInformation> results = new ArrayList<>();
        for (List<SymbolDefinition> definitions : globalDefinitionsSnapshot()) {
            for (SymbolDefinition definition : definitions) {
                String name = definition.getName();
                if (normalized.isEmpty() || name.toLowerCase(Locale.ROOT).contains(normalized)) {
                    SymbolInformation info = new SymbolInformation();
                    info.setName(name);
                    info.setKind(definition.getType().getSymbolKind());
                    info.setLocation(definition.getLocation());
                    info.setContainerName(definition.getContainerName());
                    results.add(info);
                }
            }
        }
        return results;
    }

    private void addSymbolCompletions(String prefix, Set<String> seen, List<CompletionItem> items, Map<String, List<SymbolDefinition>> definitions) {
        for (Map.Entry<String, List<SymbolDefinition>> entry : definitions.entrySet()) {
            String name = entry.getKey();
            if (!name.startsWith(prefix) || !seen.add(name)) {
                continue;
            }
            SymbolDefinition candidate = entry.getValue().get(0);
            CompletionItem item = new CompletionItem(name);
            item.setKind(candidate.getType().getCompletionKind());
            items.add(item);
        }
    }

    private void addSymbolCompletions(String prefix, Set<String> seen, List<CompletionItem> items, List<SymbolDefinition> definitions) {
        Map<String, SymbolDefinition> ordered = new LinkedHashMap<>();
        for (SymbolDefinition definition : definitions) {
            ordered.putIfAbsent(definition.getName(), definition);
        }
        for (SymbolDefinition definition : ordered.values()) {
            if (!definition.getName().startsWith(prefix) || !seen.add(definition.getName())) {
                continue;
            }
            CompletionItem item = new CompletionItem(definition.getName());
            item.setKind(definition.getType().getCompletionKind());
            items.add(item);
        }
    }

    private CalDocument createDocument(String uri) {
        CalDocument document = CalDocument.fromUri(uri);
        if (document.getPath() != null) {
            documentsByPath.put(document.getPath(), document);
        }
        return document;
    }

    private List<List<SymbolDefinition>> globalDefinitionsSnapshot() {
        List<List<SymbolDefinition>> snapshot = new ArrayList<>();
        synchronized (this) {
            for (List<SymbolDefinition> definitions : globalIndex.values()) {
                snapshot.add(new ArrayList<>(definitions));
            }
        }
        return snapshot;
    }

    private void indexWorkspace() {
        for (Path folder : workspaceFolders) {
            if (Files.isDirectory(folder)) {
                try {
                    Files.walk(folder)
                            .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".cal"))
                            .forEach(this::loadWorkspaceFile);
                } catch (IOException ignored) {
                    // ignore workspace scanning failures
                }
            }
        }
    }

    private void loadWorkspaceFile(Path path) {
        CalDocument document = documentsByPath.get(path);
        if (document == null) {
            document = new CalDocument(path.toUri().toString(), path);
            documentsByPath.put(path, document);
            documentsByUri.putIfAbsent(document.getUri(), document);
        }
        synchronized (document) {
            if (!document.isOpen()) {
                try {
                    document.reloadFromDisk();
                } catch (IOException ignored) {
                    return;
                }
                refreshGlobalIndex(document);
            }
        }
    }

    private void reloadWorkspaceFile(Path path) {
        CalDocument document = documentsByPath.get(path);
        if (document == null) {
            loadWorkspaceFile(path);
            return;
        }
        synchronized (document) {
            if (document.isOpen()) {
                return;
            }
            try {
                document.reloadFromDisk();
            } catch (IOException ignored) {
                return;
            }
            refreshGlobalIndex(document);
        }
    }

    private void removeWorkspaceFile(Path path) {
        CalDocument document = documentsByPath.get(path);
        if (document == null) {
            return;
        }
        synchronized (document) {
            if (document.isOpen()) {
                removeFromGlobalIndex(document);
                return;
            }
        }
        documentsByPath.remove(path, document);
        documentsByUri.remove(document.getUri(), document);
        synchronized (document) {
            removeFromGlobalIndex(document);
        }
    }

    public void handleFileEvent(String uri, FileChangeType changeType) {
        Path path = uriToPath(uri);
        if (path == null || changeType == null) {
            return;
        }
        switch (changeType) {
            case Created:
                loadWorkspaceFile(path);
                break;
            case Changed:
                reloadWorkspaceFile(path);
                break;
            case Deleted:
                removeWorkspaceFile(path);
                break;
            default:
                break;
        }
    }

    private List<SymbolDefinition> getGlobalDefinitions(String name) {
        synchronized (this) {
            List<SymbolDefinition> defs = globalIndex.get(name);
            return defs == null ? Collections.emptyList() : new ArrayList<>(defs);
        }
    }

    private List<CalDocument> allDocuments() {
        LinkedHashSet<CalDocument> ordered = new LinkedHashSet<>(documentsByUri.values());
        ordered.addAll(documentsByPath.values());
        return new ArrayList<>(ordered);
    }

    private synchronized void refreshGlobalIndex(CalDocument document) {
        removeFromGlobalIndex(document);
        List<SymbolDefinition> updated = document.getWorkspaceVisibleDefinitions();
        globalSymbolsByDocument.put(document, updated);
        for (SymbolDefinition definition : updated) {
            globalIndex.computeIfAbsent(definition.getName(), ignored -> new ArrayList<>()).add(definition);
        }
    }

    private synchronized void removeFromGlobalIndex(CalDocument document) {
        List<SymbolDefinition> previous = globalSymbolsByDocument.getOrDefault(document, Collections.emptyList());
        for (SymbolDefinition definition : previous) {
            List<SymbolDefinition> list = globalIndex.get(definition.getName());
            if (list != null) {
                list.removeIf(entry -> entry.getLocation().equals(definition.getLocation()));
                if (list.isEmpty()) {
                    globalIndex.remove(definition.getName());
                }
            }
        }
        globalSymbolsByDocument.remove(document);
    }

    private Path uriToPath(String uri) {
        try {
            return uriToPath(URI.create(uri));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Path uriToPath(URI uri) {
        if (uri == null || uri.getScheme() == null || uri.getScheme().isEmpty()) {
            return null;
        }
        if (!"file".equals(uri.getScheme())) {
            return null;
        }
        try {
            return Paths.get(uri);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildHoverMarkdown(SymbolDefinition symbol) {
        StringBuilder builder = new StringBuilder();
        builder.append("**")
                .append(symbol.getType().name().toLowerCase(Locale.ROOT).replace('_', ' '))
                .append("** `")
                .append(symbol.getName())
                .append('`');
        if (symbol.getContainerName() != null && !symbol.getContainerName().isEmpty()) {
            builder.append(" _(in ").append(symbol.getContainerName()).append(")_");
        }
        builder.append("\n\n")
                .append(symbol.getLocation().getUri());
        return builder.toString();
    }

    private List<Range> getDefinitionRanges(CalDocument document, String identifier) {
        Map<String, List<SymbolDefinition>> defs = document.getDefinitionsByName();
        if (!defs.containsKey(identifier)) {
            return Collections.emptyList();
        }
        List<Range> ranges = new ArrayList<>();
        for (SymbolDefinition definition : defs.get(identifier)) {
            ranges.add(definition.getSelectionRange());
        }
        return ranges;
    }

    private boolean isDefinitionRange(Range range, List<Range> definitions) {
        for (Range def : definitions) {
            if (rangeEquals(range, def)) {
                return true;
            }
        }
        return false;
    }

    private boolean rangeEquals(Range a, Range b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getStart().getLine() == b.getStart().getLine()
                && a.getStart().getCharacter() == b.getStart().getCharacter()
                && a.getEnd().getLine() == b.getEnd().getLine()
                && a.getEnd().getCharacter() == b.getEnd().getCharacter();
    }
}
