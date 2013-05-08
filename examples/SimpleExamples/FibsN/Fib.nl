network Fib () ==> Out:

entities
  add = Add();
  z0 = Z(v=1);
  z1 = Z(v=1);

structure
  z0.Out --> add.A;
  z1.Out --> add.B;
  z0.Out --> z1.In;
  add.Out --> z0.In;
  add.Out --> Out;
end
