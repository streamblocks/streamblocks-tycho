# Module system for Cal

This document describes a proposed module system for Cal.

## Problem

Because Cal is targeting both hardware and software, its type system includes integers of many different sizes. The standard integer type `int` is, in fact, parameterized on the number of bits used for its representation, e.g. `int(size=8)` for an integer represented by 8 bits. When defining a function on integers, it will be defined for one size of integers. The absolute-value function for integers of size eight, for example, cannot be applied to integers of size nine or sixteen.

This document proposes a module system for Cal that provides polymorphism through static dispatch, making it possible to use the same name of a function for different types of its parameters. The absolute value function `abs`, for example, can be defined for both `int(size=8)` and `int(size=9)` and even for `float`.

## Module definition

Modules are structured in a hierarchy that defines which modules implement which modules. A module consists of
- a name,
- a list of parameters,
- a list of modules that it implements, and
- a list of declarations.

A simple example looks like follows.
```
module Add(type t) :
  function add(t x, t y) --> t end
end
```
The `Add` module has a type parameter `t` and a value member of a function type taking two values of type `t` as parameters and returing a value of type `t`. The list of modules it implements is empty.

An implementation of this module where `t` is bound to `float` looks like follows.
```
module FloatAdd() implements Add(t: float) :
  function add(float x, float y) --> float :
    native_float_add(x, y)
  end
end
```
The `FloatAdd` module implements `Add` where the parameter `t` is bound to `float`. Its member `add` delegates the operation to another function. Members of a module cannot be changed, e.g. the variable `add` is constant and cannot be assigned to. The `implements` relation is a partial order on modules, i.e. cyclic `implements`-definitions are errors.

## Module instantiation

To use a module, it needs to be in scope. It can be brought to scope by an import declaration.
```
import module Add;
import module FloatAdd;
```
When in scope, their members can be accessed using the following syntax:
```
import module FloatAdd;
float x = FloatAdd()::add(3.0, 5.2);
```
In this example, the module `FloatAdd` is brought into scope with the import statement, then an instance of `FloatAdd` is retrieved and its member function `add` is applied to the values `3.0` and `5.2`, and the result is bound to the variable `x`.

## Module lookup

A module implementation can also be selected by supplying parameters to the base module. All modules for which that parameterization matches are considered, but a module `Bar` that implements a module `Foo` takese precedence over `Foo`. If there is more than one module left, it is an error.
```
import module Add;
import module FloatAdd;
float x = Add(t: float)::add(3.0, 5.2);
```
In this example, the compiler will look up all implementations of `Add` that are in scope for which `t` can be bound to `float`, i.e. `Add` and `FloatAdd`. Since `FloatAdd` implements `Add`, `FloatAdd` will be selected.

## Module parameter inference

In some cases, the parameters of a module instantiation can be determined by how its members are used.
```
import module Add;
import module FloatAdd;
float x = Add()::add(3.0, 5.2);
```
In this example, `3.0` and `5.2` are of type `float`, and by type inference the parameter `t` of `Add` can be computed to be `float`. If the compiler can determine all parameters of the module by how the member is used, then the expression is valid. If some parameters cannot be inferred by the compiler, they must be provided in the code.

## Modules with special status

Some modules are known by the compiler and define special language constructs. The `Add` module, for example, defines the `+` operation. The addition `3.0 + 5.2` is textually transformed to `Add()::add(3.0, 5.2)`, after which the normal parameter inference, module lookup and module instantiation is performed, resulting in `FloatAdd::add(3.0, 5.2)` if `FloatAdd` is the most specific module in scope.

## Examples

```
actor Add(type t, module add : Add(t: t)) t X, t Y ==> t Z :
  action X:[x], Y:[y] ==> Z:[add::add(x, y)] end
end

import module IntAdd; // could be imported by default
import module FloatAdd; // could be imported by default

// Use the same Add for different types.
network Foo () ==> :
entities
  addInt = Add(t: int(size=8));
  addFloat = Add(t: float);
  ...
structure
  ...
end

// Parameterize the whole network with an addable type.
network Bar (type t, module add : Add(t:t)) ==> :
entities
  add1 = Add(t:t)
  add2 = Add(t:t)
structure
  ...
end
```

## Syntax

```
module instance = id, '(', parameter assignments, ')' ;
module member = module instance , '::', id ;
module declaration = 'module', id, '(', parameter declarations, ')',
    [ 'implements', module instance, {',' module instance} ], ':',
    declarations,
    'end' ;
```
