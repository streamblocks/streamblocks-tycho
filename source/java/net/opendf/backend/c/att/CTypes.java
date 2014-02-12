package net.opendf.backend.c.att;

import java.util.Map.Entry;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.CArrayType;
import net.opendf.backend.c.CNamedType;
import net.opendf.backend.c.CType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.net.Connection;

public class CTypes extends Module<CTypes.Required> {

	public interface Required {

		CType ctype(TypeExpr value);

		String simpleExpression(Expression value);

		CType ctype(Expression initialValue);

		PortDecl declaration(Port port);

		Integer constantInteger(Expression expr);

	}
	
	@Synthesized
	public CType ctype(DeclVar varDecl) {
		if (varDecl.getType() != null) {
			return get().ctype(varDecl.getType());
		} else {
			return get().ctype(varDecl.getInitialValue());
		}
	}

	@Synthesized
	public CType ctype(Connection conn) {
		PortDecl src = get().declaration(conn.getSrcPort());
		if (src.getType() != null) {
			return get().ctype(src.getType());
		}
		PortDecl dst = get().declaration(conn.getDstPort());
		if (dst.getType() != null) {
			return get().ctype(dst.getType());
		}
		return null;
	}
	
	@Synthesized
	public CType ctype(TypeExpr type) {
		switch (type.getName()) {
		case "int":
		case "uint":
			Integer size = null;
			for (Entry<String, Expression> entry : type.getValueParameters()) {
				if (entry.getKey().equals("size")) {
					size = get().constantInteger(entry.getValue());
					break;
				}
			}
			if (size != null) {
				if (size <= 8) {
					return new CNamedType(type.getName()+"8_t");
				} else if (size <= 16) {
					return new CNamedType(type.getName()+"16_t");
				} else if (size <= 32) {
					return new CNamedType(type.getName()+"32_t");
				} else if (size <= 64) {
					return new CNamedType(type.getName()+"64_t");
				}
			}
			return new CNamedType(type.getName()+"32_t");
		case "bool":
			return new CNamedType("_Bool");
		case "List":
			CType elementType = null;
			for (Entry<String, TypeExpr> entry : type.getTypeParameters()) {
				if (entry.getKey().equals("type")) {
					elementType = get().ctype(entry.getValue());
				}
			}
			String listSize = null;
			for (Entry<String, Expression> entry : type.getValueParameters()) {
				if (entry.getKey().equals("size")) {
					listSize = get().simpleExpression(entry.getValue());
				}
			}
			return new CArrayType(elementType, listSize);
		default:
			return null;
		}
	}
	
	@Synthesized
	public CType ctype(ExprInput input) {
		PortDecl port = get().declaration(input.getPort());
		CType type = get().ctype(port.getType());
		if (input.hasRepeat()) {
			int r = input.getRepeat();
			return new CArrayType(type, Integer.toString(r));
		} else {
			return type;
		}
	}

}
