package net.opendf.backend.c;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import net.opendf.ir.IRNode;
import net.opendf.ir.common.PortContainer;
import javarag.TreeTraverser;

public class IRNodeTraverser implements TreeTraverser<IRNode> {

	@Override
	public Iterable<? extends IRNode> getChildren(IRNode root) {
		Class<? extends IRNode> type = root.getClass();
		List<IRNode> children = new ArrayList<>();
		for (Method m : type.getMethods()) {
			if (!Modifier.isPublic(m.getModifiers()))
				continue;
			if (m.getParameterTypes().length > 0)
				continue;

			Class<?> returnType = m.getReturnType();
			if (IRNode.class.isAssignableFrom(returnType) || Iterable.class.isAssignableFrom(returnType)
					|| Entry.class.isAssignableFrom(returnType) || PortContainer.class.isAssignableFrom(returnType)) {
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

	private void addChildren(Collection<IRNode> list, Object child) {
		if (child instanceof IRNode) {
			list.add((IRNode) child);
		} else if (child instanceof Entry) {
			Entry<?, ?> e = (Entry<?, ?>) child;
			addChildren(list, e.getKey());
			addChildren(list, e.getValue());
		} else if (child instanceof Iterable) {
			Iterable<?> i = (Iterable<?>) child;
			for (Object o : i) {
				addChildren(list, o);
			}
		}
	}

}
