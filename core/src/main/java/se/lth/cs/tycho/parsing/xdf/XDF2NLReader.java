package se.lth.cs.tycho.parsing.xdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.ToolValueAttribute;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class XDF2NLReader {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public NamespaceDecl read(InputStream is, QID qid) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(is);
		return buildNetwork(document, qid);
	}

	private NamespaceDecl buildNetwork(Document doc, QID qid) {
		ImmutableList.Builder<EntityDecl> entities = ImmutableList.builder();
		ImmutableList.Builder<InstanceDecl> instances = ImmutableList.builder();
		buildNodes(doc, qid.getButLast(), entities, instances);
		ImmutableList.Builder<StructureStatement> connections = ImmutableList.builder();
		buildConnections(doc, connections);
		ImmutableList<PortDecl> inputPorts = buildPorts(doc, true);
		ImmutableList<PortDecl> outputPorts = buildPorts(doc, false);
		NlNetwork network = new NlNetwork(ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(), inputPorts, outputPorts, instances.build(), connections.build(), ImmutableList.empty());
		EntityDecl entityDecl = EntityDecl.global(Availability.PUBLIC, qid.getLast().toString(), network);
		entities.add(entityDecl);
		return new NamespaceDecl(qid.getButLast(), ImmutableList.empty(), ImmutableList.empty(), entities.build(), ImmutableList.empty());
	}

	private ImmutableList<PortDecl> buildPorts(Document doc, boolean isInput) {
		String kind = isInput ? "Input" : "Output";
		ImmutableList.Builder<PortDecl> builder = ImmutableList.builder();
		for (Element port : selectChildren(doc.getDocumentElement(), "Port")) {
			if (port.getAttribute("kind").equalsIgnoreCase(kind)) {
				builder.add(new PortDecl(port.getAttribute("name")));
			}
		}
		return builder.build();
	}

	private void buildConnections(Document input, ImmutableList.Builder<StructureStatement> connections) {
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
	}

	private void buildNodes(Document input, QID ns, ImmutableList.Builder<EntityDecl> imports, ImmutableList.Builder<InstanceDecl> entities) {
		ImportManager manager = new ImportManager(ns);
		for (Element instance : selectChildren(input.getDocumentElement(), "Instance")) {
			String instanceName = instance.getAttribute("id");
			QID entityQid = QID.parse(selectChild(instance, "Class").getAttribute("name"));
			List<Parameter<Expression>> parameters = new ArrayList<>();
			for (Element parameter : selectChildren(instance, "Parameter")) {
				String name = parameter.getAttribute("name");
				Expression expr = buildExpression(selectChild(parameter, "Expr"));
				Parameter<Expression> assignment = Parameter.of(name, expr);
				parameters.add(assignment);
			}
			manager.add(instanceName, entityQid, parameters);
		}
		manager.generate(imports, entities);
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

	private static class ImportManager {
		private final Map<String, QID> entities;
		private final Map<String, List<Parameter<Expression>>> instanceParameters;
		private final QID currentNamespace;

		public ImportManager(QID currentNamespace) {
			this.entities = new LinkedHashMap<>();
			this.currentNamespace = currentNamespace;
			this.instanceParameters = new LinkedHashMap<>();
		}

		public void add(String instance, QID entity, List<Parameter<Expression>> parameters) {
			entities.put(instance, entity);
			instanceParameters.put(instance, parameters);
		}

		public void generate(Consumer<EntityDecl> importConsumer, Consumer<InstanceDecl> instanceConsumer) {
			Set<String> usedNames = new HashSet<>();
			Map<QID, String> localName = new HashMap<>();
			for (QID entity : entities.values()) {
				if (entity.getButLast().equals(currentNamespace)) {
					String name = entity.getLast().toString();
					usedNames.add(name);
					localName.put(entity, name);
				}
			}
			for (QID entity : entities.values()) {
				if (!localName.containsKey(entity)) {
					String name = generateName(usedNames, entity.getLast().toString());
					localName.put(entity, name);
					EntityDecl importDecl = EntityDecl.importDecl(Availability.LOCAL, name, entity);
					importConsumer.accept(importDecl);
				}
			}

			for (String instance : entities.keySet()) {
				String entityName = localName.get(entities.get(instance));
				EntityInstanceExpr instanceExpr = new EntityInstanceExpr(entityName, instanceParameters.get(instance));
				instanceConsumer.accept(new InstanceDecl(instance, instanceExpr));
			}
		}

		private String generateName(Set<String> usedNames, String base) {
			String result = base;
			int i = 0;
			while (usedNames.contains(result)) {
				result = base + "_" + i;
				i = i + 1;
			}
			return result;
		}
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
