# Overview of the Tÿcho Code Base
This is a draft document that will provide an overview of the Tÿcho code base.

## Compilation Flow
This section should describe the compilation flow from stream program source code to generated code. The following stages will be discussed:
- parsing,
- namespaces and imports,
- analysis,
- network elaboration,
- actor translation, and
- code generation.

## Program Representation
This section should describe how stream programs are represented in Tÿcho. The following concepts should be discussed:
- tree structure and attribution,
- immutabilily and structural sharing,
- declarations, statements and expressions and their respective classes, and
- compilation tasks.

## Compiler Phases
This section shoud describe how compiler phases are represented. The following parts should be discussed:
- how a phase analyzes transforms a compilation task,
- command-line options for a phase,
- compilation context, and
- the interface `Phase`.

## Platforms
This section should describe how different target platforms can be handled. It should describe what constitutes a platform and how a add another platform to Tÿcho.
