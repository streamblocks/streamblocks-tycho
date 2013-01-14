/* 
BEGINCOPYRIGHT JWJ
ENDCOPYRIGHT
*/

package net.opendf.ir.common;


public class DeclVar extends Decl {
	
	@Override
	public DeclKind getKind() {
		return DeclKind.value;
	}

	@Override
	public <R,P> R accept(DeclVisitor<R,P> v, P p) {
		return v.visitDeclVar(this, p);
	}

	public Expression getInitialValue() {
        return initialValue;
    }
    
    public TypeExpr getType() {
        return typeExpr;
    }
    
    public boolean  isAssignable() {
    	return isAssignable;
    }
    
    //
    // Ctor
    //
    
	public DeclVar(TypeExpr type, String name, NamespaceDecl namespace) {
		this(type, name, namespace, null, true);
	}

    public DeclVar(TypeExpr type, String name, NamespaceDecl namespace,
    		Expression initialValue, boolean isAssignable) {
    	
    	super(name, namespace);
    	
        this.typeExpr = type;
        this.initialValue = initialValue;
        this.isAssignable = isAssignable;
    }



    private Expression  initialValue;
    private TypeExpr    typeExpr;
    private boolean     isAssignable;
}
