/* 
 *  @author Per Andersson<Per.Andersson@cs.lth.se>, Lund University
 *  Example, the expression a+b*c is represented as:
 *    operands = {a, b, c}
 *    operations = {+, *}
 */

package net.opendf.ir.common;



public class ExprBinaryOp extends Expression {

    public void accept(ExpressionVisitor v) {
        v.visitExprBinaryOp(this);
    }

    public ExprBinaryOp(java.lang.String[] operations, Expression[] operands) {
    	assert(operations.length == operands.length-1);
    	this.operations = operations;
    	this.operands = operands;
    }
    public java.lang.String[] getOperations(){
    	return operations;
    }
    
    public Expression[] getOperands(){
    	return operands;
    }

    private java.lang.String[] operations;
    private Expression[] operands;
}
