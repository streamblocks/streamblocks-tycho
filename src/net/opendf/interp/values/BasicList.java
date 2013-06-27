package net.opendf.interp.values;

import java.util.ArrayList;

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
		list.get(i).assignTo(r);
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
