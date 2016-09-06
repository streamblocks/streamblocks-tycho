package se.lth.cs.tycho.parsing.xdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.ToolValueAttribute;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.InstanceDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.Expression;
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
		ImmutableList<PortDecl> inputPorts = getPorts(doc, true);
		ImmutableList<PortDecl> outputPorts = getPorts(doc, false);
		NlNetwork network = new NlNetwork(ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(), inputPorts, outputPorts, instances, connections);
		EntityDecl networkDecl = EntityDecl.global(Availability.PUBLIC, qid.getLast().toString(), network);
		return new NamespaceDecl(qid.getButLast(), ImmutableList.empty(), ImmutableList.empty(), ImmutableList.of(networkDecl), ImmutableList.empty());
	}

	private ImmutableList<PortDecl> getPorts(Document doc, boolean isInput) {
		String kind = isInput ? "Input" : "Output";
		ImmutableList.Builder<PortDecl> builder = ImmutableList.builder();
		for (Element port : selectChildren(doc.getDocumentElement(), "Port")) {
			if (port.getAttribute("kind").equalsIgnoreCase(kind)) {
				builder.add(new PortDecl(port.getAttribute("name")));
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
				}
				break;
			case "UnaryOp":
				Element op = selectChild(expr, "Op");
				String operation = op.getAttribute("name");
				Expression operand = buildExpression(selectChild(expr, "Expr"));
				return new ExprUnaryOp(operation, operand);
		}
		throw new UnsupportedOperationException("All parameter kinds are not implemented.");
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
