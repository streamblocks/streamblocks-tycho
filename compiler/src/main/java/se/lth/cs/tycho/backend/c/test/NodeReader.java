package se.lth.cs.tycho.backend.c.test;

import java.io.File;

import se.lth.cs.tycho.ir.entity.PortContainer;

public interface NodeReader {
	public PortContainer fromId(String id);
	public PortContainer fromFile(File file);
}
