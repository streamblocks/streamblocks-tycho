package se.lth.cs.tycho.ir.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javarag.TreeTraverser;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.entity.PortContainer;

public class IRNodeTraverser implements TreeTraverser<Object> {

	@Override
	public Iterable<? extends Object> getChildren(Object root) {
		if (root instanceof Connection) {
			Connection c = (Connection) root;
			return Arrays.asList(c.getSrcPort(), c.getDstPort());
		}
		if (root instanceof NamespaceDecl) {
			NamespaceDecl ns = (NamespaceDecl) root;
			List<IRNode> children = new ArrayList<>();
			children.addAll(ns.getImports());
			children.addAll(ns.getAllDecls());
			children.addAll(ns.getNamespaceDecls());
			return children;
		}
		Class<?> type = root.getClass();
		List<Object> children = new ArrayList<>();
		for (Method m : type.getMethods()) {
			if (!Modifier.isPublic(m.getModifiers()))
				continue;
			if (m.getParameterTypes().length > 0)
				continue;
			if (m.getName().equals("getLocation") && m.getReturnType() == NamespaceDecl.class)
				continue;

			Class<?> returnType = m.getReturnType();
			if (IRNode.class.isAssignableFrom(returnType) || Iterable.class.isAssignableFrom(returnType)
					|| Entry.class.isAssignableFrom(returnType) || PortContainer.class.isAssignableFrom(returnType)
					|| Node.Identifier.class.isAssignableFrom(returnType)
					|| Map.class.isAssignableFrom(returnType)
					|| Parameter.class.isAssignableFrom(returnType)) {
				try {
					Object child = m.invoke(root);
					addChildren(children, child);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					System.err.println("WARNING: Could not add all children.");
				}
			}
		}
		return children;
	}

	private void addChildren(Collection<Object> list, Object child) {
		if (child instanceof Parameter) {
			Parameter<?> p = (Parameter<?>) child;
			addChildren(list, p.getValue());
		} else if (child instanceof IRNode) {
			list.add((IRNode) child);
		} else if (child instanceof Node.Identifier) {
			list.add(child);
		} else if (child instanceof Entry) {
			Entry<?, ?> e = (Entry<?, ?>) child;
			addChildren(list, e.getKey());
			addChildren(list, e.getValue());
		} else if (child instanceof Iterable) {
			Iterable<?> i = (Iterable<?>) child;
			for (Object o : i) {
				addChildren(list, o);
			}
		} else if (child instanceof Map) {
			for (Entry<?,?> entry : ((Map<?,?>) child).entrySet()) {
				addChildren(list, entry.getKey());
				addChildren(list, entry.getValue());
			}
		}
	}

}
