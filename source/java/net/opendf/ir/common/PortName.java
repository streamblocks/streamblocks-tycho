package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

public class PortName extends AbstractIRNode {
	
	
	public  String []  toStringArray() { return name; }
	
	public  PortName  append(PortName n) {
		String [] r = new String [size() + n.size()];
		
		for (int i = 0; i < size(); i++)
			r[i] = name[i];
		for (int i = 0; i < n.size(); i++)
			r[size() + i] = n.name[i];
		
		return new PortName(r);
	}
	
	public int  size() { return (name == null) ? 0 : name.length; }
	
	@Override
	public int hashCode() {
		int n = 0;
		for (String s : name) {
			n += s.hashCode();
		}
		return n;
	}
	
	@Override
	public boolean  equals(Object a) {
		if (a instanceof PortName) {
			PortName p = (PortName)a;
			if (size() != p.size())
				return false;
			for (int i = 0; i < size(); i++) {
				if (!name[i].equals(p.name[i]))
					return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String n : name) {
			if (!first) sb.append(PortDecl.portNameSeparator);
			first = false;
			sb.append(n);
		}
		return sb.toString();
	}

	//
	//  Ctor
	//
	
	public PortName() {
		this (new String[0]);
	}
	
	public PortName(String s) {
		this(new String [] { s });
	}
	
	private PortName(String [] name) {
		
		assert name != null;
		
		this.name = name;
	}
	
	private String []  name;

}
