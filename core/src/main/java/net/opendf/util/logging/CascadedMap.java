/* 
BEGINCOPYRIGHT X
	
	Copyright (c) 2007, Xilinx Inc.
	All rights reserved.
	
	Redistribution and use in source and binary forms, 
	with or without modification, are permitted provided 
	that the following conditions are met:
	- Redistributions of source code must retain the above 
	  copyright notice, this list of conditions and the 
	  following disclaimer.
	- Redistributions in binary form must reproduce the 
	  above copyright notice, this list of conditions and 
	  the following disclaimer in the documentation and/or 
	  other materials provided with the distribution.
	- Neither the name of the copyright holder nor the names 
	  of its contributors may be used to endorse or promote 
	  products derived from this software without specific 
	  prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
	CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
	INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
	MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
	HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
	OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	
ENDCOPYRIGHT
*/

package net.opendf.util.logging;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CascadedMap<K, V> extends AbstractMap<K, V> {
	
	//
	//  AbstractMap
	//
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K,V>> s = new HashSet<Entry<K,V>>(localMap.entrySet());

		if (this.getParent() != null) {
			for (Entry<K, V> e : parent.entrySet()) {
				if (!localMap.containsKey(e.getKey())) {
					s.add(e);
				}
			}
		}
		
		return Collections.unmodifiableSet(s);
	}
	
	@Override
	public V put(K k, V v) {
		return localMap.put(k, v);
	}

	@Override
	public V get(Object k) {
		if (localMap.containsKey(k)) {
			return localMap.get(k);
		} else if (parent != null) {
			return parent.get(k);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean containsKey(Object k) {
		if (localMap.containsKey(k))
			return true;
		return parent != null && parent.containsKey(k); 
	}

	@Override
	public Set<K>  keySet() {
		Set<K> s = new HashSet<K>(localMap.keySet());
		if (parent != null) {
			s.addAll(parent.keySet());
		}
		return s;
	}	
	
	//
	//  CascadedMap
	//
	
	public Map<K, V>  getLocalMap() {
		return localMap;
	}
	
	public Map<K, V>  getParent() {
		return parent;
	}
	
	public CascadedMap(Map<K, V> parent) {
		this(Collections.EMPTY_MAP, parent);
	}
	
	public CascadedMap(Map<K, V> m, Map<K, V> p) {
		this.parent = p;
		this.localMap = new HashMap<K, V>(m);
	}
		
	private Map<K, V>  			localMap;
	private Map<K, V>  			parent;
}
