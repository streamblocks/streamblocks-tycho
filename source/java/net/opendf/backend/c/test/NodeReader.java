package net.opendf.backend.c.test;

import java.io.File;

import net.opendf.ir.entity.PortContainer;

public interface NodeReader {
	public PortContainer fromId(String id);
	public PortContainer fromFile(File file);
}
