package se.lth.cs.tycho.parsing.xdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.ToolValueAttribute;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XDF2NLReader {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public NamespaceDecl read(InputStream is, QID qid) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(is);
		return buildNetwork(document, qid);
	}

	private NamespaceDecl buildNetwork(Document doc, QID qid) {
		ImmutableList.Builder<EntityDecl> entities = ImmutableList.builder();
		ImmutableList.Builder<Map.Entry<String, EntityExpr>> instances = ImmutableList.builder();
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
		ImmutableList.Builder<StructureStatement> result = ImmutableList.builder();
		for (Element conn : selectChildren(input.getDocumentElement(), "Connection")) {
			String src = conn.getAttribute("src");
			Port srcPort = new Port(conn.getAttribute("src-port"));
			String dst = conn.getAttribute("dst");
			Port dstPort = new Port(conn.getAttribute("dst-port"));
			ImmutableList<ToolAttribute> attributes;
			String bufferSize = conn.getAttribute("buffer-size");
			if (bufferSize.equals("")) {
				attributes = ImmutableList.empty();
			} else {
				attributes = ImmutableList.of(new ToolValueAttribute("buffer-size", new ExprLiteral(ExprLiteral.Kind.Integer, bufferSize)));
			}
			result.add(new StructureConnectionStmt(new PortReference(src, ImmutableList.empty(), srcPort.getName()), new PortReference(dst, ImmutableList.empty(), dstPort.getName()), attributes));
		}
	}

	private void buildNodes(Document input, QID ns, ImmutableList.Builder<EntityDecl> imports, ImmutableList.Builder<Map.Entry<String, EntityExpr>> entities) {
		Set<QID> imported = new HashSet<>();
		Set<String> entityNames = new HashSet<>();
		for (Element instance : selectChildren(input.getDocumentElement(), "Instance")) {
			String instanceName = instance.getAttribute("id");
			QID entityQid = QID.parse(selectChild(instance, "Class").getAttribute("name"));
			String entityName = uniqueName(entityNames, entityQid.getLast().toString());
			if (!entityQid.getButLast().equals(ns) && imported.add(entityQid)) {
				imports.add(EntityDecl.importDecl(Availability.LOCAL, entityName, entityQid));
			}
			entities.add(ImmutableEntry.of(instanceName, new EntityInstanceExpr(entityName, ImmutableList.empty(), ImmutableList.empty())));
		}
	}

	private String uniqueName(Set<String> existingNames, String base) {
		String name;
		if (existingNames.contains(base)) {
			int i = 0;
			do {
				name = base + "_" + i;
			} while (existingNames.contains(name));
		} else {
			name = base;
		}
		existingNames.add(name);
		return name;
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
