package net.opendf.backend.c.att;

import java.io.PrintWriter;

import net.opendf.ir.entity.am.ICall;
import net.opendf.ir.entity.am.ITest;

public class ControllersWithStats extends Controllers {
	@Override
	public void controllerInstruction(ICall c, PrintWriter w) {
		w.println("printf(\"transition " + c.T() + "\\n\");");
		super.controllerInstruction(c, w);
	}
	
	@Override
	public void controllerInstruction(ITest t, PrintWriter w) {
		w.println("printf(\"condition " + t.C() + "\\n\");");
		super.controllerInstruction(t, w);
	}
}
