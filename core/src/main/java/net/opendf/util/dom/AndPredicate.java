package net.opendf.util.dom;

import org.w3c.dom.Element;

public class AndPredicate implements ElementPredicate {

	public boolean test(Element e) {
		for (ElementPredicate p : predicates) {
			if (!p.test(e))
				return false;
		}
		return true;
	}
	
	
	//
	//  Ctor
	//
	
	public AndPredicate(ElementPredicate p1, ElementPredicate p2) {
		this (new ElementPredicate [] {p1, p2});
	}
	
	public AndPredicate(ElementPredicate p1, ElementPredicate p2, ElementPredicate p3) {
		this (new ElementPredicate [] {p1, p2, p3});
	}
	
	public AndPredicate(ElementPredicate [] predicates) {
		this.predicates = predicates;
	}
		
	private ElementPredicate []  predicates;

}
