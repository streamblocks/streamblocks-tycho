package ch.epfl.vlsc.tycho.lsp4j.workspace;

import org.eclipse.lsp4j.CodeLens;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public interface WorkspaceDocumentManager {


    /**
     * Checks whether the given file is open in workspace
     *
     * @param filePath
     * @return
     */
    boolean isFileOpen(Path filePath);

    /**
     * Opens the given file in document manager.
     * <p>
     * Usage example:
     * <p>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     * lock = documentManager.openFile(filePath, "");
     * } finally {
     * lock.ifPresent(Lock:unlock);
     * }
     *
     * @param filePath
     * @param content
     * @throws WorkspaceDocumentException
     */
    void openFile(Path filePath, String content) throws WorkspaceDocumentException;

    /**
     * Updates given file in document manager with new content.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     documentManager.updateFile(filePath, range, "");
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath       Path of the file
     * @param updatedContent New content of the file
     * @throws WorkspaceDocumentException when file cannot be updated.
     */
    void updateFile(Path filePath, String updatedContent) throws WorkspaceDocumentException;

    /**
     * Updates code lenses of a given file in document manager with new code lenses sent to client.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     documentManager.setCodeLenses(filePath, lenses);
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath Path of the file
     * @param codeLens New code lenses of the file
     * @throws WorkspaceDocumentException when file cannot be updated.
     */
    void setCodeLenses(Path filePath, List<CodeLens> codeLens) throws WorkspaceDocumentException;

    /**
     * Set the pruned source content of a given file.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     documentManager.setPrunedContent(filePath, prunedSource);
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath Path of the file
     * @param prunedSource Pruned source of the file
     * @throws WorkspaceDocumentException when file cannot be updated.
     */
    void setPrunedContent(Path filePath, String prunedSource) throws WorkspaceDocumentException;

    /**
     * Returns the code lenses of the file.
     *
     * @param filePath Path of the file
     * @return Code lenses of the file
     */
    List<CodeLens> getCodeLenses(Path filePath);

    /**
     * Close the given file in document manager.
     *
     * @param filePath Path of the file
     * @throws WorkspaceDocumentException when file cannot be closed.
     */
    void closeFile(Path filePath) throws WorkspaceDocumentException;

    /**
     * Returns the content of the file.
     *
     * @param filePath Path of the file
     * @return Content of the file
     * @throws WorkspaceDocumentException when file cannot be read.
     */
    String getFileContent(Path filePath) throws WorkspaceDocumentException;

    /**
     * Acquire a file lock.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     //your code
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath Path of the file
     * @return {@link Lock} retrieving a lock for the file. You must call Lock.unlock() once you are done with the work.
     */
    Optional<Lock> lockFile(Path filePath);

    /**
     * Returns a list of all file paths.
     * @return set of {@link Path}
     */
    Set<Path> getAllFilePaths();

    /**
     * Clear all file paths.
     *
     */
    void clearAllFilePaths();

}
