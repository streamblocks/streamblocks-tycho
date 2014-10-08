package net.opendf.backend.c.att;

import java.util.Map.Entry;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.CArrayType;
import net.opendf.backend.c.CNamedType;
import net.opendf.backend.c.CType;
import net.opendf.ir.Port;
import net.opendf.ir.TypeExpr;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.expr.ExprInput;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.net.Connection;

public class CTypes extends Module<CTypes.Decls> {

	public interface Decls {

		@Synthesized
		CType ctype(Expression expr);

		@Synthesized
		CType ctype(TypeExpr value);

		String simpleExpression(Expression value);

		PortDecl declaration(Port port);

		Integer constantInteger(Expression expr);

	}

	@Synthesized
	public CType ctype(LocalVarDecl varDecl) {
		if (varDecl.getType() != null) {
			return e().ctype(varDecl.getType());
		} else {
			return e().ctype(varDecl.getInitialValue());
		}
	}

	@Synthesized
	public CType ctype(Connection conn) {
		PortDecl src = e().declaration(conn.getSrcPort());
		if (src.getType() != null) {
			return e().ctype(src.getType());
		}
		PortDecl dst = e().declaration(conn.getDstPort());
		if (dst.getType() != null) {
			return e().ctype(dst.getType());
		}
		return null;
	}

	@Synthesized
	public CType ctype(TypeExpr type) {
		switch (type.getName()) {
		case "int":
		case "uint":
			/*
			 * Integer size = null; for (Entry<String, Expression> entry :
			 * type.getValueParameters()) { if (entry.getKey().equals("size")) {
			 * size = e().constantInteger(entry.getValue()); break; } } if
			 * (size != null) { if (size <= 8) { return new
			 * CNamedType(type.getName()+"8_t"); } else if (size <= 16) { return
			 * new CNamedType(type.getName()+"16_t"); } else if (size <= 32) {
			 * return new CNamedType(type.getName()+"32_t"); } else if (size <=
			 * 64) { return new CNamedType(type.getName()+"64_t"); } }
			 */
			return new CNamedType(type.getName() + "32_t");
		case "bool":
			return new CNamedType("_Bool");
		case "List":
			CType elementType = null;
			for (Entry<String, TypeExpr> entry : type.getTypeParameters()) {
				if (entry.getKey().equals("type")) {
					elementType = e().ctype(entry.getValue());
				}
			}
			String listSize = null;
			for (Entry<String, Expression> entry : type.getValueParameters()) {
				if (entry.getKey().equals("size")) {
					listSize = e().simpleExpression(entry.getValue());
				}
			}
			return new CArrayType(elementType, listSize);
		default:
			return null;
		}
	}

	@Synthesized
	public CType ctype(ExprInput input) {
		PortDecl port = e().declaration(input.getPort());
		CType type = e().ctype(port.getType());
		if (input.hasRepeat()) {
			int r = input.getRepeat();
			return new CArrayType(type, Integer.toString(r));
		} else {
			return type;
		}
	}

}
