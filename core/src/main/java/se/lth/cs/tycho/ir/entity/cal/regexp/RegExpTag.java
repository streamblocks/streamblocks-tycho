package se.lth.cs.tycho.ir.entity.cal.regexp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.function.Consumer;

public class RegExpTag extends RegExp {

    private final QID qid;

    public RegExpTag(QID qid) {
        this(null, qid);
    }

    private RegExpTag(RegExpTag original, QID qid) {
        super(original);
        this.qid = qid;
    }

    public QID getQID() {
        return qid;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
    }

    @Override
    public RegExp transformChildren(Transformation transformation) {
        return this;
    }
}
