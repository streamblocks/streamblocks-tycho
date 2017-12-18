# Tycho Compiler
Tycho is a compiler for dataflow programs that supports most of Cal, RVC-CAL and a language for Kahn processes.

# Example
The following actor reads two streams of numbers and produces a stream that is the pairwise sum of two incoming streams.
```
actor Add () uint(size=8) X, uint(size=8) Y ==> uint(size=8) Z :
    action X:[x], Y:[y] ==> Z:[x + y] end
end
```

# Installation
To build Tycho, you need the following:

* Java SE Development Kit 8 (or later)
* Apache Maven
* Git
* C compiler (for example Clang or GCC)

Tycho is installed using the following commands:
```
git clone https://bitbucket.org/dataflow/dataflow.git tycho
cd tycho
mvn install
cd ..
```

To make it easier to invoke the Tycho compiler, you can symlink `tychoc` to a file in your PATH.

# Running the compiler
To compile an actor called `Add` in a file called `source/arith.cal` and generate the output to the folder `target` you invoke:
```
tychoc --source-path source --target-path target Add
```
You can then compile the generated code.
```
cc target/*.c -o Add
```
Given two input files `in-1` and `in-2` you can ge the pairwise sum of their byte streams in a file `out` by running the program.
```
./Add in-1 in-2 out
```
Use the `--help` flag to see more available options.
```
tychoc --help
```
