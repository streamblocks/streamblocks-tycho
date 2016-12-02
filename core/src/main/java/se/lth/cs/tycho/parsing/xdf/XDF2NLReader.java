package se.lth.cs.tycho.parsing.xdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.lth.cs.tycho.ir.*;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.InstanceDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XDF2NLReader {
	private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public NamespaceDecl read(InputStream is, QID qid) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(is);
		return buildNetwork(document, qid);
	}

	private NamespaceDecl buildNetwork(Document doc, QID qid) {
		ImmutableList<InstanceDecl> instances = getInstances(doc);
		ImmutableList<StructureStatement> connections = getConnections(doc);
		ImmutableList<ParameterVarDecl> parameters = getParameters(doc);
		ImmutableList<LocalVarDecl> varDecls = getVariableDeclarations(doc);
		ImmutableList<PortDecl> inputPorts = getPorts(doc, true);
		ImmutableList<PortDecl> outputPorts = getPorts(doc, false);
		NlNetwork network = new NlNetwork(ImmutableList.empty(), parameters, ImmutableList.empty(), varDecls, inputPorts, outputPorts, instances, connections);
		GlobalEntityDecl networkDecl = GlobalEntityDecl.global(Availability.PUBLIC, qid.getLast().toString(), network);
		return new NamespaceDecl(qid.getButLast(), ImmutableList.empty(), ImmutableList.empty(), ImmutableList.of(networkDecl), ImmutableList.empty());
	}

	private ImmutableList<PortDecl> getPorts(Document doc, boolean isInput) {
		String kind = isInput ? "Input" : "Output";
		ImmutableList.Builder<PortDecl> builder = ImmutableList.builder();
		for (Element port : selectChildren(doc.getDocumentElement(), "Port")) {
			if (port.getAttribute("kind").equalsIgnoreCase(kind)) {
				List<Element> ts = selectChildren(port, "Type");
				if (ts.isEmpty()) {
					builder.add(new PortDecl(port.getAttribute("name")));
				} else {
					TypeExpr type = getTypeExpr(ts.get(0));
					builder.add(new PortDecl(port.getAttribute("name"), type));
				}
			}
		}
		return builder.build();
	}

	private ImmutableList<ParameterVarDecl> getParameters(Document doc) {
		ImmutableList.Builder<ParameterVarDecl> builder = ImmutableList.builder();
		for (Element decl : selectChildren(doc.getDocumentElement(), "Decl")) {
			if (decl.getAttribute("kind").equals("Param")) {
				String name = decl.getAttribute("name");
				TypeExpr type = getTypeExpr(selectChild(decl, "Type"));
				builder.add(new ParameterVarDecl(type, name, null));
			}
		}
		return builder.build();
	}

	private ImmutableList<LocalVarDecl> getVariableDeclarations(Document doc) {
		ImmutableList.Builder<LocalVarDecl> builder = ImmutableList.builder();
		for (Element decl : selectChildren(doc.getDocumentElement(), "Decl")) {
			if (decl.getAttribute("kind").equals("Variable")) {
				String name = decl.getAttribute("name");
				TypeExpr type = getTypeExpr(selectChild(decl, "Type"));
				Expression value = buildExpression(selectChild(decl, "Expr"));
				builder.add(new LocalVarDecl(type, name, value, true));
			}
		}
		return builder.build();
	}

	private ImmutableList<StructureStatement> getConnections(Document input) {
		ImmutableList.Builder<StructureStatement> connections = ImmutableList.builder();
		for (Element conn : selectChildren(input.getDocumentElement(), "Connection")) {
			String src = conn.getAttribute("src");
			if (src.isEmpty()) src = null;
			Port srcPort = new Port(conn.getAttribute("src-port"));
			String dst = conn.getAttribute("dst");
			if (dst.isEmpty()) dst = null;
			Port dstPort = new Port(conn.getAttribute("dst-port"));
			ImmutableList<ToolAttribute> attributes;
			String bufferSize = conn.getAttribute("buffer-size");
			if (bufferSize.equals("")) {
				attributes = ImmutableList.empty();
			} else {
				attributes = ImmutableList.of(new ToolValueAttribute("buffer-size", new ExprLiteral(ExprLiteral.Kind.Integer, bufferSize)));
			}
			connections.add(new StructureConnectionStmt(new PortReference(src, ImmutableList.empty(), srcPort.getName()), new PortReference(dst, ImmutableList.empty(), dstPort.getName())).withAttributes(attributes));
		}
		return connections.build();
	}

	private ImmutableList<InstanceDecl> getInstances(Document input) {
		ImmutableList.Builder<InstanceDecl> entities = ImmutableList.builder();
		for (Element instance : selectChildren(input.getDocumentElement(), "Instance")) {
			String instanceName = instance.getAttribute("id");
			QID entityQid = QID.parse(selectChild(instance, "Class").getAttribute("name"));
			List<ValueParameter> parameters = new ArrayList<>();
			for (Element parameter : selectChildren(instance, "Parameter")) {
				String name = parameter.getAttribute("name");
				Expression expr = buildExpression(selectChild(parameter, "Expr"));
				ValueParameter assignment = new ValueParameter(name, expr);
				parameters.add(assignment);
			}
			entities.add(new InstanceDecl(instanceName, new EntityInstanceExpr(new EntityReferenceGlobal(entityQid), parameters)));
		}
		return entities.build();
	}

	private TypeExpr getTypeExpr(Element typeExpr) {
		assert typeExpr.getTagName().equals("Type");
		String name = typeExpr.getAttribute("name");
		ImmutableList.Builder<ValueParameter> valueParameters = ImmutableList.builder();
		ImmutableList.Builder<TypeParameter> typeParameters = ImmutableList.builder();
		for (Element entry : selectChildren(typeExpr, "Entry")) {
			String parameterName = entry.getAttribute("name");
			switch (entry.getAttribute("kind")) {
				case "Expr":
					Expression value = buildExpression(selectChild(entry, "Expr"));
					valueParameters.add(new ValueParameter(parameterName, value));
					break;
				default:
					throw new UnsupportedOperationException("Unknown XDF type attribute kind: '" + entry.getAttribute("kind") + "'");
			}
		}
		return new NominalTypeExpr(name, typeParameters.build(), valueParameters.build());
	}

	private Expression buildExpression(Element expr) {
		assert expr.getTagName().equals("Expr");
		switch (expr.getAttribute("kind")) {
			case "Literal":
				switch (expr.getAttribute("literal-kind")) {
					case "Integer":
						return new ExprLiteral(ExprLiteral.Kind.Integer, expr.getAttribute("value"));
					case "String":
						return new ExprLiteral(ExprLiteral.Kind.String, expr.getAttribute("value"));
					case "Real":
						return new ExprLiteral(ExprLiteral.Kind.Real, expr.getAttribute("value"));
					case "Boolean":
						boolean value;
						switch (expr.getAttribute("value")) {
							case "true":
								value = true; break;
							case "false":
								value = false; break;
							default:
								throw new UnsupportedOperationException("Unknown XDF boolean literal : '" + expr.getAttribute("value") + "'");
						}
						return new ExprLiteral(value ? ExprLiteral.Kind.True : ExprLiteral.Kind.False);
					default:
						throw new UnsupportedOperationException("Unknown XDF literal kind: '" + expr.getAttribute("literal-kind") + "'");
				}
			case "UnaryOp":
				Element op = selectChild(expr, "Op");
				String operation = op.getAttribute("name");
				Expression operand = buildExpression(selectChild(expr, "Expr"));
				return new ExprUnaryOp(operation, operand);
			case "Var":
				String name = expr.getAttribute("name");
				return new ExprVariable(Variable.variable(name));
			case "BinOpSeq":
				ImmutableList<Expression> exprs = selectChildren(expr, "Expr").stream()
						.map(this::buildExpression)
						.collect(ImmutableList.collector());
				ImmutableList<String> ops = selectChildren(expr, "Op").stream()
						.map(o -> o.getAttribute("name"))
						.collect(ImmutableList.collector());
				return new ExprBinaryOp(ops, exprs);
		}
		throw new UnsupportedOperationException("Unknown XDF expression: '" + expr.getAttribute("kind") + "'");
	}

	private Element selectChild(Node n, String name) {
		return selectChildren(n, name).get(0);
	}

	private List<Element> selectChildren(Node n, String name) {
		List<Element> result = new ArrayList<>();
		NodeList children = n.getChildNodes();
		for (int i = 0, len = children.getLength(); i < len; i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) child;
				if (element.getNodeName().equalsIgnoreCase(name)) {
					result.add(element);
				}
			}
		}
		return result;
	}

}
