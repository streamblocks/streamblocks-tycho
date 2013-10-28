package net.opendf.interp.values;

import java.util.ArrayList;

import net.opendf.interp.exception.CALIndexOutOfBoundsException;

public class BasicList implements List {
	private ArrayList<BasicRef> list;
	
	private BasicList(ArrayList<BasicRef> list) {
		this.list = list;
	}
	
	@Override
	public Iterator iterator() {
		return new Iterator();
	}

	@Override
	public void get(int i, Ref r) {
		if(i<0 || i>=list.size()){
			throw new CALIndexOutOfBoundsException("Index=" + i + ", Size=" + list.size());
		}
		list.get(i).assignTo(r);
	}

	@Override
	public Ref getRef(int i) {
		if(i<0 || i>=list.size()){
			throw new CALIndexOutOfBoundsException("Index=" + i + ", Size=" + list.size());
		}
		return list.get(i);
	}

	@Override
	public void set(int i, RefView r) {
		r.assignTo(list.get(i));
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List copy() {
		Builder b = new Builder();
		for (BasicRef r : list) b.add(r);
		return b.build();
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer("[");
		String sep = "";
		for(BasicRef v : list){
			sb.append(sep);
			sep = ", ";
			sb.append(v.toString());
		}
		sb.append("]");
		return sb.toString();
	}
	private class Iterator implements net.opendf.interp.values.Iterator {
		private int index = 0;

		@Override
		public Value getValue() {
			return list.get(index).getValue();
		}

		@Override
		public long getLong() {
			return list.get(index).getLong();
		}

		@Override
		public double getDouble() {
			return list.get(index).getDouble();
		}

		@Override
		public String getString() {
			return list.get(index).getString();
		}

		@Override
		public void assignTo(Ref r) {
			list.get(index).assignTo(r);
		}

		@Override
		public boolean finished() {
			return index >= list.size();
		}

		@Override
		public void advance() {
			index++;
		}
	}
	
	public static class Builder implements net.opendf.interp.values.Builder {
		
		private ArrayList<BasicRef> list = new ArrayList<BasicRef>();
		
		@Override
		public void add(RefView r) {
			BasicRef element = new BasicRef();
			r.assignTo(element);
			list.add(element);
		}

		@Override
		public BasicList build() {
			return new BasicList(list);
		}
		
	}
}
