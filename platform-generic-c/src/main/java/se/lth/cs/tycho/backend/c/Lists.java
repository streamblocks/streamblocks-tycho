package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.ListType;
import se.lth.cs.tycho.type.Type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module
public interface Lists {
    @Binding(BindingKind.INJECTED)
    Backend backend();

    default Emitter emitter() {
        return backend().emitter();
    }

    default void declareListTypes() {
        emitter().emit("// LIST DECLARATIONS");

        // 2023/01/02: Start of code added by Gareth Callanan to fix an error where nested list types could be
        // declared containing an inner value of type list but that type had not been declared yet.
        //
        // In the logic below, we order the list declarations by number of nested lists ascending to prevent this
        // use before declaration error.

        // 1. Get a list of all list declarations
        List<ListType> lists = listTypes().collect(Collectors.toList());

        // 2. Sort the list using a lambda.
        Collections.sort(lists, (o1, o2) -> (getListNestingDepth(o1) > getListNestingDepth(o2)) ? 1 :
                (getListNestingDepth(o1) < getListNestingDepth(o2)) ? -1 : 0);

        // 3. Declare the sorted lists
        for (ListType listType : lists) {
            declareType(listType);
        }

        // 2023/01/02: End of code added by Gareth Callanan
    }

    default void declareType(ListType type) {
        String typeName = backend().code().type(type);
        String elementType = backend().code().type(type.getElementType());
        if (type.getElementType() instanceof AlgebraicType || backend().alias().isAlgebraicType(type.getElementType()))
            elementType += "*";
        int size = type.getSize().getAsInt();

        emitter().emit("typedef struct {");
        emitter().increaseIndentation();
        emitter().emit("%s data[%d];", elementType, size);
        emitter().decreaseIndentation();
        emitter().emit("} %s;", typeName);
    }

    default Stream<ListType> listTypes() {
        return backend().task().walk()
                .flatMap(this::listType)
                .distinct();
    }

    default Stream<ListType> listType(IRNode node) {
        return Stream.empty();
    }

    default Stream<ListType> listType(VarDecl decl) {
        return wrapIfList(backend().types().declaredType(decl));
    }

    default Stream<ListType> listType(Expression expr) {
        Type t = backend().types().type(expr);
        return wrapIfList(t);
    }

    default Stream<ListType> wrapIfList(Type t) {
        return Stream.empty();
    }

    default Stream<ListType> wrapIfList(ListType t) {
        return Stream.of(t);
    }

    /**
     * Find the number of nested lists within a listType object.
     *
     * @param listType List type obect to find the nesting of.
     * @return Number of nested lists within this list.
     */
    default int getListNestingDepth(ListType listType) {
        int nestingLevel = 0;
        ListType currentListType = listType;

        // Recursively search through the types of the lists until the type of the list is not itself a ListType.
        while (currentListType.getElementType() instanceof ListType) {
            nestingLevel++;
            currentListType = (ListType) currentListType.getElementType();
        }

        return nestingLevel;
    }
}
