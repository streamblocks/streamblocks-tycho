package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.decl.LocationKind;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.OptionalInt;

@Module
public interface Constants {

	ModuleKey<Constants> key = new ModuleKey<Constants>() {
		@Override
		public Constants createInstance(CompilationTask unit, AttributeManager manager) {
			return MultiJ.from(Constants.class)
					.bind("names").to(manager.getAttributeModule(Names.key, unit))
					.instance();
		}
	};

	@Binding(BindingKind.INJECTED)
	Names names();

	default OptionalInt intValue(Expression e) {
		return OptionalInt.empty();
	}

	default OptionalInt intValue(ExprVariable var) {
		VarDecl decl = names().declaration(var.getVariable());
		if (decl.isConstant() &&
				(decl.getLocationKind() == LocationKind.GLOBAL || decl.getLocationKind() == LocationKind.LOCAL) &&
				decl.getValue() != null) {
			return intValue(decl.getValue());
		} else {
			return OptionalInt.empty();
		}
	}

	default OptionalInt intValue(ExprLiteral literal) {
		switch (literal.getKind()) {
			case Integer: try {
				String text = literal.getText();
				int radix = 10;
				if (text.startsWith("0x")) {
					text = text.substring(2);
					radix = 16;
				}
				return OptionalInt.of(Integer.parseInt(text, radix));
			} catch (NumberFormatException e) {
				return OptionalInt.empty();
			}
			default: {
				return OptionalInt.empty();
			}
		}
	}

	default OptionalInt intValue(ExprUnaryOp unary) {
		OptionalInt v = intValue(unary.getOperand());
		if (v.isPresent()) {
			int vv = v.getAsInt();
			switch (unary.getOperation()) {
				case "-": return OptionalInt.of(-vv);
				default: return OptionalInt.empty();
			}
		} else {
			return OptionalInt.empty();
		}
	}

	default OptionalInt intValue(ExprBinaryOp binary) {
		OptionalInt a = intValue(binary.getOperands().get(0));
		OptionalInt b = intValue(binary.getOperands().get(1));
		if (a.isPresent() && b.isPresent()) {
			int aa = a.getAsInt();
			int bb = b.getAsInt();
			switch(binary.getOperations().get(0)) {
				case "+": return OptionalInt.of(aa + bb);
				case "-": return OptionalInt.of(aa - bb);
				case "*": return OptionalInt.of(aa * bb);
				case "/": return OptionalInt.of(aa / bb);
				default: return OptionalInt.empty();
			}
		} else {
			return OptionalInt.empty();
		}
	}
}
