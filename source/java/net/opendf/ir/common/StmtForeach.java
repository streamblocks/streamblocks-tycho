package net.opendf.ir.common;

public class StmtForeach extends Statement {

    public <R,P> R accept(StatementVisitor<R,P> v, P p) {
        return v.visitStmtForeach(this, p);
    }

    public StmtForeach(GeneratorFilter [] generators, Statement body) {
        this.generators = generators;
        this.body = body;
    }
    
    public GeneratorFilter[] getGenerators() {
		return generators;
	}

    public Statement getBody() {
        return body;
    }

    private GeneratorFilter []	generators;
    private Statement    		body;
}
