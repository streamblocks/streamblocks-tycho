# Overview of the Tÿcho Code Base
This is a draft document that will provide an overview of the Tÿcho code base.

## Compilation Flow
The Tÿcho compiler can be invoked using `tychoc com.example.Network` to compile the entity (i.e. actor, network or process) with the name `Network` in the namespace `com.example`. It accepts command line arguments for configuring the compilation, for example `--source-path src`, which sets the path for the source files to `src` instead of the current directory. When the compiler is invoked, it scans all Cal files in the source path and loads all declarations in the namespace (`com.example`) of the given entity and checks that the given entity is declared in that namespace. The next step in the compilation is to load all depencencies. The namespaces of all imported names are loaded and, in turn, everything they may depend on. When everything is loaded, the program is analyzed using standard static analyses, such as name analysis and type analysis. This is the end of what is called the front-end of the compiler, and the intention is that all programs that reach this stage of the compilation should be able to finish successfully.

After the front-end of the compiler is the network elaborator. Its task is to interpret the network description to build a single graph from it, where nodes are actors or processes and the edges are the connections between their ports. Network descriptions can be nested, having entities being network descriptions themselves. The elaborator recursively elaborates nested networks to create a single flat graph.

When the network is elaborated, is is known exactly which actors and processes are part of the program. Then follows a transformation of the actors and processes to actor machines. Optimizations are then performed on this representation.

At this point, the main program is represented as a graph where all nodes are actor machines. The actor machines, in turn, refers to types and values that are defined in the namespaces. This collection of namespaces together with the main program graph is given to the backend to generate code from.

### Source Code Pointers
Most of the source code described in this section is in `compiler/src/main/java`. Here follows pointers to some of the key classes described above. Some of the other classes are described in more detail later in this document.

- The class with the main-method invoked by `tychoc` is `se.lth.cs.tycho.compiler.Main`.
- The compiler class is `se.lth.cs.tycho.compiler.Compiler`.
- Parts of the compiler is specific to the target platform, and the default platform description class is `se.lth.cs.tycho.compiler.platform.C`.
- The class that loads Cal declarations from source files is `se.lth.cs.tycho.compiler.CalLoader`.

## Program Representation
Cal programs are represented in the compiler by their abstract syntax trees. The package `se.lth.cs.tycho.ir`, in the source tree `core/src/main/java`, and its subpackages contains classes and interfaces for representing the different syntactical elements of a Cal program. The subpackages `stmt` and `expr` contain classes for representing statements and expressions, respectively. For example `se.lth.cs.tycho.ir.stmt.StmtWhile` represents a while-statement and `se.lth.cs.tycho.ir.expr.ExprApplication` represents a function application. The subpackage `decl` contains classes for representing declarations of variables, types and entites. (Note that the type declarations are not currently used, but serve as a placeholder for future addition.) Other subpackages include `types` for type annotations, `entity` for actors, actor machines and network descriptions, and `network` for the flat graph representation of a network.

The abstract syntax trees in Tÿcho are immutable data structures. As a consequence, all program transformations need to build new versions of the tree. However, if a subtree is not affected by a transformation, it can be reused in the new tree. To simplify structural sharing, all node classes should implement a method `copy` with the same parameters as its constructor has, that returns a node with the given children. The `copy` method on a node is allowed to return the node itself if the children given as parameters are the children of the node.

### TODO Write about MultiJ as an alternative to visitors.

### TODO Write about the compilation task

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
Baz baz = task.getModule(Baz.key); // Foo is indirectly instantiated.
Foo foo = task.getModule(Foo.key); // That Foo is retrieved here.
IRNode node = ...;
int nodeQuux = baz.quux(node); // Indirectly evaluates Foo.bar.
int nodeBar = foo.bar(node); // This should therefore be the same.
assert(nodeQuux == nodeBar);
```

Some of the more interesting attributes that can be computed, for example the declaration of a variable, requires information from parents in the abstract syntax tree. For this purpose, a module `TreeShadow` exists, that collects the parent links of the tree. The `TreeShadow` module has an attribute `parent` that returns the parent of a node.

### TODO Write about how to store computed attribute values.
### TODO Write about some of the common attribute modules.

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
