package ch.epfl.vlsc.tycho.lsp4j.common;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tycho CAL document for LSP
 *
 * @author Endri Bezati
 */

public class TychoLspDocument {

    private Path path;
    private String uri;
    private String projectRoot;
    private List<GlobalEntityDecl> projectEntities = new ArrayList<>();
    private boolean withinProject = false;
    private String ownerEntity = "";
    private Path ownerEntityPath = null;

    public TychoLspDocument(String uri) {
        try {
            this.uri = uri;
            this.path = Paths.get(new URL(uri).toURI());
            this.projectRoot = ""; // -- FIXME
            if (this.projectRoot == null) {
                return;
            }
            try {
                this.withinProject = !Files.isSameFile(this.path.getParent(), Paths.get(projectRoot));
            } catch (IOException e) {
                withinProject = false;
            }
            if (withinProject) {
                // -- FIXME : project retrieve entities
                this.projectEntities = new ArrayList<>();
                this.ownerEntity = "";
                this.ownerEntityPath = Paths.get(projectRoot).resolve("src").resolve(ownerEntity);
            }
        } catch (Exception e) {
            // -- ignore
        }
    }

    public TychoLspDocument(Path path, String projectRoot) {
        this.uri = path.toUri().toString();
        this.projectRoot = projectRoot;
        this.path = path;
        this.withinProject = true;
    }


    /**
     * Get the path of the given URI.
     *
     * @return {@link Path} get the path
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Get source root path.
     *
     * @return {@link Path} source root path
     */
    public Path getProjectRootPath() {
        return Paths.get(this.projectRoot);
    }

    /**
     * Get the URI of the given string URI.
     *
     * @return {@link URI} get the URI
     * @throws MalformedURLException can throw malformed url se.lth.cs.tycho.interp.exception
     * @throws URISyntaxException    can throw URI syntax se.lth.cs.tycho.interp.exception
     */
    public URI getURI() throws MalformedURLException, URISyntaxException {
        return new URL(uri).toURI();
    }

    /**
     * Get the uri.
     *
     * @return {@link String} get the string uri
     */
    public String getURIString() {
        return this.uri;
    }

    /**
     * Get source root.
     *
     * @return {@link String} source root
     */
    public String getProjectRoot() {
        return this.projectRoot;
    }

    /**
     * Set URI.
     *
     * @param uri string URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Set source root.
     *
     * @param sourceRoot source root
     */
    public void setProjectRootRoot(String sourceRoot) {
        this.projectRoot = sourceRoot;
    }

    /**
     * Get the project modules list.
     *
     * @return {@link List} list of project modules
     */
    public List<GlobalEntityDecl> getProjectEntities() {
        return projectEntities;
    }

    public boolean isWithinProject() {
        return withinProject;
    }

    public String getOwnerEntity() {
        return ownerEntity;
    }

    public Path getOwnerEntityPath() {
        return ownerEntityPath;
    }

    @Override
    public String toString() {
        return "{" + "projectRoot:" + this.projectRoot + ", uri:" + this.uri + "}";
    }

    private NamespaceDecl getNamespaceDeclForDocument(String filePath) {
        Path input = Paths.get(filePath);
        CalParser parser = null;
        NamespaceDecl ns = null;
        try {
            parser = new CalParser(Files.newInputStream(input));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ns = parser.CompilationUnit();

        } catch (ParseException e) {
            // -- TODO : implement me
        }
        return ns;
    }

    private List<GlobalEntityDecl> getCurrentProjectGlobalEntities(Path projectRoot) {
        List<GlobalEntityDecl> globalEntityDecls = new ArrayList<>();
        // -- TODO : implement me
        return globalEntityDecls;
    }

}
