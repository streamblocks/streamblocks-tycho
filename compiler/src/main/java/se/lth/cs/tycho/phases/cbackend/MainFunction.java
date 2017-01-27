package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Compiler;
import se.lth.cs.tycho.comp.Namespaces;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Module
public interface MainFunction {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default MainNetwork mainNetwork() {
		return backend().mainNetwork();
	}

	default void generateCode() {

		CompilationTask task = backend().task();
		String targetName = Namespaces.findEntities(task, task.getIdentifier())
				.findFirst().get().getOriginalName();
		//System.out.println("There are " + Namespaces.findEntities(task, task.getIdentifier()).count() + "Entities");
		System.out.println(targetName);
		Path path = backend().context().getConfiguration().get(Compiler.targetPath);
		List<VarDecl> varDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getVarDecls().stream()).collect(Collectors.toList());
		List<EntityDecl> entityDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getEntityDecls().stream()).collect(Collectors.toList());
		//List<StarImport>
		System.out.println("There are " + varDecls.size() + " varDecls");
		System.out.println("There are " + entityDecls.size() + " entityDecls");
		for (int i =0; i < entityDecls.size(); i++) {
			System.out.println("Actor: " + entityDecls.get(i).getName());
			Path target = path.resolve(entityDecls.get(i).getName() + ".c");
			emitter().open(target);
			include();
			backend().global().globalVariables(varDecls);
			List<EntityDecl> l = new ArrayList<EntityDecl>();
			l.add(entityDecls.get(i));
			backend().structure().actorDecls(l);
			mainNetwork().main(task.getNetwork(), entityDecls.get(i).getName());
			emitter().close();
		}

		generateHostProgram();
		generateMakefile();
	}

	default void generateMakefile() {
		Path target = backend().context().getConfiguration().get(Compiler.targetPath);
		Path host_target = target.resolve("Makefile");
		PrintWriter writer;
		try {
			writer = new PrintWriter(Files.newBufferedWriter(host_target));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String listLine;
		try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/Makefile")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
				writer.flush();
			}

			ArrayList<ActorInfo> al = loadActorInfo();
			listLine = "LIST = ";
			for(ActorInfo a : al) {
				listLine += a.getName() + " ";
			}
		} catch (IOException e) {
			throw new Error(e);
		}

		List<String> fileContent = null;
		try {
			fileContent = new ArrayList<>(Files.readAllLines(host_target, StandardCharsets.UTF_8));

			for (int i = 0; i < fileContent.size(); i++) {
				if (fileContent.get(i).equals("LIST=")) {
					fileContent.set(i, listLine);
					break;
				}
			}

			Files.write(host_target, fileContent, StandardCharsets.UTF_8);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	default void generateHostProgram() {
		Path target = backend().context().getConfiguration().get(Compiler.targetPath);
		Path host_target = target.resolve("host.cpp");
		PrintWriter writer;
		try {
			writer = new PrintWriter(Files.newBufferedWriter(host_target));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		include_host("host_prolog", writer);
		loadCores(writer);
		include_host("host_epilog", writer);
		writer.close();
	}

	public class ActorInfo {
		public String name;
		public int row;
		public int col;

		public ActorInfo() {
			name = "";
			row = 0;
			col = 0;
		}

		public ActorInfo(String n, int r, int c) {
			name = n;
			row = r;
			col = c;
		}

		public String getName() {
			return name;
		}
	}

	public static ArrayList<ActorInfo> loadActorInfo() {
		ArrayList<ActorInfo> al = new ArrayList<ActorInfo>();
		try {
			InputStream fXmlFile = ClassLoader.getSystemResourceAsStream("config.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Configuration");

			Node nNode = nList.item(0);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				NodeList actors = doc.getElementsByTagName("actor_info");
				int count = 0;
				while(count < actors.getLength()) {
					// load each actor
					Element actor = (Element) actors.item(count);
					String actor_name = actor.getElementsByTagName("name").item(0).getTextContent();
					int row = Integer.parseInt(actor.getElementsByTagName("row").item(0).getTextContent());
					int col = Integer.parseInt(actor.getElementsByTagName("col").item(0).getTextContent());
					//System.out.println("Name is " + actor_name + " row = " + row + " col " + col);
					ActorInfo a = new ActorInfo(actor_name, row, col);
					al.add(a);
					count++;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return al;
	}


	public static void loadCores(PrintWriter writer) {
		try {
			//File fXmlFile = new File("/home/vivek/work/xml/config.xml");
			InputStream fXmlFile = ClassLoader.getSystemResourceAsStream("config.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Configuration");

			Node nNode = nList.item(0);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				NodeList actors = doc.getElementsByTagName("actor_info");
				int count = 0;
				while(count < actors.getLength()) {
					// load each actor
					Element actor = (Element) actors.item(count);
					String actor_name = actor.getElementsByTagName("name").item(0).getTextContent();
					int row = Integer.parseInt(actor.getElementsByTagName("row").item(0).getTextContent());
					int col = Integer.parseInt(actor.getElementsByTagName("col").item(0).getTextContent());
					//System.out.println("Name is " + actor_name + " row = " + row + " col " + col);
					writer.println("    e_load_group(\"" + actor_name + ".srec\", &edev, " + row + ", " + col + ", 1, 1, E_TRUE);");
					count++;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static int getBufferSize() {
		int bufferSize = 0;
		try {
			//File fXmlFile = new File("/home/vivek/work/xml/config.xml");
			InputStream fXmlFile = ClassLoader.getSystemResourceAsStream("config.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Configuration");

			Node nNode = nList.item(0);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				bufferSize = Integer.parseInt(doc.getElementsByTagName("BUFFER_SIZE").item(0).getTextContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bufferSize;
	}


	default void include() {
		emitter().emitRawLine("\n");
		emitter().emit("%s", "#ifndef BUFFER_SIZE");
		emitter().emit("#define BUFFER_SIZE %d", getBufferSize());
		emitter().emit("%s", "#endif");
		emitter().emitRawLine("\n");
		try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/included_fifo.c")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			reader.lines().forEach(emitter()::emitRawLine);
		} catch (IOException e) {
			throw new Error(e);
		}

	}

	default void include_host(String name, PrintWriter writer) {
		if(name == "host_prolog") {
			try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/host_prolog")) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					writer.println(line);
				}
			} catch (IOException e) {
				throw new Error(e);
			}
		} else if(name == "host_epilog") {
			try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/host_epilog")) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					writer.println(line);
				}
			} catch (IOException e) {
				throw new Error(e);
			}
		}
	}

}
