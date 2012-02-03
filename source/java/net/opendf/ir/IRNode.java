package net.opendf.ir;

import java.util.HashMap;

public interface IRNode {
    public void setAttribute(String key, Object value);

    public Object getAttribute(String key);    
}
