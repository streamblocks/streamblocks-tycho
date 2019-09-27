package ch.epfl.vlsc.tycho.lsp4j.workspace;

public class WorkspaceDocumentException extends Exception {

    public WorkspaceDocumentException(String message) {
        super(message);
    }

    public WorkspaceDocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkspaceDocumentException(Throwable cause) {
        super(cause);
    }

    public WorkspaceDocumentException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
