package net.opendf.util.dom;

import org.w3c.dom.Element;

public class OrPredicate implements ElementPredicate {

	public boolean test(Element e) {
		for (ElementPredicate p : predicates) {
			if (p.test(e))
				return true;
		}
		return false;
	}
	
	
	//
	//  Ctor
	//
	
	public OrPredicate(ElementPredicate p1, ElementPredicate p2) {
		this (new ElementPredicate [] {p1, p2});
	}
	
	public OrPredicate(ElementPredicate p1, ElementPredicate p2, ElementPredicate p3) {
		this (new ElementPredicate [] {p1, p2, p3});
	}
	
	public OrPredicate(ElementPredicate [] predicates) {
		this.predicates = predicates;
	}
		
	private ElementPredicate []  predicates;

}
