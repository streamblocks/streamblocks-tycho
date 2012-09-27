/* 
 *  @author Per Andersson<Per.Andersson@cs.lth.se>, Lund University
 */

package net.opendf.ir.common;



public class ExprUnaryOp extends Expression {

    public <R,P> R accept(ExpressionVisitor<R,P> v, P p) {
        return v.visitExprUnaryOp(this, p);
    }

    public ExprUnaryOp(java.lang.String operation, Expression operand) {
    	this.operation = operation;
    	this.operand = operand;
    }
    public java.lang.String getOperation(){
    	return operation;
    }
    
    public Expression getOperand(){
    	return operand;
    }

    private java.lang.String operation;
    private Expression operand;
}
