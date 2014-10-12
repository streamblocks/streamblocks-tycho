package se.lth.cs.tycho.parsing.xdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;
import se.lth.cs.tycho.ir.entity.xdf.XDFConnection;
import se.lth.cs.tycho.ir.entity.xdf.XDFInstance;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class XDFReader {
	DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();

	public XDFNetwork read(InputStream is) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = fact.newDocumentBuilder();
		Document document = builder.parse(is);
		return buildXDF(document);
	}

	private XDFNetwork buildXDF(Document doc) {
		ImmutableList<XDFInstance> nodes = buildNodes(doc);
		ImmutableList<XDFConnection> conns = buildConnections(doc, nodes);
		ImmutableList<PortDecl> inputPorts = buildPorts(doc, true);
		ImmutableList<PortDecl> outputPorts = buildPorts(doc, false);
		return new XDFNetwork(inputPorts, outputPorts, nodes, conns);
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

	private ImmutableList<XDFConnection> buildConnections(Document input, List<XDFInstance> nodes) {
		ImmutableList.Builder<XDFConnection> result = ImmutableList.builder();
		for (Element conn : selectChildren(input.getDocumentElement(), "Connection")) {
			String src = conn.getAttribute("src");
			Port srcPort = new Port(conn.getAttribute("src-port"));
			String dst = conn.getAttribute("dst");
			Port dstPort = new Port(conn.getAttribute("dst-port"));
			result.add(new XDFConnection(src, srcPort, dst, dstPort));
		}
		return result.build();
	}

	private ImmutableList<XDFInstance> buildNodes(Document input) {
		ImmutableList.Builder<XDFInstance> result = ImmutableList.builder();
		for (Element instance : selectChildren(input.getDocumentElement(), "Instance")) {
			String id = instance.getAttribute("id");
			String name = selectChild(instance, "Class").getAttribute("name");
			result.add(new XDFInstance(id, QID.parse(name)));
		}
		return result.build();
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
