/* 
BEGINCOPYRIGHT JWJ
ENDCOPYRIGHT
*/

package net.opendf.ir.common;

import net.opendf.ir.common.Decl.DeclKind;

public class DeclVar extends Decl {
	
	@Override
	public DeclKind getKind() {
		return DeclKind.value;
	}

	@Override
	public void  accept(DeclVisitor v) {
		v.visitDeclVar(this);
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
