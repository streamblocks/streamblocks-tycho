package net.opendf.backend.c.att;

import java.lang.reflect.Method;
import java.util.List;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;

public class Utilities extends Module<Utilities.Required> {

	public interface Required {

		int lookupIndex(Object node, Object node2);

	}

	@Inherited
	public Object parent(Object p) {
		return p;
	}

	@Synthesized
	public int index(Object node) {
		return get().lookupIndex(node, node);
	}
	
	@Inherited
	public int lookupIndex(Object node, Object element) {
		Class<?> type = node.getClass();
		for (Method m : type.getMethods()) {
			if (m.getParameterTypes().length == 0 && List.class.isAssignableFrom(m.getReturnType())) {
				try {
					List<?> result = (List<?>) m.invoke(node);
					int index = 0;
					for (Object e : result) {
						if (e == element) {
							return index;
						}
						index += 1;
					}
				} catch (Throwable e) {
				}
			}
		}
		return -1;
	}

}
