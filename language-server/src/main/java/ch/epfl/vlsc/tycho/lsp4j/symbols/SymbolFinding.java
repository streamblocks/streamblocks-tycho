package ch.epfl.vlsc.tycho.lsp4j.symbols;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.InstanceDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolFinding {
    private List<Either<SymbolInformation, DocumentSymbol>> symbols;
    private String uri;

    private Visitor visitor;

    public SymbolFinding(String uri) {
        this.uri = uri;

        visitor = MultiJ.from(Visitor.class)
                .bind("uri").to(uri).instance();

    }

    public List<Either<SymbolInformation, DocumentSymbol>> visit(IRNode node) {
        List<DocumentSymbol> documentSymbols = visitor.visit(node);
        return documentSymbols.stream().<Either<SymbolInformation, DocumentSymbol>>map(Either::forRight).collect(Collectors.toList());
    }


    @Module
    interface Visitor {
        @Binding(BindingKind.INJECTED)
        String uri();

        default List<DocumentSymbol> visit(IRNode node) {
            return new ArrayList<>();
        }

        default List<DocumentSymbol> visit(NamespaceDecl decl) {
            DocumentSymbol ds = new DocumentSymbol();
            ds.setName(decl.getQID().getLast().toString());
            ds.setKind(SymbolKind.Namespace);
            ds.setRange(getRange(decl));
            ds.setSelectionRange(getRange(decl));

            List<DocumentSymbol> children = new ArrayList<>();
            for (GlobalEntityDecl globalEntityDecl : decl.getEntityDecls()) {
                DocumentSymbol globalEntityDS = new DocumentSymbol();
                globalEntityDS.setName(globalEntityDecl.getName());
                globalEntityDS.setKind(SymbolKind.Class);
                globalEntityDS.setChildren(visit(globalEntityDecl.getEntity()));
                globalEntityDS.setRange(getRange(globalEntityDecl));
                globalEntityDS.setSelectionRange(getRange(globalEntityDecl));
                children.add(globalEntityDS);
            }
            ds.setChildren(children);
            return Arrays.asList(ds);
        }

        default List<DocumentSymbol> visit(CalActor actor) {
            List<DocumentSymbol> childrenDS = new ArrayList<>();

            // -- Input Ports
            if (!actor.getInputPorts().isEmpty()) {
                childrenDS.add(getPortDeclsDS(actor, "Input Ports", actor.getInputPorts()));
            }

            // -- Output Ports
            if (!actor.getOutputPorts().isEmpty()) {
                childrenDS.add(getPortDeclsDS(actor, "Output Ports", actor.getOutputPorts()));
            }

            // -- Variables{
            if (!actor.getVarDecls().isEmpty()) {
                List<DocumentSymbol> children = new ArrayList<>();
                for (VarDecl varDecl : actor.getVarDecls()) {
                    children.addAll(visit(varDecl));
                }
                DocumentSymbol varDeclDs = getSymbol(actor, "Variables", SymbolKind.Variable, children);
                childrenDS.add(varDeclDs);
            }

            // -- Actions
            if (!actor.getActions().isEmpty()) {
                List<DocumentSymbol> children = new ArrayList<>();
                for (Action action : actor.getActions()) {
                    children.addAll(visit(action));
                }
                DocumentSymbol actionDS = getSymbol(actor, "Actions", SymbolKind.Method, children);
                childrenDS.add(actionDS);
            }
            return childrenDS;
        }

        default List<DocumentSymbol> visit(VarDecl varDecl) {
            DocumentSymbol symbol = getSymbol(varDecl, varDecl.getName(), SymbolKind.Variable, Collections.emptyList());
            return Arrays.asList(symbol);
        }

        default List<DocumentSymbol> visit(PortDecl port) {
            DocumentSymbol symbol = getSymbol(port, port.getName(), SymbolKind.Field, Collections.emptyList());
            return Arrays.asList(symbol);
        }

        default List<DocumentSymbol> visit(NlNetwork nlNetwork) {
            List<DocumentSymbol> childrenDS = new ArrayList<>();

            // -- Input Ports
            if (!nlNetwork.getInputPorts().isEmpty()) {
                childrenDS.add(getPortDeclsDS(nlNetwork, "Input Ports", nlNetwork.getInputPorts()));
            }

            // -- Output Ports
            if (!nlNetwork.getOutputPorts().isEmpty()) {
                childrenDS.add(getPortDeclsDS(nlNetwork, "Output Ports", nlNetwork.getOutputPorts()));
            }

            // -- Variables{
            if (!nlNetwork.getVarDecls().isEmpty()) {
                List<DocumentSymbol> children = new ArrayList<>();
                for (VarDecl varDecl : nlNetwork.getVarDecls()) {
                    children.addAll(visit(varDecl));
                }
                DocumentSymbol varDeclDs = getSymbol(nlNetwork, "Variables", SymbolKind.Variable, children);
                childrenDS.add(varDeclDs);
            }

            // -- Entities
            if (!nlNetwork.getEntities().isEmpty()) {
                List<DocumentSymbol> children = new ArrayList<>();
                for (InstanceDecl decl : nlNetwork.getEntities()) {
                    children.addAll(visit(decl));
                }
                DocumentSymbol varDeclDs = getSymbol(nlNetwork, "Entities", SymbolKind.Object, children);
                childrenDS.add(varDeclDs);
            }

            // -- Structure
            if (!nlNetwork.getStructure().isEmpty()) {
                List<DocumentSymbol> children = new ArrayList<>();
                for (StructureStatement str : nlNetwork.getStructure()) {
                    children.addAll(visit(str));
                }
                DocumentSymbol varDeclDs = getSymbol(nlNetwork, "Structure", SymbolKind.Object, children);
                childrenDS.add(varDeclDs);
            }
            return childrenDS;
        }


        default List<DocumentSymbol> visit(Action action) {
            String label;
            if (action.getTag() == null) {
                label = "unnamed";

            } else {
                label = action.getTag().toString();
                //DocumentSymbol symbol = getSymbolRange(action, label, SymbolKind.Function, getRange(action.getTag()), getRange(action), new ArrayList<>());
                //return Arrays.asList(symbol);
            }

            DocumentSymbol symbol = getSymbol(action, label, SymbolKind.Function, Collections.emptyList());
            return Arrays.asList(symbol);
        }

        default List<DocumentSymbol> visit(InstanceDecl instanceDecl) {
            DocumentSymbol symbol = getSymbol(instanceDecl, instanceDecl.getInstanceName(), SymbolKind.Object, Collections.emptyList());
            return Arrays.asList(symbol);
        }

        default List<DocumentSymbol> visit(StructureStatement structureStatement) {
            return Collections.emptyList();
        }

        default List<DocumentSymbol> visit(StructureConnectionStmt structureConnectionStmt) {
            DocumentSymbol symbol = getSymbol(structureConnectionStmt, structureConnectionStmt.toString(), SymbolKind.Operator, Collections.emptyList());
            return Arrays.asList(symbol);
        }

        // -- Helper Methods

        default DocumentSymbol getPortDeclsDS(IRNode node, String name, List<PortDecl> portDecls) {
            List<DocumentSymbol> children = new ArrayList<>();
            for (PortDecl port : portDecls) {
                children.addAll(visit(port));
            }
            DocumentSymbol ds = getSymbol(node, name, SymbolKind.Field, children);
            return ds;
        }

        default DocumentSymbol getSymbol(IRNode node, String symbolName, SymbolKind kind, List<DocumentSymbol> children) {
            DocumentSymbol lspSymbol = new DocumentSymbol();
            lspSymbol.setName(symbolName);
            lspSymbol.setKind(kind);
            lspSymbol.setChildren(new ArrayList<>());
            lspSymbol.setRange(getRange(node));
            lspSymbol.setSelectionRange(getRange(node));
            lspSymbol.setChildren(children);
            return lspSymbol;
        }

        default DocumentSymbol getSymbolRange(IRNode node, String symbolName, SymbolKind kind, Range range, Range selection, List<DocumentSymbol> children) {
            DocumentSymbol lspSymbol = new DocumentSymbol();
            lspSymbol.setName(symbolName);
            lspSymbol.setKind(kind);
            lspSymbol.setChildren(new ArrayList<>());
            lspSymbol.setRange(range);
            lspSymbol.setSelectionRange(selection);
            lspSymbol.setChildren(children);
            return lspSymbol;
        }

        default Range getRange(IRNode node) {
            Range r = new Range();

            int startLine = node.getFromLineNumber() == 0 ? 0 : node.getFromLineNumber() - 1; // LSP range is 0 based
            int startChar = node.getFromColumnNumber() == 0 ? 0 : node.getFromColumnNumber() - 1;
            int endLine = node.getToLineNumber() == 0 ? 0 : node.getToLineNumber() - 1;
            int endChar = node.getToColumnNumber() == 0 ? 0 : node.getToColumnNumber() - 1;

            if (endLine <= 0) {
                endLine = startLine;
            }

            if (endChar <= 0) {
                endChar = startChar + 1;
            }

            r.setStart(new Position(startLine, startChar));
            r.setEnd(new Position(endLine, endChar));

            return r;
        }

    }

}
