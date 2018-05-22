# Overview of the Tÿcho Code Base
This is a draft document that will provide an overview of the Tÿcho code base.

## Compilation Flow
This section gives an overview of what happens when the Tÿcho compiler, `tychoc`, is invoked. To compile an actor or network with the name `Network` in the namespace `com.example`, the Tÿcho compiler can be invoked from the command-line with `tychoc com.example.Network`. Command-line arguments for configuring the compilation can be given to `tychoc`, for example `--source-path src`, to set the path for the source files to `src` instead of the current working directory. When the compiler is invoked, it scans all Cal files in the source path and loads all declarations in the namespace (`com.example`) of the given entity and checks that the given entity is declared in that namespace. The next step in the compilation is to load all depencencies. The namespaces of all imported names are loaded and, in turn, everything they may depend on. When everything is loaded, the program is analyzed for name and type errors and other problems that can be detected at compiletime. This is the end of what is called the front-end of the compiler, and the intention is that all programs that reach this stage of the compilation should be able to finish successfully.

After the front-end of the compiler is passed, the network is elaborated. The network elaborator interprets the network description to build a graph from it, where nodes are actors or processes and the edges are the connections between their ports. Network descriptions can be nested, having entities being network descriptions themselves. The elaborator recursively elaborates nested networks to create a single flat graph.

When the network is elaborated, is is known exactly which actors and processes are part of the program. Then follows a transformation of the actors and processes to actor machines. This is the representation on which optimizations are performed.

At this point, the main program is a network graph where all nodes are actor machines. The actor machines may use types and values that are defined outside the actors in the namespaces. The collection of namespaces used by the actor machines together with the main program graph is given to the backend to generate code for a given target platform.

### Source Code Pointers
Most of the source code described in this section is in `compiler/src/main/java`. Here follows pointers to some of the key classes described above. Some of the other classes are described in more detail later in this document.

- The class with the main-method invoked by `tychoc` is `se.lth.cs.tycho.compiler.Main`.
- The compiler class is `se.lth.cs.tycho.compiler.Compiler`.
- Parts of the compiler is specific to the target platform, and the default platform description class is `se.lth.cs.tycho.compiler.platform.C`.
- The class that loads Cal declarations from source files is `se.lth.cs.tycho.compiler.CalLoader`.

## Program Representation
Cal programs are represented in the compiler by their abstract syntax trees. The package `se.lth.cs.tycho.ir`, in the source tree `core/src/main/java`, and its subpackages contains classes and interfaces for representing the different syntactical elements of a Cal program. The subpackages `stmt` and `expr` contain classes for representing statements and expressions, respectively. For example `se.lth.cs.tycho.ir.stmt.StmtWhile` represents a while-statement and `se.lth.cs.tycho.ir.expr.ExprApplication` represents a function application. The subpackage `decl` contains classes for representing declarations of variables, types and entites. (Note that the type declarations are not currently used, but serve as a placeholder for future addition.) Other subpackages include `types` for type annotations, `entity` for actors, actor machines and network descriptions, and `network` for the flat graph representation of a network.

The abstract syntax trees in Tÿcho are immutable data structures. As a consequence, all program transformations need to build new versions of the tree. However, if a subtree is not affected by a transformation, it can be reused in the new tree. To simplify structural sharing, all node classes should implement a method `copy` with the same parameters as its constructor has, that returns a node with the given children. The `copy` method on a node is allowed to return the node itself if the children given as parameters are the children of the node.

The root node of a tree is called a *compilation task* and is described by `se.lth.cs.tycho.compiler.CompilationTask`. A compilation task contains the qualified identifier of the entity that is compiled (e.g. com.example.Network), a list of loaded source files and the resulting flat network. Each phase of the compilation takes a compilation task as input and returns a (possibly transformed) compilation task as output.

A common way of keeping a class hierarchy open for adding methods is to use the visitor pattern. However, Tÿcho does *not* implement the visitor pattern, but uses a library called MultiJ to allow the same kind of extensibility. A MultiJ module is a Java interface with default methods that is annotated with the `@org.multij.Module` annotation. Methods that have the same name but different parameter types constitutes a multi-method. When a module is compiled, a new class is generated that implements dynamic dispatch on the parameter types of all multi-methods in a module. The following example is a module with one multi-method `foo` consisting of three methods.

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

When the multi-method is called with some parameters, the actual types of the parameters are compared to the parameter types of the definitions to find the most specific definition for the given parameters. Here follows a small example that creates an instance of the module `Example` and calls the multi-method `foo` with some parameters.

```java
Example example = MultiJ.instance(Example.class);
Object a = "hello";
example.foo(a); // returns 5, the length of "hello".
Object b = new BitSet();
example.foo(b); // return 0, the cardinality of the empty bitset.
Object c = Integer.of(42);
example.foo(c); // returns -1, because foo(Object) is selected.
```

Note that the static type of `a`, `b` and `c` is `Object`, but the method selection of MultiJ depends on the object types instead. This library can, for example, be used to write a pretty printer for abstract syntax trees.

```java
@Module
public interface Pretty {
    String pretty(Expression expr);
    default String pretty(ExprVariable var) {
        return var.getVariable().getName();
    }
    default String pretty(ExprUnaryOp op) {
        return "(" + op.getOperand() + " " + pretty(op.getOperand()) + ")";
    }
    // One method for each expression type.
}
```

Here follows an example use of the module `Pretty`.

```java
Expression e = new ExprUnaryOp("-", new ExprVariable(Variable.variable("x")));
Pretty p = MultiJ.instance(Pretty.class);
p.pretty(e); // returns "(- x)"
```

## Computed Attributes
Some information about the program is not directly represented in the abstract syntax tree, but rather computed from it. A reference from a variable to its declaration is one example, and the value of a constant expression is another. These pieces of information are called *attributes*.

An attribute is defined as an instance method in a class. A class that defines attributes is called a *module*. A module key is an object that describes how to instantiate a module. The `CompilationTask` has a method `getModule` for instantiating modules for a given key, that creates at most one instance per key and compilation task. Successive invocations of `getMethod` on the same compilation task and with the same key returns the first instantiation on all invocations. The following class defines a module `Foo` with an attribute `bar`. The value of `bar` is the same for all nodes, and is a randomly chosen integer.

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

If another module depends on `Foo`, its initializer needs to get `Foo` using `CompilationTask.getModule` to make sure only one instance of `Foo` is used for each compilation task. Here follows a module `Baz` with an attribute `quux` that is equal to `bar` in `Foo`.

```java
public class Baz {
    public static final ModuleKey<Baz> key = Baz::new;

    private final Foo foo;

    public Baz(CompilationTask task) {
        // The dependency on Foo is injected here
        foo = task.getModule(Foo.key);
    }

    public int quux(IRNode node) {
        // Returns the value of bar.
        return foo.bar(node);
    }
}
```

The instantiation of `Baz` triggers the instantiation of `Foo` iff `Foo` is not already instantiated. The following code asserts that `Baz.quux` and `Foo.bar` of a node are the same.

```java
CompilationTask task = ...;
Baz baz = task.getModule(Baz.key); // Foo is indirectly instantiated
Foo foo = task.getModule(Foo.key); // That Foo is retrieved here
IRNode node = ...;
int nodeQuux = baz.quux(node); // Indirectly evaluates Foo.bar
int nodeBar = foo.bar(node); // This should therefore be the same
assert(nodeQuux == nodeBar);
```

Some of the more interesting attributes that can be computed, for example the declaration of a variable, requires information from parents in the abstract syntax tree. For this purpose, a module `TreeShadow` exists, that collects the parent links of the tree. The `TreeShadow` module has an attribute `parent` that returns the parent of a node.

Most attribute modules are defined as MultiJ modules because they typically require different definitions for different node types.

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
