package ch.epfl.vlsc.tycho.lsp4j;

import ch.epfl.vlsc.tycho.lsp4j.analysis.CalWorkspace;
import ch.epfl.vlsc.tycho.lsp4j.util.TextPositionUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import ch.epfl.vlsc.tycho.lsp4j.analysis.CalDocument;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.SymbolInformation;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class CalWorkspaceTest {

    private CalWorkspace workspace;
    private Path tempFile;
    private String uri;
    private String calSource;

    @Before
    public void setUp() throws Exception {
        workspace = new CalWorkspace();
        calSource = """
namespace ext:

    external procedure nsSetGlobal(int(size=8) x) end

    external function nsAddGlobal(int(size=8) x) --> int(size=8) end

    actor TestActor(int(size=8) value) int(size=8) In ==> int(size=8) Out :
        external procedure actorSetGlobal(int(size=8) x) end
        external function actorAddGlobal(int(size=8) x) --> int(size=8) end

        initialize ==> do
            actorSetGlobal(value);
        end

        action In:[x] ==> Out:[actorAddGlobal(x)] end

    end

    actor TestNs(int(size=8) value) int(size=8) In ==> int(size=8) Out :

        initialize ==> do
            nsSetGlobal(value);
        end

        action In:[x] ==> Out:[nsAddGlobal(x)] end

    end

    network Test() int(size=8) In ==> int(size=8) Out :
    entities
        testActor = TestActor(value = 42);
        testNs = TestNs(value = -42);
    structure
        In --> testActor.In;
        testActor.Out --> testNs.In;
        testNs.Out --> Out;
    end

end
""";
        tempFile = Files.createTempFile("tycho-lsp", ".cal");
        Files.writeString(tempFile, calSource);
        uri = tempFile.toUri().toString();
        workspace.openDocument(uri, calSource);
        if (!workspace.diagnostics(uri).isEmpty()) {
            throw new IllegalStateException("Failed to parse CAL source: " + workspace.diagnostics(uri).get(0).getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void definitionReturnsAtLeastOneLocation() {
        TextDocumentPositionParams position = positionAt("TestActor(value");
        List<Location> definitions = workspace.definitionLocations(position);
        assertFalse("expected definition to be found", definitions.isEmpty());
        Location location = definitions.get(0);
        assertEquals(uri, location.getUri());
        Range range = location.getRange();
        assertNotNull("definition range should be present", range);

        int actorDeclOffset = calSource.indexOf("actor TestActor");
        Position expectedStart = TextPositionUtils.positionAt(calSource, actorDeclOffset);
        assertEquals(expectedStart.getLine(), range.getStart().getLine());
        assertEquals(expectedStart.getCharacter(), range.getStart().getCharacter());
    }

    @Test
    public void hoverReturnsMarkdownInformation() {
        TextDocumentPositionParams position = positionAt("TestActor(value");
        Optional<Hover> hover = workspace.hover(position);
        assertTrue("hover should be present", hover.isPresent());
        Hover value = hover.get();
        assertNotNull(value.getContents());
        assertTrue("hover content should be markup", value.getContents().isRight());
        String markdown = value.getContents().getRight().getValue();
        assertTrue(markdown.contains("TestActor"));
        assertNotNull(value.getRange());
    }

    @Test
    public void referencesRespondToDeclarationToggle() {
        TextDocumentPositionParams position = positionAt("TestActor(value");
        List<? extends Location> references = workspace.references(position, false);
        assertEquals(1, references.size());
        assertEquals(uri, references.get(0).getUri());

        List<? extends Location> allRefs = workspace.references(position, true);
        assertEquals(2, allRefs.size());
    }

    @Test
    public void completionIncludesSymbols() {
        TextDocumentPositionParams position = positionAt("TestActor(value");
        List<CompletionItem> completions = workspace.completions(position);
        boolean hasActor = completions.stream()
                .anyMatch(item -> "TestActor".equals(item.getLabel()));
        assertTrue("Expected completion items to include TestActor", hasActor);
    }

    private TextDocumentPositionParams positionAt(String marker) {
        int offset = calSource.indexOf(marker);
        assertTrue("marker must exist in source", offset >= 0);
        Position position = TextPositionUtils.positionAt(calSource, offset + 1);
        TextDocumentPositionParams params = new TextDocumentPositionParams();
        params.setTextDocument(new TextDocumentIdentifier(uri));
        params.setPosition(position);
        return params;
    }

    @Test
    public void definitionResolvesAcrossFiles() throws Exception {
        Path tempDir = Files.createTempDirectory("tycho-multi");
        Path actorPath = tempDir.resolve("ExtActors.cal");
        Files.writeString(actorPath, calSource);

        Path networkPath = tempDir.resolve("UsesTestActor.cal");
        String networkSource = """
namespace ext:

    network UsesTestActor() ==> int(size=8) Out :
    entities
        worker = TestActor(value=0);
    structure
        worker.Out --> Out;
    end

end
""";
        Files.writeString(networkPath, networkSource);

        CalWorkspace multiWorkspace = new CalWorkspace();
        multiWorkspace.configure(tempDir.toUri(), null);

        String actorUri = actorPath.toUri().toString();
        String networkUri = networkPath.toUri().toString();
        multiWorkspace.handleFileEvent(actorUri, FileChangeType.Created);
        multiWorkspace.handleFileEvent(networkUri, FileChangeType.Created);

        CalDocument actorDocument = multiWorkspace.getDocument(actorUri).orElse(null);
        assertNotNull("workspace should index closed actor file", actorDocument);
        assertTrue("actor document should have text", actorDocument.getText().isPresent());
        assertTrue("actor document diagnostics " + actorDocument.getDiagnostics(), actorDocument.getDiagnostics().isEmpty());
        assertTrue("actor document should have workspace-visible symbols", !actorDocument.getWorkspaceVisibleDefinitions().isEmpty());

        List<SymbolInformation> symbols = multiWorkspace.workspaceSymbols("");
        assertTrue("workspace symbols should include TestActor", symbols.stream().anyMatch(info -> "TestActor".equals(info.getName())));

        multiWorkspace.openDocument(networkUri, networkSource);

        int offset = networkSource.indexOf("TestActor(value=0)");
        assertTrue("network source must reference TestActor", offset >= 0);
        Position position = TextPositionUtils.positionAt(networkSource, offset + 1);
        TextDocumentPositionParams params = new TextDocumentPositionParams();
        params.setTextDocument(new TextDocumentIdentifier(networkUri));
        params.setPosition(position);

        List<Location> definitions = multiWorkspace.definitionLocations(params);
        assertFalse("expected cross-file definition", definitions.isEmpty());
        boolean hasTestActorDefinition = definitions.stream()
                .anyMatch(loc -> actorPath.toUri().toString().equals(loc.getUri()));
        assertTrue("definition URIs " + definitions, hasTestActorDefinition);

        try (var paths = Files.walk(tempDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        if (!path.equals(tempDir)) {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception ignored) {
                                // ignore cleanup issues for test directory
                            }
                        }
                    });
        }
        Files.deleteIfExists(tempDir);
    }
}
