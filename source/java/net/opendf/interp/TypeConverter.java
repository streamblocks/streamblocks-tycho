package net.opendf.interp;

import net.opendf.interp.exception.CALRuntimeException;
import net.opendf.interp.values.Collection;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.List;
import net.opendf.interp.values.Procedure;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;

public enum TypeConverter {
	
	instance;
	
	public static TypeConverter getInstance() {
		return instance;
	}
	
	public boolean getBoolean(RefView r) throws CALRuntimeException { return r.getLong() != 0; }
	public void setBoolean(Ref r, boolean v) { r.setLong(v ? 1 : 0); }
	
	public Function getFunction(RefView r) throws CALRuntimeException { return (Function) r.getValue(); }
	public void setFunction(Ref r, Function v) { r.setValue(v); }
	
	public Procedure getProcedure(RefView r) throws CALRuntimeException { return (Procedure) r.getValue(); }
	public void setProcedure(Ref r, Procedure v) { r.setValue(v); }
	
	public List getList(RefView r) throws CALRuntimeException { return (List) r.getValue(); }
	public void setList(Ref r, List v) { r.setValue(v); }

	public Collection getCollection(RefView r) throws CALRuntimeException { return (Collection) r.getValue(); }
	public void setCollection(Ref r, Collection v) { r.setValue(v); }
	
	public int getInt(RefView r) throws CALRuntimeException { return (int) r.getLong(); }
	public void setInt(Ref r, int v) { r.setLong(v); }


}
