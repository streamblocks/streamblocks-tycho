package ch.epfl.vlsc.tycho.lsp4j;

import se.lth.cs.tycho.parsing.cal.CalParserConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CAL Token Id
 *
 * @author Endri Bezati
 */
public enum CalTokenId {

    EOF(CalParserConstants.EOF),
    MULTI_LINE_COMMENT(CalParserConstants.MULTI_LINE_COMMENT),
    ACTION(CalParserConstants.ACTION),
    ACTOR(CalParserConstants.ACTOR),
    ALL(CalParserConstants.ALL),
    AND(CalParserConstants.AND),
    BEGIN(CalParserConstants.BEGIN),
    CONST(CalParserConstants.CONST),
    DIV(CalParserConstants.DIV),
    DO(CalParserConstants.DO),
    DOM(CalParserConstants.DOM),
    ELSE(CalParserConstants.ELSE),
    ELSIF(CalParserConstants.ELSIF),
    END(CalParserConstants.END),
    ENDACTION(CalParserConstants.ENDACTION),
    ENDACTOR(CalParserConstants.ENDACTOR),
    ENDCHOOSE(CalParserConstants.ENDCHOOSE),
    ENDFOREACH(CalParserConstants.ENDFOREACH),
    ENDFUNCTION(CalParserConstants.ENDFUNCTION),
    ENDIF(CalParserConstants.ENDIF),
    ENDINITIALIZE(CalParserConstants.ENDINITIALIZE),
    ENDLAMBDA(CalParserConstants.ENDLAMBDA),
    ENDLET(CalParserConstants.ENDLET),
    ENDPRIORITY(CalParserConstants.ENDPRIORITY),
    ENDPROC(CalParserConstants.ENDPROC),
    ENDPROCEDURE(CalParserConstants.ENDPROCEDURE),
    ENDSCHEDULE(CalParserConstants.ENDSCHEDULE),
    ENDWHILE(CalParserConstants.ENDWHILE),
    ENTITY(CalParserConstants.ENTITY),
    FALSE(CalParserConstants.FALSE),
    FOR(CalParserConstants.FOR),
    FOREACH(CalParserConstants.FOREACH),
    FSM(CalParserConstants.FSM),
    FUNCTION(CalParserConstants.FUNCTION),
    GUARD(CalParserConstants.GUARD),
    IF(CalParserConstants.IF),
    IMPORT(CalParserConstants.IMPORT),
    IN(CalParserConstants.IN),
    INITIALIZE(CalParserConstants.INITIALIZE),
    LAMBDA(CalParserConstants.LAMBDA),
    LET(CalParserConstants.LET),
    MAP(CalParserConstants.MAP),
    MOD(CalParserConstants.MOD),
    MULTI(CalParserConstants.MULTI),
    MUTABLE(CalParserConstants.MUTABLE),
    NAMESPACE(CalParserConstants.NAMESPACE),
    NOT(CalParserConstants.NOT),
    NULL(CalParserConstants.NULL),
    OLD(CalParserConstants.OLD),
    OR(CalParserConstants.OR),
    PRIORITY(CalParserConstants.PRIORITY),
    PROC(CalParserConstants.PROC),
    PROCEDURE(CalParserConstants.PROCEDURE),
    REGEXP(CalParserConstants.REGEXP),
    REPEAT(CalParserConstants.REPEAT),
    RNG(CalParserConstants.RNG),
    SCHEDULE(CalParserConstants.SCHEDULE),
    THEN(CalParserConstants.THEN),
    TRUE(CalParserConstants.TRUE),
    TYPE(CalParserConstants.TYPE),
    VAR(CalParserConstants.VAR),
    WHILE(CalParserConstants.WHILE),
    PUBLIC(CalParserConstants.PUBLIC),
    PRIVATE(CalParserConstants.PRIVATE),
    LOCAL(CalParserConstants.LOCAL),
    NETWORK(CalParserConstants.NETWORK),
    ENTITIES(CalParserConstants.ENTITIES),
    STRUCTURE(CalParserConstants.STRUCTURE),
    EXTERNAL(CalParserConstants.EXTERNAL),
    COLON(CalParserConstants.COLON),
    DOT(CalParserConstants.DOT),
    COMMA(CalParserConstants.COMMA),
    LONG_DOUBLE_ARROW_RITHT(CalParserConstants.LONG_DOUBLE_ARROW_RITHT),
    LONG_SINGLE_ARROW_RIGHT(CalParserConstants.LONG_SINGLE_ARROW_RIGHT),
    LONG_SINGLE_ARROW_LEFT(CalParserConstants.LONG_SINGLE_ARROW_LEFT),
    LPAREN(CalParserConstants.LPAREN),
    RPAREN(CalParserConstants.RPAREN),
    LCURLY(CalParserConstants.LCURLY),
    RCURLY(CalParserConstants.RCURLY),
    LSQUARE(CalParserConstants.LSQUARE),
    RSQUARE(CalParserConstants.RSQUARE),
    EQ(CalParserConstants.EQ),
    COLON_EQ(CalParserConstants.COLON_EQ),
    SINGLE_ARROW_RIGHT(CalParserConstants.SINGLE_ARROW_RIGHT),
    VERTICAL_BAR(CalParserConstants.VERTICAL_BAR),
    GREATER_THAN(CalParserConstants.GREATER_THAN),
    STAR(CalParserConstants.STAR),
    DOT_STAR(CalParserConstants.DOT_STAR),
    CINNAMON_BUN(CalParserConstants.CINNAMON_BUN),
    OP(CalParserConstants.OP),
    OP_CHAR(CalParserConstants.OP_CHAR),
    STRING(CalParserConstants.STRING),
    INTEGER(CalParserConstants.INTEGER),
    REAL(CalParserConstants.REAL),
    DECIMAL_LITERAL(CalParserConstants.DECIMAL_LITERAL),
    HEXADECIMAL_LITERAL(CalParserConstants.HEXADECIMAL_LITERAL),
    OCTAL_LITERAL(CalParserConstants.OCTAL_LITERAL),
    EXPONENT(CalParserConstants.EXPONENT),
    NON_ZERO_DECIMAL_DIGIT(CalParserConstants.NON_ZERO_DECIMAL_DIGIT),
    DECIMAL_DIGIT(CalParserConstants.DECIMAL_DIGIT),
    OCTAL_DIGIT(CalParserConstants.OCTAL_DIGIT),
    HEXADECIMAL_DIGIT(CalParserConstants.HEXADECIMAL_DIGIT),
    ID(CalParserConstants.ID);


    private static final Map<Integer, CalTokenId> lookup = new HashMap<>();

    static {
        for (CalTokenId t : CalTokenId.values()) {
            lookup.put(t.getId(), t);
        }
    }


    private Integer id;

    CalTokenId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }


    public static CalTokenId getById(Integer id) {
        if (lookup.containsKey(id)){
            return lookup.get(id);
        }else{
            return null;
        }
    }

}
