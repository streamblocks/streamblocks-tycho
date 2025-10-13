package ch.epfl.vlsc.tycho.lsp4j.analysis;

import ch.epfl.vlsc.tycho.lsp4j.util.AstRangeUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.InstanceDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefinitionCollector {
    private final String documentUri;
    private final String sourceText;
    private final Map<String, List<SymbolDefinition>> definitionsByName = new HashMap<>();
    private final List<SymbolDefinition> allDefinitions = new ArrayList<>();

    public DefinitionCollector(String documentUri, String sourceText) {
        this.documentUri = documentUri;
        this.sourceText = sourceText;
    }

    public void collect(NamespaceDecl namespaceDecl) {
        definitionsByName.clear();
        allDefinitions.clear();
        if (namespaceDecl == null) {
            return;
        }
        SymbolDefinition namespaceSymbol = createSymbol(namespaceDecl.getQID().toString(), SymbolType.NAMESPACE, namespaceDecl, namespaceDecl, null);
        addSymbol(namespaceSymbol);
        for (GlobalEntityDecl decl : namespaceDecl.getEntityDecls()) {
            SymbolDefinition entitySymbol = createSymbol(decl.getName(), resolveGlobalEntityType(decl), decl, decl, namespaceSymbol.getName());
            addSymbol(entitySymbol);
            if (decl.getEntity() instanceof CalActor) {
                collectActorSymbols((CalActor) decl.getEntity(), decl.getName());
            } else if (decl.getEntity() instanceof NlNetwork) {
                collectNetworkSymbols((NlNetwork) decl.getEntity(), decl.getName());
            }
        }
    }

    public Map<String, List<SymbolDefinition>> getDefinitionsByName() {
        return definitionsByName;
    }

    public List<SymbolDefinition> getAllDefinitions() {
        return allDefinitions;
    }

    public List<SymbolDefinition> getWorkspaceVisibleDefinitions() {
        List<SymbolDefinition> result = new ArrayList<>();
        for (SymbolDefinition definition : allDefinitions) {
            if (definition.getType().isWorkspaceVisible()) {
                result.add(definition);
            }
        }
        return result;
    }

    private void collectActorSymbols(CalActor actor, String containerName) {
        for (PortDecl port : actor.getInputPorts()) {
            addSymbol(createSymbol(port.getName(), SymbolType.PORT, port, port, containerName));
        }
        for (PortDecl port : actor.getOutputPorts()) {
            addSymbol(createSymbol(port.getName(), SymbolType.PORT, port, port, containerName));
        }
        for (VarDecl varDecl : actor.getVarDecls()) {
            addSymbol(createSymbol(varDecl.getName(), SymbolType.VARIABLE, varDecl, varDecl, containerName));
        }
        for (Action action : actor.getActions()) {
            String actionName = action.getTag() == null ? "action" : action.getTag().toString();
            addSymbol(createSymbol(actionName, SymbolType.ACTION, action, action, containerName));
        }
    }

    private void collectNetworkSymbols(NlNetwork network, String containerName) {
        for (PortDecl port : network.getInputPorts()) {
            addSymbol(createSymbol(port.getName(), SymbolType.PORT, port, port, containerName));
        }
        for (PortDecl port : network.getOutputPorts()) {
            addSymbol(createSymbol(port.getName(), SymbolType.PORT, port, port, containerName));
        }
        for (VarDecl varDecl : network.getVarDecls()) {
            addSymbol(createSymbol(varDecl.getName(), SymbolType.VARIABLE, varDecl, varDecl, containerName));
        }
        for (InstanceDecl instance : network.getEntities()) {
            addSymbol(createSymbol(instance.getInstanceName(), SymbolType.ACTOR, instance, instance, containerName));
        }
    }

    private SymbolType resolveGlobalEntityType(GlobalEntityDecl decl) {
        if (decl.getEntity() instanceof CalActor) {
            return SymbolType.ACTOR;
        }
        if (decl.getEntity() instanceof NlNetwork) {
            return SymbolType.NETWORK;
        }
        return SymbolType.ACTOR;
    }

    private SymbolDefinition createSymbol(String name, SymbolType type, se.lth.cs.tycho.ir.IRNode node, se.lth.cs.tycho.ir.IRNode selectionNode, String containerName) {
        Range range = AstRangeUtils.toRange(node);
        Range selection = AstRangeUtils.toRange(selectionNode);
        range = adjustRange(type, name, range, true);
        selection = adjustRange(type, name, selection, false);
        Location location = new Location(documentUri, range);
        return new SymbolDefinition(name, type, location, selection, containerName);
    }

    private void addSymbol(SymbolDefinition symbolDefinition) {
        allDefinitions.add(symbolDefinition);
        definitionsByName.computeIfAbsent(symbolDefinition.getName(), ignored -> new ArrayList<>()).add(symbolDefinition);
    }

    private Range adjustRange(SymbolType type, String name, Range original, boolean includeKeyword) {
        if (sourceText == null || original == null) {
            return original;
        }
        if (hasRealPosition(original)) {
            return original;
        }
        FallbackMatch match = findFallbackMatch(type, name);
        if (match == null) {
            return original;
        }
        int startOffset = includeKeyword ? match.keywordStart : match.nameStart;
        int endOffset;
        if (includeKeyword) {
            endOffset = findLineEnd(match.keywordStart);
        } else {
            endOffset = match.nameStart + name.length();
        }
        return new Range(
                ch.epfl.vlsc.tycho.lsp4j.util.TextPositionUtils.positionAt(sourceText, startOffset),
                ch.epfl.vlsc.tycho.lsp4j.util.TextPositionUtils.positionAt(sourceText, endOffset));
    }

    private boolean hasRealPosition(Range range) {
        if (range == null) {
            return false;
        }
        boolean startPlaceholder = range.getStart().getLine() == 0 && range.getStart().getCharacter() == 0;
        boolean endPlaceholder = range.getEnd().getLine() == 0 && range.getEnd().getCharacter() <= 1;
        return !(startPlaceholder && endPlaceholder);
    }

    private FallbackMatch findFallbackMatch(SymbolType type, String name) {
        List<String> keywords = keywordsFor(type);
        if (keywords.isEmpty()) {
            return null;
        }
        for (String keyword : keywords) {
            FallbackMatch match = tryMatch(keyword, name);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private FallbackMatch tryMatch(String keyword, String name) {
        int keywordIndex = indexOfKeyword(keyword, name);
        if (keywordIndex < 0) {
            return null;
        }
        int nameIndex = keywordIndex + keyword.length();
        return new FallbackMatch(keywordIndex, nameIndex);
    }

    private int indexOfKeyword(String keyword, String name) {
        List<String> patterns = List.of(
                keyword + name + "(",
                keyword + name + " ",
                keyword + name + ":",
                keyword + name + "\n",
                keyword + name + "\r"
        );
        for (String pattern : patterns) {
            int index = sourceText.indexOf(pattern);
            if (index >= 0) {
                return index;
            }
        }
        return -1;
    }

    private int findLineEnd(int start) {
        int newline = sourceText.indexOf('\n', start);
        if (newline < 0) {
            return sourceText.length();
        }
        return newline;
    }

    private List<String> keywordsFor(SymbolType type) {
        switch (type) {
            case NAMESPACE:
                return List.of("namespace ");
            case ACTOR:
                return List.of("actor ");
            case NETWORK:
                return List.of("network ");
            default:
                return List.of();
        }
    }

    private static final class FallbackMatch {
        final int keywordStart;
        final int nameStart;

        FallbackMatch(int keywordStart, int nameStart) {
            this.keywordStart = keywordStart;
            this.nameStart = nameStart;
        }
    }
}
