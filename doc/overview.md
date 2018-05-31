# Overview of the Tÿcho Code Base
This is a draft document that will provide an overview of the Tÿcho code base.

## Compilation Flow
This section gives an overview of what happens when the Tÿcho compiler, `tychoc`, is invoked. To compile an entity (i.e. an actor, a network or a process) with the name `Network` in the namespace `com.example`, the Tÿcho compiler can be invoked from the command-line with `tychoc com.example.Network`. Command-line arguments for configuring the compilation can be given to `tychoc`, for example `--source-path src`, to set the path for the source files to `src` instead of the current working directory. When the compiler is invoked, it scans all Cal files in the source path and loads all declarations in the namespace (`com.example`) of the given entity and checks that the given entity (`Network`) is declared in that namespace. The next step in the compilation is to load all depencencies. The namespaces of all imported names are loaded and, in turn, everything they may depend on. When everything has been loaded, the program is analyzed for name and type errors and other problems that can be detected at compiletime. That is the end of what is called the front-end of the compiler, and the intention is that all programs that reach this stage of the compilation should be able to finish successfully.

Networks of actors can be described programmatically using a network language. After the front-end of the compiler is passed, the network is evaluated and elaborated. The network elaborator interprets the network description and builds a graph from it, where nodes are actors or processes and the edges are the connections between their ports. Networks can also be hierarchical, i.e. have nodes that are networks themselves. The elaborator recursively elaborates hierarchical networks to create a single flat graph.

When the network is elaborated, is is known exactly which actors and processes are part of the program. Then follows a transformation of the actors and processes to actor machines. This is the representation on which optimizations are performed.

At this point, the main program is a network graph where all nodes are actor machines. The actor machines may use types and values that are defined outside the actors in the namespaces. The collection of namespaces used by the actor machines together with the main program graph is given to the backend to generate code for a given target platform.

### Source Code Pointers
Most of the source code described in this section is in `compiler/src/main/java`. Here follows pointers to some of the key classes described above. Some of the other classes are described in more detail later in this document.

- The class with the main-method invoked by `tychoc` is `se.lth.cs.tycho.compiler.Main`.
- The compiler class is `se.lth.cs.tycho.compiler.Compiler`.
- Parts of the compiler is specific to the target platform, and the default platform description class is `se.lth.cs.tycho.compiler.platform.C`.
- The class that loads Cal declarations from source files is `se.lth.cs.tycho.compiler.CalLoader`.

## Program Representation
Programs are represented in the compiler by abstract syntax trees. The package `se.lth.cs.tycho.ir`, in the source tree `core/src/main/java`, and its subpackages contains classes and interfaces for representing the different syntactical elements of Cal programs. The subpackages `stmt` and `expr` contain classes for representing statements and expressions, respectively. For example `se.lth.cs.tycho.ir.stmt.StmtWhile` represents a while-statement and `se.lth.cs.tycho.ir.expr.ExprApplication` represents a function application. The subpackage `decl` contains classes for representing declarations of variables, types and entites. (Note that the type declarations are not currently used, but serve as a placeholder for a future addition.) Other subpackages include `types` for type annotations, `entity` for actors, actor machines and network descriptions, and `network` for the flat graph representation of a network.

The abstract syntax trees in Tÿcho are immutable data structures. As a consequence, all program transformations need to build new versions of the tree. However, if a subtree is not affected by a transformation, it can be reused in the new tree. To simplify structural sharing, all node classes should implement a method `copy` with the same parameters as its constructor has, that returns a node with the given children. The `copy` method on a node is allowed to return the node itself if the children given as parameters are the children of the node.

The root node of a tree is called a *compilation task* and is described by `se.lth.cs.tycho.compiler.CompilationTask`. A compilation task contains the qualified identifier of the entity that is compiled (e.g. `com.example.Network`), a list of loaded source files with their abstract syntax trees, and the resulting flat network. Each phase of the compilation takes a compilation task as input and returns a (possibly transformed) compilation task as output.

A common way of keeping a class hierarchy open for adding methods is to use the visitor pattern. However, Tÿcho does not implement the visitor pattern, but uses a library called MultiJ to allow similar extensibility. MultiJ is structured around modules that contain multi-methods (i.e. methods with dispatch on the runtime types of the parameters). A module is defined as a Java interface that is annotated with the `@org.multij.Module` annotation. All methods in a module that have the same name constitute a multi-method. When a multi-method is invoked, MultiJ compares the runtime types of the given arguments to the parameter types of the definitions and selects the most specific implementation. The following example is a module `Example` with one multi-method `foo` consisting of three methods.

```java
@Module
public interface Example {
    default int foo(Object o) {
        return -1;
    }
    default int foo(String s) {
        return s.length();
    }
    default int foo(BitSet s) {
        return s.cardinality();
    }
}
```

The following code shows how to create an instance of the module `Example` and call the multi-method `foo` with different parameters.

```java
Example example = MultiJ.instance(Example.class);
Object a = "hello";
example.foo(a); // returns 5, the length of "hello".
Object b = new BitSet();
example.foo(b); // return 0, the cardinality of the empty bitset.
Object c = Integer.of(42);
example.foo(c); // returns -1, because foo(Object) is selected.
```

Note that the compiletime types of `a`, `b` and `c` are all `Object`, but the method selection of MultiJ uses the runtime types instead. This library can, for example, be used to write a pretty printer for abstract syntax trees.

```java
@Module
public interface Pretty {
    String pretty(Expression expr);
    default String pretty(ExprVariable var) {
        return var.getVariable().getName();
    }
    default String pretty(ExprUnaryOp op) {
        String operand = pretty(op.getOperand());
        return "(" + op.getOperation() + " " + operand + ")";
    }
    // One method for each expression type.
}
```

Here follows an example use of the module `Pretty`.

```java
Expression var = new ExprVariable(Variable.variable("x"));
Expression neg = new ExprUnaryOp("-", var);
Pretty p = MultiJ.instance(Pretty.class);
p.pretty(neg); // returns "(- x)"
```

## Computed Attributes
Some information about the program is not directly represented in the abstract syntax tree, but rather computed from it. A reference from a variable to its declaration is one example, and the value of a constant expression is another. These pieces of information are called *attributes*. Most attributes are defined using MultiJ, but to avoid using a newly introduced concept to describe attributes, the following description uses normal classes and methods instead.

An attribute is defined as a method in a class. A class that defines attributes is called a *module*. A *module key* is an identifier of a module with a method for creating a new instance. The method `CompilationTask.getModule(...)` is responsible for instantiating the modules, and it uses the module key to make sure the same module is instantiated at most once per compilation task. Successive invocations of `getMethod` with the same key returns the same module instance. The following class defines a module `Foo` with an attribute `bar`. The value of `bar` is the same for all nodes, and is a randomly chosen integer.

```java
public class Foo {
    // The key should be a singleton object.
    public static final ModuleKey<Foo> key = t -> new Foo();

    private final int number;

    public Foo() {
        number = new java.util.Random().nextInt();
    }

    // Attribute definition
    public int bar(IRNode node) {
        // Always returns the same, randomly chosen, value.
        return number;
    }
}
```

The following code creates an instance of the module `Foo` for a compilation task and computes the attribut `bar` on a node.

```java
CompilationTask task = ...;
Foo foo = task.getModule(Foo.key);
IRNode node = ...;
int nodeBar = foo.bar(node);
```

Some of the more interesting attributes that can be computed, for example the declaration of a variable, require information from parents in the abstract syntax tree. For this purpose, a module `TreeShadow` exists, that collects the parent links of the tree. The `TreeShadow` module has an attribute `parent` that returns the parent of a node. The following code shows a module that depends on the TreeShadow module.

```java
public class Environment {
    private final TreeShadow tree;

    public Environment(ComputationTask task) {
        tree = task.getModule(TreeShadow.key);
    }
    
    public CalActor enclosingCalActor(IRNode node) {
        IRNode parent = tree.parent(node);
        while(parent != null) {
            if (parent instanceof CalActor) {
                return (CalActor) parent;
            }
            parent = tree.parent(parent);
        }
        return null;
    }
}
```

If modules have circular dependencies, the dependencies cannot be instantiated in the constructor like in the example above. One solution is to keep a reference to the computation task and instantiate the dependencies on demand.

### Memoized attributes
Attributes that are referentially transparent can be memoized in the moduels. Since the modules are instantiated for a specific tree, computed attribute values can be stored directly in the module instance for later reuse. If a tree is transformed, it will get new root, and modules that are instantiated for this new tree will not have any stored attribute values. Even attributes that depend on their parents may be memoized because the value is stored in relation to the root of the tree.

Using MultiJ, attributes can be manually stored in a map by introducing a `@Binding`. In the following example, the method `cache` is evaluated once on its first invocation and on successive invocations returns the same object.

```java
@Module
interface MemoizedFunction {
    @Binding
    default Map<IRNode, String> cache() {
        return new ConcurrentHashMap<>();
    }

    default String memoizedFunction(IRNode node) {
        return cache.computeIfAbsent(node, this::definition);
    }

    default String definition(IRNode node);

    // One definition(...) for each subtype of IRNode
}
```

Here, `memoizedFunction` is the attribute that and `definition` is the multi-method that computes its value.

## Compiler Phases
### Section Description
This section shoud describe how compiler phases are represented. The following parts should be discussed:

- how a phase analyzes transforms a compilation task,
- command-line options for a phase,
- compilation context, and
- the interface `Phase`.

## Platforms
### Section Description
This section should describe how different target platforms can be handled. It should describe what constitutes a platform and how a add another platform to Tÿcho.
