StreamBlocks Tycho Compiler  Repository
=======================================

Welcome to the StreamBlocks-Tycho compiler repository. This repository contains the frontend of the StreamBlocks dataflow compiler.

This README file is organized as follows:
1. Getting started
2. How to download this repository
3. Dependencies
4. Installation
5. StreamBlocks Platforms
6. Original Tycho repository
7. Support


### 1. Getting Started

StreamBlocks-Tycho is a compiler for dataflow programs that supports most of Cal, RVC-CAL and a language for Kahn processes.
This repository contains the frontend and the compiler infrastructure of the StreamBlocks-Tycho dataflow compiler and an example 
backend for C code generation.


### 2. How to download this repository

To get a local copy of the StreamBlocks-Tycho repository, clone this repository to the local system with the following commmand:
```
git clone https://github.com/streamblocks/streamblocks-tycho streamblocks-tycho
```

### 3. Dependencies

To build Tycho, you need the following:

* Java SE Development Kit 8 (or later)
* Apache Maven
* Git

### 4. Installation

StreamBlocks Tycho is installed using the following commands:
```
git clone https://github.com/streamblocks/streamblocks-tycho streamblocks-tycho
cd streamblocks-tycho
mvn -DskipTests install
```

### 5. StreamBlocks Platforms
The StreamBlocks dataflow compiler has a code generator for multicore platforms and another one for reconfigurable computing using High-Level synthesis.
The repository for those code-generators are located at the following link : [streamblocks-platforms](https://github.com/streamblocks/streamblocks-platforms/blob/master/README.md).

### 6. Original Tycho dataflow compiler repository

Tycho dataflow compiler compiler was developed by Lund University and the original repository is located [here](https://bitbucket.org/dataflow/dataflow/).

### 7. Support

If you have an issue with StreamBlocks-Tycho dataflow compiler please create a new issue in this repository.
