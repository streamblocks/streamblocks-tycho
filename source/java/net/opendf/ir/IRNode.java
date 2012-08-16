package net.opendf.ir;


public interface IRNode {
    public void setAttribute(String key, Object value);

    public Object getAttribute(String key);    
}
