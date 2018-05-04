# Overview of the Tÿcho Code Base
This is a draft document that will provide an overview of the Tÿcho code base.

## Compilation Flow
The Tÿcho compiler can be invoked using `tychoc com.example.Network` to compile the entity (i.e. actor, network or process) with the name `Network` in the namespace `com.example`. It accepts command line arguments for configuring the compilation, for example `--source-path src`, which sets the path for the source files to `src` instead of the current directory. When the compiler is invoked, it scans all Cal files in the source path and loads all declarations in the namespace (`com.example`) of the given entity and checks that the given entity is declared in that namespace. The next step in the compilation is to load all depencencies. The namespaces of all imported names are loaded and, in turn, everything they may depend on. When everything is loaded, the program is analyzed using standard static analyses, such as name analysis and type analysis. This is the end of what is called the front-end of the compiler, and the intention is that all programs that reach this stage of the compilation should be able to finish successfully.

After the front-end of the compiler is the network elaborator. Its task is to interpret the network description to build a single graph from it, where nodes are actors or processes and the edges are the connections between their ports. Network descriptions can be nested, having entities being network descriptions themselves. The elaborator recursively elaborates nested networks to create a single flat graph.

When the network is elaborated, is is known exactly which actors and processes are part of the program. Then follows a transformation of the actors and processes to actor machines. Optimizations are then performed on this representation.

At this point, the main program is represented as a graph where all nodes are actor machines. The actor machines, in turn, refers to types and values that are defined in the namespaces. This collection of namespaces together with the main program graph is given to the backend to generate code from.

## Program Representation
### Section Description
This section should describe how stream programs are represented in Tÿcho. The following concepts should be discussed:

- tree structure and attribution,
- immutabilily and structural sharing,
- declarations, statements and expressions and their respective classes, and
- compilation tasks.

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
