package se.lth.cs.tycho.backend.c.att;

import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.analysis.types.BottomType;
import se.lth.cs.tycho.analysis.types.IntType;
import se.lth.cs.tycho.analysis.types.LambdaType;
import se.lth.cs.tycho.analysis.types.ListType;
import se.lth.cs.tycho.analysis.types.ProcType;
import se.lth.cs.tycho.analysis.types.SimpleType;
import se.lth.cs.tycho.analysis.types.TopType;
import se.lth.cs.tycho.analysis.types.Type;
import se.lth.cs.tycho.analysis.types.TypeVisitor;
import se.lth.cs.tycho.analysis.types.UserDefinedType;
import se.lth.cs.tycho.backend.c.CArrayType;
import se.lth.cs.tycho.backend.c.CNamedType;
import se.lth.cs.tycho.backend.c.CType;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.Expression;

public class CTypes extends Module<CTypes.Decls> {

	public interface Decls {

		@Synthesized
		CType ctype(Expression expr);

		Type type(Expression expr);

		PortDecl portDeclaration(Port srcPort);

		Type type(TypeExpr type);

		Type type(VarDecl varDecl);

	}

	private static final Converter CONVERTER = new Converter();

	private final static class Converter implements TypeVisitor<CType, Void> {

		@Override
		public CType visitBottomType(BottomType type, Void param) {
			throw new Error("NOT IMPLEMENTED");
		}

		@Override
		public CType visitIntType(IntType type, Void param) {
			String base = type.isSigned() ? "int" : "uint";
			int size = 64;
			if (type.hasSize()) {
				if (type.getSize() <= 8) {
					size = 8;
				} else if (type.getSize() <= 16) {
					size = 16;
				} else if (type.getSize() <= 32) {
					size = 32;
				}
			}
			return new CNamedType(base + size + "_t");
		}

		@Override
		public CType visitLambdaType(LambdaType type, Void param) {
			throw new Error("NOT IMPLEMENTED");
		}

		@Override
		public CType visitListType(ListType type, Void param) {
			String size = type.getSize().isPresent() ? Integer.toString(type.getSize().getAsInt()) : "";
			return new CArrayType(type.getElementType().accept(this, null), size);
		}

		@Override
		public CType visitProcType(ProcType type, Void param) {
			throw new Error("NOT IMPLEMENTED");
		}

		@Override
		public CType visitSimpleType(SimpleType type, Void param) {
			switch (type.getName()) {
			case "bool":
				return new CNamedType("_Bool");
			default:
				throw new Error("NOT IMPLEMENTED");
			}
		}

		@Override
		public CType visitTopType(TopType type, Void param) {
			throw new Error("NOT IMPLEMENTED");
		}

		@Override
		public CType visitUserDefinedType(UserDefinedType type, Void param) {
			throw new Error("NOT IMPLEMENTED");
		}

	}

	private CType convert(Type type) {
		return type.accept(CONVERTER, null);
	}
	
	public CType ctype(TypeExpr type) {
		return convert(e().type(type));
	}

	public CType ctype(Expression expr) {
		return convert(e().type(expr));
	}
	
	public CType ctype(VarDecl varDecl) {
		return convert(e().type(varDecl));
	}
	
	public CType ctype(Connection conn) {
		PortDecl src = e().portDeclaration(conn.getSrcPort());
		if (src.getType() != null) {
			return convert(e().type(src.getType()));
		}
		PortDecl dst = e().portDeclaration(conn.getDstPort());
		if (dst.getType() != null) {
			return convert(e().type(dst.getType()));
		}
		return null;
	}

}
