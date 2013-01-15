package net.opendf.interp;

import net.opendf.ir.common.Decl;

public interface Declarator {
	public int declare(Decl decl, Environment env);
}
